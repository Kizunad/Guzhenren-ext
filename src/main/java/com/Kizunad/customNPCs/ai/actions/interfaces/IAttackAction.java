package com.Kizunad.customNPCs.ai.actions.interfaces;

import com.Kizunad.customNPCs.ai.actions.IAction;
import java.util.UUID;

/**
 * Interface for actions that involve attacking a target.
 */
public interface IAttackAction extends IAction {
    /**
     * Gets the UUID of the target entity.
     * @return The target entity's UUID.
     */
    UUID getTargetUuid();

    /**
     * Checks if the attack was successful (hit the target).
     * @return true if the attack hit, false otherwise.
     */
    boolean hasHit();
}
