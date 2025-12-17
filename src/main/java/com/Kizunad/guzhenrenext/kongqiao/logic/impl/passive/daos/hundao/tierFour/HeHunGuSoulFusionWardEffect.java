package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierFour;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 合魂蛊：被动【合魂护魄】。
 * <p>
 * 与“合练魂魄”（把低转魂魄炼为高转魂魄）的路径不同，这里把合魂理解为“结构融合与稳定化”：
 * 把魂魄中的杂乱片段合并为更稳固的护层（魂魄抗性），用于战斗中卸去部分冲击。
 * <ul>
 *   <li>充能：每秒消耗魂魄，将其转化为魂魄抗性（hunpo_kangxing）。</li>
 *   <li>上限：动态写入 hunpo_kangxing_shangxian，至少 +1，避免为 0；上限受魂道道痕自增幅。</li>
 *   <li>护层：受伤时按比例消耗魂魄抗性抵消伤害，粒子对周围玩家可见。</li>
 * </ul>
 * </p>
 */
public class HeHunGuSoulFusionWardEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:he_hun_gu_passive_soul_fusion_ward";

    private static final String NBT_ACTIVE = "HeHunGuSoulFusionWardActive";
    private static final String NBT_ORIGINAL_MAX_RESISTANCE =
        "HeHunGuOriginalMaxResistance";
    private static final String NBT_LAST_PARTICLE_TICK =
        "HeHunGuLastFusionParticleTick";

    private static final double DEFAULT_SOUL_COST_PER_SECOND = 0.60;
    private static final double DEFAULT_RESISTANCE_GAIN_PER_SECOND = 1.50;

    private static final double DEFAULT_MAX_RESISTANCE_BASE = 1.0;
    private static final double DEFAULT_MAX_RESISTANCE_HUNPO_RATIO = 0.50;

    private static final double DEFAULT_BASE_REDUCTION_RATIO = 0.15;
    private static final double DEFAULT_MAX_REDUCTION_RATIO = 0.40;
    private static final double DEFAULT_RESISTANCE_PER_DAMAGE_ABSORBED = 1.0;

    private static final double DAO_HEN_DIVISOR = 1000.0;
    private static final double DEFAULT_DAO_HEN_RATIO_SCALE = 0.12;

    private static final int PARTICLE_COUNT = 12;
    private static final double PARTICLE_SPREAD = 0.35;
    private static final double PARTICLE_SPEED = 0.03;
    private static final double PARTICLE_Y_FACTOR = 0.6;
    private static final int PARTICLE_COOLDOWN_TICKS = 6;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onEquip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (!(user instanceof Player player)) {
            return;
        }
        if (!player.getPersistentData().contains(NBT_ORIGINAL_MAX_RESISTANCE)) {
            player
                .getPersistentData()
                .putDouble(
                    NBT_ORIGINAL_MAX_RESISTANCE,
                    HunPoHelper.getMaxResistance(player)
                );
        }
        applyMaxResistance(player, usageInfo);
        setActive(player, false);
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

        applyMaxResistance(player, usageInfo);

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );

        final double soulCost = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "soul_cost_per_second",
                DEFAULT_SOUL_COST_PER_SECOND
            ) * selfMultiplier
        );
        if (HunPoHelper.getAmount(player) < soulCost) {
            setActive(player, false);
            return;
        }

        HunPoHelper.modify(player, -soulCost);
        setActive(player, true);

        final double gain = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "resistance_gain_per_second",
                DEFAULT_RESISTANCE_GAIN_PER_SECOND
            ) * selfMultiplier
        );
        if (gain > 0.0) {
            HunPoHelper.modifyResistance(player, gain);
        }
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
        if (damage <= 0.0F) {
            return damage;
        }
        if (!isActive(player)) {
            return damage;
        }

        final double daoHen = DaoHenHelper.getDaoHen(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final double ratioScale = getMetaDouble(
            usageInfo,
            "dao_hen_ratio_scale",
            DEFAULT_DAO_HEN_RATIO_SCALE
        );
        final double baseRatio = clamp(
            getMetaDouble(
                usageInfo,
                "base_reduction_ratio",
                DEFAULT_BASE_REDUCTION_RATIO
            ),
            0.0,
            1.0
        );
        final double maxRatio = clamp(
            getMetaDouble(
                usageInfo,
                "max_reduction_ratio",
                DEFAULT_MAX_REDUCTION_RATIO
            ),
            0.0,
            1.0
        );
        final double ratio = clamp(
            baseRatio + (daoHen / DAO_HEN_DIVISOR) * ratioScale,
            0.0,
            maxRatio
        );

        final double desiredAbsorb = damage * ratio;
        if (desiredAbsorb <= 0.0) {
            return damage;
        }

        final double resistancePerDamage = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "resistance_per_damage_absorbed",
                DEFAULT_RESISTANCE_PER_DAMAGE_ABSORBED
            )
        );
        if (resistancePerDamage <= 0.0) {
            return damage;
        }

        final double availableResistance = HunPoHelper.getResistance(player);
        final double maxAbsorbByResistance =
            availableResistance / resistancePerDamage;
        final double absorb = Math.min(desiredAbsorb, maxAbsorbByResistance);
        if (absorb <= 0.0) {
            return damage;
        }

        HunPoHelper.modifyResistance(player, -absorb * resistancePerDamage);
        spawnWardParticles(player);

        final float finalDamage = (float) Math.max(0.0, damage - absorb);
        return finalDamage;
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        deactivate(user);
    }

    private static boolean isActive(final Player player) {
        return player.getPersistentData().getBoolean(NBT_ACTIVE);
    }

    private static void setActive(final Player player, final boolean active) {
        player.getPersistentData().putBoolean(NBT_ACTIVE, active);
    }

    private static void deactivate(final LivingEntity user) {
        if (!(user instanceof Player player)) {
            return;
        }
        setActive(player, false);
        restoreOriginalMaxResistance(player);
    }

    private static void restoreOriginalMaxResistance(final Player player) {
        if (!player.getPersistentData().contains(NBT_ORIGINAL_MAX_RESISTANCE)) {
            return;
        }
        double original = player
            .getPersistentData()
            .getDouble(NBT_ORIGINAL_MAX_RESISTANCE);
        HunPoHelper.setMaxResistance(player, original);
        player.getPersistentData().remove(NBT_ORIGINAL_MAX_RESISTANCE);
    }

    private static void applyMaxResistance(
        final Player player,
        final NianTouData.Usage usageInfo
    ) {
        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );

        final double base = Math.max(
            1.0,
            getMetaDouble(
                usageInfo,
                "max_resistance_base",
                DEFAULT_MAX_RESISTANCE_BASE
            )
        );
        final double hunpoRatio = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "max_resistance_hunpo_ratio",
                DEFAULT_MAX_RESISTANCE_HUNPO_RATIO
            )
        );

        final double maxHunpo = Math.max(0.0, HunPoHelper.getMaxAmount(player));
        double desired = (base + maxHunpo * hunpoRatio) * selfMultiplier;

        // 额外 +1：保证上限字段不为 0，避免 UI/逻辑分母为 0 的问题。
        desired = Math.max(desired, 1.0);

        // 避免把玩家现有抗性“突然压低”（例如历史数据里上限为 0 但抗性已被写入）。
        desired = Math.max(desired, HunPoHelper.getResistance(player));

        HunPoHelper.setMaxResistance(player, desired);
    }

    private static void spawnWardParticles(final Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int lastTick = player
            .getPersistentData()
            .getInt(NBT_LAST_PARTICLE_TICK);
        if ((player.tickCount - lastTick) < PARTICLE_COOLDOWN_TICKS) {
            return;
        }
        player.getPersistentData().putInt(NBT_LAST_PARTICLE_TICK, player.tickCount);

        serverLevel.sendParticles(
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
        serverLevel.sendParticles(
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

    private static double getMetaDouble(
        final NianTouData.Usage usage,
        final String key,
        final double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }
}

