package com.Kizunad.customNPCs_test.goap;

import com.Kizunad.customNPCs.ai.decision.goals.PlanBasedGoal;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

import java.util.Arrays;
import java.util.List;

/**
 * 收集木板目标 - 基于 GOAP 规划的示例目标
 * <p>
 * 此目标演示如何使用 PlanBasedGoal 自动规划复杂任务。
 * <p>
 * 目标：获得木板
 * 规划器将自动生成：[ChopWood, CraftPlanks]
 */
public class GatherPlanksGoal extends PlanBasedGoal {
    
    private final float priority;
    
    /**
     * 创建收集木板目标
     * @param priority 优先级
     */
    public GatherPlanksGoal(float priority) {
        this.priority = priority;
    }
    
    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        return priority;
    }
    
    @Override
    public WorldState getDesiredState(INpcMind mind, LivingEntity entity) {
        // 目标：拥有木板
        WorldState goal = new WorldState();
        goal.setState("has_planks", true);
        return goal;
    }
    
    @Override
    public List<IGoapAction> getAvailableActions(INpcMind mind, LivingEntity entity) {
        // 提供可用的动作：砍木和制作木板
        return Arrays.asList(
            new ChopWoodGoapAction(),
            new CraftPlanksGoapAction()
        );
    }
    
    @Override
    public String getName() {
        return "gather_planks";
    }
}
