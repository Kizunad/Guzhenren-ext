package com.Kizunad.customNPCs.capabilities.mind;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.ai.NpcMindRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * NpcMind Data Attachment
 * <p>
 * NeoForge 21.1+ 使用 Data Attachments 替代了旧的 Capability 系统
 */
public class NpcMindAttachment {
    
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = 
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CustomNPCsMod.MODID);
    
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<NpcMind>> NPC_MIND = 
        ATTACHMENT_TYPES.register(
            "npc_mind",
            () -> AttachmentType.<net.minecraft.nbt.CompoundTag, NpcMind>serializable(() -> new NpcMind()).build()
        );
    
    /**
     * 为实体附加 NpcMind
     */
    @EventBusSubscriber(modid = CustomNPCsMod.MODID)
    public static class AttachmentHandler {
        @SubscribeEvent
        public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
            if (event.getLevel().isClientSide()) {
                return;
            }
            
            Entity entity = event.getEntity();
            if (!(entity instanceof PathfinderMob mob)) {
                return;
            }

            // 仅对被标记的实体附加 NpcMind。标记规则：
            // 1) 实体含有数据标签 "customnpcs:mind_allowed"（游戏内 /data 或 spawn 时赋予）
            // 2) 实体含有任意以 "npc_mind:" 开头的 tag（便于快速调试/指令）
            // 3) 实体类实现了自定义接口 INpcMindCarrier（预留扩展，当前无实现）
            boolean hasAllowDataTag =
                mob
                    .getTags()
                    .stream()
                    .anyMatch(tag -> tag.equals("customnpcs:mind_allowed"));
            boolean hasDebugTag =
                mob.getTags().stream().anyMatch(tag -> tag.startsWith("npc_mind:"));
            boolean isCarrier = mob instanceof INpcMindCarrier;

            if (!hasAllowDataTag && !hasDebugTag && !isCarrier) {
                return;
            }
            
            // 检查是否已有 NpcMind
            if (!entity.hasData(NPC_MIND)) {
                entity.setData(NPC_MIND, new NpcMind());
            }

            // 初始化默认 Sensors/Goals（避免未注册导致逻辑缺失）
            var mind = entity.getData(NPC_MIND);
            NpcMindRegistry.initializeMind(mind);
        }
    }
}

/**
 * 标记接口：实现此接口的实体会默认附加 NpcMind。
 */
interface INpcMindCarrier {}
