package com.Kizunad.guzhenrenext.bastion.event;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.network.BastionNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 基地同步事件 - 处理玩家登录和维度切换时的基地数据同步。
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class BastionSyncEvents {

    private BastionSyncEvents() {
        // 工具类
    }

    /**
     * 玩家登录时同步附近基地。
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BastionNetworkHandler.syncNearbyBastionsToPlayer(player);
        }
    }

    /**
     * 玩家切换维度后同步新维度的基地。
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BastionNetworkHandler.syncNearbyBastionsToPlayer(player);
        }
    }

    /**
     * 玩家重生后同步附近基地。
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BastionNetworkHandler.syncNearbyBastionsToPlayer(player);
        }
    }
}
