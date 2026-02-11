package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.service.SpiritUnlockService;
import com.Kizunad.guzhenrenext.xianqiao.spirit.LandSpiritMenu;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 地灵管理界面。
 * <p>
 * 该界面采用纯文字方式展示地灵与仙窍关键状态，
 * 不依赖任何贴图资源，便于在早期功能阶段快速验证服务端数据同步链路。
 * </p>
 */
public class LandSpiritScreen extends AbstractContainerScreen<LandSpiritMenu> {

    /** 文本颜色（浅灰）。 */
    private static final int TEXT_COLOR = 0xE0E0E0;

    /** 标题文本颜色（亮白）。 */
    private static final int TITLE_COLOR = 0xFFFFFF;

    /** 背景外框颜色。 */
    private static final int BG_OUTER_COLOR = 0xE0111111;

    /** 背景内层颜色。 */
    private static final int BG_INNER_COLOR = 0xE01A1A1A;

    /** 界面宽度。 */
    private static final int BG_WIDTH = 198;

    /** 界面高度。 */
    private static final int BG_HEIGHT = 150;

    /** 左侧文本起始 X。 */
    private static final int LABEL_X = 8;

    /** 标题 Y。 */
    private static final int TITLE_Y = 6;

    /** 数据首行 Y。 */
    private static final int FIRST_LINE_Y = 22;

    /** 行间距。 */
    private static final int LINE_HEIGHT = 12;

    /** 好感度缩放因子：permille / 10 -> 一位小数。 */
    private static final double FAVORABILITY_PERMILLE_FACTOR = 10.0D;

    /** 好感度上限。 */
    private static final int FAVORABILITY_MAX = 100;

    /** 时间流速缩放因子：permille / 1000 -> 倍速。 */
    private static final double TIME_SPEED_PERMILLE_BASE = 1000.0D;

    /** 玩家背包标题 Y（屏幕不展示背包区域时下移避免视觉干扰）。 */
    private static final int INVENTORY_LABEL_Y = BG_HEIGHT - 12;

    /** 阶段提示：已达最高阶段。 */
    private static final String MAX_STAGE_REACHED_TEXT = "已达最高阶段";

    public LandSpiritScreen(LandSpiritMenu menu, Inventory inventory, Component title) {
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
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, Component.literal("§l地灵管理"), LABEL_X, TITLE_Y, TITLE_COLOR, false);

        int line = 0;
        drawLine(
            guiGraphics,
            Component.literal("仙窍坐标：" + menu.getCenterX() + ", " + menu.getCenterY() + ", " + menu.getCenterZ()),
            line
        );
        line++;

        drawLine(guiGraphics, Component.literal("当前半径：" + menu.getRadius()), line);
        line++;

        double timeSpeed = menu.getTimeSpeedPermille() / TIME_SPEED_PERMILLE_BASE;
        drawLine(guiGraphics, Component.literal("时间流速：" + formatOneDecimal(timeSpeed) + "x"), line);
        line++;

        drawLine(guiGraphics, Component.literal("灾劫倒计时：" + menu.getTribulationRemainingTicks() + " tick"), line);
        line++;

        double favorability = menu.getFavorabilityPermille() / FAVORABILITY_PERMILLE_FACTOR;
        drawLine(
            guiGraphics,
            Component.literal("§e好感度：§f " + formatOneDecimal(favorability) + " / " + FAVORABILITY_MAX),
            line
        );
        line++;

        drawLine(guiGraphics, Component.literal("§e转数：§f " + menu.getTier()), line);
        line++;

        int currentStage = menu.getCurrentStage();
        String stageDisplayName = SpiritUnlockService.getStageDisplayName(currentStage);
        drawLine(
            guiGraphics,
            Component.literal("§e当前阶段：§f " + currentStage + " - " + stageDisplayName),
            line
        );
        line++;

        int maxStage = SpiritUnlockService.getMaxStage();
        if (currentStage >= maxStage) {
            drawLine(guiGraphics, Component.literal("§7下一阶段需要：" + MAX_STAGE_REACHED_TEXT), line);
        } else {
            double nextMinFavorability = menu.getNextStageMinFavorabilityPermille() / FAVORABILITY_PERMILLE_FACTOR;
            drawLine(
                guiGraphics,
                Component.literal(
                    "§7下一阶段需要：转数 ≥ "
                        + menu.getNextStageMinTier()
                        + "，好感度 ≥ "
                        + formatOneDecimal(nextMinFavorability)
                ),
                line
            );
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /**
     * 按统一行高绘制文本行，确保信息对齐稳定。
     */
    private void drawLine(GuiGraphics guiGraphics, Component text, int lineIndex) {
        int y = FIRST_LINE_Y + (lineIndex * LINE_HEIGHT);
        guiGraphics.drawString(font, text, LABEL_X, y, TEXT_COLOR, false);
    }

    /**
     * 将数值格式化为一位小数，供好感度与时间流速显示。
     */
    private static String formatOneDecimal(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
