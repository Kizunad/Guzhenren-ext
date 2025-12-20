package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 光道高转被动：持续性维持（多资源）+ 太光普照（范围显形）+ 提升 Guzhenren 字段上限。
 * <p>
 * 通过 metadata 配置：<br>
 * - zhenyuan_base_cost_per_second / niantou_cost_per_second / jingli_cost_per_second / hunpo_cost_per_second<br>
 * - radius（作用半径）<br>
 * - glowing_duration_ticks（显形持续时间）<br>
 * - ignore_walls（是否无视视线）<br>
 * - cap_specs：由构造器注入各字段及其基础加成 meta key<br>
 * </p>
 */
public class GuangDaoSustainedRevealAndCapEffect implements IGuEffect {

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";
    private static final String META_NIANTOU_COST_PER_SECOND =
        "niantou_cost_per_second";
    private static final String META_JINGLI_COST_PER_SECOND =
        "jingli_cost_per_second";
    private static final String META_HUNPO_COST_PER_SECOND =
        "hunpo_cost_per_second";

    private static final String META_RADIUS = "radius";
    private static final String META_GLOWING_DURATION_TICKS =
        "glowing_duration_ticks";
    private static final String META_IGNORE_WALLS = "ignore_walls";

    private static final double DEFAULT_COST = 0.0;
    private static final double DEFAULT_RADIUS = 0.0;
    private static final int DEFAULT_DURATION_TICKS = 0;

    private static final int TICKS_PER_SECOND = 20;
    private static final int MAX_DURATION_SECONDS = 10 * 60;
    private static final int MIN_DURATION_TICKS = 1;
    private static final int MAX_DURATION_TICKS =
        TICKS_PER_SECOND * MAX_DURATION_SECONDS;

    private final String usageId;
    private final List<CapSpec> caps;

    public GuangDaoSustainedRevealAndCapEffect(
        final String usageId,
        final List<CapSpec> caps
    ) {
        this.usageId = usageId;
        this.caps = caps == null ? List.of() : List.copyOf(caps);
    }

    @Override
    public String getUsageId() {
        return usageId;
    }

    @Override
    public void onSecond(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(usageId)) {
            setActive(user, false);
            clearAll(user);
            return;
        }

        final double zhenyuanBaseCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double niantouCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_NIANTOU_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double jingliCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_JINGLI_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double hunpoCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HUNPO_COST_PER_SECOND,
                DEFAULT_COST
            )
        );

        if (
            !GuEffectCostHelper.tryConsumeSustain(
                user,
                niantouCostPerSecond,
                jingliCostPerSecond,
                hunpoCostPerSecond,
                zhenyuanBaseCostPerSecond
            )
        ) {
            setActive(user, false);
            clearAll(user);
            return;
        }

        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.GUANG_DAO
        );
        applyCaps(user, usageInfo, multiplier);
        applyReveal(user, usageInfo, multiplier);

        setActive(user, true);
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }
        setActive(user, false);
        clearAll(user);
    }

    private void applyReveal(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        final double baseRadius = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        final int baseDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_GLOWING_DURATION_TICKS,
                DEFAULT_DURATION_TICKS
            )
        );
        if (baseDuration <= 0 || baseRadius <= 0.0) {
            return;
        }

        final double radius = Math.max(0.0, baseRadius * Math.max(0.0, multiplier));
        final int duration = scaleDuration(baseDuration, multiplier);
        final boolean ignoreWalls = UsageMetadataHelper.getBoolean(
            usageInfo,
            META_IGNORE_WALLS,
            false
        );

        final AABB box = user.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = user.level().getEntitiesOfClass(
            LivingEntity.class,
            box,
            e ->
                e.isAlive()
                    && e != user
                    && !e.isAlliedTo(user)
                    && (ignoreWalls || user.hasLineOfSight(e))
        );
        for (LivingEntity target : targets) {
            target.addEffect(
                new MobEffectInstance(
                    MobEffects.GLOWING,
                    duration,
                    0,
                    true,
                    true
                )
            );
        }
    }

    private void applyCaps(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        for (CapSpec cap : caps) {
            final double base = Math.max(
                0.0,
                UsageMetadataHelper.getDouble(
                    usageInfo,
                    cap.amountMetaKey(),
                    0.0
                )
            );
            final double amount = base * Math.max(0.0, multiplier);
            GuzhenrenVariableModifierService.setAdditiveModifier(
                user,
                cap.variableKey(),
                usageId,
                amount
            );
        }
    }

    private void clearAll(final LivingEntity user) {
        for (CapSpec cap : caps) {
            GuzhenrenVariableModifierService.removeModifier(
                user,
                cap.variableKey(),
                usageId
            );
        }
    }

    private static int scaleDuration(final int baseTicks, final double multiplier) {
        final double scaled = baseTicks * Math.max(0.0, multiplier);
        return (int) UsageMetadataHelper.clamp(
            Math.round(scaled),
            MIN_DURATION_TICKS,
            MAX_DURATION_TICKS
        );
    }

    private void setActive(final LivingEntity user, final boolean active) {
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        if (actives == null) {
            return;
        }
        if (active) {
            actives.add(usageId);
            return;
        }
        actives.remove(usageId);
    }

    public record CapSpec(String variableKey, String amountMetaKey) {}
}
