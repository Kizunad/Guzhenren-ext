package com.Kizunad.guzhenrenext.kongqiao.network;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

public record PacketSyncNianTouUnlocks(Set<ResourceLocation> unlockedItems)
    implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSyncNianTouUnlocks> TYPE =
        new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                "guzhenrenext",
                "sync_niantou_unlocks"
            )
        );

    public static final StreamCodec<ByteBuf, PacketSyncNianTouUnlocks> STREAM_CODEC =
        StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.apply(
                ByteBufCodecs.collection(HashSet::new)
            ),
            PacketSyncNianTouUnlocks::unlockedItems,
            PacketSyncNianTouUnlocks::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncNianTouUnlocks payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Client Side Handling
            if (context.player() == null) {
                return;
            }
            NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(context.player());
            if (unlocks == null) {
                unlocks = new NianTouUnlocks();
                context.player()
                    .setData(KongqiaoAttachments.NIANTOU_UNLOCKS.get(), unlocks);
            }
            unlocks.setUnlockedItems(payload.unlockedItems());
        });
    }
}
