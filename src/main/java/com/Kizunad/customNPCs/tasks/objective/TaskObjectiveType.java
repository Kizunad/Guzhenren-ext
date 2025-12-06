package com.Kizunad.customNPCs.tasks.objective;

/**
 * 任务目标枚举。
 */
public enum TaskObjectiveType {
    SUBMIT_ITEM;

    public static TaskObjectiveType fromString(String value) {
        if (value == null || value.isEmpty()) {
            return SUBMIT_ITEM;
        }
        try {
            return TaskObjectiveType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return SUBMIT_ITEM;
        }
    }
}
