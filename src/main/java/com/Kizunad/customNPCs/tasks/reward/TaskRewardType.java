package com.Kizunad.customNPCs.tasks.reward;

/**
 * 任务奖励类型。
 */
public enum TaskRewardType {
    ITEM;

    public static TaskRewardType fromString(String raw) {
        if (raw == null || raw.isEmpty()) {
            return ITEM;
        }
        try {
            return TaskRewardType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ITEM;
        }
    }
}
