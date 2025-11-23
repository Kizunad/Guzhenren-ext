package com.Kizunad.customNPCs_test.goap;

import com.Kizunad.customNPCs.ai.decision.goals.PlanBasedGoal;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.goap.real.RealDropItemAction;
import com.Kizunad.customNPCs_test.goap.real.RealMoveToItemAction;
import com.Kizunad.customNPCs_test.goap.real.RealMoveToTargetAction;
import com.Kizunad.customNPCs_test.goap.real.RealPickUpItemAction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 搬运工目标
 * <p>
 * 使用 GOAP 规划将物品从一个位置搬运到另一个位置
 * <p>
 * 目标状态: item_at_target = true
 */
public class CourierGoal extends PlanBasedGoal {
    
    private final ItemEntity sourceItem;
    private final BlockPos targetPos;
    private final float basePriority;
    
    /**
     * 构造函数
     * @param sourceItem 源物品实体
     * @param targetPos 目标位置
     * @param basePriority 基础优先级
     */
    public CourierGoal(ItemEntity sourceItem, BlockPos targetPos, float basePriority) {
        this.sourceItem = sourceItem;
        this.targetPos = targetPos;
        this.basePriority = basePriority;
    }
    
    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        // 如果源物品不存在，优先级为0
        if (!sourceItem.isAlive()) {
            return 0.0f;
        }
        
        return basePriority;
    }
    
    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        // 源物品必须存在
        return sourceItem.isAlive();
    }
    
    @Override
    public WorldState getDesiredState(INpcMind mind, LivingEntity entity) {
        WorldState desired = new WorldState();
        desired.setState("item_at_target", true);
        return desired;
    }
    
    public WorldState getCurrentState(INpcMind mind, LivingEntity entity) {
        WorldState current = new WorldState();
        
        // 检查是否持有物品
        ItemStack heldItem = entity.getItemInHand(InteractionHand.MAIN_HAND);
        boolean hasItem = !heldItem.isEmpty();
        current.setState("has_item", hasItem);
        
        // 检查是否可见源物品
        if (sourceItem.isAlive() && !hasItem) {
            current.setState("item_visible", true);
            
            // 检查是否在物品附近
            double itemDistance = entity.position().distanceTo(sourceItem.position());
            current.setState("at_item_location", itemDistance <= 2.0);
        } else {
            current.setState("item_visible", !hasItem);
            current.setState("at_item_location", false);
        }
        
        // 检查是否在目标位置附近
        double targetDistance = entity.blockPosition().distToCenterSqr(
            targetPos.getX() + 0.5, 
            targetPos.getY(), 
            targetPos.getZ() + 0.5
        );
        current.setState("at_target_location", Math.sqrt(targetDistance) <= 2.0);
        
        // 检查物品是否已在目标位置
        current.setState("item_at_target", false);
        current.setState("item_picked", false);
        
        return current;
    }
    
    @Override
    public List<IGoapAction> getAvailableActions(INpcMind mind, LivingEntity entity) {
        List<IGoapAction> actions = new ArrayList<>();
        
        // 添加移动到源物品的动作
        actions.add(new RealMoveToItemAction(sourceItem));
        
        // 添加拾取物品的动作
        actions.add(new RealPickUpItemAction(sourceItem));
        
        // 添加移动到目标位置的动作
        actions.add(new RealMoveToTargetAction(targetPos));
        
        // 添加丢弃物品的动作
        actions.add(new RealDropItemAction());
        
        return actions;
    }
    
    public void onStart(INpcMind mind, LivingEntity entity) {
        System.out.println("[CourierGoal] 开始搬运任务: " + 
            sourceItem.getItem().getHoverName().getString() + 
            " 从 " + sourceItem.blockPosition().toShortString() + 
            " 到 " + targetPos.toShortString());
    }
    
    public void onStop(INpcMind mind, LivingEntity entity) {
        System.out.println("[CourierGoal] 停止搬运任务");
    }
    
    public void onPlanSuccess(INpcMind mind, LivingEntity entity) {
        System.out.println("[CourierGoal] 成功完成搬运任务！");
    }
    
    public void onPlanFailure(INpcMind mind, LivingEntity entity) {
        System.out.println("[CourierGoal] 搬运任务失败");
    }
    
    @Override
    public String getName() {
        return "courier";
    }
}
