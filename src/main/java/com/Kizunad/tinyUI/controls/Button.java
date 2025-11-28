package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;

public class Button extends InteractiveElement {

    private static final int BORDER_THICKNESS = 1;
    private static final int CONTENT_PADDING = 4;

    private String text;
    private Theme theme;
    private Runnable onClick;
    private boolean pressed;

    public Button(final String text, final Theme theme) {
        this.text = Objects.requireNonNullElse(text, "");
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    public void setText(final String text) {
        this.text = Objects.requireNonNullElse(text, "");
    }

    public void setTheme(final Theme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    public void setOnClick(final Runnable onClick) {
        this.onClick = onClick;
    }

    @Override
    public boolean onMouseClick(final double mouseX, final double mouseY, final int button) {
        if (!isEnabledAndVisible() || !isPointInside(mouseX, mouseY)) {
            return false;
        }
        pressed = true;
        return true;
    }

    @Override
    public boolean onMouseRelease(final double mouseX, final double mouseY, final int button) {
        if (!pressed) {
            return false;
        }
        final boolean inside = isPointInside(mouseX, mouseY);
        pressed = false;
        if (inside && onClick != null && isEnabledAndVisible()) {
            onClick.run();
            return true;
        }
        return inside;
    }

    @Override
    protected void onRender(final UIRenderContext context, final double mouseX, final double mouseY,
                            final float partialTicks) {
        final boolean hovered = isPointInside(mouseX, mouseY);
        final int bgColor = chooseBackground(hovered);
        context.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), bgColor);
        drawBorder(context);
        drawLabel(context);
    }

    protected int chooseBackground(final boolean hovered) {
        if (!isEnabled()) {
            return theme.getBackgroundColor();
        }
        if (pressed) {
            return theme.getAccentColor();
        }
        if (hovered) {
            return theme.getPrimaryColor();
        }
        return theme.getBackgroundColor();
    }

    private void drawBorder(final UIRenderContext context) {
        final int x = getAbsoluteX();
        final int y = getAbsoluteY();
        final int w = getWidth();
        final int h = getHeight();
        final int color = theme.getAccentColor();
        context.drawRect(x, y, w, BORDER_THICKNESS, color);
        context.drawRect(x, y + h - BORDER_THICKNESS, w, BORDER_THICKNESS, color);
        context.drawRect(x, y, BORDER_THICKNESS, h, color);
        context.drawRect(x + w - BORDER_THICKNESS, y, BORDER_THICKNESS, h, color);
    }

    private void drawLabel(final UIRenderContext context) {
        final int textX = getAbsoluteX() + CONTENT_PADDING;
        final int textY = getAbsoluteY() + CONTENT_PADDING;
        context.drawText(text, textX, textY, theme.getTextColor());
    }
}
