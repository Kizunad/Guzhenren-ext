package com.Kizunad.guzhenrenext.xianqiao.opening;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptStage;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BiomeInferenceServiceTests {

    private static final double BENMING_RAW_VALUE = 1.0D;
    private static final double DAO_MARK_TOTAL = 100.0D;
    private static final double RESOURCE_MAX = 100.0D;
    private static final double RESOURCE_CURRENT = 70.0D;
    private static final double TIZHI_BASE = 80.0D;
    private static final double RANK_FIVE = 5.0D;
    private static final double STAGE_FIVE = 5.0D;
    private static final double KONGQIAO_ACTIVE = 7.0D;
    private static final double TARGET_BASE = 100.0D;
    private static final double HEAVEN_QI = 32.0D;
    private static final double HEAVEN_QI_MAX = 40.0D;
    private static final double HUMAN_QI = 80.0D;
    private static final double EARTH_QI = 82.0D;

    @Test
    void benmingPriorityCanStablyShiftPrimaryBiomeUnderConflict() {
        BiomeInferenceService service = new BiomeInferenceService();
        Map<String, Double> daoMarks = new HashMap<>();
        daoMarks.put("shuidao", 55.0D);
        daoMarks.put("tudao", 45.0D);

        ResolvedOpeningProfile profile = profile(
            daoMarks,
            AscensionConditionSnapshot.BenmingGuFallbackState.RESOLVED,
            "benminggu:1"
        );

        BiomeInferenceService.BiomePreferenceResult result = service.infer(profile);

        assertEquals("tudao", result.benmingPriorityDaoKey());
        assertFalse(result.usedDefaultFallback());
        assertEquals("minecraft", result.primaryBiome().namespace());
        assertEquals("savanna", result.primaryBiome().path());
        assertEquals("minecraft:savanna", result.primaryBiomeId());
        assertEquals(result.primaryBiomeId(), result.rankedBiomeIds().getFirst());
    }

    @Test
    void daoMarkWeightingPrefersSemanticallyMatchedFamilies() {
        BiomeInferenceService service = new BiomeInferenceService();
        Map<String, Double> daoMarks = new HashMap<>();
        daoMarks.put("shuidao", 90.0D);
        daoMarks.put("mudao", 10.0D);

        ResolvedOpeningProfile profile = profile(
            daoMarks,
            AscensionConditionSnapshot.BenmingGuFallbackState.MISSING,
            "unknown"
        );

        BiomeInferenceService.BiomePreferenceResult result = service.infer(profile);

        assertTrue(
            "minecraft:swamp".equals(result.primaryBiomeId())
                || "minecraft:mangrove_swamp".equals(result.primaryBiomeId())
                || "minecraft:river".equals(result.primaryBiomeId())
        );
        assertFalse("minecraft:desert".equals(result.primaryBiomeId()));
    }

    @Test
    void tieBreakUsesDeterministicBiomeResourceKeyOrdering() {
        BiomeInferenceService service = new BiomeInferenceService();
        Map<String, Double> daoMarks = Map.of("rendao", DAO_MARK_TOTAL);
        ResolvedOpeningProfile profile = profile(
            daoMarks,
            AscensionConditionSnapshot.BenmingGuFallbackState.MISSING,
            "unknown"
        );

        BiomeInferenceService.BiomePreferenceResult first = service.infer(profile);
        BiomeInferenceService.BiomePreferenceResult second = service.infer(profile);

        assertEquals(first.primaryBiomeId(), second.primaryBiomeId());
        assertIterableEquals(first.rankedBiomeIds(), second.rankedBiomeIds());
    }

    @Test
    void emptyInputUsesStableDefaultFallbackChain() {
        BiomeInferenceService service = new BiomeInferenceService();
        ResolvedOpeningProfile profile = profile(
            Map.of(),
            AscensionConditionSnapshot.BenmingGuFallbackState.MISSING,
            "unknown"
        );

        BiomeInferenceService.BiomePreferenceResult result = service.infer(profile);

        assertTrue(result.usedDefaultFallback());
        assertEquals("minecraft", result.secondaryBiomes().getFirst().namespace());
        assertEquals("savanna", result.secondaryBiomes().getFirst().path());
        assertEquals("minecraft:desert", result.primaryBiomeId());
        assertEquals("minecraft:savanna", result.secondaryBiomeIds().getFirst());
    }

    private static ResolvedOpeningProfile profile(
        Map<String, Double> daoMarks,
        AscensionConditionSnapshot.BenmingGuFallbackState benmingState,
        String benmingToken
    ) {
        AscensionConditionSnapshot snapshot = new AscensionConditionSnapshot(
            BENMING_RAW_VALUE,
            benmingState,
            benmingToken,
            daoMarks,
            daoMarks.isEmpty()
                ? AscensionConditionSnapshot.DaoMarkCoverageState.MISSING
                : AscensionConditionSnapshot.DaoMarkCoverageState.COMPLETE,
            DAO_MARK_TOTAL,
            DAO_MARK_TOTAL,
            AscensionConditionSnapshot.AptitudeResourceState.HEALTHY,
            RESOURCE_MAX,
            RESOURCE_MAX,
            RESOURCE_CURRENT,
            RESOURCE_MAX,
            RESOURCE_CURRENT,
            RESOURCE_MAX,
            TIZHI_BASE,
            RANK_FIVE,
            STAGE_FIVE,
            KONGQIAO_ACTIVE,
            HEAVEN_QI,
            HEAVEN_QI_MAX,
            HUMAN_QI,
            HUMAN_QI,
            EARTH_QI,
            TARGET_BASE,
            TARGET_BASE,
            true
        );
        AscensionThreeQiEvaluator evaluator = new AscensionThreeQiEvaluator();
        return new ResolvedOpeningProfile(snapshot, evaluator.evaluate(snapshot), AscensionAttemptStage.CONFIRMED);
    }
}
