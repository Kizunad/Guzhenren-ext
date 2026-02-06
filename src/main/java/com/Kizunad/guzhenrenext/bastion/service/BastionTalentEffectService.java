package com.Kizunad.guzhenrenext.bastion.service;

import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.talent.BastionTalentData;
import com.Kizunad.guzhenrenext.bastion.talent.BastionTalentNode;
import com.Kizunad.guzhenrenext.bastion.talent.BastionTalentRegistry;

/**
 * 基地天赋效果服务。
 * <p>
 * 遍历已解锁天赋节点，根据 effectId 汇总数值并返回最终倍率。
 * </p>
 */
public final class BastionTalentEffectService {

    private static final String EFFECT_RESOURCE_OUTPUT = "resource_output";
    private static final String EFFECT_GUARDIAN_DAMAGE = "guardian_damage";
    private static final String EFFECT_GUARDIAN_DAMAGE_REDUCTION = "guardian_damage_reduction";
    private static final String EFFECT_EXPANSION_SPEED = "expansion_speed";
    private static final String EFFECT_ZHI_DAO_AURA_RANGE = "zhi_dao_aura_range";
    private static final String EFFECT_CONVERSION_BONUS = "conversion_bonus";
    private static final String EFFECT_ZHI_DAO_CONVERSION_BONUS = "zhi_dao_conversion_bonus";
    private static final String EFFECT_HUN_DAO_CONVERSION_BONUS = "hun_dao_conversion_bonus";
    private static final String EFFECT_MU_DAO_CONVERSION_BONUS = "mu_dao_conversion_bonus";

    private BastionTalentEffectService() {
    }

    /**
     * 获取资源产出倍率。
     *
     * @param bastion 基地数据
     * @return 资源产出乘数（1.0 = 无加成，1.1 = +10%）
     */
    public static double getResourceOutputMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_RESOURCE_OUTPUT);
    }

    /**
     * 获取守卫伤害倍率。
     *
     * @param bastion 基地数据
     * @return 守卫伤害乘数
     */
    public static double getGuardianDamageMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_GUARDIAN_DAMAGE);
    }

    /**
     * 获取守卫减伤倍率。
     *
     * @param bastion 基地数据
     * @return 守卫减伤乘数
     */
    public static double getGuardianDamageReductionMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_GUARDIAN_DAMAGE_REDUCTION);
    }

    /**
     * 获取扩张速度倍率。
     *
     * @param bastion 基地数据
     * @return 扩张速度乘数
     */
    public static double getExpansionSpeedMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_EXPANSION_SPEED);
    }

    /**
     * 获取光环范围倍率。
     * <p>
     * 当前仅应用有实际数值意义的道途专属光环范围加成。
     * </p>
     *
     * @param bastion 基地数据
     * @return 光环范围乘数（1.0 = 无加成，1.12 = +12%）
     */
    public static double getAuraRangeMultiplier(BastionData bastion) {
        double bonus = sumEffectValue(bastion, EFFECT_ZHI_DAO_AURA_RANGE);
        return 1.0 + bonus;
    }

    /**
     * 获取资源转化效率倍率。
     * <p>
     * 累加通用与各道途转化效率加成，返回最终乘数。
     * </p>
     *
     * @param bastion 基地数据
     * @return 转化效率乘数（1.0 = 无加成）
     */
    public static double getConversionBonusMultiplier(BastionData bastion) {
        double bonus = sumEffectValue(bastion, EFFECT_CONVERSION_BONUS)
            + sumEffectValue(bastion, EFFECT_ZHI_DAO_CONVERSION_BONUS)
            + sumEffectValue(bastion, EFFECT_HUN_DAO_CONVERSION_BONUS)
            + sumEffectValue(bastion, EFFECT_MU_DAO_CONVERSION_BONUS);
        return 1.0 + bonus;
    }

    private static double sumEffectValue(BastionData bastion, String effectId) {
        if (bastion == null || effectId == null) {
            return 0.0;
        }
        BastionTalentData talentData = bastion.talentData();
        double sum = 0.0;
        for (String nodeId : talentData.unlockedNodes()) {
            BastionTalentNode node = BastionTalentRegistry.getNode(nodeId);
            if (node != null && effectId.equals(node.effectId())) {
                sum += node.effectValue();
            }
        }
        return sum;
    }
}
