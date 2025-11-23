package com.Kizunad.customNPCs_test.goap;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

/**
 * 测试用 GOAP 动作 - 获取苹果
 * <p>
 * 前置条件: 无
 * 效果: has_apple = true
 * 代价: 1.0
 */
public class GetAppleGoapAction implements IGoapAction {
    
    private final WorldState preconditions;
    private final WorldState effects;
    
    public GetAppleGoapAction() {
        // 前置条件：无
        this.preconditions = new WorldState();
        
        // 效果：获得苹果
        this.effects = new WorldState();
        this.effects.setState("has_apple", true);
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
        mind.getMemory().rememberLongTerm("has_apple", true);
        return ActionStatus.SUCCESS;
    }

    
    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        System.out.println("[GetAppleGoapAction] 开始获取苹果");
    }
    
    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        System.out.println("[GetAppleGoapAction] 停止");
    }
    
    @Override
    public boolean canInterrupt() {
        return true;
    }
    
    @Override
    public String getName() {
        return "get_apple";
    }
}
