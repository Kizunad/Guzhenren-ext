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

    private static final int SETTINGS_VIEWPORT_W = 240;
    private static final int SETTINGS_VIEWPORT_H = 180;
    private static final int SETTINGS_CONTENT_H = 300;
    private static final int SETTINGS_ITEM_W = 200;
    private static final int SETTINGS_ITEM_H_SMALL = 16;
    private static final int SETTINGS_ITEM_H_MED = 20;
    private static final int SETTINGS_ITEM_H_LARGE = 24;
    private static final int SETTINGS_BTN_W = 100;
    private static final int SETTINGS_GAP = 10;

    private static final int SHOP_VIEWPORT_W = 300;
    private static final int SHOP_VIEWPORT_H = 200;
    private static final int SHOP_GRID_W = 180;
    private static final int SHOP_GRID_CONTENT_H = 300;
    private static final int SHOP_ITEM_SIZE = 50;
    private static final int SHOP_DETAILS_W = 110;
    private static final int SHOP_BTN_W = 100;
    private static final int SHOP_DETAILS_CONTENT_W = 100;
    private static final int SHOP_DETAILS_ITEM_NAME_H = 20;
    private static final int SHOP_DETAILS_ITEM_DESC_H = 60;
    private static final int SHOP_BTN_H = 24;
    private static final int SHOP_MODAL_CONTENT_X = 50;
    private static final int SHOP_ITEM_COUNT = 20;
    private static final int SHOP_ITEM_PRICE_MULTIPLIER = 10;

    private static final int DEMO_ID_LAYOUT = 0;
    private static final int DEMO_ID_HOTKEY = 1;
    private static final int DEMO_ID_SLOT = 2;
    private static final int DEMO_ID_SETTINGS = 3;
    private static final int DEMO_ID_SHOP = 4;
    private static final int SHOP_MODAL_CONTENT_Y = 50;
    private static final int SHOP_MODAL_CONTENT_W = 200;
    private static final int SHOP_MODAL_CONTENT_H = 100;
    private static final int SHOP_DIALOG_X = 75;
    private static final int SHOP_DIALOG_Y = 70;
    private static final int SHOP_DIALOG_W = 150;
    private static final int SHOP_DIALOG_H = 80;
    private static final int SHOP_DIALOG_BTN_W = 60;
    private static final int SHOP_DIALOG_BTN_H = 24;
    private static final int SHOP_DIALOG_BG_COLOR = 0xFF444444;
    private static final int SHOP_DIALOG_MSG_X = 10;
    private static final int SHOP_DIALOG_MSG_Y = 10;
    private static final int SHOP_DIALOG_MSG_W = 130;
    private static final int SHOP_DIALOG_MSG_H = 20;
    private static final int SHOP_DIALOG_BTN_Y = 40;
    private static final int SHOP_DIALOG_BTN_NO_X = 80;

    private static final int LAYOUT_DEMO_W = 200;
    private static final int LAYOUT_DEMO_H = 120;
    private static final int LAYOUT_DEMO_GAP = 4;

    private static final int HOTKEY_DEMO_W = 220;
    private static final int HOTKEY_DEMO_H = 140;
    private static final int HOTKEY_TITLE_X = 10;
    private static final int HOTKEY_TITLE_Y = 10;
    private static final int HOTKEY_TITLE_W = 200;
    private static final int HOTKEY_TITLE_H = 16;
    private static final int HOTKEY_CAPTURE_X = 10;
    private static final int HOTKEY_CAPTURE_Y = 32;
    private static final int HOTKEY_CAPTURE_W = 120;
    private static final int HOTKEY_CAPTURE_H = 24;
    private static final int HOTKEY_INPUT_X = 10;
    private static final int HOTKEY_INPUT_Y = 64;
    private static final int HOTKEY_INPUT_W = 120;
    private static final int HOTKEY_INPUT_H = 20;
    private static final int HOTKEY_SAVE_X = 140;
    private static final int HOTKEY_SAVE_Y = 32;
    private static final int HOTKEY_SAVE_W = 60;
    private static final int HOTKEY_SAVE_H = 24;

    private static final int SLOT_DEMO_W = 180;
    private static final int SLOT_DEMO_H = 180;
    private static final int SLOT_DEMO_CONTENT_H = 240;
    private static final int SLOT_GRID_COLS = 5;
    private static final int SLOT_GRID_GAP_X = 2;
    private static final int SLOT_GRID_GAP_Y = 4;
    private static final int SLOT_COUNT = 15;
    private static final int SLOT_SIZE = 30;

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
        names.put(DEMO_ID_LAYOUT, "Layout + Buttons");
        names.put(DEMO_ID_HOTKEY, "Hotkey Config");
        names.put(DEMO_ID_SLOT, "Slot Grid");
        names.put(DEMO_ID_SETTINGS, "Settings Panel");
        names.put(DEMO_ID_SHOP, "Shop Interface");
        return Collections.unmodifiableMap(names);
    }

    private void registerDefault(final Theme theme) {
        demos.put(DEMO_ID_LAYOUT, () -> buildLayoutDemo(theme));
        demos.put(DEMO_ID_HOTKEY, () -> buildHotkeyDemo(theme));
        demos.put(DEMO_ID_SLOT, () -> buildSlotGridDemo(theme));
        demos.put(DEMO_ID_SETTINGS, () -> buildSettingsDemo(theme));
        demos.put(DEMO_ID_SHOP, () -> buildShopDemo(theme));
    }

    private static UIRoot buildLayoutDemo(final Theme theme) {
        final UIRoot root = new UIRoot();
        root.setViewport(LAYOUT_DEMO_W, LAYOUT_DEMO_H);
        final FlexLayout layout = new FlexLayout(FlexLayout.Direction.ROW, LAYOUT_DEMO_GAP, LAYOUT_DEMO_GAP);

        final UIElement container = new UIElement() { };
        container.setFrame(0, 0, LAYOUT_DEMO_W, LAYOUT_DEMO_H);
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
        root.setViewport(HOTKEY_DEMO_W, HOTKEY_DEMO_H);

        final UIElement container = new UIElement() { };
        container.setFrame(0, 0, HOTKEY_DEMO_W, HOTKEY_DEMO_H);
        root.addChild(container);

        final Label title = new Label("Hotkey Capture", theme);
        title.setFrame(HOTKEY_TITLE_X, HOTKEY_TITLE_Y, HOTKEY_TITLE_W, HOTKEY_TITLE_H);

        final HotkeyCapture capture = new HotkeyCapture(theme);
        capture.setFrame(HOTKEY_CAPTURE_X, HOTKEY_CAPTURE_Y, HOTKEY_CAPTURE_W, HOTKEY_CAPTURE_H);

        final TextInput nameInput = new TextInput(theme);
        nameInput.setFrame(HOTKEY_INPUT_X, HOTKEY_INPUT_Y, HOTKEY_INPUT_W, HOTKEY_INPUT_H);
        nameInput.setText("Action name");

        final Button save = new Button("Save", theme);
        save.setFrame(HOTKEY_SAVE_X, HOTKEY_SAVE_Y, HOTKEY_SAVE_W, HOTKEY_SAVE_H);

        container.addChild(title);
        container.addChild(capture);
        container.addChild(nameInput);
        container.addChild(save);
        return root;
    }

    private static UIRoot buildSlotGridDemo(final Theme theme) {
        final UIRoot root = new UIRoot();
        root.setViewport(SLOT_DEMO_W, SLOT_DEMO_H);

        final ScrollContainer scroll = new ScrollContainer(theme);
        scroll.setFrame(0, 0, SLOT_DEMO_W, SLOT_DEMO_H);
        root.addChild(scroll);

        final UIElement gridContainer = new UIElement() { };
        gridContainer.setFrame(0, 0, SLOT_DEMO_W, SLOT_DEMO_CONTENT_H);
        scroll.setContent(gridContainer);

        final GridLayout gridLayout = new GridLayout(SLOT_GRID_COLS, 0, SLOT_GRID_GAP_X, SLOT_GRID_GAP_Y);
        for (int i = 0; i < SLOT_COUNT; i++) {
            final Button slot = new Button("S" + i, theme);
            slot.setFrame(0, 0, SLOT_SIZE, SLOT_SIZE);
            gridContainer.addChild(slot);
        }
        gridLayout.layout(gridContainer, null);
        return root;
    }

    private static UIRoot buildSettingsDemo(final Theme theme) {
        final UIRoot root = new UIRoot();
        root.setViewport(SETTINGS_VIEWPORT_W, SETTINGS_VIEWPORT_H);

        final ScrollContainer scroll = new ScrollContainer(theme);
        scroll.setFrame(0, 0, SETTINGS_VIEWPORT_W, SETTINGS_VIEWPORT_H);
        root.addChild(scroll);

        final UIElement content = new UIElement() { };
        content.setFrame(0, 0, SETTINGS_VIEWPORT_W, SETTINGS_CONTENT_H); // Larger height for scrolling
        scroll.setContent(content);

        final FlexLayout layout = new FlexLayout(FlexLayout.Direction.COLUMN, SETTINGS_GAP, SETTINGS_GAP);

        // Title
        final Label title = new Label("Settings", theme);
        title.setFrame(0, 0, SETTINGS_ITEM_W, SETTINGS_ITEM_H_MED);
        content.addChild(title);

        // Volume Setting
        final Label volumeLabel = new Label("Master Volume", theme);
        volumeLabel.setFrame(0, 0, SETTINGS_ITEM_W, SETTINGS_ITEM_H_SMALL);
        content.addChild(volumeLabel);

        final TextInput volumeInput = new TextInput(theme);
        volumeInput.setFrame(0, 0, SETTINGS_ITEM_W, SETTINGS_ITEM_H_MED);
        volumeInput.setText("100");
        content.addChild(volumeInput);

        // Graphics Setting
        final com.Kizunad.tinyUI.controls.ToggleButton graphicsToggle =
                new com.Kizunad.tinyUI.controls.ToggleButton("Fancy Graphics", theme);
        graphicsToggle.setFrame(0, 0, SETTINGS_ITEM_W, SETTINGS_ITEM_H_LARGE);
        content.addChild(graphicsToggle);

        // Audio Setting
        final com.Kizunad.tinyUI.controls.ToggleButton audioToggle =
                new com.Kizunad.tinyUI.controls.ToggleButton("Mute Music", theme);
        audioToggle.setFrame(0, 0, SETTINGS_ITEM_W, SETTINGS_ITEM_H_LARGE);
        content.addChild(audioToggle);

        // Save Button
        final Button saveBtn = new Button("Save Changes", theme);
        saveBtn.setFrame(0, 0, SETTINGS_BTN_W, SETTINGS_ITEM_H_LARGE);
        content.addChild(saveBtn);

        // Layout params
        final Map<UIElement, com.Kizunad.tinyUI.layout.FlexParams> params = new HashMap<>();
        params.put(title, new com.Kizunad.tinyUI.layout.FlexParams(0, 0, 0));
        params.put(volumeLabel, new com.Kizunad.tinyUI.layout.FlexParams(0, 0, 0));
        params.put(volumeInput, new com.Kizunad.tinyUI.layout.FlexParams(0, 0, 0));
        params.put(graphicsToggle, new com.Kizunad.tinyUI.layout.FlexParams(0, 0, 0));
        params.put(audioToggle, new com.Kizunad.tinyUI.layout.FlexParams(0, 0, 0));
        params.put(saveBtn, new com.Kizunad.tinyUI.layout.FlexParams(0, 0, 0));

        layout.layout(content, params);

        return root;
    }

    private static UIRoot buildShopDemo(final Theme theme) {
        final UIRoot root = new UIRoot();
        root.setViewport(SHOP_VIEWPORT_W, SHOP_VIEWPORT_H);

        // Split view: Left Grid, Right Details
        final FlexLayout mainLayout = new FlexLayout(FlexLayout.Direction.ROW, 5, 5);
        final UIElement mainContainer = new UIElement() { };
        mainContainer.setFrame(0, 0, SHOP_VIEWPORT_W, SHOP_VIEWPORT_H);
        root.addChild(mainContainer);

        // Right: Details Pane
        final UIElement detailsPane = new UIElement() { };
        detailsPane.setFrame(0, 0, SHOP_DETAILS_W, SHOP_VIEWPORT_H);
        mainContainer.addChild(detailsPane);

        final FlexLayout detailsLayout = new FlexLayout(FlexLayout.Direction.COLUMN, 5, 5);
        
        final Label itemName = new Label("Selected: None", theme);
        itemName.setFrame(0, 0, SHOP_DETAILS_CONTENT_W, SHOP_DETAILS_ITEM_NAME_H);
        detailsPane.addChild(itemName);

        final Label itemDesc = new Label("Description...", theme);
        itemDesc.setFrame(0, 0, SHOP_DETAILS_CONTENT_W, SHOP_DETAILS_ITEM_DESC_H);
        detailsPane.addChild(itemDesc);

        final Button buyBtn = new Button("Buy", theme);
        buyBtn.setFrame(0, 0, SHOP_BTN_W, SHOP_BTN_H);
        detailsPane.addChild(buyBtn);

        // Left: Item Grid
        final ScrollContainer gridScroll = new ScrollContainer(theme);
        gridScroll.setFrame(0, 0, SHOP_GRID_W, SHOP_VIEWPORT_H);
        mainContainer.addChild(gridScroll);

        final UIElement gridContent = new UIElement() { };
        gridContent.setFrame(0, 0, SHOP_GRID_W, SHOP_GRID_CONTENT_H);
        gridScroll.setContent(gridContent);

        final GridLayout gridLayout = new GridLayout(3, 0, 5, 5);
        for (int i = 0; i < SHOP_ITEM_COUNT; i++) {
            final int index = i;
            final Button item = new Button("Item " + i, theme);
            item.setFrame(0, 0, SHOP_ITEM_SIZE, SHOP_ITEM_SIZE);
            item.setOnClick(() -> {
                itemName.setText("Selected: Item " + index);
                itemDesc.setText("Description for Item " + index + "\nPrice: "
                        + (index + 1) * SHOP_ITEM_PRICE_MULTIPLIER + " Gold");
            });
            gridContent.addChild(item);
        }
        gridLayout.layout(gridContent, null);

        // Layout Main Container
        final Map<UIElement, com.Kizunad.tinyUI.layout.FlexParams> mainParams = new HashMap<>();
        mainParams.put(gridScroll, new com.Kizunad.tinyUI.layout.FlexParams(2, 1, 0)); // Grow 2
        mainParams.put(detailsPane, new com.Kizunad.tinyUI.layout.FlexParams(1, 1, 0)); // Grow 1
        mainLayout.layout(mainContainer, mainParams);

        // Layout Details Pane
        final Map<UIElement, com.Kizunad.tinyUI.layout.FlexParams> detailsParams = new HashMap<>();
        detailsParams.put(itemName, new com.Kizunad.tinyUI.layout.FlexParams(0, 0, 0));
        detailsParams.put(itemDesc, new com.Kizunad.tinyUI.layout.FlexParams(1, 1, 0));
        detailsParams.put(buyBtn, new com.Kizunad.tinyUI.layout.FlexParams(0, 0, 0));
        detailsLayout.layout(detailsPane, detailsParams);

        // Modal Logic
        final com.Kizunad.tinyUI.controls.ModalOverlay modal = new com.Kizunad.tinyUI.controls.ModalOverlay(theme);
        modal.setFrame(0, 0, SHOP_VIEWPORT_W, SHOP_VIEWPORT_H);
        modal.setVisible(false);
        root.addChild(modal); // Add on top

        // Modal Content
        // We can't easily layer inside setContent unless we have a Panel.
        // Let's just use the modal overlay's ability to block input and show a simple "Dialog" structure.
        // For this demo, let's make the content a simple interactive element that draws a box.
        
        final com.Kizunad.tinyUI.core.InteractiveElement dialog = new com.Kizunad.tinyUI.core.InteractiveElement() {
            @Override
            protected void onRender(com.Kizunad.tinyUI.core.UIRenderContext context,
                                    double mouseX, double mouseY, float partialTicks) {
                context.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight(), SHOP_DIALOG_BG_COLOR);
                context.drawRect(getAbsoluteX(), getAbsoluteY(), getWidth(), 2, theme.getAccentColor());
                super.onRender(context, mouseX, mouseY, partialTicks);
            }
        };
        dialog.setFrame(SHOP_DIALOG_X, SHOP_DIALOG_Y, SHOP_DIALOG_W, SHOP_DIALOG_H);
        
        final Label msg = new Label("Confirm Purchase?", theme);
        msg.setFrame(SHOP_DIALOG_MSG_X, SHOP_DIALOG_MSG_Y, SHOP_DIALOG_MSG_W, SHOP_DIALOG_MSG_H);
        dialog.addChild(msg);
        
        final Button yesBtn = new Button("Yes", theme);
        yesBtn.setFrame(SHOP_DIALOG_MSG_X, SHOP_DIALOG_BTN_Y, SHOP_DIALOG_BTN_W, SHOP_DIALOG_BTN_H);
        yesBtn.setOnClick(() -> {
            modal.setVisible(false);
            itemName.setText("Purchased!");
        });
        dialog.addChild(yesBtn);
        
        final Button noBtn = new Button("No", theme);
        noBtn.setFrame(SHOP_DIALOG_BTN_NO_X, SHOP_DIALOG_BTN_Y, SHOP_DIALOG_BTN_W, SHOP_DIALOG_BTN_H);
        noBtn.setOnClick(() -> modal.setVisible(false));
        dialog.addChild(noBtn);
        
        modal.setContent(dialog);

        buyBtn.setOnClick(() -> {
            modal.setVisible(true);
        });

        return root;
    }
}
