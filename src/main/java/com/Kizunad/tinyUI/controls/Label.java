package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;

public final class Label extends UIElement {

    public enum HorizontalAlign {
        LEFT, CENTER, RIGHT
    }

    private static final int DEFAULT_PADDING = 2;

    private String text;
    private int color;
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;

    public Label(final String text, final Theme theme) {
        this.text = Objects.requireNonNullElse(text, "");
        this.color = theme.getTextColor();
    }

    public void setText(final String text) {
        this.text = Objects.requireNonNullElse(text, "");
    }

    public void setColor(final int color) {
        this.color = color;
    }

    public void setHorizontalAlign(final HorizontalAlign align) {
        this.horizontalAlign = Objects.requireNonNull(align, "align");
    }

    @Override
    protected void onRender(final UIRenderContext context, final double mouseX, final double mouseY,
                            final float partialTicks) {
        final int width = getWidth();
        int drawX = getAbsoluteX() + DEFAULT_PADDING;
        if (horizontalAlign == HorizontalAlign.CENTER) {
            drawX = getAbsoluteX() + (width - DEFAULT_PADDING * 2) / 2;
        } else if (horizontalAlign == HorizontalAlign.RIGHT) {
            drawX = getAbsoluteX() + width - DEFAULT_PADDING;
        }
        final int drawY = getAbsoluteY() + DEFAULT_PADDING;
        context.drawText(text, drawX, drawY, color);
    }
}
