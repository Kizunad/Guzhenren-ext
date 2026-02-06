package com.Kizunad.guzhenrenext.bastion.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.menu.BastionMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * 基地系统客户端事件订阅。
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BastionClientEvents {

    private BastionClientEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(BastionMenus.BASTION_MANAGEMENT.get(), BastionManagementScreen::new);
    }
}
