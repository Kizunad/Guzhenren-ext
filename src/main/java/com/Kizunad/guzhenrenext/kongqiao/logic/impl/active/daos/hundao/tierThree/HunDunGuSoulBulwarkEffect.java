package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 三转魂盾蛊：主动【魂盾凝壁】。
 * <p>
 * 设计目标：给魂盾蛊一个“瞬时防御爆发”的窗口，补足只靠被动卸劲时的短板。
 * <ul>
 *   <li>施放：消耗魂魄与真元，获得短时吸收与抗性。</li>
 *   <li>增幅：吸收等级随魂道道痕提升（有上限），体现魂道越深越能“立壁”。</li>
 *   <li>限制：冷却避免高频强行顶伤害。</li>
 * </ul>
 * </p>
 */
public class HunDunGuSoulBulwarkEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:hundungu_active_soul_bulwark";

    private static final String NBT_COOLDOWN_UNTIL_TICK =
        "HunDunGuSoulBulwarkCooldownUntilTick";

    private static final int TICKS_PER_SECOND = 20;

    private static final double DEFAULT_ACTIVATE_SOUL_COST = 8.0;
    private static final double DEFAULT_ACTIVATE_ZHENYUAN_BASE_COST = 120.0;
    private static final int DEFAULT_DURATION_TICKS = 80;
    private static final int DEFAULT_COOLDOWN_TICKS = 160;

    private static final int DEFAULT_ABSORPTION_BASE_LEVEL = 0;
    private static final int DEFAULT_ABSORPTION_MAX_LEVEL = 2;
    private static final int DEFAULT_RESISTANCE_AMPLIFIER = 0;

    private static final double DAO_HEN_DIVISOR = 1000.0;

    private static final int PARTICLE_COUNT = 18;
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
                    "魂盾凝壁冷却中，剩余 " + (remain / TICKS_PER_SECOND) + " 秒"
                ),
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
                Component.literal("魂魄/真元不足，魂盾难凝。"),
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

        final int duration = Math.max(
            1,
            getMetaInt(usageInfo, "duration_ticks", DEFAULT_DURATION_TICKS)
        );

        final int absorptionAmplifier = calculateAbsorptionAmplifier(
            player,
            usageInfo
        );
        final int resistanceAmplifier = Math.max(
            0,
            getMetaInt(
                usageInfo,
                "resistance_amplifier",
                DEFAULT_RESISTANCE_AMPLIFIER
            )
        );

        applyEffect(
            player,
            MobEffects.ABSORPTION,
            duration,
            absorptionAmplifier
        );
        applyEffect(
            player,
            MobEffects.DAMAGE_RESISTANCE,
            duration,
            resistanceAmplifier
        );

        spawnBulwarkParticles(serverLevel, player);
        return true;
    }

    private static void applyEffect(
        final Player player,
        final Holder<MobEffect> effect,
        final int duration,
        final int amplifier
    ) {
        player.addEffect(
            new MobEffectInstance(effect, duration, amplifier, true, true, true)
        );
    }

    private static int calculateAbsorptionAmplifier(
        final Player player,
        final NianTouData.Usage usageInfo
    ) {
        final int base = Math.max(
            0,
            getMetaInt(
                usageInfo,
                "absorption_base_level",
                DEFAULT_ABSORPTION_BASE_LEVEL
            )
        );
        final int max = Math.max(
            base,
            getMetaInt(
                usageInfo,
                "absorption_max_level",
                DEFAULT_ABSORPTION_MAX_LEVEL
            )
        );

        final double daoHen = DaoHenHelper.getDaoHen(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final int bonus = (int) Math.floor(daoHen / DAO_HEN_DIVISOR);
        return Math.min(max, base + bonus);
    }

    private static void spawnBulwarkParticles(
        final ServerLevel level,
        final Player player
    ) {
        level.sendParticles(
            ParticleTypes.SOUL_FIRE_FLAME,
            player.getX(),
            player.getY() + player.getBbHeight() * PARTICLE_Y_FACTOR,
            player.getZ(),
            PARTICLE_COUNT,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPEED
        );
        level.sendParticles(
            ParticleTypes.SOUL,
            player.getX(),
            player.getY() + player.getBbHeight() * PARTICLE_Y_FACTOR,
            player.getZ(),
            PARTICLE_COUNT,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPEED
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
