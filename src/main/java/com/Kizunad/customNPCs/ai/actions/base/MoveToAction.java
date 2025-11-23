package com.Kizunad.customNPCs.ai.actions.base;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;

/**
 * 移动动作 - 让 NPC 移动到指定位置
 * <p>
 * 支持：
 * - 移动到固定坐标
 * - 移动到实体位置（动态跟踪）
 * <p>
 * 使用原版的 PathNavigation 系统进行寻路
 */
public class MoveToAction implements IAction {
    
    private final Vec3 targetPos;
    private final Entity targetEntity;
    private final double speed;
    private final double acceptableDistance; // 可接受的到达距离
    
    private PathNavigation navigation;
    private int pathUpdateCooldown; // 路径更新冷却（避免每 tick 都重新计算）
    private static final int PATH_UPDATE_INTERVAL = 10; // 每 10 ticks 更新一次路径
    
    /**
     * 创建移动到坐标的动作
     * @param target 目标坐标
     * @param speed 移动速度
     */
    public MoveToAction(Vec3 target, double speed) {
        this(target, speed, 2.0); // 默认接受距离 2 格
    }
    
    /**
     * 创建移动到坐标的动作（指定接受距离）
     * @param target 目标坐标
     * @param speed 移动速度
     * @param acceptableDistance 可接受的到达距离
     */
    public MoveToAction(Vec3 target, double speed, double acceptableDistance) {
        this.targetPos = target;
        this.targetEntity = null;
        this.speed = speed;
        this.acceptableDistance = acceptableDistance;
        this.pathUpdateCooldown = 0;
    }
    
    /**
     * 创建移动到实体的动作
     * @param target 目标实体
     * @param speed 移动速度
     */
    public MoveToAction(Entity target, double speed) {
        this(target, speed, 2.0);
    }
    
    /**
     * 创建移动到实体的动作（指定接受距离）
     * @param target 目标实体
     * @param speed 移动速度
     * @param acceptableDistance 可接受的到达距离
     */
    public MoveToAction(Entity target, double speed, double acceptableDistance) {
        this.targetPos = null;
        this.targetEntity = target;
        this.speed = speed;
        this.acceptableDistance = acceptableDistance;
        this.pathUpdateCooldown = 0;
    }
    
    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        // 检查实体是否为 Mob（只有 Mob 才有 Navigation）
        if (!(entity instanceof net.minecraft.world.entity.Mob mob)) {
            return ActionStatus.FAILURE;
        }
        
        // 获取目标位置
        Vec3 currentTarget = getCurrentTarget();
        if (currentTarget == null) {
            System.out.println("[MoveToAction] 目标无效，移动失败");
            return ActionStatus.FAILURE;
        }
        
        // 检查是否已到达
        double distanceToTarget = entity.position().distanceTo(currentTarget);
        if (distanceToTarget <= acceptableDistance) {
            System.out.println("[MoveToAction] 已到达目标，距离: " + String.format("%.2f", distanceToTarget));
            return ActionStatus.SUCCESS;
        }
        
        // 更新路径（带冷却）
        pathUpdateCooldown--;
        if (pathUpdateCooldown <= 0) {
            boolean pathCreated = navigation.moveTo(currentTarget.x, currentTarget.y, currentTarget.z, speed);
            pathUpdateCooldown = PATH_UPDATE_INTERVAL;
            
            if (!pathCreated && navigation.getPath() == null) {
                System.out.println("[MoveToAction] 无法创建路径到目标");
                return ActionStatus.FAILURE;
            }
        }
        
        // 检查寻路是否卡住
        if (navigation.isDone()) {
            // 寻路结束但未到达目标，可能是路径失败
            if (distanceToTarget > acceptableDistance) {
                System.out.println("[MoveToAction] 寻路失败，距离目标还有: " + String.format("%.2f", distanceToTarget));
                return ActionStatus.FAILURE;
            }
        }
        
        return ActionStatus.RUNNING;
    }
    
    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        if (entity instanceof net.minecraft.world.entity.Mob mob) {
            this.navigation = mob.getNavigation();
            this.pathUpdateCooldown = 0; // 立即开始寻路
            
            String targetName = targetEntity != null ? targetEntity.getName().getString() : targetPos.toString();
            System.out.println("[MoveToAction] 开始移动到: " + targetName + "，速度: " + speed);
        }
    }
    
    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        // 停止当前寻路
        if (navigation != null) {
            navigation.stop();
        }
        
        String targetName = targetEntity != null 
            ? targetEntity.getName().getString() 
            : (targetPos != null ? targetPos.toString() : "null");
        System.out.println("[MoveToAction] 停止移动到: " + targetName);
    }
    
    @Override
    public boolean canInterrupt() {
        // 移动动作可以被中断（例如遇到危险时）
        return true;
    }
    
    @Override
    public String getName() {
        if (targetEntity != null) {
            return "move_to_entity_" + targetEntity.getId();
        } else {
            return "move_to_pos";
        }
    }
    
    /**
     * 获取当前目标位置
     * @return 目标坐标，如果无效则返回 null
     */
    private Vec3 getCurrentTarget() {
        if (targetEntity != null && targetEntity.isAlive()) {
            return targetEntity.position();
        } else if (targetPos != null) {
            return targetPos;
        }
        return null;
    }
}
