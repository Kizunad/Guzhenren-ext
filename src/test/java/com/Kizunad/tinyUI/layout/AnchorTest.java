package com.Kizunad.tinyUI.layout;

import com.Kizunad.tinyUI.core.UIElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AnchorTest {

    private static final int PARENT_WIDTH = 100;
    private static final int PARENT_HEIGHT = 80;
    private static final int WIDTH = 10;
    private static final int HEIGHT = 10;
    private static final int OFFSET = -5;
    private static final int EXPECTED_CENTER_X = 45;
    private static final int EXPECTED_CENTER_Y = 35;
    private static final int EXPECTED_RIGHT_BOTTOM_X = 85;
    private static final int EXPECTED_RIGHT_BOTTOM_Y = 65;

    @Test
    void centersElement() {
        final UIElement element = new UIElement() { };
        final Anchor.Spec spec = new Anchor.Spec(
                WIDTH, HEIGHT, Anchor.Horizontal.CENTER, Anchor.Vertical.CENTER, 0, 0
        );
        Anchor.apply(element, PARENT_WIDTH, PARENT_HEIGHT, spec);
        assertEquals(EXPECTED_CENTER_X, element.getX());
        assertEquals(EXPECTED_CENTER_Y, element.getY());
    }

    @Test
    void anchorsRightBottomWithOffset() {
        final UIElement element = new UIElement() { };
        final Anchor.Spec spec = new Anchor.Spec(
                WIDTH, HEIGHT, Anchor.Horizontal.RIGHT, Anchor.Vertical.BOTTOM, OFFSET, OFFSET
        );
        Anchor.apply(element, PARENT_WIDTH, PARENT_HEIGHT, spec);
        assertEquals(EXPECTED_RIGHT_BOTTOM_X, element.getX());
        assertEquals(EXPECTED_RIGHT_BOTTOM_Y, element.getY());
    }
}
