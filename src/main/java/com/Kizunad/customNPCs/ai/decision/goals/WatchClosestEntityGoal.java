package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.actions.base.LookAtAction;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 注视最近实体目标
 * <p>
 * 当感知到周围有实体时，NPC 会注视最近的实体。
 * 这是一个反应式目标，优先级取决于是否有可见实体。
 */
public class WatchClosestEntityGoal implements IGoal {

    private static final float PRIORITY_ACTIVE = 0.6f; // 当有目标时的优先级（高于 Idle，低于 Survival）
    private static final float PRIORITY_INACTIVE = 0.0f; // 无目标时的优先级
    private static final float LOOK_MAX_YAW = 30.0F; // 最大偏航角
    private static final float LOOK_MAX_PITCH = 30.0F; // 最大俯仰角
    private static final int LOOK_ACTION_DURATION = 20;

    private String targetUUID;
    private boolean isRunning;

    @Override
    public String getName() {
        return "watch_closest_entity";
    }

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        // 检查记忆中是否有最近实体的记录
        if (mind.getMemory().hasMemory("nearest_entity")) {
            return PRIORITY_ACTIVE;
        }
        return PRIORITY_INACTIVE;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return mind.getMemory().hasMemory("nearest_entity");
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        isRunning = true;
        // 获取目标 UUID
        Object uuidObj = mind.getMemory().getMemory("nearest_entity");
        if (uuidObj instanceof String) {
            targetUUID = (String) uuidObj;
        }
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        if (targetUUID == null) {
            return;
        }

        // 尝试找到目标实体
        Entity target = null;
        if (entity.level() instanceof ServerLevel serverLevel) {
            target = serverLevel.getEntity(UUID.fromString(targetUUID));
        }

        if (
            target != null &&
            entity instanceof net.minecraft.world.entity.Mob mob
        ) {
            // 使用 LookAtAction 注视目标
            // 注意：WatchClosestEntityGoal 是一个持续性目标，它应该不断提交 LookAtAction 或者
            // 如果 LookAtAction 是瞬时的，它可以在 tick 中提交。
            // 但 LookAtAction 通常是持续一段时间的。
            // 这里我们检查执行器是否空闲，如果空闲则提交一个新的 LookAtAction
            if (mind.getActionExecutor().isIdle()) {
                mind
                    .getActionExecutor()
                    .addAction(new LookAtAction(target, LOOK_ACTION_DURATION));
            }
        }
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        isRunning = false;
        targetUUID = null;
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        // 如果记忆中不再有最近实体，或者目标丢失，则结束
        return !mind.getMemory().hasMemory("nearest_entity");
    }
}
