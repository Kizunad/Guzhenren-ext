package com.Kizunad.guzhenrenext.xianqiao.ascension.contract;

/**
 * 升仙尝试中断策略契约。
 * <p>
 * Task 1 仅冻结唯一且确定的策略，不实现任何恢复、回滚或重排主线的运行逻辑。
 * </p>
 */
public final class AscensionInterruptionPolicy {

    private AscensionInterruptionPolicy() {
    }

    /**
     * 对中断原因给出唯一决议。
     * <p>
     * 当前契约明确要求：重复触发、断线、重登、重入都不得打开新事务分支。
     * </p>
     *
     * @param reason 中断原因
     * @return 固定返回“保留唯一事务并冻结输入”的决议
     */
    public static AscensionInterruptionDecision decide(AscensionInterruptionReason reason) {
        if (reason == null) {
            throw new IllegalArgumentException("reason 不能为空");
        }
        return AscensionInterruptionDecision.KEEP_SINGLE_ATTEMPT_AND_FREEZE_INPUT;
    }
}
