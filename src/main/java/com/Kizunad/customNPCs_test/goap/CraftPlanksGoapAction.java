package com.Kizunad.customNPCs_test.goap;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

/**
 * 测试用 GOAP 动作 - 制作木板
 * <p>
 * 前置条件: has_wood = true
 * 效果: has_planks = true
 * 代价: 1.0
 */
public class CraftPlanksGoapAction implements IGoapAction {
    
    private final WorldState preconditions;
    private final WorldState effects;
    
    public CraftPlanksGoapAction() {
        // 前置条件：有木头
        this.preconditions = new WorldState();
        this.preconditions.setState("has_wood", true);
        
        // 效果：获得木板
        this.effects = new WorldState();
        this.effects.setState("has_planks", true);
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
        return 1.0f;
    }
    
    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        // 测试用动作：立即成功并在记忆中设置状态
        mind.getMemory().rememberLongTerm("has_planks", true);
        return ActionStatus.SUCCESS;
    }

    
    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        System.out.println("[CraftPlanksGoapAction] 开始制作木板");
    }
    
    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        System.out.println("[CraftPlanksGoapAction] 停止");
    }
    
    @Override
    public boolean canInterrupt() {
        return true;
    }
    
    @Override
    public String getName() {
        return "craft_planks";
    }
}
