package com.Kizunad.guzhenrenext.kongqiao.client.ui;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoI18n;
import com.Kizunad.guzhenrenext.kongqiao.menu.GuchongFeedMenu;
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

/**
 * 蛊虫喂食独立 TinyUI 界面。
 */
public class GuchongFeedScreen extends TinyUIContainerScreen<GuchongFeedMenu> {

    private static final int SLOT_SIZE = 18;
    private static final int GRID_GAP = 2;
    private static final int GRID_PADDING = 2;
    private static final int TITLE_HEIGHT = 16;
    private static final int SECTION_GAP = 12;
    private static final int PANEL_PADDING = 16;
    private static final int WINDOW_WIDTH = 600;
    private static final int PLAYER_TITLE_HEIGHT = 14;
    private static final int SUBTITLE_HEIGHT = 14;
    private static final int BUTTON_HEIGHT = 18;
    private static final int AUTO_FEED_BUTTON_WIDTH = 140;

    private final Theme theme = Theme.vanilla();

    public GuchongFeedScreen(
        GuchongFeedMenu menu,
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
        int feedColumns = menu.getInventory().getColumns();
        int feedRows = menu.getInventory().getRows();
        int gridWidth =
            feedColumns * SLOT_SIZE +
            (feedColumns - 1) * GRID_GAP +
            GRID_PADDING * 2;
        int gridHeight =
            feedRows * SLOT_SIZE + (feedRows - 1) * GRID_GAP + GRID_PADDING * 2;

        UIElement feedGrid = ContainerUI.scrollableGrid(
            0,
            menu.getContainerSlots(),
            feedColumns,
            SLOT_SIZE,
            GRID_GAP,
            GRID_PADDING,
            theme
        );
        feedGrid.setFrame(0, 0, gridWidth, gridHeight);

        Label title = new Label(
            KongqiaoI18n.text(KongqiaoI18n.FEED_TITLE),
            theme
        );
        // 预留副标题，方便后续扩展动态提示
        Label subTitle = new Label(Component.empty(), theme);

        Button autoFeed = new Button(
            KongqiaoI18n.text(KongqiaoI18n.FEED_BUTTON_AUTO),
            theme
        );
        autoFeed.setEnabled(false);

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
            SUBTITLE_HEIGHT +
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

        title.setFrame(
            PANEL_PADDING,
            PANEL_PADDING,
            WINDOW_WIDTH - PANEL_PADDING * 2,
            TITLE_HEIGHT
        );
        panel.addChild(title);

        int gridX = PANEL_PADDING + (contentWidth - gridWidth) / 2;
        feedGrid.setFrame(
            gridX,
            PANEL_PADDING + TITLE_HEIGHT + SECTION_GAP,
            gridWidth,
            gridHeight
        );
        panel.addChild(feedGrid);

        subTitle.setFrame(
            gridX,
            feedGrid.getY() + feedGrid.getHeight() + SECTION_GAP / 2,
            gridWidth,
            SUBTITLE_HEIGHT
        );
        panel.addChild(subTitle);

        Label playerLabel = new Label(
            KongqiaoI18n.text(KongqiaoI18n.COMMON_PLAYER_INVENTORY),
            theme
        );
        int playerSectionY = subTitle.getY() + SUBTITLE_HEIGHT + SECTION_GAP;
        int playerX =
            PANEL_PADDING + (contentWidth - playerGrid.getWidth()) / 2;
        playerLabel.setFrame(
            PANEL_PADDING,
            playerSectionY,
            WINDOW_WIDTH - PANEL_PADDING * 2,
            PLAYER_TITLE_HEIGHT
        );
        panel.addChild(playerLabel);

        playerGrid.setFrame(
            playerX,
            playerLabel.getY() + PLAYER_TITLE_HEIGHT + SECTION_GAP / 2,
            playerGrid.getWidth(),
            playerGrid.getHeight()
        );
        panel.addChild(playerGrid);

        autoFeed.setFrame(
            PANEL_PADDING,
            windowHeight - PANEL_PADDING - BUTTON_HEIGHT,
            AUTO_FEED_BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        panel.addChild(autoFeed);
    }
}
