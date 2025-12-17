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

public class AttackInventoryScreen
    extends TinyUIContainerScreen<AttackInventoryMenu> {

    private static final int SLOT_SIZE = 18;
    private static final int GRID_GAP = 2;
    private static final int GRID_PADDING = 2;
    private static final int TITLE_HEIGHT = 16;
    private static final int SECTION_GAP = 12;
    private static final int PLAYER_TITLE_HEIGHT = 14;
    private static final int BUTTON_HEIGHT = 18;
    private static final int PANEL_PADDING = 16;
    private static final int WINDOW_WIDTH = 600;
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_GAP = 8;

    private final Theme theme = Theme.vanilla();

    public AttackInventoryScreen(
        AttackInventoryMenu menu,
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
        int attackColumns = menu.getInventory().getColumns();
        int attackRows = menu.getInventory().getRows();
        int gridWidth =
            attackColumns * SLOT_SIZE +
            (attackColumns - 1) * GRID_GAP +
            GRID_PADDING * 2;
        int gridHeight =
            attackRows * SLOT_SIZE + (attackRows - 1) * GRID_GAP + GRID_PADDING * 2;

        UIElement attackGrid = ContainerUI.scrollableGrid(
            0,
            menu.getContainerSlots(),
            attackColumns,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        attackGrid.setFrame(0, 0, gridWidth, gridHeight);

        UIElement playerGrid = InventoryUI.playerInventoryGrid(
            menu.getContainerSlots(),
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );

        int contentWidth = Math.max(gridWidth, playerGrid.getWidth());
        int windowHeight =
            PANEL_PADDING +
            TITLE_HEIGHT +
            SECTION_GAP +
            gridHeight +
            SECTION_GAP +
            PLAYER_TITLE_HEIGHT +
            SECTION_GAP +
            playerGrid.getHeight() +
            SECTION_GAP +
            BUTTON_HEIGHT +
            PANEL_PADDING;

        UIElement window = new UIElement() {};
        window.setFrame(0, 0, WINDOW_WIDTH, windowHeight);
        Anchor.apply(
            window,
            root.getWidth(),
            root.getHeight(),
            new Anchor.Spec(
                WINDOW_WIDTH,
                windowHeight,
                Anchor.Horizontal.CENTER,
                Anchor.Vertical.CENTER,
                0,
                0
            )
        );
        root.addChild(window);

        SolidPanel panel = new SolidPanel(theme);
        panel.setFrame(0, 0, WINDOW_WIDTH, windowHeight);
        window.addChild(panel);

        Label titleLabel = new Label(
            KongqiaoI18n.text(KongqiaoI18n.ATTACK_TITLE),
            theme
        );
        titleLabel.setFrame(
            PANEL_PADDING,
            PANEL_PADDING,
            WINDOW_WIDTH - PANEL_PADDING * 2,
            TITLE_HEIGHT
        );
        panel.addChild(titleLabel);

        int gridX = PANEL_PADDING + (contentWidth - gridWidth) / 2;
        attackGrid.setFrame(
            gridX,
            PANEL_PADDING + TITLE_HEIGHT + SECTION_GAP,
            gridWidth,
            gridHeight
        );
        panel.addChild(attackGrid);

        Label playerLabel = new Label(
            KongqiaoI18n.text(KongqiaoI18n.COMMON_PLAYER_INVENTORY),
            theme
        );
        int playerSectionY = attackGrid.getY() + attackGrid.getHeight() + SECTION_GAP;
        playerLabel.setFrame(
            PANEL_PADDING,
            playerSectionY,
            WINDOW_WIDTH - PANEL_PADDING * 2,
            PLAYER_TITLE_HEIGHT
        );
        panel.addChild(playerLabel);

        int playerX = PANEL_PADDING + (contentWidth - playerGrid.getWidth()) / 2;
        playerGrid.setFrame(
            playerX,
            playerLabel.getY() + PLAYER_TITLE_HEIGHT + SECTION_GAP / 2,
            playerGrid.getWidth(),
            playerGrid.getHeight()
        );
        panel.addChild(playerGrid);

        int buttonsY = windowHeight - PANEL_PADDING - BUTTON_HEIGHT;

        Button swapButton = new Button(
            KongqiaoI18n.text(KongqiaoI18n.ATTACK_BUTTON_SWAP),
            theme
        );
        swapButton.setFrame(PANEL_PADDING, buttonsY, BUTTON_WIDTH, BUTTON_HEIGHT);
        swapButton.setOnClick(
            () -> sendAction(ServerboundKongqiaoActionPayload.Action.SWAP_ATTACK)
        );
        panel.addChild(swapButton);

        Button returnButton = new Button(
            KongqiaoI18n.text(KongqiaoI18n.ATTACK_BUTTON_RETURN),
            theme
        );
        returnButton.setFrame(
            PANEL_PADDING + BUTTON_WIDTH + BUTTON_GAP,
            buttonsY,
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        returnButton.setOnClick(
            () -> sendAction(ServerboundKongqiaoActionPayload.Action.OPEN_KONGQIAO)
        );
        panel.addChild(returnButton);
    }

    @Override
    protected double getUiScale() {
        return com.Kizunad.guzhenrenext.config.ClientConfig.INSTANCE.kongQiaoUiScale.get();
    }

    private void sendAction(ServerboundKongqiaoActionPayload.Action action) {
        PacketDistributor.sendToServer(new ServerboundKongqiaoActionPayload(action));
    }
}
