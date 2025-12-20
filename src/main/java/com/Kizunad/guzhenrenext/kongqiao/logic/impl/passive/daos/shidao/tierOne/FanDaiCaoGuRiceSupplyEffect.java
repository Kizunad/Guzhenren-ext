package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 一转饭袋草蛊：被动【米香续命】。
 * <p>
 * 设计目标：把“灌注真元就能生长”落到玩法上——当玩家饥饿时自动补给，避免跑图时被饿死。
 * </p>
 */
public class FanDaiCaoGuRiceSupplyEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:fan_dai_cao_gu_passive_rice_supply";

    private static final String META_TRIGGER_HUNGER = "trigger_hunger";
    private static final String META_HUNGER_GAIN = "hunger_gain";
    private static final String META_SATURATION_GAIN = "saturation_gain";

    private static final int DEFAULT_TRIGGER_HUNGER = 8;
    private static final int DEFAULT_HUNGER_GAIN = 1;
    private static final double DEFAULT_SATURATION_GAIN = 0.1;
    private static final double DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND = 36.0;

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

        final int triggerHunger = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_TRIGGER_HUNGER,
                DEFAULT_TRIGGER_HUNGER
            )
        );
        if (player.getFoodData().getFoodLevel() > triggerHunger) {
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
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
        final double hunpoCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_HUNPO_COST_PER_SECOND,
                0.0
            )
        );
        final double zhenyuanBaseCostPerSecond = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                GuEffectCostHelper.META_ZHENYUAN_BASE_COST_PER_SECOND,
                DEFAULT_ZHENYUAN_BASE_COST_PER_SECOND
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
            KongqiaoAttachments.getActivePassives(user).remove(USAGE_ID);
            return;
        }

        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.SHI_DAO
        );
        final int baseHungerGain = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_HUNGER_GAIN,
                DEFAULT_HUNGER_GAIN
            )
        );
        final int hungerGain = (int) Math.round(baseHungerGain * multiplier);
        final double saturationGain = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_SATURATION_GAIN,
                DEFAULT_SATURATION_GAIN
            )
        ) * multiplier;
        if (hungerGain > 0 || saturationGain > 0.0) {
            player.getFoodData().eat(hungerGain, (float) saturationGain);
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
