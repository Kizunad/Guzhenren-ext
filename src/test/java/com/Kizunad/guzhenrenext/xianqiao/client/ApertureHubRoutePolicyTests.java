package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubRoutePolicy;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubRoutePolicy.CardRoutePolicy;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubCardRegistry;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApertureHubRoutePolicyTests {

    @Test
    void routePolicyCoversEveryFrozenModuleCardWithExplicitTaxonomy() {
        List<String> expectedCardIds = HubCardRegistry.cardIds();
        List<String> actualCardIds = HubRoutePolicy.orderedPolicies().stream()
            .map(CardRoutePolicy::cardId)
            .toList();
        Map<String, String> expectedTaxonomyByCardId = HubCardRegistry.cards()
            .stream()
            .collect(
                Collectors.toMap(
                    HubCardRegistry.CardDefinition::cardId,
                    contract -> contract.taxonomy().serializedName()
                )
            );
        Map<String, String> actualTaxonomyByCardId = HubRoutePolicy.orderedPolicies().stream()
            .collect(Collectors.toMap(CardRoutePolicy::cardId, policy -> policy.taxonomy().serializedName()));

        assertEquals(expectedCardIds, actualCardIds);
        assertEquals(expectedTaxonomyByCardId, actualTaxonomyByCardId);
    }

    @Test
    void routePolicyOnlyLaunchesApprovedChildScreens() {
        assertEquals(
            List.of(
                HubRoutePolicy.LAND_SPIRIT_SCREEN_CLASS_NAME,
                HubRoutePolicy.RESOURCE_CONTROLLER_SCREEN_CLASS_NAME,
                HubRoutePolicy.ALCHEMY_FURNACE_SCREEN_CLASS_NAME,
                HubRoutePolicy.STORAGE_GU_SCREEN_CLASS_NAME,
                HubRoutePolicy.CLUSTER_NPC_SCREEN_CLASS_NAME
            ),
            HubRoutePolicy.approvedDirectScreenClassNames()
        );

        assertTrue(HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_LAND_SPIRIT).launchesScreen());
        assertTrue(HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_RESOURCE).launchesScreen());
        assertTrue(HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_ALCHEMY).launchesScreen());
        assertTrue(HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_STORAGE).launchesScreen());
        assertTrue(HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_CLUSTER).launchesScreen());
        assertFalse(HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_TRIBULATION).launchesScreen());
        assertFalse(HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_DAO_MARK).launchesScreen());
    }

    @Test
    void routePolicyUsesExplicitPlaceholderTargetsForUnsupportedModules() {
        CardRoutePolicy overview = HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_APERTURE_OVERVIEW);
        CardRoutePolicy tribulation = HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_TRIBULATION);
        CardRoutePolicy daomark = HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_DAO_MARK);

        assertTrue(overview.staysOnHub());
        assertEquals(HubRoutePolicy.RouteTarget.CURRENT_HUB_OVERVIEW, overview.target());
        assertEquals(HubRoutePolicy.CURRENT_HUB_OVERVIEW_NOTICE, overview.noticeText());

        assertTrue(tribulation.usesPlaceholder());
        assertEquals(HubRoutePolicy.RouteTarget.TRIBULATION_SUB_VIEW, tribulation.target());
        assertEquals(HubRoutePolicy.TRIBULATION_PLACEHOLDER_NOTICE, tribulation.noticeText());

        assertTrue(daomark.usesPlaceholder());
        assertEquals(HubRoutePolicy.RouteTarget.DAOMARK_SUB_VIEW, daomark.target());
        assertEquals(HubRoutePolicy.DAOMARK_PLACEHOLDER_NOTICE, daomark.noticeText());
    }

    @Test
    void routePolicyKeepsSummaryRouteSeparateFromRouteOnlyModules() {
        CardRoutePolicy resource = HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_RESOURCE);
        CardRoutePolicy alchemy = HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_ALCHEMY);
        CardRoutePolicy storage = HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_STORAGE);
        CardRoutePolicy cluster = HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_CLUSTER);
        CardRoutePolicy daomark = HubRoutePolicy.routeForCard(HubRoutePolicy.CARD_DAO_MARK);

        assertTrue(resource.isSummaryRoute());
        assertTrue(resource.launchesScreen());
        assertEquals(HubRoutePolicy.RouteTarget.RESOURCE_CONTROLLER_SCREEN, resource.target());

        assertTrue(alchemy.isRouteOnly());
        assertTrue(storage.isRouteOnly());
        assertTrue(cluster.isRouteOnly());
        assertTrue(daomark.isRouteOnly());
        assertFalse(alchemy.isSummaryRoute());
        assertFalse(daomark.launchesScreen());
    }

    @Test
    void routePolicyRejectsUnknownCardIdToPreventImplicitBranching() {
        assertThrows(IllegalArgumentException.class, () -> HubRoutePolicy.routeForCard("unknown-card"));
    }
}
