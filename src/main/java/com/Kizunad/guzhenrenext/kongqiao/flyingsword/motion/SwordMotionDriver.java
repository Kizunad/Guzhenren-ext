package com.Kizunad.guzhenrenext.kongqiao.flyingsword.motion;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordConstants;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.integration.domain.SwordSpeedModifiers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

/**
 * 飞剑运动驱动入口。
 * <p>
 * 负责各模式下的位置计算与速度应用。
 * 从 {@code FlyingSwordEntity} 中提取，统一运动逻辑。
 * </p>
 * <p>
 * 设计原则：
 * <ul>
 *     <li>运动驱动只负责"如何移动"，不负责"为什么移动"（由 AI 决定）</li>
 *     <li>所有运动通过 {@link #applyVelocityTowards} 统一处理</li>
 *     <li>速度计算考虑加速度、领域加成等因素</li>
 * </ul>
 * </p>
 */
public final class SwordMotionDriver {

    private SwordMotionDriver() {}

    // ===== 模式运动入口 =====

    /**
     * ORBIT：环绕运动。
     * <p>
     * 围绕主人旋转飞行。
     * </p>
     */
    public static void tickOrbit(FlyingSwordEntity sword, LivingEntity owner) {
        if (sword == null || owner == null) {
            return;
        }

        // 基于 tickCount 计算环绕角度
        double angle = (sword.tickCount % FlyingSwordConstants.ORBIT_DEGREES_PER_REV)
                * FlyingSwordConstants.DEG_TO_RAD;

        // 环绕中心：主人位置 + 高度偏移
        Vec3 center = owner.position().add(
                0.0,
                owner.getBbHeight() * FlyingSwordConstants.OWNER_HEIGHT_ORBIT,
                0.0
        );

        // 目标位置：环绕圆周上的点
        Vec3 desired = center.add(
                Math.cos(angle) * FlyingSwordConstants.ORBIT_RADIUS,
                FlyingSwordConstants.ORBIT_VERTICAL_OFFSET,
                Math.sin(angle) * FlyingSwordConstants.ORBIT_RADIUS
        );

        Vec3 delta = desired.subtract(sword.position());

        // 环绕速度（下降时减速）
        double orbitSpeed = sword.getSwordAttributes().speedBase;
        if (FlyingSwordConstants.ORBIT_VERTICAL_OFFSET <= 0) {
            orbitSpeed *= FlyingSwordConstants.ORBIT_SPEED_WHEN_DESCENDING_SCALE;
        }

        applyVelocityTowards(sword, delta, orbitSpeed);
    }

    /**
     * HOVER：悬停运动。
     * <p>
     * 在主人头顶静止悬停。
     * </p>
     */
    public static void tickHover(FlyingSwordEntity sword, LivingEntity owner) {
        if (sword == null || owner == null) {
            return;
        }

        Vec3 desired = owner.position().add(
                0.0,
                owner.getBbHeight() * FlyingSwordConstants.OWNER_HEIGHT_HOVER_MULTIPLIER
                        + FlyingSwordConstants.HOVER_HEIGHT_OFFSET,
                0.0
        );

        Vec3 delta = desired.subtract(sword.position());
        double speed = sword.getSwordAttributes().speedBase * FlyingSwordConstants.HOVER_SPEED_SCALE;

        applyVelocityTowards(sword, delta, speed);
    }

    /**
     * RECALL：召回运动。
     * <p>
     * 快速返回主人身边。
     * </p>
     */
    public static void tickRecall(FlyingSwordEntity sword, LivingEntity owner) {
        if (sword == null || owner == null) {
            return;
        }

        Vec3 desired = owner.position().add(
                0.0,
                owner.getBbHeight() * FlyingSwordConstants.OWNER_HEIGHT_ORBIT,
                0.0
        );

        Vec3 delta = desired.subtract(sword.position());

        // 召回使用最大速度
        applyVelocityTowards(sword, delta, sword.getSwordAttributes().speedMax);
    }

    /**
     * 向指定位置移动（通用）。
     * <p>
     * 用于自定义移动目标（如战斗追击由 CombatOps 调用）。
     * </p>
     *
     * @param sword    飞剑实体
     * @param target   目标位置
     * @param speed    期望速度
     */
    public static void moveTowards(FlyingSwordEntity sword, Vec3 target, double speed) {
        if (sword == null || target == null) {
            return;
        }
        Vec3 delta = target.subtract(sword.position());
        applyVelocityTowards(sword, delta, speed);
    }

    // ===== 核心速度处理 =====

    /**
     * 向目标方向应用速度（核心方法）。
     * <p>
     * 统一处理：
     * <ul>
     *     <li>速度插值（加速度）</li>
     *     <li>领域速度加成</li>
     *     <li>速度上限</li>
     *     <li>朝向更新</li>
     * </ul>
     * </p>
     *
     * @param sword     飞剑实体
     * @param delta     目标方向向量（目标位置 - 当前位置）
     * @param baseSpeed 基础期望速度
     */
    public static void applyVelocityTowards(FlyingSwordEntity sword, Vec3 delta, double baseSpeed) {
        if (sword == null || delta == null) {
            return;
        }

        double distance = delta.length();
        if (distance < FlyingSwordConstants.MIN_DISTANCE) {
            sword.setDeltaMovement(Vec3.ZERO);
            return;
        }

        var attrs = sword.getSwordAttributes();

        // 领域速度加成
        double domainScale = SwordSpeedModifiers.computeDomainSpeedScale(sword);

        // 目标速度：取基础速度和距离的较小值，但不超过最大速度
        double targetSpeed = Math.max(
                baseSpeed,
                Math.min(distance, attrs.speedMax * domainScale)
        );

        // 期望速度向量
        Vec3 desiredVel = delta.normalize().scale(targetSpeed);

        // 当前速度
        Vec3 current = sword.getDeltaMovement();

        // 加速度插值
        double lerpFactor = Math.min(1.0, attrs.accel);
        Vec3 newVel = current.lerp(desiredVel, lerpFactor);

        // 速度上限
        double maxSpeed = attrs.speedMax * domainScale;
        if (newVel.length() > maxSpeed) {
            newVel = newVel.normalize().scale(maxSpeed);
        }

        // 应用速度
        sword.setDeltaMovement(newVel);

        // 执行移动
        sword.move(MoverType.SELF, sword.getDeltaMovement());

        // 更新朝向（面向运动方向）
        if (newVel.lengthSqr() > FlyingSwordConstants.LOOK_EPSILON) {
            float yaw = (float) (Math.atan2(newVel.z, newVel.x) * FlyingSwordConstants.RAD_TO_DEG)
                    - FlyingSwordConstants.LOOK_ROTATE_DEG_OFFSET;
            sword.setYRot(yaw);
        }
    }

    /**
     * 计算到达目标所需的 tick 数（估算）。
     * <p>
     * 用于预测和 UI 显示。
     * </p>
     */
    public static int estimateTicksToReach(FlyingSwordEntity sword, Vec3 target) {
        if (sword == null || target == null) {
            return Integer.MAX_VALUE;
        }

        double distance = sword.position().distanceTo(target);
        double speed = sword.getSwordAttributes().speedMax;

        if (speed <= 0) {
            return Integer.MAX_VALUE;
        }

        return (int) Math.ceil(distance / speed);
    }

    /**
     * 立即停止飞剑运动。
     */
    public static void stopMotion(FlyingSwordEntity sword) {
        if (sword != null) {
            sword.setDeltaMovement(Vec3.ZERO);
        }
    }

    /**
     * 获取当前运动状态快照。
     */
    public static KinematicsSnapshot snapshot(FlyingSwordEntity sword) {
        if (sword == null) {
            return KinematicsSnapshot.EMPTY;
        }
        return new KinematicsSnapshot(
                sword.position(),
                sword.getDeltaMovement(),
                sword.getYRot(),
                sword.getXRot()
        );
    }
}
