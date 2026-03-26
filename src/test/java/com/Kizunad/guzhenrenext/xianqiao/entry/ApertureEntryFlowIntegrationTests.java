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
        assertTrue(hubScreenSource.contains("TAB_ASCENSION"));
        assertTrue(hubScreenSource.contains("createAscensionPanel"));
        assertTrue(hubScreenSource.contains("BUTTON_ASCENSION_ENTRY"));
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

        int snapshotIndex = coreBlockSource.indexOf("if (initializationState.openingSnapshot() != null)");
        int liveIndex = coreBlockSource.indexOf("getPlayerList().getPlayer(owner)");
        assertTrue(snapshotIndex >= 0);
        assertTrue(liveIndex >= 0);
        assertTrue(snapshotIndex < liveIndex);
        assertTrue(coreBlockSource.contains("resolveFromSnapshot(initializationState.openingSnapshot())"));
        assertTrue(coreBlockSource.contains("resolveFromPlayer(ownerPlayer, false)"));
        assertFalse(coreBlockSource.contains("DEFAULT_EARTH_QI"));
    }
}
