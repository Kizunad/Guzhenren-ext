package com.Kizunad.customNPCs_test.goap.real;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * 真实的移动到物品 GOAP 动作
 * <p>
 * 移动到ItemEntity附近
 * <p>
 * 前置条件: item_visible = true
 * 效果: at_item_location = true
 * 代价: 10.0 (移动代价较高)
 */
public class RealMoveToItemAction implements IGoapAction {
    
    private final WorldState preconditions;
    private final WorldState effects;
    private final ItemEntity targetItem;
    private int tickCount;
    private int stuckTicks;
    private double lastDistance;
    private boolean teleportAttempted;
    private static final int MAX_MOVE_TIME = 200; // 最多移动 200 ticks (10秒)
    private static final int STUCK_THRESHOLD = 60; // 60 ticks 没有进展视为卡住
    private static final double ARRIVAL_DISTANCE = 2.6; // 到达距离阈值，留出拾取导航余量
    private static final double STUCK_DISTANCE_THRESHOLD = 0.05; // 距离变化阈值
    private static final double MAX_COORDINATE = 30000000.0; // 世界边界
    private static final double MIN_Y_COORDINATE = -64.0;
    private static final double MAX_Y_COORDINATE = 320.0;
    
    /**
     * 构造函数 - 指定目标物品
     */
    public RealMoveToItemAction(ItemEntity targetItem) {
        this.targetItem = targetItem;
        this.tickCount = 0;
        this.stuckTicks = 0;
        this.lastDistance = Double.MAX_VALUE;
        this.teleportAttempted = false;
        
        // 前置条件：物品可见
        this.preconditions = new WorldState();
        this.preconditions.setState("item_visible", true);
        
        // 效果：到达物品位置
        this.effects = new WorldState();
        this.effects.setState("at_item_location", true);
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
        return 10.0f; // 移动代价较高
    }
    
    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        tickCount++;
        
        // 检查目标物品是否仍然存在
        if (!targetItem.isAlive()) {
            System.out.println("[RealMoveToItemAction] 目标物品已消失");
            return ActionStatus.FAILURE;
        }
        
        // 检查当前距离
        if (!isValidPosition(targetItem.position())) {
            System.out.println("[RealMoveToItemAction] 目标坐标无效，终止计划: " + targetItem.position());
            return ActionStatus.FAILURE;
        }

        double currentDistance = entity.position().distanceTo(targetItem.position());
        
        // 检查是否已到达
        if (currentDistance <= ARRIVAL_DISTANCE) {
            System.out.println("[RealMoveToItemAction] 已到达物品位置，距离: " + 
                String.format("%.2f", currentDistance));
            mind.getMemory().rememberShortTerm("at_item_location", true, 100);
            return ActionStatus.SUCCESS;
        }
        
        // 检查超时
        if (tickCount >= MAX_MOVE_TIME) {
            System.out.println("[RealMoveToItemAction] 移动超时 (" + MAX_MOVE_TIME + " ticks)");
            return ActionStatus.FAILURE;
        }
        
        // 检测是否卡住
        if (Math.abs(currentDistance - lastDistance) < STUCK_DISTANCE_THRESHOLD) {
            stuckTicks++;
            if (stuckTicks >= STUCK_THRESHOLD) {
                if (tryTeleportToItem(entity)) {
                    return ActionStatus.RUNNING;
                }
                System.out.println("[RealMoveToItemAction] 实体卡住，无法继续移动");
                return ActionStatus.FAILURE;
            }
        } else {
            stuckTicks = 0; // 重置卡住计数
        }
        lastDistance = currentDistance;
        
        // 执行导航
        if (entity instanceof Mob mob) {
            PathNavigation navigation = mob.getNavigation();
            
            // 每10 ticks重新计算路径（跟随移动的物品）
            if (tickCount % 10 == 0 || !navigation.isInProgress()) {
                boolean pathSuccess = navigation.moveTo(
                    targetItem.getX(), 
                    targetItem.getY(), 
                    targetItem.getZ(), 
                    1.0 // 移动速度倍率
                );

                if (!pathSuccess && tickCount > 40) {
                    if (tryTeleportToItem(mob)) {
                        return ActionStatus.RUNNING;
                    }
                    System.out.println("[RealMoveToItemAction] 无法找到到物品的路径");
                    return ActionStatus.FAILURE;
                }
            }
            
            // 定期输出进度
            if (tickCount % 20 == 0) {
                System.out.println("[RealMoveToItemAction] 移动中... 距离: " + 
                    String.format("%.2f", currentDistance) + " (" + tickCount + "/" + MAX_MOVE_TIME + ")");
            }
            
            return ActionStatus.RUNNING;
        } else {
            System.out.println("[RealMoveToItemAction] 实体不是Mob，无法移动");
            return ActionStatus.FAILURE;
        }
    }
    
    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        tickCount = 0;
        stuckTicks = 0;
        lastDistance = Double.MAX_VALUE;
        teleportAttempted = false;
        if (!isValidPosition(targetItem.position())) {
            System.out.println("[RealMoveToItemAction] 起始时发现目标坐标无效，直接失败");
            return;
        }
        System.out.println("[RealMoveToItemAction] 开始移动到物品: " + 
            targetItem.getItem().getHoverName().getString() + 
            " 位置: " + targetItem.blockPosition().toShortString());
    }
    
    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        // 停止导航
        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
        }
        System.out.println("[RealMoveToItemAction] 停止移动");
    }
    
    @Override
    public boolean canInterrupt() {
        return true;
    }
    
    @Override
    public String getName() {
        return "real_move_to_item";
    }

    private boolean isValidPosition(net.minecraft.world.phys.Vec3 pos) {
        if (!Double.isFinite(pos.x) || !Double.isFinite(pos.y) || !Double.isFinite(pos.z)) {
            return false;
        }
        if (Math.abs(pos.x) > MAX_COORDINATE || Math.abs(pos.z) > MAX_COORDINATE) {
            return false;
        }
        return pos.y >= MIN_Y_COORDINATE && pos.y <= MAX_Y_COORDINATE;
    }

    private boolean tryTeleportToItem(LivingEntity entity) {
        if (!(entity instanceof Mob mob) || teleportAttempted) {
            return false;
        }
        teleportAttempted = true;
        mob.getNavigation().stop();
        net.minecraft.world.phys.Vec3 targetPos = targetItem.position();
        double safeY = Math.max(targetPos.y, mob.getY());
        mob.teleportTo(targetPos.x, safeY, targetPos.z);
        stuckTicks = 0;
        lastDistance = Double.MAX_VALUE;
        System.out.println("[RealMoveToItemAction] 触发兜底传送到物品附近: " + targetPos);
        return true;
    }
}
