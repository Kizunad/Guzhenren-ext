package com.Kizunad.customNPCs.tasks;

import com.Kizunad.customNPCs.capabilities.tasks.PlayerTaskAttachment;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.network.OpenTaskBoardPayload;
import com.Kizunad.customNPCs.network.OpenTaskBoardPayload.SubmitObjectiveEntry;
import com.Kizunad.customNPCs.network.OpenTaskBoardPayload.TaskEntry;
import com.Kizunad.customNPCs.tasks.data.PlayerTaskData;
import com.Kizunad.customNPCs.tasks.data.TaskProgress;
import com.Kizunad.customNPCs.tasks.data.TaskProgressState;
import com.Kizunad.customNPCs.tasks.objective.SubmitItemObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.objective.TaskObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.reward.ItemRewardDefinition;
import com.Kizunad.customNPCs.tasks.reward.TaskRewardDefinition;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;

/**
 * 服务端→客户端任务同步服务。
 */
public final class TaskBoardSyncService {

    private TaskBoardSyncService() {}

    public static OpenTaskBoardPayload buildPayload(
        CustomNpcEntity npc,
        ServerPlayer player
    ) {
        List<TaskDefinition> definitions = NpcTaskBoard.collectTasks(npc);
        PlayerTaskData data = PlayerTaskAttachment.get(player);
        List<TaskEntry> entries = new ArrayList<>();
        for (TaskDefinition definition : definitions) {
            TaskProgress progress = data.getProgress(definition.id());
            boolean completed = progress == null &&
                data.hasCompleted(definition.id());
            TaskProgressState state;
            if (progress != null) {
                state = progress.getState();
            } else if (completed) {
                state = TaskProgressState.COMPLETED;
            } else {
                state = TaskProgressState.AVAILABLE;
            }
            List<SubmitObjectiveEntry> objectives = buildObjectives(
                definition,
                progress,
                completed
            );
            List<ItemStack> rewards = buildRewards(definition);
            entries.add(new TaskEntry(
                definition.id(),
                Component.literal(definition.title()),
                Component.literal(definition.description()),
                definition.type(),
                state,
                objectives,
                rewards
            ));
        }
        return new OpenTaskBoardPayload(npc.getId(), entries);
    }

    private static List<SubmitObjectiveEntry> buildObjectives(
        TaskDefinition definition,
        TaskProgress progress,
        boolean completed
    ) {
        List<SubmitObjectiveEntry> list = new ArrayList<>();
        for (int i = 0; i < definition.objectiveCount(); i++) {
            TaskObjectiveDefinition objective = definition.objective(i);
            if (objective instanceof SubmitItemObjectiveDefinition submit) {
                ItemStack itemStack = createObjectiveStack(submit);
                int current = progress != null
                    ? progress.getObjectiveProgress(i)
                    : (completed ? submit.requiredCount() : 0);
                list.add(new SubmitObjectiveEntry(
                    itemStack,
                    submit.requiredCount(),
                    current
                ));
            }
        }
        return list;
    }

    private static List<ItemStack> buildRewards(TaskDefinition definition) {
        List<ItemStack> stacks = new ArrayList<>();
        for (TaskRewardDefinition reward : definition.rewards()) {
            if (reward instanceof ItemRewardDefinition itemReward) {
                stacks.add(itemReward.stack().copy());
            }
        }
        return stacks;
    }

    private static ItemStack createObjectiveStack(
        SubmitItemObjectiveDefinition submit
    ) {
        if (submit.requiredPotion() != null) {
            ItemStack stack = PotionContents.createItemStack(
                submit.item(),
                submit.requiredPotion()
            );
            stack.setCount(submit.requiredCount());
            return stack;
        }
        ItemStack stack = new ItemStack(submit.item());
        if (submit.requiredNbt() != null) {
            stack.set(
                DataComponents.CUSTOM_DATA,
                CustomData.of(submit.requiredNbt().copy())
            );
        }
        return stack;
    }
}
