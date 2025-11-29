package com.Kizunad.tinyUI.demo;

import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.theme.Theme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DemoRegistryTest {

    private static final int EXPECTED_DEMO_COUNT = 6;
    private static final int DEMO_INDEX_0 = 0;
    private static final int DEMO_INDEX_1 = 1;
    private static final int DEMO_INDEX_2 = 2;
    private static final int DEMO_INDEX_3 = 3;
    private static final int DEMO_INDEX_4 = 4;
    private static final int DEMO_INDEX_5 = 5;

    @Test
    void providesDefaultDemos() {
        final DemoRegistry registry = new DemoRegistry(Theme.vanilla());
        assertEquals(EXPECTED_DEMO_COUNT, registry.getDemoNames().size());
        assertTrue(registry.create(DEMO_INDEX_0).isPresent());
        assertTrue(registry.create(DEMO_INDEX_1).isPresent());
        assertTrue(registry.create(DEMO_INDEX_2).isPresent());
        assertTrue(registry.create(DEMO_INDEX_3).isPresent());
        assertTrue(registry.create(DEMO_INDEX_4).isPresent());
        assertTrue(registry.create(DEMO_INDEX_5).isPresent());
    }

    @Test
    void buildsRootWithChildren() {
        final DemoRegistry registry = new DemoRegistry(Theme.vanilla());
        final UIRoot root = registry.create(DEMO_INDEX_1).orElseThrow();
        assertTrue(root.getChildren().size() > 0, "demo root should have children");
    }
}
