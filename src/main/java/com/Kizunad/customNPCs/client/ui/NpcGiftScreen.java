package com.Kizunad.customNPCs.client.ui;

import com.Kizunad.customNPCs.menu.NpcGiftMenu;
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
 * 赠礼界面：9 格礼物槽 + 玩家背包，右下有确认按钮。
 */
public class NpcGiftScreen extends TinyUIContainerScreen<NpcGiftMenu> {

    private static final int WINDOW_WIDTH = 220;
    private static final int WINDOW_HEIGHT = 240;
    private static final int SLOT_SIZE = 18;
    private static final int GRID_GAP = 2;
    private static final int GRID_PADDING = 1;
    private static final int GIFT_COLS = 3;
    private static final int GIFT_SLOT_COUNT = 9;
    private static final int TITLE_Y = 6;
    private static final int TITLE_HEIGHT = 12;
    private static final int TITLE_WIDTH = 180;
    private static final int GIFT_GRID_X = 10;
    private static final int GIFT_GRID_Y = 22;
    private static final int PLAYER_GRID_X = 10;
    private static final int PLAYER_GRID_Y = 110;
    private static final int CONFIRM_WIDTH = 80;
    private static final int CONFIRM_HEIGHT = 20;
    private static final int CONFIRM_MARGIN = 10;

    private final Theme theme;

    public NpcGiftScreen(
        NpcGiftMenu menu,
        Inventory playerInventory,
        Component title
    ) {
        super(menu, playerInventory, title);
        this.theme = Theme.vanilla();
    }

    @Override
    protected double getUiScale() {
        return com.Kizunad.guzhenrenext.config.ClientConfig.INSTANCE.kongQiaoUiScale.get();
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

        String npcName = menu.getNpcEntityId() >= 0 ? "Gift to NPC" : "Gift";
        Label titleLabel = new Label(npcName, theme);
        titleLabel.setFrame(GIFT_GRID_X, TITLE_Y, TITLE_WIDTH, TITLE_HEIGHT);
        window.addChild(titleLabel);

        UIElement giftGrid = ContainerUI.scrollableGrid(
            0,
            GIFT_SLOT_COUNT,
            GIFT_COLS,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        giftGrid.setFrame(
            GIFT_GRID_X,
            GIFT_GRID_Y,
            giftGrid.getWidth(),
            giftGrid.getHeight()
        );
        window.addChild(giftGrid);

        UIElement playerGrid = InventoryUI.playerInventoryGrid(
            GIFT_SLOT_COUNT,
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
