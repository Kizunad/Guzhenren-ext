package com.Kizunad.tinyUI.layout;

import com.Kizunad.tinyUI.core.UIElement;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class FlexLayoutTest {

    private static final int CONTAINER_WIDTH = 100;
    private static final int CONTAINER_HEIGHT = 20;
    private static final int GAP = 2;
    private static final int PADDING = 2;
    private static final int CHILD_BASIS = 10;

    @Test
    void rowLayoutDistributesGrowSpace() {
        final UIElement container = new UIElement() { };
        container.setFrame(0, 0, CONTAINER_WIDTH, CONTAINER_HEIGHT);

        final UIElement c1 = makeChild(CHILD_BASIS, CHILD_BASIS);
        final UIElement c2 = makeChild(CHILD_BASIS, CHILD_BASIS);
        final UIElement c3 = makeChild(CHILD_BASIS, CHILD_BASIS);
        container.addChild(c1);
        container.addChild(c2);
        container.addChild(c3);

        final Map<UIElement, FlexParams> params = new HashMap<>();
        params.put(c1, new FlexParams(1, 1, CHILD_BASIS));
        params.put(c2, new FlexParams(1, 1, CHILD_BASIS));
        params.put(c3, new FlexParams(0, 1, CHILD_BASIS));

        final FlexLayout layout = new FlexLayout(FlexLayout.Direction.ROW, GAP, PADDING);
        layout.layout(container, params);

        // availableMain = 100 - 2*2 - 2*(3-1) = 92, bases sum = 30, remaining = 62
        // grow1 items get floor(62/2)=31 each -> 41 width, last remains 10
        assertEquals(2, c1.getX());
        assertEquals(41, c1.getWidth());
        assertEquals(45, c2.getX());
        assertEquals(41, c2.getWidth());
        assertEquals(88, c3.getX());
        assertEquals(10, c3.getWidth());

        final int expectedHeight = 16; // availableCross = 20 - 2*2
        assertEquals(expectedHeight, c1.getHeight());
        assertEquals(expectedHeight, c2.getHeight());
        assertEquals(expectedHeight, c3.getHeight());
    }

    @Test
    void columnLayoutPlacesChildrenVertically() {
        final UIElement container = new UIElement() { };
        container.setFrame(0, 0, CONTAINER_WIDTH, CONTAINER_HEIGHT);
        final UIElement c1 = makeChild(CHILD_BASIS, CHILD_BASIS);
        final UIElement c2 = makeChild(CHILD_BASIS, CHILD_BASIS);
        container.addChild(c1);
        container.addChild(c2);

        final FlexLayout layout = new FlexLayout(FlexLayout.Direction.COLUMN, GAP, PADDING);
        layout.layout(container, null);

        assertEquals(2, c1.getY());
        assertEquals(2 + c1.getHeight() + GAP, c2.getY());
        final int expectedWidth = CONTAINER_WIDTH - PADDING * 2;
        assertEquals(expectedWidth, c1.getWidth());
        assertEquals(expectedWidth, c2.getWidth());
    }

    private static UIElement makeChild(final int width, final int height) {
        final UIElement child = new UIElement() { };
        child.setFrame(0, 0, width, height);
        return child;
    }
}
