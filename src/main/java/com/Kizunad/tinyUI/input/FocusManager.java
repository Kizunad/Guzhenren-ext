package com.Kizunad.tinyUI.input;

import com.Kizunad.tinyUI.core.InteractiveElement;
import java.util.Objects;
import java.util.Optional;

/**
 * 管理焦点切换，保证同一时间仅一个元素持有焦点。
 */
public final class FocusManager {

    private InteractiveElement focused;

    public boolean requestFocus(final InteractiveElement element) {
        Objects.requireNonNull(element, "element");
        if (!element.isFocusable()) {
            return false;
        }
        if (element == focused) {
            return true;
        }
        clearFocus();
        focused = element;
        focused.setFocused(true);
        return true;
    }

    public void clearFocus() {
        if (focused == null) {
            return;
        }
        final InteractiveElement previous = focused;
        focused = null;
        previous.setFocused(false);
    }

    public Optional<InteractiveElement> getFocused() {
        return Optional.ofNullable(focused);
    }

    public boolean isFocused(final InteractiveElement element) {
        return focused == element;
    }
}
