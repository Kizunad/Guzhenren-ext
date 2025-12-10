package com.Kizunad.guzhenrenext.customNPCImpl.ai.Action;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.Mob;

/**
 * NPC 行动冷却跟踪器，负责按实体维度记录调用时间，避免兼容逻辑被持续刷屏。
 */
final class NpcCooldownTracker {

    private final long cooldownTicks;
    private final Map<UUID, Long> lastTriggerGameTime = new ConcurrentHashMap<>();

    NpcCooldownTracker(long cooldownTicks) {
        this.cooldownTicks = Math.max(0L, cooldownTicks);
    }

    /**
     * 判断实体是否仍处于冷却时间内。
     */
    boolean shouldThrottle(Mob entity) {
        if (entity == null || cooldownTicks <= 0) {
            return false;
        }
        long now = entity.level().getGameTime();
        Long last = lastTriggerGameTime.get(entity.getUUID());
        if (last == null) {
            return false;
        }
        return now - last < cooldownTicks;
    }

    /**
     * 记录实体成功触发的时间点，供后续冷却计算。
     */
    void markUsed(Mob entity) {
        if (entity == null || cooldownTicks <= 0) {
            return;
        }
        lastTriggerGameTime.put(entity.getUUID(), entity.level().getGameTime());
    }
}
