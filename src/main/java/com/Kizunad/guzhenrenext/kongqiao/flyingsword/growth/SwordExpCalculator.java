package com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality.SwordQuality;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.warden.Warden;

/**
 * 飞剑经验计算器。
 * <p>
 * 负责所有与经验相关的计算：
 * <ul>
 *     <li>经验获取量计算</li>
 *     <li>升级所需经验计算</li>
 *     <li>等级判定与升级检测</li>
 *     <li>突破消耗计算</li>
 * </ul>
 * </p>
 * <p>
 * 所有方法均为纯函数，无副作用，便于测试和调试。
 * </p>
 */
public final class SwordExpCalculator {

    private SwordExpCalculator() {}

    // ==================== 经验获取计算 ====================

    /**
     * 计算攻击造成的经验获取量。
     *
     * @param damage   造成的伤害
     * @param target   攻击目标（可为 null）
     * @param isKill   是否击杀
     * @param quality  飞剑品质
     * @return 获得的经验值
     */
    public static int calculateExpGain(
        double damage,
        @Nullable LivingEntity target,
        boolean isKill,
        SwordQuality quality
    ) {
        if (damage <= 0) {
            // 无伤害时给予保底经验
            return SwordGrowthTuning.EXP_MINIMUM_HIT;
        }

        // 基础经验 = 伤害 × 系数
        double baseExp = damage * SwordGrowthTuning.EXP_PER_DAMAGE;

        // 目标类型倍率
        double targetMultiplier = getTargetMultiplier(target);

        // 击杀倍率
        double killMultiplier = isKill
            ? SwordGrowthTuning.EXP_KILL_MULTIPLIER
            : 1.0;

        // 品质经验倍率
        double qualityMultiplier =
            quality != null ? quality.getExpMultiplier() : 1.0;

        // 计算最终经验
        double totalExp =
            baseExp * targetMultiplier * killMultiplier * qualityMultiplier;

        // 取整并限制范围
        int expGain = (int) Math.round(totalExp);
        expGain = Math.max(SwordGrowthTuning.EXP_GAIN_MIN, expGain);
        expGain = Math.min(SwordGrowthTuning.EXP_GAIN_CAP, expGain);

        if (SwordGrowthTuning.DEBUG_LOG_EXP_GAIN) {
            System.out.printf(
                "[SwordExp] damage=%.1f target=%s kill=%b quality=%s -> exp=%d%n",
                damage,
                target != null ? target.getType().getDescriptionId() : "null",
                isKill,
                quality != null ? quality.name() : "null",
                expGain
            );
        }

        return expGain;
    }

    /**
     * 获取目标类型的经验倍率。
     */
    private static double getTargetMultiplier(@Nullable LivingEntity target) {
        if (target == null) {
            return 1.0;
        }

        // Boss 类型
        if (isBoss(target)) {
            return SwordGrowthTuning.EXP_BOSS_MULTIPLIER;
        }

        // 精英类型
        if (isElite(target)) {
            return SwordGrowthTuning.EXP_ELITE_MULTIPLIER;
        }

        return 1.0;
    }

    /**
     * 检查是否为 Boss 实体。
     */
    private static boolean isBoss(@Nullable LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        return (
            entity instanceof WitherBoss ||
            entity instanceof EnderDragon ||
            entity instanceof Warden
        );
    }

    /**
     * 检查是否为精英实体。
     */
    private static boolean isElite(@Nullable LivingEntity entity) {
        if (entity == null) {
            return false;
        }

        // 基于最大生命值判断
        if (entity.getMaxHealth() >= SwordGrowthTuning.ELITE_HEALTH_THRESHOLD) {
            return true;
        }

        // 特定类型
        return entity instanceof ElderGuardian;
    }

    // ==================== 升级经验计算 ====================

    /**
     * 计算升到下一级所需的经验。
     * <p>
     * 公式：expToNext = EXP_BASE × (1 + level)^EXP_EXPONENT × lowLevelDiscount
     * </p>
     *
     * @param currentLevel 当前等级（1-based）
     * @param quality      飞剑品质（影响经验需求曲线）
     * @return 升到下一级所需经验
     */
    public static int calculateExpToNextLevel(
        int currentLevel,
        SwordQuality quality
    ) {
        // 已达品质上限
        if (quality != null && currentLevel >= quality.getMaxLevel()) {
            return Integer.MAX_VALUE;
        }

        // 已达绝对上限
        if (currentLevel >= SwordGrowthTuning.LEVEL_ABSOLUTE_CAP) {
            return Integer.MAX_VALUE;
        }

        // 基础公式
        double level = Math.max(1, currentLevel);
        double exp =
            SwordGrowthTuning.EXP_BASE *
            Math.pow(1 + level, SwordGrowthTuning.EXP_EXPONENT);

        // 低等级折扣
        if (currentLevel <= SwordGrowthTuning.LOW_LEVEL_THRESHOLD) {
            exp *= SwordGrowthTuning.LOW_LEVEL_DISCOUNT;
        }

        // 取整并确保至少需要 1 经验
        int expToNext = (int) Math.round(exp);
        return Math.max(1, expToNext);
    }

    /**
     * 计算达到指定等级所需的累计经验。
     *
     * @param targetLevel 目标等级
     * @param quality     飞剑品质
     * @return 累计经验总量
     */
    public static long calculateTotalExpForLevel(
        int targetLevel,
        SwordQuality quality
    ) {
        long total = 0;
        int maxLevel =
            quality != null
                ? quality.getMaxLevel()
                : SwordGrowthTuning.LEVEL_ABSOLUTE_CAP;
        int cap = Math.min(targetLevel, maxLevel);

        for (int lv = 1; lv < cap; lv++) {
            int expToNext = calculateExpToNextLevel(lv, quality);
            if (expToNext == Integer.MAX_VALUE) {
                break;
            }
            total += expToNext;
        }

        return total;
    }

    // ==================== 等级判定 ====================

    /**
     * 升级结果封装。
     */
    public static class LevelUpResult {

        /** 新等级 */
        public final int newLevel;
        /** 剩余经验（升级后留存） */
        public final int remainingExp;
        /** 升了几级 */
        public final int levelsGained;
        /** 是否达到品质上限 */
        public final boolean hitMaxLevel;

        public LevelUpResult(
            int newLevel,
            int remainingExp,
            int levelsGained,
            boolean hitMaxLevel
        ) {
            this.newLevel = newLevel;
            this.remainingExp = remainingExp;
            this.levelsGained = levelsGained;
            this.hitMaxLevel = hitMaxLevel;
        }
    }

    /**
     * 计算添加经验后的等级变化。
     * <p>
     * 支持连续升级（一次性添加大量经验时）。
     * </p>
     *
     * @param currentLevel 当前等级
     * @param currentExp   当前经验
     * @param addedExp     新增经验
     * @param quality      飞剑品质
     * @return 升级结果
     */
    public static LevelUpResult calculateLevelUp(
        int currentLevel,
        int currentExp,
        int addedExp,
        SwordQuality quality
    ) {
        int level = Math.max(1, currentLevel);
        int exp = Math.max(0, currentExp) + Math.max(0, addedExp);
        int levelsGained = 0;
        int maxLevel =
            quality != null
                ? quality.getMaxLevel()
                : SwordGrowthTuning.LEVEL_ABSOLUTE_CAP;

        // 循环检测升级
        while (level < maxLevel) {
            int expToNext = calculateExpToNextLevel(level, quality);
            if (expToNext == Integer.MAX_VALUE) {
                break;
            }

            if (exp >= expToNext) {
                exp -= expToNext;
                level++;
                levelsGained++;

                if (SwordGrowthTuning.DEBUG_LOG_LEVEL_UP) {
                    System.out.printf(
                        "[SwordExp] LEVEL UP! %d -> %d, remaining exp=%d%n",
                        level - 1,
                        level,
                        exp
                    );
                }
            } else {
                break;
            }
        }

        // 达到上限时，经验不超过最后一级所需
        boolean hitMax = level >= maxLevel;
        if (hitMax) {
            exp = Math.min(exp, calculateExpToNextLevel(maxLevel - 1, quality));
        }

        return new LevelUpResult(level, exp, levelsGained, hitMax);
    }

    // ==================== 突破计算 ====================

    /**
     * 计算突破到下一品质所需的经验。
     *
     * @param quality 当前品质
     * @return 突破所需经验，无法突破返回 -1
     */
    public static int calculateBreakthroughExp(SwordQuality quality) {
        if (quality == null || quality.isMaxQuality()) {
            return -1;
        }

        // 突破消耗 = 当前品质最后一级经验 × 突破倍率
        int lastLevelExp = calculateExpToNextLevel(
            quality.getMaxLevel() - 1,
            quality
        );
        if (lastLevelExp == Integer.MAX_VALUE) {
            return -1;
        }

        return (int) Math.round(
            lastLevelExp * SwordGrowthTuning.BREAKTHROUGH_EXP_RATIO
        );
    }

    /**
     * 突破结果封装。
     */
    public static class BreakthroughResult {

        /** 是否成功 */
        public final boolean success;
        /** 新品质 */
        public final SwordQuality newQuality;
        /** 新等级 */
        public final int newLevel;
        /** 剩余经验 */
        public final int remainingExp;
        /** 失败原因（成功时为 null） */
        public final String failReason;

        public BreakthroughResult(
            boolean success,
            SwordQuality newQuality,
            int newLevel,
            int remainingExp,
            String failReason
        ) {
            this.success = success;
            this.newQuality = newQuality;
            this.newLevel = newLevel;
            this.remainingExp = remainingExp;
            this.failReason = failReason;
        }

        public static BreakthroughResult fail(
            String reason,
            SwordQuality quality,
            int level,
            int exp
        ) {
            return new BreakthroughResult(false, quality, level, exp, reason);
        }

        public static BreakthroughResult success(
            SwordQuality newQuality,
            int newLevel,
            int remainingExp
        ) {
            return new BreakthroughResult(
                true,
                newQuality,
                newLevel,
                remainingExp,
                null
            );
        }
    }

    /**
     * 尝试突破到下一品质。
     *
     * @param currentQuality 当前品质
     * @param currentLevel   当前等级
     * @param currentExp     当前经验
     * @return 突破结果
     */
    public static BreakthroughResult tryBreakthrough(
        SwordQuality currentQuality,
        int currentLevel,
        int currentExp
    ) {
        // 检查是否已是最高品质
        if (currentQuality == null) {
            return BreakthroughResult.fail(
                "品质无效",
                SwordQuality.COMMON,
                currentLevel,
                currentExp
            );
        }
        if (currentQuality.isMaxQuality()) {
            return BreakthroughResult.fail(
                "已达最高品质",
                currentQuality,
                currentLevel,
                currentExp
            );
        }

        // 检查等级是否达标
        if (!currentQuality.canBreakthrough(currentLevel)) {
            return BreakthroughResult.fail(
                String.format(
                    "等级不足（需要Lv%d）",
                    currentQuality.getMaxLevel()
                ),
                currentQuality,
                currentLevel,
                currentExp
            );
        }

        // 检查经验是否足够
        int requiredExp = calculateBreakthroughExp(currentQuality);
        if (requiredExp < 0) {
            return BreakthroughResult.fail(
                "无法计算突破经验",
                currentQuality,
                currentLevel,
                currentExp
            );
        }
        if (currentExp < requiredExp) {
            return BreakthroughResult.fail(
                String.format(
                    "经验不足（需要%d，当前%d）",
                    requiredExp,
                    currentExp
                ),
                currentQuality,
                currentLevel,
                currentExp
            );
        }

        // 突破成功
        SwordQuality newQuality = currentQuality.getNextQuality();
        int newLevel = SwordGrowthTuning.BREAKTHROUGH_RESET_LEVEL
            ? 1
            : currentLevel;
        int remainingExp = SwordGrowthTuning.BREAKTHROUGH_RESET_EXP
            ? 0
            : (currentExp - requiredExp);

        return BreakthroughResult.success(newQuality, newLevel, remainingExp);
    }

    // ==================== 辅助方法 ====================

    /**
     * 计算当前等级进度百分比。
     *
     * @param currentExp 当前经验
     * @param level      当前等级
     * @param quality    品质
     * @return 进度百分比（0.0 ~ 1.0）
     */
    public static double calculateLevelProgress(
        int currentExp,
        int level,
        SwordQuality quality
    ) {
        int expToNext = calculateExpToNextLevel(level, quality);
        if (expToNext == Integer.MAX_VALUE || expToNext <= 0) {
            return 1.0;
        }
        return Math.min(1.0, Math.max(0.0, (double) currentExp / expToNext));
    }

    /**
     * 格式化经验进度条（文本形式）。
     *
     * @param currentExp 当前经验
     * @param level      当前等级
     * @param quality    品质
     * @param barLength  进度条长度
     * @return 进度条字符串，如 "[████░░░░░░] 40%"
     */
    public static String formatExpBar(
        int currentExp,
        int level,
        SwordQuality quality,
        int barLength
    ) {
        double progress = calculateLevelProgress(currentExp, level, quality);
        int filled = (int) Math.round(progress * barLength);

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        bar.append("] ");
        bar.append(
            String.format(
                "%.0f%%",
                progress * SwordGrowthTuning.EXP_BAR_PERCENT_MULTIPLIER
            )
        );

        return bar.toString();
    }

    /**
     * 计算距离满级还需多少经验。
     *
     * @param currentLevel 当前等级
     * @param currentExp   当前经验
     * @param quality      品质
     * @return 还需经验量
     */
    public static long calculateExpToMaxLevel(
        int currentLevel,
        int currentExp,
        SwordQuality quality
    ) {
        int maxLevel =
            quality != null
                ? quality.getMaxLevel()
                : SwordGrowthTuning.LEVEL_ABSOLUTE_CAP;

        if (currentLevel >= maxLevel) {
            return 0;
        }

        // 当前级剩余经验
        int expToNext = calculateExpToNextLevel(currentLevel, quality);
        long remaining =
            expToNext == Integer.MAX_VALUE
                ? 0
                : Math.max(0, expToNext - currentExp);

        // 后续等级累计
        for (int lv = currentLevel + 1; lv < maxLevel; lv++) {
            expToNext = calculateExpToNextLevel(lv, quality);
            if (expToNext == Integer.MAX_VALUE) {
                break;
            }
            remaining += expToNext;
        }

        return remaining;
    }
}
