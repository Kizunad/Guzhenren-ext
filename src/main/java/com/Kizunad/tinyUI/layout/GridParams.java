package com.Kizunad.tinyUI.layout;

/**
 * Grid 子项参数（当前仅支持 1x1，占位为后续扩展跨行列）。
 */
public final class GridParams {

    public static final GridParams SINGLE = new GridParams(1, 1);

    private final int colSpan;
    private final int rowSpan;

    public GridParams(final int colSpan, final int rowSpan) {
        this.colSpan = Math.max(1, colSpan);
        this.rowSpan = Math.max(1, rowSpan);
    }

    public int getColSpan() {
        return colSpan;
    }

    public int getRowSpan() {
        return rowSpan;
    }
}
