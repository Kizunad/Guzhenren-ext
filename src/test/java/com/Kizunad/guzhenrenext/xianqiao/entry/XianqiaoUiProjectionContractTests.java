package com.Kizunad.guzhenrenext.xianqiao.entry;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptStage;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.InitPhase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class XianqiaoUiProjectionContractTests {

    @Test
    void allShellSurfacesShareSameStageRiskAndPrimaryBlocker() {
        XianqiaoUiProjection.ProjectionSnapshot projection = XianqiaoUiProjection.project(
            new XianqiaoUiProjection.ProjectionInput(
                InitPhase.COMPLETED,
                AscensionAttemptStage.QI_OBSERVATION_AND_HARMONIZATION,
                68,
                71,
                59,
                83,
                true,
                false,
                false,
                false,
                true,
                false
            )
        );

        List<XianqiaoUiProjection.SurfaceSummary> surfaces = projection.allSurfaceSummaries();
        assertEquals(7, surfaces.size());
        for (XianqiaoUiProjection.SurfaceSummary surface : surfaces) {
            assertEquals("观气调和", surface.stageLabel());
            assertEquals("三气仍在调和", surface.riskLabel());
            assertEquals("三气尚未充足", surface.primaryBlocker());
            assertFalse(surface.detail().contains("CONFIRMED"));
            assertFalse(surface.detail().contains("快照"));
            assertFalse(surface.detail().contains("事务"));
        }
        assertEquals("继续调和三气", projection.entryButtonLabel());
        assertTrue(projection.gameplayEntryAvailable());
        assertEquals("dao", projection.detectorRoute().targetModule());
        assertEquals("environment", projection.detectorRoute().targetSubview());
    }

    @Test
    void confirmedCapableProjectionExposesSingleEntryActionAcrossSurfaces() {
        XianqiaoUiProjection.ProjectionSnapshot projection = XianqiaoUiProjection.project(
            new XianqiaoUiProjection.ProjectionInput(
                InitPhase.COMPLETED,
                AscensionAttemptStage.CONFIRMED,
                92,
                91,
                90,
                93,
                true,
                true,
                true,
                true,
                true,
                true
            )
        );

        assertEquals("发起升仙冲关", projection.entryButtonLabel());
        assertTrue(projection.gameplayEntryAvailable());
        for (XianqiaoUiProjection.SurfaceSummary surface : projection.allSurfaceSummaries()) {
            assertEquals("劫机已定", surface.stageLabel());
            assertEquals("冲关契机已成", surface.riskLabel());
            assertEquals("无显式阻塞", surface.primaryBlocker());
            assertFalse(surface.detail().contains("CONFIRMED"));
            assertFalse(surface.detail().contains("transaction"));
        }
        assertTrue(projection.homeCard().detail().contains("升仙气机已定（亲自引动）"));
        assertNotEquals("待实现", projection.detectorRoute().displayTargetPath());
    }

    @Test
    void cultivationProgressKeepsGameplayEntryUnavailable() {
        XianqiaoUiProjection.ProjectionSnapshot projection = XianqiaoUiProjection.project(
            new XianqiaoUiProjection.ProjectionInput(
                InitPhase.UNINITIALIZED,
                AscensionAttemptStage.CULTIVATION_PROGRESS,
                20,
                25,
                10,
                40,
                false,
                false,
                false,
                false,
                false,
                false
            )
        );

        assertEquals("继续修炼", projection.entryButtonLabel());
        assertFalse(projection.gameplayEntryAvailable());
        assertEquals("修炼推进", projection.homeBanner().stageLabel());
        assertEquals("五转巅峰未满足", projection.homeBanner().primaryBlocker());
        assertFalse(projection.homeCard().detail().contains("快照"));
    }
}
