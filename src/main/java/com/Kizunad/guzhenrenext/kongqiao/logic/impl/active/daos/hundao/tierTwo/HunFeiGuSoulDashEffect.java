package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 二转魂飞蛊：主动【魂翼疾掠】。
 * <p>
 * 世界观定位：以魂为翼，借势疾掠，主打空战位移与地形穿插。
 * <ul>
 *   <li>限制：必须在空中（离地）才能施展，避免地面无脑位移。</li>
 *   <li>代价：消耗魂魄与真元，并有冷却，避免高频刷屏。</li>
 *   <li>保护：附带短暂缓降，防止位移后摔落失控。</li>
 * </ul>
 * </p>
 */
public class HunFeiGuSoulDashEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:hunfeigu_active_soul_dash";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "HunFeiGuSoulDashCooldownUntilTick";

    private static final int TICKS_PER_SECOND = 20;

    private static final double DEFAULT_ACTIVATE_SOUL_COST = 1.50;
    private static final double DEFAULT_ACTIVATE_ZHENYUAN_BASE_COST = 80.0;

    private static final double DEFAULT_DASH_FORWARD_SPEED = 1.25;
    private static final double DEFAULT_DASH_UP_SPEED = 0.25;
    private static final int DEFAULT_SLOW_FALLING_TICKS = 60;
    private static final int DEFAULT_COOLDOWN_TICKS = 80;

    private static final double FORWARD_EPSILON_SQR = 1.0E-6;
    private static final int TRAIL_PARTICLE_COUNT = 18;
    private static final double TRAIL_SPREAD = 0.05;
    private static final double TRAIL_SPEED = 0.01;
    private static final double TRAIL_Y_FACTOR = 0.55;

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
                    "魂翼疾掠冷却中，剩余 " + (remain / TICKS_PER_SECOND) + " 秒"
                ),
                true
            );
            return false;
        }

        if (player.onGround()) {
            player.displayClientMessage(
                Component.literal("需在空中方可施展【魂翼疾掠】。"),
                true
            );
            return false;
        }

        final double soulCost = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "activate_soul_cost",
                DEFAULT_ACTIVATE_SOUL_COST
            )
        );
        final double zhenyuanBaseCost = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "activate_zhenyuan_base_cost",
                DEFAULT_ACTIVATE_ZHENYUAN_BASE_COST
            )
        );
        final double realZhenyuanCost =
            ZhenYuanHelper.calculateGuCost(user, zhenyuanBaseCost);

        if (!hasEnoughCost(user, soulCost, realZhenyuanCost)) {
            player.displayClientMessage(
                Component.literal("魂魄/真元不足，魂翼难振。"),
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

        consumeCost(user, soulCost, realZhenyuanCost);

        final double forwardSpeed = getMetaDouble(
            usageInfo,
            "dash_forward_speed",
            DEFAULT_DASH_FORWARD_SPEED
        );
        final double upSpeed = getMetaDouble(
            usageInfo,
            "dash_up_speed",
            DEFAULT_DASH_UP_SPEED
        );

        applyDash(player, forwardSpeed, upSpeed);

        final int slowFallingTicks = getMetaInt(
            usageInfo,
            "slow_falling_ticks",
            DEFAULT_SLOW_FALLING_TICKS
        );
        if (slowFallingTicks > 0) {
            player.addEffect(
                new MobEffectInstance(
                    MobEffects.SLOW_FALLING,
                    slowFallingTicks,
                    0,
                    true,
                    false,
                    true
                )
            );
        }

        spawnTrailParticles(serverLevel, player);
        return true;
    }

    private static void applyDash(
        final Player player,
        final double forwardSpeed,
        final double upSpeed
    ) {
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0, look.z);
        if (forward.lengthSqr() > FORWARD_EPSILON_SQR) {
            forward = forward.normalize().scale(forwardSpeed);
        } else {
            forward = Vec3.ZERO;
        }

        final Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(
            motion.x + forward.x,
            motion.y + upSpeed,
            motion.z + forward.z
        );
        player.fallDistance = 0.0F;
        player.hasImpulse = true;
    }

    private static void spawnTrailParticles(
        final ServerLevel level,
        final Player player
    ) {
        level.sendParticles(
            ParticleTypes.SOUL_FIRE_FLAME,
            player.getX(),
            player.getY() + player.getBbHeight() * TRAIL_Y_FACTOR,
            player.getZ(),
            TRAIL_PARTICLE_COUNT,
            TRAIL_SPREAD,
            TRAIL_SPREAD,
            TRAIL_SPREAD,
            TRAIL_SPEED
        );
    }

    private static boolean hasEnoughCost(
        final LivingEntity user,
        final double soulCost,
        final double zhenyuanCost
    ) {
        if (soulCost > 0.0 && HunPoHelper.getAmount(user) < soulCost) {
            return false;
        }
        if (zhenyuanCost > 0.0 && !ZhenYuanHelper.hasEnough(user, zhenyuanCost)) {
            return false;
        }
        return true;
    }

    private static void consumeCost(
        final LivingEntity user,
        final double soulCost,
        final double zhenyuanCost
    ) {
        if (soulCost > 0.0) {
            HunPoHelper.modify(user, -soulCost);
        }
        if (zhenyuanCost > 0.0) {
            ZhenYuanHelper.modify(user, -zhenyuanCost);
        }
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

