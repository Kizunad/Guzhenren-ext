package com.Kizunad.guzhenrenext.xianqiao.opening;

import java.util.Objects;

/**
 * 开窍画像解析结果。
 * <p>
 * 该类型是 Task-2 的冻结输出，后续规划层只能读取本 record，
 * 不能再回源读取实时 PlayerVariables。
 * </p>
 */
public record ResolvedOpeningProfile(
    AscensionConditionSnapshot ascensionConditionSnapshot,
    AscensionThreeQiEvaluation threeQiEvaluation,
    int zhuanshu,
    int jieduan,
    int kongqiao,
    int benmingGuCode,
    BenmingGuState benmingGuState,
    String dominantDaoMark,
    double dominantDaoMarkValue,
    double totalDaoMarkValue,
    DaoMarkState daoMarkState,
    int aptitudeScore,
    AptitudeState aptitudeState,
    HumanQiSource humanQiSource,
    boolean earthQiFallbackApplied
) {

    private static final int SCORE_MIN = 0;

    private static final int SCORE_MAX = 100;

    private static final String DEFAULT_DAO_MARK = "generic";

    public ResolvedOpeningProfile {
        Objects.requireNonNull(ascensionConditionSnapshot, "ascensionConditionSnapshot");
        Objects.requireNonNull(threeQiEvaluation, "threeQiEvaluation");
        Objects.requireNonNull(benmingGuState, "benmingGuState");
        Objects.requireNonNull(daoMarkState, "daoMarkState");
        Objects.requireNonNull(aptitudeState, "aptitudeState");
        Objects.requireNonNull(humanQiSource, "humanQiSource");
        zhuanshu = Math.max(0, zhuanshu);
        jieduan = Math.max(0, jieduan);
        kongqiao = Math.max(0, kongqiao);
        dominantDaoMark = normalizeDaoMark(dominantDaoMark);
        dominantDaoMarkValue = sanitizeNonNegative(dominantDaoMarkValue);
        totalDaoMarkValue = sanitizeNonNegative(totalDaoMarkValue);
        aptitudeScore = clampScore(aptitudeScore);
    }

    private static String normalizeDaoMark(String daoMark) {
        if (daoMark == null || daoMark.isBlank()) {
            return DEFAULT_DAO_MARK;
        }
        return daoMark;
    }

    private static double sanitizeNonNegative(double value) {
        if (!Double.isFinite(value) || value <= 0.0) {
            return 0.0;
        }
        return value;
    }

    private static int clampScore(int score) {
        return Math.max(SCORE_MIN, Math.min(SCORE_MAX, score));
    }

    public enum BenmingGuState {
        RESOLVED,
        UNKNOWN_FALLBACK
    }

    public enum DaoMarkState {
        RESOLVED,
        SPARSE_FALLBACK
    }

    public enum AptitudeState {
        RESOLVED,
        ALL_ZERO_FALLBACK
    }

    public enum HumanQiSource {
        REN_QI,
        HUMAN_QI_FALLBACK,
        MISSING_FALLBACK
    }
}
