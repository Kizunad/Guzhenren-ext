package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubSnapshot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApertureHubSnapshotTests {

    @Test
    void snapshotFromCoreUsesExplicitFallbackForUnsupportedSummaryRouteModules() {
        HubSnapshot.CoreSnapshot core = new HubSnapshot.CoreSnapshot(
            HubSnapshot.DataClass.REAL_CORE,
            1,
            4,
            2,
            8,
            4,
            7,
            125,
            8600,
            3,
            false,
            72000L
        );

        HubSnapshot snapshot = HubSnapshot.fromCore(core);

        assertEquals(HubSnapshot.DataClass.REAL_CORE, snapshot.core().dataClass());
        assertEquals(125, snapshot.core().timeSpeedPercent());
        assertEquals(8600, snapshot.core().favorabilityPercent());
        assertEquals(3, snapshot.core().tier());
        assertFalse(snapshot.core().frozen());
        assertEquals(72000L, snapshot.core().tribulationTick());
        assertEquals(HubSnapshot.DataClass.REAL_SUMMARY, snapshot.landSpirit().dataClass());
        assertEquals(HubSnapshot.DataClass.SUMMARY_ROUTE, snapshot.resource().dataClass());
        assertEquals(HubSnapshot.RealSummaryState.MISSING, snapshot.landSpirit().state());
        assertEquals(HubSnapshot.SummaryRouteState.ROUTE_FALLBACK, snapshot.resource().state());
        assertEquals(HubSnapshot.SPIRIT_ROUTE_FALLBACK_TEXT, snapshot.landSpirit().fallbackText());
        assertEquals(HubSnapshot.RESOURCE_ROUTE_FALLBACK_TEXT, snapshot.resource().fallbackText());
        assertFalse(snapshot.landSpirit().isAvailable());
        assertTrue(snapshot.resource().isRouteFallback());
        assertFalse(snapshot.resource().fallbackText().contains("0"));
    }

    @Test
    void snapshotFromApertureMenuCorePathNowBuildsRealLandSpiritSummaryFromCoreTruth() {
        HubSnapshot.CoreSnapshot core = new HubSnapshot.CoreSnapshot(
            HubSnapshot.DataClass.REAL_CORE,
            2,
            6,
            -1,
            4,
            5,
            6,
            140,
            8750,
            4,
            false,
            96000L
        );

        HubSnapshot.LandSpiritSnapshot landSpirit = HubSnapshot.LandSpiritSnapshot.fromCoreSnapshot(core);

        assertEquals(HubSnapshot.DataClass.REAL_SUMMARY, landSpirit.dataClass());
        assertEquals(HubSnapshot.RealSummaryState.AVAILABLE, landSpirit.state());
        assertEquals(875, landSpirit.favorabilityPermille());
        assertEquals(4, landSpirit.tier());
        assertEquals(4, landSpirit.stage());
        assertEquals(6, landSpirit.nextStageMinTier());
        assertEquals(800, landSpirit.nextStageMinFavorabilityPermille());
        assertTrue(landSpirit.isAvailable());
        assertEquals(HubSnapshot.SPIRIT_ROUTE_FALLBACK_TEXT, landSpirit.fallbackText());
    }

    @Test
    void snapshotCanPromoteToAvailableRealSummaryAndConservativeSummaryRoute() {
        HubSnapshot.CoreSnapshot core = new HubSnapshot.CoreSnapshot(
            HubSnapshot.DataClass.REAL_CORE,
            -3,
            3,
            -2,
            2,
            7,
            5,
            100,
            7800,
            2,
            true,
            50000L
        );
        HubSnapshot.LandSpiritSnapshot spirit = new HubSnapshot.LandSpiritSnapshot(
            HubSnapshot.DataClass.REAL_SUMMARY,
            HubSnapshot.RealSummaryState.AVAILABLE,
            905,
            4,
            2,
            5,
            1200,
            HubSnapshot.SPIRIT_ROUTE_FALLBACK_TEXT
        );
        HubSnapshot.ResourceSnapshot resource = new HubSnapshot.ResourceSnapshot(
            HubSnapshot.DataClass.SUMMARY_ROUTE,
            HubSnapshot.SummaryRouteState.CONSERVATIVE_LOCAL_SUMMARY,
            true,
            630,
            113,
            27,
            400,
            HubSnapshot.RESOURCE_LOCAL_SUMMARY_NOTICE
        );

        HubSnapshot snapshot = HubSnapshot.fromCore(core).withLandSpirit(spirit).withResource(resource);

        assertTrue(snapshot.landSpirit().isAvailable());
        assertFalse(snapshot.resource().isRouteFallback());
        assertEquals(905, snapshot.landSpirit().favorabilityPermille());
        assertEquals(630, snapshot.resource().progressPermille());
        assertEquals(27, snapshot.resource().auraValue());
        assertTrue(snapshot.resource().fallbackText().contains("局部采样"));
        assertEquals(HubSnapshot.RESOURCE_LOCAL_SUMMARY_NOTICE, snapshot.resource().fallbackText());
    }

    @Test
    void snapshotTypesRejectWrongDataClassToProtectBoundaries() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new HubSnapshot.CoreSnapshot(
                HubSnapshot.DataClass.SUMMARY_ROUTE,
                0,
                0,
                0,
                0,
                1,
                1,
                100,
                1000,
                1,
                false,
                1L
            )
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> new HubSnapshot.LandSpiritSnapshot(
                HubSnapshot.DataClass.SUMMARY_ROUTE,
                HubSnapshot.RealSummaryState.AVAILABLE,
                0,
                0,
                0,
                0,
                0,
                HubSnapshot.SPIRIT_ROUTE_FALLBACK_TEXT
            )
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> new HubSnapshot.ResourceSnapshot(
                HubSnapshot.DataClass.REAL_SUMMARY,
                HubSnapshot.SummaryRouteState.ROUTE_FALLBACK,
                false,
                0,
                0,
                0,
                0,
                HubSnapshot.RESOURCE_ROUTE_FALLBACK_TEXT
            )
        );
    }
}
