package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionModifier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

/**
 * 基地词缀效果服务 - 统一管理守卫相关的词缀应用与处理。
 * <p>
 * 负责处理守卫受到/造成伤害、生成、死亡时的词缀效果。
 * </p>
 */
public final class BastionModifierService {

    private BastionModifierService() {
    }

    /** HARDENED 减伤倍率（保留 70% 伤害）。 */
    private static final float HARDENED_DAMAGE_MULTIPLIER = 0.7f;

    /** VOLATILE 爆炸半径。 */
    private static final float VOLATILE_EXPLOSION_RADIUS = 3.0f;

    /** CLOAKED 隐身持续时间（tick）。*/
    private static final int CLOAKED_INVISIBILITY_TICKS = 200;

    /** CLOAKED 首击伤害倍率。*/
    private static final float CLOAKED_FIRST_STRIKE_MULTIPLIER = 1.5f;

    /** PROLIFERATING 分裂概率。*/
    private static final double PROLIFERATING_SPLIT_CHANCE = 0.3;

    /** 分裂守卫的生命值比例（相较原体）。 */
    private static final float PROLIFERATING_HEALTH_RATIO = 0.5f;

    /**
     * 词缀视觉效果配置。
     */
    private static final class VisualConfig {
        /** HARDENED 粒子数量。 */
        static final int HARDENED_PARTICLE_COUNT = 4;
        /** HARDENED 粒子扩散范围。 */
        static final double HARDENED_SPREAD = 0.2;
        /** HARDENED 粒子速度。 */
        static final double HARDENED_SPEED = 0.01;

        /** VOLATILE 粒子数量。 */
        static final int VOLATILE_PARTICLE_COUNT = 6;
        /** VOLATILE 粒子扩散范围。 */
        static final double VOLATILE_SPREAD = 0.3;
        /** VOLATILE 粒子速度。 */
        static final double VOLATILE_SPEED = 0.02;

        /** CLOAKED 粒子数量。 */
        static final int CLOAKED_PARTICLE_COUNT = 4;
        /** CLOAKED 粒子扩散范围。 */
        static final double CLOAKED_SPREAD = 0.25;
        /** CLOAKED 粒子速度。 */
        static final double CLOAKED_SPEED = 0.05;

        /** PROLIFERATING 粒子数量。 */
        static final int PROLIFERATING_PARTICLE_COUNT = 5;
        /** PROLIFERATING 粒子扩散范围。 */
        static final double PROLIFERATING_SPREAD = 0.25;
        /** PROLIFERATING 粒子速度。 */
        static final double PROLIFERATING_SPEED = 0.01;

        /** 粒子高度偏移比例。 */
        static final double HEIGHT_OFFSET_RATIO = 0.5;

        private VisualConfig() {
        }
    }

    /**
     * 守卫受到伤害时调用 - 应用 HARDENED 减伤。
     *
     * @param guardian 守卫实体
     * @param bastion  所属基地数据
     * @param damage   原始伤害
     * @return 处理后的伤害
     */
    public static float modifyIncomingDamage(Mob guardian, BastionData bastion, float damage) {
        if (guardian == null || bastion == null) {
            return damage;
        }
        if (damage <= 0.0f) {
            return damage;
        }
        if (!bastion.modifiers().contains(BastionModifier.HARDENED)) {
            return damage;
        }
        return damage * HARDENED_DAMAGE_MULTIPLIER;
    }

    /**
     * 守卫死亡时调用 - 处理 VOLATILE 爆炸与 PROLIFERATING 分裂。
     *
     * @param guardian 守卫实体
     * @param bastion  所属基地数据
     * @param level    服务端世界
     */
    public static void onGuardianDeath(Mob guardian, BastionData bastion, ServerLevel level) {
        if (guardian == null || bastion == null || level == null) {
            return;
        }

        if (bastion.modifiers().contains(BastionModifier.VOLATILE)) {
            level.explode(
                guardian,
                guardian.getX(),
                guardian.getY(),
                guardian.getZ(),
                VOLATILE_EXPLOSION_RADIUS,
                Level.ExplosionInteraction.MOB
            );
        }

        if (bastion.modifiers().contains(BastionModifier.PROLIFERATING)) {
            if (level.getRandom().nextDouble() < PROLIFERATING_SPLIT_CHANCE) {
                trySpawnOffspring(level, guardian, bastion);
            }
        }
    }

    /**
     * 守卫生成时调用 - 应用 CLOAKED 隐身效果。
     *
     * @param guardian 守卫实体
     * @param bastion  所属基地数据
     */
    public static void onGuardianSpawn(Mob guardian, BastionData bastion) {
        if (guardian == null || bastion == null) {
            return;
        }
        if (!bastion.modifiers().contains(BastionModifier.CLOAKED)) {
            return;
        }
        guardian.addEffect(new MobEffectInstance(
            MobEffects.INVISIBILITY,
            CLOAKED_INVISIBILITY_TICKS,
            0,
            false,
            false
        ));
    }

    /**
     * 守卫攻击时调用 - 应用 CLOAKED 首击加成。
     * <p>
     * 基于隐身状态判断首次出手，成功后移除隐身效果以避免重复触发。
     * </p>
     *
     * @param guardian 守卫实体
     * @param bastion  所属基地数据
     * @param damage   原始伤害
     * @return 处理后的伤害
     */
    public static float modifyOutgoingDamage(Mob guardian, BastionData bastion, float damage) {
        if (guardian == null || bastion == null) {
            return damage;
        }
        if (damage <= 0.0f) {
            return damage;
        }
        if (!bastion.modifiers().contains(BastionModifier.CLOAKED)) {
            return damage;
        }

        if (guardian.hasEffect(MobEffects.INVISIBILITY)) {
            guardian.removeEffect(MobEffects.INVISIBILITY);
            return damage * CLOAKED_FIRST_STRIKE_MULTIPLIER;
        }
        return damage;
    }

    /**
     * 尝试生成一个分裂守卫。
     * <p>
     * 生成同类型守卫，生命值设为原体一半，继承基础数据（占位，需外部负责标记）。
     * </p>
     */
    private static void trySpawnOffspring(ServerLevel level, Mob guardian, BastionData bastion) {
        Mob offspring = (Mob) guardian.getType().create(level);
        if (offspring == null) {
            return;
        }
        offspring.moveTo(guardian.getX(), guardian.getY(), guardian.getZ(), guardian.getYRot(), guardian.getXRot());

        float maxHealth = offspring.getMaxHealth();
        float scaledHealth = Math.max(1.0f, maxHealth * PROLIFERATING_HEALTH_RATIO);
        offspring.setHealth(scaledHealth);

        level.addFreshEntity(offspring);

        // 分裂体需要沿用守卫标记，以便其他服务识别。这里复用已有工具。
        com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData.markAsGuardian(
            offspring,
            bastion.id(),
            com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData.getTier(guardian)
        );
    }

    /**
     * 为指定词缀生成视觉粒子效果。
     *
     * @param level    服务端世界
     * @param guardian 守卫实体
     * @param modifier 词缀类型
     */
    public static void spawnModifierParticles(ServerLevel level, Mob guardian, BastionModifier modifier) {
        if (level == null || guardian == null || modifier == null) {
            return;
        }

        double x = guardian.getX();
        double y = guardian.getY() + guardian.getBbHeight() * VisualConfig.HEIGHT_OFFSET_RATIO;
        double z = guardian.getZ();

        switch (modifier) {
            case HARDENED -> level.sendParticles(
                ParticleTypes.CRIT,
                x, y, z,
                VisualConfig.HARDENED_PARTICLE_COUNT,
                VisualConfig.HARDENED_SPREAD,
                VisualConfig.HARDENED_SPREAD,
                VisualConfig.HARDENED_SPREAD,
                VisualConfig.HARDENED_SPEED
            );
            case VOLATILE -> level.sendParticles(
                ParticleTypes.FLAME,
                x, y, z,
                VisualConfig.VOLATILE_PARTICLE_COUNT,
                VisualConfig.VOLATILE_SPREAD,
                VisualConfig.VOLATILE_SPREAD,
                VisualConfig.VOLATILE_SPREAD,
                VisualConfig.VOLATILE_SPEED
            );
            case CLOAKED -> level.sendParticles(
                ParticleTypes.PORTAL,
                x, y, z,
                VisualConfig.CLOAKED_PARTICLE_COUNT,
                VisualConfig.CLOAKED_SPREAD,
                VisualConfig.CLOAKED_SPREAD,
                VisualConfig.CLOAKED_SPREAD,
                VisualConfig.CLOAKED_SPEED
            );
            case PROLIFERATING -> level.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                x, y, z,
                VisualConfig.PROLIFERATING_PARTICLE_COUNT,
                VisualConfig.PROLIFERATING_SPREAD,
                VisualConfig.PROLIFERATING_SPREAD,
                VisualConfig.PROLIFERATING_SPREAD,
                VisualConfig.PROLIFERATING_SPEED
            );
            default -> { }
        }
    }

    /**
     * 为守卫生成所有激活词缀的视觉效果。
     *
     * @param level    服务端世界
     * @param guardian 守卫实体
     * @param bastion  基地数据
     */
    public static void spawnAllModifierParticles(ServerLevel level, Mob guardian, BastionData bastion) {
        if (level == null || guardian == null || bastion == null) {
            return;
        }
        for (BastionModifier modifier : bastion.modifiers()) {
            spawnModifierParticles(level, guardian, modifier);
        }
    }
}
