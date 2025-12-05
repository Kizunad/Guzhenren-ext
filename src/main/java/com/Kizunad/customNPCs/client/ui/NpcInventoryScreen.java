package com.Kizunad.customNPCs.client.ui;

import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.menu.NpcInventoryMenu;
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

/**
 * Debug 用 NPC 背包查看界面，基于 tinyUI。
 */
public class NpcInventoryScreen
    extends TinyUIContainerScreen<NpcInventoryMenu> {

    private static final int SLOT_SIZE = 18;
    private static final int GRID_GAP = 2;
    private static final int GRID_PADDING = 1;
    private static final int TITLE_HEIGHT = 12;
    private static final int TITLE_WIDTH = 160;
    private static final int TITLE_MARGIN_TOP = 6;
    private static final int CONTENT_PADDING = 12;
    private static final int COLUMN_GAP = 12;
    private static final int SECTION_GAP = 10;
    private static final int PLAYER_SECTION_GAP = 14;
    private static final int MAIN_COLS = NpcInventory.SLOTS_PER_ROW;
    private static final int VISIBLE_ROWS = 6;
    private static final int SCROLLBAR_ALLOWANCE = 14;
    private static final int EQUIP_COLS = 1;
    private static final int NPC_MAIN_START = 0;

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
        NpcInventory inventory = menu.getInventory();
        int mainCount = inventory != null
            ? inventory.getMainSize()
            : NpcInventory.DEFAULT_MAIN_SIZE;

        String npcName = menu.getNpc() != null
            ? menu.getNpc().getName().getString()
            : "NPC Inventory";
        Label title = new Label("Inventory - " + npcName, theme);

        int mainContentWidth =
            MAIN_COLS * SLOT_SIZE +
            (MAIN_COLS - 1) * GRID_GAP +
            GRID_PADDING * 2;
        int mainViewportWidth = mainContentWidth + SCROLLBAR_ALLOWANCE;
        int mainViewportHeight =
            VISIBLE_ROWS * SLOT_SIZE +
            (VISIBLE_ROWS - 1) * GRID_GAP +
            GRID_PADDING * 2;
        int equipContentWidth =
            EQUIP_COLS * SLOT_SIZE +
            GRID_PADDING * 2; // 单列，不需要列间距
        int equipContentHeight =
            NpcInventoryMenu.NPC_EQUIP_COUNT * SLOT_SIZE +
            (NpcInventoryMenu.NPC_EQUIP_COUNT - 1) * GRID_GAP +
            GRID_PADDING * 2;

        // NPC 主背包（固定视口，超出滚动）
        UIElement npcMainGrid = ContainerUI.scrollableGrid(
            NPC_MAIN_START,
            mainCount,
            MAIN_COLS,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        npcMainGrid.setFrame(
            0,
            0,
            mainViewportWidth,
            mainViewportHeight
        );

        // 装备列：主手 + 头/胸/腿/脚 + 副手
        UIElement equipGrid = ContainerUI.scrollableGrid(
            menu.getNpcEquipStart(),
            NpcInventoryMenu.NPC_EQUIP_COUNT,
            1,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        equipGrid.setFrame(0, 0, equipContentWidth, equipContentHeight);

        // 玩家背包
        UIElement playerGrid = InventoryUI.playerInventoryGrid(
            menu.getTotalNpcSlots(),
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );

        int npcAreaWidth = mainViewportWidth + COLUMN_GAP + equipContentWidth;
        int contentWidth = Math.max(npcAreaWidth, playerGrid.getWidth());
        int npcAreaHeight = Math.max(mainViewportHeight, equipContentHeight);

        int windowWidth = contentWidth + CONTENT_PADDING * 2;
        int titleX = CONTENT_PADDING + (contentWidth - TITLE_WIDTH) / 2;
        int titleY = CONTENT_PADDING + TITLE_MARGIN_TOP;
        int npcAreaTop = titleY + TITLE_HEIGHT + SECTION_GAP;
        int playerTop = npcAreaTop + npcAreaHeight + PLAYER_SECTION_GAP;
        int windowHeight = playerTop + playerGrid.getHeight() + CONTENT_PADDING;

        UIElement window = new UIElement() {};
        window.setFrame(0, 0, windowWidth, windowHeight);
        Anchor.Spec center = new Anchor.Spec(
            windowWidth,
            windowHeight,
            Anchor.Horizontal.CENTER,
            Anchor.Vertical.CENTER,
            0,
            0
        );
        Anchor.apply(window, root.getWidth(), root.getHeight(), center);
        root.addChild(window);

        title.setFrame(titleX, titleY, TITLE_WIDTH, TITLE_HEIGHT);
        window.addChild(title);

        int leftBase = CONTENT_PADDING + (contentWidth - npcAreaWidth) / 2;
        npcMainGrid.setFrame(
            leftBase,
            npcAreaTop,
            mainViewportWidth,
            mainViewportHeight
        );
        window.addChild(npcMainGrid);

        equipGrid.setFrame(
            leftBase + mainViewportWidth + COLUMN_GAP,
            npcAreaTop,
            equipContentWidth,
            equipContentHeight
        );
        window.addChild(equipGrid);

        playerGrid.setFrame(
            CONTENT_PADDING + (contentWidth - playerGrid.getWidth()) / 2,
            playerTop,
            playerGrid.getWidth(),
            playerGrid.getHeight()
        );
        window.addChild(playerGrid);
    }
}
