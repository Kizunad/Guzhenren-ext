package com.Kizunad.guzhenrenext.kongqiao.client.ui;

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
    private static final int CONTENT_PADDING = 14;
    private static final int TITLE_HEIGHT = 16;
    private static final int BUTTON_WIDTH = 140;
    private static final int BUTTON_HEIGHT = 18;
    private static final int BUTTON_GAP = 10;
    private static final int SECTION_GAP = 12;
    private static final int PLAYER_SECTION_GAP = 18;
    private static final int INFO_MARGIN_TOP = 4;
    private static final int INFO_LABEL_HEIGHT = 12;

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

        Label title = new Label(Component.literal("Attack Inventory"), theme);
        Label info = new Label(
            Component.literal("此处存放可快速切换的攻击蛊虫/道具"),
            theme
        );

        Button returnBtn = new Button("Return to 空窍", theme);
        returnBtn.setOnClick(
            () -> sendAction(ServerboundKongqiaoActionPayload.Action.OPEN_KONGQIAO)
        );
        Button swapBtn = new Button("Swap With Player", theme);
        swapBtn.setOnClick(
            () -> sendAction(ServerboundKongqiaoActionPayload.Action.SWAP_ATTACK)
        );

        UIElement playerGrid = InventoryUI.playerInventoryGrid(
            containerSlots,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );

        int contentWidth = Math.max(gridWidth, playerGrid.getWidth());
        int windowWidth = contentWidth + CONTENT_PADDING * 2;
        int windowHeight =
            TITLE_HEIGHT +
            SECTION_GAP +
            gridHeight +
            SECTION_GAP +
            BUTTON_HEIGHT +
            PLAYER_SECTION_GAP +
            playerGrid.getHeight() +
            CONTENT_PADDING * 2;

        UIElement window = new UIElement() {};
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

        int gridX = CONTENT_PADDING + (contentWidth - gridWidth) / 2;
        title.setFrame(gridX, CONTENT_PADDING, gridWidth, TITLE_HEIGHT);
        window.addChild(title);

        attackGrid.setFrame(
            gridX,
            CONTENT_PADDING + TITLE_HEIGHT + SECTION_GAP,
            gridWidth,
            gridHeight
        );
        window.addChild(attackGrid);

        info.setFrame(
            gridX,
            attackGrid.getY() + attackGrid.getHeight() + INFO_MARGIN_TOP,
            gridWidth,
            INFO_LABEL_HEIGHT
        );
        window.addChild(info);

        int buttonsWidth = BUTTON_WIDTH * 2 + BUTTON_GAP;
        int buttonsX = CONTENT_PADDING + (contentWidth - buttonsWidth) / 2;
        int buttonsY = attackGrid.getY() + attackGrid.getHeight() + SECTION_GAP;
        returnBtn.setFrame(
            buttonsX,
            buttonsY,
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        swapBtn.setFrame(
            buttonsX + BUTTON_WIDTH + BUTTON_GAP,
            buttonsY,
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        window.addChild(returnBtn);
        window.addChild(swapBtn);

        playerGrid.setFrame(
            CONTENT_PADDING + (contentWidth - playerGrid.getWidth()) / 2,
            buttonsY + BUTTON_HEIGHT + PLAYER_SECTION_GAP,
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
