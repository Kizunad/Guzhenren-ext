package com.Kizunad.tinyUI.demo;

import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.theme.Theme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DemoRegistryTest {

    @Test
    void providesDefaultDemos() {
        final DemoRegistry registry = new DemoRegistry(Theme.vanilla());
        assertEquals(3, registry.getDemoNames().size());
        assertTrue(registry.create(0).isPresent());
        assertTrue(registry.create(1).isPresent());
        assertTrue(registry.create(2).isPresent());
    }

    @Test
    void buildsRootWithChildren() {
        final DemoRegistry registry = new DemoRegistry(Theme.vanilla());
        final UIRoot root = registry.create(1).orElseThrow();
        assertTrue(root.getChildren().size() > 0, "demo root should have children");
    }
}
