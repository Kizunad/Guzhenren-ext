package com.Kizunad.customNPCs.ai.decision;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.ArrayList;
import java.util.List;
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
    private IGoal currentGoal;
    private int ticksSinceLastEvaluation;
    private static final int EVALUATION_INTERVAL = 20; // 每秒重新评估一次（20 ticks）
    private static final float HYSTERESIS_THRESHOLD = 0.1f; // 10% 滞后阈值

    public UtilityGoalSelector() {
        this.goals = new ArrayList<>();
        this.currentGoal = null;
        this.ticksSinceLastEvaluation = 0;
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
        if (currentGoal == goal) {
            currentGoal = null;
        }
    }

    /**
     * 每个 tick 调用
     */
    public void tick(INpcMind mind, LivingEntity entity) {
        ticksSinceLastEvaluation++;

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
                currentGoal = null;
                reevaluate(mind, entity, null); // 立即选择新目标
            } else {
                currentGoal.tick(mind, entity);
            }
        } else {
            // 如果没有当前目标，立即选择一个
            reevaluate(mind, entity, null);
        }
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
        com.Kizunad.customNPCs.ai.sensors.SensorEventType interruptLevel
    ) {
        IGoal bestGoal = null;
        float bestPriority = 0.0f;

        for (IGoal goal : goals) {
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

        // 滞后判断:新目标必须显著优于当前目标
        if (bestGoal != currentGoal) {
            // 动态阈值:紧急情况下更容易切换
            float threshold = HYSTERESIS_THRESHOLD;
            if (
                interruptLevel ==
                com.Kizunad.customNPCs.ai.sensors.SensorEventType.CRITICAL
            ) {
                threshold = 0.0f; // CRITICAL 事件立即响应,忽略滞后
            }

            // 只有当新目标优先级显著高于当前目标时才切换
            if (bestPriority > currentPriority * (1.0f + threshold)) {
                if (currentGoal != null) {
                    System.out.println(
                        "[UtilityGoalSelector] 切换目标: " +
                            currentGoal.getName() +
                            " (" +
                            currentPriority +
                            ") -> " +
                            bestGoal.getName() +
                            " (" +
                            bestPriority +
                            ") [中断: " +
                            interruptLevel +
                            "]"
                    );
                    // 切换目标时清空旧计划，防止遗留动作继续执行
                    mind.getActionExecutor().stopCurrentPlan();
                    currentGoal.stop(mind, entity);
                } else {
                    System.out.println(
                        "[UtilityGoalSelector] 选择新目标: " +
                            bestGoal.getName() +
                            " (" +
                            bestPriority +
                            ")"
                    );
                }

                currentGoal = bestGoal;

                if (currentGoal != null) {
                    currentGoal.start(mind, entity);
                }
            } else {
                // 优先级差距不足,保持当前目标
                if (interruptLevel != null) {
                    System.out.println(
                        "[UtilityGoalSelector] 滞后阻止切换: 当前 " +
                            currentGoal.getName() +
                            " (" +
                            currentPriority +
                            ") vs 新 " +
                            bestGoal.getName() +
                            " (" +
                            bestPriority +
                            ")"
                    );
                }
            }
        }
    }

    /**
     * 获取当前活动目标
     */
    public IGoal getCurrentGoal() {
        return currentGoal;
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
        com.Kizunad.customNPCs.ai.sensors.SensorEventType eventType
    ) {
        ticksSinceLastEvaluation = 0;
        reevaluate(mind, entity, eventType);
    }
}
