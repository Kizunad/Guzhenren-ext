package com.Kizunad.guzhenrenext.bastion.entity;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 基地实体注册类。
 * <p>
 * 负责注册基地相关的所有实体类型。
 * </p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class BastionEntities {

    private BastionEntities() {
    }

    private static final float LAND_SPIRIT_WIDTH = 0.6f;
    private static final float LAND_SPIRIT_HEIGHT = 0.7f;

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
        Registries.ENTITY_TYPE,
        GuzhenrenExt.MODID
    );

    public static final DeferredHolder<EntityType<?>, EntityType<LandSpiritEntity>> LAND_SPIRIT =
        ENTITY_TYPES.register("land_spirit", () ->
            EntityType.Builder.of(LandSpiritEntity::new, MobCategory.MISC)
                .sized(LAND_SPIRIT_WIDTH, LAND_SPIRIT_HEIGHT) // 类似狐狸的大小
                .build(ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, "land_spirit").toString())
        );

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(LAND_SPIRIT.get(), LandSpiritEntity.createAttributes().build());
    }
}
