package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
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
 * 血道被动：持续维持扣费 + 每秒回血/回资源。
 * <p>
 * 说明：该效果用于“血气补充、血雨领域余波”等偏辅助类被动。
 * 数值缩放使用 {@link DaoHenEffectScalingHelper} 做倍率裁剪，
 * 防止持续回复随道痕膨胀到离谱数量级。
 * </p>
 */
public class XueDaoSustainedResourceRegenEffect implements IGuEffect {

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

    private final String usageId;

    public XueDaoSustainedResourceRegenEffect(final String usageId) {
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
        applyHeal(user, usageInfo, selfMultiplier);
        applyResourceGain(user, usageInfo, selfMultiplier);
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
        if (heal > 0.0) {
            user.heal((float) heal);
        }
    }

    private static void applyResourceGain(
        final LivingEntity user,
        final NianTouData.Usage usageInfo,
        final double multiplier
    ) {
        final double baseNianTou = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_NIANTOU_GAIN_PER_SECOND,
                0.0
            )
        );
        if (baseNianTou > 0.0) {
            NianTouHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(baseNianTou, multiplier)
            );
        }

        final double baseJingLi = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_JINGLI_GAIN_PER_SECOND,
                0.0
            )
        );
        if (baseJingLi > 0.0) {
            JingLiHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(baseJingLi, multiplier)
            );
        }

        final double baseHunPo = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HUNPO_GAIN_PER_SECOND,
                0.0
            )
        );
        if (baseHunPo > 0.0) {
            HunPoHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(baseHunPo, multiplier)
            );
        }

        final double baseZhenYuan = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_GAIN_PER_SECOND,
                0.0
            )
        );
        if (baseZhenYuan > 0.0) {
            ZhenYuanHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(baseZhenYuan, multiplier)
            );
        }
    }
}

