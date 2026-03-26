package com.Kizunad.guzhenrenext.xianqiao.ascension.contract;

import java.util.List;
import net.minecraft.core.BlockPos;

/**
 * 开窍架构硬约束契约。
 * <p>
 * 该类型只定义只读约束，不实现任何世界写入逻辑。
 * </p>
 */
public final class AscensionOpeningArchitectureContract {

    /**
     * 领土边界真源语义，固定为 min/max chunk 闭区间。
     */
    public static final String BOUNDARY_TRUTH_SOURCE = "MIN_MAX_CHUNK_CLOSED_RANGE";

    /**
     * 九天碎片 v1 每次向四向对称扩张的 chunk 数。
     */
    public static final int FRAGMENT_V1_SYMMETRIC_CHUNK_DELTA = 1;

    private static final int CHUNK_SIZE = 16;
    private static final int DOUBLE_CHUNK_SIZE = CHUNK_SIZE * 2;

    private AscensionOpeningArchitectureContract() {
    }

    /**
     * 2x2 采样锚点必须围绕接缝中心。
     *
     * @param center 参考中心
     * @return 西北、东北、西南、东南四锚点
     */
    public static List<BlockPos> seamAnchorsForTwoByTwo(BlockPos center) {
        return List.of(
            center.offset(-CHUNK_SIZE, 0, -CHUNK_SIZE),
            center.offset(0, 0, -CHUNK_SIZE),
            center.offset(-CHUNK_SIZE, 0, 0),
            center.offset(0, 0, 0)
        );
    }

    /**
     * 4x4 布局锚点必须围绕同一接缝中心。
     *
     * @param center 参考中心
     * @return 16 个锚点
     */
    public static List<BlockPos> seamAnchorsForFourByFour(BlockPos center) {
        return List.of(
            center.offset(-DOUBLE_CHUNK_SIZE, 0, -DOUBLE_CHUNK_SIZE),
            center.offset(-CHUNK_SIZE, 0, -DOUBLE_CHUNK_SIZE),
            center.offset(0, 0, -DOUBLE_CHUNK_SIZE),
            center.offset(CHUNK_SIZE, 0, -DOUBLE_CHUNK_SIZE),
            center.offset(-DOUBLE_CHUNK_SIZE, 0, -CHUNK_SIZE),
            center.offset(-CHUNK_SIZE, 0, -CHUNK_SIZE),
            center.offset(0, 0, -CHUNK_SIZE),
            center.offset(CHUNK_SIZE, 0, -CHUNK_SIZE),
            center.offset(-DOUBLE_CHUNK_SIZE, 0, 0),
            center.offset(-CHUNK_SIZE, 0, 0),
            center.offset(0, 0, 0),
            center.offset(CHUNK_SIZE, 0, 0),
            center.offset(-DOUBLE_CHUNK_SIZE, 0, CHUNK_SIZE),
            center.offset(-CHUNK_SIZE, 0, CHUNK_SIZE),
            center.offset(0, 0, CHUNK_SIZE),
            center.offset(CHUNK_SIZE, 0, CHUNK_SIZE)
        );
    }
}
