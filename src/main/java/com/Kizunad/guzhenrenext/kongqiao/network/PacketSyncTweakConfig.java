package com.Kizunad.guzhenrenext.kongqiao.network;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 同步玩家 TweakConfig（客户端侧用于 UI 渲染）。
 */
public record PacketSyncTweakConfig(TweakConfig config)
    implements CustomPacketPayload {

    private static final ResourceLocation ID =
        ResourceLocation.fromNamespaceAndPath("guzhenrenext", "sync_tweak_config");
    public static final Type<PacketSyncTweakConfig> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, PacketSyncTweakConfig> STREAM_CODEC =
        new StreamCodec<>() {
            @Override
            public PacketSyncTweakConfig decode(final ByteBuf buffer) {
                final FriendlyByteBuf buf = new FriendlyByteBuf(buffer);
                final Set<String> disabled = new HashSet<>();
                final int disabledSize = buf.readVarInt();
                for (int i = 0; i < disabledSize; i++) {
                    disabled.add(buf.readUtf());
                }

                final int wheelSize = buf.readVarInt();
                final java.util.List<String> wheel = new java.util.ArrayList<>();
                for (int i = 0; i < wheelSize; i++) {
                    wheel.add(buf.readUtf());
                }

                final TweakConfig config = new TweakConfig();
                config.setDisabledPassives(disabled);
                config.setWheelSkills(wheel);
                return new PacketSyncTweakConfig(config);
            }

            @Override
            public void encode(final ByteBuf buffer, final PacketSyncTweakConfig value) {
                final FriendlyByteBuf buf = new FriendlyByteBuf(buffer);
                final TweakConfig config = value.config;
                final Set<String> disabled = config != null
                    ? config.getDisabledPassives()
                    : java.util.Set.of();
                buf.writeVarInt(disabled.size());
                for (String id : disabled) {
                    buf.writeUtf(id);
                }

                final java.util.List<String> wheel = config != null
                    ? config.getWheelSkills()
                    : java.util.List.of();
                buf.writeVarInt(wheel.size());
                for (String id : wheel) {
                    buf.writeUtf(id);
                }
            }
        };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final PacketSyncTweakConfig payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null) {
                return;
            }
            context.player().setData(
                KongqiaoAttachments.TWEAK_CONFIG.get(),
                payload.config
            );
        });
    }
}

