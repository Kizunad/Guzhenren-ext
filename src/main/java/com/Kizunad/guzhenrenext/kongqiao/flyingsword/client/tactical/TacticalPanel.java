package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client.tactical;

import com.Kizunad.tinyUI.core.UIElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import com.Kizunad.tinyUI.theme.Theme;
import java.util.Objects;

public class TacticalPanel extends UIElement {

    private final TacticalTheme theme;

    private TacticalSurface surface = TacticalSurface.SECTION;
    private TacticalTone tone = TacticalTone.INFO;
    private boolean highlighted;

    public TacticalPanel(final TacticalTheme theme) {
        this.theme = Objects.requireNonNull(theme, "theme");
    }

    public void setSurface(final TacticalSurface surface) {
        this.surface = Objects.requireNonNull(surface, "surface");
    }

    public void setTone(final TacticalTone tone) {
        this.tone = Objects.requireNonNull(tone, "tone");
    }

    public void setHighlighted(final boolean highlighted) {
        this.highlighted = highlighted;
    }

    public TacticalSurface getSurface() {
        return surface;
    }

    public TacticalTone getTone() {
        return tone;
    }

    public boolean isHighlighted() {
        return highlighted;
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
        final Theme surfaceTheme = theme.createSurfaceTheme(surface, tone);
        final int borderWidth = theme.hairline();
        final int accentBandHeight = Math.min(height, theme.accentBandHeight());
        final int borderColor = theme.borderColor(surface, tone);

        context.drawRect(x, y, width, height, surfaceTheme.getBackgroundColor());
        if (width > borderWidth * 2 && height > borderWidth * 2) {
            context.drawRect(
                x + borderWidth,
                y + borderWidth,
                width - borderWidth * 2,
                height - borderWidth * 2,
                theme.surfaceOverlayColor(surface)
            );
        }

        context.drawRect(x, y, width, borderWidth, borderColor);
        context.drawRect(x, y + height - borderWidth, width, borderWidth, borderColor);
        context.drawRect(x, y, borderWidth, height, borderColor);
        context.drawRect(x + width - borderWidth, y, borderWidth, height, borderColor);
        context.drawRect(x, y, width, accentBandHeight, theme.accentColor(tone));

        if (highlighted) {
            context.drawRect(
                x,
                y,
                Math.min(width, theme.panelMarkerWidth()),
                height,
                theme.accentColor(tone)
            );
        }
    }
}
