package com.Kizunad.customNPCs.ai.actions;

/**
 * 动作执行结果，包含状态与可选原因。
 */
public record ActionResult(ActionStatus status, String reason) {
    public ActionResult(ActionStatus status) {
        this(status, "");
    }
}
