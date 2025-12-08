package com.Kizunad.customNPCs.tasks;

import com.Kizunad.customNPCs.capabilities.tasks.PlayerTaskAttachment;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.network.OpenTaskBoardPayload;
import com.Kizunad.customNPCs.network.OpenTaskBoardPayload.ObjectiveEntry;
import com.Kizunad.customNPCs.network.OpenTaskBoardPayload.TaskEntry;
import com.Kizunad.customNPCs.tasks.data.PlayerTaskData;
import com.Kizunad.customNPCs.tasks.data.TaskProgress;
import com.Kizunad.customNPCs.tasks.data.TaskProgressState;
import com.Kizunad.customNPCs.tasks.objective.GuardEntityObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.objective.KillEntityObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.objective.SubmitItemObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.objective.TaskObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.objective.TaskObjectiveType;
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
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.Items;

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
            TaskProgress progress = data.getProgress(
                definition.id(),
                npc.getUUID()
            );
            boolean completed = progress == null &&
                data.hasCompleted(definition.id(), npc.getUUID());
            TaskProgressState state;
            if (progress != null) {
                state = progress.getState();
            } else if (completed) {
                state = TaskProgressState.COMPLETED;
            } else {
                state = TaskProgressState.AVAILABLE;
            }
            List<ObjectiveEntry> objectives = buildObjectives(
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

    private static List<ObjectiveEntry> buildObjectives(
        TaskDefinition definition,
        TaskProgress progress,
        boolean completed
    ) {
        List<ObjectiveEntry> list = new ArrayList<>();
        for (int i = 0; i < definition.objectiveCount(); i++) {
            TaskObjectiveDefinition objective = definition.objective(i);
            if (objective instanceof SubmitItemObjectiveDefinition submit) {
                ItemStack itemStack = createObjectiveStack(submit);
                int current = progress != null
                    ? progress.getObjectiveProgress(i)
                    : (completed ? submit.requiredCount() : 0);
                list.add(new ObjectiveEntry(
                    TaskObjectiveType.SUBMIT_ITEM,
                    itemStack,
                    itemStack.getHoverName(),
                    submit.requiredCount(),
                    current
                ));
            } else if (objective instanceof KillEntityObjectiveDefinition kill) {
                ItemStack icon = createKillIcon(kill);
                int current = progress != null
                    ? progress.getObjectiveProgress(i)
                    : (completed ? kill.requiredKills() : 0);
                Component name = kill.customName() != null
                    ? kill.customName()
                    : Component.translatable(
                        kill.entityType().getDescriptionId()
                    );
                list.add(new ObjectiveEntry(
                    TaskObjectiveType.KILL_ENTITY,
                    icon,
                    name,
                    kill.requiredKills(),
                    current
                ));
            } else if (objective instanceof GuardEntityObjectiveDefinition guard) {
                ItemStack icon = createGuardIcon(guard);
                int required = guard.totalDurationSeconds();
                int current = progress != null
                    ? progress.getObjectiveProgress(i)
                    : (completed ? required : 0);
                Component targetName;
                if (guard.entityToSpawn() != null) {
                    targetName = Component.translatable(
                        guard.entityToSpawn().getDescriptionId()
                    );
                } else if (
                    guard.targetType() ==
                        GuardEntityObjectiveDefinition.GuardTargetType.SPAWN
                ) {
                    targetName = Component.translatable(
                        "gui.customnpcs.task_board.objective.guard.target.unknown"
                    );
                } else {
                    targetName = Component.translatable(
                        "gui.customnpcs.task_board.objective.guard.target.self"
                    );
                }
                list.add(new ObjectiveEntry(
                    TaskObjectiveType.GUARD_ENTITY,
                    icon,
                    targetName,
                    required,
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

    private static ItemStack createKillIcon(KillEntityObjectiveDefinition kill) {
        SpawnEggItem egg = SpawnEggItem.byId(kill.entityType());
        if (egg != null) {
            return new ItemStack(egg);
        }
        ItemStack stack = new ItemStack(Items.NAME_TAG);
        if (kill.customName() != null) {
            stack.set(DataComponents.CUSTOM_NAME, kill.customName());
        }
        return stack;
    }

    private static ItemStack createGuardIcon(
        GuardEntityObjectiveDefinition guard
    ) {
        if (guard.entityToSpawn() != null) {
            SpawnEggItem egg = SpawnEggItem.byId(guard.entityToSpawn());
            if (egg != null) {
                return new ItemStack(egg);
            }
            ItemStack stack = new ItemStack(Items.NAME_TAG);
            stack.set(
                DataComponents.CUSTOM_NAME,
                Component.translatable(guard.entityToSpawn().getDescriptionId())
            );
            return stack;
        }
        return new ItemStack(Items.SHIELD);
    }
}
