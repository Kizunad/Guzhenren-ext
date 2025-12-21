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
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 血战蛊被动：血量越低，体能增幅越强（每秒刷新）。
 * <p>
 * 说明：该效果属于“辅助/防御”混合型，被动强度受道痕缩放但做裁剪，
 * 避免在高道痕下持续时间过长或增幅过高。
 * </p>
 */
public class XueDaoLowHealthFuryEffect implements IGuEffect {

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND;
    private static final String META_NIANTOU_COST_PER_SECOND =
        GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND;
    private static final String META_JINGLI_COST_PER_SECOND =
        GuEffectCostHelper.META_JINGLI_COST_PER_SECOND;
    private static final String META_HUNPO_COST_PER_SECOND =
        GuEffectCostHelper.META_HUNPO_COST_PER_SECOND;

    private static final String META_HEALTH_RATIO_THRESHOLD =
        "health_ratio_threshold";
    private static final String META_BUFF_DURATION_TICKS =
        "buff_duration_ticks";
    private static final String META_BASE_AMPLIFIER =
        "base_amplifier";
    private static final String META_MAX_BONUS_AMPLIFIER =
        "max_bonus_amplifier";

    private static final double DEFAULT_COST_PER_SECOND = 0.0;
    private static final double DEFAULT_HEALTH_RATIO_THRESHOLD = 0.50;
    private static final int DEFAULT_BUFF_DURATION_TICKS = 60;
    private static final int DEFAULT_BASE_AMPLIFIER = 0;
    private static final int DEFAULT_MAX_BONUS_AMPLIFIER = 2;
    private static final double RATIO_EPSILON = 0.0001;

    private static final Holder<MobEffect> EFFECT_STRENGTH =
        MobEffects.DAMAGE_BOOST;
    private static final Holder<MobEffect> EFFECT_RESISTANCE =
        MobEffects.DAMAGE_RESISTANCE;

    private final String usageId;

    public XueDaoLowHealthFuryEffect(final String usageId) {
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

        final double threshold = UsageMetadataHelper.clamp(
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HEALTH_RATIO_THRESHOLD,
                DEFAULT_HEALTH_RATIO_THRESHOLD
            ),
            0.05,
            0.95
        );
        final double maxHealth = Math.max(1.0, user.getMaxHealth());
        final double ratio = UsageMetadataHelper.clamp(user.getHealth() / maxHealth, 0.0, 1.0);
        if (ratio >= threshold) {
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
        final int baseDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_BUFF_DURATION_TICKS,
                DEFAULT_BUFF_DURATION_TICKS
            )
        );
        final int duration = DaoHenEffectScalingHelper.scaleDurationTicks(
            baseDuration,
            selfMultiplier
        );
        if (duration <= 0) {
            return;
        }

        final int baseAmp = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_BASE_AMPLIFIER,
                DEFAULT_BASE_AMPLIFIER
            )
        );
        final int maxBonus = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_MAX_BONUS_AMPLIFIER,
                DEFAULT_MAX_BONUS_AMPLIFIER
            )
        );

        final double severity = UsageMetadataHelper.clamp(
            (threshold - ratio) / Math.max(RATIO_EPSILON, threshold),
            0.0,
            1.0
        );
        final int bonus = (int) Math.round(maxBonus * severity);
        final int amplifier = Math.max(0, baseAmp + bonus);

        if (EFFECT_STRENGTH != null) {
            user.addEffect(new MobEffectInstance(EFFECT_STRENGTH, duration, amplifier, true, true));
        }
        if (EFFECT_RESISTANCE != null) {
            user.addEffect(
                new MobEffectInstance(EFFECT_RESISTANCE, duration, Math.max(0, amplifier - 1), true, true)
            );
        }
    }
}
