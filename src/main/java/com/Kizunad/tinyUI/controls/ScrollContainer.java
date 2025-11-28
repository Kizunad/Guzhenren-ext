package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;

public final class ScrollContainer extends InteractiveElement {

    private static final int SCROLL_STEP = 12;
    private static final int BORDER_THICKNESS = 1;

    private final Theme theme;
    private UIElement content;
    private int scrollY;

    public ScrollContainer(final Theme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    public void setContent(final UIElement content) {
        if (this.content != null) {
            removeChild(this.content);
        }
        this.content = content;
        if (content != null) {
            addChild(content);
            applyScroll();
        }
    }

    @Override
    public boolean onMouseScroll(final double mouseX, final double mouseY, final double delta) {
        if (!isEnabledAndVisible() || content == null || !isPointInside(mouseX, mouseY)) {
            return false;
        }
        final int maxScroll = Math.max(0, content.getHeight() - getHeight());
        scrollY = clamp(scrollY - (int) (delta * SCROLL_STEP), 0, maxScroll);
        applyScroll();
        return true;
    }

    @Override
    protected void onRender(final UIRenderContext context, final double mouseX, final double mouseY,
                            final float partialTicks) {
        context.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(),
                theme.getBackgroundColor());
        drawBorder(context);
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

    private void applyScroll() {
        if (content == null) {
            return;
        }
        content.setFrame(0, -scrollY, getWidth(), content.getHeight());
        content.onLayoutUpdated();
    }

    private static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }
}
