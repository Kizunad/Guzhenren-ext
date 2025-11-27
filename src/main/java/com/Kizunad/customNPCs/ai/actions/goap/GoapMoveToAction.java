package com.Kizunad.customNPCs.ai.actions.goap;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.base.MoveToAction;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * GOAP wrapper for MoveToAction.
 */
public class GoapMoveToAction implements IGoapAction {

    private final MoveToAction wrappedAction;
    private final WorldState preconditions;
    private final WorldState effects;
    private final float cost;

    public GoapMoveToAction(Entity target, String targetLocationKey, float cost) {
        this.wrappedAction = new MoveToAction(target, 1.0); // Default speed 1.0
        this.cost = cost;
        
        this.preconditions = new WorldState();
        // Precondition: we know where the target is (visible or known location)
        // For simplicity, we assume we can always try to move if we have a target
        
        this.effects = new WorldState();
        this.effects.setState(targetLocationKey, true); // e.g. "at_item_location"
    }
    
    public GoapMoveToAction(Vec3 targetPos, String targetLocationKey, float cost) {
        this.wrappedAction = new MoveToAction(targetPos, 1.0); // Default speed 1.0
        this.cost = cost;
        
        this.preconditions = new WorldState();
        
        this.effects = new WorldState();
        this.effects.setState(targetLocationKey, true);
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
        return cost;
    }

    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        return wrappedAction.tick(mind, entity);
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
        return "goap_move_to";
    }
}
