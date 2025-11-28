package com.Kizunad.tinyUI.layout;

/**
 * Flex 子项参数。
 */
public final class FlexParams {

    public static final FlexParams DEFAULT = new FlexParams(0, 1, -1);

    private final int flexGrow;
    private final int flexShrink;
    private final int basis;

    public FlexParams(final int flexGrow, final int flexShrink, final int basis) {
        this.flexGrow = Math.max(0, flexGrow);
        this.flexShrink = Math.max(0, flexShrink);
        this.basis = basis;
    }

    public int getFlexGrow() {
        return flexGrow;
    }

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
