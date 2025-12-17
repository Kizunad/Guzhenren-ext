package com.Kizunad.customNPCs.client.ui;

import com.Kizunad.customNPCs.ai.interaction.NpcTradeHooks;
import com.Kizunad.customNPCs.menu.NpcTradeMenu;
import com.Kizunad.customNPCs.network.ServerboundTradePayload;
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
 * 自定义 NPC 交易界面。
 * 布局结构：
 * - 居中窗口
 * - 左侧：玩家提供物品 + 总价值
 * - 右侧：NPC 提供物品 + 选中价值
 * - 中间：交易意愿 + 确认按钮
 * - 底部：玩家背包
 */
public class NpcTradeScreen extends TinyUIContainerScreen<NpcTradeMenu> {

    private static final int WINDOW_WIDTH = 320;
    private static final int WINDOW_HEIGHT = 240;

    // 布局常量
    private static final int SLOT_SIZE = 18;
    private static final int GRID_GAP = 2;
    private static final int GRID_PADDING = 1;
    private static final int OFFER_GRID_SIZE = 9;
    private static final int OFFER_GRID_COLUMNS = 3;
    private static final int PLAYER_OFFER_START_INDEX = 0;
    private static final int NPC_OFFER_START_INDEX = 9;
    private static final int PLAYER_INV_START_INDEX = 18;

    private static final int PANEL_Y_START = 30;
    private static final int GRID_3X3_SIZE =
        (SLOT_SIZE * 3) + (GRID_GAP * 2) + (GRID_PADDING * 2); // ~60

    private static final int PANEL_MARGIN_SIDE = 30;
    private static final int LEFT_PANEL_X = PANEL_MARGIN_SIDE;
    private static final int RIGHT_PANEL_X =
        WINDOW_WIDTH - PANEL_MARGIN_SIDE - GRID_3X3_SIZE;

    private static final int PLAYER_INV_Y = 150;
    private static final int TITLE_WIDTH = 100;
    private static final int TITLE_X_OFFSET = -36;
    private static final int TITLE_HEIGHT = 12;
    private static final int TITLE_MARGIN_TOP = 8;
    private static final int LABEL_WIDTH = 100;
    private static final int LABEL_HEIGHT = 12;
    private static final int LABEL_MARGIN_TOP = 4;
    private static final int LABEL_Y_OFFSET = 4;
    private static final int WILL_LABEL_WIDTH = 80;
    private static final int WILLINGNESS_LABEL_X_OFFSET = 10;
    private static final int CENTER_PANEL_OFFSET_Y = 18;
    private static final int BUTTON_WIDTH = 60;
    private static final int BUTTON_HEIGHT = 20;

    private final Theme theme;
    private Label leftValLabel;
    private Label rightValLabel;

    public NpcTradeScreen(
        NpcTradeMenu menu,
        Inventory playerInventory,
        Component title
    ) {
        super(menu, playerInventory, title);
        this.theme = Theme.vanilla();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        updateValues();
    }

    private void updateValues() {
        if (menu == null) {
            return;
        }

        // Calculate Player Value
        int playerVal = 0;
        for (int i = 0; i < OFFER_GRID_SIZE; i++) {
            playerVal += NpcTradeHooks.calculateBuyValue(
                menu.playerOffer.getItem(i)
            );
        }
        if (leftValLabel != null) {
            leftValLabel.setText("Total Value: " + playerVal);
        }

        // Calculate NPC Value
        int npcVal = 0;
        float multiplier = menu.getPriceMultiplier();
        for (int i = 0; i < OFFER_GRID_SIZE; i++) {
            npcVal += NpcTradeHooks.calculateSellValue(
                menu.npcOffer.getItem(i),
                multiplier
            );
        }
        if (rightValLabel != null) {
            rightValLabel.setText("Selected Value: " + npcVal);
        }
    }

    @Override
    protected double getUiScale() {
        return com.Kizunad.guzhenrenext.config.ClientConfig.INSTANCE.kongQiaoUiScale.get();
    }

    @Override
    protected void initUI(UIRoot root) {
        UIElement window = createWindow(root);
        addTitle(window);
        addOfferPanels(window);
        addCenterControls(window);
        addPlayerInventory(window);
    }

    private UIElement createWindow(UIRoot root) {
        UIElement window = new UIElement() {};
        window.setFrame(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        Anchor.Spec centerSpec = new Anchor.Spec(
            WINDOW_WIDTH,
            WINDOW_HEIGHT,
            Anchor.Horizontal.CENTER,
            Anchor.Vertical.CENTER,
            0,
            0
        );
        Anchor.apply(window, root.getWidth(), root.getHeight(), centerSpec);
        root.addChild(window);
        return window;
    }

    private void addTitle(UIElement window) {
        Label titleLabel = new Label(this.title.getString(), theme);
        Anchor.Spec titleSpec = new Anchor.Spec(
            TITLE_WIDTH + TITLE_X_OFFSET,
            TITLE_HEIGHT,
            Anchor.Horizontal.CENTER,
            Anchor.Vertical.TOP,
            0,
            TITLE_MARGIN_TOP
        );
        Anchor.apply(titleLabel, WINDOW_WIDTH, WINDOW_HEIGHT, titleSpec);
        window.addChild(titleLabel);
    }

    private void addOfferPanels(UIElement window) {
        UIElement playerOfferGrid = ContainerUI.scrollableGrid(
            PLAYER_OFFER_START_INDEX,
            OFFER_GRID_SIZE,
            OFFER_GRID_COLUMNS,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        playerOfferGrid.setFrame(
            LEFT_PANEL_X,
            PANEL_Y_START,
            GRID_3X3_SIZE,
            GRID_3X3_SIZE
        );
        window.addChild(playerOfferGrid);

        leftValLabel = new Label("Total Value: 0", theme);
        leftValLabel.setFrame(
            LEFT_PANEL_X,
            PANEL_Y_START + GRID_3X3_SIZE + LABEL_Y_OFFSET,
            LABEL_WIDTH,
            LABEL_HEIGHT
        );
        window.addChild(leftValLabel);

        UIElement npcOfferGrid = ContainerUI.scrollableGrid(
            NPC_OFFER_START_INDEX,
            OFFER_GRID_SIZE,
            OFFER_GRID_COLUMNS,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        npcOfferGrid.setFrame(
            RIGHT_PANEL_X,
            PANEL_Y_START,
            GRID_3X3_SIZE,
            GRID_3X3_SIZE
        );
        window.addChild(npcOfferGrid);

        rightValLabel = new Label("Selected Value: 0", theme);
        rightValLabel.setFrame(
            RIGHT_PANEL_X,
            PANEL_Y_START + GRID_3X3_SIZE + LABEL_Y_OFFSET,
            LABEL_WIDTH,
            LABEL_HEIGHT
        );
        window.addChild(rightValLabel);
    }

    private void addCenterControls(UIElement window) {
        int centerX = WINDOW_WIDTH / 2;
        int centerPanelY = PANEL_Y_START + CENTER_PANEL_OFFSET_Y;

        Label willingnessLabel = new Label("交易意愿: 中", theme);
        willingnessLabel.setFrame(
            centerX - WILL_LABEL_WIDTH / 2 + WILLINGNESS_LABEL_X_OFFSET,
            centerPanelY,
            WILL_LABEL_WIDTH,
            TITLE_HEIGHT
        );
        window.addChild(willingnessLabel);

        Button tradeBtn = new Button("Trade", theme);
        tradeBtn.setFrame(
            centerX - BUTTON_WIDTH / 2,
            centerPanelY + CENTER_PANEL_OFFSET_Y,
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        tradeBtn.setOnClick(() -> {
            if (this.menu != null) {
                PacketDistributor.sendToServer(
                    new ServerboundTradePayload(this.menu.containerId)
                );
            }
        });
        window.addChild(tradeBtn);
    }

    private void addPlayerInventory(UIElement window) {
        UIElement playerInvGrid = InventoryUI.playerInventoryGrid(
            PLAYER_INV_START_INDEX,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );

        Anchor.Spec invSpec = new Anchor.Spec(
            playerInvGrid.getWidth(),
            playerInvGrid.getHeight(),
            Anchor.Horizontal.CENTER,
            Anchor.Vertical.TOP,
            0,
            PLAYER_INV_Y
        );
        Anchor.apply(playerInvGrid, WINDOW_WIDTH, WINDOW_HEIGHT, invSpec);
        window.addChild(playerInvGrid);
    }
}
