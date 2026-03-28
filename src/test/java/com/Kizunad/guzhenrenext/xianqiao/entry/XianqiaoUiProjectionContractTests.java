package com.Kizunad.guzhenrenext.xianqiao.entry;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptStage;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.InitPhase;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Test
    void failedStagesProjectExplicitRecoveryBlockersAndDisableEntry() {
        XianqiaoUiProjection.ProjectionSnapshot projection = XianqiaoUiProjection.project(
            new XianqiaoUiProjection.ProjectionInput(
                InitPhase.COMPLETED,
                AscensionAttemptStage.FAILED_SEVERE_INJURY,
                88,
                87,
                86,
                85,
                true,
                false,
                false,
                false,
                true,
                true
            )
        );

        assertEquals("冲关失败（重伤）", projection.homeBanner().stageLabel());
        assertEquals("冲关余波未平", projection.homeBanner().riskLabel());
        assertEquals("失败后恢复未完成", projection.homeBanner().primaryBlocker());
        assertFalse(projection.gameplayEntryAvailable());
        assertEquals("继续修炼", projection.entryButtonLabel());
    }

    @Test
    void stageRiskAndBlockerStillDeriveFromAttemptStagePlusReadinessFlags() {
        XianqiaoUiProjection.ProjectionSnapshot attemptReadyButNoFiveTurn = XianqiaoUiProjection.project(
            new XianqiaoUiProjection.ProjectionInput(
                InitPhase.COMPLETED,
                AscensionAttemptStage.READY_TO_CONFIRM,
                90,
                90,
                90,
                95,
                false,
                false,
                false,
                false,
                false,
                false
            )
        );
        assertEquals("冲关在即", attemptReadyButNoFiveTurn.homeBanner().stageLabel());
        assertEquals("火候未足", attemptReadyButNoFiveTurn.homeBanner().riskLabel());
        assertEquals("五转巅峰未满足", attemptReadyButNoFiveTurn.homeBanner().primaryBlocker());
        assertTrue(attemptReadyButNoFiveTurn.gameplayEntryAvailable());

        XianqiaoUiProjection.ProjectionSnapshot attemptCultivationButCanEnter = XianqiaoUiProjection.project(
            new XianqiaoUiProjection.ProjectionInput(
                InitPhase.COMPLETED,
                AscensionAttemptStage.CULTIVATION_PROGRESS,
                95,
                95,
                95,
                95,
                true,
                true,
                true,
                true,
                true,
                true
            )
        );
        assertEquals("修炼推进", attemptCultivationButCanEnter.homeBanner().stageLabel());
        assertEquals("冲关契机已成", attemptCultivationButCanEnter.homeBanner().riskLabel());
        assertEquals("无显式阻塞", attemptCultivationButCanEnter.homeBanner().primaryBlocker());
        assertFalse(attemptCultivationButCanEnter.gameplayEntryAvailable());

        XianqiaoUiProjection.ProjectionSnapshot notFrozenSnapshot = XianqiaoUiProjection.project(
            new XianqiaoUiProjection.ProjectionInput(
                InitPhase.COMPLETED,
                AscensionAttemptStage.ASCENSION_PREPARATION_UNLOCKED,
                85,
                84,
                83,
                90,
                true,
                false,
                false,
                false,
                false,
                false
            )
        );
        assertEquals("升仙筹备", notFrozenSnapshot.homeBanner().stageLabel());
        assertEquals("升仙气机渐起", notFrozenSnapshot.homeBanner().riskLabel());
        assertEquals("升仙气机未定", notFrozenSnapshot.homeBanner().primaryBlocker());
        assertTrue(notFrozenSnapshot.homeCard().detail().contains("升仙气机未定"));
    }

    @Test
    void projectionInputAndMenuContractNoLongerExposeSuggestedStageSurface() throws Exception {
        String projectionSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/entry/XianqiaoUiProjection.java")
        );
        String menuSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/block/ApertureHubMenu.java")
        );

        assertTrue(projectionSource.contains("AscensionAttemptStage attemptStage"));
        assertTrue(projectionSource.contains("input.attemptStage()"));
        assertFalse(projectionSource.contains("AscensionAttemptStage suggestedStage"));
        assertFalse(projectionSource.contains("input.suggestedStage()"));

        assertTrue(menuSource.contains("DATA_ATTEMPT_STAGE"));
        assertTrue(menuSource.contains("getAttemptStage()"));
        assertFalse(menuSource.contains("DATA_SUGGESTED_STAGE"));
        assertFalse(menuSource.contains("getSuggestedStage()"));
    }

    @Test
    void projectionReadPathConsumesPersistedAttemptStateFromBoundary() throws Exception {
        String coreBlockSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/block/ApertureCoreBlockEntity.java")
        );
        String entryFlowSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/entry/ApertureEntryFlowService.java")
        );

        assertTrue(coreBlockSource.contains("worldData.getAscensionAttemptState(owner)"));
        assertFalse(coreBlockSource.contains("resolveFromSnapshot(initializationState.openingSnapshot())"));
        assertFalse(coreBlockSource.contains("resolveFromPlayer(ownerPlayer, false)"));
        assertTrue(entryFlowSource.contains("commitConfirmedAttemptTransaction("));
        assertTrue(entryFlowSource.contains("if (!attemptState.hasCommittedConfirmationHandoff())"));
        assertFalse(entryFlowSource.contains("if (!attemptState.canEnterConfirmed())"));
        assertTrue(entryFlowSource.contains("attemptState.internalRecoveryCompleted()"));
        assertTrue(entryFlowSource.contains("attemptState.externalRiskRecoveryCompleted()"));
    }
}
