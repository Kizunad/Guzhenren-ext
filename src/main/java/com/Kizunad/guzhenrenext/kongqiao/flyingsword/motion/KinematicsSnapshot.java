package com.Kizunad.guzhenrenext.kongqiao.flyingsword.motion;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordCalculator;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.context.CalcContexts;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.integration.domain.SwordSpeedModifiers;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.tuning.FlyingSwordCoreTuning;
import net.minecraft.world.phys.Vec3;

/**
 * 每 tick 由实体生成的运动学快照，缓存常用计算结果，避免重复计算。
 */
public record KinematicsSnapshot(
    Vec3 position,
    Vec3 currentVelocity,
    float yRot,
    float xRot,
    double effectiveBase,
    double effectiveMax,
    double effectiveAccel,
    double turnRate,
    double domainScale
) {
    private static final double BASE_EPSILON = 1.0e-8;
    private static final double DOMAIN_SCALE_FLOOR = 0.1;
    private static final double MAX_SPEED_EPSILON = 1.0e-6;

    /**
     * 空快照常量。
     */
    public static final KinematicsSnapshot EMPTY = new KinematicsSnapshot(
        Vec3.ZERO,
        Vec3.ZERO,
        0.0f,
        0.0f,
        0.0,
        0.0,
        0.0,
        0.0,
        1.0
    );

    /**
     * 简化构造（用于 SwordMotionDriver）。
     */
    public KinematicsSnapshot(
        Vec3 position,
        Vec3 velocity,
        float yRot,
        float xRot
    ) {
        this(position, velocity, yRot, xRot, 0.0, 0.0, 0.0, 0.0, 1.0);
    }

    /** 构建完整快照。 */
    public static KinematicsSnapshot capture(FlyingSwordEntity sword) {
        var ctx = CalcContexts.from(sword);
        double base = FlyingSwordCalculator.effectiveSpeedBase(
            sword.getSwordAttributes().speedBase,
            ctx
        );
        double max = FlyingSwordCalculator.effectiveSpeedMax(
            sword.getSwordAttributes().speedMax,
            ctx
        );
        double accel = FlyingSwordCalculator.effectiveAccel(
            sword.getSwordAttributes().accel,
            ctx
        );
        double turn = sword.getSwordAttributes().turnRate;
        double domainScale = SwordSpeedModifiers.computeDomainSpeedScale(sword);
        return new KinematicsSnapshot(
            sword.position(),
            sword.getDeltaMovement(),
            sword.getYRot(),
            sword.getXRot(),
            base,
            max,
            accel,
            turn,
            domainScale
        );
    }

    /** 计算基于 base 的缩放系数。 */
    public double baseScale(double rawBase) {
        return rawBase <= BASE_EPSILON ? 1.0 : effectiveBase / rawBase;
    }

    /** 返回领域缩放后的最大速度。 */
    public double scaledMaxSpeed() {
        return effectiveMax * domainScale;
    }

    /** 返回领域缩放后的基础速度。 */
    public double scaledBaseSpeed() {
        return effectiveBase * domainScale;
    }

    /** 返回领域缩放后的加速度。 */
    public double scaledAccel() {
        return effectiveAccel * Math.max(DOMAIN_SCALE_FLOOR, domainScale);
    }

    /** 返回领域缩放后的最大转向速度（仍为角度/ tick）。 */
    public double scaledTurnRate() {
        double base = turnRate * Math.max(DOMAIN_SCALE_FLOOR, domainScale);
        double maxSpeed = Math.max(MAX_SPEED_EPSILON, effectiveMax);
        double speedRatio = Math.min(1.0, currentVelocity.length() / maxSpeed);
        double speedScale =
            1.0 + FlyingSwordCoreTuning.TURN_RATE_SPEED_SCALE * speedRatio;
        return base * speedScale;
    }
}
