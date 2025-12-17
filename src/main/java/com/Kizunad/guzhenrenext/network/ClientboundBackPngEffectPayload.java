package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.client.effect.BackPngTimedEffects;
import com.Kizunad.renderPNG.client.BackPngEffect;
import com.Kizunad.renderPNG.client.BackPngEffectManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端背后 PNG 特效同步包。
 * <p>
 * 服务端触发技能后发送给客户端，由客户端设置 {@link BackPngEffectManager} 并按 duration 自动清理。
 * </p>
 */
public record ClientboundBackPngEffectPayload(
    int entityId,
    int durationTicks,
    ResourceLocation texture,
    float width,
    float height,
    float backOffset,
    float upOffset,
    int argbColor,
    boolean fullBright
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ClientboundBackPngEffectPayload> TYPE =
        new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, "back_png_effect")
        );

    public static final StreamCodec<ByteBuf, ClientboundBackPngEffectPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public ClientboundBackPngEffectPayload decode(ByteBuf buffer) {
                FriendlyByteBuf buf = new FriendlyByteBuf(buffer);
                int entityId = buf.readVarInt();
                int duration = buf.readVarInt();
                ResourceLocation texture = buf.readResourceLocation();
                float width = buf.readFloat();
                float height = buf.readFloat();
                float backOffset = buf.readFloat();
                float upOffset = buf.readFloat();
                int argb = buf.readInt();
                boolean fullBright = buf.readBoolean();
                return new ClientboundBackPngEffectPayload(
                    entityId,
                    duration,
                    texture,
                    width,
                    height,
                    backOffset,
                    upOffset,
                    argb,
                    fullBright
                );
            }

            @Override
            public void encode(ByteBuf buffer, ClientboundBackPngEffectPayload value) {
                FriendlyByteBuf buf = new FriendlyByteBuf(buffer);
                buf.writeVarInt(value.entityId);
                buf.writeVarInt(value.durationTicks);
                buf.writeResourceLocation(value.texture);
                buf.writeFloat(value.width);
                buf.writeFloat(value.height);
                buf.writeFloat(value.backOffset);
                buf.writeFloat(value.upOffset);
                buf.writeInt(value.argbColor);
                buf.writeBoolean(value.fullBright);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundBackPngEffectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            BackPngEffect effect = new BackPngEffect(
                payload.texture,
                payload.width,
                payload.height,
                payload.backOffset,
                payload.upOffset,
                payload.argbColor,
                payload.fullBright
            );
            BackPngEffectManager.setForEntity(payload.entityId, effect);
            BackPngTimedEffects.start(payload.entityId, payload.durationTicks);
        });
    }
}

