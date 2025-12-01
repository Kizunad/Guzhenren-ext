package com.Kizunad.customNPCs.ai.actions.interfaces;

import com.Kizunad.customNPCs.ai.actions.IAction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;

/**
 * 交互类动作接口 - 用于描述与方块或实体交互的动作
 */
public interface IInteractAction extends IAction {
    /**
     * 获取交互目标坐标
     * @return 目标方块坐标，若与实体交互则为 null
     */
    BlockPos getTargetPos();

    /**
     * 获取用于交互的手
     * @return 使用的 InteractionHand
     */
    InteractionHand getHand();
}
