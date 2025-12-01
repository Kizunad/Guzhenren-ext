package com.Kizunad.customNPCs.ai.actions.interfaces;

import com.Kizunad.customNPCs.ai.actions.IAction;
import java.util.UUID;

/**
 * 攻击类动作接口 - 用于描述对目标发起攻击的动作
 */
public interface IAttackAction extends IAction {
    /**
     * 获取目标实体的 UUID
     * @return 目标实体 UUID
     */
    UUID getTargetUuid();

    /**
     * 检查攻击是否命中
     * @return 命中返回 true，否则返回 false
     */
    boolean hasHit();
}
