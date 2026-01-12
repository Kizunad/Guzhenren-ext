package com.Kizunad.guzhenrenext.kongqiao.flyingsword;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 飞剑实体注册表。
 */
public final class FlyingSwordEntities {

    private FlyingSwordEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
        Registries.ENTITY_TYPE,
        GuzhenrenExt.MODID
    );

    public static final DeferredHolder<EntityType<?>, EntityType<FlyingSwordEntity>> FLYING_SWORD =
        ENTITY_TYPES.register(
            "flying_sword",
            () ->
                EntityType.Builder
                    .<FlyingSwordEntity>of(FlyingSwordEntity::new, MobCategory.MISC)
                    .sized(
                        FlyingSwordConstants.ENTITY_WIDTH,
                        FlyingSwordConstants.ENTITY_HEIGHT
                    )
                    .clientTrackingRange(FlyingSwordConstants.CLIENT_TRACK_RANGE)
                    .updateInterval(1)
                    .build(
                        ResourceLocation.fromNamespaceAndPath(
                            GuzhenrenExt.MODID,
                            "flying_sword"
                        ).toString()
                    )
        );

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
