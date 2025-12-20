package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
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
import net.minecraft.world.item.ItemStack;

/**
 * 三转大腹便便蛊：主动【暴食成盾】。
 * <p>
 * 设计目标：以“全饱食”换一段时间的高额护体，提供关键时刻的保命手段。</p>
 */
public class DaFuPianPianGuFullBellyShieldEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:dafupianpiangu_active_full_belly_shield";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "DaFuPianPianGuFullBellyShieldCooldownUntilTick";

    private static final String META_ABSORPTION_PER_HUNGER =
        "absorption_per_hunger";
    private static final String META_MAX_ABSORPTION = "max_absorption";
    private static final String META_RESISTANCE_SECONDS = "resistance_seconds";
    private static final String META_COOLDOWN_TICKS = "cooldown_ticks";

    private static final double DEFAULT_ABSORPTION_PER_HUNGER = 1.0;
    private static final double DEFAULT_MAX_ABSORPTION = 18.0;
    private static final int DEFAULT_RESISTANCE_SECONDS = 6;
    private static final int DEFAULT_COOLDOWN_TICKS = 500;
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
                Component.literal("暴食成盾冷却中，剩余 " + remain + "t"),
                true
            );
            return false;
        }

        final int hunger = player.getFoodData().getFoodLevel();
        if (hunger <= 0) {
            player.displayClientMessage(
                Component.literal("空腹无脂，难以成盾。"),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.SHI_DAO
        );
        final double per = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_ABSORPTION_PER_HUNGER,
                DEFAULT_ABSORPTION_PER_HUNGER
            )
        ) * multiplier;
        final double max = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                META_MAX_ABSORPTION,
                DEFAULT_MAX_ABSORPTION
            )
        ) * multiplier;
        final double gained = Math.min(max, hunger * per);
        if (gained > 0.0) {
            player.setAbsorptionAmount(
                (float) (player.getAbsorptionAmount() + gained)
            );
        }

        final int resistanceSeconds = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                META_RESISTANCE_SECONDS,
                DEFAULT_RESISTANCE_SECONDS
            )
        );
        if (resistanceSeconds > 0) {
            player.addEffect(
                new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    resistanceSeconds * TICKS_PER_SECOND,
                    0,
                    true,
                    false,
                    true
                )
            );
        }

        // 以 exhaustion 的形式快速“吃空”饱食与饱和，避免依赖版本差异较大的 FoodData setter。
        final float exhaustion = (hunger + 20) * EXHAUSTION_PER_HUNGER;
        player.causeFoodExhaustion(exhaustion);

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
}
