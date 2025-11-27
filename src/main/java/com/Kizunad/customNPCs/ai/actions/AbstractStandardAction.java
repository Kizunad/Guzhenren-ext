package com.Kizunad.customNPCs.ai.actions;

import com.Kizunad.customNPCs.ai.actions.config.ActionConfig;
import com.Kizunad.customNPCs.ai.actions.util.NavigationUtil;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * 标准动作抽象基类 - 为所有标准动作提供通用功能
 * <p>
 * 提供以下功能：
 * - 超时控制：自动检测并处理超时
 * - 重试机制：支持失败后重试
 * - 上下文管理：UUID 引用、位置、世界等
 * - 失效检查：实体/世界变更检测
 * - 统一日志：结构化日志输出
 * <p>
 * 子类只需实现 {@link #tickInternal(INpcMind, Mob)} 即可。
 */
@SuppressWarnings("checkstyle:MagicNumber")
public abstract class AbstractStandardAction implements IAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStandardAction.class);

    // ==================== 超时控制 ====================
    /**
     * 超时时长（ticks）
     */
    protected final int timeoutTicks;

    /**
     * 已执行的 tick 数
     */
    protected int elapsedTicks = 0;

    // ==================== 重试机制 ====================
    /**
     * 最大重试次数
     */
    protected final int maxRetries;

    /**
     * 当前重试次数
     */
    protected int currentRetry = 0;

    // ==================== 上下文管理 ====================
    /**
     * 目标实体的 UUID（如果存在）
     */
    protected final UUID targetUuid;

    /**
     * 导航范围（blocks）
     */
    protected final double navRange;

    /**
     * 动作名称（用于日志和调试）
     */
    private final String actionName;

    // ==================== 配置 ====================
    /**
     * 配置实例
     */
    protected static final ActionConfig CONFIG = ActionConfig.getInstance();

    /**
     * 构造函数（使用默认值）
     * @param actionName 动作名称
     */
    protected AbstractStandardAction(String actionName) {
        this(
            actionName,
            null,
            CONFIG.getDefaultTimeoutTicks(),
            CONFIG.getDefaultMaxRetries(),
            CONFIG.getDefaultNavRange()
        );
    }

    /**
     * 构造函数（指定目标实体）
     * @param actionName 动作名称
     * @param targetUuid 目标实体 UUID
     */
    protected AbstractStandardAction(String actionName, UUID targetUuid) {
        this(
            actionName,
            targetUuid,
            CONFIG.getDefaultTimeoutTicks(),
            CONFIG.getDefaultMaxRetries(),
            CONFIG.getDefaultNavRange()
        );
    }

    /**
     * 构造函数（完整参数）
     * @param actionName 动作名称
     * @param targetUuid 目标实体 UUID（可为 null）
     * @param timeoutTicks 超时时长
     * @param maxRetries 最大重试次数
     * @param navRange 导航范围
     */
    protected AbstractStandardAction(
        String actionName,
        UUID targetUuid,
        int timeoutTicks,
        int maxRetries,
        double navRange
    ) {
        this.actionName = actionName;
        this.targetUuid = targetUuid;
        this.timeoutTicks = timeoutTicks;
        this.maxRetries = maxRetries;
        this.navRange = navRange;
    }

    @Override
    public final ActionStatus tick(INpcMind mind, LivingEntity entity) {
        // 检查实体类型
        if (!(entity instanceof Mob mob)) {
            LOGGER.error("[{}] 实体不是 Mob 类型: {}", actionName, entity.getClass().getSimpleName());
            return ActionStatus.FAILURE;
        }

        // 超时检测
        if (++elapsedTicks > timeoutTicks) {
            LOGGER.warn("[{}] 超时失败，已执行 {} ticks", actionName, elapsedTicks);
            return ActionStatus.FAILURE;
        }

        // 上下文失效检测
        if (targetUuid != null) {
            Entity target = resolveEntity(mob.level());
            if (target == null) {
                LOGGER.warn("[{}] 目标实体 {} 不存在或已失效", actionName, targetUuid);
                return ActionStatus.FAILURE;
            }
        }

        // 调用子类实现
        ActionStatus status = tickInternal(mind, mob);

        // 定期日志输出（仅在调试模式下）
        if (CONFIG.isDebugLoggingEnabled() && elapsedTicks % CONFIG.getLogIntervalTicks() == 0) {
            LOGGER.debug("[{}] Tick {} | Status: {}", actionName, elapsedTicks, status);
        }

        return status;
    }

    /**
     * 子类实现的实际 tick 逻辑
     * @param mind NPC 的思维
     * @param mob NPC 实体（已转换为 Mob 类型）
     * @return 动作状态
     */
    protected abstract ActionStatus tickInternal(INpcMind mind, Mob mob);

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        this.elapsedTicks = 0;
        this.currentRetry = 0;
        LOGGER.info("[{}] 动作开始", actionName);
        onStart(mind, entity);
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        LOGGER.info("[{}] 动作停止 | 已执行 {} ticks", actionName, elapsedTicks);
        onStop(mind, entity);
    }

    /**
     * 子类可选的 start 逻辑
     * @param mind NPC 的思维
     * @param entity NPC 实体
     */
    protected void onStart(INpcMind mind, LivingEntity entity) {
        // 子类可覆盖
    }

    /**
     * 子类可选的 stop 逻辑
     * @param mind NPC 的思维
     * @param entity NPC 实体
     */
    protected void onStop(INpcMind mind, LivingEntity entity) {
        // 子类可覆盖
    }

    @Override
    public String getName() {
        return actionName;
    }

    // ==================== 工具方法 ====================

    /**
     * 解析目标实体
     * @param level 世界
     * @return 目标实体，如果不存在则返回 null
     */
    protected Entity resolveEntity(Level level) {
        if (targetUuid == null) {
            return null;
        }
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getEntity(targetUuid);
        }
        return null;
    }

    /**
     * 检查是否在范围内（使用统一的导航工具）
     * @param entityPos 实体位置
     * @param targetPos 目标位置
     * @param threshold 距离阈值
     * @return true 如果在范围内
     */
    protected boolean isInRange(Vec3 entityPos, Vec3 targetPos, double threshold) {
        return NavigationUtil.isInRange(entityPos, targetPos, threshold);
    }

    /**
     * 增加重试计数
     * @return true 如果还可以重试，false 如果已达到最大重试次数
     */
    protected boolean incrementRetry() {
        currentRetry++;
        if (currentRetry > maxRetries) {
            LOGGER.warn("[{}] 已达到最大重试次数 {}", actionName, maxRetries);
            return false;
        }
        LOGGER.debug("[{}] 重试 {}/{}", actionName, currentRetry, maxRetries);
        return true;
    }
}
