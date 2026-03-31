package com.Kizunad.guzhenrenext.faction.core;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

/**
 * 势力核心数据模型。
 * <p>
 * 本 record 为不可变数据结构，记录势力的基本信息：
 * 1) UUID 唯一标识
 * 2) 名称
 * 3) 类型（宗门/家族/散修群体）
 * 4) 创建时间（游戏 tick）
 * 5) 状态（活跃/解散/战争中）
 * 6) 基础属性（势力值、资源储量）
 * </p>
 */
public record FactionCore(
    UUID id,
    String name,
    FactionType type,
    long createdAt,
    FactionStatus status,
    int power,
    int resources
) {

    // ========== NBT 序列化常量 ==========

    private static final String KEY_ID = "id";

    private static final String KEY_NAME = "name";

    private static final String KEY_TYPE = "type";

    private static final String KEY_CREATED_AT = "createdAt";

    private static final String KEY_STATUS = "status";

    private static final String KEY_POWER = "power";

    private static final String KEY_RESOURCES = "resources";

    // ========== 数值范围常量 ==========

    private static final int MIN_POWER = 0;

    private static final int MAX_POWER = 10000;

    private static final int MIN_RESOURCES = 0;

    private static final int MAX_RESOURCES = 10000;

    /**
     * 规范化构造器。
     * <p>
     * 验证必填字段，限制数值范围。
     * </p>
     */
    public FactionCore {
        Objects.requireNonNull(id, "id 不能为空");
        Objects.requireNonNull(name, "name 不能为空");
        Objects.requireNonNull(type, "type 不能为空");
        Objects.requireNonNull(status, "status 不能为空");

        if (name.isEmpty()) {
            throw new IllegalArgumentException("name 不能为空字符串");
        }

        if (createdAt < 0L) {
            throw new IllegalArgumentException("createdAt 不能为负数");
        }

        // 限制势力值范围
        if (power < MIN_POWER || power > MAX_POWER) {
            throw new IllegalArgumentException(
                String.format("power 必须在 [%d, %d] 范围内，当前值：%d", MIN_POWER, MAX_POWER, power)
            );
        }

        // 限制资源储量范围
        if (resources < MIN_RESOURCES || resources > MAX_RESOURCES) {
            throw new IllegalArgumentException(
                String.format("resources 必须在 [%d, %d] 范围内，当前值：%d", MIN_RESOURCES, MAX_RESOURCES, resources)
            );
        }
    }

    /**
     * 将势力数据序列化到 NBT。
     *
     * @return 包含势力数据的 CompoundTag
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID(KEY_ID, id);
        tag.putString(KEY_NAME, name);
        tag.putString(KEY_TYPE, type.name());
        tag.putLong(KEY_CREATED_AT, createdAt);
        tag.putString(KEY_STATUS, status.name());
        tag.putInt(KEY_POWER, power);
        tag.putInt(KEY_RESOURCES, resources);
        return tag;
    }

    /**
     * 从 NBT 反序列化势力数据。
     *
     * @param tag 包含势力数据的 CompoundTag
     * @return 反序列化后的 FactionCore 实例；若数据不完整则返回 null
     */
    public static FactionCore load(CompoundTag tag) {
        // 验证必填字段存在
        if (!tag.hasUUID(KEY_ID) || !tag.contains(KEY_NAME) || !tag.contains(KEY_TYPE)
            || !tag.contains(KEY_CREATED_AT) || !tag.contains(KEY_STATUS) || !tag.contains(KEY_POWER)
            || !tag.contains(KEY_RESOURCES)) {
            return null;
        }

        try {
            UUID loadedId = tag.getUUID(KEY_ID);
            String loadedName = tag.getString(KEY_NAME);
            FactionType loadedType = parseEnumOrDefault(tag.getString(KEY_TYPE), FactionType.class, null);
            long loadedCreatedAt = tag.getLong(KEY_CREATED_AT);
            FactionStatus loadedStatus = parseEnumOrDefault(tag.getString(KEY_STATUS), FactionStatus.class, null);
            int loadedPower = tag.getInt(KEY_POWER);
            int loadedResources = tag.getInt(KEY_RESOURCES);

            // 验证枚举值有效
            if (loadedType == null || loadedStatus == null) {
                return null;
            }

            return new FactionCore(loadedId, loadedName, loadedType, loadedCreatedAt, loadedStatus, loadedPower,
                loadedResources);
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
     * 势力类型枚举。
     */
    public enum FactionType {
        /**
         * 宗门。
         */
        SECT,

        /**
         * 家族。
         */
        CLAN,

        /**
         * 散修群体。
         */
        ROGUE_GROUP
    }

    /**
     * 势力状态枚举。
     */
    public enum FactionStatus {
        /**
         * 活跃。
         */
        ACTIVE,

        /**
         * 解散。
         */
        DISSOLVED,

        /**
         * 战争中。
         */
        AT_WAR
    }
}
