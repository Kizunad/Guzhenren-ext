package com.Kizunad.guzhenrenext.bastion.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

/**
 * Boss 奖励数据工具。
 * <p>
 * 语义：在 Boss 生成时将奖励倍率写入 PersistentData，
 * 掉落阶段读取并放大掉落表或经验。
 * </p>
 */
public final class BossRewardData {

    private BossRewardData() {
    }

    /** 根标签名，避免与其他模组冲突。 */
    public static final String ROOT_KEY = "GuzhenrenBoss";
    /** 是否为基地 Boss 的标记键。 */
    public static final String IS_BOSS_KEY = "IsBoss";
    /** 奖励倍率键。 */
    public static final String REWARD_MULTIPLIER_KEY = "RewardMultiplier";

    /**
     * 读取奖励倍率，缺省为 1.0。
     */
    public static double getRewardMultiplier(LivingEntity entity) {
        if (entity == null) {
            return 1.0d;
        }
        CompoundTag root = entity.getPersistentData();
        if (!root.contains(ROOT_KEY)) {
            return 1.0d;
        }
        CompoundTag tag = root.getCompound(ROOT_KEY);
        if (!tag.contains(REWARD_MULTIPLIER_KEY)) {
            return 1.0d;
        }
        return Math.max(0.0d, tag.getDouble(REWARD_MULTIPLIER_KEY));
    }

    /**
     * 判断是否标记为基地 Boss。
     */
    public static boolean isBoss(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        CompoundTag root = entity.getPersistentData();
        if (!root.contains(ROOT_KEY)) {
            return false;
        }
        CompoundTag tag = root.getCompound(ROOT_KEY);
        return tag.getBoolean(IS_BOSS_KEY);
    }
}
