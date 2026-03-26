package com.Kizunad.guzhenrenext.xianqiao.ascension.contract;

/**
 * 升仙尝试统一服务契约。
 * <p>
 * 本类型是只读契约，不提供世界变更实现。
 * 作用是冻结“所有入口必须汇聚到同一事务主线”这一架构约束。
 * </p>
 */
public final class AscensionAttemptServiceContract {

    /**
     * 唯一事务服务标识。
     */
    public static final String SINGLE_ATTEMPT_TRANSACTION_SERVICE = "xianqiao.ascension.single_attempt";

    private AscensionAttemptServiceContract() {
    }

    /**
     * 根据入口通道解析事务服务。
     * <p>
     * 当前规则固定返回唯一服务标识，确保 legacy 命令入口和未来玩家入口不会分叉。
     * </p>
     *
     * @param entryChannel 入口通道
     * @return 唯一事务服务标识
     */
    public static String resolveTransactionService(AscensionAttemptEntryChannel entryChannel) {
        if (entryChannel == null) {
            throw new IllegalArgumentException("entryChannel 不能为空");
        }
        return SINGLE_ATTEMPT_TRANSACTION_SERVICE;
    }
}
