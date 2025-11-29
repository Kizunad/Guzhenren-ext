package com.Kizunad.tinyUI.core;

/**
 * 支持输入与焦点的元素基础类。
 */
public abstract class InteractiveElement extends UIElement {

    private boolean focusable = true;
    private boolean focused;

    public boolean isFocusable() {
        return focusable && isEnabledAndVisible();
    }

    public void setFocusable(final boolean focusable) {
        this.focusable = focusable;
    }

    public boolean isFocused() {
        return focused;
    }

    /**
     * 由 FocusManager 调用以更新焦点状态。
     */
    public void setFocused(final boolean focused) {
        if (this.focused == focused) {
            return;
        }
        this.focused = focused;
        if (focused) {
            onFocusGained();
        } else {
            onFocusLost();
        }
    }

    public boolean onMouseClick(final double mouseX, final double mouseY, final int button) {
        return false;
    }

    public boolean onMouseRelease(final double mouseX, final double mouseY, final int button) {
        return false;
    }

    public boolean onMouseScroll(final double mouseX, final double mouseY, final double delta) {
        return false;
    }

    public boolean onMouseDrag(final double mouseX, final double mouseY, final int button,
                               final double dragX, final double dragY) {
        return false;
    }

    public boolean onKeyPressed(final int keyCode, final int scanCode, final int modifiers) {
        return false;
    }

    public boolean onCharTyped(final char codePoint, final int modifiers) {
        return false;
    }

    protected void onFocusGained() {
        // 默认无行为。
    }

    protected void onFocusLost() {
        // 默认无行为。
    }
}
