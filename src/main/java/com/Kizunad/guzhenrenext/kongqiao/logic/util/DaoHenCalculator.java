package com.Kizunad.guzhenrenext.kongqiao.logic.util;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import net.minecraft.world.entity.LivingEntity;

/**
 * 道痕增幅计算器。
 * <p>
 * 核心公式：
 * Final = Base * (1 + 攻方本道/1000) * (1 + 守方本道/1000) / (1 + 守方异道/1000)
 * </p>
 */
public final class DaoHenCalculator {

    private static final double DAO_HEN_DIVISOR = 1000.0;

    private DaoHenCalculator() {}

    /**
     * 计算最终伤害/效果倍率。
     *
     * @param attacker 攻击者/施法者
     * @param victim   受击者/承受者
     * @param type     手段所属流派 (如 魂道)
     * @return 最终倍率 (默认为 1.0)
     */
    public static double calculateMultiplier(
        LivingEntity attacker,
        LivingEntity victim,
        DaoHenHelper.DaoType type
    ) {
        if (attacker == null || victim == null) {
            return 1.0;
        }

        // 1. 攻击者增幅 (本流派道痕)
        double attackerDao = DaoHenHelper.getDaoHen(attacker, type);
        double attackerBoost = 1.0 + (attackerDao / DAO_HEN_DIVISOR);

        // 2. 防御者助益 (同流派道痕共鸣，导致受到更多伤害)
        double victimDao = DaoHenHelper.getDaoHen(victim, type);
        double victimVulnerability = 1.0 + (victimDao / DAO_HEN_DIVISOR);

        // 3. 防御者减益 (异流派道痕互斥，提供防御)
        double victimTotal = DaoHenHelper.getTotalDaoHen(victim);
        double victimOtherDao = Math.max(0, victimTotal - victimDao);
        double victimResistance = 1.0 + (victimOtherDao / DAO_HEN_DIVISOR);

        // 综合计算
        // 伤害 = 基础 * 攻增幅 * 受易伤 / 受防御
        return (attackerBoost * victimVulnerability) / victimResistance;
    }
    
    /**
     * 计算仅受自身道痕影响的倍率 (用于治疗、Buff等)。
     * 公式: (1 + 本道/1000) / (1 + 异道/1000)
     */
    public static double calculateSelfMultiplier(LivingEntity user, DaoHenHelper.DaoType type) {
        if (user == null) {
            return 1.0;
        }

        double selfDao = DaoHenHelper.getDaoHen(user, type);
        double totalDao = DaoHenHelper.getTotalDaoHen(user);
        double otherDao = Math.max(0, totalDao - selfDao);

        double boost = 1.0 + (selfDao / DAO_HEN_DIVISOR);
        double resistance = 1.0 + (otherDao / DAO_HEN_DIVISOR);

        return boost / resistance;
    }
}
