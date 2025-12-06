package com.Kizunad.customNPCs.ai.interaction;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

/**
 * 任务占位组件：预留任务槽、事件标记的存储。
 */
public class NpcQuestState {

    private boolean questEnabled = true;
    private CompoundTag questSlots = new CompoundTag();
    private static final String TASK_IDS_KEY = "task_ids";

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

    /**
     * 获取 NPC 当前分配的任务 ID 列表。
     */
    public List<ResourceLocation> getTaskIds() {
        List<ResourceLocation> ids = new ArrayList<>();
        if (!questSlots.contains(TASK_IDS_KEY, Tag.TAG_LIST)) {
            return ids;
        }
        ListTag list = questSlots.getList(TASK_IDS_KEY, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            Tag tag = list.get(i);
            if (tag instanceof StringTag stringTag) {
                ResourceLocation id = ResourceLocation.tryParse(
                    stringTag.getAsString()
                );
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

    public void setTaskIds(List<ResourceLocation> ids) {
        ListTag list = new ListTag();
        for (ResourceLocation id : ids) {
            list.add(StringTag.valueOf(id.toString()));
        }
        questSlots.put(TASK_IDS_KEY, list);
    }

    /**
     * 若当前没有任务，使用默认列表填充。
     */
    public void ensureTaskIds(List<ResourceLocation> defaults) {
        if (defaults == null || defaults.isEmpty()) {
            return;
        }
        if (!getTaskIds().isEmpty()) {
            return;
        }
        setTaskIds(defaults);
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
