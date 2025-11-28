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

    @Test
    void centersElement() {
        final UIElement element = new UIElement() { };
        final Anchor.Spec spec = new Anchor.Spec(
                WIDTH, HEIGHT, Anchor.Horizontal.CENTER, Anchor.Vertical.CENTER, 0, 0
        );
        Anchor.apply(element, PARENT_WIDTH, PARENT_HEIGHT, spec);
        assertEquals(45, element.getX());
        assertEquals(35, element.getY());
    }

    @Test
    void anchorsRightBottomWithOffset() {
        final UIElement element = new UIElement() { };
        final Anchor.Spec spec = new Anchor.Spec(
                WIDTH, HEIGHT, Anchor.Horizontal.RIGHT, Anchor.Vertical.BOTTOM, OFFSET, OFFSET
        );
        Anchor.apply(element, PARENT_WIDTH, PARENT_HEIGHT, spec);
        assertEquals(85, element.getX());
        assertEquals(65, element.getY());
    }
}
