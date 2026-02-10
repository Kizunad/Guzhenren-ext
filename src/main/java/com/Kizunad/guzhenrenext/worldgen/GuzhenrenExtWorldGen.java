package com.Kizunad.guzhenrenext.worldgen;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 世界生成注册表。
 */
public final class GuzhenrenExtWorldGen {

    private GuzhenrenExtWorldGen() {
    }

    private static final DeferredRegister<Feature<?>> FEATURES =
        DeferredRegister.create(Registries.FEATURE, GuzhenrenExt.MODID);

    // Bastion 世界生成特性已移除，待重写

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
