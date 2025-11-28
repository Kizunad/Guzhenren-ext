package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;

/**
 * 工具提示控件 - 用于显示快速提示信息。
 * 通常用于鼠标悬停时显示附加说明。
 * <p>
 * 功能：
 * <ul>
 *   <li>显示带有背景和边框的提示文本</li>
 *   <li>自动处理内边距</li>
 *   <li>使用主题颜色</li>
 * </ul>
 *
 * @see UIElement
 * @see Theme
 */
public final class Tooltip extends UIElement {

    /** 内边距（像素） */
    private static final int PADDING = 4;

    /** 提示文本 */
    private String text;
    /** 主题配置 */
    private final Theme theme;

    /**
     * 创建工具提示。
     *
     * @param text 提示文本（如果为 null 则使用空字符串）
     * @param theme 主题配置（不能为 null）
     * @throws NullPointerException 如果 theme 为 null
     */
    public Tooltip(final String text, final Theme theme) {
        this.text = Objects.requireNonNullElse(text, "");
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    /**
     * 设置提示文本。
     *
     * @param text 新的提示文本（如果为 null 则使用空字符串）
     */
    public void setText(final String text) {
        this.text = Objects.requireNonNullElse(text, "");
    }

    @Override
    protected void onRender(final UIRenderContext context, final double mouseX, final double mouseY,
                            final float partialTicks) {
        final int absX = getAbsoluteX();
        final int absY = getAbsoluteY();
        context.drawRect(absX, absY, getWidth(), getHeight(), theme.getBackgroundColor());
        context.drawRect(absX, absY, getWidth(), PADDING / 2, theme.getAccentColor());
        context.drawText(text, absX + PADDING, absY + PADDING, theme.getTextColor());
    }
}
