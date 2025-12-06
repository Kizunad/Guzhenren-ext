package com.Kizunad.customNPCs.network;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.decision.NpcCommandType;
import com.Kizunad.customNPCs.ai.interaction.MaterialWorkService;
import com.Kizunad.customNPCs.ai.interaction.NpcTradeHooks;
import com.Kizunad.customNPCs.ai.status.StatusProviderRegistry;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import com.Kizunad.customNPCs.menu.NpcCraftMenu;
import com.Kizunad.customNPCs.menu.NpcGiftMenu;
import com.Kizunad.customNPCs.menu.NpcMaterialMenu;
import com.Kizunad.customNPCs.menu.NpcWorkMenu;
import com.Kizunad.customNPCs.network.dto.DialogueOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.Kizunad.customNPCs.tasks.TaskBoardSyncService;

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
    private static final ResourceLocation ACTION_HIRE =
        ResourceLocation.fromNamespaceAndPath(CustomNPCsMod.MODID, "hire");
    private static final ResourceLocation ACTION_OPPRESS =
        ResourceLocation.fromNamespaceAndPath(CustomNPCsMod.MODID, "oppress");
    private static final ResourceLocation ACTION_ORDERS =
        ResourceLocation.fromNamespaceAndPath(CustomNPCsMod.MODID, "orders");
    private static final ResourceLocation ACTION_DISMISS =
        ResourceLocation.fromNamespaceAndPath(CustomNPCsMod.MODID, "dismiss");
    private static final ResourceLocation ACTION_OWNER_OPTS =
        ResourceLocation.fromNamespaceAndPath(CustomNPCsMod.MODID, "owner_opts");
    private static final ResourceLocation ACTION_TASKS =
        ResourceLocation.fromNamespaceAndPath(CustomNPCsMod.MODID, "tasks");
    private static final ResourceLocation ORDER_FOLLOW =
        ResourceLocation.fromNamespaceAndPath(CustomNPCsMod.MODID, "order_follow");
    private static final ResourceLocation ORDER_SIT =
        ResourceLocation.fromNamespaceAndPath(CustomNPCsMod.MODID, "order_sit");
    private static final ResourceLocation ORDER_WORK =
        ResourceLocation.fromNamespaceAndPath(CustomNPCsMod.MODID, "order_work");
    private static final ResourceLocation ORDER_WORK_MATERIAL =
        ResourceLocation.fromNamespaceAndPath(
            CustomNPCsMod.MODID,
            "order_work_material"
        );
    private static final ResourceLocation ORDER_WORK_CRAFT =
        ResourceLocation.fromNamespaceAndPath(
            CustomNPCsMod.MODID,
            "order_work_craft"
        );
    private static final ResourceLocation ORDER_GUARD =
        ResourceLocation.fromNamespaceAndPath(CustomNPCsMod.MODID, "order_guard");
    private static final double OPPRESSION_THRESHOLD = 0.8D;
    private static final double POWER_HEALTH_WEIGHT = 0.5D;
    private static final double POWER_ATTACK_WEIGHT = 10.0D;
    private static final double POWER_ARMOR_WEIGHT = 2.0D;
    private static final double POWER_TOUGHNESS_WEIGHT = 1.0D;
    private static final int OPPRESSION_MEMORY_DURATION = 120;
    private static final double OPPRESSION_MELEE_RANGE = 3.5D;

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

        ResourceLocation actionId = payload.actionId();

        if (ACTION_HIRE.equals(actionId)) {
            handleHire(npc, serverPlayer);
            return;
        }
        if (ACTION_OPPRESS.equals(actionId)) {
            handleOppress(npc, serverPlayer);
            return;
        }
        if (ACTION_ORDERS.equals(actionId) || ACTION_OWNER_OPTS.equals(actionId)) {
            openCommandMenu(npc, serverPlayer);
            return;
        }
        if (ACTION_DISMISS.equals(actionId)) {
            handleDismiss(npc, serverPlayer);
            return;
        }
        if (ACTION_TASKS.equals(actionId)) {
            openTaskBoard(npc, serverPlayer);
            return;
        }
        if (ORDER_FOLLOW.equals(actionId)) {
            setCommand(npc, serverPlayer, NpcCommandType.FOLLOW);
            return;
        }
        if (ORDER_SIT.equals(actionId)) {
            setCommand(npc, serverPlayer, NpcCommandType.SIT);
            return;
        }
        if (ORDER_WORK.equals(actionId)) {
            setCommand(npc, serverPlayer, NpcCommandType.WORK);
            openWorkMenu(npc, serverPlayer);
            return;
        }
        if (ORDER_WORK_MATERIAL.equals(actionId)) {
            openMaterialMenu(npc, serverPlayer);
            return;
        }
        if (ORDER_WORK_CRAFT.equals(actionId)) {
            openCraftMenu(npc, serverPlayer);
            return;
        }
        if (ORDER_GUARD.equals(actionId)) {
            setCommand(npc, serverPlayer, NpcCommandType.GUARD);
            return;
        }
    }

    private static void handleHire(CustomNpcEntity npc, ServerPlayer player) {
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return;
        }
        // 待实现：扣除金币/雇佣成本
        mind
            .getMemory()
            .rememberLongTerm(WorldStateKeys.OWNER_UUID, player.getUUID());
        mind
            .getMemory()
            .rememberLongTerm(WorldStateKeys.RELATIONSHIP_TYPE, "HIRED");
        player.displayClientMessage(
            Component.literal(
                "You hired " + npc.getDisplayName().getString() + "!"
            ),
            false
        );
    }

    private static void handleDismiss(CustomNpcEntity npc, ServerPlayer player) {
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return;
        }
        mind.getMemory().forget(WorldStateKeys.OWNER_UUID);
        mind.getMemory().forget(WorldStateKeys.RELATIONSHIP_TYPE);
        mind.getMemory().forget(WorldStateKeys.CURRENT_COMMAND);
        player.displayClientMessage(
            Component.literal(
                "You dismissed " + npc.getDisplayName().getString() + "."
            ),
            false
        );
    }

    private static void handleOppress(CustomNpcEntity npc, ServerPlayer player) {
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return;
        }
        // 待实现：更精确的力量检定算法，当前按简单判定执行
        double npcPower = calculatePower(npc);
        double playerPower = calculatePower(player);
        if (playerPower < npcPower * OPPRESSION_THRESHOLD) {
            player.displayClientMessage(
                Component.literal("Oppression failed; the NPC fights back!"),
                false
            );
            npc.setTarget(player);
            rememberThreat(mind, npc, player);
            return;
        }
        mind
            .getMemory()
            .rememberLongTerm(WorldStateKeys.OWNER_UUID, player.getUUID());
        mind
            .getMemory()
            .rememberLongTerm(WorldStateKeys.RELATIONSHIP_TYPE, "OPPRESSED");
        player.displayClientMessage(
            Component.literal(
                "You oppressed " + npc.getDisplayName().getString() + "!"
            ),
            false
        );
    }

    private static void openCommandMenu(CustomNpcEntity npc, ServerPlayer player) {
        // 发送新的 OpenInteractGuiPayload，包含指令选项
        List<DialogueOption> options = new ArrayList<>();
        options.add(new DialogueOption(
            Component.literal("Follow Me"),
            ORDER_FOLLOW,
            ""
        ));
        options.add(new DialogueOption(
            Component.literal("Stay Here (Sit)"),
            ORDER_SIT,
            ""
        ));
        options.add(new DialogueOption(
            Component.literal("Work"),
            ORDER_WORK,
            ""
        ));
        options.add(new DialogueOption(
            Component.literal("Guard"),
            ORDER_GUARD,
            ""
        ));
        options.add(new DialogueOption(
            Component.literal("Dismiss"),
            ACTION_DISMISS,
            ""
        ));

        var statuses = StatusProviderRegistry.collect(npc);
        
        PacketDistributor.sendToPlayer(
            player,
            new OpenInteractGuiPayload(
                npc.getId(),
                npc.getDisplayName(),
                npc.getHealth(),
                npc.getMaxHealth(),
                true, // isOwner
                true, // startInDialogueMode
                statuses,
                options
            )
        );
    }

    private static void openTaskBoard(
        CustomNpcEntity npc,
        ServerPlayer player
    ) {
        PacketDistributor.sendToPlayer(
            player,
            TaskBoardSyncService.buildPayload(npc, player)
        );
    }

    private static void openMaterialMenu(CustomNpcEntity npc, ServerPlayer player) {
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return;
        }
        MaterialWorkService.syncOwnerMaterial(npc, mind.getMaterialWallet(), player);
        player.openMenu(
            new SimpleMenuProvider(
                (id, inv, p) -> new NpcMaterialMenu(id, inv, npc),
                npc.getDisplayName()
            ),
            buf -> buf.writeVarInt(npc.getId())
        );
    }

    private static void openWorkMenu(CustomNpcEntity npc, ServerPlayer player) {
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return;
        }
        MaterialWorkService.syncOwnerMaterial(npc, mind.getMaterialWallet(), player);
        player.openMenu(
            new SimpleMenuProvider(
                (id, inv, p) -> new NpcWorkMenu(id, inv, npc),
                npc.getDisplayName()
            ),
            buf -> buf.writeVarInt(npc.getId())
        );
    }

    private static void openCraftMenu(CustomNpcEntity npc, ServerPlayer player) {
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return;
        }
        MaterialWorkService.syncOwnerMaterial(npc, mind.getMaterialWallet(), player);
        player.openMenu(
            new SimpleMenuProvider(
                (id, inv, p) -> new NpcCraftMenu(id, inv, npc),
                npc.getDisplayName()
            ),
            buf -> buf.writeVarInt(npc.getId())
        );
    }

    private static void setCommand(CustomNpcEntity npc, ServerPlayer player, NpcCommandType command) {
        var mind = npc.getData(NpcMindAttachment.NPC_MIND);
        if (mind == null) {
            return;
        }
        mind.getMemory().rememberLongTerm(WorldStateKeys.CURRENT_COMMAND, command.name());
        player.displayClientMessage(
            Component.literal("Command set to: " + command.name()),
            true
        );
        // 重新打开交互界面，刷新状态
        openCommandMenu(npc, player);
    }

    private static double calculatePower(net.minecraft.world.entity.LivingEntity entity) {
        double health = entity.getMaxHealth();
        double attack = entity
            .getAttribute(Attributes.ATTACK_DAMAGE) != null
            ? entity.getAttribute(Attributes.ATTACK_DAMAGE).getValue()
            : 1.0D;
        double armor = entity
            .getAttribute(Attributes.ARMOR) != null
            ? entity.getAttribute(Attributes.ARMOR).getValue()
            : 0.0D;
        double toughness = entity
            .getAttribute(Attributes.ARMOR_TOUGHNESS) != null
            ? entity.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue()
            : 0.0D;
        return health * POWER_HEALTH_WEIGHT +
            attack * POWER_ATTACK_WEIGHT +
            armor * POWER_ARMOR_WEIGHT +
            toughness * POWER_TOUGHNESS_WEIGHT;
    }

    private static void rememberThreat(
        com.Kizunad.customNPCs.capabilities.mind.INpcMind mind,
        CustomNpcEntity npc,
        ServerPlayer player
    ) {
        double distance = npc.distanceTo(player);
        var memory = mind.getMemory();
        memory.rememberShortTerm(
            WorldStateKeys.DISTANCE_TO_TARGET,
            distance,
            OPPRESSION_MEMORY_DURATION
        );
        memory.rememberShortTerm(
            WorldStateKeys.TARGET_IN_RANGE,
            distance <= OPPRESSION_MELEE_RANGE,
            OPPRESSION_MEMORY_DURATION
        );
        memory.rememberShortTerm(
            WorldStateKeys.TARGET_VISIBLE,
            true,
            OPPRESSION_MEMORY_DURATION
        );
        memory.rememberShortTerm(
            WorldStateKeys.HOSTILE_NEARBY,
            true,
            OPPRESSION_MEMORY_DURATION
        );
        memory.rememberShortTerm(
            WorldStateKeys.IN_DANGER,
            true,
            OPPRESSION_MEMORY_DURATION
        );
        memory.rememberShortTerm(
            WorldStateKeys.THREAT_DETECTED,
            true,
            OPPRESSION_MEMORY_DURATION
        );
        memory.rememberShortTerm(
            WorldStateKeys.CURRENT_THREAT_ID,
            player.getUUID(),
            OPPRESSION_MEMORY_DURATION
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
