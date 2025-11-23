package com.Kizunad.customNPCs_test.goap.real;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 真实的丢弃物品 GOAP 动作
 * <p>
 * 在当前位置丢弃主手中的物品
 * <p>
 * 前置条件: has_item = true, at_target_location = true
 * 效果: item_at_target = true, has_item = false
 * 代价: 1.0
 */
public class RealDropItemAction implements IGoapAction {
    
    private final WorldState preconditions;
    private final WorldState effects;
    private boolean dropped = false;
    
    public RealDropItemAction() {
        // 前置条件：拥有物品且已到达目标位置
        this.preconditions = new WorldState();
        this.preconditions.setState("has_item", true);
        this.preconditions.setState("at_target_location", true);
        
        // 效果：物品在目标位置，不再持有物品
        this.effects = new WorldState();
        this.effects.setState("item_at_target", true);
        this.effects.setState("has_item", false);
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
        if (dropped) {
            return ActionStatus.SUCCESS;
        }
        
        // 获取主手物品
        ItemStack heldItem = entity.getItemInHand(InteractionHand.MAIN_HAND);
        
        if (heldItem.isEmpty()) {
            System.out.println("[RealDropItemAction] 主手没有物品可丢弃");
            return ActionStatus.FAILURE;
        }
        
        // 生成掉落物实体
        var droppedEntity = entity.spawnAtLocation(heldItem.copy());
        
        if (droppedEntity == null) {
            System.out.println("[RealDropItemAction] 生成掉落物失败");
            return ActionStatus.FAILURE;
        }
        
        // 清空主手
        entity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        
        // 更新记忆
        mind.getMemory().rememberLongTerm("has_item", false);
        mind.getMemory().rememberLongTerm("item_at_target", true);
        mind.getMemory().rememberShortTerm("dropped_item_pos", droppedEntity.blockPosition().toString(), 200);
        
        System.out.println("[RealDropItemAction] 成功丢弃物品: " + 
            heldItem.getHoverName().getString() + " 在位置 " + 
            droppedEntity.blockPosition().toShortString());
        
        dropped = true;
        return ActionStatus.SUCCESS;
    }
    
    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        dropped = false;
        ItemStack heldItem = entity.getItemInHand(InteractionHand.MAIN_HAND);
        System.out.println("[RealDropItemAction] 开始丢弃物品: " + 
            (heldItem.isEmpty() ? "无" : heldItem.getHoverName().getString()));
    }
    
    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        System.out.println("[RealDropItemAction] 停止");
    }
    
    @Override
    public boolean canInterrupt() {
        return !dropped; // 丢弃完成后不可中断
    }
    
    @Override
    public String getName() {
        return "real_drop_item";
    }
}
