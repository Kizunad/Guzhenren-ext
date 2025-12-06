package com.Kizunad.customNPCs.ai.sensors;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.ai.util.EntityRelationUtil;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 视觉传感器 - 让 NPC 能"看到"周围的实体
 * <p>
 * 功能：
 * - 扫描视野范围内的实体
 * - 视线检测（Line of Sight）
 * - 将看到的**实体**信息存入记忆
 */
public class VisionSensor implements ISensor {

    private static final double DEFAULT_RANGE = 16.0D; // 默认视野范围（格）
    private static final int MEMORY_DURATION = 100; // 记忆持续时间（5秒）
    private static final int SENSOR_PRIORITY = 10; // 传感器优先级
    private static final int SCAN_INTERVAL_THREAT = 2; // 发现威胁时的扫描间隔（ticks）
    private static final int SCAN_INTERVAL_VISIBLE = 10; // 发现可见实体时的扫描间隔（ticks）
    private static final int SCAN_INTERVAL_HIDDEN = 20; // 未发现实体时的扫描间隔（ticks）
    private static final float NEAR_THREAT_DISTANCE = 5.0f; // 近距离威胁判定阈值（格）
    private static final int CRITICAL_DEDUP_WINDOW = 10; // 关键实体去重窗口（ticks）
    private static final int IMPORTANT_DEDUP_WINDOW = 25; // 重要实体去重窗口（ticks）
    private static final String MEMORY_VISIBLE_COUNT = "visible_entities_count";
    private static final String MEMORY_NEAREST_ENTITY = "nearest_entity";
    private static final String MEMORY_NEAREST_ENTITY_TYPE =
        "nearest_entity_type";
    private static final String MEMORY_NEAREST_ENTITY_DISTANCE =
        "nearest_entity_distance";
    private static final String MEMORY_HOSTILE_COUNT = "hostile_entities_count";
    private static final String MEMORY_NEAREST_HOSTILE = "nearest_hostile";
    private static final String MEMORY_NEAREST_HOSTILE_DISTANCE =
        "nearest_hostile_distance";
    private static final String MEMORY_THREAT_DISTANCE_BUCKET =
        "target_distance_bucket";
    private static final String MEMORY_ALLY_COUNT = "ally_entities_count";
    private static final String MEMORY_NEAREST_ALLY = "nearest_ally";

    private final double range;
    private int currentScanInterval = SCAN_INTERVAL_VISIBLE; // 默认 10 ticks
    private final InterruptThrottle interruptThrottle = new InterruptThrottle(
        CRITICAL_DEDUP_WINDOW,
        IMPORTANT_DEDUP_WINDOW,
        IMPORTANT_DEDUP_WINDOW
    );

    public VisionSensor() {
        this(DEFAULT_RANGE);
    }

    public VisionSensor(double range) {
        this.range = range;
    }

    @Override
    public String getName() {
        return "vision";
    }

    @Override
    public int getScanInterval() {
        return currentScanInterval;
    }

    @Override
    public void setScanInterval(int ticks) {
        this.currentScanInterval = Math.max(1, ticks);
    }

    /**
     * 执行视觉感知核心扫描逻辑。
     * <p>
     * 详细流程：
     * <ol>
     * <li>基于 NPC 位置和视野范围计算搜索盒子（AABB）。</li>
     * <li>查询盒子内所有有效的附近活体实体（排除自身）。</li>
     * <li>清空旧的可见实体相关记忆。</li>
     * <li>对附近实体进行视线（LineOfSight）检查，筛选出真正可见实体。</li>
     * <li>将可见实体分类为敌对（hostile）和盟友（ally）。</li>
     * <li>根据威胁情况动态调整下次扫描间隔（威胁时高频，无威胁时低频）。</li>
     * <li>记录记忆：可见数量、最近实体、敌对数量/最近敌对、盟友数量等。</li>
     * <li>调试日志输出可见实体信息。</li>
     * <li>检查最近威胁/可见实体，若符合条件触发中断（CRITICAL 或 IMPORTANT 级别）。</li>
     * </ol>
     * <p>
     * 优化点：使用 Stream API 高效过滤；去重节流避免频繁中断；记忆短期存储（默认 5 秒）。
     *
     * @param mind NPC 思维组件，用于记忆读写和中断触发
     * @param entity NPC 实体（观察者/扫描主体）
     * @param level 服务器世界实例，用于实体查询、射线碰撞检测
     */
    @Override
    public void sense(INpcMind mind, LivingEntity entity, ServerLevel level) {
        // 步骤1: 计算 NPC 当前位置和搜索范围盒子
        Vec3 position = entity.position();
        AABB searchBox = new AABB(position, position).inflate(range);

        // 额外扫描坐骑候选，避免错过静止载具
        scanMountCandidates(mind, entity, level, searchBox);

        // 步骤2: 查询附近所有有效活体实体（使用过滤器排除无效目标）
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
            LivingEntity.class,
            searchBox,
            e -> isValidEntity(entity, e)
        );

        // 步骤3: 清空上次的可见实体相关记忆，避免残留
        mind.getMemory().forget(MEMORY_VISIBLE_COUNT);
        mind.getMemory().forget(MEMORY_NEAREST_ENTITY);

        // 快速退出：无附近实体时记录0可见并延长扫描间隔
        if (nearbyEntities.isEmpty()) {
            rememberVisibleCount(mind, 0);
            setScanInterval(SCAN_INTERVAL_HIDDEN);
            return;
        }

        // 步骤4: 视线检查，筛选真正可见实体
        List<LivingEntity> visibleEntities = findVisibleEntities(
            entity,
            nearbyEntities,
            level
        );

        // 步骤5: 分类可见实体为敌对和盟友
        List<LivingEntity> hostiles = visibleEntities
            .stream()
            .filter(target -> EntityRelationUtil.isHostileTo(entity, target))
            .toList();
        List<LivingEntity> allies = visibleEntities
            .stream()
            .filter(target -> EntityRelationUtil.isAlly(entity, target))
            .toList();

        // 判断是否有可见/威胁，提升感知优先级
        boolean hasVisible = !visibleEntities.isEmpty();
        boolean hasThreat = !hostiles.isEmpty();

        // 步骤6: 动态调整扫描间隔（威胁>可见>隐藏）
        adjustScanInterval(hasThreat, hasVisible);

        // 步骤7: 输出调试日志（仅开发时可见）
        logVisibleDebug(visibleEntities);

        // 步骤8: 记忆可见实体总数
        rememberVisibleCount(mind, visibleEntities.size());

        // 步骤9: 找出并记忆最近的可见实体（ID、类型、距离）
        LivingEntity nearest = findNearest(entity, visibleEntities);
        rememberNearest(mind, entity, nearest);

        // 步骤10: 更新敌对实体记忆（数量、最近敌对、距离桶、威胁状态等）
        LivingEntity nearestHostile = updateHostiles(mind, entity, hostiles);

        // 步骤11: 更新盟友实体记忆（数量、最近盟友）
        updateAllies(mind, entity, allies);

        // 步骤12: 优先敌对，其次最近可见；检查是否触发中断
        LivingEntity interruptTarget = nearestHostile != null
            ? nearestHostile
            : nearest;
        if (interruptTarget != null) {
            float interruptDistance = entity.distanceTo(interruptTarget);
            triggerInterruptIfNeeded(
                mind,
                entity,
                interruptTarget,
                interruptDistance,
                level
            );
        }
    }

    private void scanMountCandidates(
        INpcMind mind,
        LivingEntity observer,
        ServerLevel level,
        AABB searchBox
    ) {
        List<Entity> mounts = level.getEntities(
            observer,
            searchBox,
            candidate -> isMountCandidate(observer, candidate)
        );
        Entity nearestMount = null;
        double bestDistanceSq = Double.MAX_VALUE;
        for (Entity mount : mounts) {
            double distanceSq = observer.distanceToSqr(mount.position());
            if (distanceSq < bestDistanceSq) {
                bestDistanceSq = distanceSq;
                nearestMount = mount;
            }
        }

        if (nearestMount != null) {
            mind
                .getMemory()
                .rememberShortTerm(
                    WorldStateKeys.HAS_MOUNT_NEARBY,
                    true,
                    MEMORY_DURATION
                );
            mind
                .getMemory()
                .rememberShortTerm(
                    WorldStateKeys.MOUNT_UUID,
                    nearestMount.getUUID(),
                    MEMORY_DURATION
                );
            mind
                .getMemory()
                .rememberShortTerm(
                    WorldStateKeys.MOUNT_TYPE,
                    resolveMountType(nearestMount),
                    MEMORY_DURATION
                );
        } else {
            mind.getMemory().forget(WorldStateKeys.HAS_MOUNT_NEARBY);
            mind.getMemory().forget(WorldStateKeys.MOUNT_UUID);
            mind.getMemory().forget(WorldStateKeys.MOUNT_TYPE);
        }
    }

    private boolean isMountCandidate(LivingEntity observer, Entity candidate) {
        if (candidate == null || candidate == observer || candidate.isRemoved()) {
            return false;
        }
        if (!candidate.isAlive() || !candidate.getPassengers().isEmpty()) {
            return false;
        }
        if (candidate instanceof AbstractHorse horse) {
            return horse.isTamed() && !horse.isBaby();
        }
        return candidate instanceof Boat || candidate instanceof AbstractMinecart;
    }

    private String resolveMountType(Entity mount) {
        if (mount instanceof AbstractHorse) {
            return "horse";
        }
        if (mount instanceof Boat) {
            return "boat";
        }
        if (mount instanceof AbstractMinecart) {
            return "minecart";
        }
        return mount.getType().toString();
    }

    private List<LivingEntity> findVisibleEntities(
        LivingEntity observer,
        List<LivingEntity> nearbyEntities,
        ServerLevel level
    ) {
        return nearbyEntities
            .stream()
            .filter(target -> hasLineOfSight(observer, target, level))
            .toList();
    }

    private void adjustScanInterval(boolean hasThreat, boolean hasVisible) {
        if (hasThreat) {
            setScanInterval(SCAN_INTERVAL_THREAT);
        } else if (hasVisible) {
            setScanInterval(SCAN_INTERVAL_VISIBLE);
        } else {
            setScanInterval(SCAN_INTERVAL_HIDDEN);
        }
    }

    private void logVisibleDebug(List<LivingEntity> visibleEntities) {
        if (visibleEntities.isEmpty()) {
            return;
        }
        MindLog.decision(
            MindLogLevel.DEBUG,
            "可见实体数量: {}",
            visibleEntities.size()
        );
        for (LivingEntity visible : visibleEntities) {
            MindLog.decision(
                MindLogLevel.DEBUG,
                "  - {} at {}",
                visible.getType().getDescription().getString(),
                visible.blockPosition().toShortString()
            );
        }
    }

    private void rememberVisibleCount(INpcMind mind, int count) {
        mind
            .getMemory()
            .rememberShortTerm(MEMORY_VISIBLE_COUNT, count, MEMORY_DURATION);
    }

    private LivingEntity findNearest(
        LivingEntity observer,
        List<LivingEntity> candidates
    ) {
        return candidates
            .stream()
            .min((a, b) ->
                Double.compare(
                    observer.distanceToSqr(a),
                    observer.distanceToSqr(b)
                )
            )
            .orElse(null);
    }

    private void rememberNearest(
        INpcMind mind,
        LivingEntity observer,
        LivingEntity nearest
    ) {
        if (nearest == null) {
            return;
        }
        mind
            .getMemory()
            .rememberShortTerm(
                MEMORY_NEAREST_ENTITY,
                nearest.getUUID().toString(),
                MEMORY_DURATION
            );
        mind
            .getMemory()
            .rememberShortTerm(
                MEMORY_NEAREST_ENTITY_TYPE,
                nearest.getType().toString(),
                MEMORY_DURATION
            );
        mind
            .getMemory()
            .rememberShortTerm(
                MEMORY_NEAREST_ENTITY_DISTANCE,
                observer.distanceTo(nearest),
                MEMORY_DURATION
            );
    }

    private LivingEntity updateHostiles(
        INpcMind mind,
        LivingEntity observer,
        List<LivingEntity> hostiles
    ) {
        mind
            .getMemory()
            .rememberShortTerm(
                MEMORY_HOSTILE_COUNT,
                hostiles.size(),
                MEMORY_DURATION
            );
        LivingEntity nearestHostile = findNearest(observer, hostiles);
        if (nearestHostile != null) {
            double distance = observer.distanceTo(nearestHostile);
            int distanceBucket = getDistanceBucket((float) distance);
            mind
                .getMemory()
                .rememberShortTerm(
                    MEMORY_NEAREST_HOSTILE,
                    nearestHostile.getUUID(),
                    MEMORY_DURATION
                );
            mind
                .getMemory()
                .rememberShortTerm(
                    MEMORY_NEAREST_HOSTILE_DISTANCE,
                    distance,
                    MEMORY_DURATION
                );
            mind
                .getMemory()
                .rememberShortTerm("threat_detected", true, MEMORY_DURATION);
            mind
                .getMemory()
                .rememberShortTerm(
                    WorldStateKeys.TARGET_VISIBLE,
                    true,
                    MEMORY_DURATION
                );
            mind
                .getMemory()
                .rememberShortTerm(
                    WorldStateKeys.TARGET_IN_RANGE,
                    distance <= NEAR_THREAT_DISTANCE,
                    MEMORY_DURATION
                );
            mind
                .getMemory()
                .rememberShortTerm(
                    WorldStateKeys.DISTANCE_TO_TARGET,
                    distance,
                    MEMORY_DURATION
                );
            mind
                .getMemory()
                .rememberShortTerm(
                    MEMORY_THREAT_DISTANCE_BUCKET,
                    distanceBucket,
                    MEMORY_DURATION
                );
            mind
                .getMemory()
                .rememberShortTerm(
                    "current_threat_id",
                    nearestHostile.getUUID(),
                    MEMORY_DURATION
                );
            mind
                .getMemory()
                .rememberShortTerm(
                    WorldStateKeys.HOSTILE_NEARBY,
                    true,
                    MEMORY_DURATION
                );
        } else {
            mind.getMemory().forget(MEMORY_NEAREST_HOSTILE);
            mind.getMemory().forget(MEMORY_NEAREST_HOSTILE_DISTANCE);
            mind.getMemory().forget("threat_detected");
            mind.getMemory().forget(WorldStateKeys.TARGET_VISIBLE);
            mind.getMemory().forget(WorldStateKeys.TARGET_IN_RANGE);
            mind.getMemory().forget(WorldStateKeys.DISTANCE_TO_TARGET);
            mind.getMemory().forget(MEMORY_THREAT_DISTANCE_BUCKET);
            mind.getMemory().forget("current_threat_id");
            mind.getMemory().forget(WorldStateKeys.HOSTILE_NEARBY);
        }
        return nearestHostile;
    }

    private void updateAllies(
        INpcMind mind,
        LivingEntity observer,
        List<LivingEntity> allies
    ) {
        mind
            .getMemory()
            .rememberShortTerm(
                MEMORY_ALLY_COUNT,
                allies.size(),
                MEMORY_DURATION
            );
        LivingEntity nearestAlly = findNearest(observer, allies);
        if (nearestAlly != null) {
            mind
                .getMemory()
                .rememberShortTerm(
                    MEMORY_NEAREST_ALLY,
                    nearestAlly.getUUID(),
                    MEMORY_DURATION
                );
        } else {
            mind.getMemory().forget(MEMORY_NEAREST_ALLY);
        }
    }

    @Override
    public int getPriority() {
        return SENSOR_PRIORITY; // 视觉是重要的感知，高优先级
    }

    /**
     * 检查实体是否是有效的感知目标
     * @param observer 观察者
     * @param target 目标
     * @return 是否有效
     */
    protected boolean isValidEntity(
        LivingEntity observer,
        LivingEntity target
    ) {
        return target != observer && target.isAlive();
    }

    /**
     * 检查两个实体之间是否有视线
     * @param observer 观察者
     * @param target 目标
     * @param level 世界
     * @return 是否能看到
     */
    private boolean hasLineOfSight(
        LivingEntity observer,
        Entity target,
        ServerLevel level
    ) {
        Vec3 eyePos = observer.getEyePosition();
        Vec3 targetPos = target.getEyePosition();

        // 射线检测
        ClipContext context = new ClipContext(
            eyePos,
            targetPos,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            observer
        );

        HitResult result = level.clip(context);

        // 如果射线没有击中方块，说明视线畅通
        return result.getType() == HitResult.Type.MISS;
    }

    /**
     * 根据检测到的实体触发适当的中断
     * <p>
     * 触发规则:
     * - 玩家 (Player): IMPORTANT 级别
     * - 敌对生物且距离 < 5格: CRITICAL 级别
     * - 敌对生物且距离 >= 5格: IMPORTANT 级别
     *
     * @param mind NPC 思维
     * @param observer 观察者实体
     * @param target 检测到的目标实体
     * @param distance 距离
     * @param level 服务器世界
     */
    private void triggerInterruptIfNeeded(
        INpcMind mind,
        LivingEntity observer,
        LivingEntity target,
        float distance,
        ServerLevel level
    ) {
        int distanceBucket = getDistanceBucket(distance);
        long gameTime = level.getGameTime();
        UUID targetId = target.getUUID();

        SensorEventType eventType = null;

        // 检测玩家
        if (target instanceof net.minecraft.world.entity.player.Player) {
            eventType = SensorEventType.IMPORTANT;
        }

        // 检测敌对生物
        if (eventType == null && isHostile(target, observer)) {
            eventType = distance < NEAR_THREAT_DISTANCE
                ? SensorEventType.CRITICAL
                : SensorEventType.IMPORTANT;
        }

        if (eventType == null) {
            return;
        }

        if (
            !interruptThrottle.allowInterrupt(
                targetId,
                eventType,
                distanceBucket,
                gameTime
            )
        ) {
            return;
        }

        mind.triggerInterrupt(observer, eventType);

        MindLog.decision(
            MindLogLevel.INFO,
            "检测到目标 {}@{} (bucket {}),触发 {} 中断",
            target.getType().getDescription().getString(),
            String.format("%.1f", distance),
            distanceBucket,
            eventType
        );
    }

    private int getDistanceBucket(float distance) {
        return distance < NEAR_THREAT_DISTANCE ? 0 : 1;
    }

    /**
     * 判断目标是否对观察者敌对
     *
     * @param target 目标实体
     * @param observer 观察者
     * @return 是否敌对
     */
    private boolean isHostile(LivingEntity target, LivingEntity observer) {
        return EntityRelationUtil.isHostileTo(observer, target);
    }
}
