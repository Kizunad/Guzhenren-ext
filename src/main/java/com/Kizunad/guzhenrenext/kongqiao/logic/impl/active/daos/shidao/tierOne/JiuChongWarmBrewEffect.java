package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCooldownHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 一转酒虫：主动【温酒化元】。
 * <p>
 * 设计目标：提供一个低转可用的“紧急真元补给”按钮，代价是快速消耗饱食度并进入冷却。
 * </p>
 */
public class JiuChongWarmBrewEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:jiu_chong_active_warm_brew";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "JiuChongWarmBrewCooldownUntilTick";

    private static final String META_HUNGER_COST = "hunger_cost";
    private static final String META_ZHENYUAN_GAIN = "zhenyuan_gain";
    private static final String META_SPEED_SECONDS = "speed_seconds";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final int DEFAULT_HUNGER_COST = 2;
    private static final double DEFAULT_ZHENYUAN_GAIN = 60.0;
    private static final int DEFAULT_SPEED_SECONDS = 6;
    private static final int DEFAULT_COOLDOWN_TICKS = 200;
    private static final int TICKS_PER_SECOND = 20;

    private static final float EXHAUSTION_PER_HUNGER = 4.0F;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public boolean onActivate(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return false;
        }
        if (!(user instanceof ServerPlayer player)) {
            return false;
        }

        final int remain = GuEffectCooldownHelper.getRemainingTicks(
            user,
            NBT_COOLDOWN_UNTIL_TICK
        );
        if (remain > 0) {
            player.displayClientMessage(
                Component.literal("温酒化元冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        final int hungerCost = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_HUNGER_COST,
                DEFAULT_HUNGER_COST
            )
        );
        if (!hasEnoughHunger(player, hungerCost)) {
            player.displayClientMessage(
                Component.literal("饱食不足，酒意难以化元。"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        if (hungerCost > 0) {
            player.causeFoodExhaustion(hungerCost * EXHAUSTION_PER_HUNGER);
        }

        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.SHI_DAO
        );
        final double gain = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ZHENYUAN_GAIN,
                DEFAULT_ZHENYUAN_GAIN
            )
        ) * multiplier;
        if (gain > 0.0) {
            ZhenYuanHelper.modify(user, gain);
        }

        final int speedSeconds = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_SPEED_SECONDS,
                DEFAULT_SPEED_SECONDS
            )
        );
        if (speedSeconds > 0) {
            player.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    speedSeconds * TICKS_PER_SECOND,
                    0,
                    true,
                    false,
                    true
                )
            );
        }

        final int cooldownTicks = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_COOLDOWN_TICKS,
                DEFAULT_COOLDOWN_TICKS
            )
        );
        if (cooldownTicks > 0) {
            GuEffectCooldownHelper.setCooldownUntilTick(
                user,
                NBT_COOLDOWN_UNTIL_TICK,
                user.tickCount + cooldownTicks
            );
        }
        return true;
    }

    private static boolean hasEnoughHunger(
        final Player player,
        final int hungerCost
    ) {
        if (hungerCost <= 0) {
            return true;
        }
        return player.getFoodData().getFoodLevel() >= hungerCost;
    }
}
