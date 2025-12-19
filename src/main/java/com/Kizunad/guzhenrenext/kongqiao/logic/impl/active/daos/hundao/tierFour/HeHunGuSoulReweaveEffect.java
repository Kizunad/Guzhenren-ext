package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierFour;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 四转合魂蛊：主动【合魂返照】。
 * <p>
 * 世界观定位：把“护层”（魂魄抗性）反向回流，短瞬稳住魂躯以脱困续航。
 * <ul>
 *   <li>代价：消耗魂魄抗性（hunpo_kangxing），相当于削薄护层。</li>
 *   <li>收益：清理若干常见负面状态，回复少量生命与魂魄。</li>
 *   <li>限制：冷却，避免无脑续命。</li>
 * </ul>
 * </p>
 */
public class HeHunGuSoulReweaveEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:he_hun_gu_active_soul_reweave";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "HeHunGuSoulReweaveCooldownUntilTick";

    private static final int TICKS_PER_SECOND = 20;

    private static final double DEFAULT_RESISTANCE_COST = 15.0;
    private static final double DEFAULT_HEAL_RATIO = 0.08;
    private static final double DEFAULT_SOUL_RESTORE_RATIO = 0.03;
    private static final int DEFAULT_COOLDOWN_TICKS = 200;

    private static final int PARTICLE_COUNT = 20;
    private static final double PARTICLE_SPREAD = 0.35;
    private static final double PARTICLE_SPEED = 0.02;
    private static final double PARTICLE_Y_FACTOR = 0.6;

    private static final List<Holder<MobEffect>> NEGATIVE_EFFECTS =
        List.of(
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.WEAKNESS,
            MobEffects.POISON,
            MobEffects.WITHER,
            MobEffects.BLINDNESS,
            MobEffects.DARKNESS
        );

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
                    "合魂返照冷却中，剩余 " + (remain / TICKS_PER_SECOND) + " 秒"
                ),
                true
            );
            return false;
        }

        final double resistanceCost = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "resistance_cost",
                DEFAULT_RESISTANCE_COST
            )
        );
        if (resistanceCost > 0.0
            && HunPoHelper.getResistance(player) < resistanceCost) {
            player.displayClientMessage(
                Component.literal("魂魄抗性不足，护层难以返照。"),
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

        if (resistanceCost > 0.0) {
            HunPoHelper.modifyResistance(player, -resistanceCost);
        }

        removeCommonNegativeEffects(player);
        applyHealAndSoulRestore(player, usageInfo);
        spawnReweaveParticles(serverLevel, player);
        return true;
    }

    private static void removeCommonNegativeEffects(final LivingEntity user) {
        for (Holder<MobEffect> effect : NEGATIVE_EFFECTS) {
            if (user.hasEffect(effect)) {
                user.removeEffect(effect);
            }
        }
    }

    private static void applyHealAndSoulRestore(
        final LivingEntity user,
        final NianTouData.Usage usageInfo
    ) {
        final double healRatio = clamp(
            getMetaDouble(usageInfo, "heal_ratio", DEFAULT_HEAL_RATIO),
            0.0,
            1.0
        );
        if (healRatio > 0.0) {
            final float healAmount = (float) (user.getMaxHealth() * healRatio);
            if (healAmount > 0.0F) {
                user.heal(healAmount);
            }
        }

        final double soulRatio = clamp(
            getMetaDouble(
                usageInfo,
                "soul_restore_ratio",
                DEFAULT_SOUL_RESTORE_RATIO
            ),
            0.0,
            1.0
        );
        if (soulRatio > 0.0) {
            final double maxSoul = HunPoHelper.getMaxAmount(user);
            final double restore = Math.max(0.0, maxSoul * soulRatio);
            if (restore > 0.0) {
                HunPoHelper.modify(user, restore);
            }
        }
    }

    private static void spawnReweaveParticles(
        final ServerLevel level,
        final LivingEntity user
    ) {
        level.sendParticles(
            ParticleTypes.SOUL_FIRE_FLAME,
            user.getX(),
            user.getY() + user.getBbHeight() * PARTICLE_Y_FACTOR,
            user.getZ(),
            PARTICLE_COUNT,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPEED
        );
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
