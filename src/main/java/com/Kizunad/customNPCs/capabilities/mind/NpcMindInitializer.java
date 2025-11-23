package com.Kizunad.customNPCs.capabilities.mind;

import com.Kizunad.customNPCs.ai.decision.goals.IdleGoal;
import com.Kizunad.customNPCs.ai.decision.goals.SurvivalGoal;
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
            
            // 只在第一次加入时注册（避免重复注册）
            if (mind.getGoalSelector().getCurrentGoal() == null) {
                // 注册传感器
                mind.getSensorManager().registerSensor(new com.Kizunad.customNPCs.ai.sensors.VisionSensor());
                
                // 注册基础目标
                mind.getGoalSelector().registerGoal(new SurvivalGoal());
                mind.getGoalSelector().registerGoal(
                    new com.Kizunad.customNPCs.ai.decision.goals.WatchClosestEntityGoal()
                );
                mind.getGoalSelector().registerGoal(new IdleGoal());
                
                System.out.println("[NpcMind] 为实体 " + entity.getName().getString() + " 初始化思维系统（含视觉传感器）");
            }
        }
    }
}
