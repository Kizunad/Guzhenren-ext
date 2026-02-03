package com.Kizunad.guzhenrenext.bastion.talent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.Set;

/**
 * 基地天赋数据 - 存储已解锁的天赋节点和可用天赋点。
 *
 * @param unlockedNodes 已解锁的节点 ID 集合
 * @param availablePoints 可用天赋点数
 * @param totalPointsSpent 已花费的总天赋点数
 */
public record BastionTalentData(
    Set<String> unlockedNodes,
    int availablePoints,
    int totalPointsSpent
) {
    /** 默认空数据。 */
    public static final BastionTalentData DEFAULT = new BastionTalentData(
        Set.of(),
        0,
        0
    );

    /**
     * 检查节点是否已解锁。
     */
    public boolean isUnlocked(String nodeId) {
        return unlockedNodes.contains(nodeId);
    }

    /**
     * 解锁节点并扣除点数。
     */
    public BastionTalentData withUnlock(String nodeId, int cost) {
        Set<String> newUnlocked = new HashSet<>(unlockedNodes);
        newUnlocked.add(nodeId);
        return new BastionTalentData(
            Set.copyOf(newUnlocked),
            availablePoints - cost,
            totalPointsSpent + cost
        );
    }

    /**
     * 添加天赋点。
     */
    public BastionTalentData withAddPoints(int points) {
        return new BastionTalentData(
            unlockedNodes,
            availablePoints + points,
            totalPointsSpent
        );
    }

    /** Set 的 CODEC（转换为 List）。 */
    private static final Codec<Set<String>> STRING_SET_CODEC =
        Codec.STRING.listOf().xmap(Set::copyOf, java.util.List::copyOf);

    public static final Codec<BastionTalentData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            STRING_SET_CODEC.optionalFieldOf("unlocked_nodes", Set.of())
                .forGetter(BastionTalentData::unlockedNodes),
            Codec.INT.optionalFieldOf("available_points", 0)
                .forGetter(BastionTalentData::availablePoints),
            Codec.INT.optionalFieldOf("total_points_spent", 0)
                .forGetter(BastionTalentData::totalPointsSpent)
        ).apply(instance, BastionTalentData::new)
    );
}
