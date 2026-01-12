package com.Kizunad.guzhenrenext.kongqiao.flyingsword.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 飞剑渲染注册。
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FlyingSwordRenderers {

    private FlyingSwordRenderers() {}

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FlyingSwordEntities.FLYING_SWORD.get(), FlyingSwordEntityRenderer::new);
    }
}
