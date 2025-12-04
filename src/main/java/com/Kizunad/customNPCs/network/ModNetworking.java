package com.Kizunad.customNPCs.network;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络通道注册。
 */
public final class ModNetworking {

    private static final String PROTOCOL_VERSION = "1";

    private ModNetworking() {}

    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(
            ServerboundTradePayload.TYPE,
            ServerboundTradePayload.STREAM_CODEC,
            ServerboundTradePayload::handle
        );
    }

    public static void hook(IEventBus modEventBus) {
        modEventBus.addListener(ModNetworking::register);
    }
}
