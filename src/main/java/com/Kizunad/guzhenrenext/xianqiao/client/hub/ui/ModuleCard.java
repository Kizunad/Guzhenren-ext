package com.Kizunad.guzhenrenext.xianqiao.client.hub.ui;

import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubRoutePolicy;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubStatusEvaluator;
import com.Kizunad.tinyUI.core.UIRenderContext;
import java.util.Objects;

public final class ModuleCard extends HubPanel {

    private static final int PRIORITY_WIDTH = 74;
    private static final int BADGE_WIDTH = 56;
    private static final int BADGE_HEIGHT = HubUiTokens.SMALL_CONTROL_HEIGHT;
    private static final int PILL_WIDTH = 74;
    private static final int PILL_HEIGHT = HubUiTokens.MEDIUM_CONTROL_HEIGHT;
    private static final int CHIP_WIDTH = 86;
    private static final int CHIP_HEIGHT = HubUiTokens.MEDIUM_CONTROL_HEIGHT;
    private static final int TITLE_Y = HubUiTokens.PANEL_PADDING + HubUiTokens.SMALL_CONTROL_HEIGHT + 4;
    private static final int SUMMARY_Y = TITLE_Y + HubUiTokens.TITLE_REGION_HEIGHT + 2;
    private static final int TEXT_X = HubUiTokens.PANEL_PADDING;
    private static final int TITLE_TEXT_Y_OFFSET = 0;
    private static final int SUMMARY_TEXT_Y_OFFSET = 1;
    private static final int FOOTNOTE_TEXT_Y_OFFSET = 2;

    private final String cardId;
    private final PriorityStrip priorityStrip;
    private final StateBadge taxonomyBadge;
    private final StatePill statePill;
    private final RouteChip routeChip;

    private String title;
    private String summary = "";
    private String footnote = "";
    private HubRoutePolicy.HubCardTaxonomy taxonomy = HubRoutePolicy.HubCardTaxonomy.ROUTE_ONLY;
    private Runnable onClick;
    private boolean pressed;

    public ModuleCard(final String cardId, final String title) {
        this.cardId = Objects.requireNonNull(cardId, "cardId");
        if (cardId.isBlank()) {
            throw new IllegalArgumentException("cardId 不能为空");
        }
        this.title = Objects.requireNonNullElse(title, "");
        priorityStrip = new PriorityStrip();
        taxonomyBadge = StateBadge.forTaxonomy(taxonomy);
        statePill = new StatePill();
        routeChip = new RouteChip();

        addChild(priorityStrip);
        addChild(taxonomyBadge);
        addChild(statePill);
        addChild(routeChip);

        priorityStrip.setVisible(false);
        routeChip.setVisible(false);
        setTaxonomy(taxonomy);
        statePill.applyRisk(HubStatusEvaluator.RiskLevel.UNKNOWN);
    }

    public String getCardId() {
        return cardId;
    }

    public void setTitle(final String title) {
        this.title = Objects.requireNonNullElse(title, "");
    }

    public void setSummary(final String summary) {
        this.summary = Objects.requireNonNullElse(summary, "");
    }

    public void setFootnote(final String footnote) {
        this.footnote = Objects.requireNonNullElse(footnote, "");
    }

    public void setOnClick(final Runnable onClick) {
        this.onClick = onClick;
    }

    public void setTaxonomy(final HubRoutePolicy.HubCardTaxonomy taxonomy) {
        this.taxonomy = Objects.requireNonNull(taxonomy, "taxonomy");
        taxonomyBadge.applyTaxonomy(taxonomy);
        setTone(HubUiTokens.toneForTaxonomy(taxonomy));
    }

    public HubRoutePolicy.HubCardTaxonomy getTaxonomy() {
        return taxonomy;
    }

    public PriorityStrip getPriorityStrip() {
        return priorityStrip;
    }

    public StateBadge getTaxonomyBadge() {
        return taxonomyBadge;
    }

    public StatePill getStatePill() {
        return statePill;
    }

    public RouteChip getRouteChip() {
        return routeChip;
    }

    public void applyDataClass(final HubSnapshot.DataClass dataClass) {
        final HubUiTokens.HubTone tone = HubUiTokens.toneForDataClass(dataClass);
        setTone(tone);
        taxonomyBadge.applyDataClass(dataClass);
    }

    public void applyRisk(final HubStatusEvaluator.RiskLevel riskLevel) {
        statePill.applyRisk(riskLevel);
    }

    public void applyRoutePolicy(final HubRoutePolicy.CardRoutePolicy policy) {
        Objects.requireNonNull(policy, "policy");
        setTaxonomy(policy.taxonomy());
        priorityStrip.setVisible(true);
        priorityStrip.setTone(HubUiTokens.toneForRouteKind(policy.routeKind()));
        priorityStrip.setText(HubUiTokens.labelForRouteKind(policy.routeKind()));
        routeChip.setVisible(true);
        routeChip.applyPolicy(policy);
        if (!policy.noticeText().isBlank()) {
            setFootnote(policy.noticeText());
        }
    }

    @Override
    public boolean onMouseClick(final double mouseX, final double mouseY, final int button) {
        if (!isEnabledAndVisible() || !isPointInside(mouseX, mouseY)) {
            return false;
        }
        pressed = true;
        return true;
    }

    @Override
    public boolean onMouseRelease(final double mouseX, final double mouseY, final int button) {
        if (!pressed) {
            return false;
        }
        final boolean inside = isPointInside(mouseX, mouseY);
        pressed = false;
        if (inside && onClick != null && isEnabledAndVisible()) {
            onClick.run();
            return true;
        }
        return inside;
    }

    @Override
    protected void onRender(
        final UIRenderContext context,
        final double mouseX,
        final double mouseY,
        final float partialTicks
    ) {
        drawPanelChrome(context, pressed);
        layoutChildren();
        drawTexts(context);
    }

    private void drawTexts(final UIRenderContext context) {
        final HubUiTokens.HubTonePalette palette = getResolvedPalette();
        context.drawText(
            title,
            getAbsoluteX() + TEXT_X,
            getAbsoluteY() + TITLE_Y + TITLE_TEXT_Y_OFFSET,
            palette.textColor()
        );
        context.drawText(
            summary,
            getAbsoluteX() + TEXT_X,
            getAbsoluteY() + SUMMARY_Y + SUMMARY_TEXT_Y_OFFSET,
            palette.mutedTextColor()
        );
        context.drawText(
            footnote,
            getAbsoluteX() + TEXT_X,
            getAbsoluteY() + resolveFootnoteY() + FOOTNOTE_TEXT_Y_OFFSET,
            palette.lineColor()
        );
    }

    private void layoutChildren() {
        final int innerWidth = Math.max(0, getWidth() - HubUiTokens.PANEL_PADDING * 2);
        final int badgeX = Math.max(
            HubUiTokens.PANEL_PADDING,
            getWidth() - HubUiTokens.PANEL_PADDING - BADGE_WIDTH
        );
        final int bottomY = Math.max(
            HubUiTokens.PANEL_PADDING,
            getHeight() - HubUiTokens.PANEL_PADDING - PILL_HEIGHT
        );
        final int footnoteY = Math.max(
            SUMMARY_Y + HubUiTokens.SUMMARY_REGION_HEIGHT,
            bottomY - HubUiTokens.FOOTNOTE_REGION_HEIGHT - 2
        );

        priorityStrip.setFrame(
            HubUiTokens.PANEL_PADDING,
            HubUiTokens.PANEL_PADDING,
            Math.min(PRIORITY_WIDTH, innerWidth),
            HubUiTokens.SMALL_CONTROL_HEIGHT
        );
        taxonomyBadge.setFrame(badgeX, HubUiTokens.PANEL_PADDING, BADGE_WIDTH, BADGE_HEIGHT);
        statePill.setFrame(
            HubUiTokens.PANEL_PADDING,
            bottomY,
            Math.min(PILL_WIDTH, innerWidth),
            PILL_HEIGHT
        );
        routeChip.setFrame(
            Math.max(HubUiTokens.PANEL_PADDING, getWidth() - HubUiTokens.PANEL_PADDING - CHIP_WIDTH),
            bottomY,
            Math.min(CHIP_WIDTH, innerWidth),
            CHIP_HEIGHT
        );
    }

    private int resolveFootnoteY() {
        final int bottomY = Math.max(
            HubUiTokens.PANEL_PADDING,
            getHeight() - HubUiTokens.PANEL_PADDING - PILL_HEIGHT
        );
        return Math.max(
            SUMMARY_Y + HubUiTokens.SUMMARY_REGION_HEIGHT,
            bottomY - HubUiTokens.FOOTNOTE_REGION_HEIGHT - 2
        );
    }
}
