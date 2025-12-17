package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

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
 * 三转魂盾蛊：被动【魂盾护体】。
 * <p>
 * 世界观定位：魂魄凝作护盾，既护肉身亦护魂躯，适合近战缠斗中的“以魂换伤”。
 * </p>
 * <p>
 * 机制设计（KISS / 可调参）：
 * <ul>
 *   <li>维持：每秒消耗少量魂魄维持盾形；魂魄不足则本被动暂停。</li>
 *   <li>触发：受到伤害时，按比例吸收一部分伤害，吸收量会额外消耗魂魄。</li>
 *   <li>增幅：魂道道痕提升吸收比例上限，同时也提高消耗，体现“越强越耗”。</li>
 * </ul>
 * </p>
 */
public class HunDunGuSoulShieldEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:hundungu_passive_soul_shield";

    private static final String NBT_ACTIVE = "HunDunGuSoulShieldActive";
    private static final String NBT_LAST_PARTICLE_TICK =
        "HunDunGuLastShieldParticleTick";

    private static final double DEFAULT_MAINTAIN_SOUL_COST_PER_SECOND = 0.10;
    private static final double DEFAULT_BASE_REDUCTION_RATIO = 0.12;
    private static final double DEFAULT_MAX_REDUCTION_RATIO = 0.35;
    private static final double DEFAULT_SOUL_COST_PER_DAMAGE_ABSORBED = 0.20;

    private static final double DAO_HEN_DIVISOR = 1000.0;
    private static final double DEFAULT_DAO_HEN_RATIO_SCALE = 0.10;

    private static final int PARTICLE_COUNT = 10;
    private static final double PARTICLE_SPREAD = 0.35;
    private static final double PARTICLE_SPEED = 0.02;
    private static final double PARTICLE_Y_FACTOR = 0.6;
    private static final int PARTICLE_COOLDOWN_TICKS = 6;

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
            setActive(user, false);
            return;
        }

        if (!(user instanceof Player player)) {
            return;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final double baseCost = getMetaDouble(
            usageInfo,
            "maintain_soul_cost_per_second",
            DEFAULT_MAINTAIN_SOUL_COST_PER_SECOND
        );
        final double maintainCost = Math.max(0.0, baseCost * selfMultiplier);

        if (HunPoHelper.getAmount(player) < maintainCost) {
            setActive(player, false);
            return;
        }

        HunPoHelper.modify(player, -maintainCost);
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

        final double costPerAbsorbed = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "soul_cost_per_damage_absorbed",
                DEFAULT_SOUL_COST_PER_DAMAGE_ABSORBED
            )
        );
        if (costPerAbsorbed <= 0.0) {
            return damage;
        }

        final double availableSoul = HunPoHelper.getAmount(player);
        final double maxAbsorbBySoul = availableSoul / costPerAbsorbed;
        final double absorb = Math.min(desiredAbsorb, maxAbsorbBySoul);
        if (absorb <= 0.0) {
            setActive(player, false);
            return damage;
        }

        HunPoHelper.modify(player, -absorb * costPerAbsorbed);
        spawnShieldParticles(player);

        final float finalDamage = (float) Math.max(0.0, damage - absorb);
        return finalDamage;
    }

    @Override
    public void onUnequip(
        final LivingEntity user,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        setActive(user, false);
    }

    private static boolean isActive(final Player player) {
        return player.getPersistentData().getBoolean(NBT_ACTIVE);
    }

    private static void setActive(
        final LivingEntity user,
        final boolean active
    ) {
        if (user instanceof Player player) {
            player.getPersistentData().putBoolean(NBT_ACTIVE, active);
        }
    }

    private static void spawnShieldParticles(final Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int lastTick = player
            .getPersistentData()
            .getInt(NBT_LAST_PARTICLE_TICK);
        if ((player.tickCount - lastTick) < PARTICLE_COOLDOWN_TICKS) {
            return;
        }
        player
            .getPersistentData()
            .putInt(NBT_LAST_PARTICLE_TICK, player.tickCount);

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
