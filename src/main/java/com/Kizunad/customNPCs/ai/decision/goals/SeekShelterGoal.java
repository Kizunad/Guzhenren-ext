package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.base.MoveToAction;
import com.Kizunad.customNPCs.ai.actions.common.InteractBlockAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 寻找掩体Goal - 在危险环境中寻找并移动到安全的掩体
 * <p>
 * 使用标准动作: {@link MoveToAction} + {@link InteractBlockAction}
 * <p>
 * 优先级: 中
 * 触发条件: 处于危险环境（夜晚、暴露）
 */
public class SeekShelterGoal implements IGoal {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeekShelterGoal.class);

    private static final int SHELTER_SEARCH_RADIUS = 20; // 搜索半径
    private static final int SHELTER_MEMORY_DURATION = 200; // 10秒
    private static final double ARRIVAL_DISTANCE = 2.0;
    private static final float PRIORITY_DANGER = 0.5f;
    private static final int VERTICAL_SEARCH_RADIUS = 5;

    private MoveToAction moveAction = null;
    private InteractBlockAction interactAction = null;
    private BlockPos shelterPos = null;
    private boolean isDoorShelter = false;

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        if (isInDangerousEnvironment(entity)) {
            // 中等优先级
            return PRIORITY_DANGER;
        }
        return 0.0f;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return isInDangerousEnvironment(entity);
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        mind.getMemory().rememberShortTerm(
            "seeking_shelter",
            true,
            SHELTER_MEMORY_DURATION
        );

        LOGGER.info(
            "[SeekShelterGoal] {} 开始寻找掩体",
            entity.getName().getString()
        );
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 如果还没有找到掩体，搜索一个
        if (shelterPos == null) {
            shelterPos = findNearestShelter(entity);
            if (shelterPos != null) {
                LOGGER.debug(
                    "[SeekShelterGoal] 找到掩体位置: ({}, {}, {})",
                    shelterPos.getX(),
                    shelterPos.getY(),
                    shelterPos.getZ()
                );
            } else {
                LOGGER.warn("[SeekShelterGoal] 未找到合适的掩体");
                return;
            }
        }

        // 如果还没有移动动作，创建一个
        if (moveAction == null && shelterPos != null) {
            Vec3 targetPos = Vec3.atCenterOf(shelterPos);
            moveAction = new MoveToAction(targetPos, 1.0);
            moveAction.start(mind, entity);
        }

        // 执行移动
        if (moveAction != null) {
            ActionStatus status = moveAction.tick(mind, entity);
            if (status == ActionStatus.SUCCESS) {
                LOGGER.info("[SeekShelterGoal] 已到达掩体");
                moveAction = null;
                
                // 如果是门，尝试打开
                if (isDoorShelter && interactAction == null) {
                    interactAction = new InteractBlockAction(shelterPos);
                    interactAction.start(mind, entity);
                }
            } else if (status == ActionStatus.FAILURE) {
                LOGGER.warn("[SeekShelterGoal] 移动失败，重新搜索");
                moveAction = null;
                shelterPos = null;
            }
        }

        // 执行门交互
        if (interactAction != null) {
            ActionStatus status = interactAction.tick(mind, entity);
            if (status != ActionStatus.RUNNING) {
                LOGGER.debug("[SeekShelterGoal] 门交互完成");
                interactAction = null;
            }
        }
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        mind.getMemory().forget("seeking_shelter");
        
        if (moveAction != null) {
            moveAction.stop(mind, entity);
            moveAction = null;
        }
        if (interactAction != null) {
            interactAction.stop(mind, entity);
            interactAction = null;
        }
        shelterPos = null;
        isDoorShelter = false;

        LOGGER.info("[SeekShelterGoal] {} 停止寻找掩体", entity.getName().getString());
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        // 不再处于危险环境或已到达掩体
        boolean safe = !isInDangerousEnvironment(entity);
        boolean arrived = shelterPos != null 
            && entity.position().distanceTo(Vec3.atCenterOf(shelterPos)) < ARRIVAL_DISTANCE;
        
        return safe || arrived;
    }

    @Override
    public String getName() {
        return "seek_shelter";
    }

    /**
     * 检查是否处于危险环境
     */
    private boolean isInDangerousEnvironment(LivingEntity entity) {
        Level level = entity.level();
        
        // 夜晚
        boolean isNight = level.isNight();
        
        // 暴露在外（天空可见）
        boolean isExposed = level.canSeeSky(entity.blockPosition());
        
        // 下雨
        boolean isRaining = level.isRaining();
        
        return (isNight || isRaining) && isExposed;
    }

    /**
     * 寻找最近的掩体（建筑物或洞穴）
     */
    private BlockPos findNearestShelter(LivingEntity entity) {
        Level level = entity.level();
        BlockPos entityPos = entity.blockPosition();
        BlockPos bestShelter = null;
        double bestDistance = Double.MAX_VALUE;

        // 搜索附近的方块
        for (int dx = -SHELTER_SEARCH_RADIUS; dx <= SHELTER_SEARCH_RADIUS; dx++) {
            for (
                int dy = -VERTICAL_SEARCH_RADIUS;
                dy <= VERTICAL_SEARCH_RADIUS;
                dy++
            ) {
                for (int dz = -SHELTER_SEARCH_RADIUS; dz <= SHELTER_SEARCH_RADIUS; dz++) {
                    BlockPos pos = entityPos.offset(dx, dy, dz);
                    
                    // 检查是否是有遮蔽的位置
                    if (isShelterLocation(level, pos)) {
                        double distance = entityPos.distSqr(pos);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            bestShelter = pos;
                            
                            // 检查是否是门
                            BlockState state = level.getBlockState(pos);
                            isDoorShelter = state.getBlock() instanceof DoorBlock;
                        }
                    }
                }
            }
        }

        return bestShelter;
    }

    /**
     * 检查位置是否可以作为掩体
     */
    private boolean isShelterLocation(Level level, BlockPos pos) {
        // 位置上方有方块（有遮蔽）
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        boolean hasCover = !aboveState.isAir() && aboveState.isSolidRender(level, above);
        
        // 位置本身是空气或门
        BlockState currentState = level.getBlockState(pos);
        boolean canStand = currentState.isAir() || currentState.getBlock() instanceof DoorBlock;
        
        return hasCover && canStand;
    }
}
