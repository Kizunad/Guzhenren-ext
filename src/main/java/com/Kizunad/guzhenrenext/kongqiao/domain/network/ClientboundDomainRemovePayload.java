package com.Kizunad.guzhenrenext.kongqiao.domain.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 领域移除包（服务端 -> 客户端）。
 */
public record ClientboundDomainRemovePayload(UUID domainId) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "domain_remove"
    );

    public static final Type<ClientboundDomainRemovePayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDomainRemovePayload> STREAM_CODEC =
        StreamCodec.of(
            (buf, payload) -> buf.writeUUID(payload.domainId),
            buf -> new ClientboundDomainRemovePayload(buf.readUUID())
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundDomainRemovePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> clazz = Class.forName(
                    "com.Kizunad.guzhenrenext.kongqiao.domain.client.DomainRenderer"
                );
                clazz.getMethod("removeDomain", UUID.class).invoke(null, payload.domainId);
            } catch (Exception ignored) {
                // Dedicated Server 环境或客户端类未加载时忽略
            }
        });
    }
}
