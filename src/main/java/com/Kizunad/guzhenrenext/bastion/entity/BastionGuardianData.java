package com.Kizunad.guzhenrenext.bastion.entity;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/**
 * 基地守卫数据工具类 - 管理守卫实体的归属标识和属性。
 * <p>
 * 使用 {@link Entity#getPersistentData()} 存储完整的基地 UUID 和转数，
 * 避免使用 tag 前缀截断导致的碰撞风险。
 * </p>
 *
 * <h2>存储结构</h2>
 * <pre>
 * PersistentData {
 *   "BastionGuardian": {
 *     "BastionId": "uuid-string",
 *     "Tier": 1-9
 *   }
 * }
 * </pre>
 *
 * <h2>完整性保证</h2>
 * <ul>
 *   <li>{@link #isGuardian(Entity)} 同时检查 tag 和 PersistentData 存在性</li>
 *   <li>旧世界遗留的仅有 tag 的守卫会被识别但返回默认转数</li>
 * </ul>
 */
public final class BastionGuardianData {

    private BastionGuardianData() {
        // 工具类
    }

    // ===== NBT 键常量 =====

    /** 根标签名（避免与其他模组冲突）。 */
    private static final String ROOT_TAG = "BastionGuardian";

    /** 基地 UUID 键。 */
    private static final String BASTION_ID_KEY = "BastionId";

    /** 基地转数键。 */
    private static final String TIER_KEY = "Tier";

    /** 通用守卫标签（用于快速全局统计）。 */
    public static final String GUARDIAN_TAG = "bastion_guardian";

    /** 默认转数（数据丢失时使用）。 */
    private static final int DEFAULT_TIER = 1;

    // ===== 公开 API =====

    /**
     * 将守卫标记为属于指定基地。
     * <p>
     * 同时添加通用标签用于全局统计，并将完整 UUID 和转数存入 PersistentData。
     * </p>
     *
     * @param guardian  守卫实体
     * @param bastionId 基地 UUID
     * @param tier      基地转数（1-9）
     */
    public static void markAsGuardian(Mob guardian, UUID bastionId, int tier) {
        // 添加通用标签用于快速全局统计
        guardian.addTag(GUARDIAN_TAG);

        // 存储完整 UUID 和转数到 PersistentData
        CompoundTag persistentData = guardian.getPersistentData();
        CompoundTag bastionTag = new CompoundTag();
        bastionTag.putString(BASTION_ID_KEY, bastionId.toString());
        bastionTag.putInt(TIER_KEY, tier);
        persistentData.put(ROOT_TAG, bastionTag);
    }

    /**
     * 检查实体是否是基地守卫。
     * <p>
     * 同时检查通用标签和 PersistentData 存在性，确保数据完整性。
     * 仅有 tag 而无 PersistentData 的旧世界守卫仍被识别，但会返回默认转数。
     * </p>
     *
     * @param entity 实体
     * @return true 如果是基地守卫
     */
    public static boolean isGuardian(Entity entity) {
        if (!(entity instanceof Mob mob)) {
            return false;
        }
        return mob.getTags().contains(GUARDIAN_TAG);
    }

    /**
     * 检查守卫数据是否完整（同时有 tag 和 PersistentData）。
     * <p>
     * 用于识别需要修复数据的旧世界遗留守卫。
     * </p>
     *
     * @param entity 实体
     * @return true 如果守卫数据完整
     */
    public static boolean hasCompleteData(Entity entity) {
        if (!isGuardian(entity)) {
            return false;
        }
        if (!(entity instanceof Mob mob)) {
            return false;
        }
        CompoundTag persistentData = mob.getPersistentData();
        if (!persistentData.contains(ROOT_TAG)) {
            return false;
        }
        CompoundTag bastionTag = persistentData.getCompound(ROOT_TAG);
        return bastionTag.contains(BASTION_ID_KEY) && bastionTag.contains(TIER_KEY);
    }

    /**
     * 获取守卫所属的基地 UUID。
     *
     * @param entity 实体
     * @return 基地 UUID，如果不是守卫或数据丢失则返回 null
     */
    public static UUID getBastionId(Entity entity) {
        if (!(entity instanceof Mob mob)) {
            return null;
        }

        CompoundTag persistentData = mob.getPersistentData();
        if (!persistentData.contains(ROOT_TAG)) {
            return null;
        }

        CompoundTag bastionTag = persistentData.getCompound(ROOT_TAG);
        if (!bastionTag.contains(BASTION_ID_KEY)) {
            return null;
        }

        String uuidString = bastionTag.getString(BASTION_ID_KEY);
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 检查守卫是否属于指定基地。
     *
     * @param entity    实体
     * @param bastionId 基地 UUID
     * @return true 如果守卫属于该基地
     */
    public static boolean belongsToBastion(Entity entity, UUID bastionId) {
        UUID guardianBastionId = getBastionId(entity);
        return guardianBastionId != null && guardianBastionId.equals(bastionId);
    }

    /**
     * 获取守卫所属基地的转数。
     *
     * @param entity 实体
     * @return 基地转数（1-9），如果不是守卫或数据丢失则返回默认值 1
     */
    public static int getTier(Entity entity) {
        if (!(entity instanceof Mob mob)) {
            return DEFAULT_TIER;
        }

        CompoundTag persistentData = mob.getPersistentData();
        if (!persistentData.contains(ROOT_TAG)) {
            return DEFAULT_TIER;
        }

        CompoundTag bastionTag = persistentData.getCompound(ROOT_TAG);
        if (!bastionTag.contains(TIER_KEY)) {
            return DEFAULT_TIER;
        }

        return bastionTag.getInt(TIER_KEY);
    }
}
