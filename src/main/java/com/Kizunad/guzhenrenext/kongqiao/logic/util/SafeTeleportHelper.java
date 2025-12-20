package com.Kizunad.guzhenrenext.kongqiao.logic.util;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 安全传送工具。
 * <p>
 * 用于“短距挪移/换位/闪避”等宇道类效果，避免把实体传送到方块内部导致卡位窒息。
 * </p>
 */
public final class SafeTeleportHelper {

    private static final double[] Y_OFFSETS = new double[] {0.0, 0.5, 1.0};
    private static final double MIN_DISTANCE_SQR = 0.0001;

    private SafeTeleportHelper() {}

    /**
     * 在给定目标点附近寻找可站立/可碰撞通过的位置。
     * <p>
     * 规则：尝试若干 y 偏移，使实体包围盒在新位置不碰撞，同时在世界边界内。
     * 找不到则返回实体当前位置。
     * </p>
     */
    public static Vec3 findSafeTeleportPos(
        final LivingEntity entity,
        final Vec3 base
    ) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(base, "base");

        if (!(entity.level() instanceof ServerLevel level)) {
            return entity.position();
        }

        for (double yOffset : Y_OFFSETS) {
            final Vec3 candidate = base.add(0.0, yOffset, 0.0);
            final AABB moved = entity.getBoundingBox().move(
                candidate.x - entity.getX(),
                candidate.y - entity.getY(),
                candidate.z - entity.getZ()
            );
            if (!level.noCollision(entity, moved)) {
                continue;
            }
            final BlockPos pos = BlockPos.containing(candidate);
            if (!level.getWorldBorder().isWithinBounds(pos)) {
                continue;
            }
            return candidate;
        }

        return entity.position();
    }

    /**
     * 将实体传送到目标点附近的“安全位置”。
     *
     * @return 是否发生了有效传送
     */
    public static boolean teleportSafely(
        final LivingEntity entity,
        final Vec3 base
    ) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(base, "base");

        final Vec3 safe = findSafeTeleportPos(entity, base);
        if (safe.distanceToSqr(entity.position()) <= MIN_DISTANCE_SQR) {
            return false;
        }
        entity.teleportTo(safe.x, safe.y, safe.z);
        entity.resetFallDistance();
        return true;
    }
}
