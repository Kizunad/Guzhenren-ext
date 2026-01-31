package com.Kizunad.guzhenrenext.bastion.guardian.shazhao;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import com.Kizunad.guzhenrenext.bastion.guardian.BastionGuardianCombatRules;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ShazhaoMetadataHelper;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataManager;
import java.util.ArrayList;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 守卫杀招服务（复用 niantou 的杀招 JSON 配置格式）。
 * <p>
 * 约束：
 * <ul>
 *   <li>只复用 ShazhaoData（JSON）里的字段与 metadata key，不复用玩家端的 effect 实现。</li>
 *   <li>无消耗：不扣真元/念头/精力/魂魄，也不做解锁/物品校验。</li>
 *   <li>施放者为守卫（Mob），目标为其当前 target 或附近敌人。</li>
 * </ul>
 * </p>
 */
public final class BastionGuardianShazhaoService {

    private BastionGuardianShazhaoService() {
    }

    private static final class Keys {
        static final String ROOT = "BastionGuardianShazhao";
        static final String NEXT_CAST = "NextCast";

        private Keys() {
        }
    }

    private static final class Config {
        static final int CAST_CHECK_INTERVAL_TICKS = 10;
        static final int DEFAULT_COOLDOWN_TICKS = 20 * 20;

        // 领域类杀招只做“瞬发一次”，不做持续（避免依赖 PlayerTickEvent 体系）。
        static final int DOMAIN_ONE_SHOT_DURATION_TICKS = 60;

        static final int ZHI_DEFAULT_RADIUS = 8;
        static final int HUN_DEFAULT_RADIUS = 6;
        static final int MU_DEFAULT_RADIUS = 5;
        static final int LI_DEFAULT_RADIUS = 4;

        static final int ZHI_BUFF_AMP_DIV = 3;
        static final int ZHI_ENCHANT_PARTICLE_COUNT = 30;
        static final double ZHI_PARTICLE_CENTER_Y_OFFSET = 1.0;
        static final double ZHI_PARTICLE_SPREAD_XZ_RATIO = 0.2;
        static final double ZHI_PARTICLE_SPREAD_Y = 0.6;
        static final float ZHI_SOUND_PITCH = 1.1f;

        static final int HUN_PARTICLE_COUNT = 30;
        static final double HUN_PARTICLE_CENTER_Y_OFFSET = 1.0;
        static final double HUN_PARTICLE_SPREAD = 0.6;
        static final float HUN_SOUND_PITCH = 0.9f;

        static final double MU_DEFAULT_ENEMY_DAMAGE = 4.0;
        static final double MU_DEFAULT_LIFESTEAL = 0.5;
        static final int MU_ROOT_SLOW_AMP = 6;
        static final int MU_PARTICLE_COUNT = 25;
        static final double MU_PARTICLE_CENTER_Y_OFFSET = 1.0;
        static final double MU_PARTICLE_SPREAD_XZ = 0.8;
        static final double MU_PARTICLE_SPREAD_Y = 0.6;
        static final float MU_SOUND_VOL = 0.9f;

        static final double LI_DEFAULT_REPEL = 1.5;
        static final double LI_GRAVITY_FORCE = -0.05;
        static final double LI_PARTICLE_CENTER_Y_OFFSET = 0.5;
        static final int LI_PARTICLE_COUNT = 25;
        static final double LI_PARTICLE_SPREAD_XZ = 0.8;
        static final double LI_PARTICLE_SPREAD_Y = 0.4;
        static final double LI_PARTICLE_SPEED = 0.02;
        static final float LI_SOUND_PITCH = 0.9f;

        static final double MIN_HORIZONTAL_VECTOR_SQR = 0.0001;

        static final int MIN_TIER = 1;

        private Config() {
        }
    }

    private static final class Ids {
        static final String ZHI_DOMAIN =
            "guzhenrenext:shazhao_active_zhi_dao_starry_chess_board";
        static final String HUN_DOMAIN =
            "guzhenrenext:shazhao_active_hun_dao_nether_ghost_domain";
        static final String MU_DOMAIN =
            "guzhenrenext:shazhao_active_mu_dao_forest_manifestation";
        static final String LI_DOMAIN =
            "guzhenrenext:shazhao_active_li_dao_overlord_force_field";

        private Ids() {
        }
    }

    public static void tryCast(ServerLevel level, Mob guardian, BastionDao dao, int tier) {
        if (level == null || guardian == null || dao == null) {
            return;
        }
        if (!guardian.isAlive() || guardian.isRemoved()) {
            return;
        }

        long gameTime = level.getGameTime();
        if ((gameTime % Config.CAST_CHECK_INTERVAL_TICKS) != 0) {
            return;
        }

        int safeTier = Math.max(Config.MIN_TIER, tier);

        var pdc = guardian.getPersistentData();
        var root = pdc.getCompound(Keys.ROOT);
        if (gameTime < root.getLong(Keys.NEXT_CAST)) {
            return;
        }

        ResourceLocation shazhaoId = selectShazhaoId(level, guardian, dao, safeTier);
        ShazhaoData data = shazhaoId == null ? null : ShazhaoDataManager.get(shazhaoId);
        if (data == null) {
            return;
        }

        boolean success = switch (dao) {
            case ZHI_DAO -> castZhiDomainOneShot(level, guardian, data, safeTier);
            case HUN_DAO -> castHunDomainOneShot(level, guardian, data, safeTier);
            case MU_DAO -> castMuDomainOneShot(level, guardian, data, safeTier);
            case LI_DAO -> castLiDomainOneShot(level, guardian, data, safeTier);
        };

        int cooldown = success ? resolveCooldownTicks(data) : Config.CAST_CHECK_INTERVAL_TICKS;
        root.putLong(Keys.NEXT_CAST, gameTime + Math.max(0, cooldown));
        pdc.put(Keys.ROOT, root);
    }

    @Nullable
    private static ResourceLocation selectShazhaoId(
        ServerLevel level,
        Mob guardian,
        BastionDao dao,
        int tier
    ) {
        ResourceLocation fromConfig = selectShazhaoIdFromBastionType(level, guardian, tier);
        if (fromConfig != null) {
            return fromConfig;
        }

        // 回退：保持现有默认
        return switch (dao) {
            case ZHI_DAO -> ResourceLocation.parse(Ids.ZHI_DOMAIN);
            case HUN_DAO -> ResourceLocation.parse(Ids.HUN_DOMAIN);
            case MU_DAO -> ResourceLocation.parse(Ids.MU_DOMAIN);
            case LI_DAO -> ResourceLocation.parse(Ids.LI_DOMAIN);
        };
    }

    @Nullable
    private static ResourceLocation selectShazhaoIdFromBastionType(ServerLevel level, Mob guardian, int tier) {
        if (level == null || guardian == null) {
            return null;
        }

        var guardianBastionId = BastionGuardianData.getBastionId(guardian);
        if (guardianBastionId == null) {
            return null;
        }

        BastionSavedData savedData = BastionSavedData.get(level);
        BastionData bastion = savedData.getBastion(guardianBastionId);
        if (bastion == null) {
            return null;
        }

        BastionTypeConfig config = BastionTypeManager.getOrDefault(bastion.bastionType());
        Optional<BastionTypeConfig.GuardianShazhaoConfig> guardianShazhao = config.guardianShazhao();
        if (guardianShazhao.isEmpty()) {
            return null;
        }

        var pool = guardianShazhao.get().activePool();
        if (pool == null || pool.isEmpty()) {
            return null;
        }

        // 过滤满足 minTier 的条目，并按权重随机
        int totalWeight = 0;
        var eligible = new ArrayList<BastionTypeConfig.WeightedShazhao>();
        for (var entry : pool) {
            if (entry == null) {
                continue;
            }
            if (entry.minTier() > tier) {
                continue;
            }
            if (entry.weight() <= 0) {
                continue;
            }
            eligible.add(entry);
            totalWeight += entry.weight();
        }
        if (eligible.isEmpty() || totalWeight <= 0) {
            return null;
        }

        int roll = level.random.nextInt(totalWeight);
        int cumulative = 0;
        for (var entry : eligible) {
            cumulative += entry.weight();
            if (roll < cumulative) {
                try {
                    return ResourceLocation.parse(entry.shazhaoId());
                } catch (Exception ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private static int resolveCooldownTicks(ShazhaoData data) {
        if (data == null) {
            return Config.DEFAULT_COOLDOWN_TICKS;
        }

        // 兼容 bastion 高转服务的 key："{skillId}_cooldown" 或 "cooldown"
        String id = data.shazhaoID();
        int cd = ShazhaoMetadataHelper.getInt(data, id + "_cooldown", -1);
        if (cd >= 0) {
            return cd;
        }
        cd = ShazhaoMetadataHelper.getInt(data, "cooldown", -1);
        if (cd >= 0) {
            return cd;
        }
        // 部分主动杀招用 cooldown_ticks
        cd = ShazhaoMetadataHelper.getInt(data, id + "_cooldown_ticks", -1);
        if (cd >= 0) {
            return cd;
        }
        cd = ShazhaoMetadataHelper.getInt(data, "cooldown_ticks", -1);
        if (cd >= 0) {
            return cd;
        }

        return Config.DEFAULT_COOLDOWN_TICKS;
    }

    // ===== 领域类杀招（守卫版：瞬发一次） =====

    private static boolean castZhiDomainOneShot(ServerLevel level, Mob guardian, ShazhaoData data, int tier) {
        double radius = readRadius(data, Ids.ZHI_DOMAIN, Config.ZHI_DEFAULT_RADIUS);
        if (radius <= 0.0) {
            return false;
        }

        // 智道：友方增益（力量/抗性），敌方削弱（虚弱/挖掘疲劳）
        AABB area = guardian.getBoundingBox().inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!target.isAlive() || target.isRemoved() || target == guardian) {
                continue;
            }
            if (target.distanceToSqr(guardian) > radius * radius) {
                continue;
            }

            boolean allied = target.isAlliedTo(guardian);
            if (BastionGuardianCombatRules.isGuardian(guardian)
                && BastionGuardianCombatRules.isGuardian(target)) {
                allied = BastionGuardianCombatRules.isSameBastion(guardian, target);
            }

            if (allied) {
                target.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_BOOST,
                    Config.DOMAIN_ONE_SHOT_DURATION_TICKS,
                    Math.max(0, tier / Config.ZHI_BUFF_AMP_DIV),
                    false,
                    false,
                    true
                ));
                target.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    Config.DOMAIN_ONE_SHOT_DURATION_TICKS,
                    0,
                    false,
                    false,
                    true
                ));
            } else {
                target.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    Config.DOMAIN_ONE_SHOT_DURATION_TICKS,
                    1,
                    false,
                    false,
                    true
                ));
                target.addEffect(new MobEffectInstance(
                    MobEffects.DIG_SLOWDOWN,
                    Config.DOMAIN_ONE_SHOT_DURATION_TICKS,
                    1,
                    false,
                    false,
                    true
                ));
            }
        }

        level.sendParticles(
            ParticleTypes.ENCHANT,
            guardian.getX(),
            guardian.getY() + Config.ZHI_PARTICLE_CENTER_Y_OFFSET,
            guardian.getZ(),
            Config.ZHI_ENCHANT_PARTICLE_COUNT,
            radius * Config.ZHI_PARTICLE_SPREAD_XZ_RATIO,
            Config.ZHI_PARTICLE_SPREAD_Y,
            radius * Config.ZHI_PARTICLE_SPREAD_XZ_RATIO,
            0.0
        );
        level.playSound(
            null,
            guardian.blockPosition(),
            SoundEvents.ENCHANTMENT_TABLE_USE,
            SoundSource.HOSTILE,
            1.0f,
            Config.ZHI_SOUND_PITCH
        );
        return true;
    }

    private static boolean castHunDomainOneShot(ServerLevel level, Mob guardian, ShazhaoData data, int tier) {
        double radius = readRadius(data, Ids.HUN_DOMAIN, Config.HUN_DEFAULT_RADIUS);
        if (radius <= 0.0) {
            return false;
        }
        double enemyDamage = ShazhaoMetadataHelper.getDouble(
            data,
            metaKey(Ids.HUN_DOMAIN, "enemy_damage"),
            2.0
        );
        double soulDamage = ShazhaoMetadataHelper.getDouble(data, metaKey(Ids.HUN_DOMAIN, "soul_damage"), 0.0);
        int weaknessAmp = Math.max(
            0,
            ShazhaoMetadataHelper.getInt(
                data,
                metaKey(Ids.HUN_DOMAIN, "weakness_amplifier"),
                0
            )
        );

        AABB area = guardian.getBoundingBox().inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!target.isAlive() || target.isRemoved() || target == guardian) {
                continue;
            }
            if (target.distanceToSqr(guardian) > radius * radius) {
                continue;
            }
            if (target.isAlliedTo(guardian)) {
                continue;
            }
            if (BastionGuardianCombatRules.isGuardian(target)
                && !BastionGuardianCombatRules.canGuardianDamage(level, guardian, target)) {
                continue;
            }

            target.addEffect(new MobEffectInstance(
                MobEffects.BLINDNESS,
                Config.DOMAIN_ONE_SHOT_DURATION_TICKS,
                0
            ));
            target.addEffect(new MobEffectInstance(
                MobEffects.CONFUSION,
                Config.DOMAIN_ONE_SHOT_DURATION_TICKS,
                0
            ));
            if (weaknessAmp > 0) {
                target.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    Config.DOMAIN_ONE_SHOT_DURATION_TICKS,
                    weaknessAmp
                ));
            }
            if (enemyDamage > 0.0) {
                target.hurt(guardian.damageSources().magic(), (float) enemyDamage);
            }
            if (soulDamage > 0.0) {
                target.hurt(guardian.damageSources().magic(), (float) soulDamage);
            }
        }

        level.sendParticles(
            ParticleTypes.SOUL,
            guardian.getX(),
            guardian.getY() + Config.HUN_PARTICLE_CENTER_Y_OFFSET,
            guardian.getZ(),
            Config.HUN_PARTICLE_COUNT,
            Config.HUN_PARTICLE_SPREAD,
            Config.HUN_PARTICLE_SPREAD,
            Config.HUN_PARTICLE_SPREAD,
            0.0
        );
        level.playSound(
            null,
            guardian.blockPosition(),
            SoundEvents.SOUL_ESCAPE.value(),
            SoundSource.HOSTILE,
            1.0f,
            Config.HUN_SOUND_PITCH
        );
        return true;
    }

    private static boolean castMuDomainOneShot(ServerLevel level, Mob guardian, ShazhaoData data, int tier) {
        double radius = readRadius(data, Ids.MU_DOMAIN, Config.MU_DEFAULT_RADIUS);
        if (radius <= 0.0) {
            return false;
        }
        double enemyDamage = ShazhaoMetadataHelper.getDouble(
            data,
            metaKey(Ids.MU_DOMAIN, "enemy_damage"),
            Config.MU_DEFAULT_ENEMY_DAMAGE
        );
        double lifesteal = ShazhaoMetadataHelper.getDouble(
            data,
            metaKey(Ids.MU_DOMAIN, "lifesteal_ratio"),
            Config.MU_DEFAULT_LIFESTEAL
        );

        float totalHeal = 0.0f;
        AABB area = guardian.getBoundingBox().inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!target.isAlive() || target.isRemoved() || target == guardian) {
                continue;
            }
            if (target.distanceToSqr(guardian) > radius * radius) {
                continue;
            }
            if (target.isAlliedTo(guardian)) {
                continue;
            }
            if (BastionGuardianCombatRules.isGuardian(target)
                && !BastionGuardianCombatRules.canGuardianDamage(level, guardian, target)) {
                continue;
            }

            target.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                Config.DOMAIN_ONE_SHOT_DURATION_TICKS,
                Config.MU_ROOT_SLOW_AMP,
                false,
                false,
                true
            ));
            if (enemyDamage > 0.0f) {
                boolean hurt = target.hurt(guardian.damageSources().magic(), (float) enemyDamage);
                if (hurt && lifesteal > 0.0) {
                    totalHeal += (float) (enemyDamage * lifesteal);
                }
            }
        }
        if (totalHeal > 0.0f) {
            guardian.heal(totalHeal);
        }

        level.sendParticles(
            ParticleTypes.HAPPY_VILLAGER,
            guardian.getX(),
            guardian.getY() + Config.MU_PARTICLE_CENTER_Y_OFFSET,
            guardian.getZ(),
            Config.MU_PARTICLE_COUNT,
            Config.MU_PARTICLE_SPREAD_XZ,
            Config.MU_PARTICLE_SPREAD_Y,
            Config.MU_PARTICLE_SPREAD_XZ,
            0.0
        );
        level.playSound(
            null,
            guardian.blockPosition(),
            SoundEvents.BONE_MEAL_USE,
            SoundSource.HOSTILE,
            Config.MU_SOUND_VOL,
            1.0f
        );
        return true;
    }

    private static boolean castLiDomainOneShot(ServerLevel level, Mob guardian, ShazhaoData data, int tier) {
        double radius = readRadius(data, Ids.LI_DOMAIN, Config.LI_DEFAULT_RADIUS);
        if (radius <= 0.0) {
            return false;
        }
        double repelStrength = ShazhaoMetadataHelper.getDouble(
            data,
            metaKey(Ids.LI_DOMAIN, "repel_strength"),
            Config.LI_DEFAULT_REPEL
        );
        double gravityMult = ShazhaoMetadataHelper.getDouble(data, metaKey(Ids.LI_DOMAIN, "gravity_multiplier"), 0.0);

        AABB area = guardian.getBoundingBox().inflate(radius);
        Vec3 center = guardian.position();

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!target.isAlive() || target.isRemoved() || target == guardian) {
                continue;
            }
            if (target.distanceToSqr(guardian) > radius * radius) {
                continue;
            }
            if (target.isAlliedTo(guardian)) {
                continue;
            }
            if (BastionGuardianCombatRules.isGuardian(target)
                && !BastionGuardianCombatRules.canGuardianDamage(level, guardian, target)) {
                continue;
            }

            if (gravityMult > 0.0) {
                target.setDeltaMovement(
                    target.getDeltaMovement().add(
                        0.0,
                        Config.LI_GRAVITY_FORCE * gravityMult,
                        0.0
                    )
                );
                target.hurtMarked = true;
            }

            if (repelStrength > 0.0) {
                applyRepel(center, target, repelStrength);
            }
        }

        level.sendParticles(
            ParticleTypes.POOF,
            guardian.getX(),
            guardian.getY() + Config.LI_PARTICLE_CENTER_Y_OFFSET,
            guardian.getZ(),
            Config.LI_PARTICLE_COUNT,
            Config.LI_PARTICLE_SPREAD_XZ,
            Config.LI_PARTICLE_SPREAD_Y,
            Config.LI_PARTICLE_SPREAD_XZ,
            Config.LI_PARTICLE_SPEED
        );
        level.playSound(
            null,
            guardian.blockPosition(),
            SoundEvents.IRON_GOLEM_ATTACK,
            SoundSource.HOSTILE,
            1.0f,
            Config.LI_SOUND_PITCH
        );
        return true;
    }

    private static double readRadius(ShazhaoData data, String id, double defaultRadius) {
        return Math.max(
            0.0,
            ShazhaoMetadataHelper.getDouble(data, metaKey(id, "radius"), defaultRadius)
        );
    }

    private static String metaKey(String shazhaoId, String suffix) {
        return shazhaoId + "_" + suffix;
    }

    private static void applyRepel(Vec3 center, LivingEntity target, double strength) {
        if (center == null || target == null || strength <= 0.0) {
            return;
        }
        Vec3 delta = target.position().subtract(center);
        Vec3 horizontal = new Vec3(delta.x, 0.0, delta.z);
        if (horizontal.lengthSqr() <= Config.MIN_HORIZONTAL_VECTOR_SQR) {
            return;
        }
        Vec3 dir = horizontal.normalize();
        target.push(dir.x * strength, 0.0, dir.z * strength);
        target.hurtMarked = true;
    }


}
