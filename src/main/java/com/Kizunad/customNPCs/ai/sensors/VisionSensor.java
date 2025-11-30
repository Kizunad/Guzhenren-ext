package com.Kizunad.customNPCs.ai.sensors;

import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.ai.util.EntityRelationUtil;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
    private static final int SCAN_INTERVAL_THREAT = 2;
    private static final int SCAN_INTERVAL_VISIBLE = 10;
    private static final int SCAN_INTERVAL_HIDDEN = 20;
    private static final float NEAR_THREAT_DISTANCE = 5.0f;
    private static final int CRITICAL_DEDUP_WINDOW = 10;
    private static final int IMPORTANT_DEDUP_WINDOW = 25;
    private static final String MEMORY_VISIBLE_COUNT = "visible_entities_count";
    private static final String MEMORY_NEAREST_ENTITY = "nearest_entity";
    private static final String MEMORY_NEAREST_ENTITY_TYPE = "nearest_entity_type";
    private static final String MEMORY_NEAREST_ENTITY_DISTANCE = "nearest_entity_distance";
    private static final String MEMORY_HOSTILE_COUNT = "hostile_entities_count";
    private static final String MEMORY_NEAREST_HOSTILE = "nearest_hostile";
    private static final String MEMORY_NEAREST_HOSTILE_DISTANCE =
        "nearest_hostile_distance";
    private static final String MEMORY_ALLY_COUNT = "ally_entities_count";
    private static final String MEMORY_NEAREST_ALLY = "nearest_ally";

    private final double range;
    private int currentScanInterval = SCAN_INTERVAL_VISIBLE; // 默认 10 ticks
    private final InterruptThrottle interruptThrottle =
        new InterruptThrottle(
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

    @Override
    public void sense(INpcMind mind, LivingEntity entity, ServerLevel level) {
        Vec3 position = entity.position();
        AABB searchBox = new AABB(position, position).inflate(range);

        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
            LivingEntity.class,
            searchBox,
            e -> isValidEntity(entity, e)
        );

        mind.getMemory().forget(MEMORY_VISIBLE_COUNT);
        mind.getMemory().forget(MEMORY_NEAREST_ENTITY);

        if (nearbyEntities.isEmpty()) {
            rememberVisibleCount(mind, 0);
            setScanInterval(SCAN_INTERVAL_HIDDEN);
            return;
        }

        List<LivingEntity> visibleEntities = findVisibleEntities(
            entity,
            nearbyEntities,
            level
        );
        List<LivingEntity> hostiles = visibleEntities
            .stream()
            .filter(target -> EntityRelationUtil.isHostileTo(entity, target))
            .toList();
        List<LivingEntity> allies = visibleEntities
            .stream()
            .filter(target -> EntityRelationUtil.isAlly(entity, target))
            .toList();

        boolean hasVisible = !visibleEntities.isEmpty();
        boolean hasThreat = !hostiles.isEmpty();

        adjustScanInterval(hasThreat, hasVisible);
        logVisibleDebug(visibleEntities);
        rememberVisibleCount(mind, visibleEntities.size());

        LivingEntity nearest = findNearest(entity, visibleEntities);
        rememberNearest(mind, entity, nearest);

        LivingEntity nearestHostile = updateHostiles(
            mind,
            entity,
            hostiles
        );
        updateAllies(mind, entity, allies);

        LivingEntity interruptTarget =
            nearestHostile != null ? nearestHostile : nearest;
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
                    observer.distanceTo(nearestHostile),
                    MEMORY_DURATION
                );
            mind
                .getMemory()
                .rememberShortTerm(
                    "threat_detected",
                    true,
                    MEMORY_DURATION
                );
        } else {
            mind.getMemory().forget(MEMORY_NEAREST_HOSTILE);
            mind.getMemory().forget(MEMORY_NEAREST_HOSTILE_DISTANCE);
            mind.getMemory().forget("threat_detected");
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
            eventType =
                distance < NEAR_THREAT_DISTANCE
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
