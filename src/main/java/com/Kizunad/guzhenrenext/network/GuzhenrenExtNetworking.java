package com.Kizunad.guzhenrenext.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * guzhenrenext 专用网络注册。
 */
public final class GuzhenrenExtNetworking {

    private static final String PROTOCOL_VERSION = "1";

    private GuzhenrenExtNetworking() {}

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(GuzhenrenExtNetworking::onRegisterPayloadHandlers);
    }

    private static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(
            ClientboundKongqiaoSyncPayload.TYPE,
            ClientboundKongqiaoSyncPayload.STREAM_CODEC,
            ClientboundKongqiaoSyncPayload::handle
        );
        registrar.playToServer(
            ServerboundKongqiaoActionPayload.TYPE,
            ServerboundKongqiaoActionPayload.STREAM_CODEC,
            ServerboundKongqiaoActionPayload::handle
        );
    }
}
