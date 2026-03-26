package com.Kizunad.guzhenrenext.xianqiao.opening;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class AscensionThreeQiEvaluatorTests {

    private static final double DELTA = 1.0E-6D;
    private static final double RANK_FIVE = 5.0D;
    private static final double STAGE_FIVE = 5.0D;
    private static final double KONGQIAO_ACTIVE = 7.0D;
    private static final double BENMING_ID = 1.0D;
    private static final double DAO_MARK_TOTAL = 120.0D;
    private static final double RESOURCE_MAX = 100.0D;
    private static final double RESOURCE_CURRENT = 80.0D;
    private static final double TIZHI_BASE = 90.0D;
    private static final double TARGET_BASE = 100.0D;

    @Test
    void readyThresholdMetButConfirmedThresholdNotMet() {
        AscensionThreeQiEvaluator evaluator = new AscensionThreeQiEvaluator();
        AscensionConditionSnapshot snapshot = snapshot(24.0D, 40.0D, 60.0D, 0.0D, 60.0D, true);

        AscensionThreeQiEvaluator.ThreeQiEvaluation evaluation = evaluator.evaluate(snapshot);

        assertTrue(evaluation.fiveTurnPeak());
        assertTrue(evaluation.readyToConfirm());
        assertFalse(evaluation.confirmedThresholdMet());
        assertFalse(evaluation.canEnterConfirmed());
        assertEquals(60.0D, evaluation.heavenScore(), DELTA);
        assertEquals(60.0D, evaluation.humanScore(), DELTA);
        assertEquals(60.0D, evaluation.earthScore(), DELTA);
    }

    @Test
    void confirmedRequiresPlayerInitiatedAndHigherThresholds() {
        AscensionThreeQiEvaluator evaluator = new AscensionThreeQiEvaluator();
        AscensionConditionSnapshot initiatedSnapshot = snapshot(32.0D, 40.0D, 80.0D, 0.0D, 82.0D, true);
        AscensionConditionSnapshot passiveSnapshot = snapshot(32.0D, 40.0D, 80.0D, 0.0D, 82.0D, false);

        AscensionThreeQiEvaluator.ThreeQiEvaluation initiated = evaluator.evaluate(initiatedSnapshot);
        AscensionThreeQiEvaluator.ThreeQiEvaluation passive = evaluator.evaluate(passiveSnapshot);

        assertTrue(initiated.confirmedThresholdMet());
        assertTrue(initiated.canEnterConfirmed());
        assertTrue(initiated.confirmedAttemptTrigger().canStartAscensionAttempt());

        assertTrue(passive.confirmedThresholdMet());
        assertFalse(passive.canEnterConfirmed());
        assertFalse(passive.confirmedAttemptTrigger().canStartAscensionAttempt());
    }

    @Test
    void humanQiCanFallbackWhenRenqiMissing() {
        AscensionThreeQiEvaluator evaluator = new AscensionThreeQiEvaluator();
        AscensionConditionSnapshot snapshot = snapshot(35.0D, 40.0D, 0.0D, 88.0D, 88.0D, true);

        AscensionThreeQiEvaluator.ThreeQiEvaluation evaluation = evaluator.evaluate(snapshot);

        assertTrue(evaluation.humanQiUsesFallback());
        assertEquals(88.0D, evaluation.humanQiValue(), DELTA);
    }

    @Test
    void heavenScoreUsesClampedQiyunMaxRange() {
        AscensionThreeQiEvaluator evaluator = new AscensionThreeQiEvaluator();
        AscensionConditionSnapshot snapshot = snapshot(20.0D, 100.0D, 80.0D, 0.0D, 80.0D, true);

        AscensionThreeQiEvaluator.ThreeQiEvaluation evaluation = evaluator.evaluate(snapshot);

        assertEquals(50.0D, evaluation.heavenScore(), DELTA);
    }

    @Test
    void fiveTurnPeakRequiresRankAndStageBothEqualFive() {
        AscensionThreeQiEvaluator evaluator = new AscensionThreeQiEvaluator();

        AscensionThreeQiEvaluator.ThreeQiEvaluation ok = evaluator.evaluate(
            snapshot(32.0D, 40.0D, 80.0D, 0.0D, 82.0D, true, RANK_FIVE, STAGE_FIVE)
        );
        AscensionThreeQiEvaluator.ThreeQiEvaluation wrongRank = evaluator.evaluate(
            snapshot(32.0D, 40.0D, 80.0D, 0.0D, 82.0D, true, 4.0D, STAGE_FIVE)
        );
        AscensionThreeQiEvaluator.ThreeQiEvaluation wrongStage = evaluator.evaluate(
            snapshot(32.0D, 40.0D, 80.0D, 0.0D, 82.0D, true, RANK_FIVE, 4.0D)
        );

        assertTrue(ok.fiveTurnPeak());
        assertFalse(wrongRank.fiveTurnPeak());
        assertFalse(wrongStage.fiveTurnPeak());
        assertFalse(wrongRank.readyToConfirm());
        assertFalse(wrongStage.canEnterConfirmed());
    }

    private static AscensionConditionSnapshot snapshot(
        double qiyun,
        double qiyunMax,
        double renqi,
        double fallbackHumanQi,
        double earthQi,
        boolean playerInitiated
    ) {
        return snapshot(qiyun, qiyunMax, renqi, fallbackHumanQi, earthQi, playerInitiated, RANK_FIVE, STAGE_FIVE);
    }

    private static AscensionConditionSnapshot snapshot(
        double qiyun,
        double qiyunMax,
        double renqi,
        double fallbackHumanQi,
        double earthQi,
        boolean playerInitiated,
        double zhuanshu,
        double jieduan
    ) {
        return new AscensionConditionSnapshot(
            BENMING_ID,
            AscensionConditionSnapshot.BenmingGuFallbackState.RESOLVED,
            "benminggu:1",
            Map.of("tudao", DAO_MARK_TOTAL),
            AscensionConditionSnapshot.DaoMarkCoverageState.COMPLETE,
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
            zhuanshu,
            jieduan,
            KONGQIAO_ACTIVE,
            qiyun,
            qiyunMax,
            renqi,
            fallbackHumanQi,
            earthQi,
            TARGET_BASE,
            TARGET_BASE,
            playerInitiated
        );
    }
}
