package com.Kizunad.guzhenrenext.xianqiao.opening;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** 依据冻结画像与推断结果生成确定性布局。 */
public final class OpeningLayoutPlanner {

    private static final int ONE = 1;

    private static final int TWO = 2;

    private static final int APTITUDE_FOR_TWO_BY_TWO = 25;

    private static final int APTITUDE_FOR_THREE_BY_THREE = 50;

    private static final int APTITUDE_FOR_FOUR_BY_FOUR = 75;

    private static final int DEFAULT_ORIGIN_CHUNK = 0;

    private static final int MAX_RESERVED_CHAOS_BLOCKS = 16;

    private static final int SAFEZONE_MAX_OUTSIDE_DISTANCE_BLOCKS = 8;

    private static final int WARNING_MAX_OUTSIDE_DISTANCE_BLOCKS = 16;

    private static final int LETHAL_ZONE_START_OUTSIDE_DISTANCE_BLOCKS = 17;

    private static final double CHUNK_CENTER_OFFSET = 0.5D;

    private static final String DEFAULT_BIOME_ID = "minecraft:plains";

    public InitialTerrainPlan plan(
        ResolvedOpeningProfile profile,
        BiomeInferenceService.BiomeInferenceResult inferenceResult
    ) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(inferenceResult, "inferenceResult");

        InitialTerrainPlan.LayoutTier layoutTier = resolveLayoutTier(profile.aptitudeScore());
        int layoutSize = layoutTier.size();
        InitialTerrainPlan.LayoutOrigin layoutOrigin = resolveLayoutOrigin(layoutSize);
        InitialTerrainPlan.CoreAnchor coreAnchor = new InitialTerrainPlan.CoreAnchor(
            layoutOrigin.originChunkX() + layoutSize / (double) TWO,
            layoutOrigin.originChunkZ() + layoutSize / (double) TWO,
            resolveCoreAnchorSemantics(layoutSize)
        );
        InitialTerrainPlan.TeleportAnchor teleportAnchor = resolveTeleportAnchor(layoutSize, layoutOrigin);
        InitialTerrainPlan.InitialChunkBoundary initialChunkBoundary = new InitialTerrainPlan.InitialChunkBoundary(
            layoutOrigin.originChunkX(),
            layoutOrigin.originChunkX() + layoutSize - ONE,
            layoutOrigin.originChunkZ(),
            layoutOrigin.originChunkZ() + layoutSize - ONE
        );
        InitialTerrainPlan.RingParameters ringParameters = new InitialTerrainPlan.RingParameters(
            MAX_RESERVED_CHAOS_BLOCKS,
            SAFEZONE_MAX_OUTSIDE_DISTANCE_BLOCKS,
            WARNING_MAX_OUTSIDE_DISTANCE_BLOCKS,
            LETHAL_ZONE_START_OUTSIDE_DISTANCE_BLOCKS
        );

        List<String> biomeCandidates = resolveBiomeCandidates(inferenceResult);
        List<InitialTerrainPlan.PlannedTerrainCell> plannedCells = buildPlannedCells(layoutSize, layoutOrigin,
            biomeCandidates);

        return new InitialTerrainPlan(
            layoutTier,
            layoutSize,
            plannedCells.size(),
            coreAnchor,
            teleportAnchor,
            layoutOrigin,
            initialChunkBoundary,
            ringParameters,
            resolveFallbackPolicy(inferenceResult),
            plannedCells
        );
    }

    private static BiomeInferenceService.BiomeFallbackPolicy resolveFallbackPolicy(
        BiomeInferenceService.BiomeInferenceResult inferenceResult
    ) {
        BiomeInferenceService.BiomeFallbackPolicy fallbackPolicy = inferenceResult.fallbackPolicy();
        if (fallbackPolicy == null) {
            return BiomeInferenceService.BiomeFallbackPolicy.NONE;
        }
        return fallbackPolicy;
    }

    private static InitialTerrainPlan.LayoutTier resolveLayoutTier(int aptitudeScore) {
        if (aptitudeScore >= APTITUDE_FOR_FOUR_BY_FOUR) {
            return InitialTerrainPlan.LayoutTier.FOUR_BY_FOUR;
        }
        if (aptitudeScore >= APTITUDE_FOR_THREE_BY_THREE) {
            return InitialTerrainPlan.LayoutTier.THREE_BY_THREE;
        }
        if (aptitudeScore >= APTITUDE_FOR_TWO_BY_TWO) {
            return InitialTerrainPlan.LayoutTier.TWO_BY_TWO;
        }
        return InitialTerrainPlan.LayoutTier.ONE_BY_ONE;
    }

    private static InitialTerrainPlan.LayoutOrigin resolveLayoutOrigin(int layoutSize) {
        return new InitialTerrainPlan.LayoutOrigin(
            DEFAULT_ORIGIN_CHUNK,
            DEFAULT_ORIGIN_CHUNK,
            InitialTerrainPlan.LayoutOriginSemantics.NORTHWEST_CORNER_CHUNK
        );
    }

    private static InitialTerrainPlan.CoreAnchorSemantics resolveCoreAnchorSemantics(int layoutSize) {
        if (layoutSize % TWO == 0) {
            return InitialTerrainPlan.CoreAnchorSemantics.SEAM_CENTER;
        }
        return InitialTerrainPlan.CoreAnchorSemantics.ODD_CENTER_CELL;
    }

    private static InitialTerrainPlan.TeleportAnchor resolveTeleportAnchor(
        int layoutSize,
        InitialTerrainPlan.LayoutOrigin layoutOrigin
    ) {
        boolean evenLayout = layoutSize % TWO == 0;
        int anchorCellX = evenLayout ? layoutSize / TWO - ONE : layoutSize / TWO;
        int anchorCellZ = evenLayout ? layoutSize / TWO - ONE : layoutSize / TWO;
        int anchorChunkX = layoutOrigin.originChunkX() + anchorCellX;
        int anchorChunkZ = layoutOrigin.originChunkZ() + anchorCellZ;
        InitialTerrainPlan.TeleportAnchorSemantics semantics = evenLayout
            ? InitialTerrainPlan.TeleportAnchorSemantics.EVEN_SEAM_KERNEL_NORTHWEST_CELL
            : InitialTerrainPlan.TeleportAnchorSemantics.ODD_CENTER_CELL;
        return new InitialTerrainPlan.TeleportAnchor(
            anchorCellX,
            anchorCellZ,
            anchorChunkX,
            anchorChunkZ,
            anchorChunkX + CHUNK_CENTER_OFFSET,
            anchorChunkZ + CHUNK_CENTER_OFFSET,
            semantics
        );
    }

    private static List<InitialTerrainPlan.PlannedTerrainCell> buildPlannedCells(
        int layoutSize,
        InitialTerrainPlan.LayoutOrigin layoutOrigin,
        List<String> biomeCandidates
    ) {
        List<CellDraft> drafts = new ArrayList<>();
        for (int cellZ = 0; cellZ < layoutSize; cellZ++) {
            for (int cellX = 0; cellX < layoutSize; cellX++) {
                int dxTwice = cellX * TWO + ONE - layoutSize;
                int dzTwice = cellZ * TWO + ONE - layoutSize;
                int ringIndex = Math.max(Math.abs(dxTwice), Math.abs(dzTwice)) / TWO;
                int radialManhattanTwice = Math.abs(dxTwice) + Math.abs(dzTwice);
                drafts.add(new CellDraft(cellX, cellZ, dxTwice, dzTwice, ringIndex, radialManhattanTwice));
            }
        }
        drafts.sort(CELL_ORDER);

        String primaryBiomeId = biomeCandidates.getFirst();
        List<InitialTerrainPlan.PlannedTerrainCell> cells = new ArrayList<>(drafts.size());
        for (int index = 0; index < drafts.size(); index++) {
            CellDraft draft = drafts.get(index);
            cells.add(
                new InitialTerrainPlan.PlannedTerrainCell(
                    draft.cellX,
                    draft.cellZ,
                    layoutOrigin.originChunkX() + draft.cellX,
                    layoutOrigin.originChunkZ() + draft.cellZ,
                    index,
                    draft.ringIndex,
                    draft.ringIndex == 0,
                    primaryBiomeId,
                    biomeCandidates
                )
            );
        }
        return List.copyOf(cells);
    }

    private static List<String> resolveBiomeCandidates(BiomeInferenceService.BiomeInferenceResult inferenceResult) {
        Set<String> candidates = new LinkedHashSet<>();
        for (BiomeInferenceService.BiomePreference preference : inferenceResult.rankedPreferences()) {
            if (preference == null || preference.biomeId() == null || preference.biomeId().isBlank()) {
                continue;
            }
            candidates.add(preference.biomeId());
        }
        Optional<String> fallbackBiomeId = inferenceResult.fallbackBiomeId();
        fallbackBiomeId.ifPresent(candidates::add);
        if (candidates.isEmpty()) {
            candidates.add(DEFAULT_BIOME_ID);
        }
        return List.copyOf(candidates);
    }

    private static final Comparator<CellDraft> CELL_ORDER = Comparator
        .comparingInt((CellDraft draft) -> draft.ringIndex)
        .thenComparingInt(draft -> draft.radialManhattanTwice)
        .thenComparingInt(draft -> Math.abs(draft.dzTwice))
        .thenComparingInt(draft -> Math.abs(draft.dxTwice))
        .thenComparingInt(draft -> draft.cellZ)
        .thenComparingInt(draft -> draft.cellX);

    private record CellDraft(
        int cellX,
        int cellZ,
        int dxTwice,
        int dzTwice,
        int ringIndex,
        int radialManhattanTwice
    ) {
    }
}
