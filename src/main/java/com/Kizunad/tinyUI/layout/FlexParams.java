package com.Kizunad.tinyUI.layout;

/**
 * Flex 子项参数 - 定义元素在 Flex 布局中的行为。
 * <p>
 * 参数说明：
 * <ul>
 *   <li><b>flexGrow</b>: 增长因子，当有剩余空间时元素如何扩展</li>
 *   <li><b>flexShrink</b>: 收缩因子，当空间不足时元素如何收缩</li>
 *   <li><b>basis</b>: 初始主轴尺寸，-1 表示使用元素当前尺寸</li>
 * </ul>
 *
 * @see FlexLayout
 */
public final class FlexParams {

    /** 默认 Flex 参数（不增长，可收缩，使用自然尺寸） */
    public static final FlexParams DEFAULT = new FlexParams(0, 1, -1);

    /** 增长因子（≥0） */
    private final int flexGrow;
    /** 收缩因子（≥0） */
    private final int flexShrink;
    /** 初始主轴尺寸（-1 = 使用当前尺寸） */
    private final int basis;

    /**
     * 创建 Flex 参数。
     *
     * @param flexGrow 增长因子（负数会被钳制为 0）
     * @param flexShrink 收缩因子（负数会被钳制为 0）
     * @param basis 初始主轴尺寸（-1 表示使用元素当前尺寸）
     */
    public FlexParams(final int flexGrow, final int flexShrink, final int basis) {
        this.flexGrow = Math.max(0, flexGrow);
        this.flexShrink = Math.max(0, flexShrink);
        this.basis = basis;
    }

    /**
     * 获取增长因子。
     * 当布局有剩余空间时，元素会根据此值按比例扩展。
     *
     * @return 增长因子（≥0）
     */
    public int getFlexGrow() {
        return flexGrow;
    }

    /**
     * 获取收缩因子。
     * 当布局空间不足时，元素会根据此值按比例收缩。
     *
     * @return 收缩因子（≥0）
     */
    public int getFlexShrink() {
        return flexShrink;
    }

    /**
     * 期望主轴尺寸，-1 表示使用当前尺寸。
     */
    public int getBasis() {
        return basis;
    }
}
