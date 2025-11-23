package com.Kizunad.customNPCs_test.goap;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

/**
 * 测试用 GOAP 动作 - 砍木头
 * <p>
 * 前置条件: 无
 * 效果: has_wood = true
 * 代价: 2.0
 */
public class ChopWoodGoapAction implements IGoapAction {
    
    private final WorldState preconditions;
    private final WorldState effects;
    
    public ChopWoodGoapAction() {
        // 前置条件：无
        this.preconditions = new WorldState();
        
        // 效果：获得木头
        this.effects = new WorldState();
        this.effects.setState("has_wood", true);
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
        return 2.0f; // 砍木头比获取苹果慢
    }
    
    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        // 测试用动作：立即成功并在记忆中设置状态
        mind.getMemory().rememberLongTerm("has_wood", true);
        return ActionStatus.SUCCESS;
    }

    
    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        System.out.println("[ChopWoodGoapAction] 开始砍木头");
    }
    
    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        System.out.println("[ChopWoodGoapAction] 停止");
    }
    
    @Override
    public boolean canInterrupt() {
        return true;
    }
    
    @Override
    public String getName() {
        return "chop_wood";
    }
}
