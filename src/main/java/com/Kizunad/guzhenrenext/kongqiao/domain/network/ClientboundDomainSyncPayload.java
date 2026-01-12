package com.Kizunad.guzhenrenext.kongqiao.domain.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.domain.DomainData;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 领域同步包（服务端 -> 客户端）。
 * <p>
 * 目标：同步领域的中心/半径/纹理等信息，用于客户端世界渲染。
 * </p>
 */
public record ClientboundDomainSyncPayload(
    UUID domainId,
    UUID ownerUuid,
    double centerX,
    double centerY,
    double centerZ,
    double radius,
    int level,
    String texturePath,
    double heightOffset,
    float alpha,
    float rotationSpeed
) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "domain_sync"
    );

    public static final Type<ClientboundDomainSyncPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundDomainSyncPayload> STREAM_CODEC =
        StreamCodec.of(
            (buf, payload) -> {
                buf.writeUUID(payload.domainId);
                buf.writeUUID(payload.ownerUuid);
                buf.writeDouble(payload.centerX);
                buf.writeDouble(payload.centerY);
                buf.writeDouble(payload.centerZ);
                buf.writeDouble(payload.radius);
                buf.writeInt(payload.level);
                buf.writeUtf(payload.texturePath == null ? "" : payload.texturePath);
                buf.writeDouble(payload.heightOffset);
                buf.writeFloat(payload.alpha);
                buf.writeFloat(payload.rotationSpeed);
            },
            buf -> new ClientboundDomainSyncPayload(
                buf.readUUID(),
                buf.readUUID(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readInt(),
                buf.readUtf(),
                buf.readDouble(),
                buf.readFloat(),
                buf.readFloat()
            )
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundDomainSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                if (payload.texturePath == null || payload.texturePath.isBlank()) {
                    return;
                }
                ResourceLocation texture = ResourceLocation.parse(payload.texturePath);
                Class<?> clazz = Class.forName(
                    "com.Kizunad.guzhenrenext.kongqiao.domain.client.DomainRenderer"
                );
                clazz
                    .getMethod(
                        "registerDomain",
                        DomainData.class
                    )
                    .invoke(
                        null,
                        new DomainData(
                            payload.domainId,
                            payload.ownerUuid,
                            payload.centerX,
                            payload.centerY,
                            payload.centerZ,
                            payload.radius,
                            payload.level,
                            texture,
                            payload.heightOffset,
                            payload.alpha,
                            payload.rotationSpeed
                        )
                    );
            } catch (Exception ignored) {
                // Dedicated Server 环境或客户端类未加载时忽略
            }
        });
    }
}
