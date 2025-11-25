package com.Kizunad.customNPCs_test.goals;

import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.ai.decision.goals.PlanBasedGoal;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * 测试用的PlanBasedGoal
 * <p>
 * 允许自定义目标名称、优先级、目标状态和可用动作,用于测试
 */
public class TestPlanGoal extends PlanBasedGoal {

    private final String goalName;
    private final float priority;
    private final WorldState desiredState;
    private final List<IGoapAction> availableActions;

    /**
     * 创建测试目标
     *
     * @param goalName 目标名称
     * @param priority 基础优先级
     * @param desiredState 期望状态(null表示使用空状态)
     * @param availableActions 可用动作列表(null表示使用空列表)
     */
    public TestPlanGoal(
        String goalName,
        float priority,
        WorldState desiredState,
        List<IGoapAction> availableActions
    ) {
        this.goalName = goalName;
        this.priority = priority;
        this.desiredState =
            desiredState != null ? desiredState : new WorldState();
        this.availableActions =
            availableActions != null ? availableActions : List.of();
    }

    @Override
    public String getName() {
        return goalName;
    }

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        return priority;
    }

    @Override
    public WorldState getDesiredState(INpcMind mind, LivingEntity entity) {
        return desiredState;
    }

    @Override
    public List<IGoapAction> getAvailableActions(
        INpcMind mind,
        LivingEntity entity
    ) {
        return availableActions;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        // 测试目标总是可以运行(除非规划失败)
        if (isPlanningFailed()) {
            return false;
        }

        // 如果没有可用动作,则无法运行
        if (availableActions.isEmpty()) {
            return false;
        }

        return true;
    }
}
