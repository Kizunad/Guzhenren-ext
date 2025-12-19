package com.Kizunad.tinyUI;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * tinyUI 独立辅助 mod 入口。
 */
@Mod(TinyUIMod.MOD_ID)
public final class TinyUIMod {

    public static final String MOD_ID = "tinyui";

    public TinyUIMod(final IEventBus modEventBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            runClientBootstrap();
        }
    }

    private static void runClientBootstrap() {
        try {
            Class<?> bootstrap = Class.forName(
                "com.Kizunad.tinyUI.neoforge.TinyUIClientBootstrap"
            );
            bootstrap.getMethod("registerClient").invoke(null);
        } catch (ClassNotFoundException e) {
            // Dedicated Server 环境或裁剪环境下不存在客户端类，跳过即可。
        } catch (Exception e) {
            throw new RuntimeException(
                "tinyUI 客户端初始化失败，请检查客户端依赖与加载顺序。",
                e
            );
        }
    }
}
