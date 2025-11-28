package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.theme.Theme;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ButtonTest {

    private static final int SIZE = 20;

    @Test
    void clickInsideTriggersCallbackOnRelease() {
        final Theme theme = Theme.vanilla();
        final Button button = new Button("ok", theme);
        button.setFrame(0, 0, SIZE, SIZE);
        final AtomicInteger clicks = new AtomicInteger();
        button.setOnClick(clicks::incrementAndGet);

        assertTrue(button.onMouseClick(5, 5, 0));
        assertTrue(button.onMouseRelease(5, 5, 0));
        assertEquals(1, clicks.get());
    }

    @Test
    void releaseOutsideDoesNotTrigger() {
        final Button button = new Button("ok", Theme.vanilla());
        button.setFrame(0, 0, SIZE, SIZE);
        final AtomicInteger clicks = new AtomicInteger();
        button.setOnClick(clicks::incrementAndGet);

        assertTrue(button.onMouseClick(5, 5, 0));
        assertFalse(button.onMouseRelease(25, 25, 0));
        assertEquals(0, clicks.get());
    }

    @Test
    void disabledButtonDoesNotHandleClick() {
        final Button button = new Button("ok", Theme.vanilla());
        button.setFrame(0, 0, SIZE, SIZE);
        button.setEnabled(false);
        final AtomicInteger clicks = new AtomicInteger();
        button.setOnClick(clicks::incrementAndGet);

        assertFalse(button.onMouseClick(5, 5, 0));
        assertFalse(button.onMouseRelease(5, 5, 0));
        assertEquals(0, clicks.get());
    }
}
