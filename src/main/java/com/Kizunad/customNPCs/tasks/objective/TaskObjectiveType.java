package com.Kizunad.customNPCs.tasks.objective;

/**
 * 任务目标枚举。
 */
public enum TaskObjectiveType {
    SUBMIT_ITEM,
    KILL_ENTITY,
    GUARD_ENTITY;

    public static TaskObjectiveType fromString(String value) {
        if (value == null || value.isEmpty()) {
            return SUBMIT_ITEM;
        }
        String normalized = value.trim().toUpperCase();
        switch (normalized) {
            case "SUBMIT", "SUBMIT_ITEM":
                return SUBMIT_ITEM;
            case "KILL", "KILL_ENTITY":
                return KILL_ENTITY;
            case "GUARD", "GUARD_ENTITY":
                return GUARD_ENTITY;
            default:
                return SUBMIT_ITEM;
        }
    }
}
