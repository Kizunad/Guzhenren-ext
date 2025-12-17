package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.network.ClientboundLingHunGuIntuitionPayload;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 羚魂蛊：被动【灵觉】。
 * <p>
 * 机制：危机预警——当有视线外的敌人正在锁定玩家（Mob.getTarget()）时，
 * 向客户端发送“方向提示”，由客户端在屏幕边缘渲染微弱白光。
 * </p>
 * <p>
 * 性能：每秒检测一次；只选取威胁度最高（最近）的一个方向提示，避免刷屏。
 * </p>
 */
public class LingHunGuSpiritualIntuitionEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:linghungu_passive_spiritual_intuition";

    private static final double DEFAULT_RADIUS = 24.0;
    private static final double DEFAULT_FOV_DOT_THRESHOLD = 0.45;
    private static final int DEFAULT_HINT_DURATION_TICKS = 8;

    private static final double DEFAULT_BASE_SOUL_COST = 0.0;
    private static final double DEFAULT_SOUL_COST_PER_THREAT = 0.0;
    private static final double DIRECTION_EPSILON_SQR = 0.0001;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public void onSecond(
        LivingEntity user,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (user.level().isClientSide()) {
            return;
        }
        if (!(user instanceof ServerPlayer player)) {
            return;
        }

        final TweakConfig config = KongqiaoAttachments.getTweakConfig(user);
        if (config != null && !config.isPassiveEnabled(USAGE_ID)) {
            return;
        }

        final double baseRadius = getMetaDouble(
            usageInfo,
            "radius",
            DEFAULT_RADIUS
        );
        final double fengDaoMultiplier = DaoHenCalculator.calculateSelfMultiplier(
            player,
            DaoHenHelper.DaoType.FENG_DAO
        );
        final double radius = baseRadius * fengDaoMultiplier;

        final double dotThreshold = getMetaDouble(
            usageInfo,
            "fov_dot_threshold",
            DEFAULT_FOV_DOT_THRESHOLD
        );
        final int duration = getMetaInt(
            usageInfo,
            "hint_duration_ticks",
            DEFAULT_HINT_DURATION_TICKS
        );

        final AABB area = player.getBoundingBox().inflate(radius, radius, radius);
        final List<Mob> mobs = player.level().getEntitiesOfClass(Mob.class, area);

        Mob bestThreat = null;
        double bestDist2 = Double.MAX_VALUE;
        int threatCount = 0;

        for (Mob mob : mobs) {
            if (mob == null || !mob.isAlive()) {
                continue;
            }
            if (mob.getTarget() != player) {
                continue;
            }
            if (mob.isAlliedTo(player)) {
                continue;
            }

            final Vec3 to = mob.position().subtract(player.position());
            if (to.lengthSqr() <= DIRECTION_EPSILON_SQR) {
                continue;
            }

            final Vec3 look = player.getLookAngle().normalize();
            final Vec3 dir = to.normalize();
            final double dot = look.dot(dir);
            final boolean outOfFov = dot < dotThreshold;
            final boolean blocked = !player.hasLineOfSight(mob);
            if (!outOfFov && !blocked) {
                continue;
            }

            threatCount++;
            final double dist2 = to.lengthSqr();
            if (dist2 < bestDist2) {
                bestDist2 = dist2;
                bestThreat = mob;
            }
        }

        if (bestThreat == null) {
            return;
        }

        if (!tryConsumeSoul(player, usageInfo, threatCount, fengDaoMultiplier)) {
            return;
        }

        final Vec3 to = bestThreat.position().subtract(player.position());
        final float angle = (float) wrapRadians(
            Math.atan2(to.x, to.z) - Math.toRadians(player.getYRot())
        );

        final float intensity = (float) clamp01(
            (radius - Math.sqrt(bestDist2)) / Math.max(1.0, radius)
        );

        PacketDistributor.sendToPlayer(
            player,
            new ClientboundLingHunGuIntuitionPayload(angle, intensity, duration)
        );
    }

    private static boolean tryConsumeSoul(
        final ServerPlayer player,
        final NianTouData.Usage usageInfo,
        final int threatCount,
        final double fengDaoMultiplier
    ) {
        final double base = getMetaDouble(
            usageInfo,
            "base_soul_cost",
            DEFAULT_BASE_SOUL_COST
        );
        final double perThreat = getMetaDouble(
            usageInfo,
            "soul_cost_per_threat",
            DEFAULT_SOUL_COST_PER_THREAT
        );
        final double cost = (base + perThreat * threatCount) * fengDaoMultiplier;
        if (cost <= 0) {
            return true;
        }
        if (HunPoHelper.getAmount(player) < cost) {
            return false;
        }
        HunPoHelper.modify(player, -cost);
        return true;
    }

    private static double clamp01(final double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }

    private static double wrapRadians(final double angle) {
        double wrapped = angle;
        final double pi = Math.PI;
        final double twoPi = pi * 2.0;
        while (wrapped <= -pi) {
            wrapped += twoPi;
        }
        while (wrapped > pi) {
            wrapped -= twoPi;
        }
        return wrapped;
    }

    private static double getMetaDouble(
        final NianTouData.Usage usage,
        final String key,
        final double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    private static int getMetaInt(
        final NianTouData.Usage usage,
        final String key,
        final int defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Integer.parseInt(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }
}
