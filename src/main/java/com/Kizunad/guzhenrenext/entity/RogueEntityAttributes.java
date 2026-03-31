package com.Kizunad.guzhenrenext.entity;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/**
 * 散修实体属性注册。
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class RogueEntityAttributes {

    private RogueEntityAttributes() {}

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ROGUE.get(), RogueEntity.createAttributes().build());
    }
}
