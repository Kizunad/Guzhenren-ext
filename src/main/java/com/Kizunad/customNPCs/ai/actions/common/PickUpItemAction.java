package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.interfaces.IInteractAction;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to pick up an item entity.
 * Usually involves moving to the item's location until collision occurs.
 */
public class PickUpItemAction extends AbstractStandardAction implements IInteractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(PickUpItemAction.class);
    private final ItemEntity targetItem;
    private String lastReason = "";

    public PickUpItemAction(ItemEntity targetItem) {
        super("PickUpItemAction", targetItem.getUUID());
        this.targetItem = targetItem;
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        lastReason = "";
        if (!targetItem.isAlive()) {
            LOGGER.warn("[PickUpItemAction] Target item is no longer alive.");
            lastReason = "target_missing";
            return ActionStatus.FAILURE;
        }

        NpcInventory inventory = mind != null ? mind.getInventory() : null;

        // Check distance
        if (mob.getBoundingBox().intersects(targetItem.getBoundingBox().inflate(1.0))) {
            // Execute pickup
            net.minecraft.world.item.ItemStack itemStack = targetItem.getItem().copy();
            mob.take(targetItem, itemStack.getCount());

            boolean stored = false;
            ItemStack remaining = itemStack;
            if (inventory != null) {
                remaining = inventory.addItem(itemStack);
                stored = remaining.isEmpty();
            }

            if (!stored && mob.getMainHandItem().isEmpty()) {
                mob.setItemInHand(InteractionHand.MAIN_HAND, remaining);
                remaining = ItemStack.EMPTY;
                stored = true;
            }

            if (!remaining.isEmpty()) {
                mob.spawnAtLocation(remaining);
                LOGGER.warn(
                    "[PickUpItemAction] 背包与主手已满，已将剩余物品掉落"
                );
            }

            targetItem.discard();
            LOGGER.info(
                "[PickUpItemAction] Picked up item: {} (stored: {})",
                itemStack.getHoverName().getString(),
                stored
            );
            return stored ? ActionStatus.SUCCESS : ActionStatus.FAILURE;
        } else {
            LOGGER.warn(
                "[PickUpItemAction] Target too far to pick up directly"
            );
            lastReason = "too_far";
            return ActionStatus.FAILURE;
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

    @Override
    public com.Kizunad.customNPCs.ai.actions.ActionResult tickWithReason(
        INpcMind mind,
        net.minecraft.world.entity.LivingEntity entity
    ) {
        ActionStatus status = tick(mind, entity);
        return new com.Kizunad.customNPCs.ai.actions.ActionResult(status, lastReason);
    }

    @Override
    protected void onStart(INpcMind mind, net.minecraft.world.entity.LivingEntity entity) {
        super.onStart(mind, entity);
        lastReason = "";
    }
}
