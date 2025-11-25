package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.planner.GoapPlanner;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;

/**
 * 基于规划的目标抽象基类
 * <p>
 * PlanBasedGoal 自动化 GOAP 规划流程：
 * 1. 在 start() 时调用 GoapPlanner 生成动作序列
 * 2. 将生成的动作序列提交给 ActionExecutor 执行
 * 3. 在 isFinished() 时检查执行器是否完成所有动作
 * <p>
 * 子类只需要实现：
 * - getDesiredState(): 定义期望达成的世界状态
 * - getAvailableActions(): 提供可用的 GOAP 动作列表
 * - getPriority(): 计算目标优先级
 */
public abstract class PlanBasedGoal implements IGoal {

    private final GoapPlanner planner;
    private boolean started;
    private boolean planningFailed;
    private boolean completed;

    /**
     * 创建基于规划的目标
     */
    public PlanBasedGoal() {
        this.planner = new GoapPlanner();
        this.started = false;
        this.planningFailed = false;
        this.completed = false;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        // 默认：如果能生成计划则可以运行
        WorldState current = mind.getCurrentWorldState(entity);
        WorldState goal = getDesiredState(mind, entity);

        if (current == null || goal == null) {
            return false;
        }

        // 如果当前状态已满足目标，则不需要运行
        if (current.matches(goal)) {
            return false;
        }

        return true;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        started = true;
        planningFailed = false;
        completed = false;

        // 获取当前世界状态
        WorldState currentState = mind.getCurrentWorldState(entity);

        // 合并目标特定的当前状态
        WorldState goalSpecificState = getCurrentState(mind, entity);
        if (goalSpecificState != null) {
            currentState = currentState.apply(goalSpecificState);
        }

        // 获取目标状态
        WorldState goalState = getDesiredState(mind, entity);

        // 获取可用动作
        List<IGoapAction> availableActions = getAvailableActions(mind, entity);

        System.out.println("[PlanBasedGoal] " + getName() + " 开始规划");
        System.out.println("  当前状态: " + currentState);
        System.out.println("  目标状态: " + goalState);
        System.out.println("  可用动作: " + availableActions.size() + " 个");

        // 调用规划器
        List<IAction> plan = planner.plan(
            currentState,
            goalState,
            availableActions
        );

        if (plan != null && !plan.isEmpty()) {
            // 规划成功，提交动作序列
            mind.getActionExecutor().submitPlan(plan);
            System.out.println(
                "[PlanBasedGoal] 规划成功，生成 " + plan.size() + " 个动作"
            );
        } else {
            // 规划失败
            planningFailed = true;
            System.err.println(
                "[PlanBasedGoal] " + getName() + " 规划失败，无法生成有效计划"
            );
        }
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 目标本身不需要做任何事，动作由 ActionExecutor 执行
        // 子类可以重写此方法以添加额外逻辑
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        System.out.println("[PlanBasedGoal] " + getName() + " 停止");
        started = false;
        planningFailed = false;
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        // 如果规划失败，目标立即完成（失败）
        if (planningFailed) {
            return true;
        }

        // 当所有动作都执行完成后，目标完成
        if (started && mind.getActionExecutor().isIdle()) {
            completed = true;
            return true;
        }

        return completed;
    }

    /**
     * 获取目标特定的当前状态
     * <p>
     * 子类可以重写此方法以提供额外的当前状态信息。
     * 这些状态将合并到 mind.getCurrentWorldState() 返回的状态中。
     *
     * @param mind NPC思维
     * @param entity NPC实体
     * @return 目标特定的当前状态，如果没有则返回 null
     */
    protected WorldState getCurrentState(INpcMind mind, LivingEntity entity) {
        return null;
    }

    /**
     * 检查规划是否失败
     * @return true 如果规划失败
     */
    protected boolean isPlanningFailed() {
        return planningFailed;
    }
}
