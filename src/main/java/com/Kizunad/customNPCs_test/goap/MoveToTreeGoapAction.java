package com.Kizunad.customNPCs_test.goap;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;

/**
 * 移动到树木位置的 GOAP 动作
 * <p>
 * 前置条件: 无
 * 效果: at_tree_location = true
 * 代价: 2.0
 */
public class MoveToTreeGoapAction implements IGoapAction {
    
    private final WorldState preconditions;
    private final WorldState effects;
    private final BlockPos treePos;
    
    public MoveToTreeGoapAction(BlockPos treePos) {
        this.treePos = treePos;
        
        // 前置条件：无
        this.preconditions = new WorldState();
        
        // 效果：到达树木位置
        this.effects = new WorldState();
        this.effects.setState("at_tree_location", true);
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
        return 2.0f;
    }
    
    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        // 测试用：检查是否接近树木位置
        double distance = entity.position().distanceTo(
            treePos.getCenter()
        );
        
        if (distance < 3.0) {
            // 到达位置，设置记忆
            mind.getMemory().rememberLongTerm("at_tree_location", true);
            System.out.println("[MoveToTreeGoapAction] 已到达树木位置");
            return ActionStatus.SUCCESS;
        }
        
        // 测试环境中直接传送到目标位置
        entity.teleportTo(
            treePos.getX() + 0.5,
            treePos.getY(),
            treePos.getZ() + 0.5
        );
        
        return ActionStatus.RUNNING;
    }
    
    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        System.out.println("[MoveToTreeGoapAction] 开始移动到树木位置: " + treePos);
    }
    
    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        System.out.println("[MoveToTreeGoapAction] 停止");
    }
    
    @Override
    public boolean canInterrupt() {
        return true;
    }
    
    @Override
    public String getName() {
        return "move_to_tree";
    }
}
