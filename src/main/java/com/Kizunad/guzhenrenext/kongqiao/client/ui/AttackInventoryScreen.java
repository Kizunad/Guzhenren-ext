package com.Kizunad.guzhenrenext.kongqiao.client.ui;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoI18n;
import com.Kizunad.guzhenrenext.kongqiao.menu.AttackInventoryMenu;
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
 * 攻击背包页面。
 */
public class AttackInventoryScreen
    extends TinyUIContainerScreen<AttackInventoryMenu> {

    private static final int SLOT_SIZE = 18;
    private static final int GRID_GAP = 2;
    private static final int GRID_PADDING = 2;
    private static final int TITLE_HEIGHT = 16;
    private static final int BUTTON_HEIGHT = 18;
    private static final int SECTION_GAP = 12;
    private static final int INFO_LABEL_HEIGHT = 12;
    private static final int MAIN_PANEL_WIDTH = 600;
    private static final int MAIN_PANEL_PADDING = 16;
    private static final int GRID_PANEL_WIDTH = 440;
    private static final int GRID_PANEL_PADDING = 12;
    private static final int GRID_HINT_MARGIN = 8;
    private static final int EXTRA_PANEL_WIDTH = 130;
    private static final int EXTRA_PANEL_PADDING = 12;
    private static final int COLUMN_MARGIN = 10;
    private static final int COLUMN_GAP = 10;
    private static final int BUTTON_STACK_GAP = 8;
    private static final int BUTTON_STACK_TOP = 20;
    private static final int PLAYER_PANEL_WIDTH = 450;
    private static final int PLAYER_PANEL_PADDING = 12;
    private static final int PLAYER_TITLE_HEIGHT = 14;
    private static final int PANEL_VERTICAL_GAP = 20;

    private final Theme theme = Theme.vanilla();

    public AttackInventoryScreen(
        AttackInventoryMenu menu,
        Inventory inventory,
        Component title
    ) {
        super(menu, inventory, title);
    }

    @Override
    protected boolean shouldEnforceMenuBinding() {
        return true;
    }

    @Override
    protected void initUI(UIRoot root) {
        int containerSlots = menu.getContainerSlots();
        int columns = menu.getInventory().getColumns();
        int rows = menu.getInventory().getRows();

        int gridWidth =
            columns * SLOT_SIZE + (columns - 1) * GRID_GAP + GRID_PADDING * 2;
        int gridHeight =
            rows * SLOT_SIZE + (rows - 1) * GRID_GAP + GRID_PADDING * 2;
        UIElement attackGrid = ContainerUI.scrollableGrid(
            0,
            containerSlots,
            columns,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        attackGrid.setFrame(0, 0, gridWidth, gridHeight);

        Label attackTitle = new Label(
            KongqiaoI18n.text(KongqiaoI18n.ATTACK_TITLE),
            theme
        );
        // 预留提示标签，布局保留但暂不显示文字
        Label gridInfo = new Label(Component.empty(), theme);
        Label extraHint = new Label(Component.empty(), theme);

        Button feedButton = new Button(
            KongqiaoI18n.text(KongqiaoI18n.COMMON_FEED_BUTTON),
            theme
        );
        feedButton.setOnClick(() ->
            sendAction(ServerboundKongqiaoActionPayload.Action.OPEN_FEED)
        );
        Button swapButton = new Button(
            KongqiaoI18n.text(KongqiaoI18n.ATTACK_BUTTON_SWAP),
            theme
        );
        swapButton.setOnClick(() ->
            sendAction(ServerboundKongqiaoActionPayload.Action.SWAP_ATTACK)
        );
        Button returnButton = new Button(
            KongqiaoI18n.text(KongqiaoI18n.ATTACK_BUTTON_RETURN),
            theme
        );
        returnButton.setOnClick(() ->
            sendAction(ServerboundKongqiaoActionPayload.Action.OPEN_KONGQIAO)
        );

        UIElement playerGrid = InventoryUI.playerInventoryGrid(
            containerSlots,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );

        AttackLayout layout = calculateLayout(
            gridWidth,
            gridHeight,
            playerGrid.getHeight()
        );
        UIElement window = createWindow(root, layout.windowHeight());
        buildAttackPanel(
            window,
            layout,
            attackGrid,
            attackTitle,
            gridInfo,
            extraHint,
            new Button[] { feedButton, swapButton, returnButton }
        );
        buildPlayerPanel(window, layout, playerGrid);
    }

    private void sendAction(ServerboundKongqiaoActionPayload.Action action) {
        PacketDistributor.sendToServer(
            new ServerboundKongqiaoActionPayload(action)
        );
    }

    private AttackLayout calculateLayout(
        int gridWidth,
        int gridHeight,
        int playerGridHeight
    ) {
        int gridPanelAvailable =
            MAIN_PANEL_WIDTH -
            (COLUMN_MARGIN * 2 + COLUMN_GAP + EXTRA_PANEL_WIDTH);
        int gridPanelWidth = Math.max(
            Math.min(GRID_PANEL_WIDTH, gridPanelAvailable),
            gridWidth + GRID_PANEL_PADDING * 2
        );
        int gridPanelInnerWidth = gridPanelWidth - GRID_PANEL_PADDING * 2;
        int gridPanelHeight =
            gridHeight +
            GRID_HINT_MARGIN +
            INFO_LABEL_HEIGHT +
            GRID_PANEL_PADDING * 2;
        int attackPanelHeight =
            MAIN_PANEL_PADDING +
            TITLE_HEIGHT +
            SECTION_GAP +
            gridPanelHeight +
            MAIN_PANEL_PADDING;
        int playerPanelHeight =
            PLAYER_PANEL_PADDING +
            PLAYER_TITLE_HEIGHT +
            SECTION_GAP +
            playerGridHeight +
            PLAYER_PANEL_PADDING;
        int windowHeight =
            attackPanelHeight + PANEL_VERTICAL_GAP + playerPanelHeight;
        return new AttackLayout(
            gridWidth,
            gridHeight,
            gridPanelWidth,
            gridPanelHeight,
            gridPanelInnerWidth,
            attackPanelHeight,
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

    private void buildAttackPanel(
        UIElement window,
        AttackLayout layout,
        UIElement attackGrid,
        Label attackTitle,
        Label gridInfo,
        Label extraHint,
        Button[] actionButtons
    ) {
        SolidPanel attackPanel = new SolidPanel(theme);
        attackPanel.setFrame(
            0,
            0,
            MAIN_PANEL_WIDTH,
            layout.attackPanelHeight()
        );
        window.addChild(attackPanel);

        attackTitle.setFrame(
            MAIN_PANEL_PADDING,
            MAIN_PANEL_PADDING,
            MAIN_PANEL_WIDTH - MAIN_PANEL_PADDING * 2,
            TITLE_HEIGHT
        );
        attackPanel.addChild(attackTitle);

        int contentY = MAIN_PANEL_PADDING + TITLE_HEIGHT + SECTION_GAP;
        SolidPanel gridPanel = new SolidPanel(theme);
        gridPanel.setFrame(
            COLUMN_MARGIN,
            contentY,
            layout.gridPanelWidth(),
            layout.gridPanelHeight()
        );
        attackPanel.addChild(gridPanel);

        int gridOffsetX =
            GRID_PANEL_PADDING +
            (layout.gridPanelInnerWidth() - layout.gridWidth()) / 2;
        attackGrid.setFrame(
            gridOffsetX,
            GRID_PANEL_PADDING,
            layout.gridWidth(),
            layout.gridHeight()
        );
        gridPanel.addChild(attackGrid);

        gridInfo.setFrame(
            GRID_PANEL_PADDING,
            attackGrid.getY() + attackGrid.getHeight() + GRID_HINT_MARGIN,
            layout.gridPanelInnerWidth(),
            INFO_LABEL_HEIGHT
        );
        gridPanel.addChild(gridInfo);

        SolidPanel extraPanel = new SolidPanel(theme);
        extraPanel.setFrame(
            MAIN_PANEL_WIDTH - COLUMN_MARGIN - EXTRA_PANEL_WIDTH,
            contentY,
            EXTRA_PANEL_WIDTH,
            layout.gridPanelHeight()
        );
        attackPanel.addChild(extraPanel);

        extraHint.setFrame(
            EXTRA_PANEL_PADDING,
            EXTRA_PANEL_PADDING,
            EXTRA_PANEL_WIDTH - EXTRA_PANEL_PADDING * 2,
            INFO_LABEL_HEIGHT
        );
        extraPanel.addChild(extraHint);

        int buttonWidth = EXTRA_PANEL_WIDTH - EXTRA_PANEL_PADDING * 2;
        int buttonY =
            extraHint.getY() + extraHint.getHeight() + BUTTON_STACK_TOP;
        for (Button button : actionButtons) {
            button.setFrame(
                EXTRA_PANEL_PADDING,
                buttonY,
                buttonWidth,
                BUTTON_HEIGHT
            );
            extraPanel.addChild(button);
            buttonY += BUTTON_HEIGHT + BUTTON_STACK_GAP;
        }
    }

    private void buildPlayerPanel(
        UIElement window,
        AttackLayout layout,
        UIElement playerGrid
    ) {
        SolidPanel playerPanel = new SolidPanel(theme);
        playerPanel.setFrame(
            (MAIN_PANEL_WIDTH - PLAYER_PANEL_WIDTH) / 2,
            layout.attackPanelHeight() + PANEL_VERTICAL_GAP,
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

    private record AttackLayout(
        int gridWidth,
        int gridHeight,
        int gridPanelWidth,
        int gridPanelHeight,
        int gridPanelInnerWidth,
        int attackPanelHeight,
        int playerPanelHeight,
        int windowHeight
    ) {}
}
