package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubCardRegistry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApertureHubCardRegistryTests {

    private static final int EXPECTED_CARD_COUNT = 8;
    private static final int EXPECTED_STRONG_CARD_COUNT = 4;
    private static final int EXPECTED_LIGHT_CARD_COUNT = 4;

    @Test
    void firstVersionCardTaxonomyIsExplicitForEveryModule() {
        final Map<String, String> taxonomyByCardId = HubCardRegistry.cards()
            .stream()
            .collect(
                Collectors.toMap(
                    HubCardRegistry.CardDefinition::cardId,
                    contract -> contract.taxonomy().serializedName()
                )
            );

        assertEquals(EXPECTED_CARD_COUNT, taxonomyByCardId.size());
        assertEquals(
            Map.of(
                "aperture-overview", "real-summary",
                "land-spirit", "real-summary",
                "tribulation", "real-summary",
                "resource", "summary-route",
                "alchemy", "route-only",
                "storage", "route-only",
                "cluster", "route-only",
                "dao-mark", "route-only"
            ),
            taxonomyByCardId
        );
        assertEquals(
            Set.of("real-summary", "summary-route", "route-only"),
            Set.copyOf(taxonomyByCardId.values())
        );
    }

    @Test
    void firstVersionStrongCardsAndLightCardsStayFrozen() {
        final List<String> strongCardIds = HubCardRegistry.strongCards()
            .stream()
            .map(HubCardRegistry.CardDefinition::cardId)
            .toList();
        final List<String> lightCardIds = HubCardRegistry.lightCards()
            .stream()
            .map(HubCardRegistry.CardDefinition::cardId)
            .toList();

        assertEquals(EXPECTED_STRONG_CARD_COUNT, strongCardIds.size());
        assertEquals(
            List.of("aperture-overview", "land-spirit", "tribulation", "resource"),
            strongCardIds
        );
        assertEquals(EXPECTED_LIGHT_CARD_COUNT, lightCardIds.size());
        assertEquals(List.of("alchemy", "storage", "cluster", "dao-mark"), lightCardIds);
    }

    @Test
    void cardsWithoutStableSummarySourceMustFallbackInsteadOfPretendingHealthy() {
        final List<HubCardRegistry.CardDefinition> unstableCards =
            HubCardRegistry.cards()
                .stream()
                .filter(HubCardRegistry.CardDefinition::lacksStableSummarySource)
                .toList();

        final Map<String, String> fallbackByCardId = unstableCards.stream().collect(
            Collectors.toMap(
                HubCardRegistry.CardDefinition::cardId,
                HubCardRegistry.CardDefinition::fallbackText
            )
        );

        assertEquals(
            Map.of(
                "resource", "待前往资源分台查看",
                "alchemy", "待前往炼丹分台查看",
                "storage", "待前往储物分台查看",
                "cluster", "待前往集群分台查看",
                "dao-mark", "待前往道痕分台查看"
            ),
            fallbackByCardId
        );
        assertTrue(
            unstableCards.stream().allMatch(
                HubCardRegistry.CardDefinition::forbidSyntheticHealthyState
            )
        );
        assertTrue(
            unstableCards.stream().allMatch(
                contract -> contract.fallbackPresentation()
                    == HubCardRegistry.FallbackPresentation.ROUTE_TO_DETAIL
            )
        );
        assertTrue(unstableCards.stream().noneMatch(contract -> contract.fallbackText().contains("0")));
        assertTrue(unstableCards.stream().noneMatch(contract -> contract.fallbackText().contains("实时")));
    }
}
