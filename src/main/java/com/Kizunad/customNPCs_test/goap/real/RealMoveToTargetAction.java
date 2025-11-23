package com.Kizunad.customNPCs_test.goap.real;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

/**
 * 真实的移动到目标位置 GOAP 动作
 * <p>
 * 移动到指定的BlockPos
 * <p>
 * 前置条件: 无
 * 效果: at_target_location = true
 * 代价: 15.0 (移动到固定位置代价较高)
 */
public class RealMoveToTargetAction implements IGoapAction {
    
    private final WorldState preconditions;
    private final WorldState effects;
    private final BlockPos targetPos;
    private int tickCount;
    private int stuckTicks;
    private double lastDistance;
    private static final int MAX_MOVE_TIME = 300; // 最多移动 300 ticks (15秒)
    private static final int STUCK_THRESHOLD = 40; // 40 ticks 没有进展视为卡住
    private static final double ARRIVAL_DISTANCE = 2.0; // 到达距离阈值
    private static final double STUCK_DISTANCE_THRESHOLD = 0.1; // 距离变化阈值
    
    /**
     * 构造函数 - 指定目标位置
     */
    public RealMoveToTargetAction(BlockPos targetPos) {
        this.targetPos = targetPos;
        this.tickCount = 0;
        this.stuckTicks = 0;
        this.lastDistance = Double.MAX_VALUE;
        
        // 前置条件：无（可以随时移动到目标位置）
        this.preconditions = new WorldState();
        
        // 效果：到达目标位置
        this.effects = new WorldState();
        this.effects.setState("at_target_location", true);
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
        return 15.0f; // 移动代价高
    }
    
    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        tickCount++;
        
        // 计算当前距离
        double currentDistance = entity.blockPosition().distToCenterSqr(
            targetPos.getX() + 0.5, 
            targetPos.getY(), 
            targetPos.getZ() + 0.5
        );
        currentDistance = Math.sqrt(currentDistance);
        
        // 检查是否已到达
        if (currentDistance <= ARRIVAL_DISTANCE) {
            System.out.println("[RealMoveToTargetAction] 已到达目标位置，距离: " + 
                String.format("%.2f", currentDistance));
            mind.getMemory().rememberShortTerm("at_target_location", true, 100);
            return ActionStatus.SUCCESS;
        }
        
        // 检查超时
        if (tickCount >= MAX_MOVE_TIME) {
            System.out.println("[RealMoveToTargetAction] 移动超时 (" + MAX_MOVE_TIME + " ticks)");
            return ActionStatus.FAILURE;
        }
        
        // 检测是否卡住
        if (Math.abs(currentDistance - lastDistance) < STUCK_DISTANCE_THRESHOLD) {
            stuckTicks++;
            if (stuckTicks >= STUCK_THRESHOLD) {
                System.out.println("[RealMoveToTargetAction] 实体卡住，无法继续移动");
                return ActionStatus.FAILURE;
            }
        } else {
            stuckTicks = 0; // 重置卡住计数
        }
        lastDistance = currentDistance;
        
        // 执行导航
        if (entity instanceof Mob mob) {
            PathNavigation navigation = mob.getNavigation();
            
            // 每20 ticks重新计算路径
            if (tickCount % 20 == 0 || !navigation.isInProgress()) {
                boolean pathSuccess = navigation.moveTo(
                    targetPos.getX() + 0.5, 
                    targetPos.getY(), 
                    targetPos.getZ() + 0.5, 
                    1.0 // 移动速度倍率
                );
                
                if (!pathSuccess && tickCount > 60) {
                    System.out.println("[RealMoveToTargetAction] 无法找到到目标位置的路径");
                    return ActionStatus.FAILURE;
                }
            }
            
            // 定期输出进度
            if (tickCount % 20 == 0) {
                System.out.println("[RealMoveToTargetAction] 移动中... 距离: " + 
                    String.format("%.2f", currentDistance) + " (" + tickCount + "/" + MAX_MOVE_TIME + ")");
            }
            
            return ActionStatus.RUNNING;
        } else {
            System.out.println("[RealMoveToTargetAction] 实体不是Mob，无法移动");
            return ActionStatus.FAILURE;
        }
    }
    
    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        tickCount = 0;
        stuckTicks = 0;
        lastDistance = Double.MAX_VALUE;
        System.out.println("[RealMoveToTargetAction] 开始移动到目标位置: " + 
            targetPos.toShortString());
    }
    
    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        // 停止导航
        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
        }
        System.out.println("[RealMoveToTargetAction] 停止移动");
    }
    
    @Override
    public boolean canInterrupt() {
        return true;
    }
    
    @Override
    public String getName() {
        return "real_move_to_target";
    }
}
