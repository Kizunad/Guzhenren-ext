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
    private static final int EXPECTED_CELL_WIDTH = 47;
    private static final int EXPECTED_CELL_HEIGHT = 27;
    private static final int EXPECTED_C1_X = 2;
    private static final int EXPECTED_C1_Y = 2;
    private static final int EXPECTED_C2_X = 51;
    private static final int EXPECTED_C2_Y = 2;
    private static final int EXPECTED_C3_X = 2;
    private static final int EXPECTED_C3_Y = 31;

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
        assertEquals(EXPECTED_C1_X, c1.getX());
        assertEquals(EXPECTED_C1_Y, c1.getY());
        assertEquals(EXPECTED_CELL_WIDTH, c1.getWidth());
        assertEquals(EXPECTED_CELL_HEIGHT, c1.getHeight());

        assertEquals(EXPECTED_C2_X, c2.getX());
        assertEquals(EXPECTED_C2_Y, c2.getY());

        assertEquals(EXPECTED_C3_X, c3.getX());
        assertEquals(EXPECTED_C3_Y, c3.getY());
    }
}
