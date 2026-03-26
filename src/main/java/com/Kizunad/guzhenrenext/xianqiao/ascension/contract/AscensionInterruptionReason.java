package com.Kizunad.guzhenrenext.xianqiao.ascension.contract;

/**
 * 升仙尝试中断事件原因。
 */
public enum AscensionInterruptionReason {

    /** 重复触发升仙尝试。 */
    DUPLICATE_TRIGGER,

    /** 玩家断线。 */
    DISCONNECT,

    /** 玩家重登。 */
    RELOGIN,

    /** 玩家重入入口。 */
    REENTER
}
