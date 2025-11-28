package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.input.KeyStroke;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HotkeyCaptureTest {

    private static final int KEY_CODE = 70;
    private static final int MODS = KeyStroke.CONTROL | KeyStroke.SHIFT;

    @Test
    void capturesKeyStrokeAndInvokesCallback() {
        final HotkeyCapture capture = new HotkeyCapture(Theme.vanilla());
        capture.setFrame(0, 0, 50, 20);
        final AtomicReference<KeyStroke> last = new AtomicReference<>();
        capture.setOnCapture(last::set);

        assertTrue(capture.onKeyPressed(KEY_CODE, 0, MODS));
        final KeyStroke captured = capture.getCaptured();
        assertNotNull(captured);
        assertEquals(last.get(), captured);
        assertEquals(KEY_CODE, captured.getKeyCode());
        assertEquals(MODS, captured.getModifiers());
    }
}
