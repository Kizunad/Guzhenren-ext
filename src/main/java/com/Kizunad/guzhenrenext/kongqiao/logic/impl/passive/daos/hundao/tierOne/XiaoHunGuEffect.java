package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 小魂蛊逻辑：被动回复魂魄。
 */
public class XiaoHunGuEffect implements IGuEffect {

    private static final Logger LOGGER = LoggerFactory.getLogger(XiaoHunGuEffect.class);
    public static final String USAGE_ID = "guzhenren:xiaohungu_passive_regen";
    private static final double DEFAULT_REGEN = 0.2; // 每秒回复 0.2 点

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onSecond(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
        if (user.level().isClientSide()) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            return;
        }

        final double niantouCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_NIANTOU_COST_PER_SECOND,
                0.0
            )
        );
        final double jingliCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_JINGLI_COST_PER_SECOND,
                0.0
            )
        );
        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                0.0
            )
        );
        if (
            !GuEffectCostHelper.tryConsumeSustain(
                user,
                niantouCostPerSecond,
                jingliCostPerSecond,
                0.0,
                zhenyuanBaseCostPerSecond
            )
        ) {
            return;
        }

        final double amountBase = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, "regen", DEFAULT_REGEN)
        );
        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final double amount = amountBase * multiplier;

        double current = HunPoHelper.getAmount(user);
        double max = HunPoHelper.getMaxAmount(user);

        if (current < max) {
            HunPoHelper.modify(user, amount);
            // LOGGER.debug("小魂蛊为 {} 回复了 {} 点魂魄", user.getName().getString(), amount);
        }
    }
}
