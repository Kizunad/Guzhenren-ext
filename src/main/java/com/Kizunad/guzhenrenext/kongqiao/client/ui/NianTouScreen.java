package com.Kizunad.guzhenrenext.kongqiao.client.ui;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoI18n;
import com.Kizunad.guzhenrenext.kongqiao.menu.NianTouMenu;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.InventoryUI;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.ScrollContainer;
import com.Kizunad.tinyUI.controls.SolidPanel;
import com.Kizunad.tinyUI.controls.UISlot;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.Anchor;
import com.Kizunad.tinyUI.layout.GridLayout;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class NianTouScreen extends TinyUIContainerScreen<NianTouMenu> {

    // Adjust size to fit 3 columns.
    // Left: 260, Center: ~200, Right: 260 -> Total ~720 + margins.
    // Minecraft screens can be large, but let's stick to a reasonable size or use the diagram proportions.
    // Diagram: Left 260, Center ~200, Right 270. Total width ~750.
    // Height: ~400.
    private static final int WINDOW_WIDTH = 750;
    private static final int WINDOW_HEIGHT = 450;

    private static final int PANEL_PADDING = 10;
    private static final int LEFT_WIDTH = 200;
    private static final int RIGHT_WIDTH = 200;
    private static final int CENTER_WIDTH = 200; // Reduced to fit typical screen if needed, but 750 is fine for Large GUI.

    // Actually 750 is quite wide (MC default is often 427 for large scale).
    // If GUI scale is Auto, 750 might be too big for smaller screens.
    // But let's follow the diagram proportions relatively.

    private final Theme theme = Theme.vanilla();
    private Label resultLabel;
    private Label timeLabel;
    private Label costLabel;

    // Components for update
    private Button identifyButton;

    public NianTouScreen(
        NianTouMenu menu,
        Inventory playerInventory,
        Component title
    ) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void initUI(UIRoot root) {
        UIElement window = new UIElement() {};
        window.setFrame(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        Anchor.apply(
            window,
            root.getWidth(),
            root.getHeight(),
            new Anchor.Spec(
                WINDOW_WIDTH,
                WINDOW_HEIGHT,
                Anchor.Horizontal.CENTER,
                Anchor.Vertical.CENTER,
                0,
                0
            )
        );
        root.addChild(window);

        // Main Background Panel
        SolidPanel mainPanel = new SolidPanel(theme);
        mainPanel.setFrame(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        window.addChild(mainPanel);

        // Title
        Label titleLabel = new Label(
            KongqiaoI18n.text(KongqiaoI18n.NIANTOU_TITLE),
            theme
        );
        titleLabel.setFrame(
            PANEL_PADDING,
            PANEL_PADDING,
            WINDOW_WIDTH - PANEL_PADDING * 2,
            16
        );
        mainPanel.addChild(titleLabel);

        int contentY = 40;
        int contentHeight = 250;

        // Left Panel: History
        buildHistoryPanel(
            mainPanel,
            PANEL_PADDING,
            contentY,
            LEFT_WIDTH,
            contentHeight
        );

        // Center Panel: Slot + Controls
        int centerX = PANEL_PADDING + LEFT_WIDTH + PANEL_PADDING;
        buildCenterPanel(
            mainPanel,
            centerX,
            contentY,
            CENTER_WIDTH,
            contentHeight
        );

        // Right Panel: Output
        int rightX = centerX + CENTER_WIDTH + PANEL_PADDING;
        buildOutputPanel(
            mainPanel,
            rightX,
            contentY,
            RIGHT_WIDTH,
            contentHeight
        );

        // Player Inventory (Bottom)
        // Positioned below content
        int playerInvY = contentY + contentHeight + 20;
        UIElement playerGrid = InventoryUI.playerInventoryGrid(
            1, // Start index for player slots
            18, // Slot size
            2, // Gap
            0, // Padding
            theme
        );
        // Center player inventory
        int playerX = (WINDOW_WIDTH - playerGrid.getWidth()) / 2;
        playerGrid.setFrame(
            playerX,
            playerInvY,
            playerGrid.getWidth(),
            playerGrid.getHeight()
        );
        mainPanel.addChild(playerGrid);
    }

    private void buildHistoryPanel(
        UIElement parent,
        int x,
        int y,
        int w,
        int h
    ) {
        SolidPanel panel = new SolidPanel(theme);
        panel.setFrame(x, y, w, h);
        parent.addChild(panel);

        Label label = new Label(
            KongqiaoI18n.text(KongqiaoI18n.NIANTOU_HISTORY_LABEL),
            theme
        );
        label.setFrame(5, 5, w - 10, 12);
        panel.addChild(label);

        ScrollContainer scroll = new ScrollContainer(theme);
        scroll.setFrame(5, 20, w - 10, h - 25);

        // Placeholder content for history
        UIElement content = new UIElement() {};
        content.setFrame(0, 0, w - 20, 10); // Expands with children
        // Add some dummy labels or leave empty for now

        scroll.setContent(content);
        panel.addChild(scroll);
    }

    private void buildCenterPanel(
        UIElement parent,
        int x,
        int y,
        int w,
        int h
    ) {
        // Transparent container for center
        UIElement container = new UIElement() {};
        container.setFrame(x, y, w, h);
        parent.addChild(container);

        // Slot in the middle of this area
        UISlot slot = new UISlot(0, theme);
        int slotSize = 18;
        // Scale slot up visually? TinyUI slot is fixed size typically unless scaled.
        // Diagram shows a 30x30 box. Let's just center the 18x18 slot or use a background.
        int slotX = (w - slotSize) / 2;
        int slotY = 40;
        slot.setFrame(slotX, slotY, slotSize, slotSize);
        container.addChild(slot);

        // Identify Button
        identifyButton = new Button(
            KongqiaoI18n.text(KongqiaoI18n.NIANTOU_BUTTON_IDENTIFY),
            theme
        );
        identifyButton.setFrame((w - 80) / 2, slotY + slotSize + 20, 80, 20);
        identifyButton.setOnClick(this::onIdentifyClick);
        container.addChild(identifyButton);

        // Time Label
        timeLabel = new Label(Component.literal("Time: --"), theme);
        timeLabel.setFrame(10, identifyButton.getY() + 30, w - 20, 12);
        container.addChild(timeLabel);

        // Cost Label
        costLabel = new Label(Component.literal("Cost: --"), theme);
        costLabel.setFrame(10, timeLabel.getY() + 15, w - 20, 12);
        container.addChild(costLabel);
    }

    private void buildOutputPanel(
        UIElement parent,
        int x,
        int y,
        int w,
        int h
    ) {
        SolidPanel panel = new SolidPanel(theme);
        panel.setFrame(x, y, w, h);
        parent.addChild(panel);

        Label label = new Label(
            KongqiaoI18n.text(KongqiaoI18n.NIANTOU_OUTPUT_LABEL),
            theme
        );
        label.setFrame(5, 5, w - 10, 12);
        panel.addChild(label);

        resultLabel = new Label(
            KongqiaoI18n.text(KongqiaoI18n.NIANTOU_RESULT_LABEL),
            theme
        );
        resultLabel.setFrame(5, 25, w - 10, h - 30);
        panel.addChild(resultLabel);
    }

    private void onIdentifyClick() {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            // Send click to container. ID 99 for identify action.
            this.minecraft.gameMode.handleInventoryButtonClick(
                menu.containerId,
                99
            );
        }
    }

    @Override
    public void render(
        net.minecraft.client.gui.GuiGraphics guiGraphics,
        int mouseX,
        int mouseY,
        float partialTick
    ) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        updateData();
    }

    private void updateData() {
        if (menu == null) return;

        int progress = menu.getProgress();
        int total = menu.getTotalTime();
        int cost = menu.getCost();

        String timeStr = total > 0 ? (progress + " / " + total) : "--";
        timeLabel.setText(
            Component.translatable(KongqiaoI18n.NIANTOU_TIME_LABEL).append(
                ": " + timeStr
            )
        );

        costLabel.setText(
            Component.translatable(KongqiaoI18n.NIANTOU_COST_LABEL).append(
                ": " + cost
            )
        );

        // Update result text based on item if needed, or via separate packet/capability
    }
}
