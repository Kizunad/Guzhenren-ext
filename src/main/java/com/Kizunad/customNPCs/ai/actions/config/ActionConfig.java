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

    // ==================== 通用配置 ====================
    /**
     * 默认超时时长（ticks）
     */
    private int defaultTimeoutTicks = 300;

    /**
     * 默认最大重试次数
     */
    private int defaultMaxRetries = 3;

    /**
     * 默认导航范围（blocks）
     */
    private double defaultNavRange = 10.0;

    // ==================== 攻击动作配置 ====================
    /**
     * 攻击距离（blocks）
     */
    private double attackRange = 3.0;

    /**
     * 攻击冷却时长（ticks）
     */
    private int attackCooldownTicks = 20;

    /**
     * 最大攻击尝试时长（ticks）
     */
    private int maxAttackAttemptTicks = 60;

    // ==================== 使用物品动作配置 ====================
    /**
     * 默认物品使用时长（ticks）
     */
    private int defaultItemUseTicks = 32;

    /**
     * 弓的最大蓄力时长（ticks）
     */
    private int bowMaxUseTicks = 72000;

    /**
     * 超时缓冲（ticks）
     */
    private int timeoutBufferTicks = 20;

    // ==================== 方块交互配置 ====================
    /**
     * 交互距离（blocks）
     */
    private double interactRange = 4.0;

    /**
     * 交互超时时长（ticks）
     */
    private int interactTimeoutTicks = 200;

    // ==================== 导航配置 ====================
    /**
     * 路径更新间隔（ticks）
     */
    private int pathUpdateInterval = 10;

    /**
     * 导航超时（ticks）
     */
    private int navTimeout = 300;

    /**
     * 重新导航间隔（ticks）
     */
    private int renavInterval = 10;

    /**
     * 最大导航重试次数
     */
    private int maxNavRetries = 5;

    // ==================== 日志配置 ====================
    /**
     * 日志输出间隔（ticks）
     */
    private int logIntervalTicks = 20;

    /**
     * 是否启用调试日志
     */
    private boolean debugLoggingEnabled = false;

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
     * 重置为默认值
     */
    public void resetToDefaults() {
        this.defaultTimeoutTicks = 300;
        this.defaultMaxRetries = 3;
        this.defaultNavRange = 10.0;
        this.attackRange = 3.0;
        this.attackCooldownTicks = 20;
        this.maxAttackAttemptTicks = 60;
        this.defaultItemUseTicks = 32;
        this.bowMaxUseTicks = 72000;
        this.timeoutBufferTicks = 20;
        this.interactRange = 4.0;
        this.interactTimeoutTicks = 200;
        this.pathUpdateInterval = 10;
        this.navTimeout = 300;
        this.renavInterval = 10;
        this.maxNavRetries = 5;
        this.logIntervalTicks = 20;
        this.debugLoggingEnabled = false;
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
