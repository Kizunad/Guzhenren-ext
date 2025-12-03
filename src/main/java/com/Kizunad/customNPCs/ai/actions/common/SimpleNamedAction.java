package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.Mob;

/**
 * 占位动作：用于映射 LLM 返回的仅名称动作，立即成功以避免未知动作被丢弃。
 */
public class SimpleNamedAction extends AbstractStandardAction {

    public SimpleNamedAction(String name) {
        super(
            name,
            null,
            CONFIG.getDefaultTimeoutTicks(),
            CONFIG.getDefaultMaxRetries(),
            CONFIG.getDefaultNavRange()
        );
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob entity) {
        return ActionStatus.SUCCESS;
    }

    @Override
    public boolean canInterrupt() {
        return true;
    }
}
