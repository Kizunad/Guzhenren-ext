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

    @Test
    void scrollsAndClampsWithinRange() {
        final ScrollContainer container = new ScrollContainer(Theme.vanilla());
        container.setFrame(0, 0, 50, VIEW_HEIGHT);
        final TestElement content = new TestElement();
        content.setFrame(0, 0, 50, CONTENT_HEIGHT);
        container.setContent(content);

        assertTrue(container.onMouseScroll(1, 1, -2.0d), "scroll inside should handle");
        assertEquals(-24, content.getY(), "content should move up with scroll");

        assertTrue(container.onMouseScroll(1, 1, -10.0d), "scroll more down");
        assertEquals(-50, content.getY(), "content should clamp to max scroll");
    }

    @Test
    void scrollIgnoredWhenOutside() {
        final ScrollContainer container = new ScrollContainer(Theme.vanilla());
        container.setFrame(0, 0, 50, VIEW_HEIGHT);
        final TestElement content = new TestElement();
        content.setFrame(0, 0, 50, CONTENT_HEIGHT);
        container.setContent(content);

        assertFalse(container.onMouseScroll(100, 100, -2.0d), "scroll outside should not handle");
        assertEquals(0, content.getY(), "content should remain at origin");
    }

    private static final class TestElement extends UIElement {
        // No extra behavior needed.
    }
}
