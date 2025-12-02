package com.Kizunad.customNPCs.ai.interaction;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * 任务占位组件：预留任务槽、事件标记的存储。
 */
public class NpcQuestState {

    private boolean questEnabled = true;
    private CompoundTag questSlots = new CompoundTag();

    public boolean isQuestEnabled() {
        return questEnabled;
    }

    public void setQuestEnabled(boolean questEnabled) {
        this.questEnabled = questEnabled;
    }

    public CompoundTag getQuestSlots() {
        return questSlots.copy();
    }

    public void putQuestSlot(String key, CompoundTag data) {
        questSlots.put(key, data.copy());
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("enabled", questEnabled);
        tag.put("slots", questSlots.copy());
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("enabled")) {
            questEnabled = tag.getBoolean("enabled");
        }
        if (tag.contains("slots")) {
            questSlots = tag.getCompound("slots").copy();
        }
    }
}
