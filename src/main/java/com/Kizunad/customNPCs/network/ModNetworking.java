package com.Kizunad.customNPCs.network;

//import com.Kizunad.customNPCs.network.ServerboundRefreshTaskBoardPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
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
        if (FMLEnvironment.dist == Dist.CLIENT) {
            registrar.playToClient(
                OpenInteractGuiPayload.TYPE,
                OpenInteractGuiPayload.STREAM_CODEC,
                (payload, context) -> com.Kizunad.customNPCs.client.network.OpenInteractGuiClientHandler
                    .handle(payload)
            );
            registrar.playToClient(
                OpenTaskBoardPayload.TYPE,
                OpenTaskBoardPayload.STREAM_CODEC,
                (payload, context) -> com.Kizunad.customNPCs.client.network.OpenTaskBoardClientHandler
                    .handle(payload)
            );
        } else {
            registrar.playToClient(
                OpenInteractGuiPayload.TYPE,
                OpenInteractGuiPayload.STREAM_CODEC,
                OpenInteractGuiPayload::handle
            );
            registrar.playToClient(
                OpenTaskBoardPayload.TYPE,
                OpenTaskBoardPayload.STREAM_CODEC,
                OpenTaskBoardPayload::handle
            );
        }
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
        registrar.playToServer(
            ServerboundAcceptTaskPayload.TYPE,
            ServerboundAcceptTaskPayload.STREAM_CODEC,
            ServerboundAcceptTaskPayload::handle
        );
        registrar.playToServer(
            ServerboundSubmitTaskPayload.TYPE,
            ServerboundSubmitTaskPayload.STREAM_CODEC,
            ServerboundSubmitTaskPayload::handle
        );
        registrar.playToServer(
            ServerboundRefreshTaskBoardPayload.TYPE,
            ServerboundRefreshTaskBoardPayload.STREAM_CODEC,
            ServerboundRefreshTaskBoardPayload::handle
        );
    }

    public static void hook(IEventBus modEventBus) {
        modEventBus.addListener(ModNetworking::register);
    }
}
