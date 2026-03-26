package com.Kizunad.guzhenrenext.xianqiao.opening;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptStage;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OpeningProfileResolverTests {

    private static final double DELTA = 1.0E-6D;
    private static final double RANK_FIVE = 5.0D;
    private static final double STAGE_FIVE = 5.0D;
    private static final double RANK_FOUR = 4.0D;
    private static final double STAGE_FOUR = 4.0D;

    @Test
    void missingBenmingAndZeroAptitudeProduceExplicitFallbackStates() {
        OpeningProfileResolver resolver = new OpeningProfileResolver();
        Map<String, Double> raw = new HashMap<>();
        raw.put("benminggu", 0.0D);
        raw.put("zhuanshu", 5.0D);
        raw.put("jieduan", 5.0D);
        raw.put("kongqiao", 7.0D);
        raw.put("qiyun", 0.0D);
        raw.put("qiyun_shangxian", 0.0D);
        raw.put("renqi", 0.0D);
        raw.put("zuida_zhenyuan", 0.0D);
        raw.put("shouyuan", 0.0D);
        raw.put("jingli", 0.0D);
        raw.put("zuida_jingli", 0.0D);
        raw.put("hunpo", 0.0D);
        raw.put("zuida_hunpo", 0.0D);
        raw.put("tizhi", 0.0D);

        ResolvedOpeningProfile profile = resolver.resolveFromRawVariables(raw, 0.0D, false);

        assertEquals(
            AscensionConditionSnapshot.BenmingGuFallbackState.MISSING,
            profile.conditionSnapshot().benmingGuFallbackState()
        );
        assertEquals(
            AscensionConditionSnapshot.AptitudeResourceState.ALL_ZERO_OR_MISSING,
            profile.conditionSnapshot().aptitudeResourceState()
        );
        assertEquals(
            AscensionConditionSnapshot.DaoMarkCoverageState.MISSING,
            profile.conditionSnapshot().daoMarkCoverageState()
        );
        assertEquals(AscensionAttemptStage.QI_OBSERVATION_AND_HARMONIZATION, profile.suggestedStage());
        assertTrue(profile.threeQiEvaluation().earthQiMissing());
    }

    @Test
    void partialDaoMarksAreMarkedAsPartialInsteadOfHealthy() {
        OpeningProfileResolver resolver = new OpeningProfileResolver();
        Map<String, Double> raw = baseHealthyRaw();
        raw.put("daohen_huadao", 40.0D);
        raw.put("daohen_zong", 0.0D);

        ResolvedOpeningProfile profile = resolver.resolveFromRawVariables(raw, 75.0D, true);

        assertEquals(
            AscensionConditionSnapshot.DaoMarkCoverageState.PARTIAL,
            profile.conditionSnapshot().daoMarkCoverageState()
        );
        assertEquals(40.0D, profile.conditionSnapshot().daoMarkResolvedTotal(), DELTA);
        assertEquals(AscensionAttemptStage.QI_OBSERVATION_AND_HARMONIZATION, profile.suggestedStage());
    }

    @Test
    void snapshotIsFrozenAndNotAffectedByRawMapMutationAfterResolve() {
        OpeningProfileResolver resolver = new OpeningProfileResolver();
        Map<String, Double> raw = baseHealthyRaw();
        raw.put("daohen_huadao", 30.0D);
        raw.put("daohen_shuidao", 25.0D);
        raw.put("daohen_zong", 55.0D);

        ResolvedOpeningProfile first = resolver.resolveFromRawVariables(raw, 82.0D, true);
        raw.put("daohen_huadao", 999.0D);
        raw.put("qiyun", 0.0D);
        ResolvedOpeningProfile second = resolver.resolveFromRawVariables(raw, 82.0D, true);

        assertNotSame(first.conditionSnapshot(), second.conditionSnapshot());
        assertEquals(30.0D, first.conditionSnapshot().daoMarks().get("huadao"), DELTA);
        assertEquals(999.0D, second.conditionSnapshot().daoMarks().get("huadao"), DELTA);
        assertEquals(55.0D, first.threeQiEvaluation().heavenScore(), DELTA);
        assertEquals(0.0D, second.threeQiEvaluation().heavenScore(), DELTA);
    }

    @Test
    void unknownBenmingFractionUsesUnknownFallbackState() {
        OpeningProfileResolver resolver = new OpeningProfileResolver();
        Map<String, Double> raw = baseHealthyRaw();
        raw.put("benminggu", 12.25D);

        ResolvedOpeningProfile profile = resolver.resolveFromRawVariables(raw, 70.0D, true);

        assertEquals(
            AscensionConditionSnapshot.BenmingGuFallbackState.UNKNOWN,
            profile.conditionSnapshot().benmingGuFallbackState()
        );
        assertEquals("unknown", profile.conditionSnapshot().benmingGuToken());
    }

    @Test
    void rankFivePeakRequiresBothZhuanshuAndJieduanEqualFive() {
        OpeningProfileResolver resolver = new OpeningProfileResolver();

        Map<String, Double> ok = baseHealthyRaw();
        ok.put("zhuanshu", RANK_FIVE);
        ok.put("jieduan", STAGE_FIVE);
        ok.put("qiyun", 32.0D);
        ok.put("qiyun_shangxian", 40.0D);

        Map<String, Double> wrongRank = baseHealthyRaw();
        wrongRank.put("zhuanshu", RANK_FOUR);
        wrongRank.put("jieduan", STAGE_FIVE);
        wrongRank.put("qiyun", 32.0D);
        wrongRank.put("qiyun_shangxian", 40.0D);

        Map<String, Double> wrongStage = baseHealthyRaw();
        wrongStage.put("zhuanshu", RANK_FIVE);
        wrongStage.put("jieduan", STAGE_FOUR);
        wrongStage.put("qiyun", 32.0D);
        wrongStage.put("qiyun_shangxian", 40.0D);

        ResolvedOpeningProfile okProfile = resolver.resolveFromRawVariables(ok, 82.0D, true);
        ResolvedOpeningProfile wrongRankProfile = resolver.resolveFromRawVariables(wrongRank, 82.0D, true);
        ResolvedOpeningProfile wrongStageProfile = resolver.resolveFromRawVariables(wrongStage, 82.0D, true);

        assertTrue(okProfile.threeQiEvaluation().fiveTurnPeak());
        assertTrue(okProfile.threeQiEvaluation().canEnterConfirmed());
        assertEquals(AscensionAttemptStage.CONFIRMED, okProfile.suggestedStage());

        assertEquals(AscensionAttemptStage.CULTIVATION_PROGRESS, wrongRankProfile.suggestedStage());
        assertEquals(AscensionAttemptStage.ASCENSION_PREPARATION_UNLOCKED, wrongStageProfile.suggestedStage());
        assertFalse(wrongRankProfile.threeQiEvaluation().readyToConfirm());
        assertFalse(wrongStageProfile.threeQiEvaluation().confirmedThresholdMet());
    }

    @Test
    void explicitEarthQiFieldIsPreferredWhenPresent() {
        OpeningProfileResolver resolver = new OpeningProfileResolver();
        Map<String, Double> raw = baseHealthyRaw();
        raw.put("earthQi", 61.0D);
        raw.put("di_yu", 88.0D);

        ResolvedOpeningProfile profile = resolver.resolveFromRawVariables(raw, true);

        assertEquals(61.0D, profile.conditionSnapshot().earthQi(), DELTA);
        assertEquals(61.0D, profile.threeQiEvaluation().earthScore(), DELTA);
    }

    @Test
    void diYuActsAsV1EarthQiSourceWhenExplicitFieldMissing() {
        OpeningProfileResolver resolver = new OpeningProfileResolver();
        Map<String, Double> raw = baseHealthyRaw();
        raw.put("di_yu", 73.0D);

        ResolvedOpeningProfile profile = resolver.resolveFromRawVariables(raw, false);

        assertEquals(73.0D, profile.conditionSnapshot().earthQi(), DELTA);
        assertEquals(73.0D, profile.threeQiEvaluation().earthScore(), DELTA);
        assertFalse(profile.threeQiEvaluation().earthQiMissing());
    }

    private static Map<String, Double> baseHealthyRaw() {
        Map<String, Double> raw = new HashMap<>();
        raw.put("benminggu", 12.0D);
        raw.put("zhuanshu", 5.0D);
        raw.put("jieduan", 5.0D);
        raw.put("kongqiao", 7.0D);
        raw.put("qiyun", 22.0D);
        raw.put("qiyun_shangxian", 60.0D);
        raw.put("renqi", 80.0D);
        raw.put("zuida_zhenyuan", 120.0D);
        raw.put("shouyuan", 80.0D);
        raw.put("jingli", 70.0D);
        raw.put("zuida_jingli", 100.0D);
        raw.put("hunpo", 75.0D);
        raw.put("zuida_hunpo", 100.0D);
        raw.put("tizhi", 90.0D);
        raw.put("humanQiTarget", 100.0D);
        raw.put("earthQiTarget", 100.0D);
        return raw;
    }
}
