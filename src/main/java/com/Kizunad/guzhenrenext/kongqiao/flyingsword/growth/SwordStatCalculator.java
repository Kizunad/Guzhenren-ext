package com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality.SwordQuality;
import javax.annotation.Nullable;

/**
 * 飞剑属性计算器。
 * <p>
 * 负责基于品质和等级计算飞剑的最终属性值：
 * <ul>
 *     <li>伤害：BASE_DAMAGE × 品质伤害倍率 × 等级成长倍率</li>
 *     <li>速度：BASE_SPEED × 品质速度倍率 × 等级速度成长</li>
 *     <li>耐久：BASE_DURABILITY × 品质耐久倍率 × 等级耐久成长</li>
 *     <li>其他衍生属性</li>
 * </ul>
 * </p>
 * <p>
 * 数值设计目标（满配仙品1000级）：
 * <ul>
 *     <li>伤害：4.0 × 10.0 × (1 + 999 × 0.15) ≈ 4.0 × 10.0 × 150.85 ≈ 6034</li>
 *     <li>速度：0.55 × 3.0 × (1 + 999 × 0.15 × 0.3) ≈ 0.55 × 3.0 × 45.96 ≈ 75.8（会被上限截断）</li>
 * </ul>
 * </p>
 * <p>
 * 所有方法均为纯函数，无副作用。
 * </p>
 */
public final class SwordStatCalculator {

    private SwordStatCalculator() {}

    // ==================== 核心属性计算 ====================

    /**
     * 计算飞剑最终伤害。
     *
     * @param quality 飞剑品质
     * @param level   当前等级
     * @return 最终伤害值
     */
    public static double calculateDamage(
        @Nullable SwordQuality quality,
        int level
    ) {
        if (quality == null) {
            quality = SwordQuality.COMMON;
        }

        // 基础伤害 × 品质伤害倍率 × 等级伤害倍率
        double damage =
            SwordGrowthTuning.BASE_DAMAGE *
            quality.getTotalDamageMultiplier(level);

        // 应用上限
        damage = Math.min(damage, SwordGrowthTuning.DAMAGE_ABSOLUTE_CAP);

        if (SwordGrowthTuning.DEBUG_LOG_STAT_CALC) {
            System.out.printf(
                "[SwordStat] damage: quality=%s level=%d -> %.2f%n",
                quality.name(),
                level,
                damage
            );
        }

        return damage;
    }

    /**
     * 计算飞剑最大速度。
     *
     * @param quality 飞剑品质
     * @param level   当前等级
     * @return 最大速度（格/tick）
     */
    public static double calculateSpeedMax(
        @Nullable SwordQuality quality,
        int level
    ) {
        if (quality == null) {
            quality = SwordQuality.COMMON;
        }

        // 基础速度 × 品质速度倍率 × 等级速度倍率
        double speed =
            SwordGrowthTuning.BASE_SPEED_MAX *
            quality.getTotalSpeedMultiplier(level);

        // 应用上限
        speed = Math.min(speed, SwordGrowthTuning.SPEED_ABSOLUTE_CAP);

        if (SwordGrowthTuning.DEBUG_LOG_STAT_CALC) {
            System.out.printf(
                "[SwordStat] speedMax: quality=%s level=%d -> %.3f%n",
                quality.name(),
                level,
                speed
            );
        }

        return speed;
    }

    /**
     * 计算飞剑初始速度。
     *
     * @param quality 飞剑品质
     * @param level   当前等级
     * @return 初始速度（格/tick）
     */
    public static double calculateSpeedBase(
        @Nullable SwordQuality quality,
        int level
    ) {
        if (quality == null) {
            quality = SwordQuality.COMMON;
        }

        // 初始速度成长较慢，使用速度倍率的 0.7 倍
        double speedMult =
            1.0 +
            (quality.getTotalSpeedMultiplier(level) - 1.0) *
            SwordGrowthTuning.SPEED_BASE_GROWTH_COEF;
        double speed = SwordGrowthTuning.BASE_SPEED_BASE * speedMult;

        // 初始速度不超过最大速度的 80%
        double maxSpeed = calculateSpeedMax(quality, level);
        speed = Math.min(
            speed,
            maxSpeed * SwordGrowthTuning.SPEED_BASE_TO_MAX_RATIO
        );

        return speed;
    }

    /**
     * 计算飞剑加速度。
     *
     * @param quality 飞剑品质
     * @param level   当前等级
     * @return 加速度
     */
    public static double calculateAccel(
        @Nullable SwordQuality quality,
        int level
    ) {
        if (quality == null) {
            quality = SwordQuality.COMMON;
        }

        // 加速度成长较慢，使用速度倍率的 0.5 倍
        double speedMult = quality.getTotalSpeedMultiplier(level);
        double accelMult =
            1.0 + (speedMult - 1.0) * SwordGrowthTuning.ACCEL_GROWTH_COEF;
        double accel = SwordGrowthTuning.BASE_ACCEL * accelMult;

        // 加速度上限
        return Math.min(accel, SwordGrowthTuning.ACCEL_ABSOLUTE_CAP);
    }

    /**
     * 计算飞剑转向速率。
     *
     * @param quality 飞剑品质
     * @param level   当前等级
     * @return 转向速率（度/tick）
     */
    public static double calculateTurnRate(
        @Nullable SwordQuality quality,
        int level
    ) {
        if (quality == null) {
            quality = SwordQuality.COMMON;
        }

        // 转向速率成长较慢，使用品质基础加上等级微调
        double qualityBonus =
            1.0 +
            (quality.getTier() * SwordGrowthTuning.TURN_RATE_QUALITY_COEF);
        int clampedLevel = Math.max(1, Math.min(level, quality.getMaxLevel()));
        double levelBonus =
            1.0 + (clampedLevel - 1) * SwordGrowthTuning.TURN_RATE_LEVEL_COEF;

        double turnRate =
            SwordGrowthTuning.BASE_TURN_RATE * qualityBonus * levelBonus;

        // 转向速率上限
        return Math.min(turnRate, SwordGrowthTuning.TURN_RATE_ABSOLUTE_CAP);
    }

    /**
     * 计算飞剑最大耐久。
     *
     * @param quality 飞剑品质
     * @param level   当前等级
     * @return 最大耐久值
     */
    public static double calculateMaxDurability(
        @Nullable SwordQuality quality,
        int level
    ) {
        if (quality == null) {
            quality = SwordQuality.COMMON;
        }

        // 耐久成长使用独立公式
        // 品质加成：1.0 ~ 3.0（与速度倍率相同）
        double qualityMult = quality.getSpeedMultiplier();

        // 等级加成：每级增加 2%
        int clampedLevel = Math.max(1, Math.min(level, quality.getMaxLevel()));
        double levelMult =
            1.0 +
            (clampedLevel - 1) * SwordGrowthTuning.DURABILITY_LEVEL_GROWTH_COEF;

        double durability =
            SwordGrowthTuning.BASE_MAX_DURABILITY * qualityMult * levelMult;

        // 应用上限
        double cap =
            SwordGrowthTuning.BASE_MAX_DURABILITY *
            SwordGrowthTuning.DURABILITY_CAP_MULTIPLIER;
        return Math.min(durability, cap);
    }

    /**
     * 计算攻击冷却（tick）。
     *
     * @param quality 飞剑品质
     * @param level   当前等级
     * @return 攻击冷却（tick）
     */
    public static int calculateAttackCooldown(
        @Nullable SwordQuality quality,
        int level
    ) {
        if (quality == null) {
            quality = SwordQuality.COMMON;
        }

        // 基础冷却随品质和等级降低
        // 品质每级降低 5%
        double qualityReduction =
            1.0 -
            (quality.getTier() *
                SwordGrowthTuning.ATTACK_COOLDOWN_QUALITY_COEF);

        // 等级每 100 级降低 5%
        int clampedLevel = Math.max(1, Math.min(level, quality.getMaxLevel()));
        double levelReduction =
            1.0 -
            (clampedLevel / SwordGrowthTuning.ATTACK_COOLDOWN_LEVEL_PERIOD) *
            SwordGrowthTuning.ATTACK_COOLDOWN_QUALITY_COEF;

        int cooldown = (int) Math.round(
            SwordGrowthTuning.BASE_ATTACK_COOLDOWN *
                qualityReduction *
                levelReduction
        );

        // 冷却下限为 2 tick
        return Math.max(SwordGrowthTuning.ATTACK_COOLDOWN_MIN, cooldown);
    }

    // ==================== 完整属性快照 ====================

    /**
     * 飞剑完整属性快照。
     */
    public static class SwordStats {

        /** 品质 */
        public final SwordQuality quality;
        /** 等级 */
        public final int level;
        /** 最终伤害 */
        public final double damage;
        /** 最大速度 */
        public final double speedMax;
        /** 初始速度 */
        public final double speedBase;
        /** 加速度 */
        public final double accel;
        /** 转向速率 */
        public final double turnRate;
        /** 最大耐久 */
        public final double maxDurability;
        /** 攻击冷却 */
        public final int attackCooldown;

        /**
         * 构造属性快照。
         *
         * @param builder 构建器
         */
        private SwordStats(Builder builder) {
            this.quality = builder.quality;
            this.level = builder.level;
            this.damage = builder.damage;
            this.speedMax = builder.speedMax;
            this.speedBase = builder.speedBase;
            this.accel = builder.accel;
            this.turnRate = builder.turnRate;
            this.maxDurability = builder.maxDurability;
            this.attackCooldown = builder.attackCooldown;
        }

        /**
         * 属性快照构建器。
         */
        public static class Builder {

            private SwordQuality quality = SwordQuality.COMMON;
            private int level = 1;
            private double damage;
            private double speedMax;
            private double speedBase;
            private double accel;
            private double turnRate;
            private double maxDurability;
            private int attackCooldown;

            /**
             * 设置品质。
             */
            public Builder quality(SwordQuality quality) {
                this.quality = quality;
                return this;
            }

            /**
             * 设置等级。
             */
            public Builder level(int level) {
                this.level = level;
                return this;
            }

            /**
             * 设置伤害。
             */
            public Builder damage(double damage) {
                this.damage = damage;
                return this;
            }

            /**
             * 设置最大速度。
             */
            public Builder speedMax(double speedMax) {
                this.speedMax = speedMax;
                return this;
            }

            /**
             * 设置初始速度。
             */
            public Builder speedBase(double speedBase) {
                this.speedBase = speedBase;
                return this;
            }

            /**
             * 设置加速度。
             */
            public Builder accel(double accel) {
                this.accel = accel;
                return this;
            }

            /**
             * 设置转向速率。
             */
            public Builder turnRate(double turnRate) {
                this.turnRate = turnRate;
                return this;
            }

            /**
             * 设置最大耐久。
             */
            public Builder maxDurability(double maxDurability) {
                this.maxDurability = maxDurability;
                return this;
            }

            /**
             * 设置攻击冷却。
             */
            public Builder attackCooldown(int attackCooldown) {
                this.attackCooldown = attackCooldown;
                return this;
            }

            /**
             * 构建属性快照。
             */
            public SwordStats build() {
                return new SwordStats(this);
            }
        }

        /**
         * 创建构建器。
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * 格式化为调试字符串。
         */
        @Override
        public String toString() {
            return String.format(
                "SwordStats{quality=%s, level=%d, damage=%.2f, speedMax=%.3f, " +
                    "speedBase=%.3f, accel=%.4f, turnRate=%.1f, maxDura=%.1f, cd=%d}",
                quality.name(),
                level,
                damage,
                speedMax,
                speedBase,
                accel,
                turnRate,
                maxDurability,
                attackCooldown
            );
        }
    }

    /**
     * 计算完整属性快照。
     *
     * @param quality 飞剑品质
     * @param level   当前等级
     * @return 完整属性快照
     */
    public static SwordStats calculateFullStats(
        @Nullable SwordQuality quality,
        int level
    ) {
        if (quality == null) {
            quality = SwordQuality.COMMON;
        }

        return SwordStats.builder()
            .quality(quality)
            .level(level)
            .damage(calculateDamage(quality, level))
            .speedMax(calculateSpeedMax(quality, level))
            .speedBase(calculateSpeedBase(quality, level))
            .accel(calculateAccel(quality, level))
            .turnRate(calculateTurnRate(quality, level))
            .maxDurability(calculateMaxDurability(quality, level))
            .attackCooldown(calculateAttackCooldown(quality, level))
            .build();
    }

    // ==================== 属性比较 ====================

    /**
     * 计算两个属性快照的差异。
     *
     * @param before 变化前
     * @param after  变化后
     * @return 差异描述
     */
    public static String formatStatDiff(SwordStats before, SwordStats after) {
        StringBuilder sb = new StringBuilder();

        if (before.quality != after.quality) {
            sb.append(
                String.format(
                    "品质: %s → %s\n",
                    before.quality.getDisplayName(),
                    after.quality.getDisplayName()
                )
            );
        }

        if (before.level != after.level) {
            sb.append(
                String.format("等级: Lv%d → Lv%d\n", before.level, after.level)
            );
        }

        appendDoubleDiff(sb, "伤害", before.damage, after.damage, "%.1f");
        appendDoubleDiff(sb, "速度", before.speedMax, after.speedMax, "%.3f");
        appendDoubleDiff(
            sb,
            "耐久",
            before.maxDurability,
            after.maxDurability,
            "%.0f"
        );
        appendIntDiff(sb, "冷却", before.attackCooldown, after.attackCooldown);

        return sb.toString();
    }

    /**
     * 添加浮点数差异描述。
     */
    private static void appendDoubleDiff(
        StringBuilder sb,
        String name,
        double before,
        double after,
        String format
    ) {
        if (Math.abs(after - before) > SwordGrowthTuning.STAT_COMPARE_EPSILON) {
            double diff = after - before;
            String sign = diff > 0 ? "+" : "";
            sb.append(
                String.format(
                    "%s: " + format + " → " + format + " (%s" + format + ")\n",
                    name,
                    before,
                    after,
                    sign,
                    diff
                )
            );
        }
    }

    /**
     * 添加整数差异描述。
     */
    private static void appendIntDiff(
        StringBuilder sb,
        String name,
        int before,
        int after
    ) {
        if (after != before) {
            int diff = after - before;
            String sign = diff > 0 ? "+" : "";
            sb.append(
                String.format(
                    "%s: %d → %d (%s%d)\n",
                    name,
                    before,
                    after,
                    sign,
                    diff
                )
            );
        }
    }

    // ==================== 战力评估 ====================

    /**
     * 计算飞剑综合战力值。
     * <p>
     * 用于排序、匹配等场景的简化评估值。
     * </p>
     *
     * @param quality 飞剑品质
     * @param level   当前等级
     * @return 战力值（范围约 10 ~ 100,000）
     */
    public static double calculatePowerRating(
        @Nullable SwordQuality quality,
        int level
    ) {
        if (quality == null) {
            quality = SwordQuality.COMMON;
        }

        SwordStats stats = calculateFullStats(quality, level);

        // 战力公式：伤害占主导，速度和耐久作为系数
        double damageFactor = stats.damage;
        double speedFactor =
            1.0 + (stats.speedMax - SwordGrowthTuning.BASE_SPEED_MAX);
        double durabilityFactor = Math.sqrt(
            stats.maxDurability / SwordGrowthTuning.BASE_MAX_DURABILITY
        );

        // 综合战力
        double power = damageFactor * speedFactor * durabilityFactor;

        // 品质额外加成（高品质有隐性收益）
        power *= (1.0 +
            quality.getTier() * SwordGrowthTuning.POWER_RATING_QUALITY_COEF);

        return power;
    }

    /**
     * 格式化战力值显示。
     *
     * @param power 战力值
     * @return 格式化字符串（如 "12.3K", "1.5M"）
     */
    public static String formatPowerRating(double power) {
        if (power >= SwordGrowthTuning.POWER_DISPLAY_MILLION) {
            return String.format(
                "%.1fM",
                power / SwordGrowthTuning.POWER_DISPLAY_MILLION
            );
        } else if (power >= SwordGrowthTuning.POWER_DISPLAY_THOUSAND) {
            return String.format(
                "%.1fK",
                power / SwordGrowthTuning.POWER_DISPLAY_THOUSAND
            );
        } else {
            return String.format("%.0f", power);
        }
    }
}
