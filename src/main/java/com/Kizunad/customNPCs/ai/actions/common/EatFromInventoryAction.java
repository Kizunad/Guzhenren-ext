package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 从背包/手中选择最佳食物并食用。
 * - 优先低价值食物，保留高价值物品（如金苹果）到最后。
 * - 可中断；失败时回滚手中物品。
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class EatFromInventoryAction extends AbstractStandardAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(EatFromInventoryAction.class);

    private ItemStack previousMainHand = ItemStack.EMPTY;
    private ItemStack consumedStack = ItemStack.EMPTY;
    private int consumedFromSlot = -1;
    private UseItemAction delegate;
    private boolean startedUse = false;

    public EatFromInventoryAction() {
        super("EatFromInventoryAction", null, CONFIG.getDefaultItemUseTicks() + CONFIG.getTimeoutBufferTicks(), 1, 0);
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        if (!startedUse) {
            if (!prepareFood(mind, mob)) {
                LOGGER.warn("[EatFromInventoryAction] 无可用食物");
                return ActionStatus.FAILURE;
            }
            startedUse = true;
        }

        ActionStatus status = delegate.tick(mind, mob);
        if (status == ActionStatus.SUCCESS) {
            mind.getStatus().eat(consumedStack, mob);
            returnLeftovers(mind, mob);
        } else if (status == ActionStatus.FAILURE) {
            rollback(mind, mob);
        }
        return status;
    }

    @Override
    protected void onStart(INpcMind mind, LivingEntity entity) {
        previousMainHand = entity.getMainHandItem().copy();
        startedUse = false;
        consumedFromSlot = -1;
        consumedStack = ItemStack.EMPTY;
    }

    @Override
    protected void onStop(INpcMind mind, LivingEntity entity) {
        rollback(mind, entity);
    }

    @Override
    public boolean canInterrupt() {
        return true;
    }

    private boolean prepareFood(INpcMind mind, Mob mob) {
        Selection best = selectBestFood(mind.getInventory(), mob);
        if (best == null || best.stack.isEmpty()) {
            return false;
        }

        consumedStack = best.stack.copy();
        consumedStack.setCount(1);
        consumedFromSlot = best.slot;

        if (best.slot >= 0) {
            ItemStack removed = mind.getInventory().removeItem(best.slot);
            mob.setItemInHand(InteractionHand.MAIN_HAND, removed);
        } else {
            mob.setItemInHand(InteractionHand.MAIN_HAND, consumedStack.copy());
        }

        delegate = new UseItemAction(mob.getMainHandItem().getItem());
        delegate.start(mind, mob);
        return true;
    }

    private void rollback(INpcMind mind, LivingEntity entity) {
        ItemStack hand = entity.getItemInHand(InteractionHand.MAIN_HAND);
        // 如果实体未实际消耗（如僵尸无法吃），模拟消耗一份
        if (consumedFromSlot >= 0 && !hand.isEmpty()) {
            hand.shrink(1);
        }
        if (!hand.isEmpty()) {
            ItemStack remaining = mind.getInventory().addItem(hand);
            if (!remaining.isEmpty()) {
                entity.spawnAtLocation(remaining);
            }
        }
        entity.setItemInHand(InteractionHand.MAIN_HAND, previousMainHand);
        if (consumedFromSlot >= 0 && !consumedStack.isEmpty()) {
            ItemStack leftover = mind.getInventory().addItem(consumedStack);
            if (!leftover.isEmpty()) {
                entity.spawnAtLocation(leftover);
            }
        }
    }

    private void returnLeftovers(INpcMind mind, LivingEntity entity) {
        ItemStack hand = entity.getItemInHand(InteractionHand.MAIN_HAND);
        if (!hand.isEmpty()) {
            ItemStack remaining = mind.getInventory().addItem(hand);
            if (!remaining.isEmpty()) {
                entity.spawnAtLocation(remaining);
            }
        }
        entity.setItemInHand(InteractionHand.MAIN_HAND, previousMainHand);
    }

    private Selection selectBestFood(NpcInventory inventory, LivingEntity entity) {
        Selection best = null;
        ItemStack main = entity.getMainHandItem();
        if (isEdible(main, entity)) {
            best = new Selection(main.copy(), -1, scoreFood(main, entity));
        }
        ItemStack off = entity.getOffhandItem();
        if (isEdible(off, entity)) {
            best = better(best, new Selection(off.copy(), -1, scoreFood(off, entity)));
        }
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (isEdible(stack, entity)) {
                best = better(best, new Selection(stack.copy(), i, scoreFood(stack, entity)));
            }
        }
        return best;
    }

    private boolean isEdible(ItemStack stack, LivingEntity entity) {
        return !stack.isEmpty() && stack.getFoodProperties(entity) != null;
    }

    private double scoreFood(ItemStack stack, LivingEntity entity) {
        var food = stack.getFoodProperties(entity);
        if (food == null) {
            return -Double.MAX_VALUE;
        }
        double base = food.nutrition() + food.saturation() * food.nutrition() * 2.0;
        for (var pair : food.effects()) {
            if (pair.effect() != null && !pair.effect().getEffect().value().isBeneficial()) {
                base -= 4.0;
            }
        }
        if (isHighValueFood(stack.getItem())) {
            base -= 100.0;
        }
        return base;
    }

    private boolean isHighValueFood(Item item) {
        return item == Items.GOLDEN_APPLE
            || item == Items.ENCHANTED_GOLDEN_APPLE
            || item == Items.GOLDEN_CARROT;
    }

    private Selection better(Selection a, Selection b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return b.score > a.score ? b : a;
    }

    private record Selection(ItemStack stack, int slot, double score) {}
}
