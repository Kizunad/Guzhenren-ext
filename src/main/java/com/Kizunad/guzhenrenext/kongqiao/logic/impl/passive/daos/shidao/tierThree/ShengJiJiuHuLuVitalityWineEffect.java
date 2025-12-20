package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierThree;

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
 * 三转生机酒葫芦：被动【生机酒润体】。
 * <p>
 * 设计目标：提供一个“持续小治疗 + 真元续航”的温和型被动，适合长时间刷怪/跑图。</p>
 */
public class ShengJiJiuHuLuVitalityWineEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:shengjijiuhulu_passive_vitality_wine";

    private static final String META_HEAL_PER_SECOND = "heal_per_second";
    private static final String META_ZHENYUAN_REGEN = "zhenyuan_regen";
    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";

    private static final double DEFAULT_HEAL_PER_SECOND = 0.3;
    private static final double DEFAULT_ZHENYUAN_REGEN = 3.0;
    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 150.0;

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
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
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
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
            return;
        }
        if (cost > 0.0) {
            ZhenYuanHelper.modify(user, -cost);
        }

        final double heal = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_HEAL_PER_SECOND,
                DEFAULT_HEAL_PER_SECOND
            )
        );
        if (heal > 0.0 && user.getHealth() < user.getMaxHealth()) {
            user.heal((float) heal);
        }

        final double zhenyuan = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_REGEN,
                DEFAULT_ZHENYUAN_REGEN
            )
        );
        if (zhenyuan > 0.0) {
            ZhenYuanHelper.modify(user, zhenyuan);
        }

        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        if (actives != null) {
            actives.add(USAGE_ID);
        }
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        if (actives != null) {
            actives.remove(USAGE_ID);
        }
    }
}

