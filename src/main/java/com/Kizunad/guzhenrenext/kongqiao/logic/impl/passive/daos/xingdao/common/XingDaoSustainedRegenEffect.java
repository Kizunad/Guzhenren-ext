package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xingdao.common;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
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
 * 星道通用被动：持续性维持（多资源）+ 回复（生命/念头/真元）。
 * <p>
 * - 资源消耗：由 metadata 指定（含真元折算、念头/精力/魂魄等）。
 * - 回复强度：随星道道痕动态变化（倍率裁剪防止失控）。
 * </p>
 */
public class XingDaoSustainedRegenEffect implements IGuEffect {

    private static final String META_HEAL_AMOUNT = "heal_amount";
    private static final String META_NIANTOU_GAIN = "niantou_gain";
    private static final String META_ZHENYUAN_GAIN = "zhenyuan_gain";

    private static final double DEFAULT_COST = 0.0;

    private final String usageId;

    public XingDaoSustainedRegenEffect(final String usageId) {
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
            DaoHenHelper.DaoType.XING_DAO
        );

        final double heal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, META_HEAL_AMOUNT, 0.0)
        );
        if (heal > 0.0) {
            user.heal(
                (float) DaoHenEffectScalingHelper.scaleValue(heal, selfMultiplier)
            );
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

