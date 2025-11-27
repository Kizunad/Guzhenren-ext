package com.Kizunad.customNPCs.ai.actions.interfaces;

import com.Kizunad.customNPCs.ai.actions.IAction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

/**
 * Interface for actions that involve interacting with blocks or entities.
 */
public interface IInteractAction extends IAction {
    /**
     * Gets the target position of the interaction.
     * @return The target BlockPos, or null if interacting with an entity.
     */
    BlockPos getTargetPos();

    /**
     * Gets the hand used for interaction.
     * @return The InteractionHand used.
     */
    InteractionHand getHand();
}
