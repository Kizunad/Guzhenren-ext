package com.Kizunad.customNPCs.ai.actions.goap;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.InteractBlockAction;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GOAP 方块交互动作 - 包装 InteractBlockAction 添加GOAP规划信息
 * <p>
 * 前置条件:
 * - at_block_<x>_<y>_<z>: true (在方块附近)
 * - block_exists: true (方块存在)
 * <p>
 * 效果:
 * - block_interacted: true (方块已交互)
 * - door_open: true (如果是门，门已打开)
 * <p>
 * 代价: 2.5 (需要导航，可能失败)
 */
public class GoapInteractBlockAction implements IGoapAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoapInteractBlockAction.class);

    private static final float DEFAULT_COST = 2.5f;
    private static final int DOOR_EFFECT_DURATION_TICKS = 100;
    private static final int INTERACT_MEMORY_DURATION_TICKS = 200;

    private final WorldState preconditions;
    private final WorldState effects;
    private final InteractBlockAction wrappedAction;
    private final float cost;
    private final BlockPos blockPos;
    private final boolean isDoor;

    /**
     * 创建GOAP方块交互动作
     * @param blockPos 方块位置
     * @param isDoor 是否是门
     */
    public GoapInteractBlockAction(BlockPos blockPos, boolean isDoor) {
        this(blockPos, null, isDoor, DEFAULT_COST);
    }

    /**
     * 创建GOAP方块交互动作（指定面）
     * @param blockPos 方块位置
     * @param face 交互面
     * @param isDoor 是否是门
     */
    public GoapInteractBlockAction(BlockPos blockPos, Direction face, boolean isDoor) {
        this(blockPos, face, isDoor, DEFAULT_COST);
    }

    /**
     * 创建GOAP方块交互动作（完整参数）
     * @param blockPos 方块位置
     * @param face 交互面
     * @param isDoor 是否是门
     * @param cost 动作代价
     */
    public GoapInteractBlockAction(
        BlockPos blockPos,
        Direction face,
        boolean isDoor,
        float cost
    ) {
        this.wrappedAction = face != null
            ? new InteractBlockAction(blockPos, face)
            : new InteractBlockAction(blockPos);
        this.cost = cost;
        this.blockPos = blockPos;
        this.isDoor = isDoor;

        // 前置条件：在方块附近且方块存在
        this.preconditions = new WorldState();
        this.preconditions.setState(
            WorldStateKeys.atBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
            true
        );
        this.preconditions.setState(WorldStateKeys.BLOCK_EXISTS, true);

        // 效果：方块已交互
        this.effects = new WorldState();
        this.effects.setState(WorldStateKeys.BLOCK_INTERACTED, true);
        
        // 如果是门，添加门已打开效果
        if (isDoor) {
            this.effects.setState(WorldStateKeys.DOOR_OPEN, true);
        }
    }

    @Override
    public WorldState getPreconditions() {
        return preconditions;
    }

    @Override
    public WorldState getEffects() {
        return effects;
    }

    @Override
    public float getCost() {
        return cost;
    }

    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        ActionStatus status = wrappedAction.tick(mind, entity);

        // 成功时更新Memory状态
        if (status == ActionStatus.SUCCESS) {
            mind.getMemory().rememberLongTerm(WorldStateKeys.BLOCK_INTERACTED, true);
            
            if (isDoor) {
                mind.getMemory().rememberShortTerm(
                    WorldStateKeys.DOOR_OPEN,
                    true,
                    DOOR_EFFECT_DURATION_TICKS
                );
            }
            
            mind.getMemory().rememberShortTerm(
                "last_interact_block",
                blockPos.toString(),
                INTERACT_MEMORY_DURATION_TICKS
            );

            LOGGER.info(
                "[GoapInteractBlockAction] 方块交互成功: {} | 是否门: {}",
                blockPos,
                isDoor
            );
        }

        return status;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        wrappedAction.start(mind, entity);
        LOGGER.debug("[GoapInteractBlockAction] 开始交互方块: {}", blockPos);
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        wrappedAction.stop(mind, entity);
        LOGGER.debug("[GoapInteractBlockAction] 停止交互方块");
    }

    @Override
    public boolean canInterrupt() {
        return wrappedAction.canInterrupt();
    }

    @Override
    public String getName() {
        return "goap_interact_block_" + blockPos.getX() + "_" + blockPos.getY() + "_" + blockPos.getZ();
    }
}
