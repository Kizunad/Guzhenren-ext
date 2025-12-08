package com.Kizunad.customNPCs.tasks;

import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.customNPCs.capabilities.tasks.PlayerTaskAttachment;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.network.OpenTaskBoardPayload;
import com.Kizunad.customNPCs.tasks.data.PlayerTaskData;
import com.Kizunad.customNPCs.tasks.data.TaskProgress;
import com.Kizunad.customNPCs.tasks.data.TaskProgressState;
import com.Kizunad.customNPCs.tasks.objective.GuardEntityObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.objective.KillEntityObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.objective.SubmitItemObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.objective.TaskObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.reward.TaskRewardDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

/**
 * 任务交互处理：接受/提交。
 */
public final class TaskActionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        TaskActionHandler.class
    );
    private static final double MAX_DISTANCE_SQR = 20.0D * 20.0D;
    private static final double TARGET_SPAWN_YAW_RANGE = 360.0D;
    private static final double BLOCK_CENTER_OFFSET = 0.5D;
    private static final long TICKS_PER_SECOND = 20L;
    private static final double GUARD_TARGET_SEARCH_RADIUS = 5.0D;

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
        TaskProgress progress = data.getProgress(taskId, npc.getUUID());
        if (progress != null) {
            initializeKillObjectives(player, npc, definition, progress);
            initializeGuardObjectives(player, npc, definition, progress);
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
        if (!delivered && !progress.isCompleted(definition)) {
            player.displayClientMessage(
                Component.literal("没有可交付的物品"),
                false
            );
            return;
        }
        if (delivered) {
            player.displayClientMessage(Component.literal("已提交任务物资"), true);
        }
        completeIfReady(definition, progress, data, player, npc);
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

    static void grantRewards(
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

    private static void initializeKillObjectives(
        ServerPlayer player,
        CustomNpcEntity npc,
        TaskDefinition definition,
        TaskProgress progress
    ) {
        for (int i = 0; i < definition.objectiveCount(); i++) {
            TaskObjectiveDefinition objective = definition.objective(i);
            if (!(objective instanceof KillEntityObjectiveDefinition kill)) {
                continue;
            }
            // 若已有目标列表，说明该任务是从存档恢复，无需重复生成
            if (!progress.getObjectiveTargets(i).isEmpty()) {
                continue;
            }
            for (int count = 0; count < kill.requiredKills(); count++) {
                var targetId = spawnKillTarget(player, npc, kill);
                if (targetId != null) {
                    progress.getObjectiveTargets(i).add(targetId);
                }
            }
        }
    }

    private static void initializeGuardObjectives(
        ServerPlayer player,
        CustomNpcEntity npc,
        TaskDefinition definition,
        TaskProgress progress
    ) {
        long gameTime = player.serverLevel().getGameTime();
        for (int i = 0; i < definition.objectiveCount(); i++) {
            TaskObjectiveDefinition objective = definition.objective(i);
            if (!(objective instanceof GuardEntityObjectiveDefinition guard)) {
                continue;
            }
            if (!progress.getObjectiveTargets(i).isEmpty()) {
                continue;
            }
            
            java.util.UUID targetUuid = null;
            if (guard.targetType() == GuardEntityObjectiveDefinition.GuardTargetType.SELF) {
                targetUuid = npc.getUUID();
                // 让 NPC 进入防御状态? 暂时先不改 AI，只是记录
                npc.setPersistenceRequired(); 
            } else {
                targetUuid = spawnGuardTarget(player, npc, guard);
            }

            if (targetUuid != null) {
                progress.getObjectiveTargets(i).add(targetUuid);
                net.minecraft.nbt.CompoundTag tag = progress.getAdditionalData();
                tag.putLong("startTime_" + i, gameTime);
                tag.putLong(
                    "lastWave_" + i,
                    gameTime + guard.prepareTimeSeconds() * TICKS_PER_SECOND
                ); // 第一波在准备时间结束后
                progress.setAdditionalData(tag);
                
                player.displayClientMessage(
                    Component.literal("守卫任务开始！准备时间: " + guard.prepareTimeSeconds() + "秒"),
                    true
                );
            }
        }
    }

    @Nullable
    private static java.util.UUID spawnGuardTarget(
        ServerPlayer player,
        CustomNpcEntity npc,
        GuardEntityObjectiveDefinition objective
    ) {
        ServerLevel level = player.serverLevel();
        RandomSource random = level.getRandom();
        // 守卫目标默认生成在玩家附近，或者 NPC 附近
        BlockPos origin = npc.blockPosition();
        BlockPos targetPos = pickSurfacePosition(
            level,
            origin,
            GUARD_TARGET_SEARCH_RADIUS,
            random
        );
        
        if (targetPos == null) {
            targetPos = origin; // 找不到就生成在脚下
        }

        if (objective.entityToSpawn() == null) {
            return null;
        }

        var created = objective.entityToSpawn().create(level);
        if (!(created instanceof LivingEntity entity)) {
            return null;
        }
        
        entity.moveTo(
            targetPos.getX() + BLOCK_CENTER_OFFSET,
            targetPos.getY(),
            targetPos.getZ() + BLOCK_CENTER_OFFSET,
            0,
            0
        );
        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
            // 也许应该设置为不移动?
            // mob.setNoAi(true); // 太死板了
        }
        
        level.addFreshEntity(entity);
        return entity.getUUID();
    }

    @Nullable
    private static java.util.UUID spawnKillTarget(
        ServerPlayer player,
        CustomNpcEntity npc,
        KillEntityObjectiveDefinition objective
    ) {
        ServerLevel level = player.serverLevel();
        RandomSource random = level.getRandom();
        BlockPos targetPos;
        if (objective.fixedSpawnPos() != null) {
            if (objective.snapToSurface()) {
                BlockPos p = objective.fixedSpawnPos();
                int y = level.getHeight(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    p.getX(),
                    p.getZ()
                );
                targetPos = new BlockPos(p.getX(), y, p.getZ());
            } else {
                targetPos = objective.fixedSpawnPos();
            }
        } else {
            BlockPos origin = npc.blockPosition();
            targetPos = pickSurfacePosition(
                level,
                origin,
                objective.spawnRadius(),
                random
            );
        }
        if (targetPos == null) {
            LOGGER.warn(
                "未找到合适的生成位置以创建击杀目标 {}",
                objective.typeId()
            );
            return null;
        }

        var created = objective.entityType().create(level);
        if (!(created instanceof LivingEntity entity)) {
            LOGGER.warn(
                "击杀目标实体类型 {} 创建结果 {} 非 LivingEntity，生成失败",
                objective.typeId(),
                created
            );
            return null;
        }
        double yaw = random.nextDouble() * TARGET_SPAWN_YAW_RANGE;
        entity.moveTo(
            targetPos.getX() + BLOCK_CENTER_OFFSET,
            targetPos.getY(),
            targetPos.getZ() + BLOCK_CENTER_OFFSET,
            (float) yaw,
            0.0F
        );
        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
        if (objective.customName() != null) {
            entity.setCustomName(objective.customName());
            entity.setCustomNameVisible(true);
        }
        objective.applyAttributes(entity);

        if (!level.noCollision(entity)) {
            LOGGER.warn(
                "击杀目标 {} 在位置 {} 与方块碰撞，生成失败",
                objective.typeId(),
                targetPos
            );
            return null;
        }
        boolean added = level.addFreshEntity(entity);
        if (!added) {
            LOGGER.warn(
                "击杀目标 {} 在 {} 生成失败 (addFreshEntity 返回 false)",
                objective.typeId(),
                targetPos
            );
            return null;
        }
        player.displayClientMessage(
            Component.literal(
                "已生成击杀目标: " + entity.getName().getString()
            ),
            false
        );
        return entity.getUUID();
    }

    @Nullable
    private static BlockPos pickSurfacePosition(
        ServerLevel level,
        BlockPos origin,
        double radius,
        RandomSource random
    ) {
        final double minDistance = 8.0D;
        final int attempts = 12;
        double effectiveRadius = Math.max(radius, minDistance);
        for (int i = 0; i < attempts; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance =
                minDistance + (effectiveRadius - minDistance) * random.nextDouble();
            int x = origin.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = origin.getZ() + (int) Math.round(Math.sin(angle) * distance);
            if (!level.hasChunkAt(new BlockPos(x, origin.getY(), z))) {
                continue;
            }
            int y = level.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                x,
                z
            );
            if (y < level.getMinBuildHeight() || y > level.getMaxBuildHeight()) {
                continue;
            }
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos below = pos.below();
            BlockState ground = level.getBlockState(below);
            if (!ground.isFaceSturdy(level, below, Direction.UP)) {
                continue;
            }
            if (!level.getBlockState(pos).isAir()) {
                continue;
            }
            return pos;
        }
        return null;
    }

    private static void completeIfReady(
        TaskDefinition definition,
        TaskProgress progress,
        PlayerTaskData data,
        ServerPlayer player,
        CustomNpcEntity npc
    ) {
        if (!progress.isCompleted(definition)) {
            return;
        }
        if (progress.getState() == TaskProgressState.COMPLETED) {
            return;
        }
        progress.setState(TaskProgressState.COMPLETED);
        grantRewards(player, npc, definition);
        data.completeTask(definition.id(), npc.getUUID());
        player.displayClientMessage(
            Component.literal("任务完成: " + definition.title()),
            false
        );
    }
}
