package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.input.KeyStroke;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 捕获按键并输出 KeyStroke 的控件，用于快捷键配置。
 */
public final class HotkeyCapture extends InteractiveElement {

    private static final int PADDING = 4;
    private static final int BORDER = 1;

    private final Theme theme;
    private KeyStroke captured;
    private Consumer<KeyStroke> onCapture;

    public HotkeyCapture(final Theme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    public void setOnCapture(final Consumer<KeyStroke> onCapture) {
        this.onCapture = onCapture;
    }

    public KeyStroke getCaptured() {
        return captured;
    }

    @Override
    public boolean onKeyPressed(final int keyCode, final int scanCode, final int modifiers) {
        captured = KeyStroke.of(keyCode, modifiers);
        if (onCapture != null) {
            onCapture.accept(captured);
        }
        return true;
    }

    @Override
    protected void onRender(final UIRenderContext context, final double mouseX, final double mouseY,
                            final float partialTicks) {
        context.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(),
                theme.getBackgroundColor());
        drawBorder(context);
        final String label = captured != null ? captured.toString() : "[Press key]";
        context.drawText(label, getAbsoluteX() + PADDING, getAbsoluteY() + PADDING,
                theme.getTextColor());
    }

    private void drawBorder(final UIRenderContext context) {
        final int x = getAbsoluteX();
        final int y = getAbsoluteY();
        final int w = getWidth();
        final int h = getHeight();
        final int color = theme.getAccentColor();
        context.drawRect(x, y, w, BORDER, color);
        context.drawRect(x, y + h - BORDER, w, BORDER, color);
        context.drawRect(x, y, BORDER, h, color);
        context.drawRect(x + w - BORDER, y, BORDER, h, color);
    }
}
