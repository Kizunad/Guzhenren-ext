package com.Kizunad.guzhenrenext.bastion.talent;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 基地天赋树注册表（硬编码示例）。
 * <p>
 * 提供节点列表、查询与建议；后续可替换为数据驱动配置。
 * </p>
 */
public final class BastionTalentRegistry {

    private static final double VALUE_TEN_PERCENT = 0.1;
    private static final double VALUE_EIGHT_PERCENT = 0.08;
    private static final double VALUE_TWELVE_PERCENT = 0.12;
    private static final double VALUE_FIFTEEN_PERCENT = 0.15;
    private static final double VALUE_TWENTY_PERCENT = 0.2;
    private static final int COST_ONE = 1;
    private static final int COST_TWO = 2;
    private static final int COST_THREE = 3;

    private static final List<BastionTalentNode> NODES = List.of(
        new BastionTalentNode(
            "root_efficiency",
            "资源效率",
            "资源产出+10%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_ONE,
            List.of(),
            Optional.empty(),
            "resource_output",
            VALUE_TEN_PERCENT
        ),
        new BastionTalentNode(
            "guardian_damage",
            "守卫强化",
            "守卫伤害+10%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_TWO,
            List.of("root_efficiency"),
            Optional.empty(),
            "guardian_damage",
            VALUE_TEN_PERCENT
        ),
        new BastionTalentNode(
            "guardian_defense",
            "守卫护甲",
            "守卫伤害减免+10%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_TWO,
            List.of("root_efficiency"),
            Optional.empty(),
            "guardian_damage_reduction",
            VALUE_TEN_PERCENT
        ),
        new BastionTalentNode(
            "resource_conversion",
            "炼化提升",
            "资源转化效率+15%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("guardian_damage", "guardian_defense"),
            Optional.of(BastionDao.MU_DAO),
            "conversion_bonus",
            VALUE_FIFTEEN_PERCENT
        ),
        new BastionTalentNode(
            "affix_unlock_mutation",
            "变异词缀",
            "解锁特殊词缀：mutation",
            BastionTalentNode.NodeType.AFFIX_UNLOCK,
            COST_THREE,
            List.of("resource_conversion"),
            Optional.empty(),
            "affix_mutation",
            1.0
        ),
        new BastionTalentNode(
            "expansion_basic",
            "扩张基础",
            "基地扩张速度+10%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_ONE,
            List.of("root_efficiency"),
            Optional.empty(),
            "expansion_speed",
            VALUE_TEN_PERCENT
        ),
        new BastionTalentNode(
            "resource_storage",
            "储备提升",
            "资源产出与存储协同+15%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_TWO,
            List.of("root_efficiency"),
            Optional.empty(),
            "resource_output",
            VALUE_FIFTEEN_PERCENT
        ),
        new BastionTalentNode(
            "pollution_resist",
            "污染抗性",
            "基地污染抗性+10%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_TWO,
            List.of("guardian_defense"),
            Optional.empty(),
            "guardian_damage_reduction",
            VALUE_TEN_PERCENT
        ),
        new BastionTalentNode(
            "logistics_hub",
            "物流枢纽",
            "资源产出+8%，解锁后续专精",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_TWO,
            List.of("resource_storage"),
            Optional.empty(),
            "resource_output",
            VALUE_EIGHT_PERCENT
        ),
        new BastionTalentNode(
            "expansion_network",
            "扩张网络",
            "扩张速度+12%，开启深层扩张节点",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("expansion_basic", "logistics_hub"),
            Optional.empty(),
            "expansion_speed",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "expansion_command",
            "指挥架构",
            "解锁主动：临时提升扩张效率",
            BastionTalentNode.NodeType.ACTIVE_SKILL,
            COST_THREE,
            List.of("expansion_network"),
            Optional.empty(),
            "expansion_command",
            1.0
        ),
        new BastionTalentNode(
            "zhi_dao_root",
            "智道入门",
            "开启智道天赋分支",
            BastionTalentNode.NodeType.DAO_MASTERY,
            COST_TWO,
            List.of("root_efficiency", "expansion_basic"),
            Optional.of(BastionDao.ZHI_DAO),
            "zhi_dao_unlock",
            1.0
        ),
        new BastionTalentNode(
            "zhi_dao_aura_range",
            "心域扩展",
            "智道光环范围+12%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_TWO,
            List.of("zhi_dao_root"),
            Optional.of(BastionDao.ZHI_DAO),
            "zhi_dao_aura_range",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "zhi_dao_niantou_efficiency",
            "念头精研",
            "念头效率+15%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_TWO,
            List.of("zhi_dao_root"),
            Optional.of(BastionDao.ZHI_DAO),
            "zhi_dao_niantou_efficiency",
            VALUE_FIFTEEN_PERCENT
        ),
        new BastionTalentNode(
            "zhi_dao_mind_control",
            "精神控制",
            "解锁主动：短暂控制敌对单位",
            BastionTalentNode.NodeType.ACTIVE_SKILL,
            COST_THREE,
            List.of("zhi_dao_aura_range"),
            Optional.of(BastionDao.ZHI_DAO),
            "zhi_dao_mind_control",
            1.0
        ),
        new BastionTalentNode(
            "zhi_dao_clarity",
            "澄明心境",
            "念头回复与命中+10%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("zhi_dao_niantou_efficiency"),
            Optional.of(BastionDao.ZHI_DAO),
            "zhi_dao_clarity",
            VALUE_TEN_PERCENT
        ),
        new BastionTalentNode(
            "zhi_dao_thought_shield",
            "念障壁垒",
            "守卫获得精神护盾，减伤+12%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("zhi_dao_mind_control"),
            Optional.of(BastionDao.ZHI_DAO),
            "zhi_dao_thought_shield",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "zhi_dao_tier_bonus",
            "推演捷径",
            "智道转数推进效率+10%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("zhi_dao_clarity"),
            Optional.of(BastionDao.ZHI_DAO),
            "zhi_dao_tier_bonus",
            VALUE_TEN_PERCENT
        ),
        new BastionTalentNode(
            "zhi_dao_insight_loop",
            "灵机回路",
            "念头循环效率+12%，保持光环持续",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("zhi_dao_tier_bonus", "zhi_dao_thought_shield"),
            Optional.of(BastionDao.ZHI_DAO),
            "zhi_dao_insight_loop",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "zhi_dao_aura_command",
            "心域号令",
            "解锁主动：命令光环内守卫爆发",
            BastionTalentNode.NodeType.ACTIVE_SKILL,
            COST_THREE,
            List.of("zhi_dao_insight_loop"),
            Optional.of(BastionDao.ZHI_DAO),
            "zhi_dao_aura_command",
            1.0
        ),
        new BastionTalentNode(
            "zhi_dao_conversion_synergy",
            "推演转化",
            "智道转化效率+10%，与物流枢纽联动",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("zhi_dao_insight_loop", "logistics_hub"),
            Optional.of(BastionDao.ZHI_DAO),
            "zhi_dao_conversion_bonus",
            VALUE_TEN_PERCENT
        ),
        new BastionTalentNode(
            "zhi_dao_mastery",
            "智道精通",
            "智道光环与控制效果全面强化",
            BastionTalentNode.NodeType.DAO_MASTERY,
            COST_THREE,
            List.of("zhi_dao_conversion_synergy", "zhi_dao_aura_command"),
            Optional.of(BastionDao.ZHI_DAO),
            "zhi_dao_mastery",
            1.0
        ),
        new BastionTalentNode(
            "zhi_dao_affix_precision",
            "精准词缀",
            "解锁智道词缀：precision",
            BastionTalentNode.NodeType.AFFIX_UNLOCK,
            COST_THREE,
            List.of("zhi_dao_mastery"),
            Optional.of(BastionDao.ZHI_DAO),
            "affix_precision",
            1.0
        ),
        new BastionTalentNode(
            "hun_dao_root",
            "魂道入门",
            "开启魂道天赋分支",
            BastionTalentNode.NodeType.DAO_MASTERY,
            COST_TWO,
            List.of("root_efficiency", "guardian_defense"),
            Optional.of(BastionDao.HUN_DAO),
            "hun_dao_unlock",
            1.0
        ),
        new BastionTalentNode(
            "hun_dao_soul_harvest",
            "魂魄收割",
            "魂魄获取效率+12%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_TWO,
            List.of("hun_dao_root"),
            Optional.of(BastionDao.HUN_DAO),
            "hun_dao_soul_harvest",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "hun_dao_guardian_summon",
            "守卫增援",
            "解锁主动：额外召唤魂魄守卫",
            BastionTalentNode.NodeType.ACTIVE_SKILL,
            COST_THREE,
            List.of("hun_dao_root"),
            Optional.of(BastionDao.HUN_DAO),
            "hun_dao_guardian_summon",
            1.0
        ),
        new BastionTalentNode(
            "hun_dao_soul_barrier",
            "魂障壁垒",
            "守卫获得魂障，减伤+12%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("hun_dao_guardian_summon"),
            Optional.of(BastionDao.HUN_DAO),
            "hun_dao_soul_barrier",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "hun_dao_death_aura",
            "死亡光环",
            "解锁主动：死亡领域持续伤害",
            BastionTalentNode.NodeType.ACTIVE_SKILL,
            COST_THREE,
            List.of("hun_dao_soul_harvest"),
            Optional.of(BastionDao.HUN_DAO),
            "hun_dao_death_aura",
            1.0
        ),
        new BastionTalentNode(
            "hun_dao_torment",
            "灵魂折磨",
            "死亡光环伤害+12%，附带减速",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("hun_dao_death_aura"),
            Optional.of(BastionDao.HUN_DAO),
            "hun_dao_torment",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "hun_dao_spirit_chain",
            "魂链束缚",
            "魂链连接守卫，分摊伤害+12%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("hun_dao_soul_barrier", "hun_dao_torment"),
            Optional.of(BastionDao.HUN_DAO),
            "hun_dao_spirit_chain",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "hun_dao_conversion",
            "魂火炼化",
            "魂道转化效率+10%，与物流枢纽联动",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("hun_dao_soul_harvest", "logistics_hub"),
            Optional.of(BastionDao.HUN_DAO),
            "hun_dao_conversion_bonus",
            VALUE_TEN_PERCENT
        ),
        new BastionTalentNode(
            "hun_dao_command",
            "亡魂号令",
            "解锁主动：驱使亡魂集火",
            BastionTalentNode.NodeType.ACTIVE_SKILL,
            COST_THREE,
            List.of("hun_dao_spirit_chain"),
            Optional.of(BastionDao.HUN_DAO),
            "hun_dao_command",
            1.0
        ),
        new BastionTalentNode(
            "hun_dao_requiem",
            "安魂曲",
            "击杀触发范围减速与易伤+10%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("hun_dao_command"),
            Optional.of(BastionDao.HUN_DAO),
            "hun_dao_requiem",
            VALUE_TEN_PERCENT
        ),
        new BastionTalentNode(
            "hun_dao_mastery",
            "魂道精通",
            "亡魂与光环全面强化",
            BastionTalentNode.NodeType.DAO_MASTERY,
            COST_THREE,
            List.of("hun_dao_conversion", "hun_dao_requiem"),
            Optional.of(BastionDao.HUN_DAO),
            "hun_dao_mastery",
            1.0
        ),
        new BastionTalentNode(
            "hun_dao_affix_wraith",
            "幽魅词缀",
            "解锁魂道词缀：wraith",
            BastionTalentNode.NodeType.AFFIX_UNLOCK,
            COST_THREE,
            List.of("hun_dao_mastery"),
            Optional.of(BastionDao.HUN_DAO),
            "affix_wraith",
            1.0
        ),
        new BastionTalentNode(
            "mu_dao_root",
            "木道入门",
            "开启木道天赋分支",
            BastionTalentNode.NodeType.DAO_MASTERY,
            COST_TWO,
            List.of("root_efficiency", "pollution_resist"),
            Optional.of(BastionDao.MU_DAO),
            "mu_dao_unlock",
            1.0
        ),
        new BastionTalentNode(
            "mu_dao_growth_speed",
            "生长加速",
            "木道生长速度+12%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_TWO,
            List.of("mu_dao_root"),
            Optional.of(BastionDao.MU_DAO),
            "mu_dao_growth_speed",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "mu_dao_healing",
            "木气自愈",
            "木道恢复效率+10%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_TWO,
            List.of("mu_dao_root"),
            Optional.of(BastionDao.MU_DAO),
            "mu_dao_healing",
            VALUE_TEN_PERCENT
        ),
        new BastionTalentNode(
            "mu_dao_resource_regen",
            "真元再生",
            "真元回复+12%，稳定增产",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("mu_dao_growth_speed"),
            Optional.of(BastionDao.MU_DAO),
            "mu_dao_resource_regen",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "mu_dao_barkskin",
            "木甲护体",
            "守卫获得木甲，减伤+12%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("mu_dao_healing"),
            Optional.of(BastionDao.MU_DAO),
            "mu_dao_barkskin",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "mu_dao_spore_burst",
            "孢子爆发",
            "解锁主动：孢子范围控制",
            BastionTalentNode.NodeType.ACTIVE_SKILL,
            COST_THREE,
            List.of("mu_dao_resource_regen"),
            Optional.of(BastionDao.MU_DAO),
            "mu_dao_spore_burst",
            1.0
        ),
        new BastionTalentNode(
            "mu_dao_fungal_guardian",
            "真菌守卫",
            "解锁主动：召唤持续型真菌守卫",
            BastionTalentNode.NodeType.ACTIVE_SKILL,
            COST_THREE,
            List.of("mu_dao_barkskin"),
            Optional.of(BastionDao.MU_DAO),
            "mu_dao_fungal_guardian",
            1.0
        ),
        new BastionTalentNode(
            "mu_dao_guardian_synergy",
            "共生协同",
            "守卫与植物共生，输出+10%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("mu_dao_fungal_guardian", "mu_dao_resource_regen"),
            Optional.of(BastionDao.MU_DAO),
            "mu_dao_guardian_synergy",
            VALUE_TEN_PERCENT
        ),
        new BastionTalentNode(
            "mu_dao_conversion",
            "木气炼化",
            "木道转化效率+12%，与炼化提升协同",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("mu_dao_guardian_synergy", "resource_conversion"),
            Optional.of(BastionDao.MU_DAO),
            "mu_dao_conversion_bonus",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "mu_dao_guardian_bloom",
            "守卫繁茂",
            "守卫生命与再生+12%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("mu_dao_guardian_synergy", "guardian_defense"),
            Optional.of(BastionDao.MU_DAO),
            "mu_dao_guardian_bloom",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "mu_dao_mastery",
            "木道精通",
            "生长与守卫同调强化",
            BastionTalentNode.NodeType.DAO_MASTERY,
            COST_THREE,
            List.of("mu_dao_conversion", "mu_dao_guardian_bloom"),
            Optional.of(BastionDao.MU_DAO),
            "mu_dao_mastery",
            1.0
        ),
        new BastionTalentNode(
            "mu_dao_affix_rejuvenate",
            "复苏词缀",
            "解锁木道词缀：rejuvenate",
            BastionTalentNode.NodeType.AFFIX_UNLOCK,
            COST_THREE,
            List.of("mu_dao_mastery"),
            Optional.of(BastionDao.MU_DAO),
            "affix_rejuvenate",
            1.0
        ),
        new BastionTalentNode(
            "li_dao_root",
            "力道入门",
            "开启力道天赋分支",
            BastionTalentNode.NodeType.DAO_MASTERY,
            COST_TWO,
            List.of("root_efficiency", "guardian_damage"),
            Optional.of(BastionDao.LI_DAO),
            "li_dao_unlock",
            1.0
        ),
        new BastionTalentNode(
            "li_dao_attack_power",
            "蛮力打磨",
            "力道攻击力+12%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_TWO,
            List.of("li_dao_root"),
            Optional.of(BastionDao.LI_DAO),
            "li_dao_attack_power",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "li_dao_defense",
            "筋骨淬炼",
            "力道防御+10%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_TWO,
            List.of("li_dao_root"),
            Optional.of(BastionDao.LI_DAO),
            "li_dao_defense",
            VALUE_TEN_PERCENT
        ),
        new BastionTalentNode(
            "li_dao_berserker",
            "狂暴姿态",
            "解锁主动：短时间爆发攻速",
            BastionTalentNode.NodeType.ACTIVE_SKILL,
            COST_THREE,
            List.of("li_dao_attack_power"),
            Optional.of(BastionDao.LI_DAO),
            "li_dao_berserker",
            1.0
        ),
        new BastionTalentNode(
            "li_dao_overwhelm",
            "力压群雄",
            "狂暴时额外伤害+15%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("li_dao_berserker"),
            Optional.of(BastionDao.LI_DAO),
            "li_dao_overwhelm",
            VALUE_FIFTEEN_PERCENT
        ),
        new BastionTalentNode(
            "li_dao_guardian_training",
            "守卫军令",
            "守卫攻击协同+12%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("li_dao_attack_power", "guardian_damage"),
            Optional.of(BastionDao.LI_DAO),
            "li_dao_guardian_training",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "li_dao_bulwark",
            "钢筋铁骨",
            "守卫与自身减伤+12%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("li_dao_defense"),
            Optional.of(BastionDao.LI_DAO),
            "li_dao_bulwark",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "li_dao_haste",
            "气血奔腾",
            "移速与攻速+10%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("li_dao_overwhelm"),
            Optional.of(BastionDao.LI_DAO),
            "li_dao_haste",
            VALUE_TEN_PERCENT
        ),
        new BastionTalentNode(
            "li_dao_command_shout",
            "号令怒吼",
            "解锁主动：集结守卫爆发",
            BastionTalentNode.NodeType.ACTIVE_SKILL,
            COST_THREE,
            List.of("li_dao_guardian_training", "li_dao_haste"),
            Optional.of(BastionDao.LI_DAO),
            "li_dao_command_shout",
            1.0
        ),
        new BastionTalentNode(
            "li_dao_unyielding",
            "不屈意志",
            "狂暴后获得护盾，减伤+12%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("li_dao_bulwark"),
            Optional.of(BastionDao.LI_DAO),
            "li_dao_unyielding",
            VALUE_TWELVE_PERCENT
        ),
        new BastionTalentNode(
            "li_dao_counter",
            "蓄劲反击",
            "受击后下次攻击+20%",
            BastionTalentNode.NodeType.PASSIVE_BUFF,
            COST_THREE,
            List.of("li_dao_unyielding"),
            Optional.of(BastionDao.LI_DAO),
            "li_dao_counter",
            VALUE_TWENTY_PERCENT
        ),
        new BastionTalentNode(
            "li_dao_mastery",
            "力道精通",
            "守卫与自身输出全面强化",
            BastionTalentNode.NodeType.DAO_MASTERY,
            COST_THREE,
            List.of("li_dao_command_shout", "li_dao_counter"),
            Optional.of(BastionDao.LI_DAO),
            "li_dao_mastery",
            1.0
        ),
        new BastionTalentNode(
            "li_dao_affix_fury",
            "狂怒词缀",
            "解锁力道词缀：fury",
            BastionTalentNode.NodeType.AFFIX_UNLOCK,
            COST_THREE,
            List.of("li_dao_mastery"),
            Optional.of(BastionDao.LI_DAO),
            "affix_fury",
            1.0
        )
    );

    private static final Map<String, BastionTalentNode> NODE_MAP = NODES
        .stream()
        .collect(java.util.stream.Collectors.toUnmodifiableMap(BastionTalentNode::id, node -> node));

    private BastionTalentRegistry() {
    }

    /**
     * 返回全部节点的只读列表。
     */
    public static List<BastionTalentNode> getAllNodes() {
        return Collections.unmodifiableList(NODES);
    }

    /**
     * 按 id 查询节点，不存在返回 null。
     */
    public static BastionTalentNode getNode(String nodeId) {
        return NODE_MAP.get(nodeId);
    }

    /**
     * 返回全部节点 id（用于命令建议）。
     */
    public static List<String> getAllNodeIds() {
        return Collections.unmodifiableList(NODES.stream().map(BastionTalentNode::id).toList());
    }
}
