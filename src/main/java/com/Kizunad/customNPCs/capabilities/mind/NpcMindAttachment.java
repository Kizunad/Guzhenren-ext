package com.Kizunad.customNPCs.capabilities.mind;

import com.Kizunad.customNPCs.CustomNPCsMod;
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
            if (!(entity instanceof PathfinderMob)) {
                return;
            }
            
            // 检查是否已有 NpcMind
            if (!entity.hasData(NPC_MIND)) {
                // 附加 NpcMind
                entity.setData(NPC_MIND, new NpcMind());
            }
        }
    }
}
