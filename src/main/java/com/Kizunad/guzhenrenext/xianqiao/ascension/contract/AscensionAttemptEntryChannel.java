package com.Kizunad.guzhenrenext.xianqiao.ascension.contract;

/**
 * 升仙尝试入口通道契约。
 * <p>
 * 该枚举用于冻结“旧命令入口与未来玩家入口必须汇聚到同一服务主线”的约束。
 * </p>
 */
public enum AscensionAttemptEntryChannel {

    /** 旧版命令入口适配通道。 */
    LEGACY_COMMAND_ADAPTER,

    /** 未来面向玩家的统一入口通道。 */
    PLAYER_INITIATED_ENTRY
}
