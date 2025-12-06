package com.Kizunad.customNPCs.tasks.data;

import com.Kizunad.customNPCs.tasks.TaskDefinition;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 玩家身上的任务数据。
 */
public class PlayerTaskData implements INBTSerializable<CompoundTag> {

    private final Map<ResourceLocation, TaskProgress> activeTasks =
        new HashMap<>();
    private final Set<ResourceLocation> completedTasks = new HashSet<>();

    public boolean hasActive(ResourceLocation id) {
        return activeTasks.containsKey(id);
    }

    public boolean hasCompleted(ResourceLocation id) {
        return completedTasks.contains(id);
    }

    public TaskProgress getProgress(ResourceLocation id) {
        return activeTasks.get(id);
    }

    public Collection<TaskProgress> getActiveTasks() {
        return activeTasks.values();
    }

    public boolean accept(TaskDefinition definition, UUID questGiver) {
        if (
            definition == null ||
            questGiver == null ||
            hasActive(definition.id()) ||
            hasCompleted(definition.id())
        ) {
            return false;
        }
        TaskProgress progress = new TaskProgress(
            definition.id(),
            questGiver,
            definition.objectiveCount()
        );
        activeTasks.put(definition.id(), progress);
        return true;
    }

    public void completeTask(ResourceLocation id) {
        TaskProgress removed = activeTasks.remove(id);
        if (removed != null) {
            completedTasks.add(id);
        }
    }

    public void abandon(ResourceLocation id) {
        activeTasks.remove(id);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag active = new ListTag();
        for (TaskProgress progress : activeTasks.values()) {
            active.add(progress.serializeNBT(provider));
        }
        tag.put("active", active);
        ListTag completed = new ListTag();
        for (ResourceLocation id : completedTasks) {
            completed.add(StringTag.valueOf(id.toString()));
        }
        tag.put("completed", completed);
        return tag;
    }

    @Override
    public void deserializeNBT(
        HolderLookup.Provider provider,
        CompoundTag nbt
    ) {
        activeTasks.clear();
        completedTasks.clear();
        if (nbt.contains("active", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("active", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                Tag tag = list.get(i);
                if (tag instanceof CompoundTag compound) {
                    TaskProgress progress = TaskProgress.fromNBT(compound);
                    if (progress != null) {
                        activeTasks.put(progress.getTaskId(), progress);
                    }
                }
            }
        }
        if (nbt.contains("completed", Tag.TAG_LIST)) {
            ListTag list = nbt.getList("completed", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                Tag tag = list.get(i);
                if (tag instanceof StringTag stringTag) {
                    ResourceLocation id = ResourceLocation.tryParse(
                        stringTag.getAsString()
                    );
                    if (id != null) {
                        completedTasks.add(id);
                    }
                }
            }
        }
    }
}
