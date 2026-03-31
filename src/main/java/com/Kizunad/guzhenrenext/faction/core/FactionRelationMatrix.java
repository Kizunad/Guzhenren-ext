package com.Kizunad.guzhenrenext.faction.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/**
 * 势力间关系矩阵。
 * <p>
 * 本类为可变数据结构，记录势力间的关系值。关系值范围为 -100 到 +100，分为四个等级：
 * 1) HOSTILE（敌对，值 < -50）
 * 2) NEUTRAL（中立，-50 到 +50）
 * 3) FRIENDLY（友好，+50 到 +80）
 * 4) ALLIED（同盟，> +80）
 * </p>
 * <p>
 * 关系具有对称性：getRelation(A, B) == getRelation(B, A)。
 * 内部使用组合键（两个 UUID 的字典序排序后拼接）确保对称性。
 * </p>
 */
public final class FactionRelationMatrix {

    // ========== NBT 序列化常量 ==========

    private static final String KEY_RELATIONS = "relations";

    private static final String KEY_FACTION_A = "factionA";

    private static final String KEY_FACTION_B = "factionB";

    private static final String KEY_VALUE = "value";

    // ========== 关系值范围常量 ==========

    private static final int MIN_RELATION = -100;

    private static final int MAX_RELATION = 100;

    private static final int DEFAULT_RELATION = 0;

    // ========== 关系等级阈值常量 ==========

    private static final int HOSTILE_THRESHOLD = -50;

    private static final int FRIENDLY_THRESHOLD = 50;

    private static final int ALLIED_THRESHOLD = 80;

    // ========== 内部存储 ==========

    /**
     * 关系存储。key 为两个 UUID 的组合键（字典序排序），value 为关系值。
     */
    private final Map<String, Integer> relations;

    /**
     * 构造器。初始化空的关系矩阵。
     */
    public FactionRelationMatrix() {
        this.relations = new HashMap<>();
    }

    /**
     * 获取两个势力间的关系值。
     * <p>
     * 关系具有对称性，getRelation(A, B) == getRelation(B, A)。
     * 若关系未设置，返回默认值 0（中立）。
     * </p>
     *
     * @param factionA 第一个势力的 UUID
     * @param factionB 第二个势力的 UUID
     * @return 关系值，范围 [-100, 100]；未设置时返回 0
     */
    public int getRelation(UUID factionA, UUID factionB) {
        if (factionA == null || factionB == null) {
            return DEFAULT_RELATION;
        }

        // 自己与自己的关系为 0
        if (factionA.equals(factionB)) {
            return DEFAULT_RELATION;
        }

        String key = makeCompositeKey(factionA, factionB);
        return relations.getOrDefault(key, DEFAULT_RELATION);
    }

    /**
     * 设置两个势力间的关系值。
     * <p>
     * 关系值会自动限制在 [-100, 100] 范围内。
     * 关系具有对称性，setRelation(A, B, v) 同时设置 setRelation(B, A, v)。
     * </p>
     *
     * @param factionA 第一个势力的 UUID
     * @param factionB 第二个势力的 UUID
     * @param value    关系值（会被 clamp 到 [-100, 100]）
     */
    public void setRelation(UUID factionA, UUID factionB, int value) {
        if (factionA == null || factionB == null) {
            return;
        }

        // 自己与自己的关系不能修改
        if (factionA.equals(factionB)) {
            return;
        }

        // 限制关系值范围
        int clampedValue = Math.max(MIN_RELATION, Math.min(MAX_RELATION, value));

        String key = makeCompositeKey(factionA, factionB);
        relations.put(key, clampedValue);
    }

    /**
     * 获取两个势力间的关系等级。
     *
     * @param factionA 第一个势力的 UUID
     * @param factionB 第二个势力的 UUID
     * @return 关系等级枚举
     */
    public RelationLevel getRelationLevel(UUID factionA, UUID factionB) {
        int value = getRelation(factionA, factionB);

        if (value < HOSTILE_THRESHOLD) {
            return RelationLevel.HOSTILE;
        } else if (value > ALLIED_THRESHOLD) {
            return RelationLevel.ALLIED;
        } else if (value > FRIENDLY_THRESHOLD) {
            return RelationLevel.FRIENDLY;
        } else {
            return RelationLevel.NEUTRAL;
        }
    }

    /**
     * 将关系矩阵序列化到 NBT。
     *
     * @return 包含关系数据的 CompoundTag
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag relationsList = new ListTag();

        for (Map.Entry<String, Integer> entry : relations.entrySet()) {
            CompoundTag relationTag = new CompoundTag();
            String[] uuids = entry.getKey().split(":");
            if (uuids.length == 2) {
                try {
                    UUID factionA = UUID.fromString(uuids[0]);
                    UUID factionB = UUID.fromString(uuids[1]);
                    relationTag.putUUID(KEY_FACTION_A, factionA);
                    relationTag.putUUID(KEY_FACTION_B, factionB);
                    relationTag.putInt(KEY_VALUE, entry.getValue());
                    relationsList.add(relationTag);
                } catch (IllegalArgumentException exception) {
                    // 跳过无效的 UUID 格式
                }
            }
        }

        tag.put(KEY_RELATIONS, relationsList);
        return tag;
    }

    /**
     * 从 NBT 反序列化关系矩阵。
     *
     * @param tag 包含关系数据的 CompoundTag
     * @return 反序列化后的 FactionRelationMatrix 实例；若数据不完整则返回 null
     */
    public static FactionRelationMatrix load(CompoundTag tag) {
        if (!tag.contains(KEY_RELATIONS, Tag.TAG_LIST)) {
            return null;
        }

        try {
            FactionRelationMatrix matrix = new FactionRelationMatrix();
            ListTag relationsList = tag.getList(KEY_RELATIONS, Tag.TAG_COMPOUND);

            for (int i = 0; i < relationsList.size(); i++) {
                CompoundTag relationTag = relationsList.getCompound(i);

                if (!relationTag.hasUUID(KEY_FACTION_A) || !relationTag.hasUUID(KEY_FACTION_B)
                    || !relationTag.contains(KEY_VALUE)) {
                    continue;
                }

                UUID factionA = relationTag.getUUID(KEY_FACTION_A);
                UUID factionB = relationTag.getUUID(KEY_FACTION_B);
                int value = relationTag.getInt(KEY_VALUE);

                matrix.setRelation(factionA, factionB, value);
            }

            return matrix;
        } catch (Exception exception) {
            // 数据格式错误或类型转换失败
            return null;
        }
    }

    /**
     * 生成两个 UUID 的组合键。
     * <p>
     * 使用字典序排序确保 A-B 和 B-A 使用同一个 key，保证对称性。
     * </p>
     *
     * @param factionA 第一个势力的 UUID
     * @param factionB 第二个势力的 UUID
     * @return 组合键（格式：UUID1:UUID2，其中 UUID1 <= UUID2）
     */
    private static String makeCompositeKey(UUID factionA, UUID factionB) {
        int comparison = factionA.compareTo(factionB);
        if (comparison <= 0) {
            return factionA + ":" + factionB;
        } else {
            return factionB + ":" + factionA;
        }
    }

    /**
     * 关系等级枚举。
     */
    public enum RelationLevel {
        /**
         * 敌对（关系值 < -50）。
         */
        HOSTILE,

        /**
         * 中立（关系值 -50 到 +50）。
         */
        NEUTRAL,

        /**
         * 友好（关系值 +50 到 +80）。
         */
        FRIENDLY,

        /**
         * 同盟（关系值 > +80）。
         */
        ALLIED
    }
}
