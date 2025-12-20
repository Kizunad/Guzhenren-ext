package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 一转酒囊花蛊：被动【花酿回甘】。
 * <p>
 * 设计目标：提供一个“低频补给”的食道续航效果，强调“灌注真元就能生长”的原设定。
 * </p>
 */
public class JiuNangHuaGuBrewScentEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:jiu_nang_hua_gu_passive_brew_scent";

    private static final String TAG_NEXT_GRANT_TICK =
        "JiuNangHuaGuBrewScentNextGrantTick";

    private static final String META_INTERVAL_SECONDS = "interval_seconds";
    private static final String META_HUNGER_GAIN = "hunger_gain";
    private static final String META_SATURATION_GAIN = "saturation_gain";
    private static final String META_ZHENYUAN_BASE_COST_PER_SECOND =
        "zhenyuan_base_cost_per_second";

    private static final int DEFAULT_INTERVAL_SECONDS = 6;
    private static final int DEFAULT_HUNGER_GAIN = 1;
    private static final double DEFAULT_SATURATION_GAIN = 0.2;
    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 48.0;

    private static final int TICKS_PER_SECOND = 20;

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

        if (!(user instanceof Player player)) {
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

        final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
        if (actives != null) {
            actives.add(USAGE_ID);
        }

        final int intervalSeconds = Math.max(
            1,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_INTERVAL_SECONDS,
                DEFAULT_INTERVAL_SECONDS
            )
        );
        final int currentTick = user.tickCount;
        final int nextTick = user.getPersistentData().getInt(TAG_NEXT_GRANT_TICK);
        if (nextTick > currentTick) {
            return;
        }

        final int hungerGain = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_HUNGER_GAIN,
                DEFAULT_HUNGER_GAIN
            )
        );
        final double saturationGain = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_SATURATION_GAIN,
                DEFAULT_SATURATION_GAIN
            )
        );

        if (hungerGain > 0 || saturationGain > 0.0) {
            player.getFoodData().eat(hungerGain, (float) saturationGain);
        }
        user.getPersistentData()
            .putInt(
                TAG_NEXT_GRANT_TICK,
                currentTick + intervalSeconds * TICKS_PER_SECOND
            );
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

