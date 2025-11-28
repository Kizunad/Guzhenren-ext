package com.Kizunad.tinyUI.layout;

import com.Kizunad.tinyUI.core.UIElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GridLayoutTest {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 60;
    private static final int COLUMNS = 2;
    private static final int GAP = 2;
    private static final int PADDING = 2;

    @Test
    void placesChildrenInGrid() {
        final UIElement container = new UIElement() { };
        container.setFrame(0, 0, WIDTH, HEIGHT);
        final UIElement c1 = new UIElement() { };
        final UIElement c2 = new UIElement() { };
        final UIElement c3 = new UIElement() { };
        container.addChild(c1);
        container.addChild(c2);
        container.addChild(c3);

        final GridLayout layout = new GridLayout(COLUMNS, 0, GAP, PADDING);
        layout.layout(container, null);

        // cell width = (100 - 4 - 2) / 2 = 47, cell height = (60 - 4 - 2) / 2 = 27
        assertEquals(2, c1.getX());
        assertEquals(2, c1.getY());
        assertEquals(47, c1.getWidth());
        assertEquals(27, c1.getHeight());

        assertEquals(51, c2.getX());
        assertEquals(2, c2.getY());

        assertEquals(2, c3.getX());
        assertEquals(31, c3.getY());
    }
}
