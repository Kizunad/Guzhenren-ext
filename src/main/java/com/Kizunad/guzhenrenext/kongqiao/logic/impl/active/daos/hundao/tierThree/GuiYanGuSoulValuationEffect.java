package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuEffectCostHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.UsageMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * 三转鬼眼蛊：主动【估魂取魄】。
 * <p>
 * 功能性定位：瞬间评估周遭凶物魂息，按“目标数量”凝取一缕魂魄补充自身，用作续航与探索补给。
 * </p>
 */
public class GuiYanGuSoulValuationEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:gguiyangu_active_soul_valuation";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "GuiYanGuSoulValuationCooldownUntilTick";

    private static final int TICKS_PER_SECOND = 20;

    private static final double DEFAULT_RADIUS = 10.0;
    private static final double DEFAULT_HUNPO_GAIN_PER_TARGET = 1.5;
    private static final double DEFAULT_MAX_GAIN_PER_ACTIVATION = 18.0;
    private static final int DEFAULT_GLOW_DURATION_TICKS = 80;
    private static final int DEFAULT_COOLDOWN_TICKS = 200;

    private static final int PARTICLE_COUNT_PER_TARGET = 2;
    private static final int MIN_PARTICLE_COUNT = 6;
    private static final int MAX_PARTICLE_COUNT = 24;
    private static final double PARTICLE_SPREAD = 0.25;
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
                    "估魂取魄冷却中，剩余 " + (remain / TICKS_PER_SECOND) + " 秒"
                ),
                true
            );
            return false;
        }

        if (!GuEffectCostHelper.tryConsumeOnce(player, user, usageInfo)) {
            return false;
        }

        final int cooldownTicks = UsageMetadataHelper.getInt(
            usageInfo,
            "cooldown_ticks",
            DEFAULT_COOLDOWN_TICKS
        );
        if (cooldownTicks > 0) {
            setCooldownUntilTick(user, currentTick + cooldownTicks);
        }

        final double radius = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(usageInfo, "radius", DEFAULT_RADIUS)
        );
        final int glowDuration = Math.max(
            0,
            UsageMetadataHelper.getInt(
                usageInfo,
                "glow_duration_ticks",
                DEFAULT_GLOW_DURATION_TICKS
            )
        );

        final List<LivingEntity> targets = findTargets(user, radius);
        if (!targets.isEmpty() && glowDuration > 0) {
            for (LivingEntity target : targets) {
                target.addEffect(
                    new MobEffectInstance(
                        MobEffects.GLOWING,
                        glowDuration,
                        0,
                        true,
                        false,
                        true
                    )
                );
            }
        }

        final double gainPerTarget = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                "hunpo_gain_per_target",
                DEFAULT_HUNPO_GAIN_PER_TARGET
            )
        );
        final double maxGain = Math.max(
            0.0,
            UsageMetadataHelper.getDouble(
                usageInfo,
                "max_gain_per_activation",
                DEFAULT_MAX_GAIN_PER_ACTIVATION
            )
        );
        final double multiplier = DaoHenCalculator.calculateSelfMultiplier(
            user,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final double gain = Math.min(
            maxGain,
            gainPerTarget * targets.size()
        ) * multiplier;
        if (gain > 0.0) {
            HunPoHelper.modify(user, gain);
        }

        spawnParticles(serverLevel, user, targets);
        return true;
    }

    private static List<LivingEntity> findTargets(
        final LivingEntity user,
        final double radius
    ) {
        if (radius <= 0.0) {
            return List.of();
        }
        final AABB area = user.getBoundingBox().inflate(radius, radius, radius);
        return user.level().getEntitiesOfClass(
            LivingEntity.class,
            area,
            entity -> entity != user
                && entity.isAlive()
                && entity instanceof Monster
        );
    }

    private static void spawnParticles(
        final ServerLevel level,
        final LivingEntity user,
        final List<LivingEntity> targets
    ) {
        level.sendParticles(
            ParticleTypes.SOUL,
            user.getX(),
            user.getY() + user.getBbHeight() * PARTICLE_Y_FACTOR,
            user.getZ(),
            clampInt(
                targets.size() * PARTICLE_COUNT_PER_TARGET,
                MIN_PARTICLE_COUNT,
                MAX_PARTICLE_COUNT
            ),
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPEED
        );
    }

    private static int clampInt(final int value, final int min, final int max) {
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

}
