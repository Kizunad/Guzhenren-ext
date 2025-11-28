package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;

public final class ModalOverlay extends InteractiveElement {

    private static final int DEFAULT_ALPHA = 0x88000000;

    private final Theme theme;
    private UIElement content;
    private Runnable onClose;
    private int closeKeyCode = -1;

    public ModalOverlay(final Theme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    public void setContent(final UIElement content) {
        if (this.content != null) {
            removeChild(this.content);
        }
        this.content = content;
        if (content != null) {
            addChild(content);
        }
    }

    public void setOnClose(final Runnable onClose) {
        this.onClose = onClose;
    }

    public void setCloseKeyCode(final int closeKeyCode) {
        this.closeKeyCode = closeKeyCode;
    }

    @Override
    public boolean onMouseClick(final double mouseX, final double mouseY, final int button) {
        if (content != null && content.isPointInside(mouseX, mouseY)) {
            return forwardMouseClick(content, mouseX, mouseY, button);
        }
        return true; // 阻塞背景点击
    }

    @Override
    public boolean onMouseRelease(final double mouseX, final double mouseY, final int button) {
        if (content != null && content.isPointInside(mouseX, mouseY)) {
            return forwardMouseRelease(content, mouseX, mouseY, button);
        }
        return true;
    }

    @Override
    public boolean onKeyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (closeKeyCode >= 0 && keyCode == closeKeyCode) {
            close();
            return true;
        }
        return content instanceof InteractiveElement
                && ((InteractiveElement) content).onKeyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void onRender(final UIRenderContext context, final double mouseX, final double mouseY,
                            final float partialTicks) {
        context.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), DEFAULT_ALPHA);
        if (content != null) {
            content.render(context, mouseX, mouseY, partialTicks);
        }
    }

    private boolean forwardMouseClick(final UIElement element, final double mouseX,
                                      final double mouseY, final int button) {
        if (element instanceof InteractiveElement) {
            return ((InteractiveElement) element).onMouseClick(mouseX, mouseY, button);
        }
        return false;
    }

    private boolean forwardMouseRelease(final UIElement element, final double mouseX,
                                        final double mouseY, final int button) {
        if (element instanceof InteractiveElement) {
            return ((InteractiveElement) element).onMouseRelease(mouseX, mouseY, button);
        }
        return false;
    }

    private void close() {
        setVisible(false);
        if (onClose != null) {
            onClose.run();
        }
    }
}
