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

/**
 * 客户端同步空窍状态。
 */
public record ClientboundKongqiaoSyncPayload(CompoundTag data)
    implements CustomPacketPayload {

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
                ByteBufCodecs.COMPOUND_TAG.encode(buf, payload.data),
            buf ->
                new ClientboundKongqiaoSyncPayload(
                    ByteBufCodecs.COMPOUND_TAG.decode(buf)
                )
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
}
