package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.client.KongqiaoSyncClientHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundKongqiaoSyncPayload(CompoundTag data, CompoundTag projection)
    implements CustomPacketPayload {

    public ClientboundKongqiaoSyncPayload {
        data = data == null ? new CompoundTag() : data;
        projection = projection == null ? new CompoundTag() : projection;
    }

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "kongqiao_sync"
    );
    public static final Type<ClientboundKongqiaoSyncPayload> TYPE = new Type<>(
        ID
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundKongqiaoSyncPayload> STREAM_CODEC =
        StreamCodec.of(
            (buf, payload) ->
                encode(buf, payload),
            ClientboundKongqiaoSyncPayload::decode
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() ->
            KongqiaoSyncClientHandler.applySync(this, context.flow())
        );
    }

    private static void encode(
        final RegistryFriendlyByteBuf buf,
        final ClientboundKongqiaoSyncPayload payload
    ) {
        ByteBufCodecs.COMPOUND_TAG.encode(buf, payload.data);
        ByteBufCodecs.COMPOUND_TAG.encode(buf, payload.projection);
    }

    private static ClientboundKongqiaoSyncPayload decode(
        final RegistryFriendlyByteBuf buf
    ) {
        return new ClientboundKongqiaoSyncPayload(
            ByteBufCodecs.COMPOUND_TAG.decode(buf),
            ByteBufCodecs.COMPOUND_TAG.decode(buf)
        );
    }
}
