package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.interfaces.IInteractAction;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 丢弃物品动作 - 将指定手中的物品丢到当前位置。
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class DropItemAction extends AbstractStandardAction implements IInteractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DropItemAction.class);

    private final InteractionHand hand;
    private final Integer inventorySlot;
    private boolean dropped;

    public DropItemAction() {
        this(InteractionHand.MAIN_HAND, null);
    }

    public DropItemAction(InteractionHand hand) {
        this(hand, null);
    }

    public DropItemAction(int inventorySlot) {
        this(null, inventorySlot);
    }

    private DropItemAction(InteractionHand hand, Integer inventorySlot) {
        super("DropItemAction");
        this.hand = hand;
        this.inventorySlot = inventorySlot;
        this.dropped = false;
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        if (dropped) {
            return ActionStatus.SUCCESS;
        }

        boolean fromInventory = inventorySlot != null;
        ItemStack toDrop;
        Runnable restore = () -> {};

        if (fromInventory) {
            if (mind == null) {
                LOGGER.warn("[DropItemAction] mind为空，无法访问背包");
                return ActionStatus.FAILURE;
            }
            NpcInventory inventory = mind.getInventory();
            ItemStack stack = inventory.getItem(inventorySlot);
            if (stack.isEmpty()) {
                LOGGER.warn("[DropItemAction] 背包槽位 {} 为空", inventorySlot);
                return ActionStatus.FAILURE;
            }
            toDrop = stack.copy();
            inventory.setItem(inventorySlot, ItemStack.EMPTY);
            restore = () -> inventory.setItem(inventorySlot, stack);
        } else {
            if (hand == null) {
                LOGGER.warn("[DropItemAction] 未指定手，无法丢弃");
                return ActionStatus.FAILURE;
            }
            ItemStack heldItem = mob.getItemInHand(hand);
            if (heldItem.isEmpty()) {
                LOGGER.warn("[DropItemAction] 手 {} 没有可丢弃的物品", hand);
                return ActionStatus.FAILURE;
            }
            toDrop = heldItem.copy();
            mob.setItemInHand(hand, ItemStack.EMPTY);
            restore = () -> mob.setItemInHand(hand, heldItem);
        }

        var droppedEntity = mob.spawnAtLocation(toDrop);
        if (droppedEntity == null) {
            LOGGER.warn("[DropItemAction] 生成掉落物失败");
            restore.run();
            return ActionStatus.FAILURE;
        }
        dropped = true;

        LOGGER.info(
            "[DropItemAction] 丢弃物品: {} @ {}",
            toDrop.getHoverName().getString(),
            droppedEntity.blockPosition().toShortString()
        );
        return ActionStatus.SUCCESS;
    }

    @Override
    protected void onStart(INpcMind mind, LivingEntity entity) {
        dropped = false;
    }

    @Override
    public BlockPos getTargetPos() {
        return null;
    }

    @Override
    public InteractionHand getHand() {
        return hand;
    }

    @Override
    public boolean canInterrupt() {
        return !dropped;
    }
}
