package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.base.MoveToAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 避水目标：在水中时寻找附近可站立的陆地并移动上岸。
 */
public class AvoidInWaterGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "AvoidInWaterGoal: when in water, find nearby dry walkable land and move there; low priority preference.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float PRIORITY = 0.14F; // 略高于 idle，低于资源收集
    private static final double MOVE_SPEED = 1.1D;
    private static final double ACCEPTABLE_DISTANCE = 1.5D;
    private static final int SEARCH_RADIUS = 10;
    private static final int VERTICAL_RANGE = 4;
    private static final int REPATH_COOLDOWN_TICKS = 20;

    private MoveToAction moveAction;
    private BlockPos targetLand;
    private boolean finished;
    private int repathCooldown;

    @Override
    public String getName() {
        return "avoid_in_water";
    }

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        return isInWater(entity) ? PRIORITY : 0.0F;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        if (entity.level().isClientSide()) {
            return false;
        }
        if (mind.getMemory().hasMemory(WorldStateKeys.IN_DANGER)) {
            // 危险状态下由逃跑/防御类目标处理
            return false;
        }
        return isInWater(entity);
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        finished = false;
        repathCooldown = 0;
        targetLand = null;
        moveAction = null;
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        if (!isInWater(entity)) {
            finished = true;
            return;
        }

        if (repathCooldown > 0) {
            repathCooldown--;
        }

        if (needsNewPath(entity)) {
            findAndStartPath(mind, entity);
        }

        if (moveAction != null) {
            ActionStatus status = moveAction.tick(mind, entity);
            if (status == ActionStatus.SUCCESS) {
                finished = true;
            } else if (status == ActionStatus.FAILURE) {
                moveAction = null;
                targetLand = null;
                repathCooldown = 0;
            }
        }
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        if (moveAction != null) {
            moveAction.stop(mind, entity);
        }
        moveAction = null;
        targetLand = null;
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        return finished || !isInWater(entity);
    }

    private boolean needsNewPath(LivingEntity entity) {
        if (moveAction == null || targetLand == null) {
            return true;
        }
        if (repathCooldown > 0) {
            return false;
        }
        // 如果目标位置变得不可用，重新规划
        if (!(entity.level() instanceof ServerLevel level)) {
            return false;
        }
        return !isLandable(level, targetLand);
    }

    private void findAndStartPath(INpcMind mind, LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        targetLand = findNearestLand(level, entity.blockPosition());
        if (targetLand != null) {
            moveAction =
                new MoveToAction(
                    Vec3.atCenterOf(targetLand),
                    MOVE_SPEED,
                    ACCEPTABLE_DISTANCE
                );
            moveAction.start(mind, entity);
            repathCooldown = REPATH_COOLDOWN_TICKS;
            MindLog.decision(
                MindLogLevel.INFO,
                "AvoidInWaterGoal 目标陆地 ({}, {}, {})",
                targetLand.getX(),
                targetLand.getY(),
                targetLand.getZ()
            );
        } else {
            // 未找到陆地，稍后再试，避免每 tick 重刷
            repathCooldown = REPATH_COOLDOWN_TICKS;
            MindLog.decision(
                MindLogLevel.WARN,
                "AvoidInWaterGoal 未找到附近陆地"
            );
        }
    }

    private BlockPos findNearestLand(ServerLevel level, BlockPos origin) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dy = -VERTICAL_RANGE; dy <= VERTICAL_RANGE; dy++) {
                for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (!isLandable(level, pos)) {
                        continue;
                    }
                    double dist = origin.distSqr(pos);
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = pos.immutable();
                    }
                }
            }
        }
        return best;
    }

    /**
     * 判断位置是否可站立：脚下有支撑，当前位置/头顶无流体且可通过。
     */
    private boolean isLandable(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            return false;
        }
        BlockState feet = level.getBlockState(pos);
        BlockState above = level.getBlockState(pos.above());
        if (!feet.getFluidState().isEmpty() || !above.getFluidState().isEmpty()) {
            return false;
        }
        if (!feet.getCollisionShape(level, pos).isEmpty()) {
            return false;
        }
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        return below.isFaceSturdy(level, belowPos, Direction.UP);
    }

    private boolean isInWater(LivingEntity entity) {
        return entity.isInWaterOrBubble();
    }
}
