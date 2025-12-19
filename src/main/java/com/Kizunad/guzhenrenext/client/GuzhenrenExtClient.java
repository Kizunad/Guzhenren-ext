package com.Kizunad.guzhenrenext.client;

import com.Kizunad.guzhenrenext.client.gui.GuzhenrenConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Guzhenren Extension 的客户端初始化入口。
 *
 * <p>注意：此类必须保持只在客户端被加载，避免 Dedicated Server 侧触发
 * {@code net.minecraft.client.*} 类加载导致崩溃。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class GuzhenrenExtClient {

    private GuzhenrenExtClient() {}

    public static void registerConfigScreen(ModContainer modContainer) {
        modContainer.registerExtensionPoint(
            IConfigScreenFactory.class,
            (minecraft, screen) -> new GuzhenrenConfigScreen(screen)
        );
    }
}
