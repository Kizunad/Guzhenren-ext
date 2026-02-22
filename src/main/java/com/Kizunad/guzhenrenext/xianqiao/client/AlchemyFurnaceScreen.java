package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.alchemy.menu.AlchemyFurnaceMenu;
import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.UISlot;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen;
import com.Kizunad.tinyUI.theme.Theme;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 炼丹炉屏幕。
 * <p>
 * 基于 TinyUI 实现的容器屏幕，对应 {@link AlchemyFurnaceMenu}。
 * </p>
 */
public class AlchemyFurnaceScreen extends TinyUIContainerScreen<AlchemyFurnaceMenu> {

    private static final int WINDOW_WIDTH = 176;
    private static final int WINDOW_HEIGHT = 166;

    // 复用 Menu 中的布局常量，确保 UI 槽位与逻辑槽位对齐
    private static final int MAIN_SLOT_X = 80;
    private static final int MAIN_SLOT_Y = 35;
    private static final int AUX_1_X = 56;
    private static final int AUX_1_Y = 35;
    private static final int AUX_2_X = 104;
    private static final int AUX_2_Y = 35;
    private static final int AUX_3_X = 80;
    private static final int AUX_3_Y = 13;
    private static final int AUX_4_X = 80;
    private static final int AUX_4_Y = 57;
    private static final int OUTPUT_SLOT_X = 140;
    private static final int OUTPUT_SLOT_Y = 35;

    private static final int PLAYER_INV_X_START = 8;
    private static final int PLAYER_INV_Y_START = 84;
    private static final int HOTBAR_Y = 142;
    private static final int SLOT_SPACING = 18;
    private static final int PLAYER_INV_ROWS = 3;
    private static final int PLAYER_INV_COLS = 9;

    // UI 样式常量
    private static final int BACKGROUND_COLOR = 0xFFC6C6C6;
    private static final int BORDER_COLOR = 0xFF555555;
    private static final int TEXT_COLOR = 0xFF404040;
    private static final int SLOT_SIZE = 18;

    // 组件位置常量
    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 6;
    private static final int TITLE_WIDTH = 100;
    private static final int TITLE_HEIGHT = 12;

    private static final int SUCCESS_RATE_X = 110;
    private static final int SUCCESS_RATE_Y = 6;
    private static final int SUCCESS_RATE_WIDTH = 60;
    private static final int SUCCESS_RATE_HEIGHT = 12;
    private static final String SUCCESS_RATE_PREFIX = "成功率: ";
    private static final String SUCCESS_RATE_SUFFIX = "%";

    private static final int BUTTON_X = 130;
    private static final int BUTTON_Y = 60;
    private static final int BUTTON_WIDTH = 40;
    private static final int BUTTON_HEIGHT = 20;

    private static final int QUALITY_X = 110;
    private static final int QUALITY_Y = 18;
    private static final int QUALITY_WIDTH = 60;
    private static final int QUALITY_HEIGHT = 12;

    // 槽位索引常量
    private static final int SLOT_MAIN = 0;
    private static final int SLOT_AUX_1 = 1;
    private static final int SLOT_AUX_2 = 2;
    private static final int SLOT_AUX_3 = 3;
    private static final int SLOT_AUX_4 = 4;
    private static final int SLOT_OUTPUT = 5;
    private static final int PLAYER_INV_START_INDEX = 6;

    private final Theme theme;
    private Label successRateLabel;
    private Label qualityLabel;

    public AlchemyFurnaceScreen(AlchemyFurnaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.theme = Theme.vanilla();
    }

    @Override
    protected void initUI(UIRoot root) {
        // 设置视口与设计分辨率
        root.setViewport(this.width, this.height);
        
        // 居中主面板
        int startX = (this.width - WINDOW_WIDTH) / 2;
        int startY = (this.height - WINDOW_HEIGHT) / 2;

        UIElement mainPanel = new UIElement() {
            @Override
            protected void onRender(UIRenderContext context, double mouseX, double mouseY, float partialTicks) {
                // 简单的背景渲染
                context.drawRect(
                    getAbsoluteX(),
                    getAbsoluteY(),
                    getWidth(),
                    getHeight(),
                    BACKGROUND_COLOR
                );
                // 绘制边框
                context.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), 1, BORDER_COLOR);
                context.drawRect(getAbsoluteX(), getAbsoluteY() + getHeight() - 1, getWidth(), 1, BORDER_COLOR);
                context.drawRect(getAbsoluteX(), getAbsoluteY(), 1, getHeight(), BORDER_COLOR);
                context.drawRect(getAbsoluteX() + getWidth() - 1, getAbsoluteY(), 1, getHeight(), BORDER_COLOR);
            }
        };
        mainPanel.setFrame(startX, startY, WINDOW_WIDTH, WINDOW_HEIGHT);
        root.addChild(mainPanel);

        // 标题
        Label titleLabel = new Label(this.title.getString(), theme);
        titleLabel.setFrame(TITLE_X, TITLE_Y, TITLE_WIDTH, TITLE_HEIGHT);
        titleLabel.setColor(TEXT_COLOR);
        mainPanel.addChild(titleLabel);

        successRateLabel = new Label("", theme);
        successRateLabel.setFrame(SUCCESS_RATE_X, SUCCESS_RATE_Y, SUCCESS_RATE_WIDTH, SUCCESS_RATE_HEIGHT);
        successRateLabel.setColor(TEXT_COLOR);
        mainPanel.addChild(successRateLabel);
        refreshSuccessRateText();

        qualityLabel = new Label("", theme);
        qualityLabel.setFrame(QUALITY_X, QUALITY_Y, QUALITY_WIDTH, QUALITY_HEIGHT);
        qualityLabel.setColor(TEXT_COLOR);
        mainPanel.addChild(qualityLabel);
        refreshQualityText();

        // --- 炼丹炉槽位 (0-5) ---
        addSlot(mainPanel, SLOT_MAIN, MAIN_SLOT_X, MAIN_SLOT_Y);
        addSlot(mainPanel, SLOT_AUX_1, AUX_1_X, AUX_1_Y);
        addSlot(mainPanel, SLOT_AUX_2, AUX_2_X, AUX_2_Y);
        addSlot(mainPanel, SLOT_AUX_3, AUX_3_X, AUX_3_Y);
        addSlot(mainPanel, SLOT_AUX_4, AUX_4_X, AUX_4_Y);
        addSlot(mainPanel, SLOT_OUTPUT, OUTPUT_SLOT_X, OUTPUT_SLOT_Y);

        // --- 玩家槽位 (6-41) ---
        // 索引 6-32: 背包 (3行9列)
        int slotIndex = PLAYER_INV_START_INDEX;
        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLS; col++) {
                int x = PLAYER_INV_X_START + col * SLOT_SPACING;
                int y = PLAYER_INV_Y_START + row * SLOT_SPACING;
                addSlot(mainPanel, slotIndex++, x, y);
            }
        }
        // 索引 33-41: 快捷栏
        for (int col = 0; col < PLAYER_INV_COLS; col++) {
            int x = PLAYER_INV_X_START + col * SLOT_SPACING;
            addSlot(mainPanel, slotIndex++, x, HOTBAR_Y);
        }

        // --- 炼制按钮 ---
        Button refineButton = new Button("炼制", theme);
        refineButton.setFrame(BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        refineButton.setOnClick(() -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                // 发送点击包到服务端
                this.minecraft.gameMode.handleInventoryButtonClick(
                    this.menu.containerId,
                    AlchemyFurnaceMenu.BUTTON_REFINE
                );
            }
        });
        mainPanel.addChild(refineButton);
    }

    private void addSlot(UIElement parent, int index, int x, int y) {
        UISlot slot = new UISlot(index, theme);
        slot.setFrame(x, y, SLOT_SIZE, SLOT_SIZE);
        parent.addChild(slot);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshSuccessRateText();
        refreshQualityText();
    }

    private void refreshSuccessRateText() {
        if (successRateLabel == null) {
            return;
        }
        int successRate = this.menu.getSuccessRatePercent();
        successRateLabel.setText(SUCCESS_RATE_PREFIX + successRate + SUCCESS_RATE_SUFFIX);
    }

    private void refreshQualityText() {
        if (qualityLabel == null) {
            return;
        }
        qualityLabel.setText(this.menu.getOutputQualityText());
        qualityLabel.setColor(this.menu.getOutputQualityColor());
    }

    @Override
    protected double getUiScale() {
        return 1.0;
    }
}
