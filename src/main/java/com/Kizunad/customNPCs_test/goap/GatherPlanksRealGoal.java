package com.Kizunad.customNPCs_test.goap;

import com.Kizunad.customNPCs.ai.decision.goals.PlanBasedGoal;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

import java.util.Arrays;
import java.util.List;

/**
 * 收集木板目标（真实版本）- 基于 GOAP 规划的完整示例
 * <p>
 * 此目标演示真实的多步骤 GOAP 规划：
 * 1. MoveToTree - 移动到树的位置
 * 2. BreakBlock - 破坏树木方块
 * 3. CollectItem - 收集掉落的木头
 * 4. CraftPlanks - 制作木板
 * <p>
 * 目标：获得木板
 * 规划器将自动生成：[MoveToTree, BreakBlock, CollectItem, CraftPlanks]
 */
public class GatherPlanksRealGoal extends PlanBasedGoal {
    
    private final float priority;
    private final BlockPos treePos;
    
    /**
     * 创建收集木板目标（真实版本）
     * @param priority 优先级
     * @param treePos 树木位置
     */
    public GatherPlanksRealGoal(float priority, BlockPos treePos) {
        this.priority = priority;
        this.treePos = treePos;
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
        // 提供完整的动作链
        return Arrays.asList(
            new MoveToTreeGoapAction(treePos),
            new BreakBlockGoapAction(treePos),
            new CollectItemGoapAction(),
            new CraftPlanksRealGoapAction()
        );
    }
    
    @Override
    public String getName() {
        return "gather_planks_real";
    }
}
