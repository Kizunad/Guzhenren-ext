package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.cluster.ClusterNetworkSyncHelper;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.cluster.FlyingSwordClusterService;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundClusterActionPayload(Action action, UUID targetUuid)
    implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "cluster_action"
    );
    public static final Type<ServerboundClusterActionPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundClusterActionPayload> STREAM_CODEC =
        StreamCodec.of(
            (buf, payload) -> {
                buf.writeEnum(payload.action);
                buf.writeUUID(payload.targetUuid);
            },
            buf ->
                new ServerboundClusterActionPayload(
                    buf.readEnum(Action.class),
                    buf.readUUID()
                )
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        ServerboundClusterActionPayload payload,
        IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            if (serverPlayer.level().isClientSide()) {
                return;
            }

            if (payload.action == Action.DEPLOY) {
                FlyingSwordClusterService.deploy(serverPlayer, payload.targetUuid);
            } else if (payload.action == Action.RECALL) {
                FlyingSwordClusterService.recall(serverPlayer, payload.targetUuid);
            }

            ClusterNetworkSyncHelper.syncToPlayer(serverPlayer);
        });
    }

    public enum Action {
        DEPLOY,
        RECALL,
    }
}
