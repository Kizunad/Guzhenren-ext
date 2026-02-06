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

    // 智道专属效果
    private static final String EFFECT_ZHI_DAO_NIANTOU_EFFICIENCY = "zhi_dao_niantou_efficiency";
    private static final String EFFECT_ZHI_DAO_CLARITY = "zhi_dao_clarity";
    private static final String EFFECT_ZHI_DAO_THOUGHT_SHIELD = "zhi_dao_thought_shield";
    private static final String EFFECT_ZHI_DAO_TIER_BONUS = "zhi_dao_tier_bonus";
    private static final String EFFECT_ZHI_DAO_INSIGHT_LOOP = "zhi_dao_insight_loop";

    // 魂道专属效果
    private static final String EFFECT_HUN_DAO_SOUL_HARVEST = "hun_dao_soul_harvest";
    private static final String EFFECT_HUN_DAO_SOUL_BARRIER = "hun_dao_soul_barrier";
    private static final String EFFECT_HUN_DAO_TORMENT = "hun_dao_torment";
    private static final String EFFECT_HUN_DAO_SPIRIT_CHAIN = "hun_dao_spirit_chain";
    private static final String EFFECT_HUN_DAO_REQUIEM = "hun_dao_requiem";

    // 木道专属效果
    private static final String EFFECT_MU_DAO_GROWTH_SPEED = "mu_dao_growth_speed";
    private static final String EFFECT_MU_DAO_HEALING = "mu_dao_healing";
    private static final String EFFECT_MU_DAO_RESOURCE_REGEN = "mu_dao_resource_regen";
    private static final String EFFECT_MU_DAO_BARKSKIN = "mu_dao_barkskin";
    private static final String EFFECT_MU_DAO_GUARDIAN_SYNERGY = "mu_dao_guardian_synergy";
    private static final String EFFECT_MU_DAO_GUARDIAN_BLOOM = "mu_dao_guardian_bloom";

    // 力道专属效果
    private static final String EFFECT_LI_DAO_ATTACK_POWER = "li_dao_attack_power";
    private static final String EFFECT_LI_DAO_DEFENSE = "li_dao_defense";
    private static final String EFFECT_LI_DAO_OVERWHELM = "li_dao_overwhelm";
    private static final String EFFECT_LI_DAO_GUARDIAN_TRAINING = "li_dao_guardian_training";
    private static final String EFFECT_LI_DAO_BULWARK = "li_dao_bulwark";
    private static final String EFFECT_LI_DAO_HASTE = "li_dao_haste";
    private static final String EFFECT_LI_DAO_UNYIELDING = "li_dao_unyielding";
    private static final String EFFECT_LI_DAO_COUNTER = "li_dao_counter";

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

    // ==================== 智道专属效果 ====================

    /**
     * 获取念头效率倍率。
     *
     * @param bastion 基地数据
     * @return 念头效率乘数
     */
    public static double getZhiDaoNiantouEfficiencyMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_ZHI_DAO_NIANTOU_EFFICIENCY);
    }

    /**
     * 获取智道澄明心境倍率（念头回复与命中）。
     *
     * @param bastion 基地数据
     * @return 澄明心境乘数
     */
    public static double getZhiDaoClarityMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_ZHI_DAO_CLARITY);
    }

    /**
     * 获取智道念障壁垒减伤倍率。
     *
     * @param bastion 基地数据
     * @return 念障壁垒减伤乘数
     */
    public static double getZhiDaoThoughtShieldMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_ZHI_DAO_THOUGHT_SHIELD);
    }

    /**
     * 获取智道转数推进效率倍率。
     *
     * @param bastion 基地数据
     * @return 转数推进效率乘数
     */
    public static double getZhiDaoTierBonusMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_ZHI_DAO_TIER_BONUS);
    }

    /**
     * 获取智道念头循环效率倍率。
     *
     * @param bastion 基地数据
     * @return 念头循环效率乘数
     */
    public static double getZhiDaoInsightLoopMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_ZHI_DAO_INSIGHT_LOOP);
    }

    // ==================== 魂道专属效果 ====================

    /**
     * 获取魂魄获取效率倍率。
     *
     * @param bastion 基地数据
     * @return 魂魄获取效率乘数
     */
    public static double getHunDaoSoulHarvestMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_HUN_DAO_SOUL_HARVEST);
    }

    /**
     * 获取魂道魂障壁垒减伤倍率。
     *
     * @param bastion 基地数据
     * @return 魂障壁垒减伤乘数
     */
    public static double getHunDaoSoulBarrierMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_HUN_DAO_SOUL_BARRIER);
    }

    /**
     * 获取魂道灵魂折磨倍率（死亡光环伤害）。
     *
     * @param bastion 基地数据
     * @return 灵魂折磨伤害乘数
     */
    public static double getHunDaoTormentMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_HUN_DAO_TORMENT);
    }

    /**
     * 获取魂道魂链束缚倍率（伤害分摊）。
     *
     * @param bastion 基地数据
     * @return 魂链束缚乘数
     */
    public static double getHunDaoSpiritChainMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_HUN_DAO_SPIRIT_CHAIN);
    }

    /**
     * 获取魂道安魂曲倍率（击杀触发减速与易伤）。
     *
     * @param bastion 基地数据
     * @return 安魂曲乘数
     */
    public static double getHunDaoRequiemMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_HUN_DAO_REQUIEM);
    }

    // ==================== 木道专属效果 ====================

    /**
     * 获取木道生长速度倍率。
     *
     * @param bastion 基地数据
     * @return 生长速度乘数
     */
    public static double getMuDaoGrowthSpeedMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_MU_DAO_GROWTH_SPEED);
    }

    /**
     * 获取木道恢复效率倍率。
     *
     * @param bastion 基地数据
     * @return 恢复效率乘数
     */
    public static double getMuDaoHealingMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_MU_DAO_HEALING);
    }

    /**
     * 获取木道真元回复倍率。
     *
     * @param bastion 基地数据
     * @return 真元回复乘数
     */
    public static double getMuDaoResourceRegenMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_MU_DAO_RESOURCE_REGEN);
    }

    /**
     * 获取木道木甲护体减伤倍率。
     *
     * @param bastion 基地数据
     * @return 木甲护体减伤乘数
     */
    public static double getMuDaoBarkskinMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_MU_DAO_BARKSKIN);
    }

    /**
     * 获取木道共生协同倍率（守卫与植物共生输出）。
     *
     * @param bastion 基地数据
     * @return 共生协同乘数
     */
    public static double getMuDaoGuardianSynergyMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_MU_DAO_GUARDIAN_SYNERGY);
    }

    /**
     * 获取木道守卫繁茂倍率（生命与再生）。
     *
     * @param bastion 基地数据
     * @return 守卫繁茂乘数
     */
    public static double getMuDaoGuardianBloomMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_MU_DAO_GUARDIAN_BLOOM);
    }

    // ==================== 力道专属效果 ====================

    /**
     * 获取力道攻击力倍率。
     *
     * @param bastion 基地数据
     * @return 攻击力乘数
     */
    public static double getLiDaoAttackPowerMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_LI_DAO_ATTACK_POWER);
    }

    /**
     * 获取力道防御倍率。
     *
     * @param bastion 基地数据
     * @return 防御乘数
     */
    public static double getLiDaoDefenseMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_LI_DAO_DEFENSE);
    }

    /**
     * 获取力道力压群雄倍率（狂暴额外伤害）。
     *
     * @param bastion 基地数据
     * @return 力压群雄伤害乘数
     */
    public static double getLiDaoOverwhelmMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_LI_DAO_OVERWHELM);
    }

    /**
     * 获取力道守卫军令倍率（守卫攻击协同）。
     *
     * @param bastion 基地数据
     * @return 守卫军令乘数
     */
    public static double getLiDaoGuardianTrainingMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_LI_DAO_GUARDIAN_TRAINING);
    }

    /**
     * 获取力道钢筋铁骨倍率（守卫与自身减伤）。
     *
     * @param bastion 基地数据
     * @return 钢筋铁骨减伤乘数
     */
    public static double getLiDaoBulwarkMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_LI_DAO_BULWARK);
    }

    /**
     * 获取力道气血奔腾倍率（移速与攻速）。
     *
     * @param bastion 基地数据
     * @return 气血奔腾乘数
     */
    public static double getLiDaoHasteMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_LI_DAO_HASTE);
    }

    /**
     * 获取力道不屈意志倍率（狂暴后护盾减伤）。
     *
     * @param bastion 基地数据
     * @return 不屈意志减伤乘数
     */
    public static double getLiDaoUnyieldingMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_LI_DAO_UNYIELDING);
    }

    /**
     * 获取力道蓄劲反击倍率（受击后下次攻击）。
     *
     * @param bastion 基地数据
     * @return 蓄劲反击伤害乘数
     */
    public static double getLiDaoCounterMultiplier(BastionData bastion) {
        return 1.0 + sumEffectValue(bastion, EFFECT_LI_DAO_COUNTER);
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
