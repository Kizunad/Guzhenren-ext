package com.Kizunad.guzhenrenext.kongqiao.event;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.network.PacketSyncTweakConfig;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 玩家登录时同步调整面板配置，保证客户端 UI 拿到最新偏好。
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class TweakConfigSyncEvents {

    private TweakConfigSyncEvents() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        final TweakConfig config = KongqiaoAttachments.getTweakConfig(player);
        if (config == null) {
            return;
        }
        PacketDistributor.sendToPlayer(player, new PacketSyncTweakConfig(config));
    }
}

