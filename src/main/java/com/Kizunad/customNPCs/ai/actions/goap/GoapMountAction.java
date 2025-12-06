package com.Kizunad.customNPCs.ai.actions.goap;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.MountAction;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;

/**
 * GOAP 包装：在规划系统中执行 {@link MountAction}。
 */
public class GoapMountAction implements IGoapAction {

    private final MountAction wrappedAction;
    private final WorldState preconditions;
    private final WorldState effects;
    private final float cost;

    public GoapMountAction(UUID mountUuid, String proximityKey) {
        this(mountUuid, proximityKey, 1.0F);
    }

    public GoapMountAction(UUID mountUuid, String proximityKey, float cost) {
        this.wrappedAction = new MountAction(mountUuid);
        this.cost = cost;
        this.preconditions = new WorldState();
        this.preconditions.setState(WorldStateKeys.HAS_MOUNT_NEARBY, true);
        this.preconditions.setState(WorldStateKeys.IS_RIDING, false);
        if (mountUuid != null) {
            this.preconditions.setState(WorldStateKeys.MOUNT_UUID, mountUuid);
        }
        if (proximityKey != null && !proximityKey.isEmpty()) {
            this.preconditions.setState(proximityKey, true);
        }

        this.effects = new WorldState();
        this.effects.setState(WorldStateKeys.IS_RIDING, true);
        this.effects.setState(WorldStateKeys.HAS_MOUNT_NEARBY, false);
        if (mountUuid != null) {
            this.effects.setState(WorldStateKeys.MOUNT_UUID, mountUuid);
        }
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
        return "goap_mount";
    }
}
