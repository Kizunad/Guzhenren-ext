package com.Kizunad.customNPCs.ai.actions.config;

/**
 * 动作配置类 - 集中管理所有动作的默认参数
 * <p>
 * 提供类型安全的配置访问，未来可以扩展为从外部配置文件加载。
 * <p>
 * 使用方式：
 * <pre>
 * ActionConfig config = ActionConfig.getInstance();
 * double attackRange = config.getAttackRange();
 * </pre>
 */
@SuppressWarnings("checkstyle:MagicNumber")
public final class ActionConfig {

    private static final ActionConfig INSTANCE = new ActionConfig();
    private static final int DEFAULT_TIMEOUT_TICKS = 300;
    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final double DEFAULT_NAV_RANGE = 10.0;
    private static final double DEFAULT_ATTACK_RANGE = 3.0;
    private static final int DEFAULT_ATTACK_COOLDOWN_TICKS = 20;
    private static final int DEFAULT_MAX_ATTACK_ATTEMPT_TICKS = 60;
    private static final int DEFAULT_ITEM_USE_TICKS = 32;
    private static final int DEFAULT_BOW_MAX_USE_TICKS = 72000;
    private static final int DEFAULT_TIMEOUT_BUFFER_TICKS = 20;
    private static final double DEFAULT_INTERACT_RANGE = 4.0;
    private static final int DEFAULT_INTERACT_TIMEOUT_TICKS = 200;
    private static final int DEFAULT_PATH_UPDATE_INTERVAL = 10;
    private static final int DEFAULT_NAV_TIMEOUT = 300;
    private static final int DEFAULT_RENAV_INTERVAL = 10;
    private static final int DEFAULT_MAX_NAV_RETRIES = 5;
    private static final int DEFAULT_LOG_INTERVAL_TICKS = 20;
    private static final double DEFAULT_ARMOR_DESIRE_WEIGHT = 1.0;

    // ==================== 通用配置 ====================
    /**
     * 默认超时时长（ticks）
     */
    private int defaultTimeoutTicks = DEFAULT_TIMEOUT_TICKS;

    /**
     * 默认最大重试次数
     */
    private int defaultMaxRetries = DEFAULT_MAX_RETRIES;

    /**
     * 默认导航范围（blocks）
     */
    private double defaultNavRange = DEFAULT_NAV_RANGE;

    // ==================== 攻击动作配置 ====================
    /**
     * 攻击距离（blocks）
     */
    private double attackRange = DEFAULT_ATTACK_RANGE;

    /**
     * 攻击冷却时长（ticks）
     */
    private int attackCooldownTicks = DEFAULT_ATTACK_COOLDOWN_TICKS;

    /**
     * 最大攻击尝试时长（ticks）
     */
    private int maxAttackAttemptTicks = DEFAULT_MAX_ATTACK_ATTEMPT_TICKS;

    // ==================== 使用物品动作配置 ====================
    /**
     * 默认物品使用时长（ticks）
     */
    private int defaultItemUseTicks = DEFAULT_ITEM_USE_TICKS;

    /**
     * 弓的最大蓄力时长（ticks）
     */
    private int bowMaxUseTicks = DEFAULT_BOW_MAX_USE_TICKS;

    /**
     * 超时缓冲（ticks）
     */
    private int timeoutBufferTicks = DEFAULT_TIMEOUT_BUFFER_TICKS;

    // ==================== 方块交互配置 ====================
    /**
     * 交互距离（blocks）
     */
    private double interactRange = DEFAULT_INTERACT_RANGE;

    /**
     * 交互超时时长（ticks）
     */
    private int interactTimeoutTicks = DEFAULT_INTERACT_TIMEOUT_TICKS;

    // ==================== 导航配置 ====================
    /**
     * 路径更新间隔（ticks）
     */
    private int pathUpdateInterval = DEFAULT_PATH_UPDATE_INTERVAL;

    /**
     * 导航超时（ticks）
     */
    private int navTimeout = DEFAULT_NAV_TIMEOUT;

    /**
     * 重新导航间隔（ticks）
     */
    private int renavInterval = DEFAULT_RENAV_INTERVAL;

    /**
     * 最大导航重试次数
     */
    private int maxNavRetries = DEFAULT_MAX_NAV_RETRIES;

    // ==================== 日志配置 ====================
    /**
     * 日志输出间隔（ticks）
     */
    private int logIntervalTicks = DEFAULT_LOG_INTERVAL_TICKS;

    /**
     * 是否启用调试日志
     */
    private boolean debugLoggingEnabled = false;

    /**
     * GOAP 装备盔甲意愿权重（越高越倾向于优化装备）
     */
    private double armorDesireWeight = DEFAULT_ARMOR_DESIRE_WEIGHT;

    /**
     * 私有构造函数（单例模式）
     */
    private ActionConfig() {
        // 未来可以在这里加载外部配置文件
    }

    /**
     * 获取单例实例
     * @return ActionConfig 实例
     */
    public static ActionConfig getInstance() {
        return INSTANCE;
    }

    // ==================== Getter 方法 ====================

    public int getDefaultTimeoutTicks() {
        return defaultTimeoutTicks;
    }

    public int getDefaultMaxRetries() {
        return defaultMaxRetries;
    }

    public double getDefaultNavRange() {
        return defaultNavRange;
    }

    public double getAttackRange() {
        return attackRange;
    }

    public int getAttackCooldownTicks() {
        return attackCooldownTicks;
    }

    public int getMaxAttackAttemptTicks() {
        return maxAttackAttemptTicks;
    }

    public int getDefaultItemUseTicks() {
        return defaultItemUseTicks;
    }

    public int getBowMaxUseTicks() {
        return bowMaxUseTicks;
    }

    public int getTimeoutBufferTicks() {
        return timeoutBufferTicks;
    }

    public double getInteractRange() {
        return interactRange;
    }

    public int getInteractTimeoutTicks() {
        return interactTimeoutTicks;
    }

    public int getPathUpdateInterval() {
        return pathUpdateInterval;
    }

    public int getNavTimeout() {
        return navTimeout;
    }

    public int getRenavInterval() {
        return renavInterval;
    }

    public int getMaxNavRetries() {
        return maxNavRetries;
    }

    public int getLogIntervalTicks() {
        return logIntervalTicks;
    }

    public boolean isDebugLoggingEnabled() {
        return debugLoggingEnabled;
    }

    public double getArmorDesireWeight() {
        return armorDesireWeight;
    }

    // ==================== Setter 方法（用于运行时调整） ====================

    /**
     * 设置攻击距离
     * @param attackRange 攻击距离（blocks）
     * @return this（支持链式调用）
     */
    public ActionConfig setAttackRange(double attackRange) {
        this.attackRange = attackRange;
        return this;
    }

    /**
     * 设置攻击冷却时长
     * @param attackCooldownTicks 冷却时长（ticks）
     * @return this
     */
    public ActionConfig setAttackCooldownTicks(int attackCooldownTicks) {
        this.attackCooldownTicks = attackCooldownTicks;
        return this;
    }

    /**
     * 设置交互距离
     * @param interactRange 交互距离（blocks）
     * @return this
     */
    public ActionConfig setInteractRange(double interactRange) {
        this.interactRange = interactRange;
        return this;
    }

    /**
     * 设置导航超时
     * @param navTimeout 超时时长（ticks）
     * @return this
     */
    public ActionConfig setNavTimeout(int navTimeout) {
        this.navTimeout = navTimeout;
        return this;
    }

    /**
     * 启用/禁用调试日志
     * @param enabled 是否启用
     * @return this
     */
    public ActionConfig setDebugLoggingEnabled(boolean enabled) {
        this.debugLoggingEnabled = enabled;
        return this;
    }

    /**
     * 调整装备盔甲的意愿权重。
     */
    public ActionConfig setArmorDesireWeight(double weight) {
        this.armorDesireWeight = weight;
        return this;
    }

    /**
     * 重置为默认值
     */
    public void resetToDefaults() {
        this.defaultTimeoutTicks = DEFAULT_TIMEOUT_TICKS;
        this.defaultMaxRetries = DEFAULT_MAX_RETRIES;
        this.defaultNavRange = DEFAULT_NAV_RANGE;
        this.attackRange = DEFAULT_ATTACK_RANGE;
        this.attackCooldownTicks = DEFAULT_ATTACK_COOLDOWN_TICKS;
        this.maxAttackAttemptTicks = DEFAULT_MAX_ATTACK_ATTEMPT_TICKS;
        this.defaultItemUseTicks = DEFAULT_ITEM_USE_TICKS;
        this.bowMaxUseTicks = DEFAULT_BOW_MAX_USE_TICKS;
        this.timeoutBufferTicks = DEFAULT_TIMEOUT_BUFFER_TICKS;
        this.interactRange = DEFAULT_INTERACT_RANGE;
        this.interactTimeoutTicks = DEFAULT_INTERACT_TIMEOUT_TICKS;
        this.pathUpdateInterval = DEFAULT_PATH_UPDATE_INTERVAL;
        this.navTimeout = DEFAULT_NAV_TIMEOUT;
        this.renavInterval = DEFAULT_RENAV_INTERVAL;
        this.maxNavRetries = DEFAULT_MAX_NAV_RETRIES;
        this.logIntervalTicks = DEFAULT_LOG_INTERVAL_TICKS;
        this.debugLoggingEnabled = false;
        this.armorDesireWeight = DEFAULT_ARMOR_DESIRE_WEIGHT;
    }

    @Override
    public String toString() {
        return "ActionConfig{" +
            "attackRange=" + attackRange +
            ", attackCooldownTicks=" + attackCooldownTicks +
            ", interactRange=" + interactRange +
            ", navTimeout=" + navTimeout +
            ", debugLoggingEnabled=" + debugLoggingEnabled +
            '}';
    }
}
