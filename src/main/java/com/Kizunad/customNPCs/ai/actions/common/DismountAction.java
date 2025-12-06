package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 下马动作：确保 NPC 退出当前载具。
 */
public class DismountAction extends AbstractStandardAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DismountAction.class);

    public DismountAction() {
        super("DismountAction");
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        if (!mob.isPassenger()) {
            return ActionStatus.SUCCESS;
        }

        Entity vehicle = mob.getVehicle();
        mob.stopRiding();
        if (mob.isPassenger()) {
            LOGGER.debug(
                "[DismountAction] stopRiding 未立即生效，等待下个 tick | npc={}, vehicle={}",
                mob.getUUID(),
                vehicle != null ? vehicle.getUUID() : "none"
            );
            return ActionStatus.RUNNING;
        }

        return ActionStatus.SUCCESS;
    }

    @Override
    public boolean canInterrupt() {
        // 下马同样是轻量操作，允许被打断
        return true;
    }
}
