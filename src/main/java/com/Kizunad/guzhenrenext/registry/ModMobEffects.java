package com.Kizunad.guzhenrenext.registry;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.effect.MadDogMobEffect;
import com.Kizunad.guzhenrenext.effect.TearMobEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMobEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(
        Registries.MOB_EFFECT,
        GuzhenrenExt.MODID
    );

    // 注册撕裂效果
    public static final DeferredHolder<MobEffect, TearMobEffect> TEAR = MOB_EFFECTS.register(
        "tear",
        TearMobEffect::new
    );

    // 注册疯狗状态
    public static final DeferredHolder<MobEffect, MadDogMobEffect> MAD_DOG = MOB_EFFECTS.register(
        "mad_dog",
        MadDogMobEffect::new
    );

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
