package com.Kizunad.guzhenrenext.xianqiao.opening;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AscensionThreeQiEvaluatorTests {

    private static final int SCORE_59 = 59;

    private static final int SCORE_60 = 60;

    private static final int SCORE_69 = 69;

    private static final int SCORE_70 = 70;

    private static final int SCORE_75 = 75;

    private static final int SCORE_84 = 84;

    private static final int SCORE_85 = 85;

    private static final int SCORE_90 = 90;

    private final AscensionThreeQiEvaluator evaluator = new AscensionThreeQiEvaluator();

    @Test
    void belowReadyThresholdMustStayNotReadyEvenAtRankFivePeak() {
        AscensionConditionSnapshot snapshot = new AscensionConditionSnapshot(
            5,
            5,
            SCORE_59,
            SCORE_60,
            SCORE_60,
            SCORE_75,
            false,
            false
        );

        AscensionThreeQiEvaluation evaluation = evaluator.evaluate(snapshot);

        assertEquals(
            AscensionReadinessStage.NOT_READY,
            evaluation.stage(),
            "任一三气低于60分时必须阻断READY_TO_CONFIRM，避免把未达标状态误判成可确认"
        );
        assertFalse(evaluation.eachQiAtLeast60());
        assertFalse(evaluation.eachQiAtLeast70());
    }

    @Test
    void readyToConfirmRequiresRankFivePeakEachQiAtLeast60AndBalanceAtLeast75() {
        AscensionConditionSnapshot snapshot = new AscensionConditionSnapshot(
            5,
            5,
            SCORE_60,
            SCORE_69,
            SCORE_60,
            SCORE_75,
            false,
            false
        );

        AscensionThreeQiEvaluation evaluation = evaluator.evaluate(snapshot);

        assertEquals(
            AscensionReadinessStage.READY_TO_CONFIRM,
            evaluation.stage(),
            "READY_TO_CONFIRM 语义必须固定为五转巅峰+三气各不低于60+平衡值不低于75"
        );
        assertTrue(evaluation.rankFivePeak());
        assertTrue(evaluation.eachQiAtLeast60());
        assertFalse(
            evaluation.eachQiAtLeast70(),
            "当任一三气尚未达到70时仍只能停留在 READY_TO_CONFIRM，不能提前进入 CONFIRMED"
        );
        assertTrue(evaluation.balanceAtLeast75());
        assertFalse(evaluation.balanceAtLeast85());
        assertFalse(evaluation.ascensionAttemptInitiated());
    }

    @Test
    void rankAndThreeQiSatisfyButWithoutActiveAttemptMustNotBeConfirmed() {
        AscensionConditionSnapshot snapshot = new AscensionConditionSnapshot(
            5,
            5,
            SCORE_70,
            SCORE_70,
            SCORE_70,
            SCORE_85,
            false,
            false
        );

        AscensionThreeQiEvaluation evaluation = evaluator.evaluate(snapshot);

        assertEquals(
            AscensionReadinessStage.READY_TO_CONFIRM,
            evaluation.stage(),
            "即使三气达到CONFIRMED阈值，只要未主动发起升仙尝试，阶段也必须停留在 READY_TO_CONFIRM"
        );
        assertTrue(evaluation.eachQiAtLeast70());
        assertTrue(evaluation.balanceAtLeast85());
        assertFalse(evaluation.ascensionAttemptInitiated());
    }

    @Test
    void confirmedRequiresActiveAttemptAndFrozenInputSemantics() {
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

        AscensionThreeQiEvaluation evaluation = evaluator.evaluate(snapshot);

        assertEquals(
            AscensionReadinessStage.CONFIRMED,
            evaluation.stage(),
            "CONFIRMED 必须绑定主动发起升仙尝试语义，为后续输入快照冻结与可复算规划提供合法入口"
        );
        assertTrue(evaluation.rankFivePeak());
        assertTrue(evaluation.eachQiAtLeast70());
        assertTrue(evaluation.balanceAtLeast85());
        assertTrue(evaluation.ascensionAttemptInitiated());
    }

    @Test
    void nonPeakRankMustBlockReadyAndConfirmedRegardlessOfThreeQi() {
        AscensionConditionSnapshot snapshot = new AscensionConditionSnapshot(
            5,
            4,
            SCORE_90,
            SCORE_90,
            SCORE_90,
            SCORE_90,
            true,
            true
        );

        AscensionThreeQiEvaluation evaluation = evaluator.evaluate(snapshot);

        assertFalse(evaluation.rankFivePeak());
        assertEquals(
            AscensionReadinessStage.NOT_READY,
            evaluation.stage(),
            "五转巅峰是硬门槛，未达峰值时不得因三气高分而越过门槛"
        );
    }

    @Test
    void highQualityWindowRequiresAverageAndBalanceBothMeetThreshold() {
        AscensionConditionSnapshot highQualitySnapshot = new AscensionConditionSnapshot(
            5,
            5,
            SCORE_90,
            SCORE_90,
            SCORE_90,
            SCORE_90,
            true,
            true
        );
        AscensionConditionSnapshot averageEnoughButBalanceInsufficient = new AscensionConditionSnapshot(
            5,
            5,
            SCORE_90,
            SCORE_90,
            SCORE_90,
            SCORE_84,
            true,
            true
        );

        AscensionThreeQiEvaluation highQuality = evaluator.evaluate(highQualitySnapshot);
        AscensionThreeQiEvaluation notHighQuality = evaluator.evaluate(averageEnoughButBalanceInsufficient);

        assertTrue(
            highQuality.highQualityWindow(),
            "高品质升仙窗口必须同时满足三气均分不低于85且平衡值不低于90"
        );
        assertFalse(
            notHighQuality.highQualityWindow(),
            "只满足均分但平衡值不足时，不得标记为高品质窗口，防止奖励窗口条件被放宽"
        );
    }

    @Test
    void confirmedMustRequireSnapshotFrozen() {
        AscensionConditionSnapshot snapshot = new AscensionConditionSnapshot(
            5,
            5,
            SCORE_70,
            SCORE_70,
            SCORE_70,
            SCORE_85,
            true,
            false
        );

        AscensionThreeQiEvaluation evaluation = evaluator.evaluate(snapshot);

        assertEquals(
            AscensionReadinessStage.READY_TO_CONFIRM,
            evaluation.stage(),
            "当快照未冻结时，阶段必须停留在 READY_TO_CONFIRM，避免把可变输入误标记为 CONFIRMED"
        );
    }
}
