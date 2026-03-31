package com.Kizunad.guzhenrenext.entity;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 实体注册表。
 */
public final class ModEntities {

    private static final float ENTITY_WIDTH = 0.6F;

    private static final float ENTITY_HEIGHT = 1.8F;

    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
        Registries.ENTITY_TYPE,
        GuzhenrenExt.MODID
    );

    public static final DeferredHolder<EntityType<?>, EntityType<RogueEntity>> ROGUE = ENTITY_TYPES.register(
        "rogue",
        () ->
            EntityType.Builder
                .<RogueEntity>of(RogueEntity::new, MobCategory.CREATURE)
                .sized(ENTITY_WIDTH, ENTITY_HEIGHT)
                .build(ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, "rogue").toString())
    );

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
