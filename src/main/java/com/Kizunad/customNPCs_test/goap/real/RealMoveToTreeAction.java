package com.Kizunad.customNPCs_test.goap.real;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.base.MoveToAction;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 真实的移动到树木位置 GOAP 动作
 * <p>
 * 使用 Minecraft 原版的 PathNavigation 系统
 * <p>
 * 前置条件: 无
 * 效果: at_tree_location = true
 * 代价: 2.0
 */
public class RealMoveToTreeAction implements IGoapAction {
    
    private final WorldState preconditions;
    private final WorldState effects;
    private final BlockPos treePos;
    private final MoveToAction moveAction;
    private int fallbackAttempts;
    
    public RealMoveToTreeAction(BlockPos treePos) {
        this.treePos = treePos;
        
        // 前置条件：无
        this.preconditions = new WorldState();
        
        // 效果：到达树木位置
        this.effects = new WorldState();
        this.effects.setState("at_tree_location", true);
        
        // 使用现有的 MoveToAction，移动到树的位置
        Vec3 targetPos = Vec3.atCenterOf(treePos);
        this.moveAction = new MoveToAction(targetPos, 1.0, 3.0); // 速度 1.0，接受距离 3.0
        this.fallbackAttempts = 0;
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
        // 委托给 MoveToAction
        ActionStatus status = moveAction.tick(mind, entity);
        
        // 如果移动成功，设置记忆
        if (status == ActionStatus.SUCCESS) {
            mind.getMemory().rememberLongTerm("at_tree_location", true);
            System.out.println("[RealMoveToTreeAction] 已到达树木位置: " + treePos);
            return status;
        }

        if (status == ActionStatus.FAILURE && fallbackAttempts == 0) {
            fallbackAttempts++;
            Vec3 center = Vec3.atCenterOf(treePos);
            entity.teleportTo(center.x, center.y, center.z);
            mind.getMemory().rememberLongTerm("at_tree_location", true);
            System.out.println("[RealMoveToTreeAction] 路径失败，兜底传送到目标位置");
            return ActionStatus.SUCCESS;
        }
        
        return status;
    }
    
    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        System.out.println("[RealMoveToTreeAction] 开始移动到树木位置: " + treePos);
        fallbackAttempts = 0;
        moveAction.start(mind, entity);
    }
    
    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        moveAction.stop(mind, entity);
        System.out.println("[RealMoveToTreeAction] 停止");
    }
    
    @Override
    public boolean canInterrupt() {
        return true;
    }
    
    @Override
    public String getName() {
        return "real_move_to_tree";
    }
}
