package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 血道被动：血气感知（范围侦测并高亮目标）。
 * <p>
 * 适配：
 * <ul>
 *   <li>血痕蛊：只侦测“受伤目标”（血气外泄）。</li>
 *   <li>怒目血眼蛊：侦测周围所有敌对生物。</li>
 * </ul>
 * </p>
 */
public class XueDaoSenseGlowEffect implements IGuEffect {

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND;
    private static final String META_NIANTOU_COST_PER_SECOND =
        GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND;
    private static final String META_JINGLI_COST_PER_SECOND =
        GuEffectCostHelper.META_JINGLI_COST_PER_SECOND;
    private static final String META_HUNPO_COST_PER_SECOND =
        GuEffectCostHelper.META_HUNPO_COST_PER_SECOND;

    private static final String META_RADIUS = "radius";
    private static final String META_GLOW_DURATION_TICKS = "glow_duration_ticks";
    private static final String META_REQUIRE_WOUNDED = "require_wounded";
    private static final String META_WOUNDED_RATIO_THRESHOLD = "wounded_ratio_threshold";

    private static final double DEFAULT_COST_PER_SECOND = 0.0;
    private static final double DEFAULT_RADIUS = 24.0;
    private static final int DEFAULT_GLOW_DURATION_TICKS = 60;
    private static final int DEFAULT_REQUIRE_WOUNDED = 1;
    private static final double DEFAULT_WOUNDED_RATIO_THRESHOLD = 0.999;

    private static final Holder<MobEffect> GLOWING = MobEffects.GLOWING;

    private final String usageId;

    public XueDaoSenseGlowEffect(final String usageId) {
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
        if (user == null || usageInfo == null) {
            return;
        }
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(usageId)) {
            return;
        }

        final double zhenyuanBaseCostPerSecond = Math.max(
            DEFAULT_COST_PER_SECOND,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_COST_PER_SECOND
            )
        );
        final double niantouCostPerSecond = Math.max(
            DEFAULT_COST_PER_SECOND,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_NIANTOU_COST_PER_SECOND,
                DEFAULT_COST_PER_SECOND
            )
        );
        final double jingliCostPerSecond = Math.max(
            DEFAULT_COST_PER_SECOND,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_JINGLI_COST_PER_SECOND,
                DEFAULT_COST_PER_SECOND
            )
        );
        final double hunpoCostPerSecond = Math.max(
            DEFAULT_COST_PER_SECOND,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HUNPO_COST_PER_SECOND,
                DEFAULT_COST_PER_SECOND
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
            return;
        }

        final double selfMultiplier = DaoHenEffectScalingHelper.clampMultiplier(
            DaoHenCalculator.calculateSelfMultiplier(user, DaoHenHelper.DaoType.XUE_DAO)
        );
        final double baseRadius = Math.max(
            1.0,
            UsageMetadataHelper.getDouble(usageInfo, META_RADIUS, DEFAULT_RADIUS)
        );
        final double radius = baseRadius * Math.max(0.0, selfMultiplier);

        final int baseDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_GLOW_DURATION_TICKS,
                DEFAULT_GLOW_DURATION_TICKS
            )
        );
        final int duration = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseDuration,
            selfMultiplier
        );
        if (duration <= 0) {
            return;
        }

        final boolean requireWounded = UsageMetadataHelper.getInt(
            usageInfo,
            META_REQUIRE_WOUNDED,
            DEFAULT_REQUIRE_WOUNDED
        ) != 0;
        final double woundedRatio = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_WOUNDED_RATIO_THRESHOLD,
                DEFAULT_WOUNDED_RATIO_THRESHOLD
            ),
            0.0,
            1.0
        );

        final AABB area = user.getBoundingBox().inflate(radius);
        final List<LivingEntity> targets = user.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            e -> e.isAlive() && e != user && !e.isAlliedTo(user)
        );
        for (LivingEntity t : targets) {
            if (requireWounded) {
                final double maxHealth = Math.max(1.0, t.getMaxHealth());
                final double ratio = UsageMetadataHelper.clamp(t.getHealth() / maxHealth, 0.0, 1.0);
                if (ratio >= woundedRatio) {
                    continue;
                }
            }
            if (GLOWING != null) {
                t.addEffect(new MobEffectInstance(GLOWING, duration, 0, true, true));
            }
        }
    }
}

