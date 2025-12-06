package com.Kizunad.customNPCs.tasks;

/**
 * 任务类型：用于分类主线/支线/每日等任务，当前仅影响展示。
 */
public enum TaskType {
    MAIN,
    SIDE,
    DAILY,
    HIDDEN;

    /**
     * 将 json 中的字符串解析为任务类型，默认回退到 SIDE。
     */
    public static TaskType fromString(String value) {
        if (value == null || value.isEmpty()) {
            return SIDE;
        }
        try {
            return TaskType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return SIDE;
        }
    }
}
