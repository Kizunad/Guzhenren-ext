package com.Kizunad.customNPCs_test.goap;

import com.Kizunad.customNPCs.ai.decision.goals.PlanBasedGoal;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.goap.real.RealMoveToItemAction;
import com.Kizunad.customNPCs_test.goap.real.RealPickUpItemAction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜集物品目标
 * <p>
 * 使用 GOAP 规划拾取指定物品
 * <p>
 * 目标状态: has_item = true, item_picked = true
 */
public class GatherItemGoal extends PlanBasedGoal {
    
    private final ItemEntity targetItem;
    private final float basePriority;
    
    /**
     * 构造函数
     * @param targetItem 目标物品实体
     * @param basePriority 基础优先级
     */
    public GatherItemGoal(ItemEntity targetItem, float basePriority) {
        this.targetItem = targetItem;
        this.basePriority = basePriority;
    }
    
    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        // 如果目标物品不存在，优先级为0
        if (!targetItem.isAlive()) {
            return 0.0f;
        }
        
        // 根据距离调整优先级（越近优先级越高）
        double distance = entity.position().distanceTo(targetItem.position());
        float distanceModifier = (float) Math.max(0.0, 1.0 - (distance / 50.0));
        
        return basePriority * (0.5f + 0.5f * distanceModifier);
    }
    
    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        // 目标物品必须存在
        return targetItem.isAlive();
    }
    
    @Override
    public WorldState getDesiredState(INpcMind mind, LivingEntity entity) {
        WorldState desired = new WorldState();
        desired.setState("has_item", true);
        desired.setState("item_picked", true);
        return desired;
    }
    
    public WorldState getCurrentState(INpcMind mind, LivingEntity entity) {
        WorldState current = new WorldState();
        
        // 检查是否可见物品
        if (targetItem.isAlive()) {
            current.setState("item_visible", true);
            
            // 检查是否在物品附近
            double distance = entity.position().distanceTo(targetItem.position());
            current.setState("at_item_location", distance <= 2.0);
        } else {
            current.setState("item_visible", false);
            current.setState("at_item_location", false);
        }
        
        // 检查是否已拾取物品
        current.setState("has_item", false);
        current.setState("item_picked", false);
        
        return current;
    }
    
    @Override
    public List<IGoapAction> getAvailableActions(INpcMind mind, LivingEntity entity) {
        List<IGoapAction> actions = new ArrayList<>();
        
        // 添加移动到物品的动作
        actions.add(new RealMoveToItemAction(targetItem));
        
        // 添加拾取物品的动作
        actions.add(new RealPickUpItemAction(targetItem));
        
        return actions;
    }
    
    public void onStart(INpcMind mind, LivingEntity entity) {
        System.out.println("[GatherItemGoal] 开始搜集物品: " + 
            targetItem.getItem().getHoverName().getString());
    }
    
    public void onStop(INpcMind mind, LivingEntity entity) {
        System.out.println("[GatherItemGoal] 停止搜集物品");
    }
    
    public void onPlanSuccess(INpcMind mind, LivingEntity entity) {
        System.out.println("[GatherItemGoal] 成功搜集物品！");
    }
    
    public void onPlanFailure(INpcMind mind, LivingEntity entity) {
        System.out.println("[GatherItemGoal] 搜集物品失败");
    }
    
    @Override
    public String getName() {
        return "gather_item";
    }
}
