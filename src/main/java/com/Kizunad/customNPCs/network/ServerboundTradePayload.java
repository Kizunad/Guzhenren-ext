package com.Kizunad.customNPCs.network;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.ai.interaction.NpcTradeHooks;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.menu.NpcTradeMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端点击交易按钮 -> 服务端执行交易的负载。
 */
public record ServerboundTradePayload(int containerId)
    implements CustomPacketPayload {

    private static final double MAX_TRADE_DISTANCE = 20.0D;
    private static final double MAX_TRADE_DISTANCE_SQR =
        MAX_TRADE_DISTANCE * MAX_TRADE_DISTANCE;

    public static final CustomPacketPayload.Type<ServerboundTradePayload> TYPE =
        new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                CustomNPCsMod.MODID,
                "trade"
            )
        );

    public static final StreamCodec<
        RegistryFriendlyByteBuf,
        ServerboundTradePayload
    > STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        ServerboundTradePayload::containerId,
        ServerboundTradePayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        ServerboundTradePayload payload,
        IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            if (!(serverPlayer.containerMenu instanceof NpcTradeMenu menu)) {
                return;
            }
            if (menu.containerId != payload.containerId()) {
                return;
            }
            int npcEntityId = menu.getNpcEntityId();
            if (npcEntityId < 0) {
                return;
            }
            ServerLevel level = serverPlayer.serverLevel();
            if (!(level.getEntity(npcEntityId) instanceof CustomNpcEntity npc)) {
                return;
            }
            if (
                !npc.isAlive() ||
                npc.isRemoved() ||
                npc.distanceToSqr(serverPlayer) > MAX_TRADE_DISTANCE_SQR
            ) {
                return;
            }
            NpcTradeHooks.performTrade(npc, menu, serverPlayer);
        });
    }
}
