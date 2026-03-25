package com.Kizunad.guzhenrenext.xianqiao.entry;

/**
 * 初始化硬约束契约。
 * <p>
 * 该类型只用于冻结 Task-1 的架构语义，不承载任何运行时边界计算、落地执行或 world mutation。
 * 后续任务只能基于本契约实现，不能改写其单位与含义。
 * </p>
 */
public record ApertureInitializationInvariantContract(
    boolean chunkBoundaryTruth,
    boolean seamCenterForEvenLayouts,
    boolean symmetricFragmentExpansion,
    boolean triggerRequiresRankFivePeakAndThreeQiAndConfirmedAttempt,
    int maxReservedChaosBlocks
) {

    private static final int MAX_RESERVED_CHAOS_BLOCKS = 16;

    public static ApertureInitializationInvariantContract defaults() {
        return new ApertureInitializationInvariantContract(
            true,
            true,
            true,
            true,
            MAX_RESERVED_CHAOS_BLOCKS
        );
    }
}
