package com.Kizunad.guzhenrenext.customNPCImpl.ai.Action;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.Mob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 蛊真人模组占位 Action。
 * <p>
 * 这是一个极简的 Action 骨架，用于作为后续实现具体蛊真人逻辑的起点。
 * 不包含任何实际的游戏行为。
 */
public class GuzhenrenPlaceholderAction extends AbstractGuzhenrenAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        GuzhenrenPlaceholderAction.class
    );

    public GuzhenrenPlaceholderAction() {
        super("GuzhenrenPlaceholderAction");
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        // 这是一个占位 Action，立即成功。
        LOGGER.debug("[GuzhenrenPlaceholderAction] 占位 Action 执行完毕。");
        return ActionStatus.SUCCESS;
    }

    @Override
    public boolean canInterrupt() {
        return true; // 占位 Action 通常可以中断
    }

    @Override
    public void onStart(
        INpcMind mind,
        net.minecraft.world.entity.LivingEntity entity
    ) {
        super.onStart(mind, entity);
        LOGGER.info("[GuzhenrenPlaceholderAction] 占位 Action 启动。");
    }

    @Override
    public void onStop(
        INpcMind mind,
        net.minecraft.world.entity.LivingEntity entity
    ) {
        super.onStop(mind, entity);
        LOGGER.info("[GuzhenrenPlaceholderAction] 占位 Action 停止。");
    }
}
