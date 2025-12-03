package com.Kizunad.customNPCs.ai.decision;

import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.ai.sensors.SensorEventType;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.LivingEntity;

/**
 * Utility AI 目标选择器
 * <p>
 * 负责：
 * 1. 管理所有注册的目标
 * 2. 定期重新评估目标优先级
 * 3. 切换到最高优先级的目标
 */
public class UtilityGoalSelector {

    private final List<IGoal> goals;
    private final Map<String, Integer> goalCooldowns;
    private IGoal currentGoal;
    private int currentGoalActiveTicks;
    private int ticksSinceLastEvaluation;
    private int idleStallTicks;
    private static final int EVALUATION_INTERVAL = 20; // 每秒重新评估一次（20 ticks）
    private static final float HYSTERESIS_THRESHOLD = 0.1f; // 10% 滞后阈值
    private static final float PREEMPTION_ADDITIONAL_THRESHOLD = 0.15f; // 早期抢占额外阈值
    private static final int PREEMPTION_GRACE_TICKS = 15; // 刚切换后至少坚持 15 ticks
    private static final int GOAL_COOLDOWN_TICKS = 40; // 目标切换后的冷却期
    private static final int IDLE_STALL_THRESHOLD = 8; // 连续无目标/动作的判定阈值

    public UtilityGoalSelector() {
        this.goals = new ArrayList<>();
        this.goalCooldowns = new HashMap<>();
        this.currentGoal = null;
        this.currentGoalActiveTicks = 0;
        this.ticksSinceLastEvaluation = 0;
        this.idleStallTicks = 0;
    }

    /**
     * 注册一个目标
     * @param goal 要注册的目标
     */
    public void registerGoal(IGoal goal) {
        goals.add(goal);
    }

    /**
     * 取消注册一个目标
     * @param goal 要取消的目标
     */
    public void unregisterGoal(IGoal goal) {
        goals.remove(goal);
        goalCooldowns.remove(goal.getName());
        if (currentGoal == goal) {
            currentGoal = null;
        }
    }

    /**
     * 每个 tick 调用
     */
    public void tick(INpcMind mind, LivingEntity entity) {
        ticksSinceLastEvaluation++;
        decayCooldowns();
        if (currentGoal != null) {
            currentGoalActiveTicks++;
        } else {
            currentGoalActiveTicks = 0;
        }

        // 定期重新评估目标
        if (ticksSinceLastEvaluation >= EVALUATION_INTERVAL) {
            ticksSinceLastEvaluation = 0;
            reevaluate(mind, entity, null); // 常规评估,无中断事件
        }

        // 执行当前目标
        if (currentGoal != null) {
            // 检查当前目标是否完成或无法继续
            if (
                currentGoal.isFinished(mind, entity) ||
                !currentGoal.canRun(mind, entity)
            ) {
                // 当前目标无法继续时，终止其动作计划，避免旧计划残留
                mind.getActionExecutor().stopCurrentPlan();
                currentGoal.stop(mind, entity);
                startCooldown(currentGoal);
                currentGoal = null;
                currentGoalActiveTicks = 0;
                reevaluate(mind, entity, null); // 立即选择新目标
            } else {
                currentGoal.tick(mind, entity);
            }
        } else {
            // 如果没有当前目标，立即选择一个
            reevaluate(mind, entity, null);
        }

        guardAgainstStall(mind, entity);
    }

    /**
     * 重新评估所有目标并选择最高优先级的
     * <p>
     * 实施滞后 (Hysteresis) 机制:新目标的优先级必须显著高于当前目标才会切换,
     * 防止优先级微小波动导致的频繁切换。
     *
     * @param mind NPC 思维
     * @param entity NPC 实体
     * @param interruptLevel 中断事件级别 (null 表示常规评估,非 null 表示中断触发)
     */
    private void reevaluate(
        INpcMind mind,
        LivingEntity entity,
        SensorEventType interruptLevel
    ) {
        reevaluateInternal(mind, entity, interruptLevel, false);
    }

    private void reevaluateInternal(
        INpcMind mind,
        LivingEntity entity,
        SensorEventType interruptLevel,
        boolean ignoreCooldown
    ) {
        IGoal bestGoal = null;
        float bestPriority = 0.0f;

        if (goals.isEmpty()) {
            return;
        }

        // 如果当前目标的最终优先级高于已知的最高优先级，则更新最高优先级和最佳目标
        for (IGoal goal : goals) {
            // 检查目标是否在冷却中
            if (!ignoreCooldown && isOnCooldown(goal)) {
                continue;
            }

            // 检查目标是否可以运行
            if (!goal.canRun(mind, entity)) {
                continue;
            }

            // 获取基础优先级
            float basePriority = goal.getPriority(mind, entity);

            // 应用性格修正
            float personalityModifier = mind
                .getPersonality()
                .getModifierForGoal(goal.getName());
            float finalPriority = basePriority * (1.0f + personalityModifier);

            // 如果当前目标的最终优先级高于已知的最高优先级，则更新最高优先级和最佳目标
            if (finalPriority > bestPriority) {
                bestPriority = finalPriority;
                bestGoal = goal;
            }
        }

        // 计算当前目标的优先级(用于滞后判断)
        float currentPriority = 0.0f;
        if (currentGoal != null) {
            float basePriority = currentGoal.getPriority(mind, entity);
            float personalityModifier = mind
                .getPersonality()
                .getModifierForGoal(currentGoal.getName());
            currentPriority = basePriority * (1.0f + personalityModifier);
        }

        // 如果没有可用目标，则保持现状
        if (bestGoal == null && currentGoal == null) {
            return;
        }
        if (bestGoal == null) {
            return;
        }

        // 若当前有正在执行的计划且非中断场景，不抢占，等待动作队列空闲
        if (
            currentGoal != null &&
            bestGoal != currentGoal &&
            interruptLevel == null &&
            !mind.getActionExecutor().isIdle()
        ) {
            MindLog.decision(
                MindLogLevel.DEBUG,
                "保持当前目标 {}，因动作计划执行中且无中断事件",
                currentGoal.getName()
            );
            return;
        }

        // 滞后判断:新目标必须显著优于当前目标
        if (bestGoal != currentGoal) {
            float threshold = getSwitchThreshold(interruptLevel);

            // 只有当新目标优先级显著高于当前目标时才切换
            if (bestPriority > currentPriority * (1.0f + threshold)) {
                if (currentGoal != null) {
                    MindLog.decision(
                        MindLogLevel.INFO,
                        "切换目标: {} ({}) -> {} ({}) [中断: {}]",
                        currentGoal.getName(),
                        currentPriority,
                        bestGoal.getName(),
                        bestPriority,
                        interruptLevel
                    );
                    // 切换目标时清空旧计划，防止遗留动作继续执行
                    mind.getActionExecutor().stopCurrentPlan();
                    currentGoal.stop(mind, entity);
                    startCooldown(currentGoal);
                } else {
                    MindLog.decision(
                        MindLogLevel.INFO,
                        "选择新目标: {} ({})",
                        bestGoal.getName(),
                        bestPriority
                    );
                }

                currentGoal = bestGoal;
                currentGoalActiveTicks = 0;

                if (currentGoal != null) {
                    currentGoal.start(mind, entity);
                }
            } else {
                // 优先级差距不足,保持当前目标
                if (interruptLevel != null) {
                    MindLog.decision(
                        MindLogLevel.DEBUG,
                        "滞后阻止切换: 当前 {} ({}) vs 新 {} ({})",
                        currentGoal.getName(),
                        currentPriority,
                        bestGoal.getName(),
                        bestPriority
                    );
                }
            }
        }
    }

    /**
     * 调试用途：强制切换到指定目标，跳过优先级与滞后判断。
     * 会停止当前计划并立即调用目标的 start。
     *
     * @param mind NPC 思维
     * @param entity 实体
     * @param goal 目标实例
     */
    public void forceSwitchTo(INpcMind mind, LivingEntity entity, IGoal goal) {
        if (currentGoal != null) {
            mind.getActionExecutor().stopCurrentPlan();
            currentGoal.stop(mind, entity);
            startCooldown(currentGoal);
        }
        currentGoal = goal;
        currentGoalActiveTicks = 0;
        if (currentGoal != null) {
            currentGoal.start(mind, entity);
        }
    }

    /**
     * 强制重新评估（用于紧急情况，如受到攻击）
     *
     * @param mind NPC 思维
     * @param entity NPC 实体
     * @param eventType 触发中断的事件类型
     */
    public void forceReevaluate(
        INpcMind mind,
        LivingEntity entity,
        SensorEventType eventType
    ) {
        ticksSinceLastEvaluation = 0;
        reevaluateInternal(mind, entity, eventType, false);
    }

    private boolean isOnCooldown(IGoal goal) {
        Integer cooldownTicks = goalCooldowns.get(goal.getName());
        return cooldownTicks != null && cooldownTicks > 0;
    }

    private void startCooldown(IGoal goal) {
        if (goal != null && goals.size() > 1) {
            goalCooldowns.put(goal.getName(), GOAL_COOLDOWN_TICKS);
        }
    }

    private void decayCooldowns() {
        if (goalCooldowns.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<String, Integer>> iterator = goalCooldowns
            .entrySet()
            .iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                iterator.remove();
            } else {
                entry.setValue(remaining);
            }
        }
    }

    private float getSwitchThreshold(SensorEventType interruptLevel) {
        if (interruptLevel == SensorEventType.CRITICAL) {
            return 0.0f; // CRITICAL 事件立即响应,忽略滞后与抢占阈值
        }

        if (
            currentGoal != null &&
            currentGoalActiveTicks < PREEMPTION_GRACE_TICKS
        ) {
            // 刚切换的目标享有额外的抢占保护，需要更高的优先级差距才会被替换
            return HYSTERESIS_THRESHOLD + PREEMPTION_ADDITIONAL_THRESHOLD;
        }

        return HYSTERESIS_THRESHOLD;
    }

    /**
     * 防止长时间无目标/动作导致站桩：连续空闲 N tick 后忽略冷却强制重评估。
     */
    private void guardAgainstStall(INpcMind mind, LivingEntity entity) {
        if (currentGoal == null && mind.getActionExecutor().isIdle()) {
            idleStallTicks++;
            if (idleStallTicks >= IDLE_STALL_THRESHOLD) {
                MindLog.decision(
                    MindLogLevel.WARN,
                    "检测到连续 {} tick 无目标/动作，强制重评估（忽略冷却）",
                    idleStallTicks
                );
                reevaluateInternal(
                    mind,
                    entity,
                    SensorEventType.CRITICAL,
                    true
                );
                idleStallTicks = 0;
            }
        } else {
            idleStallTicks = 0;
        }
    }

    /**
     * 获取当前活动目标
     */
    public IGoal getCurrentGoal() {
        return currentGoal;
    }

    /**
     * 根据名称获取已注册的目标实例。
     * @param name 目标名称
     * @return 匹配的目标实例，不存在时返回 null
     */
    public IGoal getGoalByName(String name) {
        if (name == null) {
            return null;
        }
        for (IGoal goal : goals) {
            if (name.equals(goal.getName())) {
                return goal;
            }
        }
        return null;
    }

    /**
     * 判断是否已注册指定名称的目标
     * @param name 目标名称
     * @return true 表示已注册
     */
    public boolean containsGoal(String name) {
        if (name == null) {
            return false;
        }
        for (IGoal goal : goals) {
            if (name.equals(goal.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否已存在任何目标（用于避免重复初始化）
     * @return true 表示已有目标
     */
    public boolean hasRegisteredGoals() {
        return !goals.isEmpty();
    }
}
