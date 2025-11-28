package com.Kizunad.tinyUI.input;

import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIElement;
import java.util.Objects;
import java.util.Optional;

/**
 * 将鼠标/键盘输入路由到目标元素，并协调焦点与热键。
 */
public final class InputRouter {

    private UIElement root;
    private final FocusManager focusManager;
    private final HotkeyManager hotkeyManager;

    public InputRouter(final UIElement root, final FocusManager focusManager,
                       final HotkeyManager hotkeyManager) {
        this.root = Objects.requireNonNull(root, "root");
        this.focusManager = Objects.requireNonNull(focusManager, "focusManager");
        this.hotkeyManager = Objects.requireNonNull(hotkeyManager, "hotkeyManager");
    }

    public void setRoot(final UIElement root) {
        this.root = Objects.requireNonNull(root, "root");
    }

    public boolean mouseClick(final double mouseX, final double mouseY, final int button) {
        final InteractiveElement target = findTopInteractive(root, mouseX, mouseY);
        if (target == null) {
            return false;
        }
        focusManager.requestFocus(target);
        return target.onMouseClick(mouseX, mouseY, button);
    }

    public boolean mouseRelease(final double mouseX, final double mouseY, final int button) {
        final InteractiveElement target = findTopInteractive(root, mouseX, mouseY);
        if (target == null) {
            return false;
        }
        return target.onMouseRelease(mouseX, mouseY, button);
    }

    public boolean mouseScroll(final double mouseX, final double mouseY, final double delta) {
        final InteractiveElement target = findTopInteractive(root, mouseX, mouseY);
        if (target == null) {
            return false;
        }
        return target.onMouseScroll(mouseX, mouseY, delta);
    }

    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        final Optional<InteractiveElement> focused = focusManager.getFocused();
        if (focused.isPresent() && focused.get().onKeyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return hotkeyManager.dispatch(KeyStroke.of(keyCode, modifiers));
    }

    public boolean charTyped(final char codePoint, final int modifiers) {
        final Optional<InteractiveElement> focused = focusManager.getFocused();
        return focused.isPresent() && focused.get().onCharTyped(codePoint, modifiers);
    }

    private InteractiveElement findTopInteractive(final UIElement current, final double mouseX,
                                                  final double mouseY) {
        if (current == null || !current.isEnabledAndVisible()) {
            return null;
        }
        if (!current.isPointInside(mouseX, mouseY)) {
            return null;
        }
        final int childCount = current.getChildren().size();
        for (int i = childCount - 1; i >= 0; i--) {
            final UIElement child = current.getChildren().get(i);
            final InteractiveElement hit = findTopInteractive(child, mouseX, mouseY);
            if (hit != null) {
                return hit;
            }
        }
        if (current instanceof InteractiveElement) {
            return (InteractiveElement) current;
        }
        return null;
    }
}
