package com.Kizunad.customNPCs.tasks;

import com.Kizunad.customNPCs.tasks.objective.TaskObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.reward.TaskRewardDefinition;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/**
 * 任务定义。
 */
public record TaskDefinition(
    ResourceLocation id,
    String title,
    String description,
    TaskType type,
    List<TaskObjectiveDefinition> objectives,
    List<TaskRewardDefinition> rewards
) {

    public TaskDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(objectives, "objectives");
        Objects.requireNonNull(rewards, "rewards");
    }

    public TaskObjectiveDefinition objective(int index) {
        return objectives.get(index);
    }

    public int objectiveCount() {
        return objectives.size();
    }
}
