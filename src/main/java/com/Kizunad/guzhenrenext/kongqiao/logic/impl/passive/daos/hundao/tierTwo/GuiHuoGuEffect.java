package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierTwo;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 二转鬼火蛊逻辑：离魂阴火。
 * <p>
 * 防御性反伤：
 * 1. 敌人攻击时，反弹魂道伤害，优先削减魂魄抗性。
 * 2. 持续消耗魂魄维持。
 * 3. 反弹时额外消耗魂魄。
 * </p>
 */
public class GuiHuoGuEffect implements IGuEffect {

    public static final String USAGE_ID = "guzhenren:guihuogu_passive_thorns";
    private static final double BASE_SOUL_DMG = 10.0;
    private static final double PASSIVE_COST_BASE = 0.01;
    private static final double REFLECT_COST_BASE = 1.0;
    private static final double DAO_HEN_DIVISOR = 1000.0;

    private static final int CRIT_PARTICLE_COUNT = 15;
    private static final double CRIT_PARTICLE_SPREAD = 0.5;
    private static final double CRIT_PARTICLE_SPEED = 0.1;
    private static final int SOUL_FIRE_COUNT = 8;
    private static final double SOUL_FIRE_OFFSET_XZ = 0.2;
    private static final double SOUL_FIRE_OFFSET_Y = 0.4;
    private static final double SOUL_FIRE_SPEED = 0.02;
    private static final int PAIN_EFFECT_BASE_DURATION = 10;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    /**
     * 每秒维持消耗：0.01 * (1 + 魂道道痕/1000)
     */
    @Override
    public void onSecond(LivingEntity user, ItemStack stack, NianTouData.Usage usageInfo) {
        double daoHen = DaoHenHelper.getDaoHen(user, DaoHenHelper.DaoType.HUN_DAO);
        double amplifier = 1.0 + (daoHen / DAO_HEN_DIVISOR);
        double cost = PASSIVE_COST_BASE * amplifier;

        double current = HunPoHelper.getAmount(user);
        if (current >= cost) {
            HunPoHelper.modify(user, -cost);
        } else {
            // 魂魄不足，可以在这里添加逻辑（如停止生效），目前暂不处理（只是不扣成负数）
            HunPoHelper.modify(user, -current);
        }
    }

    /**
     * 受伤反制逻辑
     */
    @Override
    public float onHurt(
        LivingEntity victim,
        DamageSource source,
        float damage,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        // 1. 检查攻击者是否有效
        if (!(source.getEntity() instanceof LivingEntity attacker) || attacker == victim) {
            return damage;
        }

        // 2. 检查自身魂魄是否足够支付反击消耗
        // 反弹消耗：1.0 * (1 + 魂道道痕/1000)
        double selfDaoHen = DaoHenHelper.getDaoHen(victim, DaoHenHelper.DaoType.HUN_DAO);
        double selfAmplifier = 1.0 + (selfDaoHen / DAO_HEN_DIVISOR);
        double reflectCost = REFLECT_COST_BASE * selfAmplifier;

        if (HunPoHelper.getAmount(victim) < reflectCost) {
            return damage; // 魂魄不足，无法触发反击
        }
        HunPoHelper.modify(victim, -reflectCost);

        // 3. 计算反击伤害
        // 基础伤害 * 道痕交互倍率
        double baseDmg = BASE_SOUL_DMG;
        if (usageInfo.metadata() != null && usageInfo.metadata().containsKey("soul_damage")) {
            try {
                baseDmg = Double.parseDouble(usageInfo.metadata().get("soul_damage"));
            } catch (NumberFormatException ignored) {}
        }

        double multiplier = DaoHenCalculator.calculateMultiplier(victim, attacker, DaoHenHelper.DaoType.HUN_DAO);
        double finalDmg = baseDmg * multiplier;

        // 4. 执行伤害逻辑：优先扣抗性
        double targetResistance = HunPoHelper.getResistance(attacker);
        double remainingDmg = finalDmg;

        if (targetResistance > 0) {
            if (targetResistance >= remainingDmg) {
                // 抗性足够，只扣抗性
                HunPoHelper.modifyResistance(attacker, -remainingDmg);
                remainingDmg = 0;
            } else {
                // 抗性破碎，扣光抗性，剩余伤害扣魂魄
                HunPoHelper.modifyResistance(attacker, -targetResistance); // 清零
                remainingDmg -= targetResistance;
                
                // 破盾特效
                if (attacker.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                        ParticleTypes.CRIT,
                        attacker.getX(),
                        attacker.getY() + attacker.getEyeHeight(),
                        attacker.getZ(),
                        CRIT_PARTICLE_COUNT,
                        CRIT_PARTICLE_SPREAD,
                        CRIT_PARTICLE_SPREAD,
                        CRIT_PARTICLE_SPREAD,
                        CRIT_PARTICLE_SPEED
                    );
                }
            }
        }

        // 扣除剩余伤害的魂魄
        if (remainingDmg > 0) {
            HunPoHelper.modify(attacker, -remainingDmg);
            
            // 剧痛特效 (如果真的伤到了魂魄)
            attacker.addEffect(
                new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    PAIN_EFFECT_BASE_DURATION + (int) (remainingDmg),
                    1
                )
            );
        }

        // 5. 视觉反馈：幽蓝鬼火
        if (attacker.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                attacker.getX(),
                attacker.getY() + (attacker.getEyeHeight() / 2),
                attacker.getZ(),
                SOUL_FIRE_COUNT,
                SOUL_FIRE_OFFSET_XZ,
                SOUL_FIRE_OFFSET_Y,
                SOUL_FIRE_OFFSET_XZ,
                SOUL_FIRE_SPEED
            );
        }

        return damage;
    }
}
