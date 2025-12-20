package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierFive;

import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.QiyunHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 五转空念蛊：被动【空念生机】。
 * <p>
 * 当念头低于阈值时，持续消耗真元将“空念”转化为少量念头；并轻微损耗气运作为代价。
 * </p>
 */
public class KongNianGuEmptyThoughtPassiveEffect implements IGuEffect {

    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";
    private static final String META_THRESHOLD = "niantou_threshold";
    private static final String META_NIANTOU_GAIN_PER_SECOND =
        "niantou_gain_per_second";
    private static final String META_QIYUN_COST_PER_SECOND =
        "qiyun_cost_per_second";

    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 12.0;
    private static final double DEFAULT_THRESHOLD = 80.0;
    private static final double DEFAULT_NIANTOU_GAIN_PER_SECOND = 0.8;
    private static final double DEFAULT_QIYUN_COST_PER_SECOND = 0.05;

    private final String usageId;

    public KongNianGuEmptyThoughtPassiveEffect(final String usageId) {
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

        final double baseCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
            )
        );
        final double cost = ZhenYuanHelper.calculateGuCost(user, baseCost);
        if (cost > 0.0 && !ZhenYuanHelper.hasEnough(user, cost)) {
            setActive(user, false);
            return;
        }
        if (cost > 0.0) {
            ZhenYuanHelper.modify(user, -cost);
        }
        setActive(user, true);

        final double threshold = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_THRESHOLD,
                DEFAULT_THRESHOLD
            )
        );
        if (NianTouHelper.getAmount(user) >= threshold) {
            return;
        }

        final double gain = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_NIANTOU_GAIN_PER_SECOND,
                DEFAULT_NIANTOU_GAIN_PER_SECOND
            )
        );
        if (gain > 0.0) {
            NianTouHelper.modify(user, gain);
        }

        final double qiyunCost = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_QIYUN_COST_PER_SECOND,
                DEFAULT_QIYUN_COST_PER_SECOND
            )
        );
        if (qiyunCost > 0.0) {
            QiyunHelper.modify(user, -qiyunCost);
        }
    }

    private void setActive(final LivingEntity user, final boolean active) {
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(
            user
        );
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
