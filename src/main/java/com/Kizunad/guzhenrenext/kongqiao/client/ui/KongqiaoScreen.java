package com.Kizunad.guzhenrenext.kongqiao.client.ui;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoConstants;
import com.Kizunad.guzhenrenext.kongqiao.menu.KongqiaoMenu;
import com.Kizunad.guzhenrenext.network.ServerboundKongqiaoActionPayload;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.ContainerUI;
import com.Kizunad.tinyUI.controls.InventoryUI;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.Anchor;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 空窍 TinyUI 界面。
 */
public class KongqiaoScreen extends TinyUIContainerScreen<KongqiaoMenu> {

    private static final int SLOT_SIZE = 18;
    private static final int GRID_GAP = 2;
    private static final int GRID_PADDING = 2;
    private static final int CONTENT_PADDING = 14;
    private static final int TITLE_HEIGHT = 16;
    private static final int BUTTON_HEIGHT = 18;
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_GAP = 8;
    private static final int SECTION_GAP = 12;
    private static final int PLAYER_SECTION_GAP = 18;
    private static final int SCROLLBAR_ALLOWANCE = 14;
    private static final int HINT_MARGIN_TOP = 4;
    private static final int HINT_LABEL_HEIGHT = 12;
    private static final int BUTTON_EXTRA_GAP = 10;

    private final Theme theme = Theme.vanilla();

    public KongqiaoScreen(
        KongqiaoMenu menu,
        Inventory playerInventory,
        Component title
    ) {
        super(menu, playerInventory, title);
    }

    @Override
    protected boolean shouldEnforceMenuBinding() {
        return true;
    }

    @Override
    protected void initUI(UIRoot root) {
        int visibleRows = Math.max(menu.getVisibleRows(), 1);
        int totalSlots = menu.getTotalSlots();

        int gridWidth =
            KongqiaoConstants.COLUMNS * SLOT_SIZE +
            (KongqiaoConstants.COLUMNS - 1) * GRID_GAP +
            GRID_PADDING * 2;
        int viewportWidth = gridWidth + SCROLLBAR_ALLOWANCE;
        int viewportHeight =
            visibleRows * SLOT_SIZE +
            Math.max(0, visibleRows - 1) * GRID_GAP +
            GRID_PADDING * 2;

        UIElement window = new UIElement() {};
        UIElement kongqiaoGrid = ContainerUI.scrollableGrid(
            0,
            totalSlots,
            KongqiaoConstants.COLUMNS,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        kongqiaoGrid.setFrame(0, 0, viewportWidth, viewportHeight);

        Label title = new Label(Component.literal("空窍"), theme);
        title.setFrame(0, 0, viewportWidth, TITLE_HEIGHT);

        Label hint = new Label(
            Component.literal("可通过拓展按钮逐行解锁更多空窍"),
            theme
        );

        Button expandButton = new Button("Expand 空窍", theme);
        expandButton.setOnClick(
            () -> sendAction(ServerboundKongqiaoActionPayload.Action.EXPAND)
        );
        Button attackButton = new Button("Go to Attack Inventory", theme);
        attackButton.setOnClick(
            () -> sendAction(
                ServerboundKongqiaoActionPayload.Action.OPEN_ATTACK
            )
        );

        UIElement playerGrid = InventoryUI.playerInventoryGrid(
            menu.getTotalSlots(),
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );

        int buttonRowWidth = BUTTON_WIDTH * 2 + BUTTON_GAP;
        int contentWidth = Math.max(viewportWidth, playerGrid.getWidth());
        int windowWidth = contentWidth + CONTENT_PADDING * 2;
        int windowHeight =
            TITLE_HEIGHT +
            SECTION_GAP +
            viewportHeight +
            SECTION_GAP +
            BUTTON_HEIGHT +
            PLAYER_SECTION_GAP +
            playerGrid.getHeight() +
            CONTENT_PADDING * 2;

        window.setFrame(0, 0, windowWidth, windowHeight);
        Anchor.apply(
            window,
            root.getWidth(),
            root.getHeight(),
            new Anchor.Spec(
                windowWidth,
                windowHeight,
                Anchor.Horizontal.CENTER,
                Anchor.Vertical.CENTER,
                0,
                0
            )
        );
        root.addChild(window);

        int centerX = CONTENT_PADDING + (contentWidth - viewportWidth) / 2;
        title.setFrame(centerX, CONTENT_PADDING, viewportWidth, TITLE_HEIGHT);
        window.addChild(title);

        kongqiaoGrid.setFrame(
            centerX,
            CONTENT_PADDING + TITLE_HEIGHT + SECTION_GAP,
            viewportWidth,
            viewportHeight
        );
        window.addChild(kongqiaoGrid);

        hint.setFrame(
            centerX,
            kongqiaoGrid.getY() + kongqiaoGrid.getHeight() + HINT_MARGIN_TOP,
            viewportWidth,
            HINT_LABEL_HEIGHT
        );
        window.addChild(hint);

        int buttonRowY =
            kongqiaoGrid.getY() + kongqiaoGrid.getHeight() + SECTION_GAP + BUTTON_EXTRA_GAP;
        int buttonRowX =
            CONTENT_PADDING + (contentWidth - buttonRowWidth) / 2;
        expandButton.setFrame(
            buttonRowX,
            buttonRowY,
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        attackButton.setFrame(
            buttonRowX + BUTTON_WIDTH + BUTTON_GAP,
            buttonRowY,
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        window.addChild(expandButton);
        window.addChild(attackButton);

        playerGrid.setFrame(
            CONTENT_PADDING + (contentWidth - playerGrid.getWidth()) / 2,
            buttonRowY + BUTTON_HEIGHT + PLAYER_SECTION_GAP,
            playerGrid.getWidth(),
            playerGrid.getHeight()
        );
        window.addChild(playerGrid);
    }

    private void sendAction(ServerboundKongqiaoActionPayload.Action action) {
        PacketDistributor.sendToServer(
            new ServerboundKongqiaoActionPayload(action)
        );
    }
}
