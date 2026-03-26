package com.Kizunad.guzhenrenext.xianqiao.opening;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptTrigger;

/**
 * 三气判定器。
 * <p>
 * 该类只做纯计算，不触碰世界状态，确保同一快照输入得到同一输出。
 * </p>
 */
public final class AscensionThreeQiEvaluator {

    private static final double SCORE_MIN = 0.0D;
    private static final double SCORE_MAX = 100.0D;
    private static final double HEAVEN_MAX_CLAMP_MIN = 20.0D;
    private static final double HEAVEN_MAX_CLAMP_MAX = 40.0D;
    private static final double READY_QI_THRESHOLD = 60.0D;
    private static final double READY_BALANCE_THRESHOLD = 75.0D;
    private static final double CONFIRMED_QI_THRESHOLD = 70.0D;
    private static final double CONFIRMED_BALANCE_THRESHOLD = 85.0D;
    private static final double HIGH_QUALITY_AVERAGE_THRESHOLD = 85.0D;
    private static final double HIGH_QUALITY_BALANCE_THRESHOLD = 90.0D;
    private static final double DEFAULT_QI_TARGET = 100.0D;
    private static final double EPSILON = 1.0E-9D;
    private static final double THREE_QI_COUNT = 3.0D;
    private static final double FIVE_TURN_PEAK_RANK = 5.0D;
    private static final double FIVE_TURN_PEAK_STAGE = 5.0D;

    /**
     * 评估快照中的天地人三气与阶段门槛。
     */
    public ThreeQiEvaluation evaluate(AscensionConditionSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot 不能为空");
        }
        double heavenDenominator = clamp(
            sanitizeNonNegativeFinite(snapshot.qiyunMax()),
            HEAVEN_MAX_CLAMP_MIN,
            HEAVEN_MAX_CLAMP_MAX
        );
        double heavenQiValue = sanitizeNonNegativeFinite(snapshot.qiyun());
        double humanQiValue = resolveHumanQi(snapshot);
        double earthQiValue = sanitizeNonNegativeFinite(snapshot.earthQi());
        double humanQiTarget = resolveTarget(snapshot.humanQiTarget());
        double earthQiTarget = resolveTarget(snapshot.earthQiTarget());

        double heavenScore = toScore(heavenQiValue, heavenDenominator);
        double humanScore = toScore(humanQiValue, humanQiTarget);
        double earthScore = toScore(earthQiValue, earthQiTarget);
        double balanceScore = computeBalanceScore(heavenScore, humanScore, earthScore);
        double averageScore = (heavenScore + humanScore + earthScore) / THREE_QI_COUNT;

        boolean fiveTurnPeak = isFiveTurnPeak(snapshot);
        boolean readyToConfirm = fiveTurnPeak
            && heavenScore >= READY_QI_THRESHOLD
            && humanScore >= READY_QI_THRESHOLD
            && earthScore >= READY_QI_THRESHOLD
            && balanceScore >= READY_BALANCE_THRESHOLD;
        boolean confirmedThresholdMet = fiveTurnPeak
            && heavenScore >= CONFIRMED_QI_THRESHOLD
            && humanScore >= CONFIRMED_QI_THRESHOLD
            && earthScore >= CONFIRMED_QI_THRESHOLD
            && balanceScore >= CONFIRMED_BALANCE_THRESHOLD;
        boolean highQualityWindow = averageScore >= HIGH_QUALITY_AVERAGE_THRESHOLD
            && balanceScore >= HIGH_QUALITY_BALANCE_THRESHOLD;

        boolean playerInitiated = snapshot.playerInitiated();
        boolean canEnterConfirmed = confirmedThresholdMet && playerInitiated;

        AscensionAttemptTrigger trigger = new AscensionAttemptTrigger(
            fiveTurnPeak,
            heavenScore >= CONFIRMED_QI_THRESHOLD,
            earthScore >= CONFIRMED_QI_THRESHOLD,
            humanScore >= CONFIRMED_QI_THRESHOLD,
            balanceScore >= CONFIRMED_BALANCE_THRESHOLD,
            playerInitiated
        );

        return new ThreeQiEvaluation(
            fiveTurnPeak,
            heavenQiValue,
            humanQiValue,
            earthQiValue,
            heavenScore,
            humanScore,
            earthScore,
            balanceScore,
            averageScore,
            readyToConfirm,
            confirmedThresholdMet,
            canEnterConfirmed,
            highQualityWindow,
            trigger,
            snapshot.renqi() <= EPSILON && humanQiValue > EPSILON,
            earthQiValue <= EPSILON
        );
    }

    private static boolean isFiveTurnPeak(AscensionConditionSnapshot snapshot) {
        return isExactFiveTurnValue(snapshot.zhuanshu(), FIVE_TURN_PEAK_RANK)
            && isExactFiveTurnValue(snapshot.jieduan(), FIVE_TURN_PEAK_STAGE);
    }

    private static boolean isExactFiveTurnValue(double value, double expected) {
        double normalized = sanitizeNonNegativeFinite(value);
        return Math.abs(normalized - expected) <= EPSILON;
    }

    private static double resolveHumanQi(AscensionConditionSnapshot snapshot) {
        double renqi = sanitizeNonNegativeFinite(snapshot.renqi());
        if (renqi > EPSILON) {
            return renqi;
        }
        return sanitizeNonNegativeFinite(snapshot.fallbackHumanQi());
    }

    private static double resolveTarget(double value) {
        double normalized = sanitizeNonNegativeFinite(value);
        if (normalized <= EPSILON) {
            return DEFAULT_QI_TARGET;
        }
        return normalized;
    }

    private static double toScore(double value, double target) {
        if (target <= EPSILON) {
            return SCORE_MIN;
        }
        return clamp((value / target) * SCORE_MAX, SCORE_MIN, SCORE_MAX);
    }

    private static double computeBalanceScore(double heavenScore, double humanScore, double earthScore) {
        double min = Math.min(heavenScore, Math.min(humanScore, earthScore));
        double max = Math.max(heavenScore, Math.max(humanScore, earthScore));
        if (max <= EPSILON) {
            return SCORE_MIN;
        }
        return clamp((min / max) * SCORE_MAX, SCORE_MIN, SCORE_MAX);
    }

    private static double sanitizeNonNegativeFinite(double value) {
        if (!Double.isFinite(value) || value < 0.0D) {
            return 0.0D;
        }
        return value;
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    /**
     * 三气评估结果。
     */
    public record ThreeQiEvaluation(
        boolean fiveTurnPeak,
        double heavenQiValue,
        double humanQiValue,
        double earthQiValue,
        double heavenScore,
        double humanScore,
        double earthScore,
        double balanceScore,
        double averageScore,
        boolean readyToConfirm,
        boolean confirmedThresholdMet,
        boolean canEnterConfirmed,
        boolean highQualityWindow,
        AscensionAttemptTrigger confirmedAttemptTrigger,
        boolean humanQiUsesFallback,
        boolean earthQiMissing
    ) {

        public ThreeQiEvaluation {
            if (confirmedAttemptTrigger == null) {
                throw new IllegalArgumentException("confirmedAttemptTrigger 不能为空");
            }
        }
    }
}
