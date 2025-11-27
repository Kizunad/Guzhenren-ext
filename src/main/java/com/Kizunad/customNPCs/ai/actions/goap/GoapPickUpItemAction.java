package com.Kizunad.customNPCs.ai.actions.goap;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.PickUpItemAction;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * GOAP wrapper for PickUpItemAction.
 */
public class GoapPickUpItemAction implements IGoapAction {

    private final PickUpItemAction wrappedAction;
    private final WorldState preconditions;
    private final WorldState effects;

    public GoapPickUpItemAction(ItemEntity targetItem) {
        this.wrappedAction = new PickUpItemAction(targetItem);
        
        this.preconditions = new WorldState();
        this.preconditions.setState("at_item_location", true);
        
        this.effects = new WorldState();
        this.effects.setState("has_item", true);
        this.effects.setState("item_picked", true);
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
        ActionStatus status = wrappedAction.tick(mind, entity);
        if (status == ActionStatus.SUCCESS) {
            mind.getMemory().rememberLongTerm("has_item", true);
            mind.getMemory().rememberLongTerm("item_picked", true);
        }
        return status;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        wrappedAction.start(mind, entity);
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        wrappedAction.stop(mind, entity);
    }

    @Override
    public boolean canInterrupt() {
        return wrappedAction.canInterrupt();
    }

    @Override
    public String getName() {
        return "goap_pick_up_item";
    }
}
