package com.Kizunad.tinyUI.controls;

import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TooltipManagerTest {

    private static final double DELAY = 0.5d;

    @Test
    void activatesAfterDelayAndFollowsMouse() {
        final TooltipManager manager = new TooltipManager(DELAY);
        manager.update("tip", true, 10, 20, 0.2d);
        assertTrue(manager.getActiveState().isEmpty(), "should not show before delay");

        manager.update("tip", true, 12, 25, 0.3d);
        final Optional<TooltipManager.TooltipState> active = manager.getActiveState();
        assertTrue(active.isPresent(), "should show after delay reached");
        assertEquals(12 + 10, active.get().getX(), "should follow mouse with offset x");
        assertEquals(25 + 12, active.get().getY(), "should follow mouse with offset y");
        assertEquals("tip", active.get().getText());
    }

    @Test
    void resetOnLeave() {
        final TooltipManager manager = new TooltipManager(DELAY);
        manager.update("tip", true, 10, 20, 1.0d);
        assertTrue(manager.getActiveState().isPresent(), "active after delay");

        manager.update("tip", false, 10, 20, 0.1d);
        assertTrue(manager.getActiveState().isEmpty(), "should hide after leaving hover");
    }
}
