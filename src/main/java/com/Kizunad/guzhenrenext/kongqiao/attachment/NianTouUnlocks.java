package com.Kizunad.guzhenrenext.kongqiao.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashSet;
import java.util.Set;

/**
 * 玩家念头鉴定解锁记录。
 * 存储玩家已经鉴定过的物品 ID。
 */
public class NianTouUnlocks implements INBTSerializable<CompoundTag> {

    private final Set<ResourceLocation> unlockedItems = new HashSet<>();

    public void unlock(ResourceLocation item) {
        unlockedItems.add(item);
    }

    public boolean isUnlocked(ResourceLocation item) {
        return unlockedItems.contains(item);
    }

    public Set<ResourceLocation> getUnlockedItems() {
        return new HashSet<>(unlockedItems);
    }
    
    public void setUnlockedItems(Set<ResourceLocation> items) {
        unlockedItems.clear();
        unlockedItems.addAll(items);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (ResourceLocation id : unlockedItems) {
            list.add(StringTag.valueOf(id.toString()));
        }
        tag.put("unlocked", list);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        unlockedItems.clear();
        if (tag.contains("unlocked", Tag.TAG_LIST)) {
            ListTag list = tag.getList("unlocked", Tag.TAG_STRING);
            for (Tag t : list) {
                unlockedItems.add(ResourceLocation.parse(t.getAsString()));
            }
        }
    }
}
