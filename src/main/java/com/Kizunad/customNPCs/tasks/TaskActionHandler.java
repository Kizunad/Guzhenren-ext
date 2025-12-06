package com.Kizunad.customNPCs.tasks;

import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.customNPCs.capabilities.tasks.PlayerTaskAttachment;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.network.OpenTaskBoardPayload;
import com.Kizunad.customNPCs.tasks.data.PlayerTaskData;
import com.Kizunad.customNPCs.tasks.data.TaskProgress;
import com.Kizunad.customNPCs.tasks.data.TaskProgressState;
import com.Kizunad.customNPCs.tasks.objective.SubmitItemObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.objective.TaskObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.reward.TaskRewardDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务交互处理：接受/提交。
 */
public final class TaskActionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        TaskActionHandler.class
    );
    private static final double MAX_DISTANCE_SQR = 20.0D * 20.0D;

    private TaskActionHandler() {}

    public static void handleAccept(
        ServerPlayer player,
        int npcEntityId,
        ResourceLocation taskId
    ) {
        CustomNpcEntity npc = validateNpc(player, npcEntityId);
        if (npc == null) {
            return;
        }
        TaskDefinition definition = TaskRegistry.getInstance().get(taskId);
        if (definition == null) {
            LOGGER.warn(
                "玩家 {} 请求未知任务 {}",
                player.getGameProfile().getName(),
                taskId
            );
            return;
        }
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (
            mind == null || !mind.getQuestState().getTaskIds().contains(taskId)
        ) {
            player.displayClientMessage(
                Component.literal("该 NPC 不提供此任务"),
                false
            );
            return;
        }
        PlayerTaskData data = PlayerTaskAttachment.get(player);
        boolean accepted = data.accept(definition, npc.getUUID());
        if (!accepted) {
            player.displayClientMessage(
                Component.literal("无法接受任务，可能已在进行中或已完成"),
                false
            );
            return;
        }
        player.displayClientMessage(
            Component.literal("接受任务: " + definition.title()),
            false
        );
        syncBoard(player, npc);
    }

    public static void handleSubmit(
        ServerPlayer player,
        int npcEntityId,
        ResourceLocation taskId
    ) {
        CustomNpcEntity npc = validateNpc(player, npcEntityId);
        if (npc == null) {
            return;
        }
        TaskDefinition definition = TaskRegistry.getInstance().get(taskId);
        if (definition == null) {
            return;
        }
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            player.displayClientMessage(
                Component.literal("NPC 状态异常，无法提交"),
                false
            );
            return;
        }
        PlayerTaskData data = PlayerTaskAttachment.get(player);
        TaskProgress progress = data.getProgress(taskId, npc.getUUID());
        if (progress == null) {
            player.displayClientMessage(
                Component.literal("尚未接受该任务"),
                false
            );
            return;
        }
        if (progress.getState() == TaskProgressState.COMPLETED) {
            player.displayClientMessage(
                Component.literal("该任务已完成"),
                false
            );
            return;
        }
        boolean delivered = false;
        for (int i = 0; i < definition.objectiveCount(); i++) {
            TaskObjectiveDefinition objective = definition.objective(i);
            if (objective instanceof SubmitItemObjectiveDefinition submit) {
                int current = progress.getObjectiveProgress(i);
                int needed = submit.requiredCount() - current;
                if (needed <= 0) {
                    continue;
                }
                int deliveredAmount = transferItemsToNpc(
                    player,
                    npc,
                    mind.getInventory(),
                    submit,
                    needed
                );
                if (deliveredAmount > 0) {
                    progress.incrementObjective(
                        i,
                        deliveredAmount,
                        submit.requiredCount()
                    );
                    delivered = true;
                }
            }
        }
        if (!delivered) {
            player.displayClientMessage(
                Component.literal("没有可交付的物品"),
                false
            );
            return;
        }
        player.displayClientMessage(Component.literal("已提交任务物资"), true);
        if (progress.isCompleted(definition)) {
            progress.setState(TaskProgressState.COMPLETED);
            grantRewards(player, npc, definition);
            data.completeTask(definition.id(), npc.getUUID());
            player.displayClientMessage(
                Component.literal("任务完成: " + definition.title()),
                false
            );
        }
        syncBoard(player, npc);
    }

    public static void handleRefresh(ServerPlayer player, int npcEntityId) {
        CustomNpcEntity npc = validateNpc(player, npcEntityId);
        if (npc == null) {
            return;
        }
        syncBoard(player, npc);
    }

    private static CustomNpcEntity validateNpc(
        ServerPlayer player,
        int npcEntityId
    ) {
        var entity = player.serverLevel().getEntity(npcEntityId);
        if (!(entity instanceof CustomNpcEntity npc)) {
            return null;
        }
        if (!npc.isAlive() || npc.distanceToSqr(player) > MAX_DISTANCE_SQR) {
            return null;
        }
        return npc;
    }

    /**
     * 将玩家提交的物品转移到 NPC 背包中，只计入真实放入的数量。
     * 若 NPC 背包已满，未放入的部分会返还给玩家（返还失败则掉落）。
     */
    private static int transferItemsToNpc(
        ServerPlayer player,
        CustomNpcEntity npc,
        NpcInventory npcInventory,
        SubmitItemObjectiveDefinition objective,
        int needed
    ) {
        Inventory playerInv = player.getInventory();
        int remainingNeed = needed;
        int inserted = 0;
        for (
            int slot = 0;
            slot < playerInv.getContainerSize() && remainingNeed > 0;
            slot++
        ) {
            ItemStack stack = playerInv.getItem(slot);
            if (!objective.matches(stack)) {
                continue;
            }
            int remove = Math.min(remainingNeed, stack.getCount());
            ItemStack extracted = stack.copyWithCount(remove);
            stack.shrink(remove);
            if (stack.isEmpty()) {
                playerInv.setItem(slot, ItemStack.EMPTY);
            }
            playerInv.setChanged();

            int originalCount = extracted.getCount();
            ItemStack leftover = npcInventory.addItem(extracted);
            int placed =
                originalCount - (leftover.isEmpty() ? 0 : leftover.getCount());
            inserted += placed;
            remainingNeed -= placed;

            if (!leftover.isEmpty()) {
                // 背包已满，尽量返还给玩家，避免物品丢失
                //boolean returned = playerInv.add(leftover);
                //if (!returned) {
                //    npc.spawnAtLocation(leftover, 0.0F);
                //}
                //  这里不做任何操作
            }
        }
        return inserted;
    }

    private static void grantRewards(
        ServerPlayer player,
        CustomNpcEntity npc,
        TaskDefinition definition
    ) {
        for (TaskRewardDefinition reward : definition.rewards()) {
            reward.grant(player, npc);
        }
    }

    private static void syncBoard(ServerPlayer player, CustomNpcEntity npc) {
        OpenTaskBoardPayload payload = TaskBoardSyncService.buildPayload(
            npc,
            player
        );
        PacketDistributor.sendToPlayer(player, payload);
    }
}
