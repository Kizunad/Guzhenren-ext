package com.Kizunad.customNPCs.tasks;

import com.Kizunad.customNPCs.capabilities.tasks.PlayerTaskAttachment;
import com.Kizunad.customNPCs.tasks.data.PlayerTaskData;
import com.Kizunad.customNPCs.tasks.data.TaskProgress;
import com.Kizunad.customNPCs.tasks.data.TaskProgressState;
import com.Kizunad.customNPCs.tasks.objective.GuardEntityObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.objective.TaskObjectiveDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber
public class GuardTaskTicker {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        ServerPlayer player = (ServerPlayer) event.getEntity();
        // 每秒检查一次 (20 ticks)
        if (player.tickCount % 20 != 0) {
            return;
        }

        PlayerTaskData data = PlayerTaskAttachment.get(player);
        if (data == null) return;

        List<TaskProgress> activeTasks = data.getAllProgress();
        for (TaskProgress progress : activeTasks) {
            if (progress.getState() != TaskProgressState.ACCEPTED) {
                continue;
            }
            TaskDefinition def = TaskRegistry.getInstance().get(progress.getTaskId());
            if (def == null) continue;

            boolean changed = false;
            for (int i = 0; i < def.objectiveCount(); i++) {
                TaskObjectiveDefinition obj = def.objective(i);
                if (obj instanceof GuardEntityObjectiveDefinition guard) {
                    changed |= processGuardObjective(player, guard, progress, i);
                }
            }
            
            // 如果完成了所有目标
            if (progress.isCompleted(def)) {
                // 自动完成逻辑通常在提交时触发，但对于守卫任务，时间到了可以直接提示玩家去提交
                // 这里我们只更新进度，让玩家去点提交
            }
        }
    }

    private static boolean processGuardObjective(
        ServerPlayer player,
        GuardEntityObjectiveDefinition guard,
        TaskProgress progress,
        int index
    ) {
        CompoundTag tag = progress.getAdditionalData();
        String keyStart = "startTime_" + index;
        if (!tag.contains(keyStart)) return false;

        long startTime = tag.getLong(keyStart);
        long gameTime = player.serverLevel().getGameTime();
        long passedTicks = gameTime - startTime;
        int passedSeconds = (int) (passedTicks / 20);

        // 1. 检查目标是否存活
        List<UUID> targets = progress.getObjectiveTargets(index);
        if (targets.isEmpty()) return false; // 异常
        UUID targetId = targets.get(0);
        
        ServerLevel level = player.serverLevel();
        Entity targetEntity = level.getEntity(targetId);
        
        // 如果实体加载了但死了，或者找不到（且玩家就在附近，说明可能死了或消失了）
        // 这里简化处理：如果getEntity返回null，尝试通过UUID查找（可能在未加载的区块，暂且认为存活）
        // 但如果任务要求守卫，玩家应该在附近。
        // 严格模式：必须在附近。
        
        boolean isDead = false;
        if (targetEntity instanceof LivingEntity living) {
            if (living.isDeadOrDying()) {
                isDead = true;
            }
        } else if (targetEntity == null) {
            // 实体不在内存中。如果是玩家跑远了，任务应该失败吗？
            // 守卫任务通常要求玩家不离开。
            // 简单起见：如果玩家距离生成点太远，判定失败？
            // 暂时：如果getEntity为null，假设它还活着（避免区块卸载导致任务失败）
        }

        if (isDead) {
            player.displayClientMessage(Component.literal("§c守卫目标已死亡！任务失败！"), true);
            // 失败处理：重置进度或移除任务
            // data.removeTask(...) ? 目前没有直接移除接口
            progress.setState(TaskProgressState.COMPLETED); // 标记为完成但进度不够，导致无法提交？
            // 或者重置开始时间让玩家重新来过？
            tag.remove(keyStart); // 移除开始时间，重置状态
            progress.setObjectiveProgress(index, 0);
            progress.getObjectiveTargets(index).clear(); // 清除目标，需要重新接取? 
            // 这是一个复杂的问题，简单的做法是告诉玩家放弃任务重新接
            return true;
        }

        // 2. 更新时间进度
        if (passedSeconds >= guard.totalDurationSeconds()) {
            if (progress.getObjectiveProgress(index) < guard.totalDurationSeconds()) {
                progress.setObjectiveProgress(index, guard.totalDurationSeconds());
                player.displayClientMessage(Component.literal("§a守卫时间结束！目标存活！"), true);
                return true;
            }
            return false;
        }
        
        // 更新显示的进度
        progress.setObjectiveProgress(index, passedSeconds);

        // 3. 刷怪逻辑
        if (passedSeconds > guard.prepareTimeSeconds()) {
            String keyLastWave = "lastWave_" + index;
            long lastWave = tag.contains(keyLastWave) ? tag.getLong(keyLastWave) : startTime;
            
            if (gameTime - lastWave >= guard.waveIntervalSeconds() * 20L) {
                spawnWave(player.serverLevel(), guard, targetEntity != null ? (LivingEntity)targetEntity : player);
                tag.putLong(keyLastWave, gameTime);
                return true;
            }
        }

        return true; // 总是更新了进度
    }

    private static void spawnWave(ServerLevel level, GuardEntityObjectiveDefinition guard, LivingEntity target) {
        if (guard.attackers().isEmpty()) return;
        
        BlockPos center = target.blockPosition();
        
        for (GuardEntityObjectiveDefinition.AttackerEntry entry : guard.attackers()) {
            int count = entry.minCount() + level.random.nextInt(entry.maxCount() - entry.minCount() + 1);
            for (int i = 0; i < count; i++) {
                // 简单的圆形生成
                double angle = level.random.nextDouble() * Math.PI * 2;
                double dist = guard.spawnRadius();
                int x = center.getX() + (int)(Math.cos(angle) * dist);
                int z = center.getZ() + (int)(Math.sin(angle) * dist);
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                
                Entity mob = entry.entityType().create(level);
                if (mob instanceof Mob m) {
                    if (entry.nbt() != null) {
                        CompoundTag mobTag = entry.nbt().copy();
                        // 移除 UUID，让系统自动生成新的，避免冲突
                        mobTag.remove("UUID"); 
                        m.load(mobTag);
                    }
                    
                    m.moveTo(x + 0.5, y, z + 0.5, level.random.nextFloat() * 360f, 0);
                    m.setTarget(target); // 设置仇恨
                    level.addFreshEntity(m);
                }
            }
        }
    }
}
