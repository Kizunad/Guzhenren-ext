package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.network.PacketSyncNianTouUnlocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * guzhenrenext 专用网络注册。
 */
public final class GuzhenrenExtNetworking {

    private static final String PROTOCOL_VERSION = "1.0.0";

    private GuzhenrenExtNetworking() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(GuzhenrenExtNetworking::registerPackets);
    }

    private static void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(GuzhenrenExt.MODID)
            .versioned(PROTOCOL_VERSION);

        registrar.playToServer(
            PacketOpenNianTouGui.TYPE,
            PacketOpenNianTouGui.STREAM_CODEC,
            PacketOpenNianTouGui::handle
        );
        registrar.playToServer(
            ServerboundKongqiaoActionPayload.TYPE,
            ServerboundKongqiaoActionPayload.STREAM_CODEC,
            ServerboundKongqiaoActionPayload::handle
        );
        registrar.playToClient(
            ClientboundKongqiaoSyncPayload.TYPE,
            ClientboundKongqiaoSyncPayload.STREAM_CODEC,
            ClientboundKongqiaoSyncPayload::handle
        );

        registrar.playToClient(
            PacketSyncNianTouUnlocks.TYPE,
            PacketSyncNianTouUnlocks.STREAM_CODEC,
            PacketSyncNianTouUnlocks::handle
        );
    }
}
