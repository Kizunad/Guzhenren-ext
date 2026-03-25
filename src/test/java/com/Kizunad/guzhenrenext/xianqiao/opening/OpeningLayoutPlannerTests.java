package com.Kizunad.guzhenrenext.xianqiao.opening;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OpeningLayoutPlannerTests {

    private static final int APTITUDE_1X1 = 0;

    private static final int APTITUDE_2X2 = 25;

    private static final int APTITUDE_3X3 = 50;

    private static final int APTITUDE_4X4 = 75;

    private static final int SCORE_70 = 70;

    private static final int SCORE_85 = 85;

    private static final int EXPECTED_RESERVED_CHAOS_BLOCKS = 16;

    private static final int EXPECTED_SAFEZONE_MAX_OUTSIDE_DISTANCE = 8;

    private static final int EXPECTED_WARNING_MAX_OUTSIDE_DISTANCE = 16;

    private static final int EXPECTED_LETHAL_START_OUTSIDE_DISTANCE = 17;

    private static final String BIOME_DESERT = "minecraft:desert";

    private static final String BIOME_SWAMP = "minecraft:swamp";

    private static final String BIOME_PLAINS = "minecraft:plains";

    private final OpeningLayoutPlanner planner = new OpeningLayoutPlanner();

    @Test
    void shouldMapAptitudeToExactlyFourDiscreteLayoutTiers() {
        InitialTerrainPlan plan1x1 = planner.plan(profile(APTITUDE_1X1), inference());
        InitialTerrainPlan plan2x2 = planner.plan(profile(APTITUDE_2X2), inference());
        InitialTerrainPlan plan3x3 = planner.plan(profile(APTITUDE_3X3), inference());
        InitialTerrainPlan plan4x4 = planner.plan(profile(APTITUDE_4X4), inference());

        assertEquals(InitialTerrainPlan.LayoutTier.ONE_BY_ONE, plan1x1.layoutTier());
        assertEquals(1, plan1x1.layoutSize());
        assertEquals(1, plan1x1.cellCount());

        assertEquals(InitialTerrainPlan.LayoutTier.TWO_BY_TWO, plan2x2.layoutTier());
        assertEquals(2, plan2x2.layoutSize());
        assertEquals(4, plan2x2.cellCount());

        assertEquals(InitialTerrainPlan.LayoutTier.THREE_BY_THREE, plan3x3.layoutTier());
        assertEquals(3, plan3x3.layoutSize());
        assertEquals(9, plan3x3.cellCount());

        assertEquals(InitialTerrainPlan.LayoutTier.FOUR_BY_FOUR, plan4x4.layoutTier());
        assertEquals(4, plan4x4.layoutSize());
        assertEquals(16, plan4x4.cellCount());
    }

    @Test
    void shouldUseSeamCenterForEvenLayoutsAndKeepThreeAnchorsSeparated() {
        InitialTerrainPlan plan2x2 = planner.plan(profile(APTITUDE_2X2), inference());
        InitialTerrainPlan plan4x4 = planner.plan(profile(APTITUDE_4X4), inference());

        assertEquals(InitialTerrainPlan.CoreAnchorSemantics.SEAM_CENTER, plan2x2.coreAnchor().semantics());
        assertEquals(1.0, plan2x2.coreAnchor().seamCenterChunkX());
        assertEquals(1.0, plan2x2.coreAnchor().seamCenterChunkZ());
        assertEquals(
            InitialTerrainPlan.TeleportAnchorSemantics.EVEN_SEAM_KERNEL_NORTHWEST_CELL,
            plan2x2.teleportAnchor().semantics()
        );
        assertEquals(0, plan2x2.teleportAnchor().anchorCellX());
        assertEquals(0, plan2x2.teleportAnchor().anchorCellZ());
        assertEquals(0, plan2x2.layoutOrigin().originChunkX());
        assertEquals(0, plan2x2.layoutOrigin().originChunkZ());
        assertNotEquals(plan2x2.coreAnchor().seamCenterChunkX(), plan2x2.teleportAnchor().anchorChunkCenterX());

        assertEquals(InitialTerrainPlan.CoreAnchorSemantics.SEAM_CENTER, plan4x4.coreAnchor().semantics());
        assertEquals(2.0, plan4x4.coreAnchor().seamCenterChunkX());
        assertEquals(2.0, plan4x4.coreAnchor().seamCenterChunkZ());
        assertEquals(
            InitialTerrainPlan.TeleportAnchorSemantics.EVEN_SEAM_KERNEL_NORTHWEST_CELL,
            plan4x4.teleportAnchor().semantics()
        );
        assertEquals(1, plan4x4.teleportAnchor().anchorCellX());
        assertEquals(1, plan4x4.teleportAnchor().anchorCellZ());
    }

    @Test
    void shouldGenerateDeterministicCenterOutwardOrder() {
        InitialTerrainPlan first = planner.plan(profile(APTITUDE_4X4), inference());
        InitialTerrainPlan second = planner.plan(profile(APTITUDE_4X4), inference());

        assertEquals(first, second);

        List<InitialTerrainPlan.PlannedTerrainCell> cells = first.plannedCells();
        assertEquals(16, cells.size());
        for (int index = 0; index < cells.size(); index++) {
            assertEquals(index, cells.get(index).generationOrder());
        }

        List<InitialTerrainPlan.PlannedTerrainCell> kernel = cells.stream()
            .filter(InitialTerrainPlan.PlannedTerrainCell::centerKernel)
            .toList();
        assertEquals(4, kernel.size());
        assertTrue(kernel.stream().allMatch(cell -> cell.generationOrder() < 4));

        int previousRing = -1;
        for (InitialTerrainPlan.PlannedTerrainCell cell : cells) {
            assertTrue(cell.ringIndex() >= previousRing);
            previousRing = cell.ringIndex();
        }
    }

    @Test
    void shouldExposeBoundaryBiomeCandidatesAndRingParameters() {
        InitialTerrainPlan plan = planner.plan(profile(APTITUDE_3X3), inference());

        assertEquals(0, plan.layoutOrigin().originChunkX());
        assertEquals(0, plan.layoutOrigin().originChunkZ());

        assertEquals(0, plan.initialChunkBoundary().minChunkX());
        assertEquals(2, plan.initialChunkBoundary().maxChunkX());
        assertEquals(0, plan.initialChunkBoundary().minChunkZ());
        assertEquals(2, plan.initialChunkBoundary().maxChunkZ());
        assertEquals(3, plan.initialChunkBoundary().spanXChunks());
        assertEquals(3, plan.initialChunkBoundary().spanZChunks());

        assertEquals(EXPECTED_RESERVED_CHAOS_BLOCKS, plan.ringParameters().maxReservedChaosBlocks());
        assertEquals(EXPECTED_SAFEZONE_MAX_OUTSIDE_DISTANCE, plan.ringParameters().safeZoneMaxOutsideDistanceBlocks());
        assertEquals(EXPECTED_WARNING_MAX_OUTSIDE_DISTANCE, plan.ringParameters().warningZoneMaxOutsideDistanceBlocks());
        assertEquals(EXPECTED_LETHAL_START_OUTSIDE_DISTANCE, plan.ringParameters().lethalZoneStartOutsideDistanceBlocks());

        assertEquals(BiomeInferenceService.BiomeFallbackPolicy.STABLE_HASH_POOL, plan.biomeFallbackPolicy());
        for (InitialTerrainPlan.PlannedTerrainCell cell : plan.plannedCells()) {
            assertEquals(BIOME_DESERT, cell.primaryBiomeId());
            assertEquals(List.of(BIOME_DESERT, BIOME_SWAMP, BIOME_PLAINS), cell.biomeCandidates());
            assertEquals(cell.cellX(), cell.chunkX());
            assertEquals(cell.cellZ(), cell.chunkZ());
        }
    }

    private static ResolvedOpeningProfile profile(int aptitudeScore) {
        AscensionConditionSnapshot snapshot = new AscensionConditionSnapshot(
            5,
            5,
            SCORE_70,
            SCORE_70,
            SCORE_70,
            SCORE_85,
            true,
            true
        );
        AscensionThreeQiEvaluation evaluation = new AscensionThreeQiEvaluator().evaluate(snapshot);
        return new ResolvedOpeningProfile(
            snapshot,
            evaluation,
            5,
            5,
            1,
            1,
            ResolvedOpeningProfile.BenmingGuState.RESOLVED,
            "tudao",
            10.0,
            10.0,
            ResolvedOpeningProfile.DaoMarkState.RESOLVED,
            aptitudeScore,
            ResolvedOpeningProfile.AptitudeState.RESOLVED,
            ResolvedOpeningProfile.HumanQiSource.REN_QI,
            false
        );
    }

    private static BiomeInferenceService.BiomeInferenceResult inference() {
        return new BiomeInferenceService.BiomeInferenceResult(
            List.of(
                new BiomeInferenceService.BiomePreference(BIOME_DESERT, 30.0, 10.0, 10.0, 10.0),
                new BiomeInferenceService.BiomePreference(BIOME_SWAMP, 20.0, 5.0, 10.0, 5.0)
            ),
            Optional.of(BIOME_PLAINS),
            BiomeInferenceService.BiomeFallbackPolicy.STABLE_HASH_POOL
        );
    }
}
