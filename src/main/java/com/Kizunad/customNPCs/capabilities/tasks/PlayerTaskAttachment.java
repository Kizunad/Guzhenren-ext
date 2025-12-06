package com.Kizunad.customNPCs.capabilities.tasks;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.tasks.data.PlayerTaskData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 玩家任务数据 Attachment。
 */
public final class PlayerTaskAttachment {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES,
            CustomNPCsMod.MODID
        );

    public static final DeferredHolder<
        AttachmentType<?>,
        AttachmentType<PlayerTaskData>
    > PLAYER_TASKS = ATTACHMENT_TYPES.register(
        "player_tasks",
        () ->
            AttachmentType.<CompoundTag, PlayerTaskData>serializable(
                PlayerTaskData::new
            ).build()
    );

    private PlayerTaskAttachment() {}

    @EventBusSubscriber(modid = CustomNPCsMod.MODID)
    public static class Handler {

        @SubscribeEvent
        public static void onJoinLevel(EntityJoinLevelEvent event) {
            if (event.getLevel().isClientSide()) {
                return;
            }
            Entity entity = event.getEntity();
            if (!(entity instanceof ServerPlayer player)) {
                return;
            }
            if (!player.hasData(PLAYER_TASKS)) {
                player.setData(PLAYER_TASKS, new PlayerTaskData());
            }
        }
    }

    /**
     * 便捷方法：获取玩家任务数据，如不存在则自动创建。
     */
    public static PlayerTaskData get(ServerPlayer player) {
        PlayerTaskData data = player.getData(PLAYER_TASKS);
        if (data == null) {
            data = new PlayerTaskData();
            player.setData(PLAYER_TASKS, data);
        }
        return data;
    }
}
