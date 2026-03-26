package com.Kizunad.guzhenrenext.xianqiao.opening;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 升仙开局条件快照。
 * <p>
 * 该快照是后续规划层的只读输入，目标是把“玩家实时变量”在一次解析中冻结成确定性数据，
 * 避免后续流程在不同 tick 重读 mutable 值导致结果漂移。
 * </p>
 */
public record AscensionConditionSnapshot(
    double benmingGuRawValue,
    BenmingGuFallbackState benmingGuFallbackState,
    String benmingGuToken,
    Map<String, Double> daoMarks,
    DaoMarkCoverageState daoMarkCoverageState,
    double daoMarkTotalFromPlayer,
    double daoMarkResolvedTotal,
    AptitudeResourceState aptitudeResourceState,
    double maxZhenyuan,
    double shouyuan,
    double jingli,
    double maxJingli,
    double hunpo,
    double maxHunpo,
    double tizhi,
    double zhuanshu,
    double jieduan,
    double kongqiao,
    double qiyun,
    double qiyunMax,
    double renqi,
    double fallbackHumanQi,
    double earthQi,
    double humanQiTarget,
    double earthQiTarget,
    boolean playerInitiated
) {

    /**
     * 紧凑构造器：集中做非空与不可变拷贝，确保 record 一旦构造即不可被外部修改。
     */
    public AscensionConditionSnapshot {
        if (benmingGuFallbackState == null) {
            throw new IllegalArgumentException("benmingGuFallbackState 不能为空");
        }
        if (daoMarkCoverageState == null) {
            throw new IllegalArgumentException("daoMarkCoverageState 不能为空");
        }
        if (aptitudeResourceState == null) {
            throw new IllegalArgumentException("aptitudeResourceState 不能为空");
        }
        if (benmingGuToken == null) {
            benmingGuToken = "unknown";
        }
        daoMarks = freezeDaoMarks(daoMarks);
    }

    private static Map<String, Double> freezeDaoMarks(Map<String, Double> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        TreeMap<String, Double> sorted = new TreeMap<>();
        for (Map.Entry<String, Double> entry : source.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            double value = sanitizeNonNegativeFinite(entry.getValue());
            sorted.put(entry.getKey(), value);
        }
        return Map.copyOf(new LinkedHashMap<>(sorted));
    }

    private static double sanitizeNonNegativeFinite(Double value) {
        if (value == null || !Double.isFinite(value) || value < 0.0D) {
            return 0.0D;
        }
        return value;
    }

    /**
     * 本命蛊回退状态。
     */
    public enum BenmingGuFallbackState {
        /** 本命蛊已解析到可用编码。 */
        RESOLVED,
        /** 本命蛊缺失（例如 0 或未初始化）。 */
        MISSING,
        /** 本命蛊存在但无法稳定解释。 */
        UNKNOWN
    }

    /**
     * 道痕覆盖状态。
     */
    public enum DaoMarkCoverageState {
        /** 道痕解析完整，可直接用于后续规划权重。 */
        COMPLETE,
        /** 道痕存在但仅部分可解析，必须携带显式回退语义。 */
        PARTIAL,
        /** 道痕缺失或全空。 */
        MISSING
    }

    /**
     * 资质/资源状态。
     */
    public enum AptitudeResourceState {
        /** 核心资质输入完整且可用。 */
        HEALTHY,
        /** 核心资质输入存在缺项。 */
        PARTIAL_MISSING,
        /** 核心资质输入全零或全部不可用。 */
        ALL_ZERO_OR_MISSING
    }
}
