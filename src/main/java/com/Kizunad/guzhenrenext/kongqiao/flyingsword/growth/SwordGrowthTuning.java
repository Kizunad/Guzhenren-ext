package com.Kizunad.guzhenrenext.kongqiao.flyingsword.growth;

/**
 * 飞剑成长系统调参配置。
 * <p>
 * 所有魔法数字都在此统一定义，避免分散在代码各处。
 * </p>
 * <p>
 * 核心数值说明：
 * <ul>
 *     <li>经验范围：单次获取 1~1000，累计上限 ~100,000</li>
 *     <li>等级范围：1~1000（由品质决定上限）</li>
 *     <li>伤害范围：基础 4.0，最高可达 4.0 × 10.0（品质） × 16（满级成长） ≈ 640</li>
 *     <li>速度范围：基础 0.55，最高可达 0.55 × 3.0（品质） × 2.35 ≈ 3.88 格/tick</li>
 * </ul>
 * </p>
 */
public final class SwordGrowthTuning {

    private SwordGrowthTuning() {}

    // ==================== 经验获取 ====================

    /**
     * 每点伤害获得的基础经验。
     * <p>
     * 1.0 表示造成 1 点伤害获得 1 经验。
     * </p>
     */
    public static final double EXP_PER_DAMAGE = 1.0;

    /**
     * 击杀目标的经验倍率。
     */
    public static final double EXP_KILL_MULTIPLIER = 5.0;

    /**
     * 攻击精英怪的经验倍率。
     */
    public static final double EXP_ELITE_MULTIPLIER = 2.0;

    /**
     * 攻击 Boss 的经验倍率。
     */
    public static final double EXP_BOSS_MULTIPLIER = 10.0;

    /**
     * 单次获取经验上限（防止刷经验）。
     */
    public static final int EXP_GAIN_CAP = 1000;

    // ==================== 道痕/流派加成 ====================

    /**
     * 剑道道痕每点提供的伤害加成比例。
     * <p>
     * 例如：0.01 表示每点剑道道痕提升 1% 伤害。
     * </p>
     */
    public static final double DAOHEN_JIANDAO_DAMAGE_COEF = 0.01;

    /**
     * 剑道流派每点提供的伤害加成比例。
     * <p>
     * 例如：0.02 表示每点剑道流派提升 2% 伤害。
     * </p>
     */
    public static final double LIUPAI_JIANDAO_DAMAGE_COEF = 0.02;

    /**
     * 剑道流派每点提供的经验获取加成比例。
     * <p>
     * 例如：0.01 表示每点剑道流派提升 1% 经验获取。
     * </p>
     */
    public static final double LIUPAI_JIANDAO_EXP_COEF = 0.01;

    /**
     * 剑道道痕加成上限倍率。
     * <p>
     * 防止道痕过高导致伤害溢出。例如：5.0 表示最多 +500% 伤害。
     * </p>
     */
    public static final double DAOHEN_DAMAGE_BONUS_CAP = 5.0;

    /**
     * 剑道流派伤害加成上限倍率。
     */
    public static final double LIUPAI_DAMAGE_BONUS_CAP = 10.0;

    /**
     * 剑道流派经验加成上限倍率。
     */
    public static final double LIUPAI_EXP_BONUS_CAP = 5.0;

    /**
     * 单次获取经验下限。
     */
    public static final int EXP_GAIN_MIN = 1;

    // ==================== 经验曲线 ====================

    /**
     * 升级经验基数。
     * <p>
     * 公式：expToNext = EXP_BASE × (1 + level)^EXP_EXPONENT
     * </p>
     */
    public static final double EXP_BASE = 50.0;

    /**
     * 升级经验指数。
     * <p>
     * 控制升级曲线陡峭程度：
     * <ul>
     *     <li>1.0 = 线性增长</li>
     *     <li>1.5 = 中等曲线</li>
     *     <li>2.0 = 较陡曲线</li>
     * </ul>
     * </p>
     */
    public static final double EXP_EXPONENT = 1.5;

    /**
     * 低等级经验平滑系数。
     * <p>
     * 前 LOW_LEVEL_THRESHOLD 级的经验需求额外降低，使新手期更流畅。
     * </p>
     */
    public static final int LOW_LEVEL_THRESHOLD = 10;

    /**
     * 低等级经验折扣比例。
     */
    public static final double LOW_LEVEL_DISCOUNT = 0.5;

    // ==================== 属性基础值 ====================

    /**
     * 基础攻击伤害（凡品1级）。
     */
    public static final double BASE_DAMAGE = 4.0;

    /**
     * 基础最大速度（格/tick）。
     */
    public static final double BASE_SPEED_MAX = 0.55;

    /**
     * 基础初始速度。
     */
    public static final double BASE_SPEED_BASE = 0.25;

    /**
     * 基础加速度。
     */
    public static final double BASE_ACCEL = 0.05;

    /**
     * 基础转向速率（度/tick）。
     */
    public static final double BASE_TURN_RATE = 12.0;

    /**
     * 基础最大耐久。
     */
    public static final double BASE_MAX_DURABILITY = 100.0;

    /**
     * 基础攻击冷却（tick）。
     */
    public static final int BASE_ATTACK_COOLDOWN = 10;

    // ==================== 属性成长上限 ====================

    /**
     * 伤害成长上限倍率。
     * <p>
     * 无论品质和等级，最终伤害不超过 BASE_DAMAGE × DAMAGE_CAP_MULTIPLIER。
     * </p>
     */
    public static final double DAMAGE_CAP_MULTIPLIER = 200.0;

    /**
     * 速度成长上限倍率。
     */
    public static final double SPEED_CAP_MULTIPLIER = 10.0;

    /**
     * 耐久成长上限倍率。
     */
    public static final double DURABILITY_CAP_MULTIPLIER = 100.0;

    // ==================== 突破系统 ====================

    /**
     * 突破需要的额外经验比例。
     * <p>
     * 突破消耗 = 当前品质最后一级经验 × BREAKTHROUGH_EXP_RATIO
     * </p>
     */
    public static final double BREAKTHROUGH_EXP_RATIO = 2.0;

    /**
     * 突破成功后经验重置为 0。
     */
    public static final boolean BREAKTHROUGH_RESET_EXP = true;

    /**
     * 突破成功后等级重置为 1。
     */
    public static final boolean BREAKTHROUGH_RESET_LEVEL = false;

    // ==================== 经验奖励类型 ====================

    /**
     * 击中无效果（如 0 伤害）时的保底经验。
     */
    public static final int EXP_MINIMUM_HIT = 1;

    /**
     * 暴击伤害的经验加成倍率（暂留扩展）。
     */
    public static final double EXP_CRIT_MULTIPLIER = 1.5;

    // ==================== 数值范围校验 ====================

    /**
     * 经验值绝对上限（防止溢出/作弊）。
     * <p>
     * 单剑累计经验不超过此值。
     * </p>
     */
    public static final int EXP_ABSOLUTE_CAP = 100_000;

    /**
     * 等级绝对上限。
     */
    public static final int LEVEL_ABSOLUTE_CAP = 1000;

    /**
     * 伤害绝对上限。
     */
    public static final double DAMAGE_ABSOLUTE_CAP = 10000.0;

    /**
     * 速度绝对上限（格/tick）。
     */
    public static final double SPEED_ABSOLUTE_CAP = 10.0;

    // ==================== 调试开关 ====================

    /**
     * 启用经验获取日志。
     */
    public static final boolean DEBUG_LOG_EXP_GAIN = false;

    /**
     * 启用升级日志。
     */
    public static final boolean DEBUG_LOG_LEVEL_UP = false;

    /**
     * 启用属性计算日志。
     */
    public static final boolean DEBUG_LOG_STAT_CALC = false;

    // ==================== 计算用常量（避免 magic number） ====================

    /** 目标高度系数（瞄准目标中心偏高处） */
    public static final double TARGET_HEIGHT_RATIO = 0.6;

    /** 速度伤害加成上限 */
    public static final double SPEED_DAMAGE_BONUS_CAP = 0.5;

    /** 速度伤害加成系数 */
    public static final double SPEED_DAMAGE_BONUS_COEF = 0.3;

    /** 临时修正最小值 */
    public static final double MULTIPLIER_MIN = 0.1;

    public static final int IMPRINT_TIER_1_THRESHOLD = 10;
    public static final int IMPRINT_TIER_2_THRESHOLD = 25;
    public static final int IMPRINT_TIER_3_THRESHOLD = 45;
    public static final int IMPRINT_TIER_4_THRESHOLD = 70;
    public static final int IMPRINT_TIER_CAP = 4;

    public static final double IMPRINT_MAIN_DAMAGE_SQRT_COEF = 0.06;
    public static final double IMPRINT_MAIN_DAMAGE_BONUS_CAP = 0.8;

    public static final double IMPRINT_SUB_DAMAGE_SQRT_COEF = 0.03;
    public static final double IMPRINT_SUB_DAMAGE_BONUS_CAP = 0.3;

    public static final double IMPRINT_SUB_SPEED_SQRT_COEF = 0.025;
    public static final double IMPRINT_SUB_SPEED_BONUS_CAP = 0.25;

    public static final double IMPRINT_PROC_BASE_CHANCE = 0.08;
    public static final double IMPRINT_PROC_CHANCE_PER_TIER = 0.01;
    public static final double IMPRINT_PROC_CHANCE_CAP = 0.25;

    public static final int IMPRINT_PROC_BASE_COOLDOWN_TICKS = 40;
    public static final int IMPRINT_PROC_COOLDOWN_REDUCTION_PER_TIER = 5;
    public static final int IMPRINT_PROC_MIN_COOLDOWN_TICKS = 10;

    public static final int IMPRINT_YANDAO_BASE_BURN_SECONDS = 2;

    public static final double IMPRINT_JIANDAO_BASE_BONUS_DAMAGE_MULTIPLIER = 0.5;
    public static final double IMPRINT_JIANDAO_BONUS_DAMAGE_PER_TIER = 0.2;

    public static final double IMPRINT_LEIDAO_CHAIN_DAMAGE_RATIO = 0.25;
    public static final double IMPRINT_LEIDAO_CHAIN_RANGE = 6.0;

    /** 初始速度相对最大速度的上限比例 */
    public static final double SPEED_BASE_TO_MAX_RATIO = 0.8;

    /** 初始速度成长系数 */
    public static final double SPEED_BASE_GROWTH_COEF = 0.7;

    /** 加速度成长系数 */
    public static final double ACCEL_GROWTH_COEF = 0.5;

    /** 加速度绝对上限 */
    public static final double ACCEL_ABSOLUTE_CAP = 0.5;

    /** 转向速率品质加成系数（每品质等级） */
    public static final double TURN_RATE_QUALITY_COEF = 0.1;

    /** 转向速率等级加成系数（每级） */
    public static final double TURN_RATE_LEVEL_COEF = 0.005;

    /** 转向速率绝对上限 */
    public static final double TURN_RATE_ABSOLUTE_CAP = 60.0;

    /** 耐久等级成长系数（每级） */
    public static final double DURABILITY_LEVEL_GROWTH_COEF = 0.02;

    /** 攻击冷却品质减少系数（每品质等级） */
    public static final double ATTACK_COOLDOWN_QUALITY_COEF = 0.05;

    /** 攻击冷却等级减少周期 */
    public static final int ATTACK_COOLDOWN_LEVEL_PERIOD = 100;

    /** 攻击冷却绝对下限（tick） */
    public static final int ATTACK_COOLDOWN_MIN = 2;

    /** 战力评估品质加成系数（每品质等级） */
    public static final double POWER_RATING_QUALITY_COEF = 0.05;

    /** 属性比较精度阈值 */
    public static final double STAT_COMPARE_EPSILON = 0.001;

    /** 战力显示阈值（百万） */
    public static final double POWER_DISPLAY_MILLION = 1_000_000.0;

    /** 战力显示阈值（千） */
    public static final double POWER_DISPLAY_THOUSAND = 1_000.0;

    /** 精英判定生命值阈值 */
    public static final float ELITE_HEALTH_THRESHOLD = 40.0f;

    /** 默认召回剑耐久 */
    public static final float DEFAULT_RECALLED_DURABILITY = 100.0f;

    /** 经验进度条百分比乘数 */
    public static final double EXP_BAR_PERCENT_MULTIPLIER = 100.0;

    /** 速度成长相对伤害成长的比例 */
    public static final double SPEED_GROWTH_RELATIVE_TO_DAMAGE = 0.3;
}
