package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/**
 * 飞剑实体属性注册。
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class FlyingSwordEntityAttributes {

    private FlyingSwordEntityAttributes() {}

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(FlyingSwordEntities.FLYING_SWORD.get(), FlyingSwordEntity.createAttributes().build());
    }
}
