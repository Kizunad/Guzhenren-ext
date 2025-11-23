package com.Kizunad.customNPCs_test.goap;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

/**
 * 收集物品的 GOAP 动作
 * <p>
 * 前置条件: tree_broken = true
 * 效果: has_wood = true
 * 代价: 1.0
 */
public class CollectItemGoapAction implements IGoapAction {
    
    private final WorldState preconditions;
    private final WorldState effects;
    private int tickCount;
    
    public CollectItemGoapAction() {
        this.tickCount = 0;
        
        // 前置条件：树木已破坏
        this.preconditions = new WorldState();
        this.preconditions.setState("tree_broken", true);
        
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
        return 1.0f;
    }
    
    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        tickCount++;
        
        // 模拟收集物品需要几个 tick
        if (tickCount >= 5) {
            // 设置记忆：已获得木头
            mind.getMemory().rememberLongTerm("has_wood", true);
            System.out.println("[CollectItemGoapAction] 已收集木头");
            return ActionStatus.SUCCESS;
        }
        
        System.out.println("[CollectItemGoapAction] 正在收集物品... (" + tickCount + "/5)");
        return ActionStatus.RUNNING;
    }
    
    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        tickCount = 0;
        System.out.println("[CollectItemGoapAction] 开始收集物品");
    }
    
    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        System.out.println("[CollectItemGoapAction] 停止");
    }
    
    @Override
    public boolean canInterrupt() {
        return true;
    }
    
    @Override
    public String getName() {
        return "collect_item";
    }
}
