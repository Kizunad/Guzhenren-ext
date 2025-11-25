package com.Kizunad.customNPCs.events;

import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * NpcMind 事件处理器
 */
public class NpcMindEvents {

    /**
     * 在实体 tick 时，调用 NpcMind.tick()
     */
    @SubscribeEvent
    public void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        // 只处理服务端
        if (entity.level().isClientSide()) {
            return;
        }

        // 获取 NpcMind Data Attachment
        if (entity.hasData(NpcMindAttachment.NPC_MIND)) {
            var mind = entity.getData(NpcMindAttachment.NPC_MIND);
            mind.tick((ServerLevel) entity.level(), entity);
        }
    }
}
