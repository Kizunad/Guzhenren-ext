package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 雷道被动：电眼巡照（持续维持）- 侦测隐形目标并使其显形（发光）。
 * <p>
 * 注意：仅做“显形/标记”，不提供透视能力；并通过每秒维持成本限制常驻强度。
 * </p>
 */
public class LeiDaoSustainedRevealInvisEffect implements IGuEffect {

    private static final String META_RADIUS = "radius";
    private static final String META_REVEAL_DURATION_TICKS = "reveal_duration_ticks";
    private static final String META_NIGHT_VISION_DURATION_TICKS = "night_vision_duration_ticks";

    private static final double DEFAULT_COST = 0.0;

    private static final double DEFAULT_RADIUS = 12.0;
    private static final double MIN_RADIUS = 1.0;
    private static final double MAX_RADIUS = 64.0;

    private static final int DEFAULT_REVEAL_DURATION_TICKS = 60;
    private static final int DEFAULT_NIGHT_VISION_DURATION_TICKS = 80;

    private final String usageId;

    public LeiDaoSustainedRevealInvisEffect(final String usageId) {
        this.usageId = usageId;
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
            return;
        }

        final double zhenyuanBaseCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double niantouCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double jingliCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_JINGLI_COST_PER_SECOND,
                DEFAULT_COST
            )
        );
        final double hunpoCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_HUNPO_COST_PER_SECOND,
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
            return;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.LEI_DAO
        );

        final double radius = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS),
            MIN_RADIUS,
            MAX_RADIUS
        );
        final AABB box = user.getBoundingBox().inflate(radius);

        final int baseRevealDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_REVEAL_DURATION_TICKS,
                DEFAULT_REVEAL_DURATION_TICKS
            )
        );
        final int revealDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseRevealDuration,
            selfMultiplier
        );

        if (revealDuration > 0) {
            for (LivingEntity entity : user.level().getEntitiesOfClass(
                LivingEntity.class,
                box,
                e -> e.isAlive() && e != user && (e.isInvisible() || e.hasEffect(MobEffects.INVISIBILITY))
            )) {
                entity.addEffect(
                    new MobEffectInstance(MobEffects.GLOWING, revealDuration, 0, true, true)
                );
            }
        }

        final int baseNv = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_NIGHT_VISION_DURATION_TICKS,
                DEFAULT_NIGHT_VISION_DURATION_TICKS
            )
        );
        final int nvDuration = DaoHenEffectScalingHelper.scaleDurationTicks(baseNv, selfMultiplier);
        if (nvDuration > 0) {
            user.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, nvDuration, 0, true, false));
        }

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
}

