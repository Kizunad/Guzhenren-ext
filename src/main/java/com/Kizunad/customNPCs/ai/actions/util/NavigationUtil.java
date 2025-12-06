package com.Kizunad.customNPCs.ai.actions.util;

import com.Kizunad.customNPCs.ai.actions.config.ActionConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 导航工具类 - 统一管理导航相关逻辑
 * <p>
 * 功能：
 * - 统一的到达判定（距离检测）
 * - 粘性导航策略（移动目标跟踪）
 * - 寻路限流（每N ticks更新一次路径）
 * - 卡住检测
 */
public final class NavigationUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        NavigationUtil.class
    );
    private static final ActionConfig CONFIG = ActionConfig.getInstance();

    /**
     * 到达判定缓冲（避免寻路停在边缘时误判失败）
     */
    private static final double ARRIVAL_BUFFER = 0.75;

    /**
     * 最小移动距离（用于卡住检测）
     */
    private static final double MIN_MOVEMENT = 0.1;
    /**
     * 最小距离（用于卡住检测）
     */
    private static final double MIN_DISTANCE = 0.001d;
    /**
     * 高速兜底传送的速度阈值
     */
    private static final double SPEED_TELEPORT_THRESHOLD = 1.5;
    /**
     * 高速兜底传送的基础距离
     */
    private static final double SPEED_TELEPORT_BASE_RANGE = 2.0;
    /**
     * 每提升 1.0 速度可增加的传送距离
     */
    private static final double SPEED_TELEPORT_RANGE_PER_SPEED = 2.5;
    /**
     * 高速传送的最大可用距离
     */
    private static final double SPEED_TELEPORT_MAX_RANGE = 7.0;
    /**
     * 触发传送所需的最小距离
     */
    private static final double SPEED_TELEPORT_MIN_STEP = 0.75;
    /**
     * 检测安全落点时的采样高度
     */
    private static final double[] SPEED_TELEPORT_VERTICAL_OFFSETS =
        new double[] { 0.0, 1.0, -1.0, 2.0, -2.0 };

    /**
     * 私有构造函数（工具类）
     */
    private NavigationUtil() {}

    /**
     * 检查是否在范围内（标准到达判定）
     * @param entityPos 实体当前位置
     * @param targetPos 目标位置
     * @param threshold 距离阈值
     * @return true 如果在范围内
     */
    public static boolean isInRange(
        Vec3 entityPos,
        Vec3 targetPos,
        double threshold
    ) {
        double distance = entityPos.distanceTo(targetPos);
        return distance <= threshold;
    }

    /**
     * 检查是否已到达（带缓冲的到达判定）
     * <p>
     * 使用 ARRIVAL_BUFFER 避免寻路系统停在边缘时误判失败
     * @param entityPos 实体当前位置
     * @param targetPos 目标位置
     * @param acceptableDistance 可接受的到达距离
     * @return true 如果已到达
     */
    public static boolean hasArrived(
        Vec3 entityPos,
        Vec3 targetPos,
        double acceptableDistance
    ) {
        double distance = entityPos.distanceTo(targetPos);
        return distance <= acceptableDistance + ARRIVAL_BUFFER;
    }

    /**
     * 检查目标实体是否可达。
     * <p>
     * 使用一次性的 createPath 计算，不会启动实际导航。
     *
     * @param mob   当前实体
     * @param target 目标实体
     * @param maxDistance 最大允许距离（<=0 则忽略距离限制）
     * @return true 表示可达
     */
    public static boolean canReachEntity(
        Mob mob,
        Entity target,
        double maxDistance
    ) {
        if (mob == null || target == null) {
            return false;
        }
        if (!mob.level().equals(target.level())) {
            return false;
        }
        if (maxDistance > 0 && mob.distanceTo(target) > maxDistance) {
            return false;
        }
        PathNavigation navigation = mob.getNavigation();
        Path path = navigation.createPath(target, 0);
        return path != null && path.canReach();
    }

    /**
     * 检查坐标是否可达。
     * <p>
     * 仅用于预判，避免持续尝试不可达位置。
     *
     * @param mob 当前实体
     * @param targetPos 目标坐标
     * @return true 表示存在可达路径
     */
    public static boolean canReachPosition(Mob mob, Vec3 targetPos) {
        if (mob == null || targetPos == null) {
            return false;
        }
        PathNavigation navigation = mob.getNavigation();
        BlockPos blockPos = BlockPos.containing(targetPos);
        Path path = navigation.createPath(blockPos, 0);
        return path != null && path.canReach();
    }

    /**
     * 检查实体是否卡住
     * @param currentPos 当前位置
     * @param lastPos 上次位置
     * @param stuckTicks 已卡住的tick数（累计）
     * @param maxStuckTicks 最大允许卡住tick数
     * @return true 如果卡住且超过阈值
     */
    public static boolean isStuck(
        Vec3 currentPos,
        Vec3 lastPos,
        int stuckTicks,
        int maxStuckTicks
    ) {
        if (lastPos == null) {
            return false;
        }

        double movementDistance = currentPos.distanceTo(lastPos);
        if (movementDistance < MIN_MOVEMENT) {
            return stuckTicks >= maxStuckTicks;
        }

        return false;
    }

    /**
     * 更新路径（带限流）
     * <p>
     * 根据配置的路径更新间隔决定是否更新路径。
     * 对于移动目标，使用粘性策略（更频繁更新）。
     * @param navigation PathNavigation实例
     * @param targetPos 目标位置
     * @param speed 移动速度
     * @param pathUpdateCooldown 当前冷却计数（会被递减）
     * @param isMovingTarget 是否是移动目标
     * @return 新的 pathUpdateCooldown 值
     */
    public static int updatePathWithThrottling(
        PathNavigation navigation,
        Vec3 targetPos,
        double speed,
        int pathUpdateCooldown,
        boolean isMovingTarget
    ) {
        // 获取配置的更新间隔
        int updateInterval = CONFIG.getPathUpdateInterval();

        // 移动目标使用更短的间隔（粘性策略）
        if (isMovingTarget) {
            updateInterval = Math.max(updateInterval / 2, 1); // 至少1 tick
        }

        // 冷却中，不更新
        if (pathUpdateCooldown > 0) {
            return pathUpdateCooldown - 1;
        }

        // 执行路径更新
        boolean pathCreated = navigation.moveTo(
            targetPos.x,
            targetPos.y,
            targetPos.z,
            speed
        );

        if (CONFIG.isDebugLoggingEnabled()) {
            LOGGER.debug(
                "[NavigationUtil] 路径更新: {} | 目标: {}",
                pathCreated ? "成功" : "失败",
                targetPos
            );
        }

        // 重置冷却
        return updateInterval;
    }

    /**
     * 粘性导航：优先导航到移动目标
     * <p>
     * 对于移动中的目标（如敌对实体），使用更高的更新频率
     * @param mob NPC实体
     * @param targetEntity 目标实体
     * @param speed 移动速度
     * @param pathUpdateCooldown 当前冷却
     * @return 新的冷却值
     */
    public static int stickyNavigateToEntity(
        Mob mob,
        Entity targetEntity,
        double speed,
        int pathUpdateCooldown
    ) {
        Vec3 targetPos = targetEntity.position();
        return updatePathWithThrottling(
            mob.getNavigation(),
            targetPos,
            speed,
            pathUpdateCooldown,
            true // 总是视为移动目标
        );
    }

    /**
     * 导航到固定位置
     * @param mob NPC实体
     * @param targetPos 目标位置
     * @param speed 移动速度
     * @param pathUpdateCooldown 当前冷却
     * @return 新的冷却值
     */
    public static int navigateToPosition(
        Mob mob,
        Vec3 targetPos,
        double speed,
        int pathUpdateCooldown
    ) {
        return updatePathWithThrottling(
            mob.getNavigation(),
            targetPos,
            speed,
            pathUpdateCooldown,
            false // 固定目标
        );
    }

    /**
     * 当速度过高导致寻路系统不稳定时，尝试进行短距离传送。
     * <p>
     * 仅在服务端生效：根据速度计算最大传送距离，沿水平向量逼近目标，
     * 并检测路径/落点是否被方块阻挡。
     *
     * @param mob NPC 实体
     * @param targetPos 目标位置
     * @param moveSpeed 当前设定速度
     * @param acceptableDistance 可接受的到达距离，用于避免过冲
     * @return true 表示传送成功
     */
    public static boolean trySpeedTeleport(
        Mob mob,
        Vec3 targetPos,
        double moveSpeed,
        double acceptableDistance
    ) {
        if (mob == null || targetPos == null) {
            return false;
        }
        if (mob.level().isClientSide()) {
            return false;
        }
        if (moveSpeed < SPEED_TELEPORT_THRESHOLD) {
            return false;
        }
        Vec3 start = mob.position();
        Vec3 horizontalDelta = new Vec3(
            targetPos.x - start.x,
            0.0,
            targetPos.z - start.z
        );
        double planarDistance = horizontalDelta.length();
        if (planarDistance < MIN_DISTANCE) {
            return false;
        }

        double allowedRange =
            SPEED_TELEPORT_BASE_RANGE +
            (moveSpeed - SPEED_TELEPORT_THRESHOLD) *
            SPEED_TELEPORT_RANGE_PER_SPEED;
        allowedRange = Mth.clamp(
            allowedRange,
            SPEED_TELEPORT_MIN_STEP,
            SPEED_TELEPORT_MAX_RANGE
        );

        double desiredDistance = Math.max(
            planarDistance - acceptableDistance,
            0.0
        );
        double teleportDistance = Math.min(desiredDistance, allowedRange);
        if (teleportDistance < SPEED_TELEPORT_MIN_STEP) {
            return false;
        }

        Vec3 horizontalDir = horizontalDelta.scale(1.0d / planarDistance);
        Vec3 projected = start.add(horizontalDir.scale(teleportDistance));
        Vec3 candidate = new Vec3(projected.x, targetPos.y, projected.z);

        Vec3 safePos = findSpeedTeleportSpot(mob, candidate);
        if (safePos == null) {
            safePos = findSpeedTeleportSpot(
                mob,
                new Vec3(projected.x, start.y, projected.z)
            );
        }
        if (safePos == null) {
            return false;
        }

        mob.teleportTo(safePos.x, safePos.y, safePos.z);
        mob.getNavigation().stop();

        if (CONFIG.isDebugLoggingEnabled()) {
            LOGGER.info(
                "[NavigationUtil] 高速传送: {} -> {} | 移动速度 {} | 实际距离 {}",
                start,
                safePos,
                String.format("%.2f", moveSpeed),
                String.format("%.2f", teleportDistance)
            );
        }

        return true;
    }

    private static Vec3 findSpeedTeleportSpot(Mob mob, Vec3 candidate) {
        for (double offset : SPEED_TELEPORT_VERTICAL_OFFSETS) {
            Vec3 adjusted = candidate.add(0.0, offset, 0.0);
            if (!isTeleportDestinationClear(mob, adjusted)) {
                continue;
            }
            if (!hasClearTravelPath(mob, adjusted)) {
                continue;
            }
            return adjusted;
        }
        return null;
    }

    private static boolean isTeleportDestinationClear(
        Mob mob,
        Vec3 destination
    ) {
        Vec3 delta = destination.subtract(mob.position());
        AABB shifted = mob.getBoundingBox().move(delta);
        return mob.level().noCollision(mob, shifted);
    }

    private static boolean hasClearTravelPath(Mob mob, Vec3 destination) {
        Vec3 startEye = mob.getEyePosition();
        Vec3 destEye = new Vec3(
            destination.x,
            destination.y + mob.getEyeHeight(),
            destination.z
        );
        ClipContext context = new ClipContext(
            startEye,
            destEye,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            mob
        );
        HitResult result = mob.level().clip(context);
        return result.getType() == HitResult.Type.MISS;
    }

    /**
     * 计算两个位置之间的距离
     * @param pos1 位置1
     * @param pos2 位置2
     * @return 距离
     */
    public static double distance(Vec3 pos1, Vec3 pos2) {
        return pos1.distanceTo(pos2);
    }

    /**
     * 检查目标是否在视线范围内（简化版本）
     * <p>
     * 注意：这只是简单的距离检查，真正的视线检查需要射线追踪
     * @param mob NPC实体
     * @param targetPos 目标位置
     * @param maxDistance 最大距离
     * @return true 如果在视线范围内
     */
    public static boolean canSeeTarget(
        Mob mob,
        Vec3 targetPos,
        double maxDistance
    ) {
        double distance = mob.position().distanceTo(targetPos);
        if (distance > maxDistance) {
            return false;
        }

        // 未来可添加射线追踪检查障碍物
        return true;
    }
}
