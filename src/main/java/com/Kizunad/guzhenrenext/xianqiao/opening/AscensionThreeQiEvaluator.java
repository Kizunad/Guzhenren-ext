package com.Kizunad.guzhenrenext.xianqiao.opening;

/**
 * 三气判定契约评估器。
 * <p>
 * 该类只负责把冻结阈值映射为阶段结果，不读取运行时实体状态，不执行世界修改。
 * </p>
 */
public final class AscensionThreeQiEvaluator {

    private static final int READY_QI_THRESHOLD = 60;

    private static final int CONFIRMED_QI_THRESHOLD = 70;

    private static final int READY_BALANCE_THRESHOLD = 75;

    private static final int CONFIRMED_BALANCE_THRESHOLD = 85;

    private static final int HIGH_QUALITY_AVERAGE_THRESHOLD = 85;

    private static final int HIGH_QUALITY_BALANCE_THRESHOLD = 90;

    public AscensionThreeQiEvaluation evaluate(AscensionConditionSnapshot snapshot) {
        boolean rankFivePeak = snapshot.isRankFivePeak();
        boolean eachQiAtLeast60 = allAtLeast(snapshot, READY_QI_THRESHOLD);
        boolean eachQiAtLeast70 = allAtLeast(snapshot, CONFIRMED_QI_THRESHOLD);
        boolean balanceAtLeast75 = snapshot.balanceScore() >= READY_BALANCE_THRESHOLD;
        boolean balanceAtLeast85 = snapshot.balanceScore() >= CONFIRMED_BALANCE_THRESHOLD;
        boolean readyToConfirm = rankFivePeak && eachQiAtLeast60 && balanceAtLeast75;
        boolean confirmed = rankFivePeak
            && eachQiAtLeast70
            && balanceAtLeast85
            && snapshot.ascensionAttemptInitiated()
            && snapshot.snapshotFrozen();

        AscensionReadinessStage stage = AscensionReadinessStage.NOT_READY;
        if (readyToConfirm) {
            stage = AscensionReadinessStage.READY_TO_CONFIRM;
        }
        if (confirmed) {
            stage = AscensionReadinessStage.CONFIRMED;
        }

        boolean highQualityWindow = snapshot.averageThreeQiScore() >= HIGH_QUALITY_AVERAGE_THRESHOLD
            && snapshot.balanceScore() >= HIGH_QUALITY_BALANCE_THRESHOLD;

        return new AscensionThreeQiEvaluation(
            stage,
            rankFivePeak,
            eachQiAtLeast60,
            eachQiAtLeast70,
            balanceAtLeast75,
            balanceAtLeast85,
            highQualityWindow,
            snapshot.ascensionAttemptInitiated()
        );
    }

    private static boolean allAtLeast(AscensionConditionSnapshot snapshot, int threshold) {
        return snapshot.heavenScore() >= threshold
            && snapshot.earthScore() >= threshold
            && snapshot.humanScore() >= threshold;
    }
}
