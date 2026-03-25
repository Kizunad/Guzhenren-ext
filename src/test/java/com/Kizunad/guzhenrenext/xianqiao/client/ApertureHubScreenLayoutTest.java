package com.Kizunad.guzhenrenext.xianqiao.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApertureHubScreenLayoutTest {

    private static final int EXPECTED_V2_SECTION_COUNT = 4;
    private static final Path APERTURE_HUB_SCREEN_SOURCE = Path.of(
        "src/main/java/com/Kizunad/guzhenrenext/xianqiao/client/ApertureHubScreen.java"
    );
    private static final List<String> LEGACY_SOURCE_MARKERS = List.of(
        "TAB_COUNT = 5",
        "createTabButton(main, TAB_OVERVIEW, \"总览\"",
        "createTabButton(\n            main,\n            TAB_SPIRIT,\n            \"地灵\"",
        "createTabButton(\n            main,\n            TAB_RESOURCE,\n            \"资源\"",
        "createTabButton(\n            main,\n            TAB_TRIBULATION,\n            \"灾劫\"",
        "createTabButton(\n            main,\n            TAB_DAOMARK,\n            \"道痕\""
    );
    private static final List<String> V2_SOURCE_MARKERS = List.of(
        "buildHeaderBand",
        "buildTopSituationRow",
        "buildMainModuleGrid",
        "buildBottomUtilityRow",
        "ScrollContainer",
        "resetUiReferences();",
        "moduleCardsById.clear();"
    );
    private static final List<String> ORDERED_LAYER_SOURCE_MARKERS = List.of(
        "LAYER_HEADER_BAND",
        "LAYER_TOP_SITUATION_ROW",
        "LAYER_MAIN_MODULE_GRID",
        "LAYER_BOTTOM_UTILITY_ROW"
    );
    private static final List<String> SCROLL_FALLBACK_SOURCE_MARKERS = List.of(
        "final boolean useBodyScrollFallback = bodyContentHeight > bodyViewportHeight;",
        "if (layout.useBodyScrollFallback())",
        "new ScrollContainer(theme)"
    );
    private static final List<String> TASK6_STRONG_CARD_SOURCE_MARKERS = List.of(
        "headerDetailLabel.setText(buildHeaderDetailText(snapshot.core()));",
        "overallSummaryBlock.setBodyText(buildTopOverallSummary(snapshot, status));",
        "riskSummaryBlock.setBodyText(buildTopTribulationSummary(snapshot.core().tribulationTick(), status));",
        "case HubRoutePolicy.CARD_APERTURE_OVERVIEW -> formatCoreSummary(snapshot.core());",
        "case HubRoutePolicy.CARD_TRIBULATION -> formatTribulationSummary(snapshot.core().tribulationTick(), status);",
        "return formatTimeSpeed(coreSnapshot.timeSpeedPercent())",
        "+ \"x · 好感 \"",
        "return \"倒计时 \" + formatTribulationTime(tribulationTick);"
    );
    private static final List<String> TASK6_FALLBACK_SOURCE_MARKERS = List.of(
        "case HubRoutePolicy.CARD_LAND_SPIRIT -> snapshot.landSpirit().isAvailable()",
        ": snapshot.landSpirit().fallbackText();",
        "case HubRoutePolicy.CARD_RESOURCE -> snapshot.resource().isRouteFallback()",
        "? snapshot.resource().fallbackText()",
        "case HubRoutePolicy.CARD_RESOURCE -> snapshot.resource().fallbackText();"
    );

    @Test
    void v2MainStructureUsesFourLockedLayersInFixedOrder() {
        assertIterableEquals(
            List.of("HeaderBand", "TopSituationRow", "MainModuleGrid", "BottomUtilityRow"),
            ApertureHubV2Contract.orderedStructureLayers()
                .stream()
                .map(ApertureHubV2Contract.StructureLayer::conceptName)
                .toList()
        );
        assertEquals(EXPECTED_V2_SECTION_COUNT, ApertureHubV2Contract.orderedStructureLayers().size());
        assertFalse(
            ApertureHubV2Contract.hasLegacyFivePeerTabs(
                ApertureHubV2Contract.orderedStructureLayers()
            )
        );
    }

    @Test
    void v2FrozenTopLevelLayoutAssignsCardsOnlyToMainModuleGrid() {
        final List<ApertureHubV2Contract.LayerContract> frozenLayout = ApertureHubV2Contract.fixedTopLevelLayout();

        assertEquals(EXPECTED_V2_SECTION_COUNT, frozenLayout.size());
        assertIterableEquals(
            ApertureHubV2Contract.orderedStructureLayers(),
            frozenLayout.stream().map(ApertureHubV2Contract.LayerContract::layer).toList()
        );
        assertEquals(
            List.of("hub-title", "stability-headline"),
            frozenLayout.get(0).frozenSlots()
        );
        assertEquals(
            List.of("overall-summary", "risk-summary", "next-route"),
            frozenLayout.get(1).frozenSlots()
        );
        assertEquals(
            ApertureHubV2Contract.firstVersionCardIds(),
            frozenLayout.get(2).frozenSlots()
        );
        assertTrue(frozenLayout.get(2).hostsModuleCards());
        assertEquals(
            List.of("utility-routes", "summary-footnote", "fallback-explainer"),
            frozenLayout.get(3).frozenSlots()
        );
        assertFalse(frozenLayout.get(0).hostsModuleCards());
        assertFalse(frozenLayout.get(1).hostsModuleCards());
        assertFalse(frozenLayout.get(3).hostsModuleCards());
    }

    @Test
    void v2ContractUsesStructuralBandsInsteadOfLegacyFiveTabTextLayout() throws Exception {
        final List<String> structureConcepts = ApertureHubV2Contract.orderedStructureLayers()
            .stream()
            .map(ApertureHubV2Contract.StructureLayer::conceptName)
            .toList();
        final String currentSource = Files.readString(APERTURE_HUB_SCREEN_SOURCE);
        final List<String> frozenLayerNames = ApertureHubV2Contract.fixedTopLevelLayout()
            .stream()
            .map(layer -> layer.layer().conceptName())
            .toList();

        assertFalse(LEGACY_SOURCE_MARKERS.stream().anyMatch(currentSource::contains));
        assertTrue(V2_SOURCE_MARKERS.stream().allMatch(currentSource::contains));
        assertFalse(
            ApertureHubV2Contract.hasLegacyFivePeerTabs(
                ApertureHubV2Contract.orderedStructureLayers()
            )
        );
        assertEquals(5, ApertureHubV2Contract.legacyTabLabels().size());
        assertTrue(structureConcepts.stream().noneMatch(ApertureHubV2Contract.legacyTabLabels()::contains));
        assertIterableEquals(structureConcepts, frozenLayerNames);
        assertNotEquals(ApertureHubV2Contract.legacyTabLabels(), frozenLayerNames);
        assertTrue(ORDERED_LAYER_SOURCE_MARKERS.stream().allMatch(currentSource::contains));
    }

    @Test
    void v2ShellLayoutFallsBackToScrollContainerOnlyWhenBodyCannotFitViewport() throws Exception {
        final String currentSource = readCurrentSource();

        assertTrue(SCROLL_FALLBACK_SOURCE_MARKERS.stream().allMatch(currentSource::contains));
        assertTrue(currentSource.contains("moduleCardsById.clear();"));
        assertTrue(currentSource.contains("resetUiReferences();"));
    }

    @Test
    void task6StrongCardsBindHeaderTopSituationAndCoreTribulationCardsToRealCoreFields() throws Exception {
        final String currentSource = readCurrentSource();

        assertTrue(TASK6_STRONG_CARD_SOURCE_MARKERS.stream().allMatch(currentSource::contains));
        assertTrue(currentSource.contains("formatTier(coreSnapshot.tier())"));
        assertTrue(currentSource.contains("formatFrozenState(coreSnapshot.frozen())"));
        assertTrue(currentSource.contains("buildCoreFootnote(snapshot.core())"));
        assertTrue(currentSource.contains("buildTribulationFootnote(snapshot.core().tribulationTick(), status)"));
    }

    @Test
    void task6FallbackCardsRemainExplicitWhenHubHasNoLegalSpiritOrResourceSnapshotInjection() throws Exception {
        final String currentSource = readCurrentSource();

        assertTrue(TASK6_FALLBACK_SOURCE_MARKERS.stream().allMatch(currentSource::contains));
        assertFalse(currentSource.contains("withLandSpiritMenu("));
        assertFalse(currentSource.contains("withResourceMenu("));
        assertFalse(currentSource.contains("new LandSpiritMenu("));
        assertFalse(currentSource.contains("new ResourceControllerMenu("));
    }

    private static String readCurrentSource() throws Exception {
        return Files.readString(APERTURE_HUB_SCREEN_SOURCE);
    }
}
