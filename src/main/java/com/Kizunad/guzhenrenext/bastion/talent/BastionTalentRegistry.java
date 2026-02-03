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
    private static final double VALUE_FIFTEEN_PERCENT = 0.15;
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
