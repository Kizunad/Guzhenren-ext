package com.Kizunad.guzhenrenext.network;

import com.Kizunad.guzhenrenext.kongqiao.menu.NianTouMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketOpenNianTouGui() implements CustomPacketPayload {

public static final CustomPacketPayload.Type<PacketOpenNianTouGui> TYPE = new CustomPacketPayload.Type<>(
    ResourceLocation.fromNamespaceAndPath("guzhenrenext", "open_niantou_gui")
);
    
    // 无数据的空包
public static final StreamCodec<ByteBuf, PacketOpenNianTouGui> STREAM_CODEC = StreamCodec.unit(
    new PacketOpenNianTouGui()
);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketOpenNianTouGui payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                MenuProvider container = new SimpleMenuProvider(
                    (id, inventory, p) -> new NianTouMenu(id, inventory),
                    net.minecraft.network.chat.Component.translatable("gui.guzhenrenext.niantou.title")
                );
                player.openMenu(container);
            }
        });
    }
}
