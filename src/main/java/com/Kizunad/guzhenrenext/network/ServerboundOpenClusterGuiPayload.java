package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.cluster.ClusterNetworkSyncHelper;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoService;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundOpenClusterGuiPayload() implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "open_cluster_gui"
    );
    public static final Type<ServerboundOpenClusterGuiPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, ServerboundOpenClusterGuiPayload> STREAM_CODEC =
        StreamCodec.unit(new ServerboundOpenClusterGuiPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        ServerboundOpenClusterGuiPayload payload,
        IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            if (serverPlayer.level().isClientSide()) {
                return;
            }
            ClusterNetworkSyncHelper.syncToPlayer(serverPlayer);
            KongqiaoService.openFlyingSwordClusterMenu(serverPlayer);
        });
    }
}
