package com.Kizunad.guzhenrenext.bastion;

/**
 * 污染阶段枚举。
 * <p>
 * 阶段划分（闭区间/左闭右开）：
 * <ul>
 *   <li>NONE：0.0 - 0.33，表示几乎无污染</li>
 *   <li>LIGHT：0.33 - 0.66，轻度污染，开始产生额外压力</li>
 *   <li>MEDIUM：0.66 - 0.9，中度污染，明显抑制效果</li>
 *   <li>CRITICAL：0.9 - 1.0，失控污染，强烈抑制与伤害</li>
 * </ul>
 * </p>
 */
public enum PollutionStage {
    NONE(0.0D, 0.33D, 1.0D, "无污染"),
    LIGHT(0.33D, 0.66D, 1.1D, "轻度污染"),
    MEDIUM(0.66D, 0.9D, 1.25D, "中度污染"),
    CRITICAL(0.9D, 1.0D, 1.5D, "失控污染");

    private final double minInclusive;
    private final double maxInclusive;
    private final double drainMultiplier;
    private final String displayName;

    PollutionStage(double minInclusive, double maxInclusive, double drainMultiplier, String displayName) {
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
        this.drainMultiplier = drainMultiplier;
        this.displayName = displayName;
    }

    /**
     * 根据污染值返回对应阶段。
     * <p>
     * 数值将被夹取到 0.0-1.0；边界采用左闭右开，1.0 固定落在 CRITICAL。
     * </p>
     *
     * @param pollution 污染值（任意 double，将被夹取）
     * @return 对应的污染阶段
     */
    public static PollutionStage from(double pollution) {
        double clamped = Math.min(1.0D, Math.max(0.0D, pollution));
        for (PollutionStage stage : values()) {
            if (stage == CRITICAL) {
                return CRITICAL;
            }
            if (clamped >= stage.minInclusive && clamped < stage.maxInclusive) {
                return stage;
            }
        }
        return CRITICAL;
    }

    /**
     * 获取最小（含）阈值。
     */
    public double minInclusive() {
        return minInclusive;
    }

    /**
     * 获取最大（含）阈值。
     */
    public double maxInclusive() {
        return maxInclusive;
    }

    /**
     * 返回阶段对应的领域消耗倍率。
     */
    public double drainMultiplier() {
        return drainMultiplier;
    }

    /**
     * 返回中文描述名称，用于日志或后续 UI。
     */
    public String displayName() {
        return displayName;
    }
}
