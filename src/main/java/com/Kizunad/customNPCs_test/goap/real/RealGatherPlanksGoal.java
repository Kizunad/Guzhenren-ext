package com.Kizunad.customNPCs_test.goap.real;

import com.Kizunad.customNPCs.ai.decision.goals.PlanBasedGoal;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.goap.CraftPlanksRealGoapAction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

import java.util.Arrays;
import java.util.List;

/**
 * 真实的收集木板目标 - 使用真实 Minecraft API
 * <p>
 * 此目标使用真实的 Minecraft API 完成完整流程：
 * 1. RealMoveToTree - 使用 PathNavigation 真正移动
 * 2. RealBreakBlock - 使用 level.destroyBlock() 真正破坏方块
 * 3. RealCollectItem - 搜索并收集掉落的 ItemEntity
 * 4.  CraftPlanks - 制作木板（模拟）
 * <p>
 * 目标：获得木板
 * 规划器将自动生成动作序列
 */
public class RealGatherPlanksGoal extends PlanBasedGoal {
    
    private final float priority;
    private final BlockPos treePos;
    
    /**
     * 创建真实的收集木板目标
     * @param priority 优先级
     * @param treePos 树木位置
     */
    public RealGatherPlanksGoal(float priority, BlockPos treePos) {
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
        // 提供使用真实 API 的动作链
        return Arrays.asList(
            new RealMoveToTreeAction(treePos),
            new RealBreakBlockAction(treePos),
            new RealCollectItemAction(treePos),
            new CraftPlanksRealGoapAction() // 使用之前的制作动作
        );
    }
    
    @Override
    public String getName() {
        return "real_gather_planks";
    }
}
