package com.Kizunad.guzhenrenext.xianqiao.client.hub.ui;

import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubStatusEvaluator;
import com.Kizunad.tinyUI.core.UIRenderContext;
import java.util.Objects;

public final class StatePill extends HubPanel {

    private static final int LEADING_MARKER_WIDTH = 4;
    private static final int TEXT_LEFT_PADDING = 10;
    private static final int TEXT_Y_OFFSET = 4;

    private String text = "待核验";

    public StatePill() {
        super(HubUiTokens.HubTone.WARN);
    }

    public StatePill(final String text, final HubUiTokens.HubTone tone) {
        this();
        setText(text);
        setTone(tone);
    }

    public static StatePill forRisk(final HubStatusEvaluator.RiskLevel riskLevel) {
        final StatePill pill = new StatePill();
        pill.applyRisk(riskLevel);
        return pill;
    }

    public void setText(final String text) {
        this.text = Objects.requireNonNullElse(text, "");
    }

    public String getText() {
        return text;
    }

    public void applyRisk(final HubStatusEvaluator.RiskLevel riskLevel) {
        setTone(HubUiTokens.toneForRisk(riskLevel));
        setText(HubUiTokens.labelForRisk(riskLevel));
    }

    public void applyDataClass(final HubSnapshot.DataClass dataClass) {
        setTone(HubUiTokens.toneForDataClass(dataClass));
        setText(HubUiTokens.labelForDataClass(dataClass));
    }

    @Override
    protected void onRender(
        final UIRenderContext context,
        final double mouseX,
        final double mouseY,
        final float partialTicks
    ) {
        drawPanelChrome(context, false);
        final HubUiTokens.HubTonePalette palette = getResolvedPalette();
        context.drawRect(
            getAbsoluteX() + HubUiTokens.BORDER_THICKNESS,
            getAbsoluteY() + HubUiTokens.BORDER_THICKNESS,
            LEADING_MARKER_WIDTH,
            Math.max(0, getHeight() - HubUiTokens.BORDER_THICKNESS * 2),
            palette.accentColor()
        );
        context.drawText(
            text,
            getAbsoluteX() + TEXT_LEFT_PADDING,
            getAbsoluteY() + TEXT_Y_OFFSET,
            palette.textColor()
        );
    }
}
