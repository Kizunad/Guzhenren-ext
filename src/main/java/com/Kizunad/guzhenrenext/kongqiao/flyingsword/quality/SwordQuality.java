package com.Kizunad.guzhenrenext.kongqiao.flyingsword.quality;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth.SwordGrowthTuning;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/**
 * 飞剑品质枚举。
 * <p>
 * 品质决定飞剑的基础属性倍率和成长潜力。
 * 共分为9个品质等级，从低到高：
 * <ul>
 *     <li>凡品 (COMMON) - 最基础的飞剑</li>
 *     <li>灵品 (SPIRIT) - 蕴含微弱灵气</li>
 *     <li>玄品 (MYSTIC) - 较为罕见</li>
 *     <li>地品 (EARTH) - 地脉灵力加持</li>
 *     <li>天品 (HEAVEN) - 天地共鸣</li>
 *     <li>王品 (KING) - 王者之剑</li>
 *     <li>皇品 (EMPEROR) - 皇道至尊</li>
 *     <li>圣品 (SAINT) - 圣人所铸</li>
 *     <li>仙品 (IMMORTAL) - 仙家至宝</li>
 * </ul>
 * </p>
 * <p>
 * 数值范围说明：
 * <ul>
 *     <li>基础伤害倍率：1.0x ~ 10.0x</li>
 *     <li>速度倍率：1.0x ~ 3.0x</li>
 *     <li>最大等级上限：100 ~ 1000</li>
 *     <li>最大经验需求：~100,000</li>
 * </ul>
 * </p>
 */
public enum SwordQuality {
    // ===== 品质定义 =====
    // 参数：displayName, colorCode, damageMultiplier, speedMultiplier, maxLevel, expMultiplier

    /**
     * 凡品 - 最基础的飞剑
     */
    COMMON(
        "凡品",
        ChatFormatting.GRAY,
        1.0, // 伤害倍率
        1.0, // 速度倍率
        100, // 最大等级
        1.0, // 经验获取倍率
        0.05 // 成长系数（每级属性增长比例）
    ),

    /**
     * 灵品 - 蕴含微弱灵气
     */
    SPIRIT("灵品", ChatFormatting.WHITE, 1.3, 1.1, 150, 1.1, 0.06),

    /**
     * 玄品 - 较为罕见
     */
    MYSTIC("玄品", ChatFormatting.GREEN, 1.7, 1.2, 200, 1.2, 0.07),

    /**
     * 地品 - 地脉灵力加持
     */
    EARTH("地品", ChatFormatting.AQUA, 2.2, 1.35, 300, 1.35, 0.08),

    /**
     * 天品 - 天地共鸣
     */
    HEAVEN("天品", ChatFormatting.BLUE, 3.0, 1.5, 400, 1.5, 0.09),

    /**
     * 王品 - 王者之剑
     */
    KING("王品", ChatFormatting.LIGHT_PURPLE, 4.0, 1.7, 500, 1.7, 0.10),

    /**
     * 皇品 - 皇道至尊
     */
    EMPEROR("皇品", ChatFormatting.YELLOW, 5.5, 2.0, 650, 2.0, 0.11),

    /**
     * 圣品 - 圣人所铸
     */
    SAINT("圣品", ChatFormatting.GOLD, 7.5, 2.5, 800, 2.5, 0.12),

    /**
     * 仙品 - 仙家至宝
     */
    IMMORTAL("仙品", ChatFormatting.RED, 10.0, 3.0, 1000, 3.0, 0.15);

    // ===== 字段 =====

    private final String displayName;
    private final ChatFormatting color;
    private final double damageMultiplier;
    private final double speedMultiplier;
    private final int maxLevel;
    private final double expMultiplier;
    private final double growthCoefficient;

    // ===== 构造 =====

    SwordQuality(
        String displayName,
        ChatFormatting color,
        double damageMultiplier,
        double speedMultiplier,
        int maxLevel,
        double expMultiplier,
        double growthCoefficient
    ) {
        this.displayName = displayName;
        this.color = color;
        this.damageMultiplier = damageMultiplier;
        this.speedMultiplier = speedMultiplier;
        this.maxLevel = maxLevel;
        this.expMultiplier = expMultiplier;
        this.growthCoefficient = growthCoefficient;
    }

    // ===== Getter =====

    /**
     * 获取品质显示名称。
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 获取品质对应颜色。
     */
    public ChatFormatting getColor() {
        return color;
    }

    /**
     * 获取伤害倍率。
     * <p>
     * 基础伤害 × 伤害倍率 = 品质加成后伤害
     * </p>
     */
    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    /**
     * 获取速度倍率。
     * <p>
     * 基础速度 × 速度倍率 = 品质加成后速度
     * </p>
     */
    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    /**
     * 获取该品质的最大等级上限。
     */
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * 获取经验获取倍率。
     * <p>
     * 高品质飞剑获得经验更快。
     * </p>
     */
    public double getExpMultiplier() {
        return expMultiplier;
    }

    /**
     * 获取成长系数。
     * <p>
     * 每升一级，属性提升的比例。
     * growthCoefficient = 0.05 表示每级提升 5%。
     * </p>
     */
    public double getGrowthCoefficient() {
        return growthCoefficient;
    }

    /**
     * 获取品质等级（0-8）。
     * <p>
     * 用于数值比较和存档。
     * </p>
     */
    public int getTier() {
        return ordinal();
    }

    // ===== 显示相关 =====

    /**
     * 获取带颜色的品质名称组件。
     */
    public MutableComponent getDisplayComponent() {
        return Component.literal(displayName).setStyle(
            Style.EMPTY.withColor(color)
        );
    }

    /**
     * 获取品质徽章（用于 UI 显示）。
     * <p>
     * 格式：[品质名]
     * </p>
     */
    public MutableComponent getBadge() {
        return Component.literal("[").append(getDisplayComponent()).append("]");
    }

    /**
     * 获取完整品质描述。
     */
    public MutableComponent getFullDescription() {
        return Component.literal("")
            .append(getDisplayComponent())
            .append(
                Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY)
            )
            .append(
                Component.literal(
                    String.format(
                        "伤害×%.1f 速度×%.1f 上限Lv%d",
                        damageMultiplier,
                        speedMultiplier,
                        maxLevel
                    )
                ).withStyle(ChatFormatting.GRAY)
            );
    }

    // ===== 属性计算辅助 =====

    /**
     * 计算指定等级的属性倍率。
     * <p>
     * 公式：1 + (level - 1) × growthCoefficient
     * </p>
     *
     * @param level 当前等级（1-based）
     * @return 等级带来的属性倍率
     */
    public double getLevelMultiplier(int level) {
        int clampedLevel = Math.max(1, Math.min(level, maxLevel));
        return 1.0 + (clampedLevel - 1) * growthCoefficient;
    }

    /**
     * 计算最终伤害倍率（品质 + 等级）。
     *
     * @param level 当前等级
     * @return 综合伤害倍率
     */
    public double getTotalDamageMultiplier(int level) {
        return damageMultiplier * getLevelMultiplier(level);
    }

    /**
     * 计算最终速度倍率（品质 + 等级）。
     * <p>
     * 速度成长较慢，使用 growthCoefficient × 0.3
     * </p>
     *
     * @param level 当前等级
     * @return 综合速度倍率
     */
    public double getTotalSpeedMultiplier(int level) {
        int clampedLevel = Math.max(1, Math.min(level, maxLevel));
        double speedGrowth =
            1.0 +
            (clampedLevel - 1) *
            growthCoefficient *
            SwordGrowthTuning.SPEED_GROWTH_RELATIVE_TO_DAMAGE;
        return speedMultiplier * speedGrowth;
    }

    // ===== 静态工具方法 =====

    /**
     * 根据序号获取品质。
     *
     * @param tier 序号（0-8）
     * @return 对应品质，越界返回 COMMON
     */
    public static SwordQuality fromTier(int tier) {
        SwordQuality[] values = values();
        if (tier < 0 || tier >= values.length) {
            return COMMON;
        }
        return values[tier];
    }

    /**
     * 根据名称获取品质（大小写不敏感）。
     *
     * @param name 品质名称（如 "SPIRIT" 或 "灵品"）
     * @return 对应品质，未找到返回 COMMON
     */
    public static SwordQuality fromName(String name) {
        if (name == null || name.isEmpty()) {
            return COMMON;
        }

        // 先尝试枚举名匹配
        for (SwordQuality quality : values()) {
            if (quality.name().equalsIgnoreCase(name)) {
                return quality;
            }
        }

        // 再尝试显示名匹配
        for (SwordQuality quality : values()) {
            if (quality.displayName.equals(name)) {
                return quality;
            }
        }

        return COMMON;
    }

    /**
     * 获取下一品质（用于突破升级）。
     *
     * @return 下一品质，已是最高则返回自身
     */
    public SwordQuality getNextQuality() {
        int next = ordinal() + 1;
        SwordQuality[] values = values();
        if (next >= values.length) {
            return this;
        }
        return values[next];
    }

    /**
     * 检查是否是最高品质。
     */
    public boolean isMaxQuality() {
        return this == IMMORTAL;
    }

    /**
     * 检查是否可以突破到下一品质。
     * <p>
     * 突破条件：当前等级达到该品质的最大等级。
     * </p>
     *
     * @param currentLevel 当前等级
     * @return 是否可突破
     */
    public boolean canBreakthrough(int currentLevel) {
        if (isMaxQuality()) {
            return false;
        }
        return currentLevel >= maxLevel;
    }
}
