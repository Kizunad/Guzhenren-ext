package com.Kizunad.guzhenrenext.kongqiao.logic.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * 物理伤害来源构建工具。
 * <p>
 * 统一将“普通伤害”落到玩家/生物攻击伤害源，从而受护甲影响，避免法术伤害穿透护甲导致的强度失控。
 * </p>
 */
public final class PhysicalDamageSourceHelper {

    private PhysicalDamageSourceHelper() {}

    /**
     * 构建“普通伤害”来源。
     */
    public static DamageSource buildPhysicalDamageSource(final LivingEntity user) {
        if (user instanceof ServerPlayer player) {
            return player.damageSources().playerAttack(player);
        }
        return user.damageSources().mobAttack(user);
    }
}

