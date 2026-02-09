package com.Kizunad.guzhenrenext.bastion.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.ui.BastionMenus;
import com.Kizunad.guzhenrenext.bastion.ui.SpiritManagementScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import com.Kizunad.guzhenrenext.bastion.entity.BastionEntities;
import com.Kizunad.guzhenrenext.bastion.client.renderer.LandSpiritRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 基地系统客户端菜单界面注册入口。
 * <p>
 * 该类仅在 CLIENT 端加载，负责将菜单类型绑定到对应 Screen，
 * 避免在通用逻辑中直接引用客户端类导致 Dedicated Server 类加载问题。
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BastionClient {

    private BastionClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(BastionMenus.SPIRIT_MANAGEMENT_MENU.get(), SpiritManagementScreen::new);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BastionEntities.LAND_SPIRIT.get(), LandSpiritRenderer::new);
    }
}
