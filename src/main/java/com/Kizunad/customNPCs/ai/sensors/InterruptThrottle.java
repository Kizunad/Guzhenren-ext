package com.Kizunad.customNPCs.ai.sensors;

import java.util.UUID;

/**
 * 通用中断节流工具，按目标、事件等级与距离桶进行去重，避免传感器层频繁触发。
 */
public final class InterruptThrottle {

    private final int criticalWindowTicks;
    private final int importantWindowTicks;
    private final int infoWindowTicks;

    private UUID lastTargetId;
    private int lastDistanceBucket = -1;
    private SensorEventType lastLevel;
    private long lastGameTime = -1L;

    public InterruptThrottle(int criticalWindowTicks, int importantWindowTicks, int infoWindowTicks) {
        this.criticalWindowTicks = criticalWindowTicks;
        this.importantWindowTicks = importantWindowTicks;
        this.infoWindowTicks = infoWindowTicks;
    }

    /**
     * 判断是否允许触发中断，并在允许时记录最新触发信息。
     * @param targetId 触发源实体 UUID，可为空表示无特定实体
     * @param level 事件等级
     * @param distanceBucket 距离桶/强度桶，用于状态变化判定
     * @param gameTime 当前游戏 tick
     * @return true 表示可以触发并已记录，false 表示节流拒绝
     */
    public boolean allowInterrupt(
        UUID targetId,
        SensorEventType level,
        int distanceBucket,
        long gameTime
    ) {
        int window = getWindow(level);

        if (
            targetId != null &&
            targetId.equals(lastTargetId) &&
            lastLevel == level &&
            lastDistanceBucket == distanceBucket &&
            gameTime - lastGameTime < window
        ) {
            return false;
        }

        lastTargetId = targetId;
        lastLevel = level;
        lastDistanceBucket = distanceBucket;
        lastGameTime = gameTime;
        return true;
    }

    private int getWindow(SensorEventType level) {
        return switch (level) {
            case CRITICAL -> criticalWindowTicks;
            case IMPORTANT -> importantWindowTicks;
            case INFO -> infoWindowTicks;
        };
    }
}
