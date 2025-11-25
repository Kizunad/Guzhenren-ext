package com.Kizunad.customNPCs.ai.sensors;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.List;
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
    private static final int SCAN_INTERVAL = 5; // 每 5 ticks 扫描一次
    private static final int MEMORY_DURATION = 100; // 记忆持续时间（5秒）
    private static final int SENSOR_PRIORITY = 10; // 传感器优先级

    private final double range;

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
    public void sense(INpcMind mind, LivingEntity entity, ServerLevel level) {
        // 获取扫描范围
        Vec3 position = entity.position();
        AABB searchBox = new AABB(position, position).inflate(range);

        // 扫描范围内的所有生物
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
            LivingEntity.class,
            searchBox,
            e -> isValidEntity(entity, e)
        );

        // 清除旧的视觉记忆
        mind.getMemory().forget("visible_entities_count");
        mind.getMemory().forget("nearest_entity");

        if (nearbyEntities.isEmpty()) {
            mind
                .getMemory()
                .rememberShortTerm(
                    "visible_entities_count",
                    0,
                    MEMORY_DURATION
                );
            return;
        }

        // 过滤：只保留在视线内的实体
        List<LivingEntity> visibleEntities = nearbyEntities
            .stream()
            .filter(target -> hasLineOfSight(entity, target, level))
            .toList();

        // DEBUG
        if (!visibleEntities.isEmpty()) {
            System.out.println(
                "[VisionSensor] Visible entities: " + visibleEntities.size()
            );
            for (LivingEntity e : visibleEntities) {
                System.out.println(
                    "  - " +
                        e.getType().getDescription().getString() +
                        " at " +
                        e.blockPosition().toShortString()
                );
            }
        }

        // 存储可见实体数量
        mind
            .getMemory()
            .rememberShortTerm(
                "visible_entities_count",
                visibleEntities.size(),
                MEMORY_DURATION
            );

        if (!visibleEntities.isEmpty()) {
            // 找到最近的实体
            LivingEntity nearest = visibleEntities
                .stream()
                .min((a, b) ->
                    Double.compare(
                        entity.distanceToSqr(a),
                        entity.distanceToSqr(b)
                    )
                )
                .orElse(null);

            if (nearest != null) {
                // 存储最近实体的信息
                mind
                    .getMemory()
                    .rememberShortTerm(
                        "nearest_entity",
                        nearest.getUUID().toString(),
                        MEMORY_DURATION
                    );
                mind
                    .getMemory()
                    .rememberShortTerm(
                        "nearest_entity_type",
                        nearest.getType().toString(),
                        MEMORY_DURATION
                    );
                mind
                    .getMemory()
                    .rememberShortTerm(
                        "nearest_entity_distance",
                        entity.distanceTo(nearest),
                        MEMORY_DURATION
                    );
            }
        }
    }

    @Override
    public boolean shouldSense(long tickCount) {
        // 每 5 ticks 扫描一次以优化性能
        return tickCount % SCAN_INTERVAL == 0;
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
}
