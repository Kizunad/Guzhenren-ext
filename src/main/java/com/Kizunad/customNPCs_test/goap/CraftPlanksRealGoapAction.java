package com.Kizunad.customNPCs_test.goap;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

/**
 * 制作木板的 GOAP 动作（真实版本）
 * <p>
 * 前置条件: has_wood = true
 * 效果: has_planks = true
 * 代价: 1.0
 */
public class CraftPlanksRealGoapAction implements IGoapAction {
    
    private final WorldState preconditions;
    private final WorldState effects;
    private int tickCount;
    
    public CraftPlanksRealGoapAction() {
        this.tickCount = 0;
        
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
        tickCount++;
        
        // 模拟制作需要时间（10 ticks）
        if (tickCount >= 10) {
            // 设置记忆：已制作木板
            mind.getMemory().rememberLongTerm("has_planks", true);
            System.out.println("[CraftPlanksRealGoapAction] 已制作木板");
            return ActionStatus.SUCCESS;
        }
        
        System.out.println("[CraftPlanksRealGoapAction] 正在制作木板... (" + tickCount + "/10)");
        return ActionStatus.RUNNING;
    }
    
    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        tickCount = 0;
        System.out.println("[CraftPlanksRealGoapAction] 开始制作木板");
    }
    
    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        System.out.println("[CraftPlanksRealGoapAction] 停止");
    }
    
    @Override
    public boolean canInterrupt() {
        return true;
    }
    
    @Override
    public String getName() {
        return "craft_planks_real";
    }
}
