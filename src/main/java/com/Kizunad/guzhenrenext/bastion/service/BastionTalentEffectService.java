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
    private static final String EFFECT_EXPANSION_SPEED = "expansion_speed";

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
     * 获取扩张速度倍率。
     *
     * @param bastion 基地数据
     * @return 扩张速度乘数
     */
    public static double getExpansionSpeedMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_EXPANSION_SPEED);
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
