package com.Kizunad.tinyUI.demo;

import com.Kizunad.tinyUI.controls.ContainerUI;
import com.Kizunad.tinyUI.controls.InventoryUI;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 简化版容器 Demo：上方自定义 3x9 容器，下方玩家背包 + 热键栏。
 * 采用绝对布局，避免滚动/复杂 Flex 布局导致的重叠问题。
 */
public final class SimpleContainerScreen
    extends TinyUIContainerScreen<ComplexLayoutMenu> {

    private static final int WINDOW_WIDTH = 176;
    private static final int WINDOW_HEIGHT = 222;
    private static final int SLOT_SIZE = 18;
    private static final int GRID_COLS = 9;
    private static final int GRID_ROWS = 3;
    private static final int GRID_GAP = 2;
    private static final int GRID_PADDING = 1;

    private static final int TITLE_Y = 6;
    private static final int TITLE_HEIGHT = 12;
    private static final int CUSTOM_GRID_X = 7;
    private static final int CUSTOM_GRID_Y = 20;
    private static final int PLAYER_GRID_X = 7;
    private static final int PLAYER_GRID_Y = 104;

    private final Theme theme;
    private final int customSlotCount;

    public SimpleContainerScreen(
        ComplexLayoutMenu menu,
        Inventory playerInventory,
        Component title,
        Theme theme
    ) {
        super(menu, playerInventory, title);
        this.theme = theme;
        this.customSlotCount = menu.getCustomSlotCount();
    }

    @Override
    protected boolean shouldEnforceMenuBinding() {
        return false;
    }

    @Override
    protected void initUI(final UIRoot root) {
        root.setViewport(WINDOW_WIDTH, WINDOW_HEIGHT);

        UIElement main = new UIElement() { };
        main.setFrame(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        root.addChild(main);

        Label title = new Label("Simple Container", theme);
        title.setFrame(CUSTOM_GRID_X, TITLE_Y, WINDOW_WIDTH - CUSTOM_GRID_X * 2, TITLE_HEIGHT);
        main.addChild(title);

        UIElement customGrid = ContainerUI.scrollableGrid(
            0,
            customSlotCount,
            GRID_COLS,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        customGrid.setFrame(
            CUSTOM_GRID_X,
            CUSTOM_GRID_Y,
            customGrid.getWidth(),
            customGrid.getHeight()
        );
        main.addChild(customGrid);

        UIElement playerGrid = InventoryUI.playerInventoryGrid(
            customSlotCount,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        playerGrid.setFrame(
            PLAYER_GRID_X,
            PLAYER_GRID_Y,
            playerGrid.getWidth(),
            playerGrid.getHeight()
        );
        main.addChild(playerGrid);
    }
}
