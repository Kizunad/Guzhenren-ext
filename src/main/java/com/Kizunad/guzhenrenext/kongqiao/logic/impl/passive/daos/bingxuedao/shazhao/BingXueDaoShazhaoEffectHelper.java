package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.shazhao;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.phys.Vec3;

/**
 * 冰雪道杀招通用工具。
 * <p>
 * 目标：减少 kill-switch（资源不足）时的残留效果，并统一属性修饰/击退/冻结等细节实现。
 * </p>
 */
public final class BingXueDaoShazhaoEffectHelper {

    private static final double MIN_VALUE = 0.0;
    private static final double MIN_HORIZONTAL_VECTOR_SQR = 0.0001;

    private BingXueDaoShazhaoEffectHelper() {}

    /**
     * 为指定属性应用（或刷新）瞬时修饰符。
     * <p>
     * amount {@code <= 0} 时会自动移除对应修饰符。
     * </p>
     */
    public static void applyTransientModifier(
        final LivingEntity user,
        final Holder<Attribute> attribute,
        final ResourceLocation modifierId,
        final double amount,
        final AttributeModifier.Operation operation
    ) {
        if (user == null || attribute == null || modifierId == null || operation == null) {
            return;
        }
        final AttributeInstance attr = user.getAttribute(attribute);
        if (attr == null) {
            return;
        }

        final double clamped = Math.max(MIN_VALUE, amount);
        final AttributeModifier existing = attr.getModifier(modifierId);
        if (existing != null && Double.compare(existing.amount(), clamped) == 0) {
            return;
        }
        if (existing != null) {
            attr.removeModifier(modifierId);
        }
        if (clamped > MIN_VALUE) {
            attr.addTransientModifier(new AttributeModifier(modifierId, clamped, operation));
        }
    }

    /**
     * 移除指定属性上的修饰符。
     */
    public static void removeModifier(
        final LivingEntity user,
        final Holder<Attribute> attribute,
        final ResourceLocation modifierId
    ) {
        if (user == null || attribute == null || modifierId == null) {
            return;
        }
        final AttributeInstance attr = user.getAttribute(attribute);
        if (attr != null && attr.getModifier(modifierId) != null) {
            attr.removeModifier(modifierId);
        }
    }

    /**
     * 施加击退（以中心点为基准）。
     */
    public static void applyKnockback(
        final Vec3 center,
        final LivingEntity target,
        final double strength,
        final double verticalPush
    ) {
        if (center == null || target == null || strength <= MIN_VALUE) {
            return;
        }
        final Vec3 delta = target.position().subtract(center);
        final Vec3 horizontal = new Vec3(delta.x, 0.0, delta.z);
        if (horizontal.lengthSqr() <= MIN_HORIZONTAL_VECTOR_SQR) {
            return;
        }
        final Vec3 dir = horizontal.normalize();
        target.push(dir.x * strength, verticalPush, dir.z * strength);
        target.hurtMarked = true;
    }

    /**
     * 增加目标冻结刻数，并限制总冻结刻数上限。
     */
    public static void addFreezeTicks(
        final LivingEntity target,
        final int ticksToAdd,
        final int maxFrozenTicks
    ) {
        if (target == null || ticksToAdd <= 0 || maxFrozenTicks <= 0) {
            return;
        }
        final int current = Math.max(0, target.getTicksFrozen());
        final int next = Math.min(maxFrozenTicks, current + ticksToAdd);
        target.setTicksFrozen(next);
    }

    /**
     * 增加吸收护盾，并限制上限。
     */
    public static void addAbsorption(
        final ServerPlayer player,
        final double amount,
        final double maxAbsorption
    ) {
        if (player == null || amount <= MIN_VALUE || maxAbsorption <= MIN_VALUE) {
            return;
        }
        final float current = player.getAbsorptionAmount();
        final float next = (float) Math.min(maxAbsorption, current + amount);
        player.setAbsorptionAmount(next);
    }
}

