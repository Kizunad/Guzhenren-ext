package com.Kizunad.guzhenrenext.kongqiao.parasite;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 玩家寄生升级数据。
 * <p>
 * 使用枚举 -> 等级的 Map 保存解锁状态与等级，后续效果应用可按需读取。
 * </p>
 */
public class ParasiteUpgradeData implements INBTSerializable<CompoundTag> {

    private static final String TAG_LEVELS = "Levels";

    private final Map<ParasiteUpgrade, Integer> upgradeLevels = new EnumMap<>(ParasiteUpgrade.class);

    public ParasiteUpgradeData() {}

    /**
     * 查询是否已解锁某升级。
     */
    public boolean hasUpgrade(ParasiteUpgrade upgrade) {
        return getLevel(upgrade) > 0;
    }

    /**
     * 获取指定升级的当前等级，未解锁时返回 0。
     */
    public int getLevel(ParasiteUpgrade upgrade) {
        return upgradeLevels.getOrDefault(upgrade, 0);
    }

    /**
     * 尝试解锁或提升升级等级，最大不超过定义的 maxLevel。
     * <p>
     * 仅进行数据写入，具体消耗/校验由上层逻辑决定。
     * </p>
     */
    public void unlockOrLevelUp(ParasiteUpgrade upgrade) {
        int current = getLevel(upgrade);
        int next = Math.min(current + 1, upgrade.getMaxLevel());
        upgradeLevels.put(upgrade, next);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        CompoundTag levelsTag = new CompoundTag();
        for (Map.Entry<ParasiteUpgrade, Integer> entry : upgradeLevels.entrySet()) {
            levelsTag.putInt(entry.getKey().name(), entry.getValue());
        }
        tag.put(TAG_LEVELS, levelsTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        upgradeLevels.clear();
        if (!nbt.contains(TAG_LEVELS)) {
            return;
        }
        CompoundTag levelsTag = nbt.getCompound(TAG_LEVELS);
        for (ParasiteUpgrade upgrade : ParasiteUpgrade.values()) {
            if (levelsTag.contains(upgrade.name())) {
                int level = levelsTag.getInt(upgrade.name());
                if (level > 0) {
                    upgradeLevels.put(upgrade, Math.min(level, upgrade.getMaxLevel()));
                }
            }
        }
    }
}
