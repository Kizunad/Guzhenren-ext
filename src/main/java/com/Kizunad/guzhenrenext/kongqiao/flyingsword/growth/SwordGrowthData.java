package com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality.SwordQuality;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * 飞剑成长数据类。
 * <p>
 * 封装飞剑的品质、等级、经验状态及相关操作：
 * <ul>
 *     <li>状态存储：品质、等级、当前经验、累计经验</li>
 *     <li>经验操作：添加经验、自动升级</li>
 *     <li>突破操作：品质提升</li>
 *     <li>属性查询：通过 {@link SwordStatCalculator} 获取当前属性</li>
 *     <li>NBT 序列化</li>
 * </ul>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 创建新飞剑（凡品1级）
 * SwordGrowthData data = SwordGrowthData.create(SwordQuality.COMMON);
 *
 * // 添加经验（自动升级）
 * SwordGrowthData.ExpAddResult result = data.addExperience(500);
 * if (result.levelsGained > 0) {
 *     // 处理升级事件
 * }
 *
 * // 获取当前属性
 * SwordStatCalculator.SwordStats stats = data.getCurrentStats();
 *
 * // 尝试突破
 * SwordExpCalculator.BreakthroughResult btResult = data.tryBreakthrough();
 * if (btResult.success) {
 *     // 处理突破成功
 * }
 * </pre>
 * </p>
 */
public class SwordGrowthData {

    // ==================== NBT 键名 ====================

    private static final String TAG_QUALITY = "Quality";
    private static final String TAG_LEVEL = "Level";
    private static final String TAG_EXPERIENCE = "Experience";
    private static final String TAG_TOTAL_EXPERIENCE = "TotalExperience";

    // ==================== 字段 ====================

    /** 飞剑品质 */
    private SwordQuality quality;

    /** 当前等级（1-based） */
    private int level;

    /** 当前经验（本级内） */
    private int experience;

    /** 累计经验（用于统计） */
    private long totalExperience;

    // ==================== 构造 ====================

    /**
     * 私有构造，请使用静态工厂方法。
     */
    private SwordGrowthData() {
        this.quality = SwordQuality.COMMON;
        this.level = 1;
        this.experience = 0;
        this.totalExperience = 0;
    }

    /**
     * 创建新的飞剑成长数据。
     *
     * @param quality 初始品质
     * @return 成长数据实例
     */
    public static SwordGrowthData create(@Nullable SwordQuality quality) {
        SwordGrowthData data = new SwordGrowthData();
        data.quality = quality != null ? quality : SwordQuality.COMMON;
        return data;
    }

    /**
     * 创建指定等级的飞剑成长数据。
     *
     * @param quality 初始品质
     * @param level   初始等级
     * @return 成长数据实例
     */
    public static SwordGrowthData create(@Nullable SwordQuality quality, int level) {
        SwordGrowthData data = create(quality);
        data.level = Math.max(1, Math.min(level, data.quality.getMaxLevel()));
        return data;
    }

    /**
     * 从 NBT 反序列化。
     *
     * @param tag NBT 标签
     * @return 成长数据实例
     */
    public static SwordGrowthData fromNBT(@Nullable CompoundTag tag) {
        SwordGrowthData data = new SwordGrowthData();
        if (tag == null) {
            return data;
        }

        // 品质
        if (tag.contains(TAG_QUALITY)) {
            data.quality = SwordQuality.fromName(tag.getString(TAG_QUALITY));
        }

        // 等级
        if (tag.contains(TAG_LEVEL)) {
            data.level = Math.max(1, tag.getInt(TAG_LEVEL));
        }

        // 经验
        if (tag.contains(TAG_EXPERIENCE)) {
            data.experience = Math.max(0, tag.getInt(TAG_EXPERIENCE));
        }

        // 累计经验
        if (tag.contains(TAG_TOTAL_EXPERIENCE)) {
            data.totalExperience = Math.max(0, tag.getLong(TAG_TOTAL_EXPERIENCE));
        }

        // 验证等级不超过品质上限
        data.level = Math.min(data.level, data.quality.getMaxLevel());

        return data;
    }

    // ==================== Getter ====================

    /**
     * 获取飞剑品质。
     */
    public SwordQuality getQuality() {
        return quality;
    }

    /**
     * 获取当前等级。
     */
    public int getLevel() {
        return level;
    }

    /**
     * 获取当前经验（本级内）。
     */
    public int getExperience() {
        return experience;
    }

    /**
     * 获取累计经验。
     */
    public long getTotalExperience() {
        return totalExperience;
    }

    /**
     * 获取升到下一级所需经验。
     */
    public int getExpToNextLevel() {
        return SwordExpCalculator.calculateExpToNextLevel(level, quality);
    }

    /**
     * 获取当前等级进度（0.0 ~ 1.0）。
     */
    public double getLevelProgress() {
        return SwordExpCalculator.calculateLevelProgress(experience, level, quality);
    }

    /**
     * 检查是否达到当前品质的最大等级。
     */
    public boolean isMaxLevelForQuality() {
        return level >= quality.getMaxLevel();
    }

    /**
     * 检查是否可以突破到下一品质。
     */
    public boolean canBreakthrough() {
        return quality.canBreakthrough(level);
    }

    /**
     * 获取突破所需经验。
     *
     * @return 突破经验，不可突破返回 -1
     */
    public int getBreakthroughExpRequired() {
        return SwordExpCalculator.calculateBreakthroughExp(quality);
    }

    // ==================== 属性查询 ====================

    /**
     * 获取当前完整属性快照。
     */
    public SwordStatCalculator.SwordStats getCurrentStats() {
        return SwordStatCalculator.calculateFullStats(quality, level);
    }

    /**
     * 获取当前伤害值。
     */
    public double getCurrentDamage() {
        return SwordStatCalculator.calculateDamage(quality, level);
    }

    /**
     * 获取当前最大速度。
     */
    public double getCurrentSpeedMax() {
        return SwordStatCalculator.calculateSpeedMax(quality, level);
    }

    /**
     * 获取当前战力评级。
     */
    public double getCurrentPowerRating() {
        return SwordStatCalculator.calculatePowerRating(quality, level);
    }

    // ==================== 经验操作 ====================

    /**
     * 经验添加结果。
     */
    public static class ExpAddResult {
        /** 实际添加的经验 */
        public final int expAdded;
        /** 升了几级 */
        public final int levelsGained;
        /** 新等级 */
        public final int newLevel;
        /** 剩余经验 */
        public final int newExp;
        /** 是否达到等级上限 */
        public final boolean hitMaxLevel;

        public ExpAddResult(int expAdded, int levelsGained, int newLevel, int newExp, boolean hitMaxLevel) {
            this.expAdded = expAdded;
            this.levelsGained = levelsGained;
            this.newLevel = newLevel;
            this.newExp = newExp;
            this.hitMaxLevel = hitMaxLevel;
        }
    }

    /**
     * 添加经验（自动升级）。
     *
     * @param amount 经验量
     * @return 添加结果
     */
    public ExpAddResult addExperience(int amount) {
        if (amount <= 0) {
            return new ExpAddResult(0, 0, level, experience, isMaxLevelForQuality());
        }

        // 限制单次添加量
        int capped = Math.min(amount, SwordGrowthTuning.EXP_GAIN_CAP);

        // 累计经验
        totalExperience = Math.min(
                totalExperience + capped,
                SwordGrowthTuning.EXP_ABSOLUTE_CAP
        );

        // 计算升级
        int oldLevel = level;
        SwordExpCalculator.LevelUpResult result = SwordExpCalculator.calculateLevelUp(
                level, experience, capped, quality
        );

        // 应用结果
        level = result.newLevel;
        experience = result.remainingExp;

        return new ExpAddResult(
                capped,
                result.levelsGained,
                level,
                experience,
                result.hitMaxLevel
        );
    }

    /**
     * 设置经验（直接覆盖，不触发升级）。
     * <p>
     * 谨慎使用，通常仅用于调试或数据恢复。
     * </p>
     *
     * @param exp 新经验值
     */
    public void setExperienceRaw(int exp) {
        this.experience = Math.max(0, exp);
    }

    /**
     * 设置等级（直接覆盖）。
     * <p>
     * 谨慎使用，通常仅用于调试或数据恢复。
     * </p>
     *
     * @param newLevel 新等级
     */
    public void setLevelRaw(int newLevel) {
        this.level = Math.max(1, Math.min(newLevel, quality.getMaxLevel()));
    }

    // ==================== 突破操作 ====================

    /**
     * 尝试突破到下一品质。
     *
     * @return 突破结果
     */
    public SwordExpCalculator.BreakthroughResult tryBreakthrough() {
        SwordExpCalculator.BreakthroughResult result = SwordExpCalculator.tryBreakthrough(
                quality, level, experience
        );

        if (result.success) {
            // 应用突破结果
            this.quality = result.newQuality;
            this.level = result.newLevel;
            this.experience = result.remainingExp;
        }

        return result;
    }

    /**
     * 强制设置品质（不消耗经验）。
     * <p>
     * 谨慎使用，通常仅用于调试或 GM 命令。
     * </p>
     *
     * @param newQuality 新品质
     */
    public void setQualityRaw(SwordQuality newQuality) {
        if (newQuality == null) {
            return;
        }
        this.quality = newQuality;
        // 确保等级不超过新品质上限
        this.level = Math.min(this.level, newQuality.getMaxLevel());
    }

    // ==================== 序列化 ====================

    /**
     * 序列化为 NBT。
     *
     * @return NBT 标签
     */
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_QUALITY, quality.name());
        tag.putInt(TAG_LEVEL, level);
        tag.putInt(TAG_EXPERIENCE, experience);
        tag.putLong(TAG_TOTAL_EXPERIENCE, totalExperience);
        return tag;
    }

    /**
     * 创建深拷贝。
     *
     * @return 新实例
     */
    public SwordGrowthData copy() {
        return fromNBT(toNBT());
    }

    // ==================== 显示相关 ====================

    /**
     * 获取品质+等级的简短显示。
     * <p>
     * 格式：[品质] Lv.XX
     * </p>
     */
    public MutableComponent getShortDisplay() {
        return quality.getBadge()
                .append(Component.literal(String.format(" Lv.%d", level)));
    }

    /**
     * 获取完整状态显示。
     * <p>
     * 包含品质、等级、经验进度。
     * </p>
     */
    public MutableComponent getFullDisplay() {
        int expToNext = getExpToNextLevel();
        String expStr = expToNext == Integer.MAX_VALUE
                ? "MAX"
                : String.format("%d/%d", experience, expToNext);

        return quality.getDisplayComponent()
                .append(Component.literal(String.format(" Lv.%d [%s]", level, expStr)));
    }

    /**
     * 获取属性摘要。
     */
    public String getStatsSummary() {
        SwordStatCalculator.SwordStats stats = getCurrentStats();
        return String.format(
                "伤害:%.1f 速度:%.2f 耐久:%.0f 战力:%s",
                stats.damage,
                stats.speedMax,
                stats.maxDurability,
                SwordStatCalculator.formatPowerRating(getCurrentPowerRating())
        );
    }

    // ==================== Object 方法 ====================

    @Override
    public String toString() {
        return String.format(
                "SwordGrowthData{quality=%s, level=%d, exp=%d/%d, totalExp=%d}",
                quality.name(),
                level,
                experience,
                getExpToNextLevel(),
                totalExperience
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SwordGrowthData other)) {
            return false;
        }
        return quality == other.quality
                && level == other.level
                && experience == other.experience
                && totalExperience == other.totalExperience;
    }

    @Override
    public int hashCode() {
        int result = quality.hashCode();
        result = 31 * result + level;
        result = 31 * result + experience;
        result = 31 * result + Long.hashCode(totalExperience);
        return result;
    }
}
