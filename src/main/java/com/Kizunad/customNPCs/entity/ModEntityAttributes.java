package com.Kizunad.customNPCs.entity;

import com.Kizunad.customNPCs.CustomNPCsMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/**
 * 实体属性注册事件。
 */
@EventBusSubscriber(
    modid = CustomNPCsMod.MODID,
    bus = EventBusSubscriber.Bus.MOD
)
public final class ModEntityAttributes {

    private ModEntityAttributes() {}

    @SubscribeEvent
    public static void onEntityAttributeCreation(
        EntityAttributeCreationEvent event
    ) {
        event.put(
            ModEntities.CUSTOM_NPC.get(),
            CustomNpcEntity.createAttributes().build()
        );
    }
}
