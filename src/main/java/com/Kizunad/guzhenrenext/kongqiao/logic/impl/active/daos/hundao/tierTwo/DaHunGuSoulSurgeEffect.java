package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 二转大魂蛊：主动【聚魂返照】。
 * <p>
 * 功能性定位：消耗真元，把真元快速炼化为魂魄与少量魂魄抗性，用于续航与战前补魄。
 * </p>
 */
public class DaHunGuSoulSurgeEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:dahuongu_active_soul_surge";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "DaHunGuSoulSurgeCooldownUntilTick";

    private static final int TICKS_PER_SECOND = 20;

    private static final double DEFAULT_ACTIVATE_ZHENYUAN_BASE_COST = 640.0;
    private static final double DEFAULT_SOUL_RESTORE_RATIO = 0.20;
    private static final double DEFAULT_SOUL_RESTORE_FLAT = 10.0;
    private static final double DEFAULT_RESISTANCE_GAIN_FLAT = 2.0;
    private static final int DEFAULT_COOLDOWN_TICKS = 120;

    private static final int PARTICLE_COUNT = 16;
    private static final double PARTICLE_SPREAD = 0.35;
    private static final double PARTICLE_SPEED = 0.02;
    private static final double PARTICLE_Y_FACTOR = 0.6;

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
        if (!(user.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        final int currentTick = user.tickCount;
        final int cooldownUntil = getCooldownUntilTick(user);
        if (cooldownUntil > currentTick) {
            final int remain = cooldownUntil - currentTick;
            player.displayClientMessage(
                Component.literal(
                    "聚魂返照冷却中，剩余 " + (remain / TICKS_PER_SECOND) + " 秒"
                ),
                true
            );
            return false;
        }

        final double baseCost = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "activate_zhenyuan_base_cost",
                DEFAULT_ACTIVATE_ZHENYUAN_BASE_COST
            )
        );
        final double realCost = ZhenYuanHelper.calculateGuCost(user, baseCost);
        if (realCost > 0.0 && !ZhenYuanHelper.hasEnough(user, realCost)) {
            player.displayClientMessage(
                Component.literal("真元不足，难以聚魂返照。"),
                true
            );
            return false;
        }

        final int cooldownTicks = getMetaInt(
            usageInfo,
            "cooldown_ticks",
            DEFAULT_COOLDOWN_TICKS
        );
        if (cooldownTicks > 0) {
            setCooldownUntilTick(user, currentTick + cooldownTicks);
        }

        if (realCost > 0.0) {
            ZhenYuanHelper.modify(user, -realCost);
        }

        final double maxSoul = HunPoHelper.getMaxAmount(user);
        final double soulRatio = clamp(
            getMetaDouble(
                usageInfo,
                "soul_restore_ratio",
                DEFAULT_SOUL_RESTORE_RATIO
            ),
            0.0,
            1.0
        );
        final double soulFlat = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "soul_restore_flat",
                DEFAULT_SOUL_RESTORE_FLAT
            )
        );
        final double soulGain = Math.max(0.0, maxSoul * soulRatio + soulFlat);
        if (soulGain > 0.0) {
            HunPoHelper.modify(user, soulGain);
        }

        final double resistanceGain = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "resistance_gain_flat",
                DEFAULT_RESISTANCE_GAIN_FLAT
            )
        );
        if (resistanceGain > 0.0) {
            HunPoHelper.modifyResistance(user, resistanceGain);
        }

        spawnParticles(serverLevel, user);
        return true;
    }

    private static void spawnParticles(
        final ServerLevel level,
        final LivingEntity user
    ) {
        level.sendParticles(
            ParticleTypes.SOUL,
            user.getX(),
            user.getY() + user.getBbHeight() * PARTICLE_Y_FACTOR,
            user.getZ(),
            PARTICLE_COUNT,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPEED
        );
    }

    private static double clamp(
        final double value,
        final double min,
        final double max
    ) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private static int getCooldownUntilTick(final LivingEntity user) {
        return user.getPersistentData().getInt(NBT_COOLDOWN_UNTIL_TICK);
    }

    private static void setCooldownUntilTick(
        final LivingEntity user,
        final int untilTick
    ) {
        user.getPersistentData().putInt(NBT_COOLDOWN_UNTIL_TICK, untilTick);
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

    private static int getMetaInt(
        final NianTouData.Usage usage,
        final String key,
        final int defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Integer.parseInt(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }
}

