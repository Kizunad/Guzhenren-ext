package com.Kizunad.customNPCs.entity.render;

import com.Kizunad.customNPCs.CustomNPCsMod;
import com.Kizunad.customNPCs.entity.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 客户端实体渲染注册。
 */
@EventBusSubscriber(modid = CustomNPCsMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModEntityRenderers {

    private ModEntityRenderers() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
            ModEntities.CUSTOM_NPC.get(),
            CustomNpcRenderer::new
        );
    }
}
