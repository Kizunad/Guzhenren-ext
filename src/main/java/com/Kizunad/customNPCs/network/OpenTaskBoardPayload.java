package com.Kizunad.customNPCs.network;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.client.ui.task.NpcTaskBoardScreen;
import com.Kizunad.customNPCs.tasks.TaskType;
import com.Kizunad.customNPCs.tasks.data.TaskProgressState;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * S2C：同步任务面板数据给客户端。
 */
public record OpenTaskBoardPayload(
    int npcEntityId,
    List<TaskEntry> entries
) implements CustomPacketPayload {

    public static final Type<OpenTaskBoardPayload> TYPE =
        new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(
                CustomNPCsMod.MODID,
                "open_task_board"
            )
        );

    public static final StreamCodec<
        RegistryFriendlyByteBuf,
        OpenTaskBoardPayload
    > STREAM_CODEC = StreamCodec.of(
        OpenTaskBoardPayload::write,
        OpenTaskBoardPayload::read
    );

    public OpenTaskBoardPayload {
        entries = List.copyOf(entries);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
        OpenTaskBoardPayload payload,
        IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            mc.setScreen(new NpcTaskBoardScreen(payload));
        });
    }

    private static void write(
        RegistryFriendlyByteBuf buf,
        OpenTaskBoardPayload payload
    ) {
        buf.writeVarInt(payload.npcEntityId);
        buf.writeVarInt(payload.entries.size());
        for (TaskEntry entry : payload.entries) {
            TaskEntry.write(buf, entry);
        }
    }

    private static OpenTaskBoardPayload read(RegistryFriendlyByteBuf buf) {
        int npcId = buf.readVarInt();
        int size = buf.readVarInt();
        List<TaskEntry> entries = new java.util.ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            entries.add(TaskEntry.read(buf));
        }
        return new OpenTaskBoardPayload(npcId, entries);
    }

    public record TaskEntry(
        ResourceLocation taskId,
        Component title,
        Component description,
        TaskType type,
        TaskProgressState state,
        List<SubmitObjectiveEntry> objectives,
        List<ItemStack> rewards
    ) {

        public TaskEntry {
            objectives = List.copyOf(objectives);
            rewards = List.copyOf(rewards);
        }

        private static void write(RegistryFriendlyByteBuf buf, TaskEntry entry) {
            ResourceLocation.STREAM_CODEC.encode(buf, entry.taskId);
            ComponentSerialization.STREAM_CODEC.encode(buf, entry.title);
            ComponentSerialization.STREAM_CODEC.encode(
                buf,
                entry.description
            );
            buf.writeVarInt(entry.type.ordinal());
            buf.writeVarInt(entry.state.ordinal());
            buf.writeVarInt(entry.objectives.size());
            for (SubmitObjectiveEntry objective : entry.objectives) {
                SubmitObjectiveEntry.write(buf, objective);
            }
            buf.writeVarInt(entry.rewards.size());
            for (ItemStack stack : entry.rewards) {
                ItemStack.STREAM_CODEC.encode(buf, stack);
            }
        }

        private static TaskEntry read(RegistryFriendlyByteBuf buf) {
            ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buf);
            Component title = ComponentSerialization.STREAM_CODEC.decode(buf);
            Component description = ComponentSerialization.STREAM_CODEC.decode(
                buf
            );
            TaskType type = fromTypeOrdinal(buf.readVarInt());
            TaskProgressState state = fromStateOrdinal(buf.readVarInt());
            int objectiveSize = buf.readVarInt();
            List<SubmitObjectiveEntry> objectives = new java.util.ArrayList<>(
                objectiveSize
            );
            for (int i = 0; i < objectiveSize; i++) {
                objectives.add(SubmitObjectiveEntry.read(buf));
            }
            int rewardSize = buf.readVarInt();
            List<ItemStack> rewards = new java.util.ArrayList<>(rewardSize);
            for (int i = 0; i < rewardSize; i++) {
                rewards.add(ItemStack.STREAM_CODEC.decode(buf));
            }
            return new TaskEntry(
                id,
                title,
                description,
                type,
                state,
                objectives,
                rewards
            );
        }

        private static TaskType fromTypeOrdinal(int ordinal) {
            TaskType[] values = TaskType.values();
            if (ordinal < 0 || ordinal >= values.length) {
                return TaskType.SIDE;
            }
            return values[ordinal];
        }

        private static TaskProgressState fromStateOrdinal(int ordinal) {
            TaskProgressState[] values = TaskProgressState.values();
            if (ordinal < 0 || ordinal >= values.length) {
                return TaskProgressState.AVAILABLE;
            }
            return values[ordinal];
        }
    }

    public record SubmitObjectiveEntry(
        ItemStack item,
        int requiredCount,
        int currentCount
    ) {

        private static void write(
            RegistryFriendlyByteBuf buf,
            SubmitObjectiveEntry entry
        ) {
            ItemStack.STREAM_CODEC.encode(buf, entry.item);
            buf.writeVarInt(entry.requiredCount);
            buf.writeVarInt(entry.currentCount);
        }

        private static SubmitObjectiveEntry read(RegistryFriendlyByteBuf buf) {
            ItemStack stack = ItemStack.STREAM_CODEC.decode(buf);
            int required = buf.readVarInt();
            int current = buf.readVarInt();
            return new SubmitObjectiveEntry(stack, required, current);
        }
    }
}
