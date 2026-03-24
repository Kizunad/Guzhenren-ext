package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical;

import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import java.util.Objects;
import net.minecraft.network.chat.Component;

public final class TacticalBar extends UIElement {

    private final TacticalTheme theme;

    private Component label = Component.empty();
    private Component valueText = Component.empty();
    private TacticalTone tone = TacticalTone.INFO;
    private float fillRatio;

    public TacticalBar(final TacticalTheme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    public void setLabel(final Component label) {
        this.label = Objects.requireNonNullElse(label, Component.empty());
    }

    public void setValueText(final Component valueText) {
        this.valueText = Objects.requireNonNullElse(valueText, Component.empty());
    }

    public void setTone(final TacticalTone tone) {
        this.tone = Objects.requireNonNullElse(tone, TacticalTone.INFO);
    }

    public void setFillRatio(final float fillRatio) {
        this.fillRatio = clamp(fillRatio);
    }

    public Component getLabel() {
        return label;
    }

    public Component getValueText() {
        return valueText;
    }

    public TacticalTone getTone() {
        return tone;
    }

    public float getFillRatio() {
        return fillRatio;
    }

    @Override
    protected void onRender(
        final UIRenderContext context,
        final double mouseX,
        final double mouseY,
        final float partialTicks
    ) {
        final int width = getWidth();
        final int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        final int x = getAbsoluteX();
        final int y = getAbsoluteY();
        final int padding = theme.panelPadding();
        final int borderWidth = theme.hairline();
        final int labelWidth = context.measureTextWidth(label);
        final int valueWidth = context.measureTextWidth(valueText);
        final boolean hasCaption = labelWidth > 0 || valueWidth > 0;
        final int labelY = y + theme.tightGap();
        final int trackWidth = Math.max(0, width - padding * 2);
        final int trackX = x + padding;

        context.drawRect(x, y, width, height, theme.surfaceColor(TacticalSurface.INSET));
        context.drawRect(x, y, width, borderWidth, theme.quietLineColor());
        context.drawRect(
            x,
            y + height - borderWidth,
            width,
            borderWidth,
            theme.quietLineColor()
        );
        context.drawRect(x, y, borderWidth, height, theme.quietLineColor());
        context.drawRect(x + width - borderWidth, y, borderWidth, height, theme.quietLineColor());

        if (labelWidth > 0) {
            context.drawText(label, trackX, labelY, theme.textDimColor());
        }
        if (valueWidth > 0) {
            final int valueX = x + width - padding - valueWidth;
            context.drawText(valueText, valueX, labelY, theme.badgeTextColor(tone));
        }

        final int trackY = resolveTrackY(y, height, hasCaption, context.getFontLineHeight());
        final int fillWidth = Math.round(trackWidth * fillRatio);
        context.drawRect(trackX, trackY, trackWidth, theme.barHeight(), theme.barTrackColor());
        if (fillWidth > 0) {
            context.drawRect(trackX, trackY, fillWidth, theme.barHeight(), theme.barFillColor(tone));
            context.drawRect(trackX, trackY, fillWidth, borderWidth, theme.barGlowColor(tone));
        }
        context.drawRect(trackX, trackY, trackWidth, borderWidth, theme.quietLineColor());
        context.drawRect(
            trackX,
            trackY + theme.barHeight() - borderWidth,
            trackWidth,
            borderWidth,
            theme.barShadowColor()
        );
    }

    private int resolveTrackY(
        final int y,
        final int height,
        final boolean hasCaption,
        final int fontHeight
    ) {
        if (!hasCaption) {
            return y + Math.max(0, (height - theme.barHeight()) / 2);
        }
        final int contentTop = y + theme.tightGap() + fontHeight + theme.regularGap();
        final int maxTrackY = y + height - theme.tightGap() - theme.barHeight();
        return Math.min(contentTop, maxTrackY);
    }

    private static float clamp(final float value) {
        if (value < 0.0F) {
            return 0.0F;
        }
        if (value > 1.0F) {
            return 1.0F;
        }
        return value;
    }
}
