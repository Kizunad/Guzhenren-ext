package com.Kizunad.guzhenrenext.xianqiao.entry;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptEntryChannel;
import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptServiceContract;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApertureEntryFlowIntegrationTests {

    @Test
    void legacyCommandAndFuturePlayerEntryMustConvergeToSingleAttemptService() {
        String legacyService = AscensionAttemptServiceContract.resolveTransactionService(
            AscensionAttemptEntryChannel.LEGACY_COMMAND_ADAPTER
        );
        String futurePlayerEntryService = AscensionAttemptServiceContract.resolveTransactionService(
            AscensionAttemptEntryChannel.PLAYER_INITIATED_ENTRY
        );
        assertEquals(legacyService, futurePlayerEntryService);
        assertEquals(AscensionAttemptServiceContract.SINGLE_ATTEMPT_TRANSACTION_SERVICE, legacyService);
    }

    @Test
    void legacyBaselinePathStillExistsAndIsPinnedForAdapterMigration() throws Exception {
        String commandSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/command/ApertureCommand.java")
        );
        String hubScreenSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/client/ApertureHubScreen.java")
        );
        String hubMenuSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/block/ApertureHubMenu.java")
        );
        String worldDataSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/data/ApertureWorldData.java")
        );
        assertTrue(commandSource.contains("enter_aperture"));
        assertTrue(commandSource.contains("initializeApertureIfNeeded("));
        assertTrue(commandSource.contains("sampleInitialTerrains("));
        assertTrue(commandSource.contains("markApertureInitialized"));
        assertTrue(commandSource.contains("executeUnifiedAscensionEntry("));
        assertTrue(hubScreenSource.contains("BUTTON_ASCENSION_ENTRY"));
        assertTrue(hubScreenSource.contains("menu.getUiProjection()"));
        assertTrue(hubScreenSource.contains("projection.entryButtonLabel()"));
        assertTrue(hubScreenSource.contains("projection.gameplayEntryAvailable()"));
        assertTrue(hubMenuSource.contains("getUiProjection()"));
        assertTrue(hubMenuSource.contains("BUTTON_ASCENSION_ENTRY"));
        assertTrue(worldDataSource.contains("initializedApertures"));
    }

    @Test
    void mainBootstrapPathMustUseFrozenLayoutInsteadOfLegacyTwoByTwoSampler() throws Exception {
        String commandSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/command/ApertureCommand.java")
        );

        assertTrue(commandSource.contains("materializeInitialTerrainPlan("));
        assertTrue(commandSource.contains("resolvePlatformCenterFromLayoutPlan("));
        assertTrue(commandSource.contains("sampleInitialTerrains("));
        assertFalse(
            commandSource.contains("materializeCells(ApertureBootstrapExecutor.FrozenBootstrapPlan frozenPlan) {\n"
                + "                sampleInitialTerrains(level, player, worldData.getOrAllocate(owner).center());")
        );
    }

    @Test
    void liveEarthQiMustBeRepoBackedAndFrozenSnapshotMustWinProjection() throws Exception {
        String commandSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/command/ApertureCommand.java")
        );
        String coreBlockSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/block/ApertureCoreBlockEntity.java")
        );
        String resolverSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/opening/OpeningProfileResolver.java")
        );

        assertFalse(commandSource.contains("DEFAULT_EARTH_QI"));
        assertTrue(commandSource.contains("resolveFromPlayer(serverPlayer, playerInitiated)"));
        assertTrue(commandSource.contains("resolveFromPlayer(\n            player,\n            true\n        )"));
        assertTrue(resolverSource.contains("KEY_DI_YU = \"di_yu\""));
        assertTrue(resolverSource.contains("KEY_EARTH_QI = \"earthQi\""));

        assertTrue(coreBlockSource.contains("worldData.getAscensionAttemptState(owner)"));
        assertFalse(coreBlockSource.contains("resolveFromSnapshot(initializationState.openingSnapshot())"));
        assertFalse(coreBlockSource.contains("resolveFromPlayer(ownerPlayer, false)"));
        assertFalse(coreBlockSource.contains("DEFAULT_EARTH_QI"));
    }

    @Test
    void resolveBlockedMessageUsesPersistedAttemptStateAsLifecycleTruth() throws Exception {
        String source = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/entry/ApertureEntryFlowService.java")
        );
        String worldDataSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/data/ApertureWorldData.java")
        );
        String menuSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/block/ApertureHubMenu.java")
        );
        String projectionSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/entry/XianqiaoUiProjection.java")
        );

        assertTrue(source.contains("commitConfirmedAttemptTransaction("));
        assertTrue(source.contains("if (!attemptState.hasCommittedConfirmationHandoff())"));
        assertTrue(source.contains("private static Component resolveBlockedMessage(AscensionAttemptState attemptState)"));
        assertTrue(source.contains("AscensionAttemptStage stage = attemptState.stage();"));
        assertTrue(source.contains("if (stage == AscensionAttemptStage.CULTIVATION_PROGRESS)"));
        assertTrue(source.contains("return Component.literal(\"未达五转巅峰，尚不可冲关。\");"));
        assertTrue(source.contains("stage == AscensionAttemptStage.ASCENSION_PREPARATION_UNLOCKED"));
        assertTrue(source.contains("stage == AscensionAttemptStage.QI_OBSERVATION_AND_HARMONIZATION"));
        assertTrue(source.contains("return Component.literal(\"天地人三气尚未圆融，尚不可冲关。\");"));
        assertTrue(source.contains("if (stage == AscensionAttemptStage.READY_TO_CONFIRM)"));
        assertTrue(source.contains("return Component.literal(\"升仙气机未定，尚不可冲关。\");"));
        assertTrue(source.contains("stage == AscensionAttemptStage.FAILED_SEVERE_INJURY || stage == AscensionAttemptStage.FAILED_DEATH"));
        assertTrue(source.contains("if (!attemptState.internalRecoveryCompleted())"));
        assertTrue(source.contains("if (!attemptState.externalRiskRecoveryCompleted())"));
        assertTrue(source.contains("return Component.literal(\"当前火候未足，尚不可冲关。\");"));
        assertFalse(source.contains("attemptProfile.threeQiEvaluation().canEnterConfirmed()"));
        assertTrue(worldDataSource.contains("if (persistedAttemptState.hasCommittedConfirmationHandoff())"));
        assertTrue(worldDataSource.contains("return persistedAttemptState;"));
        assertTrue(worldDataSource.contains("if (!candidateState.canEnterConfirmed())"));
        assertTrue(worldDataSource.contains("setInitializationState(owner, currentState.withAttemptState(committedState));"));
        assertTrue(menuSource.contains("DATA_ATTEMPT_STAGE"));
        assertTrue(menuSource.contains("getAttemptStage()"));
        assertFalse(menuSource.contains("DATA_SUGGESTED_STAGE"));
        assertFalse(menuSource.contains("getSuggestedStage()"));
        assertTrue(projectionSource.contains("AscensionAttemptStage attemptStage"));
        assertFalse(projectionSource.contains("AscensionAttemptStage suggestedStage"));
    }

    @Test
    void tribulationLifecycleMustBeDrivenByPersistedRuntimeTruth() throws Exception {
        String tickHandlerSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/tribulation/TribulationTickHandler.java")
        );
        String worldDataSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/data/ApertureWorldData.java")
        );
        String managerSource = Files.readString(
            Path.of("src/main/java/com/Kizunad/guzhenrenext/xianqiao/tribulation/TribulationManager.java")
        );

        assertTrue(tickHandlerSource.contains("AscensionAttemptServiceContract.isCommittedConfirmationHandoffStage"));
        assertTrue(tickHandlerSource.contains("worldData.getTribulationRuntimeState(owner)"));
        assertTrue(tickHandlerSource.contains("TribulationManager.restoreFromRuntimeState(owner, runtimeState)"));
        assertTrue(tickHandlerSource.contains("worldData.setTribulationRuntimeState(owner, manager.snapshotRuntimeState())"));
        assertTrue(tickHandlerSource.contains("worldData.clearTribulationRuntimeState(owner)"));
        assertTrue(tickHandlerSource.contains("attemptState.stage() == AscensionAttemptStage.CONFIRMED"));
        assertFalse(tickHandlerSource.contains("worldData.setTribulationActive("));

        assertTrue(worldDataSource.contains("KEY_TRIBULATION_RUNTIME_STATE"));
        assertTrue(worldDataSource.contains("public record TribulationRuntimeState("));
        assertTrue(worldDataSource.contains("withTribulationRuntimeState("));
        assertTrue(worldDataSource.contains("public AscensionAttemptState markAttemptStage(UUID owner, AscensionAttemptStage stage)"));
        assertTrue(worldDataSource.contains("public AscensionAttemptState recordFailureAftermath(UUID owner, AscensionAttemptStage failureStage)"));
        assertTrue(worldDataSource.contains("public AscensionAttemptState markFailurePenaltyApplied(UUID owner)"));
        assertTrue(worldDataSource.contains("public AscensionAttemptState completeRecoveryIfEligible(UUID owner)"));

        assertTrue(managerSource.contains("public ApertureWorldData.TribulationRuntimeState snapshotRuntimeState()"));
        assertTrue(managerSource.contains("worldData.recordFailureAftermath(owner, failureStage)"));
        assertTrue(managerSource.contains("worldData.markFailurePenaltyApplied(owner)"));
        assertTrue(managerSource.contains("worldData.clearTribulationRuntimeState(owner);"));
    }
}
