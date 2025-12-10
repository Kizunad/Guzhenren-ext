package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.network.ClientboundKongqiaoSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 负责按需同步空窍数据到客户端。
 */
@EventBusSubscriber(
    modid = GuzhenrenExt.MODID,
    bus = EventBusSubscriber.Bus.GAME
)
public final class KongqiaoSyncService {

    private KongqiaoSyncService() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        var data = KongqiaoAttachments.getData(player);
        if (data == null || !data.isDirty()) {
            return;
        }
        PacketDistributor.sendToPlayer(
            player,
            new ClientboundKongqiaoSyncPayload(
                data.serializeNBT(player.level().registryAccess())
            )
        );
        data.clearDirty();
    }
}
