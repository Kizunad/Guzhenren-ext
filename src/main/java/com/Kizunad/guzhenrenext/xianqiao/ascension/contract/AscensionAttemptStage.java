package com.Kizunad.guzhenrenext.xianqiao.ascension.contract;

/**
 * 升仙尝试主链阶段契约。
 * <p>
 * 本枚举仅用于冻结阶段语义，不承载任何世界变更逻辑。
 * </p>
 */
public enum AscensionAttemptStage {

    /** 修炼推进阶段。 */
    CULTIVATION_PROGRESS,

    /** 升仙准备已解锁。 */
    ASCENSION_PREPARATION_UNLOCKED,

    /** 观气与调和阶段。 */
    QI_OBSERVATION_AND_HARMONIZATION,

    /** 满足前置条件，等待玩家确认发起。 */
    READY_TO_CONFIRM,

    /**
     * 玩家已主动发起升仙尝试，输入冻结。
     * <p>
     * 该状态不是普通确认提示，而是事务开始点。
     * </p>
     */
    CONFIRMED,

    /** 在原世界原地进入劫程。 */
    WORLD_TRIBULATION_IN_PLACE,

    /** 仙窍成形过程。 */
    APERTURE_FORMING,

    /** 失败结局，重伤未死。 */
    FAILED_SEVERE_INJURY,

    /** 失败结局，致死。 */
    FAILED_DEATH
}
