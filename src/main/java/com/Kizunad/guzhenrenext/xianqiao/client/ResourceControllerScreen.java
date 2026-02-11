package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.resource.ResourceControllerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * 资源控制器 GUI。
 * <p>
 * 显示核心运行指标：
 * 1) 结构状态；
 * 2) 进度条；
 * 3) 效率百分比；
 * 4) 环境灵气；
 * 5) 剩余倒计时。
 * </p>
 */
public class ResourceControllerScreen extends AbstractContainerScreen<ResourceControllerMenu> {

    private static final int TEXT_COLOR = 0x404040;

    private static final int WARN_COLOR = 0xD05050;

    private static final int OK_COLOR = 0x40A060;

    private static final int BG_WIDTH = 176;

    private static final int BG_HEIGHT = 166;

    private static final int TITLE_Y = 6;

    private static final int STATUS_Y = 18;

    private static final int PROGRESS_TEXT_Y = 32;

    private static final int PROGRESS_BAR_X = 8;

    private static final int PROGRESS_BAR_Y = 44;

    private static final int PROGRESS_BAR_WIDTH = 160;

    private static final int PROGRESS_BAR_HEIGHT = 8;

    private static final int EFFICIENCY_TEXT_Y = 58;

    private static final int AURA_TEXT_Y = 70;

    private static final int REMAINING_TEXT_Y = 82;

    private static final int BAR_BG_COLOR = 0xFF2A2A2A;

    private static final int BAR_FILL_COLOR = 0xFF6BA8FF;

    private static final int BAR_BORDER_COLOR = 0xFF808080;

    private static final int BG_OUTER_COLOR = 0xFF1E1E1E;

    private static final int BG_INNER_COLOR = 0xFF262626;

    private static final int LABEL_X = 8;

    private static final int INVENTORY_LABEL_Y = 74;

    private static final int PERCENT_100 = 100;

    private static final int PROGRESS_PERMILLE_BASE = 1000;

    public ResourceControllerScreen(ResourceControllerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = BG_WIDTH;
        this.imageHeight = BG_HEIGHT;
        this.inventoryLabelY = INVENTORY_LABEL_Y;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;

        guiGraphics.fill(left, top, left + imageWidth, top + imageHeight, BG_OUTER_COLOR);
        guiGraphics.fill(left + 1, top + 1, left + imageWidth - 1, top + imageHeight - 1, BG_INNER_COLOR);

        int barLeft = left + PROGRESS_BAR_X;
        int barTop = top + PROGRESS_BAR_Y;
        guiGraphics.fill(barLeft, barTop, barLeft + PROGRESS_BAR_WIDTH, barTop + PROGRESS_BAR_HEIGHT, BAR_BG_COLOR);

        int permille = menu.getProgressPermille();
        int fillWidth = Mth.clamp((PROGRESS_BAR_WIDTH * permille) / PROGRESS_PERMILLE_BASE, 0, PROGRESS_BAR_WIDTH);
        guiGraphics.fill(barLeft, barTop, barLeft + fillWidth, barTop + PROGRESS_BAR_HEIGHT, BAR_FILL_COLOR);

        guiGraphics.hLine(barLeft, barLeft + PROGRESS_BAR_WIDTH, barTop, BAR_BORDER_COLOR);
        guiGraphics.hLine(barLeft, barLeft + PROGRESS_BAR_WIDTH, barTop + PROGRESS_BAR_HEIGHT, BAR_BORDER_COLOR);
        guiGraphics.vLine(barLeft, barTop, barTop + PROGRESS_BAR_HEIGHT, BAR_BORDER_COLOR);
        guiGraphics.vLine(barLeft + PROGRESS_BAR_WIDTH, barTop, barTop + PROGRESS_BAR_HEIGHT, BAR_BORDER_COLOR);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, TITLE_Y, TEXT_COLOR, false);

        boolean formed = menu.isFormed();
        Component status = formed
            ? Component.literal("结构状态：已形成")
            : Component.literal("结构状态：未形成（组件不足）");
        int statusColor = formed ? OK_COLOR : WARN_COLOR;
        guiGraphics.drawString(font, status, LABEL_X, STATUS_Y, statusColor, false);

        int progressPercent = (menu.getProgressPermille() * PERCENT_100) / PROGRESS_PERMILLE_BASE;
        guiGraphics.drawString(
            font,
            Component.literal("产出进度：" + progressPercent + "%"),
            LABEL_X,
            PROGRESS_TEXT_Y,
            TEXT_COLOR,
            false
        );
        guiGraphics.drawString(
            font,
            Component.literal("当前效率：" + menu.getEfficiencyPercent() + "%"),
            LABEL_X,
            EFFICIENCY_TEXT_Y,
            TEXT_COLOR,
            false
        );
        guiGraphics.drawString(
            font,
            Component.literal("环境时道灵气：" + menu.getAuraValue()),
            LABEL_X,
            AURA_TEXT_Y,
            TEXT_COLOR,
            false
        );
        guiGraphics.drawString(
            font,
            Component.literal("预计剩余：" + menu.getRemainingTicks() + " tick"),
            LABEL_X,
            REMAINING_TEXT_Y,
            TEXT_COLOR,
            false
        );

        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT_COLOR, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
