package com.Kizunad.tinyUI;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

/**
 * tinyUI 独立辅助 mod 入口。
 */
@Mod(TinyUIMod.MOD_ID)
public final class TinyUIMod {

    public static final String MOD_ID = "tinyui";

    public TinyUIMod(final IEventBus modEventBus) {
        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.addListener(
                    com.Kizunad.tinyUI.neoforge.TinyUIClientCommands::onRegisterClientCommands);
        }
    }
}
