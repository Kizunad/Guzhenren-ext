package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierFive;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 五转魂爆蛊：被动【魂爆连环】。
 * <p>
 * 世界观定位：魂爆蛊并非“单次大招”，而是把魂魄碎片埋入敌魂，随后引爆连锁冲击。
 * <ul>
 *   <li>叠印：每次攻击消耗少量魂魄，为目标叠加“魂爆印”（可配置上限/持续时间）。</li>
 *   <li>引爆：当印记层数达到阈值，或预计本次伤害会击杀/压到低血线时，触发一次魂爆。</li>
 *   <li>魂爆：对周围敌人造成高额魂道伤害（带距离衰减），并短暂减速；粒子对周围玩家可见。</li>
 * </ul>
 * 作为五转效果，基础数值偏强，但持续消耗也更重；魂道道痕会显著放大爆发与消耗。</p>
 */
public class HunBaoGuChainDetonationEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:hun_bao_gu_passive_chain_detonation";

    private static final String NBT_MARK_OWNER = "HunBaoGuMarkOwner";
    private static final String NBT_MARK_STACKS = "HunBaoGuMarkStacks";
    private static final String NBT_MARK_EXPIRE_TICK = "HunBaoGuMarkExpireTick";

    private static final String NBT_LAST_DETONATE_TICK =
        "HunBaoGuLastDetonateTick";

    private static final double DEFAULT_SOUL_COST_PER_STACK = 1.20;
    private static final int DEFAULT_MAX_STACKS = 6;
    private static final int DEFAULT_DETONATE_STACKS = 4;
    private static final int DEFAULT_MARK_TIMEOUT_TICKS = 100;

    private static final float DEFAULT_DETONATE_HEALTH_THRESHOLD = 0.25F;
    private static final int DEFAULT_DETONATE_COOLDOWN_TICKS = 8;

    private static final double DEFAULT_SOUL_COST_PER_DETONATION = 18.0;
    private static final double DEFAULT_BASE_SOUL_DAMAGE = 140.0;
    private static final double DEFAULT_BONUS_SOUL_DAMAGE_PER_STACK = 25.0;
    private static final double DEFAULT_RADIUS = 4.5;
    private static final double DEFAULT_DISTANCE_FALLOFF = 0.12;
    private static final double DEFAULT_KNOCKBACK = 1.1;

    private static final int DEFAULT_SLOW_DURATION_TICKS = 20;
    private static final int DEFAULT_SLOW_AMPLIFIER = 1;

    private static final int MARK_PARTICLE_COUNT = 3;
    private static final int DETONATION_PARTICLE_COUNT = 20;
    private static final double PARTICLE_SPREAD = 0.35;
    private static final double PARTICLE_SPEED = 0.03;
    private static final double PARTICLE_Y_FACTOR = 0.6;
    private static final int RING_POINTS = 24;
    private static final double RING_Y_OFFSET = 0.4;
    private static final double HALF = 0.5;
    private static final double VECTOR_EPSILON_SQUARED = 0.0001;
    private static final double KNOCKBACK_Y_FACTOR = 0.1;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public float onAttack(
        final LivingEntity attacker,
        final LivingEntity target,
        final float damage,
        final ItemStack stack,
        final NianTouData.Usage usageInfo
    ) {
        if (attacker.level().isClientSide()) {
            return damage;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(attacker);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            return damage;
        }

        if (!(attacker instanceof Player player)) {
            return damage;
        }
        if (damage <= 0.0F || target == null || !target.isAlive()) {
            return damage;
        }

        final int markTimeout = getMetaInt(
            usageInfo,
            "mark_timeout_ticks",
            DEFAULT_MARK_TIMEOUT_TICKS
        );
        final int maxStacks = Math.max(
            1,
            getMetaInt(usageInfo, "max_stacks", DEFAULT_MAX_STACKS)
        );
        final int detonateStacks = Math.max(
            1,
            getMetaInt(usageInfo, "detonate_stacks", DEFAULT_DETONATE_STACKS)
        );

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final double soulCostPerStack = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "soul_cost_per_stack",
                DEFAULT_SOUL_COST_PER_STACK
            ) * selfMultiplier
        );
        if (HunPoHelper.getAmount(player) < soulCostPerStack) {
            return damage;
        }

        final int stacks = applyOrIncreaseMark(
            player,
            target,
            maxStacks,
            markTimeout
        );
        HunPoHelper.modify(player, -soulCostPerStack);
        spawnMarkParticles(target);

        final float healthThreshold = getMetaFloat(
            usageInfo,
            "detonate_health_threshold",
            DEFAULT_DETONATE_HEALTH_THRESHOLD
        );

        final boolean predictedKill = isPredictedKill(target, damage);
        final boolean predictedLowHealth = isPredictedLowHealth(
            target,
            damage,
            healthThreshold
        );
        final boolean reachedStacks = stacks >= detonateStacks;

        if (!(predictedKill || predictedLowHealth || reachedStacks)) {
            return damage;
        }

        final int cooldown = Math.max(
            0,
            getMetaInt(
                usageInfo,
                "detonate_cooldown_ticks",
                DEFAULT_DETONATE_COOLDOWN_TICKS
            )
        );
        if (!isCooldownReady(player, cooldown)) {
            return damage;
        }

        if (detonate(player, target, stacks, usageInfo)) {
            markCooldown(player);
            clearMark(target);
        }
        return damage;
    }

    private boolean detonate(
        final Player player,
        final LivingEntity center,
        final int stacks,
        final NianTouData.Usage usageInfo
    ) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        final double selfMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.HUN_DAO
        );
        final double soulCost = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "soul_cost_per_detonation",
                DEFAULT_SOUL_COST_PER_DETONATION
            ) * selfMultiplier
        );
        if (HunPoHelper.getAmount(player) < soulCost) {
            return false;
        }
        HunPoHelper.modify(player, -soulCost);

        final double baseDamage = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "base_soul_damage",
                DEFAULT_BASE_SOUL_DAMAGE
            )
        );
        final double bonusPerStack = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "bonus_soul_damage_per_stack",
                DEFAULT_BONUS_SOUL_DAMAGE_PER_STACK
            )
        );
        final double radius = Math.max(
            0.0,
            getMetaDouble(usageInfo, "radius", DEFAULT_RADIUS)
        );
        final double falloff = Math.max(
            0.0,
            getMetaDouble(
                usageInfo,
                "distance_falloff",
                DEFAULT_DISTANCE_FALLOFF
            )
        );
        final double knockback = Math.max(
            0.0,
            getMetaDouble(usageInfo, "knockback", DEFAULT_KNOCKBACK)
        );

        final int slowDuration = Math.max(
            0,
            getMetaInt(
                usageInfo,
                "slow_duration_ticks",
                DEFAULT_SLOW_DURATION_TICKS
            )
        );
        final int slowAmplifier = Math.max(
            0,
            getMetaInt(
                usageInfo,
                "slow_amplifier",
                DEFAULT_SLOW_AMPLIFIER
            )
        );

        final double rawDamage = baseDamage + stacks * bonusPerStack;
        final DamageSource soulDamage = createHunpoDamageSource(player);

        final Vec3 origin = center.position().add(0, center.getBbHeight() * HALF, 0);
        final AABB box = center.getBoundingBox().inflate(radius);
        final List<LivingEntity> victims = serverLevel.getEntitiesOfClass(
            LivingEntity.class,
            box,
            entity -> shouldAffect(player, entity)
        );

        for (LivingEntity victim : victims) {
            double dist = Math.max(0.0, victim.distanceTo(center));
            double distanceMultiplier = clamp(1.0 - dist * falloff, 0.0, 1.0);

            double daoMultiplier = DaoHenCalculator.calculateMultiplier(
                player,
                victim,
                DaoHenHelper.DaoType.HUN_DAO
            );
            double finalDamage = rawDamage * distanceMultiplier * daoMultiplier;
            if (finalDamage > 0.0) {
                victim.hurt(soulDamage, (float) finalDamage);
            }

            if (slowDuration > 0) {
                victim.addEffect(
                    new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        slowDuration,
                        slowAmplifier,
                        false,
                        true
                    )
                );
            }

            applyKnockback(origin, victim, knockback * distanceMultiplier);
        }

        spawnDetonationParticles(serverLevel, origin, radius);
        return true;
    }

    private static boolean shouldAffect(final Player owner, final LivingEntity entity) {
        if (entity == null || !entity.isAlive()) {
            return false;
        }
        if (entity == owner) {
            return false;
        }
        if (entity instanceof TamableAnimal tamable && tamable.isOwnedBy(owner)) {
            return false;
        }
        return true;
    }

    private static void applyKnockback(
        final Vec3 origin,
        final LivingEntity victim,
        final double strength
    ) {
        if (strength <= 0.0) {
            return;
        }
        Vec3 delta = victim.position().subtract(origin);
        double lenSqr = delta.lengthSqr();
        if (lenSqr <= VECTOR_EPSILON_SQUARED) {
            return;
        }
        Vec3 dir = delta.normalize();
        victim.push(
            dir.x * strength,
            KNOCKBACK_Y_FACTOR * strength,
            dir.z * strength
        );
    }

    private static int applyOrIncreaseMark(
        final Player owner,
        final LivingEntity target,
        final int maxStacks,
        final int timeoutTicks
    ) {
        final UUID ownerId = owner.getUUID();
        final int now = owner.tickCount;

        var tag = target.getPersistentData();
        boolean sameOwner = tag.hasUUID(NBT_MARK_OWNER) && ownerId.equals(tag.getUUID(NBT_MARK_OWNER));
        int expire = tag.getInt(NBT_MARK_EXPIRE_TICK);
        boolean expired = expire > 0 && now > expire;

        int stacks = tag.getInt(NBT_MARK_STACKS);
        if (!sameOwner || expired || stacks <= 0) {
            stacks = 1;
        } else {
            stacks = Math.min(maxStacks, stacks + 1);
        }

        tag.putUUID(NBT_MARK_OWNER, ownerId);
        tag.putInt(NBT_MARK_STACKS, stacks);
        tag.putInt(NBT_MARK_EXPIRE_TICK, now + Math.max(1, timeoutTicks));
        return stacks;
    }

    private static void clearMark(final LivingEntity target) {
        var tag = target.getPersistentData();
        tag.remove(NBT_MARK_OWNER);
        tag.remove(NBT_MARK_STACKS);
        tag.remove(NBT_MARK_EXPIRE_TICK);
    }

    private static boolean isCooldownReady(
        final Player player,
        final int cooldownTicks
    ) {
        int last = player.getPersistentData().getInt(NBT_LAST_DETONATE_TICK);
        return cooldownTicks <= 0 || (player.tickCount - last) >= cooldownTicks;
    }

    private static void markCooldown(final Player player) {
        player
            .getPersistentData()
            .putInt(NBT_LAST_DETONATE_TICK, player.tickCount);
    }

    private static boolean isPredictedKill(final LivingEntity target, final float damage) {
        return (target.getHealth() - damage) <= 0.0F;
    }

    private static boolean isPredictedLowHealth(
        final LivingEntity target,
        final float damage,
        final float threshold
    ) {
        float max = target.getMaxHealth();
        if (max <= 0.0F) {
            return false;
        }
        float predicted = Math.max(0.0F, target.getHealth() - damage);
        return (predicted / max) <= threshold;
    }

    private static void spawnMarkParticles(final LivingEntity target) {
        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(
            ParticleTypes.SOUL,
            target.getX(),
            target.getY() + target.getBbHeight() * PARTICLE_Y_FACTOR,
            target.getZ(),
            MARK_PARTICLE_COUNT,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPEED
        );
    }

    private static void spawnDetonationParticles(
        final ServerLevel serverLevel,
        final Vec3 origin,
        final double radius
    ) {
        if (serverLevel == null) {
            return;
        }

        serverLevel.sendParticles(
            ParticleTypes.SOUL_FIRE_FLAME,
            origin.x,
            origin.y,
            origin.z,
            DETONATION_PARTICLE_COUNT,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPEED
        );
        serverLevel.sendParticles(
            ParticleTypes.SOUL,
            origin.x,
            origin.y,
            origin.z,
            DETONATION_PARTICLE_COUNT,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPREAD,
            PARTICLE_SPEED
        );
        serverLevel.sendParticles(
            ParticleTypes.EXPLOSION,
            origin.x,
            origin.y,
            origin.z,
            1,
            0.0,
            0.0,
            0.0,
            0.0
        );

        final double ringRadius = Math.max(0.0, radius);
        for (int i = 0; i < RING_POINTS; i++) {
            double angle = (Math.PI * 2.0) * ((double) i / RING_POINTS);
            double x = origin.x + Math.cos(angle) * ringRadius;
            double z = origin.z + Math.sin(angle) * ringRadius;
            serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                x,
                origin.y + RING_Y_OFFSET,
                z,
                1,
                0.0,
                0.0,
                0.0,
                0.0
            );
        }
    }

    private static DamageSource createHunpoDamageSource(final LivingEntity source) {
        return new DamageSource(
            source
                .level()
                .registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE)
                .getHolderOrThrow(
                    net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                        net.minecraft.resources.ResourceLocation.parse(
                            "guzhenren:hunpoxiaosuan"
                        )
                    )
                )
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

    private static float getMetaFloat(
        final NianTouData.Usage usage,
        final String key,
        final float defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Float.parseFloat(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
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
}
