package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;

/**
 * 文本标签控件 - 用于显示静态或动态文本。
 * <p>
 * 功能：
 * <ul>
 *   <li>显示文本内容</li>
 *   <li>支持水平对齐（左、中、右）</li>
 *   <li>可自定义文本颜色</li>
 *   <li>自动处理内边距</li>
 * </ul>
 *
 * @see UIElement
 * @see Theme
 */
public final class Label extends UIElement {

    /**
     * 水平对齐方式。
     */
    public enum HorizontalAlign {
        /** 左对齐 */
        LEFT,
        /** 居中对齐 */
        CENTER,
        /** 右对齐 */
        RIGHT,
    }

    /** 默认内边距（像素） */
    private static final int DEFAULT_PADDING = 2;
    private static final int DEFAULT_FONT_LINE_HEIGHT = 12;

    /** 文本内容 */
    private String text;
    /** 文本颜色（ARGB 格式） */
    private int color;
    /** 水平对齐方式 */
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;

    /**
     * 创建文本标签。
     *
     * @param text 标签文本（如果为 null 则使用空字符串）
     * @param theme 主题配置（用于获取文本颜色，不能为 null）
     */
    public Label(final String text, final Theme theme) {
        this.text = Objects.requireNonNullElse(text, "");
        this.color = theme.getTextColor();
    }

    /**
     * 设置标签文本。
     *
     * @param text 新的文本内容（如果为 null 则使用空字符串）
     */
    public void setText(final String text) {
        this.text = Objects.requireNonNullElse(text, "");
    }

    /**
     * 设置文本颜色。
     *
     * @param color 新的文本颜色（ARGB 格式）
     */
    public void setColor(final int color) {
        this.color = color;
    }

    /**
     * 设置水平对齐方式。
     *
     * @param align 对齐方式（不能为 null）
     * @throws NullPointerException 如果 align 为 null
     */
    public void setHorizontalAlign(final HorizontalAlign align) {
        this.horizontalAlign = Objects.requireNonNull(align, "align");
    }

    @Override
    protected void onRender(
        final UIRenderContext context,
        final double mouseX,
        final double mouseY,
        final float partialTicks
    ) {
        final int width = getWidth();
        int drawX = getAbsoluteX() + DEFAULT_PADDING;
        if (horizontalAlign == HorizontalAlign.CENTER) {
            drawX = getAbsoluteX() + (width - DEFAULT_PADDING * 2) / 2;
        } else if (horizontalAlign == HorizontalAlign.RIGHT) {
            drawX = getAbsoluteX() + width - DEFAULT_PADDING;
        }

        // Support multi-line text by splitting on \n
        String[] lines = text.split("\n");
        int drawY = getAbsoluteY() + DEFAULT_PADDING;
        final int lineHeight = DEFAULT_FONT_LINE_HEIGHT;

        for (String line : lines) {
            context.drawText(line, drawX, drawY, color);
            drawY += lineHeight;
        }
    }
}
