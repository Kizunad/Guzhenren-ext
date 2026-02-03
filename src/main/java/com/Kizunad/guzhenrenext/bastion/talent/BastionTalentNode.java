package com.Kizunad.guzhenrenext.bastion.talent;

import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

/**
 * 基地天赋节点 - 天赋树中的单个节点定义。
 *
 * @param id           节点唯一标识符
 * @param displayName  显示名称
 * @param description  节点描述
 * @param type         节点类型
 * @param cost         解锁所需天赋点数
 * @param prerequisites 前置节点 ID 列表
 * @param dao          关联的道途（可选，用于道途专精）
 * @param effectId     效果 ID（对应具体实现）
 * @param effectValue  效果数值（如增益百分比）
 */
public record BastionTalentNode(
    String id,
    String displayName,
    String description,
    NodeType type,
    int cost,
    List<String> prerequisites,
    Optional<BastionDao> dao,
    String effectId,
    double effectValue
) {
    /** 节点类型枚举。 */
    public enum NodeType {
        /** 被动增益：持续生效的数值加成。 */
        PASSIVE_BUFF,
        /** 主动技能：需要触发的技能效果。 */
        ACTIVE_SKILL,
        /** 词缀解锁：解锁特定基地词缀。 */
        AFFIX_UNLOCK,
        /** 道途专精：特定道途的专属加成。 */
        DAO_MASTERY;

        public static final Codec<NodeType> CODEC = Codec.STRING.xmap(
            value -> NodeType.valueOf(value.toUpperCase(java.util.Locale.ROOT)),
            type -> type.name().toLowerCase(java.util.Locale.ROOT)
        );
    }

    public static final Codec<BastionTalentNode> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("id").forGetter(BastionTalentNode::id),
            Codec.STRING.fieldOf("display_name").forGetter(BastionTalentNode::displayName),
            Codec.STRING.optionalFieldOf("description", "").forGetter(BastionTalentNode::description),
            NodeType.CODEC.fieldOf("type").forGetter(BastionTalentNode::type),
            Codec.INT.optionalFieldOf("cost", 1).forGetter(BastionTalentNode::cost),
            Codec.STRING.listOf().optionalFieldOf("prerequisites", List.of())
                .forGetter(BastionTalentNode::prerequisites),
            BastionDao.CODEC.optionalFieldOf("dao").forGetter(BastionTalentNode::dao),
            Codec.STRING.optionalFieldOf("effect_id", "").forGetter(BastionTalentNode::effectId),
            Codec.DOUBLE.optionalFieldOf("effect_value", 0.0).forGetter(BastionTalentNode::effectValue)
        ).apply(instance, BastionTalentNode::new)
    );
}
