package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical;

import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import java.util.Objects;
import net.minecraft.network.chat.Component;

public final class TacticalBadge extends UIElement {

    private final TacticalTheme theme;

    private Component label = Component.empty();
    private TacticalTone tone = TacticalTone.NEUTRAL;

    public TacticalBadge(final TacticalTheme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    public TacticalBadge(
        final Component label,
        final TacticalTone tone,
        final TacticalTheme theme
    ) {
        this(theme);
        setLabel(label);
        setTone(tone);
    }

    public void setSpec(final TacticalBadgeSpec spec) {
        final TacticalBadgeSpec safeSpec = Objects.requireNonNull(spec, "spec");
        setLabel(safeSpec.label());
        setTone(safeSpec.tone());
    }

    public void setLabel(final Component label) {
        this.label = Objects.requireNonNullElse(label, Component.empty());
    }

    public void setTone(final TacticalTone tone) {
        this.tone = Objects.requireNonNullElse(tone, TacticalTone.NEUTRAL);
    }

    public Component getLabel() {
        return label;
    }

    public TacticalTone getTone() {
        return tone;
    }

    public int suggestWidth() {
        return theme.estimateCompactWidth(label);
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
        final int borderWidth = theme.hairline();
        final int markerWidth = Math.min(width, theme.badgeMarkerWidth());
        final int textX = x + markerWidth + theme.badgeSidePadding();
        final int textY = y + Math.max(0, (height - context.getFontLineHeight()) / 2);

        context.drawRect(x, y, width, height, theme.badgeBackgroundColor(tone));
        context.drawRect(x, y, width, borderWidth, theme.borderColor(TacticalSurface.INSET, tone));
        context.drawRect(
            x,
            y + height - borderWidth,
            width,
            borderWidth,
            theme.borderColor(TacticalSurface.INSET, tone)
        );
        context.drawRect(x, y, markerWidth, height, theme.accentColor(tone));
        context.drawText(label, textX, textY, theme.badgeTextColor(tone));
    }
}
