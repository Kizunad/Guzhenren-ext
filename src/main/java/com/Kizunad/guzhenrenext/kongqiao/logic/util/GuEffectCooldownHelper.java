package com.Kizunad.guzhenrenext.kongqiao.logic.util;

import net.minecraft.world.entity.LivingEntity;

/**
 * 蛊虫主动技能冷却工具。
 * <p>
 * 统一以 persistentData 存储 “冷却截止 tick”，避免各效果重复实现。
 * </p>
 */
public final class GuEffectCooldownHelper {

    private GuEffectCooldownHelper() {}

    public static int getCooldownUntilTick(
        final LivingEntity entity,
        final String nbtKey
    ) {
        if (entity == null || nbtKey == null || nbtKey.isBlank()) {
            return 0;
        }
        return entity.getPersistentData().getInt(nbtKey);
    }

    public static void setCooldownUntilTick(
        final LivingEntity entity,
        final String nbtKey,
        final int untilTick
    ) {
        if (entity == null || nbtKey == null || nbtKey.isBlank()) {
            return;
        }
        entity.getPersistentData().putInt(nbtKey, Math.max(0, untilTick));
    }

    public static boolean isOnCooldown(
        final LivingEntity entity,
        final String nbtKey
    ) {
        if (entity == null) {
            return false;
        }
        return getRemainingTicks(entity, nbtKey) > 0;
    }

    public static int getRemainingTicks(
        final LivingEntity entity,
        final String nbtKey
    ) {
        if (entity == null) {
            return 0;
        }
        final int currentTick = entity.tickCount;
        final int untilTick = getCooldownUntilTick(entity, nbtKey);
        return Math.max(0, untilTick - currentTick);
    }
}

