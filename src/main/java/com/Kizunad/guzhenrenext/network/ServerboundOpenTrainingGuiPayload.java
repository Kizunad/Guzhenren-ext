package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoService;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundOpenTrainingGuiPayload()
    implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
        GuzhenrenExt.MODID,
        "open_training_gui"
    );
    public static final Type<ServerboundOpenTrainingGuiPayload> TYPE = new Type<>(
        ID
    );

    public static final StreamCodec<ByteBuf, ServerboundOpenTrainingGuiPayload> STREAM_CODEC =
        StreamCodec.unit(new ServerboundOpenTrainingGuiPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        ServerboundOpenTrainingGuiPayload payload,
        IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            if (serverPlayer.level().isClientSide()) {
                return;
            }
            KongqiaoService.openFlyingSwordTrainingMenu(serverPlayer);
        });
    }
}
