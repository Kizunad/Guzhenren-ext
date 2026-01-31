package com.Kizunad.guzhenrenext.bastion.guardian;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;

/**
 * 基地守卫技能服务。
 * <p>
 * 目标：为不同道途提供风格差异明显的“主动技能”，并带冷却。
 * 冷却状态使用 PersistentData 存储，确保读档后行为稳定。</p>
 */
public final class BastionGuardianSkillService {

    private BastionGuardianSkillService() {
    }

    private static final class Keys {
        static final String ROOT = "BastionGuardianSkills";
        static final String NEXT_CAST = "NextCast";

        static final String ZHI_NEXT_CAST = "ZhiNextCast";
        static final String HUN_NEXT_CAST = "HunNextCast";
        static final String MU_NEXT_CAST = "MuNextCast";
        static final String LI_NEXT_CAST = "LiNextCast";

        private Keys() {
        }
    }

    private static final class SkillConfig {
        static final int MIN_TIER = 1;
        static final int CAST_CHECK_INTERVAL_TICKS = 5;
        static final int BASE_COOLDOWN_TICKS = 60;
        static final int COOLDOWN_PER_TIER = 10;

        static final double ELITE_COOLDOWN_MULT = 0.85;
        static final int ELITE_MIN_COOLDOWN_TICKS = 30;

        static final int ZHI_DEBUFF_DURATION = 60;
        static final int HUN_DEBUFF_DURATION = 80;
        static final int MU_BUFF_DURATION = 80;
        static final int LI_SHOCK_RANGE = 6;

        static final int ZHI_VULNERABLE_DURATION = 60;
        static final int ZHI_BLIND_DURATION = 50;

        static final int HUN_FEAR_DURATION = 40;
        static final int HUN_EXECUTE_CHECK_RANGE = 10;

        static final int MU_ROOT_DURATION = 50;
        static final int MU_WEAKNESS_DURATION = 60;

        static final int LI_CHARGE_RANGE = 10;
        static final int LI_STUN_DURATION = 30;

        static final float LI_KNOCKBACK_Y = 0.25f;
        static final float BASE_HEAL = 6.0f;
        static final float HEAL_PER_TIER = 2.0f;

        static final float HUN_EXECUTE_BASE_DAMAGE = 10.0f;
        static final float HUN_EXECUTE_PER_TIER = 4.0f;

        static final float LI_CHARGE_BASE_DAMAGE = 8.0f;
        static final float LI_CHARGE_PER_TIER = 3.0f;

        static final float EXECUTE_HEAL_RATIO = 0.5f;

        static final double LI_CHARGE_PUSH_XZ = 0.8;
        static final double LI_CHARGE_PUSH_Y = 0.1;

        static final int PARTICLE_COUNT_SMALL = 18;
        static final int PARTICLE_COUNT_MEDIUM = 20;
        static final int PARTICLE_COUNT_LARGE = 22;

        static final double PARTICLE_SPREAD_SMALL_XZ = 0.4;
        static final double PARTICLE_SPREAD_SMALL_Y = 0.6;
        static final double PARTICLE_SPREAD_MEDIUM_XZ = 1.0;
        static final double PARTICLE_SPREAD_MEDIUM_Y = 0.8;
        static final double PARTICLE_SPREAD_LARGE_XZ = 1.2;
        static final double PARTICLE_SPREAD_LARGE_Y = 0.6;

        static final double PARTICLE_SPEED_ZERO = 0.0;
        static final double PARTICLE_SPEED_POOF = 0.02;

        static final float SOUND_VOL_QUIET = 0.8f;
        static final float SOUND_VOL_NORMAL = 0.9f;
        static final float SOUND_VOL_LOUD = 1.0f;

        static final float SOUND_PITCH_LOW = 0.9f;
        static final float SOUND_PITCH_NORMAL = 1.0f;
        static final float SOUND_PITCH_HIGH = 1.1f;

        static final double KNOCKBACK_BASE = 0.6;
        static final double KNOCKBACK_PER_TIER = 0.05;
        static final double KNOCKBACK_MIN_LEN = 0.001;
        static final double KNOCKBACK_DIV_ZERO_GUARD = 0.001;

        static final int MU_AOE_RADIUS_BASE = 6;
        static final int MU_AOE_RADIUS_PER_TIER = 1;
        static final int MU_AOE_RADIUS_MAX = 12;

        static final int ZHI_MAX_AMP = 3;
        static final int HUN_MAX_AMP = 2;
        static final int HUN_AMP_DIV = 2;
        static final int MU_MAX_AMP = 2;
        static final int MU_AMP_DIV = 2;

        static final int ZHI_BLIND_MAX_AMP = 0;

        static final int WEAKNESS_AMP = 0;

        static final float LOW_HEALTH_RATIO = 0.35f;
        static final float EXECUTE_HEALTH_RATIO = 0.25f;

        static final double TARGET_NEAR_DISTANCE_SQ = 64.0;
        static final double TARGET_FAR_DISTANCE_SQ = 256.0;

        static final double ZHI_PARTICLE_HEIGHT_FACTOR = 0.6;
        static final double HUN_PARTICLE_HEIGHT_FACTOR = 0.5;
        static final double LI_POOF_HEIGHT_OFFSET = 0.5;

        private SkillConfig() {
        }
    }

    public static void tickGuardianSkills(Mob guardian, BastionDao dao, int tier, boolean elite) {
        if (guardian.level().isClientSide()) {
            return;
        }
        if (!(guardian.level() instanceof ServerLevel level)) {
            return;
        }
        if (!guardian.isAlive() || guardian.isRemoved()) {
            return;
        }

        long gameTime = level.getGameTime();
        if ((gameTime % SkillConfig.CAST_CHECK_INTERVAL_TICKS) != 0) {
            return;
        }

        // 冷却判定（全局）
        var data = guardian.getPersistentData();
        var root = data.getCompound(Keys.ROOT);
        long nextCast = root.getLong(Keys.NEXT_CAST);
        if (gameTime < nextCast) {
            return;
        }

        LivingEntity target = guardian.getTarget();
        if (target == null || !target.isAlive() || target.isRemoved()) {
            return;
        }

        // 按道途从技能池里选择一个技能释放
        boolean casted = switch (dao) {
            case ZHI_DAO -> castFromZhiPool(level, guardian, target, tier, elite, root, gameTime);
            case HUN_DAO -> castFromHunPool(level, guardian, target, tier, elite, root, gameTime);
            case MU_DAO -> castFromMuPool(level, guardian, target, tier, elite, root, gameTime);
            case LI_DAO -> castFromLiPool(level, guardian, target, tier, elite, root, gameTime);
        };

        if (!casted) {
            return;
        }

        int cooldown = computeCooldownTicks(tier, elite);
        root.putLong(Keys.NEXT_CAST, gameTime + cooldown);
        data.put(Keys.ROOT, root);
    }

    private static boolean castFromZhiPool(
            ServerLevel level,
            Mob guardian,
            LivingEntity target,
            int tier,
            boolean elite,
            net.minecraft.nbt.CompoundTag root,
            long gameTime) {
        if (gameTime < root.getLong(Keys.ZHI_NEXT_CAST)) {
            return false;
        }
        double distSq = guardian.distanceToSqr(target);

        boolean casted;
        if (distSq <= SkillConfig.TARGET_NEAR_DISTANCE_SQ) {
            // 近距离：致盲/短控
            casted = castZhiBlind(level, guardian, target, tier);
        } else {
            // 中远距离：减速+挖掘疲劳（原技能）
            casted = castZhiDao(guardian, target, tier);
        }
        if (!casted) {
            return false;
        }

        root.putLong(Keys.ZHI_NEXT_CAST, gameTime + computePerDaoCooldownTicks(tier, elite));
        return true;
    }

    private static boolean castFromHunPool(
            ServerLevel level,
            Mob guardian,
            LivingEntity target,
            int tier,
            boolean elite,
            net.minecraft.nbt.CompoundTag root,
            long gameTime) {
        if (gameTime < root.getLong(Keys.HUN_NEXT_CAST)) {
            return false;
        }

        double distSq = guardian.distanceToSqr(target);
        boolean casted;
        if (distSq <= SkillConfig.TARGET_NEAR_DISTANCE_SQ
            && target.getHealth() <= target.getMaxHealth() * SkillConfig.EXECUTE_HEALTH_RATIO) {
            casted = castHunExecute(level, guardian, target, tier);
        } else {
            casted = castHunDao(guardian, target, tier);
        }
        if (!casted) {
            return false;
        }

        root.putLong(Keys.HUN_NEXT_CAST, gameTime + computePerDaoCooldownTicks(tier, elite));
        return true;
    }

    private static boolean castFromMuPool(
            ServerLevel level,
            Mob guardian,
            LivingEntity target,
            int tier,
            boolean elite,
            net.minecraft.nbt.CompoundTag root,
            long gameTime) {
        if (gameTime < root.getLong(Keys.MU_NEXT_CAST)) {
            return false;
        }

        boolean casted;
        if (guardian.getHealth() <= guardian.getMaxHealth() * SkillConfig.LOW_HEALTH_RATIO) {
            // 低血：优先开群体再生
            casted = castMuDao(level, guardian, tier);
        } else {
            // 常规：控住单体并虚弱
            casted = castMuRoot(level, guardian, target, tier);
        }
        if (!casted) {
            return false;
        }

        root.putLong(Keys.MU_NEXT_CAST, gameTime + computePerDaoCooldownTicks(tier, elite));
        return true;
    }

    private static boolean castFromLiPool(
            ServerLevel level,
            Mob guardian,
            LivingEntity target,
            int tier,
            boolean elite,
            net.minecraft.nbt.CompoundTag root,
            long gameTime) {
        if (gameTime < root.getLong(Keys.LI_NEXT_CAST)) {
            return false;
        }

        double distSq = guardian.distanceToSqr(target);
        boolean casted;
        int maxRange = SkillConfig.LI_CHARGE_RANGE;
        double maxRangeSq = (double) maxRange * maxRange;
        if (distSq >= SkillConfig.TARGET_FAR_DISTANCE_SQ && distSq <= maxRangeSq) {
            casted = castLiCharge(level, guardian, target, tier);
        } else {
            casted = castLiDao(level, guardian, tier);
        }
        if (!casted) {
            return false;
        }

        root.putLong(Keys.LI_NEXT_CAST, gameTime + computePerDaoCooldownTicks(tier, elite));
        return true;
    }

    private static int computePerDaoCooldownTicks(int tier, boolean elite) {
        // 每道技能的私有冷却略短于全局冷却，用于“池内选择”。
        int base = computeCooldownTicks(tier, elite);
        return Math.max(SkillConfig.ELITE_MIN_COOLDOWN_TICKS, base / 2);
    }

    private static boolean castZhiBlind(ServerLevel level, Mob guardian, LivingEntity target, int tier) {
        target.addEffect(new MobEffectInstance(
            MobEffects.BLINDNESS,
            SkillConfig.ZHI_BLIND_DURATION,
            SkillConfig.ZHI_BLIND_MAX_AMP,
            false,
            true,
            true
        ));
        target.addEffect(new MobEffectInstance(
            MobEffects.WEAKNESS,
            SkillConfig.ZHI_VULNERABLE_DURATION,
            SkillConfig.WEAKNESS_AMP,
            false,
            true,
            true
        ));
        level.sendParticles(
            ParticleTypes.SQUID_INK,
            target.getX(),
            target.getY() + target.getBbHeight() * SkillConfig.ZHI_PARTICLE_HEIGHT_FACTOR,
            target.getZ(),
            SkillConfig.PARTICLE_COUNT_MEDIUM,
            SkillConfig.PARTICLE_SPREAD_SMALL_XZ,
            SkillConfig.PARTICLE_SPREAD_SMALL_Y,
            SkillConfig.PARTICLE_SPREAD_SMALL_XZ,
            SkillConfig.PARTICLE_SPEED_ZERO
        );
        level.playSound(
            null,
            target.blockPosition(),
            SoundEvents.INK_SAC_USE,
            SoundSource.HOSTILE,
            SkillConfig.SOUND_VOL_NORMAL,
            SkillConfig.SOUND_PITCH_LOW
        );
        return true;
    }

    private static boolean castHunExecute(ServerLevel level, Mob guardian, LivingEntity target, int tier) {
        int range = SkillConfig.HUN_EXECUTE_CHECK_RANGE;
        if (guardian.distanceToSqr(target) > (double) range * range) {
            return false;
        }
        float damage = SkillConfig.HUN_EXECUTE_BASE_DAMAGE + (tier - 1) * SkillConfig.HUN_EXECUTE_PER_TIER;
        boolean hurt = target.hurt(guardian.damageSources().mobAttack(guardian), damage);
        if (!hurt) {
            return false;
        }
        guardian.heal(damage * SkillConfig.EXECUTE_HEAL_RATIO);
        level.sendParticles(
            ParticleTypes.SOUL_FIRE_FLAME,
            target.getX(),
            target.getY() + target.getBbHeight() * SkillConfig.HUN_PARTICLE_HEIGHT_FACTOR,
            target.getZ(),
            SkillConfig.PARTICLE_COUNT_LARGE,
            SkillConfig.PARTICLE_SPREAD_SMALL_XZ,
            SkillConfig.PARTICLE_SPREAD_SMALL_Y,
            SkillConfig.PARTICLE_SPREAD_SMALL_XZ,
            SkillConfig.PARTICLE_SPEED_ZERO
        );
        level.playSound(
            null,
            target.blockPosition(),
            SoundEvents.WARDEN_ATTACK_IMPACT,
            SoundSource.HOSTILE,
            SkillConfig.SOUND_VOL_LOUD,
            SkillConfig.SOUND_PITCH_LOW
        );
        return true;
    }

    private static boolean castMuRoot(ServerLevel level, Mob guardian, LivingEntity target, int tier) {
        target.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN,
            SkillConfig.MU_ROOT_DURATION,
            Math.min(SkillConfig.ZHI_MAX_AMP, tier / 2),
            false,
            true,
            true
        ));
        target.addEffect(new MobEffectInstance(
            MobEffects.WEAKNESS,
            SkillConfig.MU_WEAKNESS_DURATION,
            SkillConfig.WEAKNESS_AMP,
            false,
            true,
            true
        ));
        level.sendParticles(
            ParticleTypes.SPORE_BLOSSOM_AIR,
            target.getX(),
            target.getY() + target.getBbHeight() * SkillConfig.ZHI_PARTICLE_HEIGHT_FACTOR,
            target.getZ(),
            SkillConfig.PARTICLE_COUNT_MEDIUM,
            SkillConfig.PARTICLE_SPREAD_MEDIUM_XZ,
            SkillConfig.PARTICLE_SPREAD_MEDIUM_Y,
            SkillConfig.PARTICLE_SPREAD_MEDIUM_XZ,
            SkillConfig.PARTICLE_SPEED_ZERO
        );
        level.playSound(
            null,
            target.blockPosition(),
            SoundEvents.BONE_MEAL_USE,
            SoundSource.HOSTILE,
            SkillConfig.SOUND_VOL_QUIET,
            SkillConfig.SOUND_PITCH_NORMAL
        );
        return true;
    }

    private static boolean castLiCharge(ServerLevel level, Mob guardian, LivingEntity target, int tier) {
        int range = SkillConfig.LI_CHARGE_RANGE;
        if (guardian.distanceToSqr(target) > (double) range * range) {
            return false;
        }

        // 给守卫一个瞬间冲刺（不改变 AI，只做一次 push）
        double dx = target.getX() - guardian.getX();
        double dz = target.getZ() - guardian.getZ();
        double len = Math.max(SkillConfig.KNOCKBACK_MIN_LEN, Math.sqrt(dx * dx + dz * dz));
        guardian.push(
            dx / len * SkillConfig.LI_CHARGE_PUSH_XZ,
            SkillConfig.LI_CHARGE_PUSH_Y,
            dz / len * SkillConfig.LI_CHARGE_PUSH_XZ
        );

        float damage = SkillConfig.LI_CHARGE_BASE_DAMAGE + (tier - 1) * SkillConfig.LI_CHARGE_PER_TIER;
        target.hurt(guardian.damageSources().mobAttack(guardian), damage);
        target.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN,
            SkillConfig.LI_STUN_DURATION,
            SkillConfig.WEAKNESS_AMP,
            false,
            true,
            true
        ));

        level.sendParticles(
            ParticleTypes.CRIT,
            target.getX(),
            target.getY() + target.getBbHeight() * SkillConfig.ZHI_PARTICLE_HEIGHT_FACTOR,
            target.getZ(),
            SkillConfig.PARTICLE_COUNT_LARGE,
            SkillConfig.PARTICLE_SPREAD_LARGE_XZ,
            SkillConfig.PARTICLE_SPREAD_LARGE_Y,
            SkillConfig.PARTICLE_SPREAD_LARGE_XZ,
            SkillConfig.PARTICLE_SPEED_POOF
        );
        level.playSound(
            null,
            target.blockPosition(),
            SoundEvents.PLAYER_ATTACK_CRIT,
            SoundSource.HOSTILE,
            SkillConfig.SOUND_VOL_LOUD,
            SkillConfig.SOUND_PITCH_HIGH
        );
        return true;
    }

    private static int computeCooldownTicks(int tier, boolean elite) {
        int cooldown = SkillConfig.BASE_COOLDOWN_TICKS + (tier - 1) * SkillConfig.COOLDOWN_PER_TIER;
        if (!elite) {
            return cooldown;
        }
        return Math.max(
            SkillConfig.ELITE_MIN_COOLDOWN_TICKS,
            (int) Math.round(cooldown * SkillConfig.ELITE_COOLDOWN_MULT)
        );
    }

    // （已移除旧调用入口：守卫不再依赖实体自持同步数据）

    private static boolean castZhiDao(Mob guardian, LivingEntity target, int tier) {
        int amp = Math.min(tier - 1, SkillConfig.ZHI_MAX_AMP);
        target.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN,
            SkillConfig.ZHI_DEBUFF_DURATION,
            amp,
            false,
            true,
            true
        ));
        target.addEffect(new MobEffectInstance(
            MobEffects.DIG_SLOWDOWN,
            SkillConfig.ZHI_DEBUFF_DURATION,
            amp,
            false,
            true,
            true
        ));

        if (guardian.level() instanceof ServerLevel level) {
            level.sendParticles(
                ParticleTypes.ENCHANT,
                target.getX(),
                target.getY() + target.getBbHeight() * SkillConfig.ZHI_PARTICLE_HEIGHT_FACTOR,
                target.getZ(),
                SkillConfig.PARTICLE_COUNT_MEDIUM,
                SkillConfig.PARTICLE_SPREAD_SMALL_XZ,
                SkillConfig.PARTICLE_SPREAD_SMALL_Y,
                SkillConfig.PARTICLE_SPREAD_SMALL_XZ,
                SkillConfig.PARTICLE_SPEED_ZERO
            );
            level.playSound(
                null,
                target.blockPosition(),
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.HOSTILE,
                SkillConfig.SOUND_VOL_NORMAL,
                SkillConfig.SOUND_PITCH_HIGH
            );
        }
        return true;
    }

    private static boolean castHunDao(Mob guardian, LivingEntity target, int tier) {
        int amp = Math.min(tier / SkillConfig.HUN_AMP_DIV, SkillConfig.HUN_MAX_AMP);
        target.addEffect(new MobEffectInstance(
            MobEffects.WITHER,
            SkillConfig.HUN_DEBUFF_DURATION,
            amp,
            false,
            true,
            true
        ));

        float heal = SkillConfig.BASE_HEAL + (tier - 1) * SkillConfig.HEAL_PER_TIER;
        guardian.heal(heal);

        if (guardian.level() instanceof ServerLevel level) {
            level.sendParticles(
                ParticleTypes.SOUL,
                guardian.getX(),
                guardian.getY() + guardian.getBbHeight() * SkillConfig.HUN_PARTICLE_HEIGHT_FACTOR,
                guardian.getZ(),
                SkillConfig.PARTICLE_COUNT_SMALL,
                SkillConfig.PARTICLE_SPREAD_SMALL_XZ,
                SkillConfig.PARTICLE_SPREAD_SMALL_Y,
                SkillConfig.PARTICLE_SPREAD_SMALL_XZ,
                SkillConfig.PARTICLE_SPEED_ZERO
            );
            level.playSound(
                null,
                guardian.blockPosition(),
                SoundEvents.SOUL_ESCAPE.value(),
                SoundSource.HOSTILE,
                SkillConfig.SOUND_VOL_NORMAL,
                SkillConfig.SOUND_PITCH_LOW
            );
        }
        return true;
    }

    private static boolean castMuDao(ServerLevel level, Mob guardian, int tier) {
        int radius = Math.min(
            SkillConfig.MU_AOE_RADIUS_MAX,
            SkillConfig.MU_AOE_RADIUS_BASE + tier * SkillConfig.MU_AOE_RADIUS_PER_TIER
        );
        AABB box = guardian.getBoundingBox().inflate(radius);
        List<Mob> allies = level.getEntitiesOfClass(
            Mob.class,
            box,
            mob -> BastionGuardianCombatRules.isGuardian(mob)
                && BastionGuardianCombatRules.isSameBastion(guardian, mob)
                && mob.isAlive()
        );

        if (allies.isEmpty()) {
            return false;
        }

        int amp = Math.min(tier / SkillConfig.MU_AMP_DIV, SkillConfig.MU_MAX_AMP);
        for (Mob ally : allies) {
            ally.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION,
                SkillConfig.MU_BUFF_DURATION,
                amp,
                false,
                true,
                true
            ));
        }

        level.sendParticles(
            ParticleTypes.HAPPY_VILLAGER,
            guardian.getX(),
            guardian.getY() + 1.0,
            guardian.getZ(),
            SkillConfig.PARTICLE_COUNT_SMALL,
            SkillConfig.PARTICLE_SPREAD_MEDIUM_XZ,
            SkillConfig.PARTICLE_SPREAD_MEDIUM_Y,
            SkillConfig.PARTICLE_SPREAD_MEDIUM_XZ,
            SkillConfig.PARTICLE_SPEED_ZERO
        );
        level.playSound(
            null,
            guardian.blockPosition(),
            SoundEvents.BONE_MEAL_USE,
            SoundSource.HOSTILE,
            SkillConfig.SOUND_VOL_QUIET,
            SkillConfig.SOUND_PITCH_NORMAL
        );
        return true;
    }

    private static boolean castLiDao(ServerLevel level, Mob guardian, int tier) {
        int radius = SkillConfig.LI_SHOCK_RANGE;
        AABB box = guardian.getBoundingBox().inflate(radius);
        List<LivingEntity> victims = level.getEntitiesOfClass(
            LivingEntity.class,
            box,
            living -> living != guardian
                && living.isAlive()
                && (living instanceof net.minecraft.world.entity.player.Player
                    || BastionGuardianCombatRules.isGuardian(living))
        );

        if (victims.isEmpty()) {
            return false;
        }

        for (LivingEntity victim : victims) {
            if (BastionGuardianCombatRules.isGuardian(victim)
                && !BastionGuardianCombatRules.canGuardianDamage(level, guardian, victim)) {
                continue;
            }

            double dx = victim.getX() - guardian.getX();
            double dz = victim.getZ() - guardian.getZ();
            double len = Math.max(SkillConfig.KNOCKBACK_MIN_LEN, Math.sqrt(dx * dx + dz * dz));
            double knock = SkillConfig.KNOCKBACK_BASE + tier * SkillConfig.KNOCKBACK_PER_TIER;
            victim.push(
                dx / Math.max(SkillConfig.KNOCKBACK_DIV_ZERO_GUARD, len) * knock,
                SkillConfig.LI_KNOCKBACK_Y,
                dz / Math.max(SkillConfig.KNOCKBACK_DIV_ZERO_GUARD, len) * knock
            );
        }

        level.sendParticles(
            ParticleTypes.POOF,
            guardian.getX(),
            guardian.getY() + SkillConfig.LI_POOF_HEIGHT_OFFSET,
            guardian.getZ(),
            SkillConfig.PARTICLE_COUNT_LARGE,
            SkillConfig.PARTICLE_SPREAD_LARGE_XZ,
            SkillConfig.PARTICLE_SPREAD_LARGE_Y,
            SkillConfig.PARTICLE_SPREAD_LARGE_XZ,
            SkillConfig.PARTICLE_SPEED_POOF
        );
        level.playSound(
            null,
            guardian.blockPosition(),
            SoundEvents.IRON_GOLEM_ATTACK,
            SoundSource.HOSTILE,
            SkillConfig.SOUND_VOL_LOUD,
            SkillConfig.SOUND_PITCH_LOW
        );
        return true;
    }
}
