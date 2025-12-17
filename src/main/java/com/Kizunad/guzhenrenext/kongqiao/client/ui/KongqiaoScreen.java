package com.Kizunad.guzhenrenext.kongqiao.client.ui;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoConstants;
import com.Kizunad.guzhenrenext.kongqiao.KongqiaoI18n;
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
    private static final int TITLE_HEIGHT = 16;
    private static final int BUTTON_HEIGHT = 18;
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_GAP = 8;
    private static final int BUTTON_COUNT = 3;
    private static final int SECTION_GAP = 12;
    private static final int SCROLLBAR_ALLOWANCE = 14;
    private static final int HINT_MARGIN_TOP = 4;
    private static final int HINT_LABEL_HEIGHT = 12;
    private static final int MAIN_PANEL_WIDTH = 600;
    private static final int MAIN_PANEL_PADDING = 16;
    private static final int GRID_PANEL_MIN_WIDTH = 550;
    private static final int GRID_PANEL_PADDING = 12;
    private static final int BUTTON_ROW_MARGIN = 18;
    private static final int PLAYER_PANEL_WIDTH = 450;
    private static final int PLAYER_PANEL_PADDING = 12;
    private static final int PLAYER_TITLE_HEIGHT = 14;
    private static final int PANEL_GAP = 20;

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
    protected double getUiScale() {
        return com.Kizunad.guzhenrenext.config.ClientConfig.INSTANCE.kongQiaoUiScale.get();
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

        Label title = new Label(
            KongqiaoI18n.text(KongqiaoI18n.KONGQIAO_TITLE),
            theme
        );
        Label hint = new Label(
            KongqiaoI18n.text(KongqiaoI18n.KONGQIAO_HINT),
            theme
        );

        Button expandButton = new Button(
            KongqiaoI18n.text(KongqiaoI18n.KONGQIAO_BUTTON_EXPAND),
            theme
        );
        expandButton.setOnClick(() ->
            sendAction(ServerboundKongqiaoActionPayload.Action.EXPAND)
        );
        Button feedButton = new Button(
            KongqiaoI18n.text(KongqiaoI18n.COMMON_FEED_BUTTON),
            theme
        );
        feedButton.setOnClick(() ->
            sendAction(ServerboundKongqiaoActionPayload.Action.OPEN_FEED)
        );
        Button attackButton = new Button(
            KongqiaoI18n.text(KongqiaoI18n.KONGQIAO_BUTTON_ATTACK),
            theme
        );
        attackButton.setOnClick(() ->
            sendAction(ServerboundKongqiaoActionPayload.Action.OPEN_ATTACK)
        );

        UIElement playerGrid = InventoryUI.playerInventoryGrid(
            menu.getTotalSlots(),
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );

        KongqiaoLayout layout = calculateLayout(
            viewportWidth,
            viewportHeight,
            playerGrid.getHeight()
        );
        UIElement window = createWindow(root, layout.windowHeight());
        buildMainPanel(
            window,
            layout,
            kongqiaoGrid,
            title,
            hint,
            new Button[] { expandButton, feedButton, attackButton }
        );
        buildPlayerPanel(window, layout, playerGrid);
    }

    private void sendAction(ServerboundKongqiaoActionPayload.Action action) {
        PacketDistributor.sendToServer(
            new ServerboundKongqiaoActionPayload(action)
        );
    }

    private KongqiaoLayout calculateLayout(
        int viewportWidth,
        int viewportHeight,
        int playerGridHeight
    ) {
        int containerInnerWidth = Math.max(
            viewportWidth,
            GRID_PANEL_MIN_WIDTH - GRID_PANEL_PADDING * 2
        );
        int availableMainWidth = MAIN_PANEL_WIDTH - MAIN_PANEL_PADDING * 2;
        int containerWidth = Math.min(
            containerInnerWidth + GRID_PANEL_PADDING * 2,
            availableMainWidth
        );
        int containerInnerHeight =
            viewportHeight + HINT_MARGIN_TOP + HINT_LABEL_HEIGHT;
        int containerHeight = containerInnerHeight + GRID_PANEL_PADDING * 2;
        int mainPanelHeight =
            MAIN_PANEL_PADDING +
            TITLE_HEIGHT +
            SECTION_GAP +
            containerHeight +
            BUTTON_ROW_MARGIN +
            BUTTON_HEIGHT +
            MAIN_PANEL_PADDING;
        int playerPanelHeight =
            PLAYER_PANEL_PADDING +
            PLAYER_TITLE_HEIGHT +
            SECTION_GAP +
            playerGridHeight +
            PLAYER_PANEL_PADDING;
        int windowHeight = mainPanelHeight + PANEL_GAP + playerPanelHeight;
        return new KongqiaoLayout(
            viewportWidth,
            viewportHeight,
            containerInnerWidth,
            containerWidth,
            containerHeight,
            availableMainWidth,
            mainPanelHeight,
            playerPanelHeight,
            windowHeight
        );
    }

    private UIElement createWindow(UIRoot root, int windowHeight) {
        UIElement window = new UIElement() {};
        window.setFrame(0, 0, MAIN_PANEL_WIDTH, windowHeight);
        Anchor.apply(
            window,
            root.getWidth(),
            root.getHeight(),
            new Anchor.Spec(
                MAIN_PANEL_WIDTH,
                windowHeight,
                Anchor.Horizontal.CENTER,
                Anchor.Vertical.CENTER,
                0,
                0
            )
        );
        root.addChild(window);
        return window;
    }

    private void buildMainPanel(
        UIElement window,
        KongqiaoLayout layout,
        UIElement kongqiaoGrid,
        Label title,
        Label hint,
        Button[] buttons
    ) {
        SolidPanel mainPanel = new SolidPanel(theme);
        mainPanel.setFrame(0, 0, MAIN_PANEL_WIDTH, layout.mainPanelHeight());
        window.addChild(mainPanel);

        title.setFrame(
            MAIN_PANEL_PADDING,
            MAIN_PANEL_PADDING,
            MAIN_PANEL_WIDTH - MAIN_PANEL_PADDING * 2,
            TITLE_HEIGHT
        );
        mainPanel.addChild(title);

        SolidPanel containerPanel = new SolidPanel(theme);
        int containerX =
            MAIN_PANEL_PADDING +
            (layout.availableMainWidth() - layout.containerWidth()) / 2;
        int containerY = MAIN_PANEL_PADDING + TITLE_HEIGHT + SECTION_GAP;
        containerPanel.setFrame(
            containerX,
            containerY,
            layout.containerWidth(),
            layout.containerHeight()
        );
        mainPanel.addChild(containerPanel);

        int gridOffsetX =
            GRID_PANEL_PADDING +
            (layout.containerInnerWidth() - layout.viewportWidth()) / 2;
        kongqiaoGrid.setFrame(
            gridOffsetX,
            GRID_PANEL_PADDING,
            layout.viewportWidth(),
            layout.viewportHeight()
        );
        containerPanel.addChild(kongqiaoGrid);

        hint.setFrame(
            GRID_PANEL_PADDING,
            kongqiaoGrid.getY() + kongqiaoGrid.getHeight() + HINT_MARGIN_TOP,
            layout.containerInnerWidth(),
            HINT_LABEL_HEIGHT
        );
        containerPanel.addChild(hint);

        int buttonRowWidth =
            BUTTON_WIDTH * BUTTON_COUNT + BUTTON_GAP * (BUTTON_COUNT - 1);
        int buttonRowX = (MAIN_PANEL_WIDTH - buttonRowWidth) / 2;
        int buttonRowY =
            containerY + layout.containerHeight() + BUTTON_ROW_MARGIN;
        for (int i = 0; i < buttons.length; i++) {
            Button button = buttons[i];
            button.setFrame(
                buttonRowX + i * (BUTTON_WIDTH + BUTTON_GAP),
                buttonRowY,
                BUTTON_WIDTH,
                BUTTON_HEIGHT
            );
            mainPanel.addChild(button);
        }
    }

    private void buildPlayerPanel(
        UIElement window,
        KongqiaoLayout layout,
        UIElement playerGrid
    ) {
        SolidPanel playerPanel = new SolidPanel(theme);
        int playerPanelX = (MAIN_PANEL_WIDTH - PLAYER_PANEL_WIDTH) / 2;
        playerPanel.setFrame(
            playerPanelX,
            layout.mainPanelHeight() + PANEL_GAP,
            PLAYER_PANEL_WIDTH,
            layout.playerPanelHeight()
        );
        window.addChild(playerPanel);

        Label playerLabel = new Label(
            KongqiaoI18n.text(KongqiaoI18n.COMMON_PLAYER_INVENTORY),
            theme
        );
        playerLabel.setFrame(
            PLAYER_PANEL_PADDING,
            PLAYER_PANEL_PADDING,
            PLAYER_PANEL_WIDTH - PLAYER_PANEL_PADDING * 2,
            PLAYER_TITLE_HEIGHT
        );
        playerPanel.addChild(playerLabel);

        int innerPlayerWidth = PLAYER_PANEL_WIDTH - PLAYER_PANEL_PADDING * 2;
        int playerGridX =
            PLAYER_PANEL_PADDING +
            (innerPlayerWidth - playerGrid.getWidth()) / 2;
        playerGrid.setFrame(
            playerGridX,
            playerLabel.getY() + PLAYER_TITLE_HEIGHT + SECTION_GAP,
            playerGrid.getWidth(),
            playerGrid.getHeight()
        );
        playerPanel.addChild(playerGrid);
    }

    private record KongqiaoLayout(
        int viewportWidth,
        int viewportHeight,
        int containerInnerWidth,
        int containerWidth,
        int containerHeight,
        int availableMainWidth,
        int mainPanelHeight,
        int playerPanelHeight,
        int windowHeight
    ) {}
}
