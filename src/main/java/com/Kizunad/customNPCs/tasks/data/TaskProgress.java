package com.Kizunad.customNPCs.tasks.data;

import com.Kizunad.customNPCs.tasks.TaskDefinition;
import com.Kizunad.customNPCs.tasks.objective.SubmitItemObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.objective.TaskObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.objective.TaskObjectiveType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

/**
 * 单个任务的玩家进度。
 */
public class TaskProgress {

    private final ResourceLocation taskId;
    @Nullable
    private UUID questGiver;
    private final int[] objectiveProgress;
    private TaskProgressState state = TaskProgressState.ACCEPTED;

    public TaskProgress(
        ResourceLocation taskId,
        UUID questGiver,
        int objectiveCount
    ) {
        this.taskId = taskId;
        this.questGiver = questGiver;
        this.objectiveProgress = new int[Math.max(1, objectiveCount)];
    }

    public ResourceLocation getTaskId() {
        return taskId;
    }

    @Nullable
    public UUID getQuestGiver() {
        return questGiver;
    }

    public void setQuestGiver(@Nullable UUID questGiver) {
        this.questGiver = questGiver;
    }

    public TaskProgressState getState() {
        return state;
    }

    public void setState(TaskProgressState state) {
        this.state = state;
    }

    public int getObjectiveProgress(int index) {
        return objectiveProgress[index];
    }

    public void setObjectiveProgress(int index, int value) {
        objectiveProgress[index] = Math.max(0, value);
    }

    public void incrementObjective(int index, int delta, int max) {
        int clamped = Math.min(max, objectiveProgress[index] + delta);
        objectiveProgress[index] = Math.max(0, clamped);
    }

    public boolean isCompleted(TaskDefinition definition) {
        if (state == TaskProgressState.COMPLETED) {
            return true;
        }
        for (int i = 0; i < definition.objectiveCount(); i++) {
            TaskObjectiveDefinition obj = definition.objective(i);
            if (obj instanceof SubmitItemObjectiveDefinition submit) {
                if (objectiveProgress[i] < submit.requiredCount()) {
                    return false;
                }
            } else if (obj.getType() == TaskObjectiveType.SUBMIT_ITEM) {
                // fallback
                return false;
            }
        }
        return true;
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("task", taskId.toString());
        if (questGiver != null) {
            tag.putUUID("giver", questGiver);
        }
        tag.putString("state", state.name());
        tag.put("progress", new IntArrayTag(objectiveProgress));
        return tag;
    }

    public static TaskProgress fromNBT(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("task"));
        if (id == null) {
            return null;
        }
        int[] progress = tag.contains("progress")
            ? tag.getIntArray("progress")
            : new int[] {0};
        TaskProgress data = new TaskProgress(id, null, progress.length);
        if (tag.hasUUID("giver")) {
            data.questGiver = tag.getUUID("giver");
        }
        if (tag.contains("state")) {
            try {
                data.state = TaskProgressState.valueOf(tag.getString("state"));
            } catch (IllegalArgumentException ignored) {}
        }
        System.arraycopy(
            progress,
            0,
            data.objectiveProgress,
            0,
            Math.min(progress.length, data.objectiveProgress.length)
        );
        return data;
    }
}
