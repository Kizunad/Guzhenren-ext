package com.Kizunad.customNPCs.ai.actions.interfaces;

import com.Kizunad.customNPCs.ai.actions.IAction;
import net.minecraft.world.item.ItemStack;

/**
 * Interface for actions that involve using an item.
 */
public interface IUseItemAction extends IAction {
    /**
     * Gets the item stack being used.
     * @return The ItemStack.
     */
    ItemStack getItemStack();

    /**
     * Checks if the item usage is complete.
     * @return true if usage is complete.
     */
    boolean isUsageComplete();
}
