package com.Kizunad.guzhenrenext.faction.core;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

/**
 * 势力成员数据模型。
 * <p>
 * 本 record 为不可变数据结构，记录成员在势力中的身份信息：
 * 1) 成员 UUID（NPC 或玩家）
 * 2) 所属势力 UUID
 * 3) 成员角色（掌门/长老/弟子/外门弟子）
 * 4) 加入时间（游戏 tick）
 * 5) 贡献值（0-100000）
 * </p>
 */
public record FactionMembership(
    UUID memberId,
    UUID factionId,
    MemberRole role,
    long joinedAt,
    int contribution
) {

    // ========== NBT 序列化常量 ==========

    private static final String KEY_MEMBER_ID = "memberId";

    private static final String KEY_FACTION_ID = "factionId";

    private static final String KEY_ROLE = "role";

    private static final String KEY_JOINED_AT = "joinedAt";

    private static final String KEY_CONTRIBUTION = "contribution";

    // ========== 数值范围常量 ==========

    private static final int MIN_CONTRIBUTION = 0;

    private static final int MAX_CONTRIBUTION = 100000;

    /**
     * 规范化构造器。
     * <p>
     * 验证必填字段，限制数值范围。
     * </p>
     */
    public FactionMembership {
        Objects.requireNonNull(memberId, "memberId 不能为空");
        Objects.requireNonNull(factionId, "factionId 不能为空");
        Objects.requireNonNull(role, "role 不能为空");

        if (joinedAt < 0L) {
            throw new IllegalArgumentException("joinedAt 不能为负数");
        }

        // 限制贡献值范围
        if (contribution < MIN_CONTRIBUTION || contribution > MAX_CONTRIBUTION) {
            throw new IllegalArgumentException(
                String.format("contribution 必须在 [%d, %d] 范围内，当前值：%d", MIN_CONTRIBUTION, MAX_CONTRIBUTION,
                    contribution)
            );
        }
    }

    /**
     * 将成员数据序列化到 NBT。
     *
     * @return 包含成员数据的 CompoundTag
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(KEY_MEMBER_ID, memberId);
        tag.putUUID(KEY_FACTION_ID, factionId);
        tag.putString(KEY_ROLE, role.name());
        tag.putLong(KEY_JOINED_AT, joinedAt);
        tag.putInt(KEY_CONTRIBUTION, contribution);
        return tag;
    }

    /**
     * 从 NBT 反序列化成员数据。
     *
     * @param tag 包含成员数据的 CompoundTag
     * @return 反序列化后的 FactionMembership 实例；若数据不完整则返回 null
     */
    public static FactionMembership load(CompoundTag tag) {
        // 验证必填字段存在
        if (!tag.hasUUID(KEY_MEMBER_ID) || !tag.hasUUID(KEY_FACTION_ID) || !tag.contains(KEY_ROLE)
            || !tag.contains(KEY_JOINED_AT) || !tag.contains(KEY_CONTRIBUTION)) {
            return null;
        }

        try {
            UUID loadedMemberId = tag.getUUID(KEY_MEMBER_ID);
            UUID loadedFactionId = tag.getUUID(KEY_FACTION_ID);
            MemberRole loadedRole = parseEnumOrDefault(tag.getString(KEY_ROLE), MemberRole.class, null);
            long loadedJoinedAt = tag.getLong(KEY_JOINED_AT);
            int loadedContribution = tag.getInt(KEY_CONTRIBUTION);

            // 验证枚举值有效
            if (loadedRole == null) {
                return null;
            }

            return new FactionMembership(loadedMemberId, loadedFactionId, loadedRole, loadedJoinedAt,
                loadedContribution);
        } catch (Exception exception) {
            // 数据格式错误或类型转换失败
            return null;
        }
    }

    /**
     * 安全解析枚举值。
     *
     * @param storedName 存储的枚举名称
     * @param enumType 枚举类型
     * @param fallback 解析失败时的回退值
     * @return 解析后的枚举值或回退值
     */
    private static <E extends Enum<E>> E parseEnumOrDefault(String storedName, Class<E> enumType, E fallback) {
        if (storedName == null || storedName.isEmpty()) {
            return fallback;
        }
        try {
            return Enum.valueOf(enumType, storedName);
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    /**
     * 成员角色枚举。
     */
    public enum MemberRole {
        /**
         * 掌门/族长。
         */
        LEADER,

        /**
         * 长老。
         */
        ELDER,

        /**
         * 弟子/成员。
         */
        MEMBER,

        /**
         * 外门弟子。
         */
        OUTER_DISCIPLE
    }
}
