package com.Kizunad.guzhenrenext;

import com.Kizunad.guzhenrenext.customNPCImpl.ai.Registery;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(GuzhenrenExt.MODID)
public class GuzhenrenExt {

    public static final String MODID = "guzhenrenext";

    public GuzhenrenExt(IEventBus modEventBus, ModContainer modContainer) {
        Registery.registerAll();
    }
}
