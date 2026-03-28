package com.Kizunad.guzhenrenext.xianqiao.opening;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptStage;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

final class OpeningLayoutPlannerTests {

    private static final double BENMING_ID = 1.0D;
    private static final double DAO_MARK_TOTAL = 100.0D;
    private static final double RANK_FIVE = 5.0D;
    private static final double STAGE_FIVE = 5.0D;
    private static final double KONGQIAO_ACTIVE = 7.0D;
    private static final double TARGET_BASE = 100.0D;
    private static final double JINGLI_CURRENT = 80.0D;
    private static final double HUNPO_CURRENT = 80.0D;
    private static final int CHUNK_BLOCK_SIZE = 16;

    private static final InitialTerrainPlan.AnchorPoint SEAM_CENTER = new InitialTerrainPlan.AnchorPoint(160, 96, -32);

    @Test
    void aptitudeMapsToExactDiscreteLayoutTiers() {
        InitialTerrainPlan oneByOne = planByAptitude(20.0D, 20.0D, 10.0D, 10.0D, 10.0D);
        InitialTerrainPlan twoByTwo = planByAptitude(60.0D, 54.0D, 45.0D, 45.0D, 45.0D);
        InitialTerrainPlan threeByThree = planByAptitude(84.0D, 78.0D, 65.0D, 65.0D, 65.0D);
        InitialTerrainPlan fourByFour = planByAptitude(120.0D, 120.0D, 100.0D, 100.0D, 100.0D);

        assertEquals(InitialTerrainPlan.LayoutTier.ONE_BY_ONE, oneByOne.layoutTier());
        assertEquals(InitialTerrainPlan.LayoutTier.TWO_BY_TWO, twoByTwo.layoutTier());
        assertEquals(InitialTerrainPlan.LayoutTier.THREE_BY_THREE, threeByThree.layoutTier());
        assertEquals(InitialTerrainPlan.LayoutTier.FOUR_BY_FOUR, fourByFour.layoutTier());
        assertEquals(1, oneByOne.cellCount());
        assertEquals(4, twoByTwo.cellCount());
        assertEquals(9, threeByThree.cellCount());
        assertEquals(16, fourByFour.cellCount());
    }

    @Test
    void twoByTwoUsesSeamCenterAndExplicitAnchorSeparation() {
        InitialTerrainPlan plan = planByAptitude(60.0D, 54.0D, 45.0D, 45.0D, 45.0D);

        assertEquals(InitialTerrainPlan.LayoutTier.TWO_BY_TWO, plan.layoutTier());
        assertEquals(SEAM_CENTER, plan.seamCenter());
        assertEquals(SEAM_CENTER, plan.coreAnchor());
        assertEquals(SEAM_CENTER.offset(0, 1, 0), plan.teleportAnchor());
        assertNotEquals(plan.layoutOrigin(), plan.seamCenter());

        InitialTerrainPlan.AnchorPoint expectedOrigin = SEAM_CENTER.offset(-CHUNK_BLOCK_SIZE, 0, -CHUNK_BLOCK_SIZE);
        assertEquals(expectedOrigin, plan.layoutOrigin());

        List<InitialTerrainPlan.AnchorPoint> orderedAnchors = plan.orderedCells().stream()
            .map(InitialTerrainPlan.PlannedCell::anchor)
            .toList();
        List<InitialTerrainPlan.AnchorPoint> expected = List.of(
            expectedOrigin,
            expectedOrigin.offset(CHUNK_BLOCK_SIZE, 0, 0),
            expectedOrigin.offset(0, 0, CHUNK_BLOCK_SIZE),
            expectedOrigin.offset(CHUNK_BLOCK_SIZE, 0, CHUNK_BLOCK_SIZE)
        );
        assertIterableEquals(expected, orderedAnchors);
    }

    @Test
    void fourByFourKernelFirstThenOuterRingIsDeterministic() {
        InitialTerrainPlan plan = planByAptitude(120.0D, 120.0D, 100.0D, 100.0D, 100.0D);

        assertEquals(InitialTerrainPlan.LayoutTier.FOUR_BY_FOUR, plan.layoutTier());
        assertEquals(SEAM_CENTER.offset(-CHUNK_BLOCK_SIZE * 2, 0, -CHUNK_BLOCK_SIZE * 2), plan.layoutOrigin());
        assertEquals(16, plan.orderedCells().size());

        List<InitialTerrainPlan.PlannedCell> ordered = plan.orderedCells();
        for (int index = 0; index < 4; index++) {
            assertEquals(0, ordered.get(index).ringOrder());
        }
        for (int index = 4; index < ordered.size(); index++) {
            assertEquals(1, ordered.get(index).ringOrder());
        }

        List<InitialTerrainPlan.AnchorPoint> kernelAnchors = ordered.subList(0, 4).stream()
            .map(InitialTerrainPlan.PlannedCell::anchor)
            .toList();
        List<InitialTerrainPlan.AnchorPoint> expectedKernel = List.of(
            SEAM_CENTER.offset(-CHUNK_BLOCK_SIZE, 0, -CHUNK_BLOCK_SIZE),
            SEAM_CENTER.offset(0, 0, -CHUNK_BLOCK_SIZE),
            SEAM_CENTER.offset(-CHUNK_BLOCK_SIZE, 0, 0),
            SEAM_CENTER.offset(0, 0, 0)
        );
        assertIterableEquals(expectedKernel, kernelAnchors);

        InitialTerrainPlan second = planByAptitude(120.0D, 120.0D, 100.0D, 100.0D, 100.0D);
        assertEquals(plan, second);
    }

    @Test
    void planContainsBoundaryZoneParametersAndBiomeReferences() {
        InitialTerrainPlan plan = planByAptitude(84.0D, 78.0D, 65.0D, 65.0D, 65.0D);

        InitialTerrainPlan.ChunkBoundary boundary = plan.initialChunkBoundary();
        assertEquals(3, boundary.widthInChunks());
        assertEquals(3, boundary.depthInChunks());

        InitialTerrainPlan.ZoneParameters zones = plan.zoneParameters();
        assertEquals(0, zones.safezoneInsetChunks());
        assertEquals(8, zones.warningBufferBlocks());
        assertEquals(16, zones.lethalBufferBlocks());
        assertEquals(16, zones.reservedChaosChunks());

        for (InitialTerrainPlan.PlannedCell cell : plan.orderedCells()) {
            assertEquals(plan.orderedCells().getFirst().biomeCandidates().size(), cell.biomeCandidates().size());
            assertEquals(
                plan.orderedCells().getFirst().biomeCandidates().getFirst(),
                cell.biomeCandidates().getFirst()
            );
        }
    }

    private static InitialTerrainPlan planByAptitude(
        double maxZhenyuan,
        double shouyuan,
        double maxJingli,
        double maxHunpo,
        double tizhi
    ) {
        OpeningLayoutPlanner planner = new OpeningLayoutPlanner();
        ResolvedOpeningProfile profile = profile(maxZhenyuan, shouyuan, maxJingli, maxHunpo, tizhi);
        BiomeInferenceService.BiomePreferenceResult biomePreference = new BiomeInferenceService().infer(profile);
        return planner.plan(profile, biomePreference, SEAM_CENTER);
    }

    private static ResolvedOpeningProfile profile(
        double maxZhenyuan,
        double shouyuan,
        double maxJingli,
        double maxHunpo,
        double tizhi
    ) {
        AscensionConditionSnapshot snapshot = new AscensionConditionSnapshot(
            BENMING_ID,
            AscensionConditionSnapshot.BenmingGuFallbackState.RESOLVED,
            "benminggu:1",
            Map.of("tudao", DAO_MARK_TOTAL),
            AscensionConditionSnapshot.DaoMarkCoverageState.COMPLETE,
            DAO_MARK_TOTAL,
            DAO_MARK_TOTAL,
            AscensionConditionSnapshot.AptitudeResourceState.HEALTHY,
            maxZhenyuan,
            shouyuan,
            JINGLI_CURRENT,
            maxJingli,
            HUNPO_CURRENT,
            maxHunpo,
            tizhi,
            RANK_FIVE,
            STAGE_FIVE,
            KONGQIAO_ACTIVE,
            32.0D,
            40.0D,
            80.0D,
            80.0D,
            82.0D,
            TARGET_BASE,
            TARGET_BASE,
            true
        );
        AscensionThreeQiEvaluator evaluator = new AscensionThreeQiEvaluator();
        return new ResolvedOpeningProfile(snapshot, evaluator.evaluate(snapshot), AscensionAttemptStage.CONFIRMED);
    }
}
