package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 光道效果通用击退工具。
 * <p>
 * 仅用于简单可靠的水平击退（可选少量抬升），不做复杂物理与抗性穿透。
 * </p>
 */
final class GuangDaoKnockbackHelper {

    private static final double EPSILON_SQR = 1.0E-6;

    private GuangDaoKnockbackHelper() {}

    static void applyKnockback(
        final LivingEntity caster,
        final LivingEntity target,
        final double strength,
        final double vertical
    ) {
        if (caster == null || target == null) {
            return;
        }
        if (strength <= 0.0) {
            return;
        }
        final Vec3 delta = target.position().subtract(caster.position());
        final Vec3 horizontal = new Vec3(delta.x, 0.0, delta.z);
        if (horizontal.lengthSqr() <= EPSILON_SQR) {
            return;
        }
        final Vec3 push = horizontal.normalize().scale(strength);
        target.push(push.x, Math.max(0.0, vertical), push.z);
        target.hasImpulse = true;
    }
}

