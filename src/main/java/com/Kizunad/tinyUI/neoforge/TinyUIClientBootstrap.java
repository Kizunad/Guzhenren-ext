package com.Kizunad.tinyUI.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;

/**
 * tinyUI 的客户端初始化入口。
 *
 * <p>避免在 @Mod 主类中直接出现 client-only 的方法引用与事件类型，确保 Dedicated Server
 * 可以安全加载该 jar。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class TinyUIClientBootstrap {

    private TinyUIClientBootstrap() {}

    public static void registerClient() {
        NeoForge.EVENT_BUS.addListener(TinyUIClientCommands::onRegisterClientCommands);
    }
}
