package com.Kizunad.tinyUI.controls;

import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TooltipManagerTest {

    private static final double DELAY = 0.5d;
    private static final int MOUSE_X_1 = 10;
    private static final int MOUSE_Y_1 = 20;
    private static final double TIME_1 = 0.2d;
    private static final int MOUSE_X_2 = 12;
    private static final int MOUSE_Y_2 = 25;
    private static final double TIME_2 = 0.3d;
    private static final int OFFSET_X = 10;
    private static final int OFFSET_Y = 12;
    private static final double TIME_AFTER_DELAY = 1.0d;
    private static final double TIME_SHORT = 0.1d;

    @Test
    void activatesAfterDelayAndFollowsMouse() {
        final TooltipManager manager = new TooltipManager(DELAY);
        manager.update("tip", true, MOUSE_X_1, MOUSE_Y_1, TIME_1);
        assertTrue(manager.getActiveState().isEmpty(), "should not show before delay");

        manager.update("tip", true, MOUSE_X_2, MOUSE_Y_2, TIME_2);
        final Optional<TooltipManager.TooltipState> active = manager.getActiveState();
        assertTrue(active.isPresent(), "should show after delay reached");
        assertEquals(MOUSE_X_2 + OFFSET_X, active.get().getX(), "should follow mouse with offset x");
        assertEquals(MOUSE_Y_2 + OFFSET_Y, active.get().getY(), "should follow mouse with offset y");
        assertEquals("tip", active.get().getText());
    }

    @Test
    void resetOnLeave() {
        final TooltipManager manager = new TooltipManager(DELAY);
        manager.update("tip", true, MOUSE_X_1, MOUSE_Y_1, TIME_AFTER_DELAY);
        assertTrue(manager.getActiveState().isPresent(), "active after delay");

        manager.update("tip", false, MOUSE_X_1, MOUSE_Y_1, TIME_SHORT);
        assertTrue(manager.getActiveState().isEmpty(), "should hide after leaving hover");
    }
}
