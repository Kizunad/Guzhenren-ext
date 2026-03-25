package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.entry.ApertureEntryChannel;
import com.Kizunad.guzhenrenext.xianqiao.entry.ApertureEntryRuntime;
import com.Kizunad.guzhenrenext.xianqiao.entry.ApertureInitializationResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundApertureEntryPayload() implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "aperture_entry"
    );

    public static final Type<ServerboundApertureEntryPayload> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, ServerboundApertureEntryPayload> STREAM_CODEC =
        StreamCodec.unit(new ServerboundApertureEntryPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundApertureEntryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            ApertureInitializationResult result = ApertureEntryRuntime.trigger(
                serverPlayer,
                ApertureEntryChannel.HUB_V1_GAMEPLAY
            );
            if (!result.message().isBlank()) {
                serverPlayer.sendSystemMessage(Component.literal(result.message()));
            }
        });
    }
}
