package com.Kizunad.guzhenrenext.xianqiao.opening;

/**
 * 升仙条件快照。
 * <p>
 * 该快照只用于冻结 Task-1 契约输入，不读取玩家实时状态，不执行任何 world mutation。
 * </p>
 */
public record AscensionConditionSnapshot(
    int zhuanshu,
    int jieduan,
    int heavenScore,
    int earthScore,
    int humanScore,
    int balanceScore,
    boolean ascensionAttemptInitiated,
    boolean snapshotFrozen
) {

    private static final int RANK_FIVE = 5;

    private static final int PEAK_STAGE = 5;

    private static final int THREE_QI_COMPONENT_COUNT = 3;

    private static final int SCORE_MIN = 0;

    private static final int SCORE_MAX = 100;

    public AscensionConditionSnapshot {
        zhuanshu = Math.max(0, zhuanshu);
        jieduan = Math.max(0, jieduan);
        heavenScore = clampScore(heavenScore);
        earthScore = clampScore(earthScore);
        humanScore = clampScore(humanScore);
        balanceScore = clampScore(balanceScore);
    }

    public boolean isRankFivePeak() {
        return zhuanshu == RANK_FIVE && jieduan == PEAK_STAGE;
    }

    public int averageThreeQiScore() {
        return (heavenScore + earthScore + humanScore) / THREE_QI_COMPONENT_COUNT;
    }

    private static int clampScore(int score) {
        return Math.max(SCORE_MIN, Math.min(SCORE_MAX, score));
    }
}
