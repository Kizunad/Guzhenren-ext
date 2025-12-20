package com.Kizunad.guzhenrenext.kongqiao.logic.util;

/**
 * 道痕倍率辅助：用于“非伤害类”效果的倍率裁剪与数值缩放。
 * <p>
 * 伤害倍率允许非常大（用于满足高阶强度扩张），但控制类/持续类效果若不裁剪，
 * 容易出现超长持续时间或过高属性增幅，导致体验与平衡崩坏。
 * </p>
 */
public final class DaoHenEffectScalingHelper {

    private static final double MIN_MULTIPLIER = 0.25;
    private static final double MAX_MULTIPLIER = 5.0;

    private static final int TICKS_PER_SECOND = 20;
    private static final int MAX_DURATION_SECONDS = 120;
    private static final int MAX_DURATION_TICKS = TICKS_PER_SECOND
        * MAX_DURATION_SECONDS;

    private DaoHenEffectScalingHelper() {}

    /**
     * 裁剪倍率，避免非伤害类效果离谱膨胀或过度衰减。
     */
    public static double clampMultiplier(final double multiplier) {
        return UsageMetadataHelper.clamp(multiplier, MIN_MULTIPLIER, MAX_MULTIPLIER);
    }

    /**
     * 缩放持续时间（tick），并做上限保护。
     */
    public static int scaleDurationTicks(
        final int baseDurationTicks,
        final double multiplier
    ) {
        if (baseDurationTicks <= 0) {
            return 0;
        }
        final double m = clampMultiplier(multiplier);
        final long scaled = Math.round(baseDurationTicks * m);
        final long capped = Math.min(scaled, (long) MAX_DURATION_TICKS);
        if (capped <= 0) {
            return 0;
        }
        return (int) capped;
    }

    /**
     * 缩放一般数值（不做上限处理，调用方可自行 clamp）。
     */
    public static double scaleValue(final double baseValue, final double multiplier) {
        return baseValue * clampMultiplier(multiplier);
    }

    /**
     * 缩放概率并自动夹紧到 [0,1]。
     */
    public static double scaleChance(final double baseChance, final double multiplier) {
        return UsageMetadataHelper.clamp(
            baseChance * clampMultiplier(multiplier),
            0.0,
            1.0
        );
    }
}

