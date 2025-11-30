package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.layout.GridLayout;
import com.Kizunad.tinyUI.theme.Theme;

/**
 * 玩家库存区域的快捷工厂。
 * 默认生成 27 个背包槽 + 9 个热键栏槽的 9x4 网格。
 */
public final class InventoryUI {

    public static final int PLAYER_COLS = 9;
    public static final int PLAYER_ROWS = 4;

    private InventoryUI() {
    }

    /**
     * 构建一个标准玩家库存网格（27 背包 + 9 热键栏）。
     *
     * @param slotStart 起始 Slot 索引（通常为自定义槽位数量）
     * @param slotSize  槽位尺寸
     * @param gap       槽位间距
     * @param padding   内边距
     * @param theme     主题
     * @return 已布局好的 UIElement，内部包含 36 个 {@link UISlot}
     */
    public static UIElement playerInventoryGrid(final int slotStart, final int slotSize,
                                                final int gap, final int padding,
                                                final Theme theme) {
        final int cellSize = Math.max(1, slotSize);
        final int innerGap = Math.max(0, gap);
        final int innerPadding = Math.max(0, padding);

        final UIElement grid = new UIElement() { };
        final int width = PLAYER_COLS * cellSize + (PLAYER_COLS - 1) * innerGap + innerPadding * 2;
        final int height = PLAYER_ROWS * cellSize + (PLAYER_ROWS - 1) * innerGap + innerPadding * 2;
        grid.setFrame(0, 0, width, height);

        final int totalSlots = PLAYER_COLS * PLAYER_ROWS;
        for (int i = 0; i < totalSlots; i++) {
            final UISlot slot = new UISlot(slotStart + i, theme);
            slot.setFrame(0, 0, cellSize, cellSize);
            grid.addChild(slot);
        }
        new GridLayout(PLAYER_COLS, PLAYER_ROWS, innerGap, innerPadding).layout(grid, null);
        return grid;
    }
}
