package com.Kizunad.customNPCs.ai.actions.goap;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.EatFromInventoryAction;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

/**
 * GOAP 进食动作，包装 EatFromInventoryAction。
 */
public class GoapEatAction implements IGoapAction {

    private final WorldState preconditions;
    private final WorldState effects;
    private final EatFromInventoryAction action = new EatFromInventoryAction();

    public GoapEatAction() {
        preconditions = new WorldState();
        preconditions.setState(WorldStateKeys.HAS_FOOD, true);
        preconditions.setState(WorldStateKeys.IS_HUNGRY, true);

        effects = new WorldState();
        effects.setState(WorldStateKeys.HUNGER_RESTORED, true);
        effects.setState(WorldStateKeys.IS_HUNGRY, false);
    }

    @Override
    public WorldState getPreconditions() {
        return preconditions;
    }

    @Override
    public WorldState getEffects() {
        return effects;
    }

    @Override
    public float getCost() {
        return 1.0f;
    }

    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        ActionStatus status = action.tick(mind, entity);
        if (status == ActionStatus.SUCCESS) {
            mind
                .getMemory()
                .rememberShortTerm(WorldStateKeys.HUNGER_RESTORED, true, 200);
        }
        return status;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        action.start(mind, entity);
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        action.stop(mind, entity);
    }

    @Override
    public boolean canInterrupt() {
        return action.canInterrupt();
    }

    @Override
    public String getName() {
        return "goap_eat";
    }
}
