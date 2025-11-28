package com.Kizunad.tinyUI.demo;

import com.Kizunad.tinyUI.controls.Button;
import com.Kizunad.tinyUI.controls.HotkeyCapture;
import com.Kizunad.tinyUI.controls.Label;
import com.Kizunad.tinyUI.controls.ScrollContainer;
import com.Kizunad.tinyUI.controls.TextInput;
import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRoot;
import com.Kizunad.tinyUI.layout.FlexLayout;
import com.Kizunad.tinyUI.layout.GridLayout;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 简易 demo 注册表，提供编号 0/1/2 的示例页面构建器。
 * 不依赖 MC，仅用于逻辑组装；渲染需由上层适配层接入。
 */
public final class DemoRegistry {

    private final Map<Integer, Supplier<UIRoot>> demos = new HashMap<>();

    public DemoRegistry(final Theme theme) {
        registerDefault(theme);
    }

    public Optional<UIRoot> create(final int id) {
        final Supplier<UIRoot> supplier = demos.get(id);
        if (supplier == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(supplier.get());
    }

    public Map<Integer, String> getDemoNames() {
        final Map<Integer, String> names = new HashMap<>();
        names.put(0, "Layout + Buttons");
        names.put(1, "Hotkey Config");
        names.put(2, "Slot Grid");
        return Collections.unmodifiableMap(names);
    }

    private void registerDefault(final Theme theme) {
        demos.put(0, () -> buildLayoutDemo(theme));
        demos.put(1, () -> buildHotkeyDemo(theme));
        demos.put(2, () -> buildSlotGridDemo(theme));
    }

    private static UIRoot buildLayoutDemo(final Theme theme) {
        final UIRoot root = new UIRoot();
        root.setViewport(200, 120);
        final FlexLayout layout = new FlexLayout(FlexLayout.Direction.ROW, 4, 4);

        final UIElement container = new UIElement() { };
        container.setFrame(0, 0, 200, 120);
        root.addChild(container);

        final Button left = new Button("Left", theme);
        final Button center = new Button("Center", theme);
        final Button right = new Button("Right", theme);
        container.addChild(left);
        container.addChild(center);
        container.addChild(right);

        final Map<UIElement, com.Kizunad.tinyUI.layout.FlexParams> params = new HashMap<>();
        params.put(left, new com.Kizunad.tinyUI.layout.FlexParams(1, 1, 0));
        params.put(center, new com.Kizunad.tinyUI.layout.FlexParams(1, 1, 0));
        params.put(right, new com.Kizunad.tinyUI.layout.FlexParams(1, 1, 0));
        layout.layout(container, params);
        return root;
    }

    private static UIRoot buildHotkeyDemo(final Theme theme) {
        final UIRoot root = new UIRoot();
        root.setViewport(220, 140);

        final UIElement container = new UIElement() { };
        container.setFrame(0, 0, 220, 140);
        root.addChild(container);

        final Label title = new Label("Hotkey Capture", theme);
        title.setFrame(10, 10, 200, 16);

        final HotkeyCapture capture = new HotkeyCapture(theme);
        capture.setFrame(10, 32, 120, 24);

        final TextInput nameInput = new TextInput(theme);
        nameInput.setFrame(10, 64, 120, 20);
        nameInput.setText("Action name");

        final Button save = new Button("Save", theme);
        save.setFrame(140, 32, 60, 24);

        container.addChild(title);
        container.addChild(capture);
        container.addChild(nameInput);
        container.addChild(save);
        return root;
    }

    private static UIRoot buildSlotGridDemo(final Theme theme) {
        final UIRoot root = new UIRoot();
        root.setViewport(180, 180);

        final ScrollContainer scroll = new ScrollContainer(theme);
        scroll.setFrame(0, 0, 180, 180);
        root.addChild(scroll);

        final UIElement gridContainer = new UIElement() { };
        gridContainer.setFrame(0, 0, 180, 240);
        scroll.setContent(gridContainer);

        final GridLayout gridLayout = new GridLayout(5, 0, 2, 4);
        for (int i = 0; i < 15; i++) {
            final Button slot = new Button("S" + i, theme);
            slot.setFrame(0, 0, 30, 30);
            gridContainer.addChild(slot);
        }
        gridLayout.layout(gridContainer, null);
        return root;
    }
}
