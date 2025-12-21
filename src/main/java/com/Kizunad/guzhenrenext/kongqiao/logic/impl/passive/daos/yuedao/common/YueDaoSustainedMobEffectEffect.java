package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.common;

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
 * 月道被动：持续维持一个药水效果（月道道痕缩放持续时间）。
 */
public class YueDaoSustainedMobEffectEffect implements IGuEffect {
    private static final String META_EFFECT_DURATION_TICKS = "effect_duration_ticks";
    private static final String META_EFFECT_AMPLIFIER = "effect_amplifier";

    private static final int DEFAULT_EFFECT_DURATION_TICKS = 20 * 4;
    private static final int DEFAULT_EFFECT_AMPLIFIER = 0;
    private static final double DEFAULT_COST = 0.0;

    private final String usageId;
    private final Holder<MobEffect> effect;

    public YueDaoSustainedMobEffectEffect(
        final String usageId,
        final Holder<MobEffect> effect
    ) {
        this.usageId = usageId;
        this.effect = effect;
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
        final int duration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_DURATION_TICKS,
                DEFAULT_EFFECT_DURATION_TICKS
            )
        );
        final int amplifier = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_EFFECT_AMPLIFIER,
                DEFAULT_EFFECT_AMPLIFIER
            )
        );
        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.YUE_DAO
        );
        final int scaledDuration = DaoHenEffectScalingHelper.scaleDurationTicks(
            duration,
            selfMultiplier
        );
        if (scaledDuration > 0) {
            user.addEffect(
                new MobEffectInstance(effect, scaledDuration, amplifier, true, true)
            );
        }
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

