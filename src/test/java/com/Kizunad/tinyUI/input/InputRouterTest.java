package com.Kizunad.tinyUI.input;

import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIRoot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class InputRouterTest {

    private static final int ROOT_SIZE = 100;
    private static final int CHILD_SIZE = 20;
    private static final int CLICK_X = 10;
    private static final int CLICK_Y = 10;
    private static final int HOTKEY_CODE = 65;

    private UIRoot root;
    private FocusManager focusManager;
    private HotkeyManager hotkeyManager;
    private InputRouter router;

    @BeforeEach
    void setUp() {
        root = new UIRoot();
        root.setViewport(ROOT_SIZE, ROOT_SIZE);
        focusManager = new FocusManager();
        hotkeyManager = new HotkeyManager();
        router = new InputRouter(root, focusManager, hotkeyManager);
    }

    @Test
    void mouseClickHitsTopMostChildAndSetsFocus() {
        final TestInteractive bottom = new TestInteractive();
        bottom.setFrame(0, 0, CHILD_SIZE, CHILD_SIZE);
        root.addChild(bottom);

        final TestInteractive top = new TestInteractive();
        top.setFrame(0, 0, CHILD_SIZE, CHILD_SIZE);
        root.addChild(top);

        final boolean handled = router.mouseClick(CLICK_X, CLICK_Y, 0);
        assertTrue(handled, "mouse click should be handled");
        assertSame(top, focusManager.getFocused().orElse(null), "topmost child should gain focus");
        assertTrue(top.clicked, "top child should receive click");
        assertFalse(bottom.clicked, "bottom child should not receive click");
    }

    @Test
    void keyPressedPrefersFocusedThenGlobalHotkey() {
        final TestInteractive focused = new TestInteractive();
        focused.setFrame(0, 0, CHILD_SIZE, CHILD_SIZE);
        root.addChild(focused);
        focusManager.requestFocus(focused);

        final boolean handledByFocused = router.keyPressed(HOTKEY_CODE, 0, 0);
        assertTrue(handledByFocused, "focused element should handle key press");
        assertTrue(focused.keyPressed, "focused element key handler invoked");

        final TestInteractive unfocused = new TestInteractive();
        unfocused.setFrame(0, 0, CHILD_SIZE, CHILD_SIZE);
        root.addChild(unfocused);

        final TestHotkeyListener hotkeyListener = new TestHotkeyListener();
        hotkeyManager.register(KeyStroke.of(HOTKEY_CODE, 0), hotkeyListener);
        focused.keyPressHandled = false; // stop handling to allow propagation

        final boolean handledByHotkey = router.keyPressed(HOTKEY_CODE, 0, 0);
        assertTrue(handledByHotkey, "hotkey listener should handle key when focus does not");
        assertTrue(hotkeyListener.handled, "hotkey listener invoked");
    }

    private static final class TestInteractive extends InteractiveElement {

        private boolean clicked;
        private boolean keyPressed;
        private boolean keyPressHandled = true;

        @Override
        public boolean onMouseClick(final double mouseX, final double mouseY, final int button) {
            clicked = true;
            return true;
        }

        @Override
        public boolean onKeyPressed(final int keyCode, final int scanCode, final int modifiers) {
            keyPressed = true;
            return keyPressHandled;
        }
    }

    private static final class TestHotkeyListener implements HotkeyListener {

        private boolean handled;

        @Override
        public boolean onHotkey(final KeyStroke stroke) {
            handled = true;
            return true;
        }
    }
}
