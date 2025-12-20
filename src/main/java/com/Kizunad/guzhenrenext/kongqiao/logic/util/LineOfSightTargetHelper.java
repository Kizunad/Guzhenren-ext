package com.Kizunad.guzhenrenext.kongqiao.logic.util;

import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 视线锁定目标工具。
 * <p>
 * 用于“主动技能/指向性效果”根据玩家视线，在一定距离内找到最贴近视线的目标实体。
 * </p>
 */
public final class LineOfSightTargetHelper {

    private LineOfSightTargetHelper() {}

    /**
     * 在玩家视线方向上，寻找最贴近视线的可见目标（LivingEntity）。
     *
     * @param player 施法者
     * @param range  最大距离（方块）
     * @return 目标实体；若不存在返回 null
     */
    public static LivingEntity findTarget(
        final ServerPlayer player,
        final double range
    ) {
        Objects.requireNonNull(player, "player");

        final double actualRange = Math.max(1.0, range);
        final Vec3 start = player.getEyePosition();
        final Vec3 end = start.add(player.getViewVector(1.0F).scale(actualRange));
        final AABB searchBox = new AABB(start, end).inflate(1.0);

        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity entity : player.level().getEntitiesOfClass(
            LivingEntity.class,
            searchBox,
            e -> e.isAlive() && e != player && player.hasLineOfSight(e)
        )) {
            final double score = distanceSquaredToSegment(
                entity.getEyePosition(),
                start,
                end
            );
            if (score < bestScore) {
                bestScore = score;
                best = entity;
            }
        }

        return best;
    }

    private static double distanceSquaredToSegment(
        final Vec3 point,
        final Vec3 start,
        final Vec3 end
    ) {
        final Vec3 ab = end.subtract(start);
        final Vec3 ap = point.subtract(start);
        final double abLen2 = ab.lengthSqr();
        if (abLen2 <= 0.0) {
            return ap.lengthSqr();
        }
        double t = ap.dot(ab) / abLen2;
        t = UsageMetadataHelper.clamp(t, 0.0, 1.0);
        final Vec3 projection = start.add(ab.scale(t));
        return point.subtract(projection).lengthSqr();
    }
}

