package com.Kizunad.guzhenrenext.kongqiao.network;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;
import java.util.Set;

public record PacketSyncNianTouUnlocks(NianTouUnlocks data) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSyncNianTouUnlocks> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("guzhenrenext", "sync_niantou_unlocks"));

    public static final StreamCodec<ByteBuf, PacketSyncNianTouUnlocks> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PacketSyncNianTouUnlocks decode(ByteBuf buffer) {
            FriendlyByteBuf buf = new FriendlyByteBuf(buffer);
            NianTouUnlocks unlocks = new NianTouUnlocks();
            
            // 1. Unlocked Items
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++) {
                ResourceLocation itemId = buf.readResourceLocation();
                int usageCount = buf.readVarInt();
                for (int j = 0; j < usageCount; j++) {
                    unlocks.unlock(itemId, buf.readUtf());
                }
                if (usageCount == 0) {
                    unlocks.unlock(itemId);
                }
            }
            
            // 2. Process
            if (buf.readBoolean()) {
                ResourceLocation id = buf.readResourceLocation();
                int remaining = buf.readVarInt();
                int total = buf.readVarInt();
                int cost = buf.readVarInt();
                String usageId = buf.readUtf();
                unlocks.startProcess(id, usageId, total, cost);
                // 覆盖 remaining (startProcess 会重置 remaining 为 total)
                unlocks.getCurrentProcess().remainingTicks = remaining;
            }

            // 3. Shazhao Unlocks
            int shazhaoCount = buf.readVarInt();
            for (int i = 0; i < shazhaoCount; i++) {
                unlocks.unlockShazhao(buf.readResourceLocation());
            }

            // 4. Shazhao Message
            unlocks.setShazhaoMessage(buf.readUtf());
            
            return new PacketSyncNianTouUnlocks(unlocks);
        }

        @Override
        public void encode(ByteBuf buffer, PacketSyncNianTouUnlocks value) {
            FriendlyByteBuf buf = new FriendlyByteBuf(buffer);
            NianTouUnlocks unlocks = value.data;
            
            // 1. Unlocked Items
            Map<ResourceLocation, Set<String>> entries = unlocks.getUnlockedUsageMap();
            buf.writeVarInt(entries.size());
            for (Map.Entry<ResourceLocation, Set<String>> entry : entries.entrySet()) {
                buf.writeResourceLocation(entry.getKey());
                Set<String> usages = entry.getValue();
                buf.writeVarInt(usages.size());
                for (String usageId : usages) {
                    buf.writeUtf(usageId);
                }
            }
            
            // 2. Process
            NianTouUnlocks.UnlockProcess process = unlocks.getCurrentProcess();
            if (process != null) {
                buf.writeBoolean(true);
                buf.writeResourceLocation(process.itemId);
                buf.writeVarInt(process.remainingTicks);
                buf.writeVarInt(process.totalTicks);
                buf.writeVarInt(process.totalCost);
                buf.writeUtf(process.usageId);
            } else {
                buf.writeBoolean(false);
            }

            // 3. Shazhao Unlocks
            Set<ResourceLocation> shazhao = unlocks.getUnlockedShazhao();
            buf.writeVarInt(shazhao.size());
            for (ResourceLocation id : shazhao) {
                buf.writeResourceLocation(id);
            }

            // 4. Shazhao Message
            buf.writeUtf(unlocks.getShazhaoMessage());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncNianTouUnlocks payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null) {
                return;
            }
            
            // 直接覆盖客户端的数据
            context.player().setData(KongqiaoAttachments.NIANTOU_UNLOCKS.get(), payload.data);
        });
    }
}
