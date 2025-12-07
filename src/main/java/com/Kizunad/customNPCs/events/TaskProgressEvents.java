package com.Kizunad.customNPCs.events;

import com.Kizunad.customNPCs.capabilities.tasks.PlayerTaskAttachment;
import com.Kizunad.customNPCs.tasks.TaskDefinition;
import com.Kizunad.customNPCs.tasks.TaskRegistry;
import com.Kizunad.customNPCs.tasks.data.PlayerTaskData;
import com.Kizunad.customNPCs.tasks.data.TaskProgress;
import com.Kizunad.customNPCs.tasks.data.TaskProgressState;
import com.Kizunad.customNPCs.tasks.objective.KillEntityObjectiveDefinition;
import com.Kizunad.customNPCs.tasks.objective.TaskObjectiveDefinition;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务进度事件监听：击杀目标计数。
 */
public class TaskProgressEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskProgressEvents.class);

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }

        PlayerTaskData data = PlayerTaskAttachment.get(player);
        if (data.getQuestGivers().isEmpty()) {
            return;
        }
        TaskRegistry registry = TaskRegistry.getInstance();
        boolean progressed = false;

        for (UUID npcId : data.getQuestGivers()) {
            for (TaskProgress progress : data.getActiveTasks(npcId)) {
                if (progress.getState() != TaskProgressState.ACCEPTED) {
                    continue;
                }
                TaskDefinition definition = registry.get(progress.getTaskId());
                if (definition == null) {
                    continue;
                }
                for (int i = 0; i < definition.objectiveCount(); i++) {
                    TaskObjectiveDefinition objective = definition.objective(i);
                    if (!(objective instanceof KillEntityObjectiveDefinition kill)) {
                        continue;
                    }
                    if (!matchesTarget(victim, kill, progress, i)) {
                        continue;
                    }
                    progress.incrementObjective(
                        i,
                        1,
                        kill.requiredKills()
                    );
                    // 防止重复统计同一个实体
                    progress.getObjectiveTargets(i).remove(victim.getUUID());
                    progressed = true;
                    int current = progress.getObjectiveProgress(i);
                    player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                            "任务进度: " + definition.title() + " " + current + "/" + kill.requiredKills()
                        ),
                        true
                    );
                }
            }
        }

        if (!progressed) {
            return;
        }

        // 若玩家正在打开任务板，则刷新客户端显示
        // 这里不强制推送完整面板，等待玩家主动刷新即可；记录日志以便排查
        LOGGER.debug(
            "玩家 {} 击杀 {} 触发任务进度更新",
            player.getGameProfile().getName(),
            victim.getType()
        );
    }

    private boolean matchesTarget(
        LivingEntity victim,
        KillEntityObjectiveDefinition objective,
        TaskProgress progress,
        int index
    ) {
        // 优先匹配已生成的目标 UUID，确保与任务生成的实体绑定
        if (!progress.getObjectiveTargets(index).isEmpty()) {
            return progress.getObjectiveTargets(index).contains(victim.getUUID());
        }
        // 退化匹配：若无绑定信息，按实体类型匹配
        return victim.getType() == objective.entityType();
    }
}
