package com.Kizunad.customNPCs.ai.actions;

/**
 * 动作执行状态枚举
 * <p>
 * 表示动作在执行过程中的状态
 */
public enum ActionStatus {
    /**
     * 动作正在执行中
     */
    RUNNING,

    /**
     * 动作成功完成
     */
    SUCCESS,

    /**
     * 动作执行失败
     */
    FAILURE,
}
