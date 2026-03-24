package com.Kizunad.guzhenrenext.xianqiao.client.hub.ui;

import com.Kizunad.tinyUI.core.InteractiveElement;
import com.Kizunad.tinyUI.core.UIRenderContext;
import java.util.Objects;

public class HubPanel extends InteractiveElement {

    private HubUiTokens.HubTone tone = HubUiTokens.HubTone.STONE;

    public HubPanel() {
    }

    public HubPanel(final HubUiTokens.HubTone tone) {
        setTone(tone);
    }

    public final void setTone(final HubUiTokens.HubTone tone) {
        this.tone = Objects.requireNonNull(tone, "tone");
    }

    public final HubUiTokens.HubTone getTone() {
        return tone;
    }

    public final HubUiTokens.HubTonePalette getResolvedPalette() {
        return HubUiTokens.palette(tone);
    }

    protected final void drawPanelChrome(
        final UIRenderContext context,
        final boolean highlighted
    ) {
        HubUiTokens.drawPanel(
            context,
            getAbsoluteX(),
            getAbsoluteY(),
            getWidth(),
            getHeight(),
            getResolvedPalette(),
            highlighted
        );
    }

    @Override
    protected void onRender(
        final UIRenderContext context,
        final double mouseX,
        final double mouseY,
        final float partialTicks
    ) {
        drawPanelChrome(context, false);
    }
}
