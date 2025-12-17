package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.registry.ModMobEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 三转犬魄蛊：主动【疯狗咬】。
 * <p>
 * 本身不直接造成额外伤害，而是给玩家施加短暂的“疯狗状态”（{@link ModMobEffects#MAD_DOG}）。<br>
 * 该状态会在战斗事件中联动，为被攻击目标施加“撕裂”（{@link ModMobEffects#TEAR}）：<br>
 * <ul>
 *   <li>撕裂：敌人移动时持续掉血，且治疗减半。</li>
 *   <li>持续时间：由奴道道痕提供加成（在事件监听中计算）。</li>
 * </ul>
 * </p>
 */
public class QuanPuGuMadDogBiteEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:quanpugu_active_mad_dog_bite";

    private static final int DEFAULT_ACTIVE_DURATION_TICKS = 200;
    private static final double DEFAULT_SOUL_COST = 0.0;
    private static final double DEFAULT_ZHENYUAN_BASE_COST = 0.0;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public boolean onActivate(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return false;
        }
        if (!(user instanceof Player player)) {
            return false;
        }
        if (!(user.level() instanceof ServerLevel)) {
            return false;
        }

        double soulCost = getMetaDouble(usageInfo, "activate_soul_cost", DEFAULT_SOUL_COST);
        if (soulCost > 0 && HunPoHelper.getAmount(player) < soulCost) {
            player.sendSystemMessage(Component.literal("魂魄不足，无法施展【疯狗咬】。"));
            return false;
        }

        double baseCost = getMetaDouble(
            usageInfo,
            "activate_zhenyuan_base_cost",
            DEFAULT_ZHENYUAN_BASE_COST
        );
        double realZhenyuanCost = ZhenYuanHelper.calculateGuCost(player, baseCost);
        if (realZhenyuanCost > 0 && !ZhenYuanHelper.hasEnough(player, realZhenyuanCost)) {
            player.sendSystemMessage(Component.literal("真元不足，无法施展【疯狗咬】。"));
            return false;
        }

        if (soulCost > 0) {
            HunPoHelper.modify(player, -soulCost);
        }
        if (realZhenyuanCost > 0) {
            ZhenYuanHelper.modify(player, -realZhenyuanCost);
        }

        int duration = getMetaInt(
            usageInfo,
            "active_duration_ticks",
            DEFAULT_ACTIVE_DURATION_TICKS
        );
        if (duration <= 0) {
            duration = DEFAULT_ACTIVE_DURATION_TICKS;
        }

        player.addEffect(
            new MobEffectInstance(
                ModMobEffects.MAD_DOG,
                duration,
                0,
                true,
                true,
                true
            )
        );
        player.sendSystemMessage(Component.literal("犬魄翻涌，已开启【疯狗咬】。"));
        return true;
    }

    @Override
    public void onUnequip(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (!user.level().isClientSide()) {
            user.removeEffect(ModMobEffects.MAD_DOG);
        }
    }

    private static double getMetaDouble(
        NianTouData.Usage usage,
        String key,
        double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
                // 念头配置属于数据驱动：无效值回退默认，避免影响服务器稳定性。
            }
        }
        return defaultValue;
    }

    private static int getMetaInt(
        NianTouData.Usage usage,
        String key,
        int defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Integer.parseInt(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
                // 念头配置属于数据驱动：无效值回退默认，避免影响服务器稳定性。
            }
        }
        return defaultValue;
    }
}
