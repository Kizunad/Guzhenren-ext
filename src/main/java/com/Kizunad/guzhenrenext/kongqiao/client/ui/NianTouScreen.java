package com.Kizunad.guzhenrenext.kongqiao.client.ui;

import com.Kizunad.guzhenrenext.kongqiao.KongqiaoI18n;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.menu.NianTouMenu;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.InventoryUI;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.ScrollContainer;
import com.Kizunad.tinyUI.controls.UISlot;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.Anchor;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

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
    // Reduced以适配常规屏幕宽度，750 在大界面下仍可接受
    private static final int CENTER_WIDTH = 200;
    private static final int TITLE_HEIGHT = 16;
    private static final int CONTENT_Y = 40;
    private static final int CONTENT_HEIGHT = 250;
    private static final int PLAYER_INV_GAP_Y = 20;
    private static final int PLAYER_INV_START_INDEX = 1;
    private static final int PLAYER_SLOT_SIZE = 18;
    private static final int PLAYER_SLOT_GAP = 2;
    private static final int PLAYER_SLOT_PADDING = 0;
    private static final int SMALL_PADDING = 5;
    private static final int DOUBLE_PADDING = 10;
    private static final int LABEL_HEIGHT = 12;
    private static final int SCROLL_TOP = 20;
    private static final int SCROLL_BOTTOM_MARGIN = 25;
    private static final int CONTENT_MIN_HEIGHT = 10;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_Y = 40;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 20;
    private static final int TIME_OFFSET = 30;
    private static final int COST_OFFSET = 15;
    private static final int RESULT_TEXT_TOP = 50;
    private static final int OUTPUT_BOTTOM_MARGIN = 30;
    private static final int HISTORY_ITEM_HEIGHT = 12;
    private static final int HISTORY_ITEM_STEP = 14;
    private static final int HISTORY_BUTTON_PADDING = 4;
    private static final String FALLBACK_NO_DATA = "未找到该物品的念头配置。";

    // Actually 750 is quite wide (MC default is often 427 for large scale).
    // If GUI scale is Auto, 750 might be too big for smaller screens.
    // But let's follow the diagram proportions relatively.

    private final Theme theme = Theme.vanilla();
    private Label resultLabel;
    private Label timeLabel;
    private Label costLabel;
    private NianTouData previewData;
    private ResourceLocation previewItemId;

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
    protected void containerTick() {
        super.containerTick();
        updateData();
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
            TITLE_HEIGHT
        );
        mainPanel.addChild(titleLabel);

        int contentY = CONTENT_Y;
        int contentHeight = CONTENT_HEIGHT;

        // Calculate total content width including padding between panels
        int totalContentWidth = LEFT_WIDTH + CENTER_WIDTH + RIGHT_WIDTH + (PANEL_PADDING * 2);
        int startX = (WINDOW_WIDTH - totalContentWidth) / 2;

        // Left Panel: History
        buildHistoryPanel(
            mainPanel,
            startX,
            contentY,
            LEFT_WIDTH,
            contentHeight
        );

        // Center Panel: Slot + Controls
        int centerX = startX + LEFT_WIDTH + PANEL_PADDING;
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
        int playerInvY = contentY + contentHeight + PLAYER_INV_GAP_Y;
        UIElement playerGrid = InventoryUI.playerInventoryGrid(
            PLAYER_INV_START_INDEX,
            PLAYER_SLOT_SIZE,
            PLAYER_SLOT_GAP,
            PLAYER_SLOT_PADDING,
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

        updateData();
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
        label.setFrame(
            SMALL_PADDING,
            SMALL_PADDING,
            w - DOUBLE_PADDING,
            LABEL_HEIGHT
        );
        panel.addChild(label);

        ScrollContainer scroll = new ScrollContainer(theme);
        scroll.setFrame(
            SMALL_PADDING,
            SCROLL_TOP,
            w - DOUBLE_PADDING,
            h - SCROLL_BOTTOM_MARGIN
        );

        // Content container
        UIElement content = new UIElement() {};
        
        // Populate history
        if (this.minecraft != null && this.minecraft.player != null) {
            NianTouUnlocks unlocks =
                KongqiaoAttachments.getUnlocks(this.minecraft.player);

            if (unlocks != null) {
                int itemY = 0;
                for (ResourceLocation id : unlocks.getUnlockedItems()) {
                    Item item = BuiltInRegistries.ITEM.get(id);
                    if (item != Items.AIR) {
                        Button itemButton = new Button(item.getDescription(), theme);
                        itemButton.setFrame(
                            0,
                            itemY,
                            w - DOUBLE_PADDING * 2,
                            HISTORY_ITEM_HEIGHT + HISTORY_BUTTON_PADDING
                        );
                        itemButton.setOnClick(() -> {
                            NianTouData data = NianTouDataManager.getData(item);
                            if (data != null) {
                                previewData = data;
                                previewItemId = id;
                                showResult(data, true);
                            }
                        });
                        content.addChild(itemButton);
                        itemY += HISTORY_ITEM_STEP;
                    }
                }
                content.setFrame(
                    0,
                    0,
                    w - DOUBLE_PADDING * 2,
                    Math.max(CONTENT_MIN_HEIGHT, itemY)
                );
            } else {
                content.setFrame(0, 0, w - DOUBLE_PADDING * 2, CONTENT_MIN_HEIGHT);
            }
        } else {
            content.setFrame(0, 0, w - DOUBLE_PADDING * 2, CONTENT_MIN_HEIGHT);
        }

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
        SolidPanel panel = new SolidPanel(theme);
        panel.setFrame(x, y, w, h);
        parent.addChild(panel);

        UISlot inputSlot = new UISlot(0, theme);
        int slotX = x + (w - SLOT_SIZE) / 2;
        inputSlot.setFrame(slotX - x, SLOT_Y, SLOT_SIZE, SLOT_SIZE);
        panel.addChild(inputSlot);

        identifyButton =
            new Button(
                KongqiaoI18n.text(KongqiaoI18n.NIANTOU_BUTTON_IDENTIFY),
                theme
            );
        int buttonX = (w - BUTTON_WIDTH) / 2;
        identifyButton.setFrame(
            buttonX,
            SLOT_Y + SLOT_SIZE + BUTTON_GAP,
            BUTTON_WIDTH,
            BUTTON_HEIGHT
        );
        identifyButton.setOnClick(() -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(
                    this.menu.containerId,
                    NianTouMenu.IDENTIFY_BUTTON_ID
                );
            }
        });
        panel.addChild(identifyButton);
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

        resultLabel =
            new Label(
                KongqiaoI18n.text(KongqiaoI18n.NIANTOU_RESULT_LABEL),
                theme
            );
        resultLabel.setFrame(
            SMALL_PADDING,
            RESULT_TEXT_TOP,
            w - DOUBLE_PADDING,
            h - OUTPUT_BOTTOM_MARGIN
        );
        panel.addChild(resultLabel);

        timeLabel =
            new Label(
                KongqiaoI18n.text(KongqiaoI18n.NIANTOU_TIME_LABEL),
                theme
            );
        timeLabel.setFrame(
            SMALL_PADDING,
            SMALL_PADDING,
            w - DOUBLE_PADDING,
            LABEL_HEIGHT
        );
        panel.addChild(timeLabel);

        costLabel =
            new Label(
                KongqiaoI18n.text(KongqiaoI18n.NIANTOU_COST_LABEL),
                theme
            );
        costLabel.setFrame(
            SMALL_PADDING,
            SMALL_PADDING + LABEL_HEIGHT + SMALL_PADDING,
            w - DOUBLE_PADDING,
            LABEL_HEIGHT
        );
        panel.addChild(costLabel);
    }

    private void updateData() {
        if (menu == null) {
            return;
        }

        int progress = menu.getProgress();
        int total = menu.getTotalTime();
        int cost = menu.getCost();

        String timeStr = total > 0 ? (progress + " / " + total) : "--";
        timeLabel.setText(
            Component
                .translatable(KongqiaoI18n.NIANTOU_TIME_LABEL)
                .append(": " + timeStr)
        );

        costLabel.setText(
            Component
                .translatable(KongqiaoI18n.NIANTOU_COST_LABEL)
                .append(": " + cost)
        );

        // 若当前槽放入不同物品，则退出历史预览模式
        ResourceLocation slotItemId = null;
        if (menu.getContainer().getContainerSize() > 0) {
            net.minecraft.world.item.ItemStack stack =
                menu.getContainer().getItem(0);
            if (!stack.isEmpty()) {
                slotItemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            }
        }
        if (
            previewData != null &&
            slotItemId != null &&
            previewItemId != null &&
            !slotItemId.equals(previewItemId)
        ) {
            previewData = null;
            previewItemId = null;
        }

        // 若有历史预览，优先显示
        if (previewData != null) {
            showResult(previewData, true);
            return;
        }

        // Update result text based on item in slot 0
        if (menu.getContainer().getContainerSize() > 0) {
            net.minecraft.world.item.ItemStack stack =
                menu.getContainer().getItem(0);
            if (!stack.isEmpty()) {
                NianTouData data = NianTouDataManager.getData(stack);

                if (data != null && data.usages() != null) {
                    // Check unlocked status
                    boolean unlocked = false;
                    if (this.minecraft != null && this.minecraft.player != null) {
                        NianTouUnlocks unlocks =
                            KongqiaoAttachments.getUnlocks(this.minecraft.player);
                        if (unlocks != null) {
                            ResourceLocation id =
                                BuiltInRegistries.ITEM.getKey(stack.getItem());
                            unlocked = unlocks.isUnlocked(id);
                        }
                    }

                    StringBuilder sb = new StringBuilder();
                    showResult(data, unlocked);
                    return;
                }
                resultLabel.setText(Component.literal(FALLBACK_NO_DATA));
                return;
            }
        }
        
        // Fallback or empty
        resultLabel.setText(KongqiaoI18n.text(KongqiaoI18n.NIANTOU_RESULT_LABEL));
    }

    private void showResult(NianTouData data, boolean unlocked) {
        if (data == null || data.usages() == null) {
            resultLabel.setText(Component.literal(FALLBACK_NO_DATA));
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (unlocked) {
            sb.append(
                    KongqiaoI18n
                        .text(KongqiaoI18n.NIANTOU_OUTPUT_LABEL)
                        .getString()
                )
                .append("\n");

            for (NianTouData.Usage usage : data.usages()) {
                sb.append("- ")
                    .append(usage.usageTitle())
                    .append(" (")
                    .append(usage.usageID())
                    .append(")")
                    .append("\n");
                sb.append("  描述: ")
                    .append(usage.usageDesc())
                    .append("\n");
                sb.append("  信息: ")
                    .append(usage.getFormattedInfo())
                    .append("\n");
                sb.append("  耗时: ")
                    .append(usage.costDuration())
                    .append("\n");
                sb.append("  消耗: ")
                    .append(usage.costTotalNiantou())
                    .append("\n");
                if (usage.metadata() != null && !usage.metadata().isEmpty()) {
                    sb.append("  元数据:\n");
                    usage.metadata()
                        .forEach((k, v) ->
                            sb.append("    ")
                                .append(k)
                                .append(": ")
                                .append(v)
                                .append("\n")
                        );
                }
                sb.append("\n");
            }
            resultLabel.setText(Component.literal(sb.toString().trim()));
        } else {
            resultLabel.setText(
                KongqiaoI18n.text(KongqiaoI18n.NIANTOU_RESULT_LABEL)
            );
        }
    }
}
