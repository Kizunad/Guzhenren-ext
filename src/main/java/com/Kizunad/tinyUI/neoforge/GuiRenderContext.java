package com.Kizunad.tinyUI.neoforge;

import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.theme.NinePatch;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * NeoForge 渲染上下文实现，包装 GuiGraphics。
 * NinePatch 在缺省纹理时退化为矩形填充（具体纹理绑定可后续扩展）。
 */
public final class GuiRenderContext implements UIRenderContext {

    private final GuiGraphics graphics;
    private final Font font;

    public GuiRenderContext(final GuiGraphics graphics, final Font font) {
        this.graphics = graphics;
        this.font = font;
    }

    @Override
    public void pushState() {
        // NeoForge GuiGraphics 已管理状态，此处留空。
    }

    @Override
    public void popState() {
        // NeoForge GuiGraphics 已管理状态，此处留空。
    }

    @Override
    public void drawRect(final int x, final int y, final int width, final int height,
                         final int argbColor) {
        graphics.fill(x, y, x + width, y + height, argbColor);
    }

    @Override
    public void drawText(final String text, final int x, final int y, final int argbColor) {
        graphics.drawString(font, text, x, y, argbColor, false);
    }

    private static final int FALLBACK_COLOR = 0xCC1E1E1E;

    @Override
    public void drawNinePatch(final NinePatch patch, final int x, final int y, final int width,
                              final int height) {
        // 极简回退：无纹理资源时改用纯色填充。
        graphics.fill(x, y, x + width, y + height, FALLBACK_COLOR);
    }
}
