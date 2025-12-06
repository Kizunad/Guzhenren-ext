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
 * C2S：玩家请求接受任务。
 */
public record ServerboundAcceptTaskPayload(
    int npcEntityId,
    ResourceLocation taskId
) implements CustomPacketPayload {

    public static final Type<ServerboundAcceptTaskPayload> TYPE =
        new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                CustomNPCsMod.MODID,
                "accept_task"
            )
        );

    public static final StreamCodec<
        RegistryFriendlyByteBuf,
        ServerboundAcceptTaskPayload
    > STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        ServerboundAcceptTaskPayload::npcEntityId,
        ResourceLocation.STREAM_CODEC,
        ServerboundAcceptTaskPayload::taskId,
        ServerboundAcceptTaskPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        ServerboundAcceptTaskPayload payload,
        IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof net.minecraft.server.level.ServerPlayer player)) {
                return;
            }
            TaskActionHandler.handleAccept(
                player,
                payload.npcEntityId(),
                payload.taskId()
            );
        });
    }
}
