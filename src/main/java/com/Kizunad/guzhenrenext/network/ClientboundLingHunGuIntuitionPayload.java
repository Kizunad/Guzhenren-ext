package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.client.LingHunGuIntuitionClientState;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 羚魂蛊灵觉：方向提示同步包（服务端 -> 客户端）。
 * <p>
 * angleRadians：相对玩家视角的角度（0=正前方，+右，-左，π/ -π=正后方）。
 * intensity：强度（0..1），用于控制白光的透明度与持续衰减。
 * durationTicks：提示持续时间（客户端会自动衰减并清理）。
 * </p>
 */
public record ClientboundLingHunGuIntuitionPayload(
    float angleRadians,
    float intensity,
    int durationTicks
) implements CustomPacketPayload {

    public static final Type<ClientboundLingHunGuIntuitionPayload> TYPE =
        new Type<>(
            ResourceLocation.fromNamespaceAndPath(
                GuzhenrenExt.MODID,
                "linghungu_intuition"
            )
        );

    public static final StreamCodec<ByteBuf, ClientboundLingHunGuIntuitionPayload> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public ClientboundLingHunGuIntuitionPayload decode(final ByteBuf buffer) {
                final FriendlyByteBuf buf = new FriendlyByteBuf(buffer);
                return new ClientboundLingHunGuIntuitionPayload(
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readVarInt()
                );
            }

            @Override
            public void encode(
                final ByteBuf buffer,
                final ClientboundLingHunGuIntuitionPayload value
            ) {
                final FriendlyByteBuf buf = new FriendlyByteBuf(buffer);
                buf.writeFloat(value.angleRadians);
                buf.writeFloat(value.intensity);
                buf.writeVarInt(value.durationTicks);
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        final ClientboundLingHunGuIntuitionPayload payload,
        final IPayloadContext context
    ) {
        context.enqueueWork(() -> LingHunGuIntuitionClientState.trigger(
            payload.angleRadians,
            payload.intensity,
            payload.durationTicks
        ));
    }
}
