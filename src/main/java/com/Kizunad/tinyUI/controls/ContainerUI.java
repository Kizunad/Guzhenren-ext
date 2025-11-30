package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.layout.GridLayout;
import com.Kizunad.tinyUI.theme.Theme;

/**
 * 通用容器区域的快捷工厂。
 * 提供可滚动的 Slot 网格，复用在任意 ContainerMenu 上。
 */
public final class ContainerUI {

    private ContainerUI() {}

    /**
     * 构建一个可滚动的容器区域，按列数均分并使用 {@link UISlot} 作为占位。
     *
     * @param slotStart  起始 Slot 索引
     * @param slotCount  Slot 数量
     * @param columns    网格列数
     * @param slotSize   单个槽位边长
     * @param gap        槽位间距
     * @param padding    网格内边距
     * @param theme      主题
     * @return ScrollContainer，内部已放置并布局所有 UISlot
     */
    public static ScrollContainer scrollableGrid(
        final int slotStart,
        final int slotCount,
        final int columns,
        final int slotSize,
        final int gap,
        final int padding,
        final Theme theme
    ) {
        final int safeSlotCount = Math.max(0, slotCount);
        final int cols = Math.max(1, columns);
        final int cellSize = Math.max(1, slotSize);
        final int innerGap = Math.max(0, gap);
        final int innerPadding = Math.max(0, padding);
        final int rows = safeSlotCount == 0
            ? 0
            : (int) Math.ceil(safeSlotCount / (double) cols);

        final ScrollContainer scroll = new ScrollContainer(theme);
        final UIElement content = new UIElement() {};

        final int width =
            cols * cellSize + (cols - 1) * innerGap + innerPadding * 2;
        final int height = rows > 0
            ? rows * cellSize + (rows - 1) * innerGap + innerPadding * 2
            : innerPadding * 2;
        content.setFrame(0, 0, width, height);

        for (int i = 0; i < safeSlotCount; i++) {
            final UISlot slot = new UISlot(slotStart + i, theme);
            slot.setFrame(0, 0, cellSize, cellSize);
            content.addChild(slot);
        }
        new GridLayout(cols, 0, innerGap, innerPadding).layout(content, null);
        scroll.setContent(content);
        return scroll;
    }
}
