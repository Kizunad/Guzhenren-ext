package com.Kizunad.customNPCs.ai.actions.base;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * 移动到主人身边的动作。
 * <p>
 * 效果：near_owner = true
 */
public class MoveToOwnerAction extends AbstractStandardAction implements IGoapAction {

    private static final double FOLLOW_DISTANCE = 3.0;
    private static final double SPEED_MODIFIER = 1.2;
    private static final float FOLLOW_COST = 5.0f;
    private MoveToAction currentDelegate;

    public MoveToOwnerAction() {
        super("move_to_owner");
    }

    @Override
    public float getCost() {
        return FOLLOW_COST;
    }

    @Override
    public WorldState getPreconditions() {
        return new WorldState();
    }

    @Override
    public WorldState getEffects() {
        WorldState effects = new WorldState();
        effects.setState(WorldStateKeys.NEAR_OWNER, true);
        return effects;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        super.start(mind, entity);
        UUID ownerId = mind.getMemory().getMemory(WorldStateKeys.OWNER_UUID, UUID.class);
        if (ownerId != null && entity.level() instanceof ServerLevel serverLevel) {
            ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerId);
            if (owner != null) {
                this.currentDelegate = new MoveToAction(owner, SPEED_MODIFIER, FOLLOW_DISTANCE);
                this.currentDelegate.start(mind, entity);
            }
        }
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob entity) {
        if (currentDelegate == null) {
             // 尝试在 tick 中初始化（如果 start 失败或者 owner 刚上线）
             UUID ownerId = mind.getMemory().getMemory(WorldStateKeys.OWNER_UUID, UUID.class);
             if (ownerId != null && entity.level() instanceof ServerLevel serverLevel) {
                 ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerId);
                 if (owner != null) {
                     currentDelegate = new MoveToAction(owner, SPEED_MODIFIER, FOLLOW_DISTANCE);
                     currentDelegate.start(mind, entity);
                 }
             }
        }
        
        if (currentDelegate == null) {
            return ActionStatus.FAILURE;
        }

        ActionStatus status = currentDelegate.tick(mind, entity);
        if (status == ActionStatus.SUCCESS) {
            mind.getMemory().rememberLongTerm(WorldStateKeys.NEAR_OWNER, true);
        }
        return status;
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        super.stop(mind, entity);
        if (currentDelegate != null) {
            currentDelegate.stop(mind, entity);
            currentDelegate = null;
        }
    }

    @Override
    public boolean canInterrupt() {
        return true;
    }

    @Override
    public String getName() {
        return "move_to_owner";
    }
}
