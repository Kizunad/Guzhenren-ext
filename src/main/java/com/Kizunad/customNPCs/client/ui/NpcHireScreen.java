package com.Kizunad.customNPCs.client.ui;

import com.Kizunad.customNPCs.menu.NpcHireMenu;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.ContainerUI;
import com.Kizunad.tinyUI.controls.InventoryUI;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.Anchor;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 雇佣支付界面：3x9 提供物品槽 + 玩家背包，点击确认提交雇佣申请。
 */
public class NpcHireScreen extends TinyUIContainerScreen<NpcHireMenu> {

    private static final int WINDOW_WIDTH = 260;
    private static final int WINDOW_HEIGHT = 300;
    private static final int SLOT_SIZE = 18;
    private static final int GRID_GAP = 2;
    private static final int GRID_PADDING = 1;
    private static final int HIRE_COLS = 9;
    private static final int HIRE_SLOT_COUNT = 27;
    private static final int TITLE_Y = 6;
    private static final int TITLE_HEIGHT = 12;
    private static final int TITLE_WIDTH = 220;
    private static final int HIRE_GRID_X = 10;
    private static final int HIRE_GRID_Y = 22;
    private static final int PLAYER_GRID_X = 10;
    private static final int PLAYER_GRID_Y = 140;
    private static final int CONFIRM_WIDTH = 100;
    private static final int CONFIRM_HEIGHT = 20;
    private static final int CONFIRM_MARGIN = 12;

    private final Theme theme;

    public NpcHireScreen(
        NpcHireMenu menu,
        Inventory playerInventory,
        Component title
    ) {
        super(menu, playerInventory, title);
        this.theme = Theme.vanilla();
    }

    @Override
    protected void initUI(UIRoot root) {
        root.setViewport(width, height);

        UIElement window = new UIElement() {};
        Anchor.apply(
            window,
            root.getWidth(),
            root.getHeight(),
            new Anchor.Spec(
                WINDOW_WIDTH,
                WINDOW_HEIGHT,
                Anchor.Horizontal.CENTER,
                Anchor.Vertical.CENTER,
                0,
                0
            )
        );
        root.addChild(window);

        String npcName = menu.getNpcEntityId() >= 0
            ? "Hire " + menu.getNpcEntityId()
            : "Hire";
        Label titleLabel = new Label(npcName, theme);
        titleLabel.setFrame(HIRE_GRID_X, TITLE_Y, TITLE_WIDTH, TITLE_HEIGHT);
        window.addChild(titleLabel);

        UIElement hireGrid = ContainerUI.scrollableGrid(
            0,
            HIRE_SLOT_COUNT,
            HIRE_COLS,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        hireGrid.setFrame(
            HIRE_GRID_X,
            HIRE_GRID_Y,
            hireGrid.getWidth(),
            hireGrid.getHeight()
        );
        window.addChild(hireGrid);

        UIElement playerGrid = InventoryUI.playerInventoryGrid(
            HIRE_SLOT_COUNT,
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
        window.addChild(playerGrid);

        Button confirm = new Button("Confirm", theme);
        confirm.setFrame(
            WINDOW_WIDTH - CONFIRM_WIDTH - CONFIRM_MARGIN,
            WINDOW_HEIGHT - CONFIRM_HEIGHT - CONFIRM_MARGIN,
            CONFIRM_WIDTH,
            CONFIRM_HEIGHT
        );
        confirm.setOnClick(this::clickConfirm);
        window.addChild(confirm);
    }

    private void clickConfirm() {
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (gameMode != null) {
            gameMode.handleInventoryButtonClick(
                this.menu.containerId,
                this.menu.getConfirmButtonId()
            );
        }
    }
}
