package com.Kizunad.customNPCs.ai.interaction;

import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

/**
 * 交易交互占位：提供服务端可调用入口，后续可挂具体交易界面。
 */
public final class NpcTradeHooks {

    private static final Component PLACEHOLDER_MESSAGE =
        Component.literal("[CustomNPC] 交易接口占位，后续可接入自定义交易/商人界面。");

    private NpcTradeHooks() {}

    public static InteractionResult tryOpenTrade(
        CustomNpcEntity npc,
        Player player,
        NpcTradeState tradeState
    ) {
        if (tradeState == null || !tradeState.isTradeEnabled()) {
            return InteractionResult.PASS;
        }
        if (!player.level().isClientSide()) {
            player.sendSystemMessage(PLACEHOLDER_MESSAGE);
        }
        return InteractionResult.SUCCESS;
    }
}
