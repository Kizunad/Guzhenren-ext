package com.Kizunad.customNPCs.tasks.reward;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.server.level.ServerPlayer;

/**
 * 任务奖励定义接口。
 */
public interface TaskRewardDefinition {

    TaskRewardType getType();

    /**
     * 将奖励应用到指定玩家。
     */
    void grant(ServerPlayer player, CustomNpcEntity npc);
}
