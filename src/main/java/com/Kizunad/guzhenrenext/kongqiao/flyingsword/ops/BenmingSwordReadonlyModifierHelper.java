package com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops;

public final class BenmingSwordReadonlyModifierHelper {

    private static final double ZERO = 0.0;
    private static final double ONE = 1.0;
    private static final double QIYUN_CLAMP_MAX = 10000.0;
    private static final int REALM_CLAMP_MIN = 0;
    private static final int REALM_CLAMP_MAX = 9;
    private static final double QIYUN_REWARD_BONUS_MAX = 0.08;
    private static final double REALM_REWARD_BONUS_MAX = 0.12;
    private static final double TOTAL_REWARD_BONUS_MAX = 0.20;
    private static final double FINAL_REWARD_MULTIPLIER_MAX = 1.20;

    private BenmingSwordReadonlyModifierHelper() {}

    public static ReadonlyModifier fromSnapshot(final CultivationSnapshot snapshot) {
        if (snapshot == null) {
            return ReadonlyModifier.identity();
        }
        final double qiyunBonus = calculateQiyunRewardBonus(snapshot.qiyun());
        final double realmBonus = calculateRealmRewardBonus(snapshot.guMasterRank());
        final double physiqueBonus = calculatePhysiqueRewardBonusNoop();
        final double totalBonus = clampDouble(
            qiyunBonus + realmBonus + physiqueBonus,
            ZERO,
            TOTAL_REWARD_BONUS_MAX
        );
        final double finalRewardMultiplier = clampDouble(
            ONE + totalBonus,
            ONE,
            FINAL_REWARD_MULTIPLIER_MAX
        );
        return new ReadonlyModifier(
            ONE + qiyunBonus,
            ONE + realmBonus,
            ONE + physiqueBonus,
            finalRewardMultiplier
        );
    }

    private static double calculateQiyunRewardBonus(final double qiyun) {
        final double clampedQiyun = clampDouble(qiyun, ZERO, QIYUN_CLAMP_MAX);
        if (clampedQiyun <= ZERO) {
            return ZERO;
        }
        final double ratio = clampedQiyun / QIYUN_CLAMP_MAX;
        return ratio * QIYUN_REWARD_BONUS_MAX;
    }

    private static double calculateRealmRewardBonus(final int guMasterRank) {
        final int clampedRank = clampInt(guMasterRank, REALM_CLAMP_MIN, REALM_CLAMP_MAX);
        if (clampedRank <= REALM_CLAMP_MIN) {
            return ZERO;
        }
        final double ratio = (double) clampedRank / (double) REALM_CLAMP_MAX;
        return ratio * REALM_REWARD_BONUS_MAX;
    }

    private static double calculatePhysiqueRewardBonusNoop() {
        // TODO(Task14): 体质倍率接入必须等待稳定 bridge helper。
        // 当前阶段严格保持 no-op（中性 1.0x），避免直接读取未知字段导致语义漂移。
        return ZERO;
    }

    private static double clampDouble(final double value, final double minValue, final double maxValue) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return minValue;
        }
        return Math.max(minValue, Math.min(maxValue, value));
    }

    private static int clampInt(final int value, final int minValue, final int maxValue) {
        return Math.max(minValue, Math.min(maxValue, value));
    }

    public record ReadonlyModifier(
        double qiyunMultiplier,
        double realmMultiplier,
        double physiqueMultiplier,
        double finalMultiplier
    ) {

        public static ReadonlyModifier identity() {
            return new ReadonlyModifier(ONE, ONE, ONE, ONE);
        }

        public double applyToReward(final double baseReward) {
            if (
                baseReward <= ZERO ||
                Double.isNaN(baseReward) ||
                Double.isInfinite(baseReward)
            ) {
                return ZERO;
            }
            return baseReward * finalMultiplier;
        }

        public double applyToCost(final double baseCost) {
            if (
                baseCost <= ZERO ||
                Double.isNaN(baseCost) ||
                Double.isInfinite(baseCost)
            ) {
                return ZERO;
            }
            return baseCost;
        }
    }
}
