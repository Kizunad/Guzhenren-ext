package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionModifier;
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
}
