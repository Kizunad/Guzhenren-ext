package com.Kizunad.customNPCs.network;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.ai.interaction.NpcTradeHooks;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.Kizunad.customNPCs.menu.NpcGiftMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * C2S：客户端点击交互按钮/对话选项后上报服务端。
 */
public record InteractActionPayload(
    int npcEntityId,
    ActionType actionType,
    ResourceLocation actionId,
    CompoundTag extraData
) implements CustomPacketPayload {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        InteractActionPayload.class
    );
    private static final double MAX_INTERACT_DISTANCE = 20.0D;
    private static final double MAX_INTERACT_DISTANCE_SQR =
        MAX_INTERACT_DISTANCE * MAX_INTERACT_DISTANCE;

    public static final CustomPacketPayload.Type<InteractActionPayload> TYPE =
        new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                CustomNPCsMod.MODID,
                "interact_action"
            )
        );

    public static final StreamCodec<
        RegistryFriendlyByteBuf,
        InteractActionPayload
    > STREAM_CODEC = StreamCodec.of(
        InteractActionPayload::write,
        InteractActionPayload::read
    );

    public InteractActionPayload {
        Objects.requireNonNull(actionType, "actionType");
        Objects.requireNonNull(actionId, "actionId");
        extraData =
            extraData == null ? new CompoundTag() : extraData.copy();
    }

    private static void write(
        RegistryFriendlyByteBuf buf,
        InteractActionPayload payload
    ) {
        buf.writeVarInt(payload.npcEntityId);
        buf.writeVarInt(payload.actionType.ordinal());
        ResourceLocation.STREAM_CODEC.encode(buf, payload.actionId);
        buf.writeNbt(payload.extraData);
    }

    private static InteractActionPayload read(RegistryFriendlyByteBuf buf) {
        int entityId = buf.readVarInt();
        ActionType actionType = ActionType.fromId(buf.readVarInt());
        ResourceLocation actionId = ResourceLocation.STREAM_CODEC.decode(buf);
        CompoundTag data = buf.readNbt();
        return new InteractActionPayload(entityId, actionType, actionId, data);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        InteractActionPayload payload,
        IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            var level = serverPlayer.serverLevel();
            var entity = level.getEntity(payload.npcEntityId());
            if (!(entity instanceof CustomNpcEntity npc)) {
                return;
            }
            if (
                !npc.isAlive() ||
                npc.isRemoved() ||
                npc.distanceToSqr(serverPlayer) > MAX_INTERACT_DISTANCE_SQR
            ) {
                return;
            }
            switch (payload.actionType()) {
                case TRADE -> handleTrade(npc, serverPlayer);
                case GIFT -> handleGift(npc, serverPlayer);
                case CHAT, CUSTOM -> handleChat(npc, serverPlayer, payload);
                default -> LOGGER.debug(
                    "未知交互动作: {}", payload.actionType()
                );
            }
        });
    }

    private static void handleTrade(
        CustomNpcEntity npc,
        ServerPlayer serverPlayer
    ) {
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return;
        }
        var tradeState = mind.getTradeState();
        if (tradeState.getPriceMultiplier() <= 0.0F) {
            tradeState.generateNewMultiplier();
        }
        InteractionResult result = NpcTradeHooks.tryOpenTrade(
            npc,
            serverPlayer,
            tradeState
        );
        LOGGER.debug("处理交易交互: result={}", result);
    }

    private static void handleGift(
        CustomNpcEntity npc,
        ServerPlayer serverPlayer
    ) {
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return;
        }
        serverPlayer.openMenu(
            new SimpleMenuProvider(
                (id, inv, player) ->
                    new NpcGiftMenu(
                        id,
                        inv,
                        npc,
                        mind.getInventory()
                    ),
                npc.getDisplayName()
            ),
            buf -> buf.writeVarInt(npc.getId())
        );
        LOGGER.debug("处理赠礼/背包交互: npc={}", npc.getStringUUID());
    }

    private static void handleChat(
        CustomNpcEntity npc,
        ServerPlayer serverPlayer,
        InteractActionPayload payload
    ) {
        LOGGER.debug(
            "处理对话交互: npc={}, actionId={}, extra={}",
            npc.getStringUUID(),
            payload.actionId(),
            payload.extraData()
        );
        serverPlayer.displayClientMessage(
            Component.literal(
                "Received action: " + payload.actionId().toString()
            ),
            true
        );
    }

    public enum ActionType {
        TRADE,
        GIFT,
        CHAT,
        CUSTOM;

        public static ActionType fromId(int id) {
            ActionType[] values = values();
            if (id < 0 || id >= values.length) {
                return CUSTOM;
            }
            return values[id];
        }
    }
}
