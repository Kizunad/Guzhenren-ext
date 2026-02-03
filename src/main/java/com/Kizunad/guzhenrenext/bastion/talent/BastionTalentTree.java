package com.Kizunad.guzhenrenext.bastion.talent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 基地天赋树 - 包含所有天赋节点的完整定义。
 *
 * @param id      天赋树标识符
 * @param nodes   所有节点列表
 */
public record BastionTalentTree(
    String id,
    List<BastionTalentNode> nodes
) {
    /** 按 ID 查找节点的缓存（懒加载）。 */
    private Map<String, BastionTalentNode> nodeMap() {
        return nodes.stream()
            .collect(Collectors.toMap(BastionTalentNode::id, Function.identity()));
    }

    /**
     * 根据 ID 查找节点。
     */
    public Optional<BastionTalentNode> getNode(String nodeId) {
        return Optional.ofNullable(nodeMap().get(nodeId));
    }

    /**
     * 获取节点总数。
     */
    public int size() {
        return nodes.size();
    }

    public static final Codec<BastionTalentTree> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("id").forGetter(BastionTalentTree::id),
            BastionTalentNode.CODEC.listOf().fieldOf("nodes").forGetter(BastionTalentTree::nodes)
        ).apply(instance, BastionTalentTree::new)
    );
}
