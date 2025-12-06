package com.Kizunad.customNPCs.network;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.tasks.TaskActionHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C2S：请求刷新当前 NPC 的任务列表。
 */
public record ServerboundRefreshTaskBoardPayload(int npcEntityId)
    implements CustomPacketPayload {

    public static final Type<ServerboundRefreshTaskBoardPayload> TYPE =
        new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                CustomNPCsMod.MODID,
                "refresh_task_board"
            )
        );

    public static final StreamCodec<
        RegistryFriendlyByteBuf,
        ServerboundRefreshTaskBoardPayload
    > STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        ServerboundRefreshTaskBoardPayload::npcEntityId,
        ServerboundRefreshTaskBoardPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        ServerboundRefreshTaskBoardPayload payload,
        IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof net.minecraft.server.level.ServerPlayer player)) {
                return;
            }
            TaskActionHandler.handleRefresh(player, payload.npcEntityId());
        });
    }
}
