package com.Kizunad.guzhenrenext.kongqiao.client;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.kongqiao.client.ui.AttackInventoryScreen;
import com.Kizunad.guzhenrenext.kongqiao.client.ui.GuchongFeedScreen;
import com.Kizunad.guzhenrenext.kongqiao.client.ui.KongqiaoScreen;
import com.Kizunad.guzhenrenext.kongqiao.menu.KongqiaoMenus;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataLoader;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataLoader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * 客户端事件：注册菜单页与按键。
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class KongqiaoClientEvents {

    private KongqiaoClientEvents() {}

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(KongqiaoMenus.KONGQIAO.get(), KongqiaoScreen::new);
        event.register(
            KongqiaoMenus.ATTACK_INVENTORY.get(),
            AttackInventoryScreen::new
        );
        event.register(
            KongqiaoMenus.GUCHONG_FEED.get(),
            GuchongFeedScreen::new
        );
        event.register(
            KongqiaoMenus.NIANTOU.get(),
            com.Kizunad.guzhenrenext.kongqiao.client.ui.NianTouScreen::new
        );
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KongqiaoKeyMappings.KONGQIAO_KEY);
        event.register(KongqiaoKeyMappings.ATTACK_SWAP_KEY);
        event.register(KongqiaoKeyMappings.ATTACK_SCREEN_KEY);
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        // 客户端也需要加载念头/杀招数据，避免多人服务器下 UI 无法鉴定。
        event.registerReloadListener(new NianTouDataLoader());
        event.registerReloadListener(new ShazhaoDataLoader());
    }
}
