package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.theme.Theme;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ToggleButtonTest {

    private static final int SIZE = 20;

    @Test
    void togglesOnReleaseInside() {
        final ToggleButton button = new ToggleButton("toggle", Theme.vanilla());
        button.setFrame(0, 0, SIZE, SIZE);
        final AtomicReference<Boolean> lastState = new AtomicReference<>();
        button.setOnToggle(lastState::set);

        assertTrue(button.onMouseClick(5, 5, 0));
        assertTrue(button.onMouseRelease(5, 5, 0));
        assertTrue(button.isToggled());
        assertEquals(Boolean.TRUE, lastState.get());

        assertTrue(button.onMouseClick(5, 5, 0));
        assertTrue(button.onMouseRelease(5, 5, 0));
        assertFalse(button.isToggled());
        assertEquals(Boolean.FALSE, lastState.get());
    }

    @Test
    void releaseOutsideDoesNotToggle() {
        final ToggleButton button = new ToggleButton("toggle", Theme.vanilla());
        button.setFrame(0, 0, SIZE, SIZE);
        button.onMouseClick(5, 5, 0);
        button.onMouseRelease(25, 25, 0);
        assertFalse(button.isToggled());
    }
}
