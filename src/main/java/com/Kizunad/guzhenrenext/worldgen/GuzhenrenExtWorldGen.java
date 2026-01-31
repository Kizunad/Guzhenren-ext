package com.Kizunad.guzhenrenext.worldgen;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 世界生成注册表。
 */
public final class GuzhenrenExtWorldGen {

    private GuzhenrenExtWorldGen() {
    }

    private static final DeferredRegister<Feature<?>> FEATURES =
        DeferredRegister.create(Registries.FEATURE, GuzhenrenExt.MODID);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> BASTION_RUIN =
        FEATURES.register(
            "bastion_ruin",
            com.Kizunad.guzhenrenext.worldgen.feature.BastionRuinFeature::new
        );

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
