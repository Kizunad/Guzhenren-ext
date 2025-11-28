package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;

public final class Tooltip extends UIElement {

    private static final int PADDING = 4;

    private String text;
    private final Theme theme;

    public Tooltip(final String text, final Theme theme) {
        this.text = Objects.requireNonNullElse(text, "");
        this.theme = Objects.requireNonNull(theme, "theme");
    }

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
