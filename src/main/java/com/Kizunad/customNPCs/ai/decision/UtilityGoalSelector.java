package com.Kizunad.customNPCs.ai.decision;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

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
            reevaluate(mind, entity);
        }
        
        // 执行当前目标
        if (currentGoal != null) {
            // 检查当前目标是否完成或无法继续
            if (currentGoal.isFinished(mind, entity) || !currentGoal.canRun(mind, entity)) {
                currentGoal.stop(mind, entity);
                currentGoal = null;
                reevaluate(mind, entity); // 立即选择新目标
            } else {
                currentGoal.tick(mind, entity);
            }
        } else {
            // 如果没有当前目标，立即选择一个
            reevaluate(mind, entity);
        }
    }
    
    /**
     * 重新评估所有目标并选择最高优先级的
     */
    private void reevaluate(INpcMind mind, LivingEntity entity) {
        IGoal bestGoal = null;
        float bestPriority = 0.0f;
        
        for (IGoal goal : goals) {
            if (!goal.canRun(mind, entity)) {
                continue;
            }
            
            float priority = goal.getPriority(mind, entity);
            if (priority > bestPriority) {
                bestPriority = priority;
                bestGoal = goal;
            }
        }
        
        // 如果找到了更好的目标，切换
        if (bestGoal != currentGoal) {
            if (currentGoal != null) {
                currentGoal.stop(mind, entity);
            }
            
            currentGoal = bestGoal;
            
            if (currentGoal != null) {
                currentGoal.start(mind, entity);
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
     */
    public void forceReevaluate(INpcMind mind, LivingEntity entity) {
        ticksSinceLastEvaluation = 0;
        reevaluate(mind, entity);
    }
}
