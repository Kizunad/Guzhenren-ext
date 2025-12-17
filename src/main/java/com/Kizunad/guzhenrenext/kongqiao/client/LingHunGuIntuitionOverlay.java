package com.Kizunad.guzhenrenext.kongqiao.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * 羚魂蛊灵觉：HUD 方向提示渲染。
 * <p>
 * 表现：在屏幕边缘绘制一段“微弱白光”，位置由角度确定，随时间衰减。
 * </p>
 */
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class LingHunGuIntuitionOverlay {

    private static final int COLOR_WHITE_RGB = 0x00FFFFFF;
    private static final float MAX_ALPHA = 0.55F;
    private static final int EDGE_MARGIN = 10;
    private static final float DIR_EPSILON = 0.0001F;
    private static final int BYTE_MAX = 255;
    private static final int ALPHA_SHIFT = 24;

    private static final int OUTER_HALF_W = 18;
    private static final int OUTER_HALF_H = 6;
    private static final int INNER_HALF_W = 12;
    private static final int INNER_HALF_H = 4;

    private LingHunGuIntuitionOverlay() {
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event) {
        LingHunGuIntuitionClientState.tick();
    }

    @SubscribeEvent
    public static void onRenderGui(final RenderGuiEvent.Post event) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }
        if (!LingHunGuIntuitionClientState.isActive()) {
            return;
        }

        final GuiGraphics graphics = event.getGuiGraphics();
        final int width = minecraft.getWindow().getGuiScaledWidth();
        final int height = minecraft.getWindow().getGuiScaledHeight();
        final int centerX = width / 2;
        final int centerY = height / 2;

        final float angle = LingHunGuIntuitionClientState.angleRadians();
        final float alpha = LingHunGuIntuitionClientState.alphaFactor() * MAX_ALPHA;
        if (alpha <= 0.0F) {
            return;
        }

        // 将“角度”映射为从屏幕中心向外的一条射线，并计算与屏幕边界的交点。
        final float dirX = (float) Math.sin(angle);
        final float dirY = (float) -Math.cos(angle);

        final float halfW = (float) centerX - EDGE_MARGIN;
        final float halfH = (float) centerY - EDGE_MARGIN;
        final float tX = Math.abs(dirX) < DIR_EPSILON ? Float.MAX_VALUE : halfW / Math.abs(dirX);
        final float tY = Math.abs(dirY) < DIR_EPSILON ? Float.MAX_VALUE : halfH / Math.abs(dirY);
        final float t = Math.min(tX, tY);

        final int x = Math.round(centerX + dirX * t);
        final int y = Math.round(centerY + dirY * t);

        drawGlowRect(graphics, x, y, alpha);
    }

    private static void drawGlowRect(
        final GuiGraphics graphics,
        final int centerX,
        final int centerY,
        final float alpha
    ) {
        final int outerArgb = argb(alpha * 0.55F);
        final int innerArgb = argb(alpha);

        graphics.fill(
            centerX - OUTER_HALF_W,
            centerY - OUTER_HALF_H,
            centerX + OUTER_HALF_W,
            centerY + OUTER_HALF_H,
            outerArgb
        );
        graphics.fill(
            centerX - INNER_HALF_W,
            centerY - INNER_HALF_H,
            centerX + INNER_HALF_W,
            centerY + INNER_HALF_H,
            innerArgb
        );
    }

    private static int argb(final float alpha) {
        final int a = Math.max(0, Math.min(BYTE_MAX, (int) (alpha * BYTE_MAX)));
        return (a << ALPHA_SHIFT) | COLOR_WHITE_RGB;
    }
}
