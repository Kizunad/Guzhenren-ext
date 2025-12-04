package com.Kizunad.customNPCs.network;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.client.ui.interact.NpcInteractScreen;
import com.Kizunad.customNPCs.network.dto.DialogueOption;
import com.Kizunad.customNPCs.network.dto.NpcStatusEntry;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * S2C：打开 NPC 交互界面时一次性下发所需数据。
 */
public record OpenInteractGuiPayload(
    int npcEntityId,
    Component displayName,
    float health,
    float maxHealth,
    boolean isOwner,
    List<NpcStatusEntry> statusEntries,
    List<DialogueOption> dialogueOptions
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenInteractGuiPayload> TYPE =
        new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                CustomNPCsMod.MODID,
                "open_interact_gui"
            )
        );

    public static final StreamCodec<
        RegistryFriendlyByteBuf,
        OpenInteractGuiPayload
    > STREAM_CODEC = StreamCodec.of(
        OpenInteractGuiPayload::write,
        OpenInteractGuiPayload::read
    );

    public OpenInteractGuiPayload {
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(statusEntries, "statusEntries");
        Objects.requireNonNull(dialogueOptions, "dialogueOptions");
        statusEntries = List.copyOf(statusEntries);
        dialogueOptions = List.copyOf(dialogueOptions);
    }

    private static void write(
        RegistryFriendlyByteBuf buf,
        OpenInteractGuiPayload payload
    ) {
        buf.writeVarInt(payload.npcEntityId);
        ComponentSerialization.STREAM_CODEC.encode(
            buf,
            payload.displayName
        );
        buf.writeFloat(payload.health);
        buf.writeFloat(payload.maxHealth);
        buf.writeBoolean(payload.isOwner);
        buf.writeVarInt(payload.statusEntries.size());
        for (NpcStatusEntry entry : payload.statusEntries) {
            NpcStatusEntry.STREAM_CODEC.encode(buf, entry);
        }
        buf.writeVarInt(payload.dialogueOptions.size());
        for (DialogueOption option : payload.dialogueOptions) {
            DialogueOption.STREAM_CODEC.encode(buf, option);
        }
    }

    private static OpenInteractGuiPayload read(
        RegistryFriendlyByteBuf buf
    ) {
        int entityId = buf.readVarInt();
        Component name = ComponentSerialization.STREAM_CODEC.decode(buf);
        float health = buf.readFloat();
        float maxHealth = buf.readFloat();
        boolean isOwner = buf.readBoolean();
        int statusCount = buf.readVarInt();
        List<NpcStatusEntry> statuses = java.util.stream.IntStream
            .range(0, statusCount)
            .mapToObj(i -> NpcStatusEntry.STREAM_CODEC.decode(buf))
            .toList();
        int optionCount = buf.readVarInt();
        List<DialogueOption> options = java.util.stream.IntStream
            .range(0, optionCount)
            .mapToObj(i -> DialogueOption.STREAM_CODEC.decode(buf))
            .toList();
        return new OpenInteractGuiPayload(
            entityId,
            name,
            health,
            maxHealth,
            isOwner,
            statuses,
            options
        );
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        OpenInteractGuiPayload payload,
        IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            mc.setScreen(
                new NpcInteractScreen(new NpcInteractScreen.InteractData(
                    payload.npcEntityId(),
                    payload.displayName(),
                    payload.health(),
                    payload.maxHealth(),
                    payload.isOwner(),
                    payload.statusEntries(),
                    payload.dialogueOptions()
                ))
            );
        });
    }
}
