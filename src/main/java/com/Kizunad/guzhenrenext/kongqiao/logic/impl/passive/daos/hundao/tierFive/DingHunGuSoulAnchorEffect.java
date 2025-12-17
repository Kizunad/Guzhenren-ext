package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierFive;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.Map;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * 五转：定魂蛊 - 魂钉锁命（被动）
 * <p>
 * 原著风味：魂魄如风，易散易乱；定魂之妙，在于“钉住魂根”，让魂躯不至于在绝境中崩散。
 * </p>
 */
public class DingHunGuSoulAnchorEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:ding_hun_gu_passive_soul_anchor";

    private static final String NBT_ACTIVE = "DingHunGuSoulAnchorActive";
    private static final String NBT_LAST_SAVE_TICK = "DingHunGuLastSaveTick";

    private static final double DEFAULT_MAINTAIN_SOUL_COST_PER_SECOND = 0.80;
    private static final double DEFAULT_SOUL_COST_ON_SAVE = 60.0;

    private static final int DEFAULT_SAVE_COOLDOWN_TICKS = 24000;

    private static final double DEFAULT_SURVIVE_RATIO_BASE = 0.06;
    private static final double DEFAULT_SURVIVE_RATIO_SCALE = 0.02;
    private static final double DEFAULT_SURVIVE_RATIO_MAX = 0.22;

    private static final double DAO_HEN_DIVISOR = 1000.0;

    private static final float MIN_DAMAGE_EPSILON = 0.0F;

    private static final int ANCHOR_PARTICLE_COUNT = 32;
    private static final double ANCHOR_PARTICLE_RADIUS = 1.2;
    private static final double ANCHOR_PARTICLE_HEIGHT = 0.8;
    private static final double ANCHOR_PARTICLE_SPEED = 0.02;
    private static final double ANCHOR_PARTICLE_JITTER = 0.15;

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
            deactivate(user);
            return;
        }

        if (!(user instanceof Player player)) {
            return;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final double baseMaintain = getMetaDouble(
            usageInfo,
            "maintain_soul_cost_per_second",
            DEFAULT_MAINTAIN_SOUL_COST_PER_SECOND
        );
        final double maintainCost = Math.max(
            0.0,
            baseMaintain * selfMultiplier
        );

        if (HunPoHelper.getAmount(player) < maintainCost) {
            setActive(player, false);
            return;
        }

        if (maintainCost > 0.0) {
            HunPoHelper.modify(player, -maintainCost);
        }
        setActive(player, true);
    }

    @Override
    public float onHurt(
        final LivingEntity victim,
        final DamageSource source,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (victim.level().isClientSide()) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(victim);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            return damage;
        }

        if (!(victim instanceof Player player)) {
            return damage;
        }
        if (damage <= MIN_DAMAGE_EPSILON) {
            return damage;
        }
        if (!isActive(player)) {
            return damage;
        }
        if (shouldIgnore(source)) {
            return damage;
        }

        final float currentHealth = player.getHealth();
        if (damage < currentHealth) {
            return damage;
        }

        final long nowTick = player.level().getGameTime();
        final int cooldownTicks = Math.max(
            0,
            getMetaInt(
                usageInfo,
                "save_cooldown_ticks",
                DEFAULT_SAVE_COOLDOWN_TICKS
            )
        );
        if (!isCooldownReady(player, nowTick, cooldownTicks)) {
            return damage;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final double baseSaveCost = getMetaDouble(
            usageInfo,
            "soul_cost_on_save",
            DEFAULT_SOUL_COST_ON_SAVE
        );
        final double saveCost = Math.max(0.0, baseSaveCost * selfMultiplier);
        if (HunPoHelper.getAmount(player) < saveCost) {
            return damage;
        }

        final double daoHen = DaoHenHelper.getDaoHen(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final double ratioBase = clamp(
            getMetaDouble(
                usageInfo,
                "survive_health_ratio_base",
                DEFAULT_SURVIVE_RATIO_BASE
            ),
            0.0,
            1.0
        );
        final double ratioMax = clamp(
            getMetaDouble(
                usageInfo,
                "survive_health_ratio_max",
                DEFAULT_SURVIVE_RATIO_MAX
            ),
            0.0,
            1.0
        );
        final double ratioScale = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "dao_hen_survive_ratio_scale",
                DEFAULT_SURVIVE_RATIO_SCALE
            )
        );

        final double desiredRatio = clamp(
            ratioBase + (daoHen / DAO_HEN_DIVISOR) * ratioScale,
            0.0,
            ratioMax
        );
        final double desiredRemainingHealth =
            player.getMaxHealth() * desiredRatio;
        final float targetRemainingHealth = (float) Math.min(
            currentHealth,
            Math.max(1.0, desiredRemainingHealth)
        );
        final float adjustedDamage = (float) Math.max(
            0.0,
            currentHealth - targetRemainingHealth
        );

        HunPoHelper.modify(player, -saveCost);
        markCooldown(player, nowTick);
        spawnAnchorParticles(player);

        return adjustedDamage;
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        deactivate(user);
    }

    private static boolean shouldIgnore(final DamageSource source) {
        return (
            source.is(DamageTypes.FELL_OUT_OF_WORLD) ||
            source.is(DamageTypes.GENERIC_KILL)
        );
    }

    private static boolean isCooldownReady(
        final Player player,
        final long nowTick,
        final int cooldownTicks
    ) {
        final long last = player
            .getPersistentData()
            .getLong(NBT_LAST_SAVE_TICK);
        return nowTick - last >= cooldownTicks;
    }

    private static void markCooldown(final Player player, final long nowTick) {
        player.getPersistentData().putLong(NBT_LAST_SAVE_TICK, nowTick);
    }

    private static void spawnAnchorParticles(final Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        final Vec3 center = player
            .position()
            .add(0.0, ANCHOR_PARTICLE_HEIGHT, 0.0);
        for (int i = 0; i < ANCHOR_PARTICLE_COUNT; i++) {
            final double angle = ((Math.PI * 2.0) * i) / ANCHOR_PARTICLE_COUNT;
            final double x =
                center.x + Math.cos(angle) * ANCHOR_PARTICLE_RADIUS;
            final double z =
                center.z + Math.sin(angle) * ANCHOR_PARTICLE_RADIUS;

            serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                x,
                center.y,
                z,
                1,
                ANCHOR_PARTICLE_JITTER,
                ANCHOR_PARTICLE_JITTER,
                ANCHOR_PARTICLE_JITTER,
                ANCHOR_PARTICLE_SPEED
            );
            if ((i & 1) == 0) {
                serverLevel.sendParticles(
                    ParticleTypes.ENCHANT,
                    x,
                    center.y,
                    z,
                    1,
                    ANCHOR_PARTICLE_JITTER,
                    ANCHOR_PARTICLE_JITTER,
                    ANCHOR_PARTICLE_JITTER,
                    ANCHOR_PARTICLE_SPEED
                );
            }
        }
    }

    private static void deactivate(final LivingEntity user) {
        if (user instanceof Player player) {
            setActive(player, false);
        }
    }

    private static boolean isActive(final Player player) {
        return player.getPersistentData().getBoolean(NBT_ACTIVE);
    }

    private static void setActive(final Player player, final boolean active) {
        player.getPersistentData().putBoolean(NBT_ACTIVE, active);
    }

    private static double clamp(
        final double value,
        final double min,
        final double max
    ) {
        return Math.max(min, Math.min(max, value));
    }

    private static double getMetaDouble(
        final NianTouData.Usage usageInfo,
        final String key,
        final double defaultValue
    ) {
        final Map<String, String> meta = usageInfo.metadata();
        if (meta == null) {
            return defaultValue;
        }
        final String value = meta.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static int getMetaInt(
        final NianTouData.Usage usageInfo,
        final String key,
        final int defaultValue
    ) {
        final Map<String, String> meta = usageInfo.metadata();
        if (meta == null) {
            return defaultValue;
        }
        final String value = meta.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
