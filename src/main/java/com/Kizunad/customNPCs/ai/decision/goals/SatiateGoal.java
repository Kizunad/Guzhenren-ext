package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.goap.GoapEatAction;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;

/**
 * 进食目标:饥饿时规划进食,战斗/危险时不触发。
 * <p>
 * 该目标会在NPC饥饿时触发,使用GOAP系统规划进食行为。
 * 在危险情况下(战斗或有可见敌人)会被抑制。
 * </p>
 */
public class SatiateGoal extends PlanBasedGoal {

    /** 基础优先级,用于计算进食目标的最低优先级 */
    private static final double BASE_PRIORITY = 0.3;

    /** 进食动作实例 */
    private final GoapEatAction eatAction = new GoapEatAction();

    /**
     * 计算进食目标的优先级。
     * <p>
     * 优先级计算公式: BASE_PRIORITY + (1.0 - hungerPercent)
     * 饥饿度越低(越饿),优先级越高。
     * 在危险情况下优先级为0。
     * </p>
     *
     * @param mind NPC的思维接口
     * @param entity NPC实体
     * @return 优先级值,范围[0.0, 1.3]
     */
    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        double hungerPercent = mind.getStatus().getHungerPercent();
        if (!mind.getStatus().isHungry() || isInDanger(mind, entity)) {
            return 0.0f;
        }
        return (float) (BASE_PRIORITY + (1.0 - hungerPercent));
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        Object hasFood = mind.getCurrentWorldState(entity).getState(
            WorldStateKeys.HAS_FOOD
        );
        return mind.getStatus().isHungry() &&
            Boolean.TRUE.equals(hasFood) &&
            !isInDanger(mind, entity);
    }

    @Override
    public String getName() {
        return "satiate";
    }

    @Override
    public WorldState getDesiredState(INpcMind mind, LivingEntity entity) {
        WorldState desired = new WorldState();
        desired.setState(WorldStateKeys.IS_HUNGRY, false);
        desired.setState(WorldStateKeys.HUNGER_RESTORED, true);
        return desired;
    }

    @Override
    public List<IGoapAction> getAvailableActions(
        INpcMind mind,
        LivingEntity entity
    ) {
        return Collections.singletonList(eatAction);
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        return !mind.getStatus().isHungry() ||
            mind.getActionExecutor().isIdle() ||
            isInDanger(mind, entity);
    }

    private boolean isInDanger(INpcMind mind, LivingEntity entity) {
        Object danger = mind.getCurrentWorldState(entity).getState(
            WorldStateKeys.IN_DANGER
        );
        Object targetVisible = mind.getCurrentWorldState(entity).getState(
            WorldStateKeys.TARGET_VISIBLE
        );
        return Boolean.TRUE.equals(danger) || Boolean.TRUE.equals(targetVisible);
    }
}
