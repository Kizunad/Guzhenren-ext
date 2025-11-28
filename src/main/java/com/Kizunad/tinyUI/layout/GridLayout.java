package com.Kizunad.tinyUI.layout;

import com.Kizunad.tinyUI.core.UIElement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 简化版均分网格布局。
 */
public final class GridLayout {

    private final int columns;
    private final int rows;
    private final int gap;
    private final int padding;

    public GridLayout(final int columns, final int rows, final int gap, final int padding) {
        this.columns = Math.max(1, columns);
        this.rows = rows;
        this.gap = Math.max(0, gap);
        this.padding = Math.max(0, padding);
    }

    public void layout(final UIElement container, final Map<UIElement, GridParams> params) {
        Objects.requireNonNull(container, "container");
        final List<UIElement> children = container.getChildren();
        if (children.isEmpty()) {
            container.onLayoutUpdated();
            return;
        }
        final Map<UIElement, GridParams> paramMap =
                params == null ? Collections.emptyMap() : params;
        final int colCount = Math.max(1, columns);
        final int rowCount = rows > 0
                ? rows
                : (int) Math.ceil((double) children.size() / colCount);
        final int cellWidth = Math.max(0, (container.getWidth() - padding * 2
                - gap * (colCount - 1)) / colCount);
        final int cellHeight = Math.max(0, (container.getHeight() - padding * 2
                - gap * (rowCount - 1)) / rowCount);

        int currentCol = 0;
        int currentRow = 0;
        for (final UIElement child : children) {
            final GridParams gp = paramMap.getOrDefault(child, GridParams.SINGLE);
            final int colSpan = Math.min(gp.getColSpan(), colCount);
            final int rowSpan = Math.min(gp.getRowSpan(), rowCount - currentRow);

            final int width = colSpan * cellWidth + gap * (colSpan - 1);
            final int height = rowSpan * cellHeight + gap * (rowSpan - 1);
            final int childX = padding + currentCol * (cellWidth + gap);
            final int childY = padding + currentRow * (cellHeight + gap);
            child.setFrame(childX, childY, width, height);
            child.onLayoutUpdated();

            currentCol += colSpan;
            if (currentCol >= colCount) {
                currentCol = 0;
                currentRow++;
                if (currentRow >= rowCount) {
                    break;
                }
            }
        }
        container.onLayoutUpdated();
    }
}
