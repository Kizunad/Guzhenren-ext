package com.Kizunad.guzhenrenext.xianqiao.client.hub.ui;

import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubRoutePolicy;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubStatusEvaluator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ModuleCardTests {

    private static final int CARD_WIDTH = 168;
    private static final int CARD_HEIGHT = 104;
    private static final int INSIDE_X = 10;
    private static final int INSIDE_Y = 10;
    private static final int OUTSIDE_X = 200;
    private static final int OUTSIDE_Y = 200;
    private static final int CHIP_WIDTH = 90;
    private static final int CHIP_HEIGHT = 18;

    @Test
    void moduleCardComposesTaxonomyRiskAndRouteChipForSummaryRouteCard() {
        final ModuleCard card = new ModuleCard(HubRoutePolicy.CARD_RESOURCE, "资源");

        card.setFrame(0, 0, CARD_WIDTH, CARD_HEIGHT);
        card.setSummary("资源为局部采样，需前往分台核验。");
        card.setFootnote("避免把局部 0 误判为健康态。");
        card.applyRoutePolicy(HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_RESOURCE));
        card.applyRisk(HubStatusEvaluator.RiskLevel.CAUTION);

        assertEquals(HubRoutePolicy.HubCardTaxonomy.SUMMARY_ROUTE, card.getTaxonomy());
        assertEquals(HubUiTokens.HubTone.AZURE, card.getTone());
        assertEquals(HubUiTokens.HubTone.AZURE, card.getTaxonomyBadge().getTone());
        assertEquals("摘要路由", card.getTaxonomyBadge().getText());
        assertEquals(HubUiTokens.HubTone.WARN, card.getStatePill().getTone());
        assertEquals("预警", card.getStatePill().getText());
        assertTrue(card.getPriorityStrip().isVisible());
        assertEquals("可直达", card.getPriorityStrip().getText());
        assertTrue(card.getRouteChip().isVisible());
        assertTrue(card.getRouteChip().isActionable());
        assertEquals("前往资源分台", card.getRouteChip().getText());
        assertTrue(card.getRouteChip().getNoticeText().isBlank());
    }

    @Test
    void routeChipMatchesPlaceholderAndDirectScreenPoliciesDeterministically() {
        final RouteChip directChip = new RouteChip();
        final RouteChip placeholderChip = new RouteChip();

        directChip.setFrame(0, 0, CHIP_WIDTH, CHIP_HEIGHT);
        directChip.applyPolicy(HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_RESOURCE));
        placeholderChip.setFrame(0, 0, CHIP_WIDTH, CHIP_HEIGHT);
        placeholderChip.applyPolicy(HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_DAO_MARK));

        assertEquals(HubUiTokens.HubTone.AZURE, directChip.getTone());
        assertTrue(directChip.isActionable());
        assertEquals("前往资源分台", directChip.getText());

        assertEquals(HubUiTokens.HubTone.WARN, placeholderChip.getTone());
        assertFalse(placeholderChip.isActionable());
        assertEquals("待前往道痕子页", placeholderChip.getText());
        assertEquals(HubRoutePolicy.DAOMARK_PLACEHOLDER_NOTICE, placeholderChip.getNoticeText());
    }

    @Test
    void routeChipClickOnlyTriggersWhenActionableAndReleasedInside() {
        final RouteChip chip = new RouteChip();
        final boolean[] clicked = new boolean[1];

        chip.setFrame(0, 0, CHIP_WIDTH, CHIP_HEIGHT);
        chip.applyPolicy(HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_RESOURCE));
        chip.setOnClick(() -> clicked[0] = true);

        assertTrue(chip.onMouseClick(INSIDE_X, INSIDE_Y, 0));
        assertTrue(chip.onMouseRelease(INSIDE_X, INSIDE_Y, 0));
        assertTrue(clicked[0]);

        clicked[0] = false;
        chip.applyPolicy(HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_DAO_MARK));
        assertFalse(chip.onMouseClick(INSIDE_X, INSIDE_Y, 0));
        assertFalse(chip.onMouseRelease(OUTSIDE_X, OUTSIDE_Y, 0));
        assertFalse(clicked[0]);
    }
}
