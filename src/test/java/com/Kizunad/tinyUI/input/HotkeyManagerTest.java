package com.Kizunad.tinyUI.input;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HotkeyManagerTest {

    private static final int KEY_CODE = 70;

    @Test
    void dispatchesToRegisteredListener() {
        final HotkeyManager manager = new HotkeyManager();
        final KeyStroke stroke = KeyStroke.of(KEY_CODE, 0);
        final TestHotkeyListener listener = new TestHotkeyListener();

        manager.register(stroke, listener);
        assertTrue(manager.dispatch(stroke), "registered listener should handle hotkey");
        assertTrue(listener.handled, "listener should be invoked");
    }

    @Test
    void unregisterStopsDispatch() {
        final HotkeyManager manager = new HotkeyManager();
        final KeyStroke stroke = KeyStroke.of(KEY_CODE, 0);
        final TestHotkeyListener listener = new TestHotkeyListener();
        manager.register(stroke, listener);

        manager.unregister(stroke, listener);
        assertFalse(manager.dispatch(stroke), "dispatch should fail after unregister");
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
