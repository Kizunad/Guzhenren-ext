package com.Kizunad.customNPCs.client.ui;

import com.Kizunad.customNPCs.menu.NpcInventoryMenu;
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
 * Debug 用 NPC 背包查看界面，基于 tinyUI。
 */
public class NpcInventoryScreen
    extends TinyUIContainerScreen<NpcInventoryMenu> {

    private static final int WINDOW_WIDTH = 230;
    private static final int WINDOW_HEIGHT = 240;
    private static final int SLOT_SIZE = 18;
    private static final int GRID_GAP = 2;
    private static final int GRID_PADDING = 1;
    private static final int TITLE_Y = 6;
    private static final int TITLE_HEIGHT = 12;
    private static final int TITLE_WIDTH = 160;
    private static final int MAIN_COLS = 9;
    private static final int MAIN_GRID_X = 7;
    private static final int MAIN_GRID_Y = 22;
    private static final int EQUIP_GRID_X = 190;
    private static final int EQUIP_GRID_Y = 22;
    private static final int PLAYER_GRID_X = 7;
    private static final int PLAYER_GRID_Y = 140;

    private final Theme theme;

    public NpcInventoryScreen(
        NpcInventoryMenu menu,
        Inventory playerInventory,
        Component title
    ) {
        super(menu, playerInventory, title);
        this.theme = Theme.vanilla();
    }

    @Override
    protected boolean shouldEnforceMenuBinding() {
        // 强制走原版容器交互（服务器权威），避免客户端本地操作被回滚导致无法搬运物品
        return true;
    }

    @Override
    protected void initUI(UIRoot root) {
        root.setViewport(WINDOW_WIDTH, WINDOW_HEIGHT);

        UIElement main = new UIElement() {};
        main.setFrame(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        root.addChild(main);

        String npcName = menu.getNpc() != null
            ? menu.getNpc().getName().getString()
            : "NPC Inventory";
        Label title = new Label("Inventory - " + npcName, theme);
        title.setFrame(MAIN_GRID_X, TITLE_Y, TITLE_WIDTH, TITLE_HEIGHT);
        main.addChild(title);

        // NPC 主背包 4x9
        UIElement npcMainGrid = ContainerUI.scrollableGrid(
            NpcInventoryMenu.NPC_MAIN_START,
            NpcInventoryMenu.NPC_MAIN_COUNT,
            MAIN_COLS,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        npcMainGrid.setFrame(
            MAIN_GRID_X,
            MAIN_GRID_Y,
            npcMainGrid.getWidth(),
            npcMainGrid.getHeight()
        );
        main.addChild(npcMainGrid);

        // 装备列：主手 + 头/胸/腿/脚 + 副手
        UIElement equipGrid = ContainerUI.scrollableGrid(
            NpcInventoryMenu.NPC_EQUIP_START,
            NpcInventoryMenu.NPC_EQUIP_COUNT,
            1,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        equipGrid.setFrame(
            EQUIP_GRID_X,
            EQUIP_GRID_Y,
            equipGrid.getWidth(),
            equipGrid.getHeight()
        );
        main.addChild(equipGrid);

        // 玩家背包
        UIElement playerGrid = InventoryUI.playerInventoryGrid(
            NpcInventoryMenu.TOTAL_NPC_SLOTS,
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
