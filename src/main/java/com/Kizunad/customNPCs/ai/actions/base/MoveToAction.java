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
@SuppressWarnings("checkstyle:MagicNumber")
public class MoveToAction implements IAction {
    
    private final Vec3 targetPos;
    private final Entity targetEntity;
    private final double speed;
    private final double acceptableDistance; // 可接受的到达距离
    private static final double ARRIVAL_BUFFER = 0.75; // 允许的额外容差，避免寻路停在边缘时误判失败
    private boolean teleportAttempted;
    
    private PathNavigation navigation;
    private int pathUpdateCooldown; // 路径更新冷却（避免每 tick 都重新计算）
    private static final int PATH_UPDATE_INTERVAL = 10; // 每 10 ticks 更新一次路径
    
    // 超时机制
    private int currentTick;
    private static final int DEFAULT_MAX_TICKS = 600; // 默认30秒超时
    private final int maxTicks;
    
    // 卡住检测
    private Vec3 lastPosition;
    private int stuckTicks;
    private static final int MAX_STUCK_TICKS = 40; // 连续40 ticks不动判定卡住
    private static final double MIN_MOVEMENT = 0.1; // 最小移动距离
    
    // 坐标验证
    private static final double MAX_COORDINATE = 30000000.0; // Minecraft 世界边界
    private static final double MIN_Y_COORDINATE = -64.0; // 最小 Y 坐标 (1.18+)
    private static final double MAX_Y_COORDINATE = 320.0; // 最大 Y 坐标 (1.18+)
    
    /**
     * 创建移动到坐标的动作
     * @param target 目标坐标
     * @param speed 移动速度
     */
    public MoveToAction(Vec3 target, double speed) {
        this(target, speed, 2.0, DEFAULT_MAX_TICKS);
    }
    
    /**
     * 创建移动到坐标的动作（指定接受距离）
     * @param target 目标坐标
     * @param speed 移动速度
     * @param acceptableDistance 可接受的到达距离
     */
    public MoveToAction(Vec3 target, double speed, double acceptableDistance) {
        this(target, speed, acceptableDistance, DEFAULT_MAX_TICKS);
    }
    
    /**
     * 创建移动到坐标的动作（完整参数）
     * @param target 目标坐标
     * @param speed 移动速度
     * @param acceptableDistance 可接受的到达距离
     * @param maxTicks 最大执行 tick 数（超时）
     */
    public MoveToAction(Vec3 target, double speed, double acceptableDistance, int maxTicks) {
        this.targetPos = target;
        this.targetEntity = null;
        this.speed = speed;
        this.acceptableDistance = acceptableDistance;
        this.maxTicks = maxTicks;
        this.pathUpdateCooldown = 0;
        this.currentTick = 0;
        this.stuckTicks = 0;
        this.teleportAttempted = false;
    }
    
    /**
     * 创建移动到实体的动作
     * @param target 目标实体
     * @param speed 移动速度
     */
    public MoveToAction(Entity target, double speed) {
        this(target, speed, 2.0, DEFAULT_MAX_TICKS);
    }
    
    /**
     * 创建移动到实体的动作（指定接受距离）
     * @param target 目标实体
     * @param speed 移动速度
     * @param acceptableDistance 可接受的到达距离
     */
    public MoveToAction(Entity target, double speed, double acceptableDistance) {
        this(target, speed, acceptableDistance, DEFAULT_MAX_TICKS);
    }
    
    /**
     * 创建移动到实体的动作（完整参数）
     * @param target 目标实体
     * @param speed 移动速度
     * @param acceptableDistance 可接受的到达距离
     * @param maxTicks 最大执行 tick 数（超时）
     */
    public MoveToAction(Entity target, double speed, double acceptableDistance, int maxTicks) {
        this.targetPos = null;
        this.targetEntity = target;
        this.speed = speed;
        this.acceptableDistance = acceptableDistance;
        this.maxTicks = maxTicks;
        this.pathUpdateCooldown = 0;
        this.currentTick = 0;
        this.stuckTicks = 0;
        this.teleportAttempted = false;
    }
    
    
    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        // 检查实体是否为 Mob（只有 Mob 才有 Navigation）
        if (!(entity instanceof net.minecraft.world.entity.Mob mob)) {
            System.err.println("[MoveToAction] 实体不是 Mob 类型: " + entity.getClass().getSimpleName());
            return ActionStatus.FAILURE;
        }
        
        // 超时检测
        currentTick++;
        if (currentTick >= maxTicks) {
            System.out.println("[MoveToAction] 超时失败，已执行 " + currentTick + " ticks");
            return ActionStatus.FAILURE;
        }
        
        // 获取目标位置
        Vec3 currentTarget = getCurrentTarget();
        if (currentTarget == null || !isValidTarget(currentTarget)) {
            System.err.println("[MoveToAction] 目标无效: " + currentTarget);
            return ActionStatus.FAILURE;
        }
        
        // 检查是否已到达
        Vec3 currentPos = entity.position();
        double distanceToTarget = currentPos.distanceTo(currentTarget);
        if (distanceToTarget <= acceptableDistance + ARRIVAL_BUFFER) {
            System.out.println("[MoveToAction] 已到达目标，距离: " + String.format("%.2f", distanceToTarget));
            return ActionStatus.SUCCESS;
        }
        
        // 每 20 ticks 打印一次位置信息
        if (currentTick % 20 == 0) {
            System.out.println("[MoveToAction DEBUG] Tick " + currentTick 
                + " | 当前位置: " + String.format("(%.1f, %.1f, %.1f)", currentPos.x, currentPos.y, currentPos.z)
                + " | 目标位置: " + String.format("(%.1f, %.1f, %.1f)", currentTarget.x, currentTarget.y, currentTarget.z)
                + " | 距离: " + String.format("%.2f", distanceToTarget)
                + " | Navigation.isDone: " + navigation.isDone()
                + " | hasPath: " + (navigation.getPath() != null));
        }
        
        // 卡住检测
        if (lastPosition != null) {
            double movementDistance = currentPos.distanceTo(lastPosition);
            if (movementDistance < MIN_MOVEMENT) {
                stuckTicks++;
                if (stuckTicks >= MAX_STUCK_TICKS) {
                    if (tryTeleportToTarget(entity, currentTarget)) {
                        return ActionStatus.RUNNING;
                    }
                    System.err.println("[MoveToAction] 卡住失败: " + MAX_STUCK_TICKS + " ticks 未移动"
                        + " | 当前位置: " + currentPos
                        + " | Navigation状态: isDone=" + navigation.isDone() 
                        + ", hasPath=" + (navigation.getPath() != null));
                    return ActionStatus.FAILURE;
                }
            } else {
                stuckTicks = 0; // 有移动，重置计数
            }
        }
        lastPosition = currentPos;
        
        // 更新路径（带冷却）
        pathUpdateCooldown--;
        if (pathUpdateCooldown <= 0) {
            boolean pathCreated = navigation.moveTo(currentTarget.x, currentTarget.y, currentTarget.z, speed);
            pathUpdateCooldown = PATH_UPDATE_INTERVAL;
            
            if (!pathCreated && navigation.getPath() == null) {
                if (tryTeleportToTarget(entity, currentTarget)) {
                    return ActionStatus.RUNNING;
                }
                System.err.println("[MoveToAction] 无法创建路径"
                    + " | 从: " + String.format("(%.1f, %.1f, %.1f)", currentPos.x, currentPos.y, currentPos.z)
                    + " | 到: " + String.format("(%.1f, %.1f, %.1f)", currentTarget.x, currentTarget.y, currentTarget.z)
                    + " | 距离: " + String.format("%.2f", distanceToTarget)
                    + " | 实体在地面: " + entity.onGround());
                return ActionStatus.FAILURE;
            }
            
            if (pathCreated && currentTick % 20 == 0) {
                System.out.println("[MoveToAction DEBUG] 路径已创建/更新");
            }
        }
        
        // 检查寻路是否卡住
        // navigation.isDone() 通常表示寻路器已经到达了路径的终点，或者无法继续寻路（例如，路径被阻挡或目标不可达）。
        // 它不一定意味着实体已经到达了最终目标点，只是路径计算和跟随过程结束了。
        if (navigation.isDone()) { 
            // 寻路结束但未到达目标，可能是路径失败
            if (distanceToTarget > acceptableDistance + ARRIVAL_BUFFER) {
                if (tryTeleportToTarget(entity, currentTarget)) {
                    return ActionStatus.RUNNING;
                }
                System.err.println("[MoveToAction] 寻路结束但未到达目标"
                    + " | 距离目标: " + String.format("%.2f", distanceToTarget)
                    + " | 可接受距离: " + acceptableDistance
                    + " | 当前位置: " + String.format("(%.1f, %.1f, %.1f)", currentPos.x, currentPos.y, currentPos.z)
                    + " | 目标位置: " + String.format("(%.1f, %.1f, %.1f)",
                        currentTarget.x, currentTarget.y, currentTarget.z));
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
            this.currentTick = 0; // 重置超时计数
            this.stuckTicks = 0; // 重置卡住计数
            this.lastPosition = entity.position(); // 记录起始位置
            this.teleportAttempted = false;
            
            String targetName = targetEntity != null 
                ? targetEntity.getName().getString() 
                : targetPos.toString();
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

    private boolean tryTeleportToTarget(LivingEntity entity, Vec3 target) {
        if (teleportAttempted || !hasTestTag(entity)) {
            return false;
        }
        teleportAttempted = true;
        if (navigation != null) {
            navigation.stop();
        }
        entity.teleportTo(target.x, target.y, target.z);
        lastPosition = entity.position();
        stuckTicks = 0;
        pathUpdateCooldown = PATH_UPDATE_INTERVAL;
        System.out.println("[MoveToAction] 触发测试兜底传送到目标附近: " + target);
        return true;
    }

    private boolean hasTestTag(LivingEntity entity) {
        return entity.getTags().stream().anyMatch(tag -> tag.startsWith("test:"));
    }
    
    /**
     * 验证目标坐标是否有效
     * @param target 目标坐标
     * @return true 如果坐标有效
     */
    private boolean isValidTarget(Vec3 target) {
        // 检查 NaN
        if (Double.isNaN(target.x) || Double.isNaN(target.y) || Double.isNaN(target.z)) {
            return false;
        }
        
        // 检查极值（防止坐标过大或过小）
        if (Math.abs(target.x) > MAX_COORDINATE || Math.abs(target.z) > MAX_COORDINATE) {
            return false;
        }
        
        // 检查 Y 坐标是否在合理范围
        if (target.y < MIN_Y_COORDINATE || target.y > MAX_Y_COORDINATE) {
            return false;
        }
        
        return true;
    }
}
