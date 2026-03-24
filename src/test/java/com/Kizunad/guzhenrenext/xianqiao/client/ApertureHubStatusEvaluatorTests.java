package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubStatusEvaluator;
import com.Kizunad.guzhenrenext.xianqiao.client.hub.HubStatusEvaluator.HubStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApertureHubStatusEvaluatorTests {

    @Test
    void evaluatorMarksDangerWhenTribulationTickIsMissingOrNonPositive() {
        HubSnapshot snapshot = HubSnapshot.fromCore(new HubSnapshot.CoreSnapshot(
            HubSnapshot.DataClass.REAL_CORE,
            0,
            2,
            0,
            2,
            3,
            3,
            100,
            7000,
            2,
            false,
            0L
        ));

        HubStatus status = new HubStatusEvaluator().evaluate(snapshot);

        assertEquals(HubStatusEvaluator.RiskLevel.DANGER, status.overallRisk());
        assertTrue(status.tribulationRiskSummary().contains("不可用"));
        assertTrue(status.recommendation().contains("灾劫分台"));
        assertFalse(status.overallSummary().contains("稳定"));
    }

    @Test
    void evaluatorMarksUnknownWhenStrongSummaryAndResourceAreBothFallback() {
        HubSnapshot snapshot = HubSnapshot.fromCore(new HubSnapshot.CoreSnapshot(
            HubSnapshot.DataClass.REAL_CORE,
            -5,
            5,
            -5,
            5,
            11,
            11,
            110,
            8200,
            3,
            false,
            96000L
        ));

        HubStatus status = new HubStatusEvaluator().evaluate(snapshot);

        assertEquals(HubStatusEvaluator.RiskLevel.UNKNOWN, status.overallRisk());
        assertTrue(status.overallSummary().contains("缺失"));
        assertTrue(status.fallbackText().contains(HubSnapshot.SPIRIT_ROUTE_FALLBACK_TEXT));
        assertTrue(status.fallbackText().contains(HubSnapshot.RESOURCE_ROUTE_FALLBACK_TEXT));
        assertFalse(status.fallbackText().contains("0"));
    }

    @Test
    void evaluatorReturnsStableWhenCoreIsHealthyAndSummarysAreAvailable() {
        HubSnapshot.CoreSnapshot core = new HubSnapshot.CoreSnapshot(
            HubSnapshot.DataClass.REAL_CORE,
            -2,
            2,
            -3,
            3,
            5,
            7,
            125,
            9100,
            4,
            false,
            120000L
        );
        HubSnapshot.LandSpiritSnapshot spirit = new HubSnapshot.LandSpiritSnapshot(
            HubSnapshot.DataClass.REAL_SUMMARY,
            HubSnapshot.RealSummaryState.AVAILABLE,
            930,
            4,
            3,
            5,
            1200,
            HubSnapshot.SPIRIT_ROUTE_FALLBACK_TEXT
        );
        HubSnapshot.ResourceSnapshot resource = new HubSnapshot.ResourceSnapshot(
            HubSnapshot.DataClass.SUMMARY_ROUTE,
            HubSnapshot.SummaryRouteState.CONSERVATIVE_LOCAL_SUMMARY,
            true,
            800,
            120,
            30,
            200,
            HubSnapshot.RESOURCE_LOCAL_SUMMARY_NOTICE
        );
        HubSnapshot snapshot = HubSnapshot.fromCore(core).withLandSpirit(spirit).withResource(resource);

        HubStatus status = new HubStatusEvaluator().evaluate(snapshot);

        assertEquals(HubStatusEvaluator.RiskLevel.STABLE, status.overallRisk());
        assertTrue(status.overallSummary().contains("稳定"));
        assertTrue(status.tribulationRiskSummary().contains("充裕"));
        assertTrue(status.recommendation().contains("持续观察"));
        assertTrue(status.fallbackText().contains(HubSnapshot.RESOURCE_LOCAL_SUMMARY_NOTICE));
    }

    @Test
    void evaluatorPointsToResourcePlatformWhenOnlyResourceRemainsRouteFallback() {
        HubSnapshot.CoreSnapshot core = new HubSnapshot.CoreSnapshot(
            HubSnapshot.DataClass.REAL_CORE,
            -4,
            4,
            -4,
            4,
            9,
            9,
            115,
            8800,
            3,
            false,
            72000L
        );
        HubSnapshot.LandSpiritSnapshot spirit = new HubSnapshot.LandSpiritSnapshot(
            HubSnapshot.DataClass.REAL_SUMMARY,
            HubSnapshot.RealSummaryState.AVAILABLE,
            960,
            4,
            3,
            5,
            1200,
            HubSnapshot.SPIRIT_ROUTE_FALLBACK_TEXT
        );
        HubSnapshot snapshot = HubSnapshot.fromCore(core).withLandSpirit(spirit);

        HubStatus status = new HubStatusEvaluator().evaluate(snapshot);

        assertEquals(HubStatusEvaluator.RiskLevel.CAUTION, status.overallRisk());
        assertTrue(status.recommendation().contains("资源分台"));
        assertFalse(status.fallbackText().contains(HubSnapshot.SPIRIT_ROUTE_FALLBACK_TEXT));
        assertTrue(status.fallbackText().contains(HubSnapshot.RESOURCE_ROUTE_FALLBACK_TEXT));
    }

    @Test
    void evaluatorTreatsInvalidBoundaryAsDangerInsteadOfAssumingHealthyZero() {
        HubSnapshot snapshot = HubSnapshot.fromCore(new HubSnapshot.CoreSnapshot(
            HubSnapshot.DataClass.REAL_CORE,
            10,
            8,
            0,
            1,
            -1,
            2,
            100,
            7600,
            2,
            false,
            48000L
        ));

        HubStatus status = new HubStatusEvaluator().evaluate(snapshot);

        assertEquals(HubStatusEvaluator.RiskLevel.DANGER, status.overallRisk());
        assertTrue(status.overallSummary().contains("边界异常"));
        assertTrue(status.recommendation().contains("边界同步"));
    }
}
