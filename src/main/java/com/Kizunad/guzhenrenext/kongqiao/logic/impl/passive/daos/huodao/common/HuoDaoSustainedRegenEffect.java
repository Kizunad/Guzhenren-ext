package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
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
 * 火道被动：持续恢复（血量/念头/精力/魂魄/真元可选），并受火道道痕缩放。
 * <p>
 * 约束：资源增益建议保持 0.01-100 的区间，避免破坏经济与战斗节奏。
 * </p>
 */
public class HuoDaoSustainedRegenEffect implements IGuEffect {
    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_NIANTOU_GAIN = "niantou_gain";
    private static final String META_JINGLI_GAIN = "jingli_gain";
    private static final String META_HUNPO_GAIN = "hunpo_gain";
    private static final String META_ZHENYUAN_GAIN = "zhenyuan_gain";

    private static final double DEFAULT_COST = 0.0;

    private final String usageId;

    public HuoDaoSustainedRegenEffect(final String usageId) {
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

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.HUO_DAO
        );

        final double heal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, 0.0)
        );
        if (heal > 0.0) {
            user.heal((float) DaoHenEffectScalingHelper.scaleValue(heal, selfMultiplier));
        }

        final double niantouGain = UsageMetadataHelper.getDouble(
            usageInfo,
            META_NIANTOU_GAIN,
            0.0
        );
        if (Double.compare(niantouGain, 0.0) != 0) {
            NianTouHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(niantouGain, selfMultiplier)
            );
        }

        final double jingliGain = UsageMetadataHelper.getDouble(
            usageInfo,
            META_JINGLI_GAIN,
            0.0
        );
        if (Double.compare(jingliGain, 0.0) != 0) {
            JingLiHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(jingliGain, selfMultiplier)
            );
        }

        final double hunpoGain = UsageMetadataHelper.getDouble(
            usageInfo,
            META_HUNPO_GAIN,
            0.0
        );
        if (Double.compare(hunpoGain, 0.0) != 0) {
            HunPoHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(hunpoGain, selfMultiplier)
            );
        }

        final double zhenyuanGain = UsageMetadataHelper.getDouble(
            usageInfo,
            META_ZHENYUAN_GAIN,
            0.0
        );
        if (Double.compare(zhenyuanGain, 0.0) != 0) {
            ZhenYuanHelper.modify(
                user,
                DaoHenEffectScalingHelper.scaleValue(zhenyuanGain, selfMultiplier)
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

