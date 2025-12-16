package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.JingLiHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.DaoHenCalculator;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * 狼魂蛊：战斗技能【贪狼吞噬】。
 * <p>
 * 当攻击生命值低于阈值的敌人时，触发一次“必定暴击”，并回复少量魂魄与精力。
 * </p>
 */
public class LangHunGuGreedyDevourEffect implements IGuEffect {

    public static final String USAGE_ID =
        "guzhenren:langhungu_active_greedy_devour";

    private static final float DEFAULT_HEALTH_THRESHOLD = 0.3f;
    private static final float DEFAULT_CRIT_MULTIPLIER = 1.5f;
    private static final double DEFAULT_RESTORE_PERCENT = 0.01;

    private static final double VECTOR_EPSILON_SQUARED = 0.0001;
    private static final double ORIGIN_HEIGHT_FACTOR = 0.65;
    private static final double HALF = 0.5;
    private static final double SMOOTH_STEP_FACTOR = 3.0;

    private static final Vector3f CLAW_COLOR_OUTER = new Vector3f(
        0.05f,
        0.4f,
        0.15f
    );
    private static final Vector3f CLAW_COLOR_INNER = new Vector3f(
        0.2f,
        1.0f,
        0.4f
    );
    private static final float CLAW_SCALE_OUTER = 1.6f;
    private static final float CLAW_SCALE_INNER = 1.1f;

    private static final int CLAW_LINE_COUNT = 3;
    private static final int CLAW_POINTS_PER_LINE = 18;
    private static final double CLAW_LINE_SPACING = 0.5;
    private static final double CLAW_LINE_HEIGHT_SPACING = 0.06;
    private static final double CLAW_LINE_PLANE_TILT_RADIANS = 0.8;
    private static final double CLAW_FORWARD_OFFSET = 0.6;
    private static final double CLAW_LENGTH = 1.4;
    private static final double CLAW_ARC_HEIGHT = 0.35;
    private static final double CLAW_SIDE_BEND = 0.2;
    private static final double CLAW_JITTER = 0.02;

    @Override
    public String getUsageId() {
        return USAGE_ID;
    }

    @Override
    public float onAttack(
        LivingEntity attacker,
        LivingEntity target,
        float damage,
        ItemStack stack,
        NianTouData.Usage usageInfo
    ) {
        if (attacker.level().isClientSide()) {
            return damage;
        }

        float threshold = getMetaFloat(
            usageInfo,
            "health_threshold",
            DEFAULT_HEALTH_THRESHOLD
        );
        if (!isLowHealth(target, threshold)) {
            return damage;
        }

        float critMultiplier = getMetaFloat(
            usageInfo,
            "crit_multiplier",
            DEFAULT_CRIT_MULTIPLIER
        );
        double daoMultiplier = DaoHenCalculator.calculateMultiplier(
            attacker,
            target,
            DaoHenHelper.DaoType.HUN_DAO
        );
        double finalMultiplier = 1.0 + (critMultiplier - 1.0) * daoMultiplier;

        restoreResources(attacker, usageInfo);
        spawnClawTrail(attacker, target);
        return (float) (damage * finalMultiplier);
    }

    private void restoreResources(
        LivingEntity attacker,
        NianTouData.Usage usageInfo
    ) {
        double restorePercent = getMetaDouble(
            usageInfo,
            "restore_percent",
            DEFAULT_RESTORE_PERCENT
        );
        double soulMax = HunPoHelper.getMaxAmount(attacker);
        double staminaMax = JingLiHelper.getMaxAmount(attacker);
        HunPoHelper.modify(attacker, soulMax * restorePercent);
        JingLiHelper.modify(attacker, staminaMax * restorePercent);
    }

    private void spawnClawTrail(LivingEntity attacker, LivingEntity target) {
        if (!(attacker.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 direction = target.position().subtract(attacker.position());
        if (direction.lengthSqr() < VECTOR_EPSILON_SQUARED) {
            direction = attacker.getLookAngle();
        }
        direction = direction.normalize();

        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = direction.cross(up);
        if (right.lengthSqr() < VECTOR_EPSILON_SQUARED) {
            right = new Vec3(1, 0, 0);
        }
        right = right.normalize();

        Vec3 origin = attacker
            .position()
            .add(0, attacker.getBbHeight() * ORIGIN_HEIGHT_FACTOR, 0)
            .add(direction.scale(CLAW_FORWARD_OFFSET));

        DustParticleOptions outer = new DustParticleOptions(
            CLAW_COLOR_OUTER,
            CLAW_SCALE_OUTER
        );
        DustParticleOptions inner = new DustParticleOptions(
            CLAW_COLOR_INNER,
            CLAW_SCALE_INNER
        );

        int centerIndex = (CLAW_LINE_COUNT - 1) / 2;
        for (int line = 0; line < CLAW_LINE_COUNT; line++) {
            int indexFromCenter = line - centerIndex;
            double lineOffset = indexFromCenter * CLAW_LINE_SPACING;
            double lineHeightOffset =
                indexFromCenter * CLAW_LINE_HEIGHT_SPACING;

            double tilt = indexFromCenter * CLAW_LINE_PLANE_TILT_RADIANS;
            double cos = Math.cos(tilt);
            double sin = Math.sin(tilt);
            Vec3 lineUp = up.scale(cos).add(right.scale(sin));
            Vec3 lineRight = right.scale(cos).subtract(up.scale(sin));

            for (int i = 0; i < CLAW_POINTS_PER_LINE; i++) {
                double t = (double) i / (CLAW_POINTS_PER_LINE - 1);
                double ease = smoothStep(t);

                double forward = ease * CLAW_LENGTH;
                double vertical = Math.sin(Math.PI * ease) * CLAW_ARC_HEIGHT;
                double bend = (ease - HALF) * CLAW_SIDE_BEND;

                double jitterX =
                    (serverLevel.random.nextDouble() - HALF) * CLAW_JITTER;
                double jitterY =
                    (serverLevel.random.nextDouble() - HALF) * CLAW_JITTER;
                double jitterZ =
                    (serverLevel.random.nextDouble() - HALF) * CLAW_JITTER;

                Vec3 pos = origin
                    .add(direction.scale(forward))
                    .add(lineRight.scale(lineOffset + bend))
                    .add(lineUp.scale(vertical + lineHeightOffset))
                    .add(jitterX, jitterY, jitterZ);

                serverLevel.sendParticles(
                    outer,
                    pos.x,
                    pos.y,
                    pos.z,
                    1,
                    0,
                    0,
                    0,
                    0
                );
                serverLevel.sendParticles(
                    inner,
                    pos.x,
                    pos.y,
                    pos.z,
                    1,
                    0,
                    0,
                    0,
                    0
                );
            }
        }
    }

    private static boolean isLowHealth(LivingEntity target, float threshold) {
        float max = target.getMaxHealth();
        if (max <= 0) {
            return false;
        }
        return (target.getHealth() / max) < threshold;
    }

    private static double smoothStep(double t) {
        double clamped = Math.max(0.0, Math.min(1.0, t));
        return clamped * clamped * (SMOOTH_STEP_FACTOR - 2.0 * clamped);
    }

    private static float getMetaFloat(
        NianTouData.Usage usage,
        String key,
        float defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Float.parseFloat(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    private static double getMetaDouble(
        NianTouData.Usage usage,
        String key,
        double defaultValue
    ) {
        if (usage.metadata() != null && usage.metadata().containsKey(key)) {
            try {
                return Double.parseDouble(usage.metadata().get(key));
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }
}
