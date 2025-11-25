package com.Kizunad.customNPCs_test.goap.real;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;


/**
 * 真实的拾取物品 GOAP 动作
 * <p>
 * 拾取指定的ItemEntity到实体的主手
 * <p>
 * 前置条件: at_item_location = true
 * 效果: has_item = true, item_picked = true
 * 代价: 1.0
 */
public class RealPickUpItemAction implements IGoapAction {
    
    private final WorldState preconditions;
    private final WorldState effects;
    private final ItemEntity targetItem;
    private int tickCount;
    private static final int MAX_PICKUP_TIME = 80; // 最多等待 80 ticks，留出靠近距离的缓冲
    private static final double PICKUP_DISTANCE = 2.0; // 拾取距离阈值
    
    /**
     * 构造函数 - 指定要拾取的物品实体
     */
    public RealPickUpItemAction(ItemEntity targetItem) {
        this.targetItem = targetItem;
        this.tickCount = 0;
        
        // 前置条件：已经到达物品位置
        this.preconditions = new WorldState();
        this.preconditions.setState("at_item_location", true);
        
        // 效果：拥有物品，物品已拾取
        this.effects = new WorldState();
        this.effects.setState("has_item", true);
        this.effects.setState("item_picked", true);
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
        
        // 检查目标物品是否仍然存在
        if (!targetItem.isAlive()) {
            System.out.println("[RealPickUpItemAction] 目标物品已消失，拾取失败");
            return ActionStatus.FAILURE;
        }
        
        // 检查距离
        double distance = entity.position().distanceTo(targetItem.position());
        if (distance > PICKUP_DISTANCE) {
            // 距离太远，尝试再靠近目标
            if (entity instanceof Mob mob) {
                if (tickCount % 5 == 0 || !mob.getNavigation().isInProgress()) {
                    mob.getNavigation().moveTo(targetItem, 1.0);
                }
            }
            // 距离太远，继续等待或失败
            if (tickCount >= MAX_PICKUP_TIME) {
                System.out.println("[RealPickUpItemAction] 超时：距离太远 (" + 
                    String.format("%.2f", distance) + " > " + PICKUP_DISTANCE + ")");
                return ActionStatus.FAILURE;
            }
            
            if (tickCount % 10 == 0) {
                System.out.println("[RealPickUpItemAction] 等待靠近物品... 距离: " + 
                    String.format("%.2f", distance));
            }
            return ActionStatus.RUNNING;
        }
        
        // 距离足够近，执行拾取
        ItemStack itemStack = targetItem.getItem().copy();
        
        // 触发拾取动画
        entity.take(targetItem, itemStack.getCount());
        
        // 将物品放入主手
        entity.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
        
        // 移除物品实体
        targetItem.discard();
        
        // 更新记忆
        mind.getMemory().rememberLongTerm("has_item", true);
        mind.getMemory().rememberLongTerm("item_picked", true);
        mind.getMemory().rememberShortTerm("picked_item_type", itemStack.getItem().toString(), 200);
        
        System.out.println("[RealPickUpItemAction] 成功拾取物品: " + 
            itemStack.getHoverName().getString() + " x" + itemStack.getCount());
        
        return ActionStatus.SUCCESS;
    }
    
    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        tickCount = 0;
        System.out.println("[RealPickUpItemAction] 开始拾取物品: " + 
            targetItem.getItem().getHoverName().getString());
    }
    
    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        System.out.println("[RealPickUpItemAction] 停止");
    }
    
    @Override
    public boolean canInterrupt() {
        return true;
    }
    
    @Override
    public String getName() {
        return "real_pick_up_item";
    }
}
