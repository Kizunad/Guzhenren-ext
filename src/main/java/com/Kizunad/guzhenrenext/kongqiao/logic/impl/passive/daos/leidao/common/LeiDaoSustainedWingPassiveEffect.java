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

/**
 * 雷道被动：雷翼余势（持续维持）- 增加机动性（移速/缓降）。
 * <p>
 * 通过持续多资源消耗，避免机动性常驻过强。
 * </p>
 */
public class LeiDaoSustainedWingPassiveEffect implements IGuEffect {

    private static final String META_SPEED_DURATION_TICKS = "speed_duration_ticks";
    private static final String META_SPEED_AMPLIFIER = "speed_amplifier";
    private static final String META_SLOW_FALL_DURATION_TICKS = "slow_fall_duration_ticks";

    private static final double DEFAULT_COST = 0.0;

    private static final int DEFAULT_SPEED_DURATION_TICKS = 60;
    private static final int DEFAULT_SPEED_AMPLIFIER = 0;
    private static final int DEFAULT_SLOW_FALL_DURATION_TICKS = 60;

    private final String usageId;

    public LeiDaoSustainedWingPassiveEffect(final String usageId) {
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

        final int speedDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_SPEED_DURATION_TICKS,
                    DEFAULT_SPEED_DURATION_TICKS
                )
            ),
            selfMultiplier
        );
        final int speedAmplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_SPEED_AMPLIFIER,
                DEFAULT_SPEED_AMPLIFIER
            )
        );
        if (speedDuration > 0) {
            user.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    speedDuration,
                    speedAmplifier,
                    true,
                    true
                )
            );
        }

        final int slowFallDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            Math.max(
                0,
                UsageMetadataHelper.getInt(
                    usageInfo,
                    META_SLOW_FALL_DURATION_TICKS,
                    DEFAULT_SLOW_FALL_DURATION_TICKS
                )
            ),
            selfMultiplier
        );
        if (slowFallDuration > 0) {
            user.addEffect(
                new MobEffectInstance(
                    MobEffects.SLOW_FALLING,
                    slowFallDuration,
                    0,
                    true,
                    true
                )
            );
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

