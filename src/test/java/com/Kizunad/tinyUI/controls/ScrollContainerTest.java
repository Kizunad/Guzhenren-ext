package com.Kizunad.tinyUI.controls;

import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.theme.Theme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ScrollContainerTest {

    private static final int VIEW_HEIGHT = 50;
    private static final int CONTENT_HEIGHT = 100;
    private static final int CONTAINER_WIDTH = 50;
    private static final double SCROLL_AMOUNT_SMALL = -2.0d;
    private static final double SCROLL_AMOUNT_LARGE = -10.0d;
    private static final int EXPECTED_OFFSET_SMALL = -24;
    private static final int EXPECTED_OFFSET_MAX = -50;
    private static final int MOUSE_X_INSIDE = 1;
    private static final int MOUSE_Y_INSIDE = 1;
    private static final int MOUSE_X_OUTSIDE = 100;
    private static final int MOUSE_Y_OUTSIDE = 100;

    @Test
    void scrollsAndClampsWithinRange() {
        final ScrollContainer container = new ScrollContainer(Theme.vanilla());
        container.setFrame(0, 0, CONTAINER_WIDTH, VIEW_HEIGHT);
        final TestElement content = new TestElement();
        content.setFrame(0, 0, CONTAINER_WIDTH, CONTENT_HEIGHT);
        container.setContent(content);

        assertTrue(container.onMouseScroll(MOUSE_X_INSIDE, MOUSE_Y_INSIDE, SCROLL_AMOUNT_SMALL),
                "scroll inside should handle");
        assertEquals(EXPECTED_OFFSET_SMALL, content.getY(), "content should move up with scroll");

        assertTrue(container.onMouseScroll(MOUSE_X_INSIDE, MOUSE_Y_INSIDE, SCROLL_AMOUNT_LARGE),
                "scroll more down");
        assertEquals(EXPECTED_OFFSET_MAX, content.getY(), "content should clamp to max scroll");
    }

    @Test
    void scrollIgnoredWhenOutside() {
        final ScrollContainer container = new ScrollContainer(Theme.vanilla());
        container.setFrame(0, 0, CONTAINER_WIDTH, VIEW_HEIGHT);
        final TestElement content = new TestElement();
        content.setFrame(0, 0, CONTAINER_WIDTH, CONTENT_HEIGHT);
        container.setContent(content);

        assertFalse(container.onMouseScroll(MOUSE_X_OUTSIDE, MOUSE_Y_OUTSIDE, SCROLL_AMOUNT_SMALL),
                "scroll outside should not handle");
        assertEquals(0, content.getY(), "content should remain at origin");
    }

    private static final class TestElement extends UIElement {
        // No extra behavior needed.
    }
}
