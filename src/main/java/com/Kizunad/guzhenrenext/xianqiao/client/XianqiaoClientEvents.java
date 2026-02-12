package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.resource.XianqiaoMenus;
import com.Kizunad.guzhenrenext.xianqiao.spirit.XianqiaoEntities;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * 仙窍系统客户端事件。
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class XianqiaoClientEvents {

    /** 仙窍维度 type 对应的天空特效注册键。 */
    private static final ResourceLocation APERTURE_EFFECTS_ID =
        ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, "aperture_world");

    private XianqiaoClientEvents() {
    }

    /**
     * 注册仙窍菜单对应的客户端 Screen。
     */
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(XianqiaoMenus.RESOURCE_CONTROLLER.get(), ResourceControllerScreen::new);
        event.register(XianqiaoMenus.LAND_SPIRIT.get(), LandSpiritScreen::new);
        event.register(XianqiaoMenus.APERTURE_HUB.get(), ApertureHubScreen::new);
    }

    /**
     * 注册仙窍维度天空特效。
     */
    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(APERTURE_EFFECTS_ID, ApertureSkyRenderer.INSTANCE);
    }

    /**
     * 注册地灵实体渲染器。
     */
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(XianqiaoEntities.LAND_SPIRIT.get(), LandSpiritRenderer::new);
    }
}
