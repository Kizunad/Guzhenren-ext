package com.Kizunad.tinyUI.demo;

import com.Kizunad.tinyUI.controls.ContainerUI;
import com.Kizunad.tinyUI.controls.InventoryUI;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.FlexLayout;
import com.Kizunad.tinyUI.layout.FlexParams;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Complex Layout Screen with real Minecraft inventories.
 * Left side: Custom 9xN scrollable inventory
 * Middle: Preview panel
 * Right: Information panel
 * Bottom: Player inventory (27 slots + 9 hotbar)
 */
public class ComplexLayoutScreen
    extends TinyUIContainerScreen<ComplexLayoutMenu> {

    private static final int WINDOW_WIDTH = 640;
    private static final int WINDOW_HEIGHT = 480;
    private static final int SLOT_SIZE = 18;
    private static final int CUSTOM_COLS = 9;
    private static final int CUSTOM_GAP = 2;
    private static final int CUSTOM_PADDING = 2;
    
    private static final int PREVIEW_LABEL_X = 10;
    private static final int PREVIEW_LABEL_Y = 10;
    private static final int PREVIEW_LABEL_W = 100;
    private static final int PREVIEW_LABEL_H = 20;
    
    private static final int PREVIEW_BOX_X = 20;
    private static final int PREVIEW_BOX_Y = 40;
    private static final int PREVIEW_BOX_W = 100;
    private static final int PREVIEW_BOX_H = 150;
    private static final int PREVIEW_BG_COLOR = 0xFF27272A;
    private static final int PREVIEW_TEXT_COLOR = 0xFF52525B;
    private static final int PREVIEW_TEXT_X = 50;
    private static final int PREVIEW_TEXT_Y = 80;
    
    private static final int INFO_WIDTH = 140;
    private static final int INFO_TITLE_H = 20;
    private static final int INFO_DESC_Y = 30;
    private static final int INFO_DESC_H = 200;
    
    private static final int SCROLL_WIDTH = 250;
    private static final int RIGHT_PANEL_WIDTH = 150;
    private static final int BOTTOM_PANEL_HEIGHT = 150;
    
    private static final int PLAYER_COLS = 9;
    private static final int PLAYER_ROWS = 4;
    private static final int PLAYER_GAP = 4;
    private static final int PLAYER_PADDING = 4;
    private static final int PLAYER_GRID_Y = 25;
    
    private static final int FLEX_GAP = 30;

    private final Theme theme;
    private final int customSlotCount;

    public ComplexLayoutScreen(
        ComplexLayoutMenu menu,
        Inventory playerInventory,
        Component title,
        Theme theme
    ) {
        super(menu, playerInventory, title);
        this.theme = theme;
        this.customSlotCount = menu.getCustomSlotCount();
    }

    @Override
    protected boolean shouldEnforceMenuBinding() {
        // Demo 场景为纯客户端菜单，允许跳过 containerMenu 绑定检查以启用交互
        return false;
    }

    @Override
    protected void initUI(UIRoot root) {
        // Main container
        UIElement main = new UIElement() {};
        main.setFrame(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        root.addChild(main);

        UIElement topSection = createTopSection();
        UIElement bottomPanel = createBottomSection();

        // --- Main Layout ---
        main.addChild(topSection);
        main.addChild(bottomPanel);

        Map<UIElement, FlexParams> mainParams = new HashMap<>();
        mainParams.put(topSection, new FlexParams(1, 1, 0));
        mainParams.put(bottomPanel, new FlexParams(0, 0, BOTTOM_PANEL_HEIGHT));

        new FlexLayout(FlexLayout.Direction.COLUMN, FLEX_GAP, FLEX_GAP).layout(
            main,
            mainParams
        );
        // 主布局完成后，强制重算内部网格，避免首次布局时尺寸为 0
        relayoutContainers(topSection, bottomPanel);
    }

    private void relayoutContainers(UIElement topSection, UIElement bottomPanel) {
        // 左侧滚动区域：刷新其内容布局（容器自身尺寸已由 FlexLayout 写入）
        if (!topSection.getChildren().isEmpty()) {
            UIElement leftScroll = topSection.getChildren().get(0);
            if (leftScroll instanceof com.Kizunad.tinyUI.controls.ScrollContainer scroll) {
                UIElement content = scroll.getChildren().isEmpty() ? null : scroll.getChildren().get(0);
                if (content != null) {
                    new com.Kizunad.tinyUI.layout.GridLayout(
                        CUSTOM_COLS, 0, CUSTOM_GAP, CUSTOM_PADDING
                    ).layout(content, null);
                }
            }
        }
        // 底部玩家背包：重新布局其网格
        if (!bottomPanel.getChildren().isEmpty()) {
            // child 0 是 Label，child 1 是背包网格
            if (bottomPanel.getChildren().size() > 1) {
                UIElement invGrid = bottomPanel.getChildren().get(1);
                new com.Kizunad.tinyUI.layout.GridLayout(
                    PLAYER_COLS,
                    PLAYER_ROWS,
                    PLAYER_GAP,
                    PLAYER_PADDING
                ).layout(invGrid, null);
            }
        }
    }
    
    private UIElement createTopSection() {
        UIElement topSection = new UIElement() {};

        // 1. Left: 9*n Grid (Scrollable) with REAL slots
        UIElement leftScroll = ContainerUI.scrollableGrid(
            0,
            customSlotCount,
            CUSTOM_COLS,
            SLOT_SIZE,
            CUSTOM_GAP,
            CUSTOM_PADDING,
            theme
        );

        // 2. Middle: Preview Window
        InteractiveElement middlePanel = createMiddlePanel();

        // 3. Right: Information
        UIElement rightPanel = createRightPanel();

        topSection.addChild(leftScroll);
        topSection.addChild(middlePanel);
        topSection.addChild(rightPanel);

        Map<UIElement, FlexParams> topParams = new HashMap<>();
        topParams.put(leftScroll, new FlexParams(0, 0, SCROLL_WIDTH));
        topParams.put(middlePanel, new FlexParams(1, 1, 0));
        topParams.put(rightPanel, new FlexParams(0, 0, RIGHT_PANEL_WIDTH));

        new FlexLayout(FlexLayout.Direction.ROW, FLEX_GAP, 0).layout(
            topSection,
            topParams
        );
        
        return topSection;
    }
    
    private InteractiveElement createMiddlePanel() {
        InteractiveElement middlePanel = new InteractiveElement() {
            @Override
            protected void onRender(
                UIRenderContext context,
                double mouseX,
                double mouseY,
                float partialTicks
            ) {
                context.drawRect(
                    getAbsoluteX(),
                    getAbsoluteY(),
                    getWidth(),
                    getHeight(),
                    theme.getBackgroundColor()
                );
                super.onRender(context, mouseX, mouseY, partialTicks);
            }
        };
        Label previewLabel = new Label("Preview", theme);
        previewLabel.setFrame(PREVIEW_LABEL_X, PREVIEW_LABEL_Y, PREVIEW_LABEL_W, PREVIEW_LABEL_H);
        middlePanel.addChild(previewLabel);

        // Placeholder for preview
        UIElement previewBox = new UIElement() {
            @Override
            protected void onRender(
                UIRenderContext context,
                double mouseX,
                double mouseY,
                float partialTicks
            ) {
                context.drawRect(
                    getAbsoluteX(),
                    getAbsoluteY(),
                    getWidth(),
                    getHeight(),
                    PREVIEW_BG_COLOR
                );
                context.drawText(
                    "Entity",
                    getAbsoluteX() + PREVIEW_TEXT_X,
                    getAbsoluteY() + PREVIEW_TEXT_Y,
                    PREVIEW_TEXT_COLOR
                );
            }
        };
        previewBox.setFrame(PREVIEW_BOX_X, PREVIEW_BOX_Y, PREVIEW_BOX_W, PREVIEW_BOX_H);
        middlePanel.addChild(previewBox);
        return middlePanel;
    }
    
    private UIElement createRightPanel() {
        UIElement rightPanel = new UIElement() {};
        Label infoTitle = new Label("Information", theme);
        infoTitle.setFrame(0, 0, INFO_WIDTH, INFO_TITLE_H);
        Label infoDesc = new Label(
            "Name: Gu Zhenren\nLevel: 99\nRank: 5\n\nStats:\n- STR: 10\n- DEX: 15\n- INT: 20",
            theme
        );
        infoDesc.setFrame(0, INFO_DESC_Y, INFO_WIDTH, INFO_DESC_H);
        rightPanel.addChild(infoTitle);
        rightPanel.addChild(infoDesc);
        return rightPanel;
    }
    
    private UIElement createBottomSection() {
        UIElement bottomPanel = new UIElement() {};
        Label invLabel = new Label("Inventory", theme);
        invLabel.setFrame(0, 0, PREVIEW_LABEL_W, PREVIEW_LABEL_H);
        bottomPanel.addChild(invLabel);

        UIElement invGrid = InventoryUI.playerInventoryGrid(
            customSlotCount,
            SLOT_SIZE,
            PLAYER_GAP,
            PLAYER_PADDING,
            theme
        );
        invGrid.setFrame(0, PLAYER_GRID_Y, invGrid.getWidth(), invGrid.getHeight());
        bottomPanel.addChild(invGrid);
        
        return bottomPanel;
    }
}
