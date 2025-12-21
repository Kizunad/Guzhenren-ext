package com.Kizunad.guzhenrenext.kongqiao.logic.impl.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenEffectScalingHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 通用被动：持续维持扣费 + 每秒回血/回资源（真元/念头/精力/魂魄）。
 * <p>
 * 通过道痕倍率缩放，并对倍率做裁剪，防止持续收益膨胀到异常数量级。
 * </p>
 */
public class DaoSustainedResourceRegenEffect implements IGuEffect {

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND;
    private static final String META_NIANTOU_COST_PER_SECOND =
        GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND;
    private static final String META_JINGLI_COST_PER_SECOND =
        GuEffectCostHelper.META_JINGLI_COST_PER_SECOND;
    private static final String META_HUNPO_COST_PER_SECOND =
        GuEffectCostHelper.META_HUNPO_COST_PER_SECOND;

    private static final String META_HEAL_PER_SECOND = "heal_per_second";
    private static final String META_NIANTOU_GAIN_PER_SECOND = "niantou_gain_per_second";
    private static final String META_JINGLI_GAIN_PER_SECOND = "jingli_gain_per_second";
    private static final String META_HUNPO_GAIN_PER_SECOND = "hunpo_gain_per_second";
    private static final String META_ZHENYUAN_GAIN_PER_SECOND = "zhenyuan_gain_per_second";

    private static final double DEFAULT_COST_PER_SECOND = 0.0;
    private static final double MAX_HEAL_PER_SECOND = 200.0;
    private static final double MAX_RESOURCE_GAIN_PER_SECOND = 1000.0;

    private final String usageId;
    private final DaoHenHelper.DaoType daoType;

    public DaoSustainedResourceRegenEffect(
        final String usageId,
        final DaoHenHelper.DaoType daoType
    ) {
        this.usageId = usageId;
        this.daoType = daoType;
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
            setActive(user, false);
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
            setActive(user, false);
            return;
        }
        setActive(user, true);

        final double multiplier = DaoHenEffectScalingHelper.clampMultiplier(
            daoType == null ? 1.0 : DaoHenCalculator.calculateSelfMultiplier(user, daoType)
        );
        applyHeal(user, usageInfo, multiplier);
        applyResourceGain(user, usageInfo, multiplier);
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user == null || user.level().isClientSide()) {
            return;
        }
        setActive(user, false);
    }

    private static void applyHeal(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        final double baseHeal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_PER_SECOND, 0.0)
        );
        final double heal = DaoHenEffectScalingHelper.scaleValue(baseHeal, multiplier);
        if (heal <= 0.0) {
            return;
        }
        final double clamped = UsageMetadataHelper.clamp(
            heal,
            0.0,
            MAX_HEAL_PER_SECOND
        );
        if (clamped > 0.0) {
            user.heal((float) clamped);
        }
    }

    private static void applyResourceGain(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        applyGain(
            user,
            usageInfo,
            multiplier,
            META_NIANTOU_GAIN_PER_SECOND,
            NianTouHelper::modify
        );
        applyGain(
            user,
            usageInfo,
            multiplier,
            META_JINGLI_GAIN_PER_SECOND,
            JingLiHelper::modify
        );
        applyGain(
            user,
            usageInfo,
            multiplier,
            META_HUNPO_GAIN_PER_SECOND,
            HunPoHelper::modify
        );
        applyGain(
            user,
            usageInfo,
            multiplier,
            META_ZHENYUAN_GAIN_PER_SECOND,
            ZhenYuanHelper::modify
        );
    }

    private static void applyGain(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double multiplier,
        final String metaKey,
        final ResourceModifier modifier
    ) {
        final double base = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, metaKey, 0.0)
        );
        if (base <= 0.0) {
            return;
        }
        final double scaled = DaoHenEffectScalingHelper.scaleValue(base, multiplier);
        final double clamped = UsageMetadataHelper.clamp(
            scaled,
            0.0,
            MAX_RESOURCE_GAIN_PER_SECOND
        );
        if (clamped > 0.0) {
            modifier.modify(user, clamped);
        }
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

    @FunctionalInterface
    private interface ResourceModifier {
        void modify(LivingEntity user, double amount);
    }
}

