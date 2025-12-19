package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 一转小智蛊：被动【启智】。
 * <p>
 * 设计目标：持续、低量地提供“念头”续航，用作鉴定/推演等系统性资源的补给。
 * 可在 JSON metadata 中调参，并支持在调整面板中开关。
 * </p>
 */
public class XiaoZhiGuEnlightenEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:xiaozhigu_passive_enlighten";

    private static final double DEFAULT_NIANTOU_REGEN_PER_SECOND = 0.10;
    private static final double DEFAULT_SOUL_COST_PER_SECOND = 0.00;
    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 0.00;

    @Override
    public String getUsageId() {
        return USAGE_ID;
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
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
            if (actives != null) {
                actives.remove(USAGE_ID);
            }
            return;
        }

        final double regen = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "niantou_regen_per_second",
                DEFAULT_NIANTOU_REGEN_PER_SECOND
            )
        );
        if (regen <= 0.0) {
            return;
        }

        final double soulCost = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "soul_cost_per_second",
                DEFAULT_SOUL_COST_PER_SECOND
            )
        );
        final double zhenyuanBaseCost = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "zhenyuan_base_cost_per_second",
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
            )
        );
        final double zhenyuanCost = ZhenYuanHelper.calculateGuCost(
            user,
            zhenyuanBaseCost
        );

        if (soulCost > 0.0 && HunPoHelper.getAmount(user) < soulCost) {
            final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
            if (actives != null) {
                actives.remove(USAGE_ID);
            }
            return;
        }
        if (zhenyuanCost > 0.0 && !ZhenYuanHelper.hasEnough(user, zhenyuanCost)) {
            final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
            if (actives != null) {
                actives.remove(USAGE_ID);
            }
            return;
        }

        if (soulCost > 0.0) {
            HunPoHelper.modify(user, -soulCost);
        }
        if (zhenyuanCost > 0.0) {
            ZhenYuanHelper.modify(user, -zhenyuanCost);
        }

        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        if (actives != null) {
            actives.add(USAGE_ID);
        }

        NianTouHelper.modify(user, regen);
    }

    private static double getMetaDouble(
        final NianTouData.Usage usage,
        final String key,
        final double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }
}

