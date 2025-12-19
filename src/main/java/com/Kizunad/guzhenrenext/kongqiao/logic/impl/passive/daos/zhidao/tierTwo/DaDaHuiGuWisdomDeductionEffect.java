package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 二转大慧蛊：被动【慧悟】。
 * <p>
 * 设计目标：维持开启后，持续消耗真元，并为“杀招推演”提供加成（由主动【慧推】读取）。
 * </p>
 */
public class DaDaHuiGuWisdomDeductionEffect implements IGuEffect {

    public static final String PASSIVE_USAGE_ID =
        "guzhenren:dadahuigu_passive_wisdom_deduction";

    private static final String ITEM_ID = "guzhenren:dadahuigu";

    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 6.4;
    private static final double DEFAULT_CHANCE_BONUS = 0.10;
    private static final double DEFAULT_FAIL_COST_MULTIPLIER = 0.85;

    @Override
    public String getUsageId() {
        return PASSIVE_USAGE_ID;
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
        if (config != null && !config.isPassiveEnabled(PASSIVE_USAGE_ID)) {
            final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
            if (actives != null) {
                actives.remove(PASSIVE_USAGE_ID);
            }
            return;
        }

        final double baseCost = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "zhenyuan_base_cost_per_second",
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
            )
        );
        final double cost = ZhenYuanHelper.calculateGuCost(user, baseCost);
        if (cost > 0.0 && !ZhenYuanHelper.hasEnough(user, cost)) {
            final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
            if (actives != null) {
                actives.remove(PASSIVE_USAGE_ID);
            }
            return;
        }
        if (cost > 0.0) {
            ZhenYuanHelper.modify(user, -cost);
        }

        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        if (actives != null) {
            actives.add(PASSIVE_USAGE_ID);
        }
    }

    public static double getChanceBonusFromConfig() {
        final NianTouData.Usage usage = resolveUsage();
        if (usage == null) {
            return DEFAULT_CHANCE_BONUS;
        }
        return Math.max(
            0.0,
            getMetaDouble(usage, "derive_chance_bonus", DEFAULT_CHANCE_BONUS)
        );
    }

    public static double getFailCostMultiplierFromConfig() {
        final NianTouData.Usage usage = resolveUsage();
        if (usage == null) {
            return DEFAULT_FAIL_COST_MULTIPLIER;
        }
        return Math.max(
            0.0,
            getMetaDouble(
                usage,
                "fail_cost_multiplier",
                DEFAULT_FAIL_COST_MULTIPLIER
            )
        );
    }

    private static NianTouData.Usage resolveUsage() {
        final ResourceLocation id;
        try {
            id = ResourceLocation.parse(ITEM_ID);
        } catch (Exception e) {
            return null;
        }
        final Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
        if (item == Items.AIR) {
            return null;
        }
        final NianTouData data = NianTouDataManager.getData(item);
        if (data == null || data.usages() == null) {
            return null;
        }
        for (NianTouData.Usage usage : data.usages()) {
            if (usage != null && PASSIVE_USAGE_ID.equals(usage.usageID())) {
                return usage;
            }
        }
        return null;
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
