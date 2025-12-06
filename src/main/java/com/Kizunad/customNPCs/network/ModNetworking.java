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
        registrar.playToClient(
            OpenInteractGuiPayload.TYPE,
            OpenInteractGuiPayload.STREAM_CODEC,
            OpenInteractGuiPayload::handle
        );
        registrar.playToServer(
            InteractActionPayload.TYPE,
            InteractActionPayload.STREAM_CODEC,
            InteractActionPayload::handle
        );
        registrar.playToServer(
            RequestMaterialConversionPayload.TYPE,
            RequestMaterialConversionPayload.STREAM_CODEC,
            RequestMaterialConversionPayload::handle
        );
        registrar.playToServer(
            RequestCraftingPayload.TYPE,
            RequestCraftingPayload.STREAM_CODEC,
            RequestCraftingPayload::handle
        );
        registrar.playToServer(
            ServerboundTradePayload.TYPE,
            ServerboundTradePayload.STREAM_CODEC,
            ServerboundTradePayload::handle
        );
        registrar.playToClient(
            SyncMaterialDataPayload.TYPE,
            SyncMaterialDataPayload.STREAM_CODEC,
            SyncMaterialDataPayload::handle
        );
    }

    public static void hook(IEventBus modEventBus) {
        modEventBus.addListener(ModNetworking::register);
    }
}
