package com.Kizunad.tinyUI.demo;

import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.ScrollContainer;
import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.FlexLayout;
import com.Kizunad.tinyUI.layout.FlexParams;
import com.Kizunad.tinyUI.layout.GridLayout;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.HashMap;
import java.util.Map;

/**
 * Complex Layout Demo implementation.
 * Replicates the "Complex Layout" demo from the HTML playground.
 */
public class ComplexLayoutDemo {

    private static final int WINDOW_WIDTH = 640;
    private static final int WINDOW_HEIGHT = 480;
    private static final int LEFT_PANEL_WIDTH = 240;
    private static final int SLOT_SIZE = 24;
    private static final int SLOT_COUNT = 63;
    private static final int GRID_COLS = 9;
    private static final int GRID_GAP = 2;
    
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
    
    private static final int INV_GRID_WIDTH = 600;
    private static final int INV_GRID_HEIGHT = 100;
    private static final int INV_GRID_Y = 25;
    
    private static final int TOTAL_SLOTS = 36;
    private static final int HOTBAR_START = 27;
    private static final int HOTBAR_OFFSET = 26;
    
    private static final int PLAYER_ROWS = 4;
    private static final int PLAYER_GAP = 4;
    private static final int BOTTOM_PANEL_HEIGHT = 150;
    private static final int FLEX_GAP = 10;

    public static UIRoot build(Theme theme) {
        UIRoot root = new UIRoot();
        root.setViewport(WINDOW_WIDTH, WINDOW_HEIGHT);

        UIElement main = new UIElement() {};
        main.setFrame(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        root.addChild(main);

        // --- Top Section ---
        UIElement topSection = new UIElement() {};
        
        // 1. Left: 9*n Grid (Scrollable)
        ScrollContainer leftScroll = new ScrollContainer(theme);
        UIElement leftContent = new UIElement() {};
        // Width: 9 cols * 20px (18+2gap) = 180px approx
        // In Java, let's use slightly larger buttons, e.g. 24px + 2px gap
        leftContent.setFrame(0, 0, LEFT_PANEL_WIDTH, 0); 
        leftScroll.setContent(leftContent);
        
        // Add many items to show scrolling
        for (int i = 0; i < SLOT_COUNT; i++) { 
            Button slot = new Button("" + (i + 1), theme);
            slot.setFrame(0, 0, SLOT_SIZE, SLOT_SIZE); 
            // slot.setOnClick(() -> 
            //    System.out.println("Clicked Left Slot " + (i + 1)));
            leftContent.addChild(slot);
        }
        new GridLayout(GRID_COLS, 0, GRID_GAP, GRID_GAP).layout(leftContent, null);

        // 2. Middle: Preview Window
        InteractiveElement middlePanel = new InteractiveElement() {
             @Override
             protected void onRender(UIRenderContext context, double mouseX, double mouseY, float partialTicks) {
                 // Draw background
                 context.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), theme.getBackgroundColor());
                 super.onRender(context, mouseX, mouseY, partialTicks);
             }
        };
        Label previewLabel = new Label("Preview", theme);
        previewLabel.setFrame(PREVIEW_LABEL_X, PREVIEW_LABEL_Y, PREVIEW_LABEL_W, PREVIEW_LABEL_H);
        middlePanel.addChild(previewLabel);
        
        // Placeholder for a character or item preview
        UIElement previewBox = new UIElement() {
            @Override
            protected void onRender(UIRenderContext context, double mouseX, double mouseY, float partialTicks) {
                context.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), PREVIEW_BG_COLOR);
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

        // 3. Right: Information
        UIElement rightPanel = new UIElement() {};
        Label infoTitle = new Label("Information", theme);
        infoTitle.setFrame(0, 0, PREVIEW_LABEL_W, INFO_TITLE_H);
        Label infoDesc = new Label(
            "Name: Gu Zhenren\nLevel: 99\nRank: 5\n\nStats:\n- STR: 10\n- DEX: 15\n- INT: 20",
            theme
        );
        infoDesc.setFrame(0, INFO_DESC_Y, INFO_WIDTH, INFO_DESC_H);
        rightPanel.addChild(infoTitle);
        rightPanel.addChild(infoDesc);

        topSection.addChild(leftScroll);
        topSection.addChild(middlePanel);
        topSection.addChild(rightPanel);

        Map<UIElement, FlexParams> topParams = new HashMap<>();
        // Left: fixed width for 9 cols (approx 24*9 + gaps) -> ~250px with scrollbar
        topParams.put(leftScroll, new FlexParams(0, 0, SCROLL_WIDTH));
        // Middle: grow
        topParams.put(middlePanel, new FlexParams(1, 1, 0));
        // Right: fixed width
        topParams.put(rightPanel, new FlexParams(0, 0, RIGHT_PANEL_WIDTH));
        
        new FlexLayout(FlexLayout.Direction.ROW, FLEX_GAP, 0).layout(topSection, topParams);

        // --- Bottom Section: Player Inventory ---
        UIElement bottomPanel = new UIElement() {};
        Label invLabel = new Label("Inventory", theme);
        invLabel.setFrame(0, 0, PREVIEW_LABEL_W, PREVIEW_LABEL_H);
        bottomPanel.addChild(invLabel);

        UIElement invGrid = new UIElement() {};
        invGrid.setFrame(0, INV_GRID_Y, INV_GRID_WIDTH, INV_GRID_HEIGHT);
        bottomPanel.addChild(invGrid);

        // 3 rows of 9 + 1 row of 9 (hotbar) = 36 slots
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            Button slot = new Button("", theme);
            slot.setFrame(0, 0, SLOT_SIZE, SLOT_SIZE);
            if (i >= HOTBAR_START) {
               // Hotbar separation visual hint?
               slot.setText("" + (i - HOTBAR_OFFSET));
            }
            invGrid.addChild(slot);
        }
        new GridLayout(GRID_COLS, PLAYER_ROWS, PLAYER_GAP, PLAYER_GAP).layout(invGrid, null);

        // --- Main Layout ---
        main.addChild(topSection);
        main.addChild(bottomPanel);

        Map<UIElement, FlexParams> mainParams = new HashMap<>();
        mainParams.put(topSection, new FlexParams(1, 1, 0));
        mainParams.put(bottomPanel, new FlexParams(0, 0, BOTTOM_PANEL_HEIGHT)); // Fixed height for inventory
        
        new FlexLayout(FlexLayout.Direction.COLUMN, FLEX_GAP, FLEX_GAP).layout(main, mainParams);

        return root;
    }
}
