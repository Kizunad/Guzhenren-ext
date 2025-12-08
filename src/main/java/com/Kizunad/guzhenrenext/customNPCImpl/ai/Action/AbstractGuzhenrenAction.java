package com.Kizunad.guzhenrenext.customNPCImpl.ai.Action;

import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.Mob;

/**
 * 蛊真人模组动作基类。
 * <p>
 * 为所有与蛊虫相关的动作提供通用工具方法。
 * 例如：检查空窍、消耗真元、获取蛊虫实例等。
 */
public abstract class AbstractGuzhenrenAction extends AbstractStandardAction {

    protected AbstractGuzhenrenAction(String actionName) {
        super(actionName);
    }

    protected AbstractGuzhenrenAction(
        String actionName,
        java.util.UUID targetUuid
    ) {
        super(actionName, targetUuid);
    }

    /**
     * 检查 NPC 是否有足够的真元。
     * (待接入具体 API)
     */
    protected boolean hasPrimevalEssence(Mob mob, int amount) {
        // TODO: 调用蛊真人 API 获取 Capability 检查真元
        return true;
    }

    /**
     * 检查 NPC 是否拥有特定蛊虫。
     * (待接入具体 API)
     */
    protected boolean hasGuWorm(Mob mob, String guId) {
        // TODO: 检查空窍或背包
        return false;
    }

    @Override
    protected void onStart(
        INpcMind mind,
        net.minecraft.world.entity.LivingEntity entity
    ) {
        super.onStart(mind, entity);
        // 可以在这里添加通用的蛊真人动作初始化逻辑
        // 例如：显示开始运功的粒子效果
    }
}
