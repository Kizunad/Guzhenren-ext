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
 * 玩家身上的任务数据，按照 NPC（任务发布者）区分，避免所有 NPC 共享同一套进度。
 */
public class PlayerTaskData implements INBTSerializable<CompoundTag> {

    private static final UUID GLOBAL_KEY = new UUID(0L, 0L);

    /**
     * 每个 NPC 对应一组进行中的任务。
     */
    private final Map<UUID, Map<ResourceLocation, TaskProgress>> activeTasks =
        new HashMap<>();

    /**
     * 每个 NPC 对应一组已完成的任务。
     */
    private final Map<UUID, Set<ResourceLocation>> completedTasks =
        new HashMap<>();

    public boolean hasActive(ResourceLocation id, UUID questGiver) {
        return getActiveMap(questGiver).containsKey(id);
    }

    public boolean hasCompleted(ResourceLocation id, UUID questGiver) {
        return getCompletedSet(questGiver).contains(id);
    }

    public TaskProgress getProgress(ResourceLocation id, UUID questGiver) {
        return getActiveMap(questGiver).get(id);
    }

    public Collection<TaskProgress> getActiveTasks(UUID questGiver) {
        return getActiveMap(questGiver).values();
    }

    public boolean accept(TaskDefinition definition, UUID questGiver) {
        if (definition == null || questGiver == null) {
            return false;
        }
        if (
            hasActive(definition.id(), questGiver) ||
            hasCompleted(definition.id(), questGiver)
        ) {
            return false;
        }
        TaskProgress progress = new TaskProgress(
            definition.id(),
            questGiver,
            definition.objectiveCount()
        );
        getActiveMap(questGiver).put(definition.id(), progress);
        return true;
    }

    public void completeTask(ResourceLocation id, UUID questGiver) {
        TaskProgress removed = getActiveMap(questGiver).remove(id);
        if (removed != null) {
            getCompletedSet(questGiver).add(id);
        }
    }

    public void abandon(ResourceLocation id, UUID questGiver) {
        getActiveMap(questGiver).remove(id);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag active = new ListTag();
        for (
            Map.Entry<UUID, Map<ResourceLocation, TaskProgress>> entry : activeTasks.entrySet()
        ) {
            CompoundTag wrapper = new CompoundTag();
            wrapper.putUUID("npc", entry.getKey());
            ListTag tasks = new ListTag();
            for (TaskProgress progress : entry.getValue().values()) {
                tasks.add(progress.serializeNBT(provider));
            }
            wrapper.put("tasks", tasks);
            active.add(wrapper);
        }
        tag.put("active", active);

        ListTag completed = new ListTag();
        for (
            Map.Entry<UUID, Set<ResourceLocation>> entry : completedTasks.entrySet()
        ) {
            CompoundTag wrapper = new CompoundTag();
            wrapper.putUUID("npc", entry.getKey());
            ListTag ids = new ListTag();
            for (ResourceLocation id : entry.getValue()) {
                ids.add(StringTag.valueOf(id.toString()));
            }
            wrapper.put("ids", ids);
            completed.add(wrapper);
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
                if (!(tag instanceof CompoundTag compound)) {
                    continue;
                }
                // 新格式：包含 npc + tasks
                if (compound.contains("tasks", Tag.TAG_LIST)) {
                    UUID npcId = readNpcId(compound);
                    ListTag tasks = compound.getList("tasks", Tag.TAG_COMPOUND);
                    for (int j = 0; j < tasks.size(); j++) {
                        Tag inner = tasks.get(j);
                        if (inner instanceof CompoundTag taskCompound) {
                            TaskProgress progress = TaskProgress.fromNBT(
                                taskCompound
                            );
                            if (progress != null) {
                                getActiveMap(npcId).put(
                                    progress.getTaskId(),
                                    progress
                                );
                            }
                        }
                    }
                    continue;
                }
                // 兼容旧格式：单层 TaskProgress 列表
                TaskProgress progress = TaskProgress.fromNBT(compound);
                if (progress != null) {
                    UUID npcId = progress.getQuestGiver() != null
                        ? progress.getQuestGiver()
                        : GLOBAL_KEY;
                    getActiveMap(npcId).put(progress.getTaskId(), progress);
                }
            }
        }
        if (nbt.contains("completed", Tag.TAG_LIST)) {
            ListTag compoundList = nbt.getList("completed", Tag.TAG_COMPOUND);
            if (!compoundList.isEmpty()) {
                for (int i = 0; i < compoundList.size(); i++) {
                    Tag tag = compoundList.get(i);
                    if (!(tag instanceof CompoundTag compound)) {
                        continue;
                    }
                    // 新格式：包含 npc + ids
                    if (compound.contains("ids", Tag.TAG_LIST)) {
                        UUID npcId = readNpcId(compound);
                        ListTag ids = compound.getList("ids", Tag.TAG_STRING);
                        for (int j = 0; j < ids.size(); j++) {
                            Tag inner = ids.get(j);
                            if (inner instanceof StringTag stringTag) {
                                ResourceLocation id = ResourceLocation.tryParse(
                                    stringTag.getAsString()
                                );
                                if (id != null) {
                                    getCompletedSet(npcId).add(id);
                                }
                            }
                        }
                        continue;
                    }
                    // 兼容旧格式：字符串列表
                    if (compound.contains("npc")) {
                        // 防止旧格式混入其他字段
                        continue;
                    }
                    ResourceLocation id = ResourceLocation.tryParse(
                        compound.getString("id")
                    );
                    if (id != null) {
                        getCompletedSet(GLOBAL_KEY).add(id);
                    }
                }
            } else {
                // 兼容旧格式：直接存储字符串列表
                ListTag stringList = nbt.getList(
                    "completed",
                    Tag.TAG_STRING
                );
                for (int i = 0; i < stringList.size(); i++) {
                    Tag tag = stringList.get(i);
                    if (tag instanceof StringTag stringTag) {
                        ResourceLocation id = ResourceLocation.tryParse(
                            stringTag.getAsString()
                        );
                        if (id != null) {
                            getCompletedSet(GLOBAL_KEY).add(id);
                        }
                    }
                }
            }
        }
    }

    private Map<ResourceLocation, TaskProgress> getActiveMap(UUID npcId) {
        return activeTasks.computeIfAbsent(
            npcId == null ? GLOBAL_KEY : npcId,
            key -> new HashMap<>()
        );
    }

    private Set<ResourceLocation> getCompletedSet(UUID npcId) {
        return completedTasks.computeIfAbsent(
            npcId == null ? GLOBAL_KEY : npcId,
            key -> new HashSet<>()
        );
    }

    private static UUID readNpcId(CompoundTag tag) {
        if (tag.hasUUID("npc")) {
            return tag.getUUID("npc");
        }
        return GLOBAL_KEY;
    }
}
