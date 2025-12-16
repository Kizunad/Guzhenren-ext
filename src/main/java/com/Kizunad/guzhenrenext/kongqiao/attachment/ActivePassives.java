package com.Kizunad.guzhenrenext.kongqiao.attachment;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 存储实体当前激活的被动蛊虫 UsageID 列表。
 * 用于快速查询，避免每次事件都扫描背包。
 */
public class ActivePassives implements INBTSerializable<CompoundTag> {

    private final Set<String> activeUsageIds = new HashSet<>();

    public ActivePassives() {}

    public void add(String usageId) {
        activeUsageIds.add(usageId);
    }

    public void remove(String usageId) {
        activeUsageIds.remove(usageId);
    }

    public boolean isActive(String usageId) {
        return activeUsageIds.contains(usageId);
    }

    public void clear() {
        activeUsageIds.clear();
    }

    @Override
    public CompoundTag serializeNBT(
        net.minecraft.core.HolderLookup.Provider provider
    ) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (String id : activeUsageIds) {
            list.add(StringTag.valueOf(id));
        }
        tag.put("ActiveIds", list);
        return tag;
    }

    @Override
    public void deserializeNBT(
        net.minecraft.core.HolderLookup.Provider provider,
        CompoundTag nbt
    ) {
        activeUsageIds.clear();
        if (nbt.contains("ActiveIds")) {
            ListTag list = nbt.getList("ActiveIds", Tag.TAG_STRING);
            for (Tag t : list) {
                activeUsageIds.add(t.getAsString());
            }
        }
    }
}
