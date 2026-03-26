package com.Kizunad.guzhenrenext.xianqiao.ascension.contract;

/**
 * 升仙尝试中断策略决议。
 */
public enum AscensionInterruptionDecision {

    /**
     * 保留当前唯一事务主线，并冻结输入，拒绝新分支。
     */
    KEEP_SINGLE_ATTEMPT_AND_FREEZE_INPUT
}
