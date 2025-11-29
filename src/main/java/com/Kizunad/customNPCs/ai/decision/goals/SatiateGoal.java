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
 * 进食目标：饥饿时规划进食，战斗/危险时不触发。
 */
public class SatiateGoal extends PlanBasedGoal {

    private final GoapEatAction eatAction = new GoapEatAction();

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        double hungerPercent = mind.getStatus().getHungerPercent();
        if (!mind.getStatus().isHungry() || isInDanger(mind, entity)) {
            return 0.0f;
        }
        return (float) (0.3 + (1.0 - hungerPercent));
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
