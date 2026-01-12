package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * 飞剑状态 HUD 渲染器。
 * <p>
 * 在屏幕右侧显示当前拥有的飞剑状态：
 * <ul>
 *     <li>品质与等级</li>
 *     <li>经验进度条</li>
 *     <li>AI 模式</li>
 *     <li>距离</li>
 * </ul>
 * </p>
 */
@EventBusSubscriber(
    value = Dist.CLIENT,
    bus = EventBusSubscriber.Bus.GAME,
    modid = GuzhenrenExt.MODID
)
public final class FlyingSwordHudOverlay {

    // ===== 布局常量 =====

    /** 右边距。 */
    private static final int MARGIN_RIGHT = 8;

    /** 顶部边距。 */
    private static final int MARGIN_TOP = 40;

    /** 单个飞剑条目的高度。 */
    private static final int ENTRY_HEIGHT = 32;

    /** 条目之间的间距。 */
    private static final int ENTRY_SPACING = 4;

    /** 条目背景宽度。 */
    private static final int ENTRY_WIDTH = 100;

    /** 经验条高度。 */
    private static final int EXP_BAR_HEIGHT = 4;

    /** 经验条宽度。 */
    private static final int EXP_BAR_WIDTH = 80;

    /** 内边距。 */
    private static final int PADDING = 4;

    // ===== 颜色常量 =====

    /** 背景颜色（半透明黑）。 */
    private static final int COLOR_BG = 0x80000000;

    /** 选中背景颜色（半透明金色）。 */
    private static final int COLOR_BG_SELECTED = 0x80FFD700;

    /** 经验条背景颜色（深灰）。 */
    private static final int COLOR_EXP_BG = 0xFF333333;

    /** 经验条前景颜色（绿色）。 */
    private static final int COLOR_EXP_FG = 0xFF00FF00;

    /** 经验条满级颜色（金色）。 */
    private static final int COLOR_EXP_MAX = 0xFFFFD700;

    /** 白色文字。 */
    private static final int COLOR_WHITE = 0xFFFFFFFF;

    /** 灰色文字。 */
    private static final int COLOR_GRAY = 0xFFAAAAAA;

    /** 模式文字颜色。 */
    private static final int COLOR_MODE = 0xFF88CCFF;

    private FlyingSwordHudOverlay() {}

    /**
     * 客户端 tick 事件处理 - 刷新飞剑数据缓存。
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        FlyingSwordHudState.tick();
    }

    /**
     * GUI 渲染事件处理 - 绘制飞剑状态 HUD。
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        // 检查是否应该渲染
        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        if (!FlyingSwordHudState.isHudEnabled()) {
            return;
        }
        if (!FlyingSwordHudState.hasSwords()) {
            return;
        }

        // 获取渲染参数
        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        Font font = mc.font;

        // 获取飞剑数据
        List<FlyingSwordHudState.SwordDisplayData> swords =
            FlyingSwordHudState.getCachedSwords();

        // 渲染每把飞剑的状态
        int y = MARGIN_TOP;
        for (FlyingSwordHudState.SwordDisplayData sword : swords) {
            renderSwordEntry(graphics, font, screenWidth, y, sword);
            y += ENTRY_HEIGHT + ENTRY_SPACING;
        }
    }

    /**
     * 渲染单个飞剑条目。
     */
    private static void renderSwordEntry(
        GuiGraphics graphics,
        Font font,
        int screenWidth,
        int y,
        FlyingSwordHudState.SwordDisplayData sword
    ) {
        int x = screenWidth - MARGIN_RIGHT - ENTRY_WIDTH;

        // 背景
        int bgColor = sword.isSelected ? COLOR_BG_SELECTED : COLOR_BG;
        graphics.fill(x, y, x + ENTRY_WIDTH, y + ENTRY_HEIGHT, bgColor);

        // 选中边框
        if (sword.isSelected) {
            // 上边框
            graphics.fill(x, y, x + ENTRY_WIDTH, y + 1, COLOR_WHITE);
            // 下边框
            graphics.fill(
                x,
                y + ENTRY_HEIGHT - 1,
                x + ENTRY_WIDTH,
                y + ENTRY_HEIGHT,
                COLOR_WHITE
            );
            // 左边框
            graphics.fill(x, y, x + 1, y + ENTRY_HEIGHT, COLOR_WHITE);
            // 右边框
            graphics.fill(
                x + ENTRY_WIDTH - 1,
                y,
                x + ENTRY_WIDTH,
                y + ENTRY_HEIGHT,
                COLOR_WHITE
            );
        }

        int textX = x + PADDING;
        int textY = y + PADDING;

        // 第一行：品质 + 等级
        String qualityText = sword.getDisplayName();
        graphics.drawString(
            font,
            qualityText,
            textX,
            textY,
            sword.getQualityColor(),
            true
        );

        // 第二行：AI 模式 + 距离
        textY += font.lineHeight + 2;
        String modeText = sword.getAIModeDisplayName();
        graphics.drawString(font, modeText, textX, textY, COLOR_MODE, true);

        String distText = String.format("%.1fm", sword.distance);
        int distWidth = font.width(distText);
        graphics.drawString(
            font,
            distText,
            x + ENTRY_WIDTH - PADDING - distWidth,
            textY,
            COLOR_GRAY,
            true
        );

        // 第三行：经验条
        int barY = y + ENTRY_HEIGHT - PADDING - EXP_BAR_HEIGHT;
        int barX = textX;

        // 经验条背景
        graphics.fill(
            barX,
            barY,
            barX + EXP_BAR_WIDTH,
            barY + EXP_BAR_HEIGHT,
            COLOR_EXP_BG
        );

        // 经验条前景
        int filledWidth = (int) (EXP_BAR_WIDTH * sword.expProgress);
        if (filledWidth > 0) {
            int expColor = sword.expProgress >= 1.0f ? COLOR_EXP_MAX : COLOR_EXP_FG;
            graphics.fill(
                barX,
                barY,
                barX + filledWidth,
                barY + EXP_BAR_HEIGHT,
                expColor
            );
        }
    }
}
