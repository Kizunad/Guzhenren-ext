package com.Kizunad.guzhenrenext.client.gui;

import com.Kizunad.guzhenrenext.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class GuzhenrenConfigScreen extends Screen {

    private static final int SLIDER_WIDTH = 200;
    private static final int SLIDER_HEIGHT = 20;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_Y_OFFSET = 20;
    private static final int TITLE_Y_POS = 20;
    private static final int TITLE_COLOR = 0xFFFFFF;
    private static final int HALF_SLIDER_WIDTH = SLIDER_WIDTH / 2;

    private final Screen lastScreen;
    private AbstractSliderButton scaleSlider;

    public GuzhenrenConfigScreen(Screen lastScreen) {
        super(Component.translatable("guzhenrenext.config.title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        double currentScale = ClientConfig.INSTANCE.kongQiaoUiScale.get();
        double min = ClientConfig.MIN_UI_SCALE;
        double max = ClientConfig.MAX_UI_SCALE;
        double range = max - min;
        double initialValue = (currentScale - min) / range;

        // Slider for UI Scale
        this.scaleSlider = new AbstractSliderButton(
            centerX - HALF_SLIDER_WIDTH, centerY - BUTTON_Y_OFFSET, SLIDER_WIDTH, SLIDER_HEIGHT,
            Component.translatable("guzhenrenext.config.scale")
                .append(": " + String.format("%.2f", currentScale)),
            initialValue
        ) {
            @Override
            protected void updateMessage() {
                double val = min + (this.value * range);
                this.setMessage(Component.translatable("guzhenrenext.config.scale")
                    .append(": " + String.format("%.2f", val)));
            }

            @Override
            protected void applyValue() {
                double val = min + (this.value * range);
                ClientConfig.INSTANCE.kongQiaoUiScale.set(val);
                ClientConfig.SPEC.save();
            }
        };

        this.addRenderableWidget(this.scaleSlider);

        // Done Button
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.minecraft.setScreen(this.lastScreen);
        })
        .bounds(centerX - HALF_SLIDER_WIDTH, centerY + BUTTON_Y_OFFSET, SLIDER_WIDTH, BUTTON_HEIGHT)
        .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, TITLE_Y_POS, TITLE_COLOR);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }
}