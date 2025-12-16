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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NianTouScreen extends TinyUIContainerScreen<NianTouMenu> {

    private static final int WINDOW_WIDTH = 750;
    private static final int WINDOW_HEIGHT = 450;

    private static final int PANEL_PADDING = 10;
    private static final int LEFT_WIDTH = 200;
    private static final int RIGHT_WIDTH = 200;
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
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 20;
    private static final int RESULT_TEXT_TOP = 50;
    private static final int OUTPUT_BOTTOM_MARGIN = 30;
	private static final int HISTORY_ITEM_HEIGHT = 12;
	private static final int HISTORY_ITEM_STEP = 14;
	private static final int HISTORY_BUTTON_PADDING = 4;
	private static final int PROGRESS_BAR_EXTRA_Y = 10;
	private static final int PERCENT_MULTIPLIER = 100;
	private static final String FALLBACK_NO_DATA = "未找到该物品的念头配置。";

    private final Theme theme = Theme.vanilla();
    private Label resultLabel;
    private Label timeLabel;
    private Label costLabel;
    private Label progressBarLabel; // Use text to simulate progress bar for now
    private Button identifyButton;

	    private NianTouData previewData;
	    private ResourceLocation previewItemId;
	    private UIElement historyContent;
	    private int historyContentWidth;
	    private boolean lastProcessing;

    public NianTouScreen(NianTouMenu menu, Inventory playerInventory, Component title) {
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

        SolidPanel mainPanel = new SolidPanel(theme);
        mainPanel.setFrame(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        window.addChild(mainPanel);

        Label titleLabel = new Label(KongqiaoI18n.text(KongqiaoI18n.NIANTOU_TITLE), theme);
        titleLabel.setFrame(PANEL_PADDING, PANEL_PADDING, WINDOW_WIDTH - PANEL_PADDING * 2, TITLE_HEIGHT);
        mainPanel.addChild(titleLabel);

        int contentY = CONTENT_Y;
        int contentHeight = CONTENT_HEIGHT;
        int totalContentWidth = LEFT_WIDTH + CENTER_WIDTH + RIGHT_WIDTH + (PANEL_PADDING * 2);
        int startX = (WINDOW_WIDTH - totalContentWidth) / 2;

        buildHistoryPanel(mainPanel, startX, contentY, LEFT_WIDTH, contentHeight);
        int centerX = startX + LEFT_WIDTH + PANEL_PADDING;
        buildCenterPanel(mainPanel, centerX, contentY, CENTER_WIDTH, contentHeight);
        int rightX = centerX + CENTER_WIDTH + PANEL_PADDING;
        buildOutputPanel(mainPanel, rightX, contentY, RIGHT_WIDTH, contentHeight);

        int playerInvY = contentY + contentHeight + PLAYER_INV_GAP_Y;
	        UIElement playerGrid = InventoryUI.playerInventoryGrid(
	            PLAYER_INV_START_INDEX,
	            PLAYER_SLOT_SIZE,
	            PLAYER_SLOT_GAP,
	            PLAYER_SLOT_PADDING,
	            theme
	        );
	        int playerX = (WINDOW_WIDTH - playerGrid.getWidth()) / 2;
	        playerGrid.setFrame(playerX, playerInvY, playerGrid.getWidth(), playerGrid.getHeight());
	        mainPanel.addChild(playerGrid);

        updateData();
    }

    private void buildHistoryPanel(UIElement parent, int x, int y, int w, int h) {
        SolidPanel panel = new SolidPanel(theme);
        panel.setFrame(x, y, w, h);
        parent.addChild(panel);

        Label label = new Label(KongqiaoI18n.text(KongqiaoI18n.NIANTOU_HISTORY_LABEL), theme);
        label.setFrame(SMALL_PADDING, SMALL_PADDING, w - DOUBLE_PADDING, LABEL_HEIGHT);
        panel.addChild(label);

	        ScrollContainer scroll = new ScrollContainer(theme);
	        scroll.setFrame(SMALL_PADDING, SCROLL_TOP, w - DOUBLE_PADDING, h - SCROLL_BOTTOM_MARGIN);

	        UIElement content = new UIElement() {};

	        historyContent = content;
	        historyContentWidth = w - DOUBLE_PADDING * 2;
	        rebuildHistoryContent();

	        scroll.setContent(content);
	        panel.addChild(scroll);
	    }

	    private void buildCenterPanel(UIElement parent, int x, int y, int w, int h) {
	        SolidPanel panel = new SolidPanel(theme);
	        panel.setFrame(x, y, w, h);
	        parent.addChild(panel);

	        UISlot inputSlot = new UISlot(0, theme);
	        int slotX = x + (w - SLOT_SIZE) / 2;
	        inputSlot.setFrame(slotX - x, SLOT_Y, SLOT_SIZE, SLOT_SIZE);
	        panel.addChild(inputSlot);

	        identifyButton = new Button(KongqiaoI18n.text(KongqiaoI18n.NIANTOU_BUTTON_IDENTIFY), theme);
	        int buttonX = (w - BUTTON_WIDTH) / 2;
	        identifyButton.setFrame(buttonX, SLOT_Y + SLOT_SIZE + BUTTON_GAP, BUTTON_WIDTH, BUTTON_HEIGHT);
	        identifyButton.setOnClick(() -> {
	            if (this.minecraft != null && this.minecraft.gameMode != null) {
	                this.minecraft.gameMode.handleInventoryButtonClick(
	                    this.menu.containerId,
	                    NianTouMenu.IDENTIFY_BUTTON_ID
	                );
	            }
	        });
	        panel.addChild(identifyButton);
	        
	        progressBarLabel = new Label(Component.literal(""), theme);
	        progressBarLabel.setFrame(
	            SMALL_PADDING,
	            SLOT_Y + SLOT_SIZE + BUTTON_GAP + BUTTON_HEIGHT + PROGRESS_BAR_EXTRA_Y,
	            w - DOUBLE_PADDING,
	            LABEL_HEIGHT
	        );
	        panel.addChild(progressBarLabel);
	    }

    private void buildOutputPanel(UIElement parent, int x, int y, int w, int h) {
        SolidPanel panel = new SolidPanel(theme);
        panel.setFrame(x, y, w, h);
        parent.addChild(panel);

        resultLabel = new Label(KongqiaoI18n.text(KongqiaoI18n.NIANTOU_RESULT_LABEL), theme);
        resultLabel.setFrame(SMALL_PADDING, RESULT_TEXT_TOP, w - DOUBLE_PADDING, h - OUTPUT_BOTTOM_MARGIN);
        panel.addChild(resultLabel);

        timeLabel = new Label(KongqiaoI18n.text(KongqiaoI18n.NIANTOU_TIME_LABEL), theme);
        timeLabel.setFrame(SMALL_PADDING, SMALL_PADDING, w - DOUBLE_PADDING, LABEL_HEIGHT);
	        panel.addChild(timeLabel);

	        costLabel = new Label(KongqiaoI18n.text(KongqiaoI18n.NIANTOU_COST_LABEL), theme);
	        costLabel.setFrame(
	            SMALL_PADDING,
	            SMALL_PADDING + LABEL_HEIGHT + SMALL_PADDING,
	            w - DOUBLE_PADDING,
	            LABEL_HEIGHT
	        );
	        panel.addChild(costLabel);
	    }

	    private void updateData() {
	        if (this.minecraft == null || this.minecraft.player == null) {
	            return;
	        }

	        NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(this.minecraft.player);
	        boolean isProcessing = unlocks != null && unlocks.isProcessing();
	        if (lastProcessing && !isProcessing) {
	            rebuildHistoryContent();
	        }
	        lastProcessing = isProcessing;
	        NianTouUnlocks.UnlockProcess process = isProcessing ? unlocks.getCurrentProcess() : null;
	        NianTouData processData = null;
	        NianTouData.Usage processUsage = null;
        if (process != null) {
            Item processingItem = BuiltInRegistries.ITEM
                .getOptional(process.itemId)
                .orElse(Items.AIR);
            if (processingItem != Items.AIR) {
                processData = NianTouDataManager.getData(processingItem);
                processUsage = findUsage(processData, process.usageId);
            }
        }

        if (isProcessing && process != null) {
            float percent = 1.0F - ((float) process.remainingTicks / process.totalTicks);
            String usageName = processUsage != null ? processUsage.usageTitle() : "未知用途";
            String bar = String.format(
                Locale.ROOT,
                "鉴定中 (%s) %.1f%%",
                usageName,
                percent * PERCENT_MULTIPLIER
            );
            progressBarLabel.setText(Component.literal(bar));
            identifyButton.setEnabled(false);
            identifyButton.setText(Component.literal("鉴定中..."));
        } else {
            progressBarLabel.setText(Component.literal(""));
            identifyButton.setEnabled(true);
            identifyButton.setText(KongqiaoI18n.text(KongqiaoI18n.NIANTOU_BUTTON_IDENTIFY));
        }

        ResourceLocation slotItemId = null;
        NianTouData slotData = null;
        if (menu.getContainer().getContainerSize() > 0) {
            ItemStack stack = menu.getContainer().getItem(0);
            if (!stack.isEmpty()) {
                slotItemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                slotData = NianTouDataManager.getData(stack);
            }
        }

        boolean previewItemChanged = previewData != null
            && slotItemId != null
            && previewItemId != null
            && !slotItemId.equals(previewItemId);
        if (previewItemChanged) {
            previewData = null;
            previewItemId = null;
        }

        if (isProcessing && process != null) {
            String usageName = processUsage != null ? processUsage.usageTitle() : "未知用途";
            timeLabel.setText(
                Component.literal("用途: " + usageName + " 剩余 " + process.remainingTicks + " ticks")
            );
            costLabel.setText(
                Component.translatable(KongqiaoI18n.NIANTOU_COST_LABEL)
                    .append(": 总 " + process.totalCost)
            );
        } else if (slotData != null && slotData.usages() != null && !slotData.usages().isEmpty()) {
            LockedUsageSummary summary = summarizeLockedUsages(unlocks, slotItemId, slotData);
            if (summary.lockedCount > 0) {
                timeLabel.setText(
                    Component.literal(
                        "耗时: 随机 (" +
                        summary.lockedCount +
                        " 项) " +
                        formatRange(summary.minDuration, summary.maxDuration) +
                        " ticks"
                    )
                );
                costLabel.setText(
                    Component.literal(
                        "消耗: 随机 (" +
                        summary.lockedCount +
                        " 项) " +
                        formatRange(summary.minCost, summary.maxCost)
                    )
                );
            } else {
                timeLabel.setText(Component.literal("耗时: 全部用途已解锁"));
                costLabel.setText(Component.literal("消耗: --"));
            }
        } else {
            timeLabel.setText(
                Component.translatable(KongqiaoI18n.NIANTOU_TIME_LABEL)
                    .append(": --")
            );
            costLabel.setText(
                Component.translatable(KongqiaoI18n.NIANTOU_COST_LABEL)
                    .append(": --")
            );
        }

        if (isProcessing) {
            if (slotItemId != null && process != null && !slotItemId.equals(process.itemId)) {
                identifyButton.setText(Component.literal("其他鉴定中"));
            }
        } else if (slotData == null) {
            identifyButton.setEnabled(false);
        } else if (
            unlocks != null &&
            slotItemId != null &&
            !unlocks.hasLockedUsage(slotItemId, slotData.usages())
        ) {
            identifyButton.setEnabled(false);
            identifyButton.setText(Component.literal("全部鉴定完成"));
        }

        if (previewData != null) {
            showResult(previewData, previewItemId, unlocks, process, false);
            return;
        }

        if (slotData != null) {
            showResult(slotData, slotItemId, unlocks, process, false);
        } else {
            resultLabel.setText(Component.literal(FALLBACK_NO_DATA));
        }
    }

    private void showResult(
        NianTouData data,
        ResourceLocation itemId,
        NianTouUnlocks unlocks,
        NianTouUnlocks.UnlockProcess process,
        boolean forceReveal
    ) {
        if (data == null || data.usages() == null) {
            resultLabel.setText(Component.literal(FALLBACK_NO_DATA));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (NianTouData.Usage usage : data.usages()) {
            if (usage == null) {
                continue;
            }
            boolean unlocked = forceReveal ||
                (unlocks != null &&
                    itemId != null &&
                    unlocks.isUsageUnlocked(itemId, usage.usageID()));
            boolean running =
                !forceReveal &&
                process != null &&
                process.itemId.equals(itemId) &&
                usage.usageID().equals(process.usageId);
            sb.append("- ").append(usage.usageTitle());
            if (running) {
                sb.append(" [鉴定中]");
            } else if (unlocked) {
                sb.append(" [已解锁]");
            } else {
                sb.append(" [未解锁]");
            }
            sb.append("\n  描述: ");
            if (unlocked || running) {
                sb.append(usage.usageDesc());
                sb.append("\n  信息: ").append(usage.getFormattedInfo());
            } else {
                sb.append("暂不可见");
                sb.append("\n  信息: ？？？");
            }
            sb.append("\n  耗时: ").append(usage.costDuration()).append(" ticks");
            sb.append("\n  消耗: ").append(usage.costTotalNiantou()).append(" 念头\n\n");
	        }
	        resultLabel.setText(Component.literal(sb.toString().trim()));
	    }

	    private void rebuildHistoryContent() {
	        if (historyContent == null || historyContentWidth <= 0) {
	            return;
	        }
	        historyContent.clearChildren();
	        if (this.minecraft == null || this.minecraft.player == null) {
	            historyContent.setFrame(0, 0, historyContentWidth, CONTENT_MIN_HEIGHT);
	            return;
	        }

	        NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(this.minecraft.player);
	        if (unlocks == null) {
	            historyContent.setFrame(0, 0, historyContentWidth, CONTENT_MIN_HEIGHT);
	            return;
	        }

	        int itemY = 0;
	        Map<ResourceLocation, Set<String>> unlockedMap = unlocks.getUnlockedUsageMap();
	        for (Map.Entry<ResourceLocation, Set<String>> entry : unlockedMap.entrySet()) {
	            ResourceLocation id = entry.getKey();
	            Item item = BuiltInRegistries.ITEM.get(id);
	            if (item == Items.AIR) {
	                continue;
	            }
	            NianTouData data = NianTouDataManager.getData(item);
	            int totalUsages =
	                (data != null && data.usages() != null)
	                    ? data.usages().size()
	                    : entry.getValue().size();
	            int unlockedCount = 0;
	            if (data != null && data.usages() != null) {
	                for (NianTouData.Usage usage : data.usages()) {
	                    if (unlocks.isUsageUnlocked(id, usage.usageID())) {
	                        unlockedCount++;
	                    }
	                }
	            } else {
	                unlockedCount = entry.getValue().size();
	            }

	            Component buttonLabel = item
	                .getDescription()
	                .copy()
	                .append(" (" + unlockedCount + "/" + totalUsages + ")");
	            Button itemButton = new Button(buttonLabel, theme);
	            itemButton.setFrame(
	                0,
	                itemY,
	                historyContentWidth,
	                HISTORY_ITEM_HEIGHT + HISTORY_BUTTON_PADDING
	            );
	            itemButton.setOnClick(() -> {
	                NianTouData historyData = NianTouDataManager.getData(item);
	                if (historyData != null) {
	                    previewData = historyData;
	                    previewItemId = id;
	                    NianTouUnlocks latestUnlocks =
	                        KongqiaoAttachments.getUnlocks(this.minecraft.player);
	                    NianTouUnlocks.UnlockProcess latestProcess =
	                        latestUnlocks != null && latestUnlocks.isProcessing()
	                            ? latestUnlocks.getCurrentProcess()
	                            : null;
	                    showResult(
	                        historyData,
	                        id,
	                        latestUnlocks,
	                        latestProcess,
	                        false
	                    );
	                }
	            });
	            historyContent.addChild(itemButton);
	            itemY += HISTORY_ITEM_STEP;
	        }

	        historyContent.setFrame(
	            0,
	            0,
	            historyContentWidth,
	            Math.max(CONTENT_MIN_HEIGHT, itemY)
	        );
	    }

	    private static String formatRange(int min, int max) {
	        if (min == Integer.MAX_VALUE) {
	            return "--";
	        }
        if (min == max) {
            return String.valueOf(min);
        }
        return min + "~" + max;
    }

    private LockedUsageSummary summarizeLockedUsages(
        NianTouUnlocks unlocks,
        ResourceLocation itemId,
        NianTouData data
    ) {
        int lockedCount = 0;
        int minDuration = Integer.MAX_VALUE;
        int maxDuration = 0;
        int minCost = Integer.MAX_VALUE;
        int maxCost = 0;
        if (data != null && data.usages() != null) {
            for (NianTouData.Usage usage : data.usages()) {
                if (usage == null) {
                    continue;
                }
                boolean usageUnlocked =
                    unlocks != null &&
                        itemId != null &&
                        unlocks.isUsageUnlocked(itemId, usage.usageID());
                if (!usageUnlocked) {
                    lockedCount++;
                    minDuration = Math.min(minDuration, usage.costDuration());
                    maxDuration = Math.max(maxDuration, usage.costDuration());
                    minCost = Math.min(minCost, usage.costTotalNiantou());
                    maxCost = Math.max(maxCost, usage.costTotalNiantou());
                }
            }
        }
        return new LockedUsageSummary(lockedCount, minDuration, maxDuration, minCost, maxCost);
    }

    private NianTouData.Usage findUsage(NianTouData data, String usageId) {
        if (data == null || data.usages() == null || usageId == null) {
            return null;
        }
        for (NianTouData.Usage usage : data.usages()) {
            if (usage != null && usage.usageID().equals(usageId)) {
                return usage;
            }
        }
        return null;
    }

    private record LockedUsageSummary(
        int lockedCount,
        int minDuration,
        int maxDuration,
        int minCost,
        int maxCost
    ) {}
}
