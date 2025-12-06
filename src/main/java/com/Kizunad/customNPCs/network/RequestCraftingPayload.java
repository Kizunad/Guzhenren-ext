package com.Kizunad.customNPCs.network;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.ai.interaction.MaterialWorkService;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * C2S：请求消耗材料点制作物品。
 */
public record RequestCraftingPayload(
    int npcEntityId,
    ResourceLocation itemId,
    int amount
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestCraftingPayload> TYPE =
        new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                CustomNPCsMod.MODID,
                "request_crafting"
            )
        );

    public static final StreamCodec<
        RegistryFriendlyByteBuf,
        RequestCraftingPayload
    > STREAM_CODEC = StreamCodec.of(
        RequestCraftingPayload::write,
        RequestCraftingPayload::read
    );

    private static void write(
        RegistryFriendlyByteBuf buf,
        RequestCraftingPayload payload
    ) {
        buf.writeVarInt(payload.npcEntityId);
        ResourceLocation.STREAM_CODEC.encode(buf, payload.itemId);
        buf.writeVarInt(payload.amount);
    }

    private static RequestCraftingPayload read(RegistryFriendlyByteBuf buf) {
        int npcId = buf.readVarInt();
        ResourceLocation itemId = ResourceLocation.STREAM_CODEC.decode(buf);
        int amount = buf.readVarInt();
        return new RequestCraftingPayload(npcId, itemId, amount);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        RequestCraftingPayload payload,
        IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            var level = serverPlayer.serverLevel();
            var entity = level.getEntity(payload.npcEntityId());
            if (entity instanceof CustomNpcEntity npc) {
                MaterialWorkService.handleCraftRequest(
                    npc,
                    serverPlayer,
                    payload.itemId(),
                    payload.amount()
                );
            }
        });
    }
}
