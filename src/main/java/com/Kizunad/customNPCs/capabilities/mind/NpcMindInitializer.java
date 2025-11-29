package com.Kizunad.customNPCs.capabilities.mind;

import com.Kizunad.customNPCs.ai.NpcMindRegistry;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * 初始化 NpcMind 的目标
 */
@EventBusSubscriber(modid = "customnpcs")
public class NpcMindInitializer {
    
    /**
     * 当实体加入世界时，为其 NpcMind 注册目标
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        
        // 获取 NpcMind 并注册目标
        if (entity.hasData(NpcMindAttachment.NPC_MIND)) {
            var mind = entity.getData(NpcMindAttachment.NPC_MIND);
            
            // 基于注册表进行一次性初始化，避免重复注册
            boolean initialized = NpcMindRegistry.initializeMind(mind);

            if (initialized) {
                MindLog.decision(
                    MindLogLevel.INFO,
                    "为实体 {} 初始化思维系统（含默认传感器/目标）",
                    entity.getName().getString()
                );
            }
        }
    }
}
