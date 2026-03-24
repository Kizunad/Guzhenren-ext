package com.Kizunad.guzhenrenext.xianqiao.client.hub.ui;

import com.Kizunad.tinyUI.core.UIRenderContext;
import java.util.Objects;

public final class PriorityStrip extends HubPanel {

    private static final int TEXT_X_OFFSET = 5;
    private static final int TEXT_Y_OFFSET = 3;

    private String text = "";

    public PriorityStrip() {
        super(HubUiTokens.HubTone.STONE);
    }

    public PriorityStrip(final String text, final HubUiTokens.HubTone tone) {
        this();
        setText(text);
        setTone(tone);
    }

    public void setText(final String text) {
        this.text = Objects.requireNonNullElse(text, "");
    }

    public String getText() {
        return text;
    }

    @Override
    protected void onRender(
        final UIRenderContext context,
        final double mouseX,
        final double mouseY,
        final float partialTicks
    ) {
        HubUiTokens.drawStrip(
            context,
            getAbsoluteX(),
            getAbsoluteY(),
            getWidth(),
            getHeight(),
            getResolvedPalette()
        );
        context.drawText(
            text,
            getAbsoluteX() + TEXT_X_OFFSET,
            getAbsoluteY() + TEXT_Y_OFFSET,
            getResolvedPalette().textColor()
        );
    }
}
