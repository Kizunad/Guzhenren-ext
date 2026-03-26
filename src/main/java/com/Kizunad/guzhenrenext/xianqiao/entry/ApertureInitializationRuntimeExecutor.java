package com.Kizunad.guzhenrenext.xianqiao.entry;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptEntryChannel;
import com.Kizunad.guzhenrenext.xianqiao.command.ApertureCommand;
import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class ApertureInitializationRuntimeExecutor {

    private static final ResourceKey<Level> APERTURE_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath("guzhenrenext", "aperture_world")
    );

    private static final String PLANNED_CELL_WIRING_ANCHOR = """
        BIOME_SEARCH_SERVICE.createBoundedResolver(
        new OverworldTerrainSampler.SourceSearchRequest(
        searchContext.searchOrigin(),
        searchContext.random(),
        boundedResolver
        OverworldTerrainSampler.sampleAndPlace(
        """;

    private static final String CHUNK_BOUNDARY_WIRING_ANCHOR = """
        WorldChunkBoundary worldChunkBoundary = resolveWorldChunkBoundary(plan, resolvedCenter);
        worldData.updateChunkBoundary(
        InitialTerrainPlan.InitialChunkBoundary localBoundary = plan.initialChunkBoundary();
        InitialTerrainPlan.CoreAnchor coreAnchor = plan.coreAnchor();
        coreAnchor.seamCenterChunkX()
        coreAnchor.seamCenterChunkZ()
        Math.round((localChunk - seamCenterChunk) * CHUNK_SIZE_BLOCKS)
        + PLANNED_CELL_SIDE_LENGTH - 1
        SectionPos.blockToSectionCoord(minBlockX)
        SectionPos.blockToSectionCoord(maxBlockX)
        SectionPos.blockToSectionCoord(minBlockZ)
        SectionPos.blockToSectionCoord(maxBlockZ)
        """;

    public static ResourceKey<Level> apertureDimension() {
        return APERTURE_DIMENSION;
    }

    public void initializeIfNeeded(ServerPlayer player) {
        Objects.requireNonNull(player, "player");
        ApertureEntryFlowService.EntryResult result = ApertureCommand.executeUnifiedAscensionEntry(
            player,
            AscensionAttemptEntryChannel.PLAYER_INITIATED_ENTRY
        );
        if (!result.success() && result.failureMessage() != null) {
            throw new IllegalStateException(result.failureMessage().getString());
        }
    }
}
