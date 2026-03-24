package com.Kizunad.guzhenrenext.xianqiao.client.hub.ui;

import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubRoutePolicy;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubStatusEvaluator;
import com.Kizunad.tinyUI.core.UIRenderContext;
import java.util.Objects;

public final class StateBadge extends HubPanel {

    private static final int TEXT_Y_OFFSET = 4;

    private String text = "待核验";

    public StateBadge() {
        super(HubUiTokens.HubTone.STONE);
    }

    public StateBadge(final String text, final HubUiTokens.HubTone tone) {
        this();
        setText(text);
        setTone(tone);
    }

    public static StateBadge forDataClass(final HubSnapshot.DataClass dataClass) {
        final StateBadge badge = new StateBadge();
        badge.applyDataClass(dataClass);
        return badge;
    }

    public static StateBadge forRisk(final HubStatusEvaluator.RiskLevel riskLevel) {
        final StateBadge badge = new StateBadge();
        badge.applyRisk(riskLevel);
        return badge;
    }

    public static StateBadge forTaxonomy(final HubRoutePolicy.HubCardTaxonomy taxonomy) {
        final StateBadge badge = new StateBadge();
        badge.applyTaxonomy(taxonomy);
        return badge;
    }

    public void setText(final String text) {
        this.text = Objects.requireNonNullElse(text, "");
    }

    public String getText() {
        return text;
    }

    public void applyDataClass(final HubSnapshot.DataClass dataClass) {
        setTone(HubUiTokens.toneForDataClass(dataClass));
        setText(HubUiTokens.labelForDataClass(dataClass));
    }

    public void applyRisk(final HubStatusEvaluator.RiskLevel riskLevel) {
        setTone(HubUiTokens.toneForRisk(riskLevel));
        setText(HubUiTokens.labelForRisk(riskLevel));
    }

    public void applyTaxonomy(final HubRoutePolicy.HubCardTaxonomy taxonomy) {
        setTone(HubUiTokens.toneForTaxonomy(taxonomy));
        setText(HubUiTokens.labelForTaxonomy(taxonomy));
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
        final int textWidth = context.measureTextWidth(text);
        final int drawX = getAbsoluteX() + Math.max(0, (getWidth() - textWidth) / 2);
        final int drawY = getAbsoluteY() + TEXT_Y_OFFSET;
        context.drawText(text, drawX, drawY, palette.textColor());
    }
}
