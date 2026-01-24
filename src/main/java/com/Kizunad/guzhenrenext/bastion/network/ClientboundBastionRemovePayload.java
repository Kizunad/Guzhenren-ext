package com.Kizunad.guzhenrenext.bastion.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 基地移除包（服务端 -> 客户端）。
 * <p>
 * 通知客户端移除指定基地的缓存数据，停止渲染其边界。
 * </p>
 *
 * @param bastionId 要移除的基地唯一标识符
 */
public record ClientboundBastionRemovePayload(
        UUID bastionId
) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "bastion_remove"
    );

    public static final Type<ClientboundBastionRemovePayload> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBastionRemovePayload> STREAM_CODEC =
        StreamCodec.of(
            (buf, payload) -> buf.writeUUID(payload.bastionId),
            buf -> new ClientboundBastionRemovePayload(buf.readUUID())
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端处理：从客户端缓存移除基地数据。
     */
    public static void handle(ClientboundBastionRemovePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> clazz = Class.forName(
                    "com.Kizunad.guzhenrenext.bastion.client.BastionClientCache"
                );
                clazz.getMethod("unregister", UUID.class).invoke(null, payload.bastionId);
            } catch (Exception ignored) {
                // Dedicated Server 环境或客户端类未加载时忽略
            }
        });
    }
}
