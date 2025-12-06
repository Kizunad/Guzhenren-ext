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
 * C2S：玩家请求提交任务物品。
 */
public record ServerboundSubmitTaskPayload(
    int npcEntityId,
    ResourceLocation taskId
) implements CustomPacketPayload {

    public static final Type<ServerboundSubmitTaskPayload> TYPE =
        new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                CustomNPCsMod.MODID,
                "submit_task"
            )
        );

    public static final StreamCodec<
        RegistryFriendlyByteBuf,
        ServerboundSubmitTaskPayload
    > STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        ServerboundSubmitTaskPayload::npcEntityId,
        ResourceLocation.STREAM_CODEC,
        ServerboundSubmitTaskPayload::taskId,
        ServerboundSubmitTaskPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        ServerboundSubmitTaskPayload payload,
        IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof net.minecraft.server.level.ServerPlayer player)) {
                return;
            }
            TaskActionHandler.handleSubmit(
                player,
                payload.npcEntityId(),
                payload.taskId()
            );
        });
    }
}
