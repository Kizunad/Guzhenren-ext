package com.Kizunad.customNPCs.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;

/**
 * CustomNPCs 的客户端初始化入口。
 *
 * <p>将 client-only 的事件类型与屏幕注册从 @Mod 主类中剥离，避免 Dedicated Server
 * 在类扫描/字节码清理阶段触发 {@code net.minecraft.client.*} 相关类加载导致崩溃。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class CustomNPCsClientBootstrap {

    private CustomNPCsClientBootstrap() {}

    public static void registerClient(IEventBus modEventBus) {
        modEventBus.addListener(ClientScreens::registerScreens);
    }
}
