package com.Kizunad.guzhenrenext.xianqiao.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.resource.XianqiaoMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * 仙窍系统客户端事件。
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class XianqiaoClientEvents {

    private XianqiaoClientEvents() {
    }

    /**
     * 注册仙窍菜单对应的客户端 Screen。
     */
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(XianqiaoMenus.RESOURCE_CONTROLLER.get(), ResourceControllerScreen::new);
    }
}
