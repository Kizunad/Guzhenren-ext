package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.cluster.ClusterClientStateCache;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundClusterStatePayload(
    int currentLoad,
    int maxComputation,
    List<UUID> activeSwordUuids
) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "cluster_state"
    );
    public static final Type<ClientboundClusterStatePayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundClusterStatePayload> STREAM_CODEC =
        StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.currentLoad);
                buf.writeInt(payload.maxComputation);
                buf.writeInt(payload.activeSwordUuids.size());
                for (UUID uuid : payload.activeSwordUuids) {
                    buf.writeUUID(uuid);
                }
            },
            buf -> {
                int load = buf.readInt();
                int max = buf.readInt();
                int size = Math.max(0, buf.readInt());
                List<UUID> uuids = new ArrayList<>(size);
                for (int index = 0; index < size; index++) {
                    uuids.add(buf.readUUID());
                }
                return new ClientboundClusterStatePayload(load, max, uuids);
            }
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow() != PacketFlow.CLIENTBOUND) {
                return;
            }
            ClusterClientStateCache.apply(this);
        });
    }
}
