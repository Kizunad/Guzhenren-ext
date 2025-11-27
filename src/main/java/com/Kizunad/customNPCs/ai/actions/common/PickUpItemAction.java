package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.interfaces.IInteractAction;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to pick up an item entity.
 * Usually involves moving to the item's location until collision occurs.
 */
public class PickUpItemAction extends AbstractStandardAction implements IInteractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(PickUpItemAction.class);
    private final ItemEntity targetItem;

    public PickUpItemAction(ItemEntity targetItem) {
        super("PickUpItemAction", targetItem.getUUID());
        this.targetItem = targetItem;
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        if (!targetItem.isAlive()) {
            LOGGER.warn("[PickUpItemAction] Target item is no longer alive.");
            return ActionStatus.FAILURE;
        }

        // Check if item is picked up (e.g. in inventory)
        if (!targetItem.isAlive()) {
             return ActionStatus.SUCCESS;
        }
        
        // Check distance
        if (mob.getBoundingBox().intersects(targetItem.getBoundingBox().inflate(1.0))) {
            // Execute pickup
            net.minecraft.world.item.ItemStack itemStack = targetItem.getItem().copy();
            mob.take(targetItem, itemStack.getCount());
            mob.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
            targetItem.discard();
            LOGGER.info("[PickUpItemAction] Picked up item: {}", itemStack.getHoverName().getString());
            return ActionStatus.SUCCESS;
        } else {
            // Too far, return RUNNING (assuming movement is handled elsewhere or we wait)
            // Ideally, this action should also handle movement if it's a "PickUp" action, 
            // but often we separate MoveTo and PickUp.
            // If we want it to be robust, we can try to move closer if needed, 
            // similar to InteractBlockAction.
            mob.getNavigation().moveTo(targetItem, 1.0);
            return ActionStatus.RUNNING;
        }
    }

    @Override
    public BlockPos getTargetPos() {
        return targetItem.blockPosition();
    }

    @Override
    public InteractionHand getHand() {
        return InteractionHand.MAIN_HAND;
    }

    @Override
    public boolean canInterrupt() {
        return true;
    }
}
