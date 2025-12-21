package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common;

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
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 力道被动：持续维持一个药水效果（维持消耗 + 力道道痕缩放）。
 */
public class LiDaoSustainedMobEffectEffect implements IGuEffect {
    private static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    private static final String META_EFFECT_AMPLIFIER = "effect_amplifier";
    private static final double DEFAULT_COST = 0.0;

    private final String usageId;
    private final Holder<MobEffect> effect;
    private final int defaultDurationTicks;
    private final int defaultAmplifier;

    public LiDaoSustainedMobEffectEffect(
        final String usageId,
        final Holder<MobEffect> effect,
        final int defaultDurationTicks,
        final int defaultAmplifier
    ) {
        this.usageId = usageId;
        this.effect = effect;
        this.defaultDurationTicks = defaultDurationTicks;
        this.defaultAmplifier = defaultAmplifier;
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
        final double zhenyuanBaseCostPerSecond = Math.max(
            DEFAULT_COST,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_COST
            )
        );

        if (!GuEffectCostHelper.tryConsumeSustain(
            user,
            niantouCostPerSecond,
            jingliCostPerSecond,
            hunpoCostPerSecond,
            zhenyuanBaseCostPerSecond
        )) {
            setActive(user, false);
            return;
        }
        setActive(user, true);

        if (effect == null) {
            return;
        }
        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.LI_DAO
        );

        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_DURATION_TICKS,
                defaultDurationTicks
            )
        );
        final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            duration,
            selfMultiplier
        );
        if (scaledDuration <= 0) {
            return;
        }
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_AMPLIFIER,
                defaultAmplifier
            )
        );

        user.addEffect(
            new MobEffectInstance(effect, scaledDuration, amplifier, true, true)
        );
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

