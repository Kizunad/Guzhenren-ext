package com.Kizunad.customNPCs.entity;

import com.Kizunad.customNPCs.CustomNPCsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 实体注册表：自定义 NPC。
 */
public final class ModEntities {

    private static final float NPC_WIDTH = 0.6f;
    private static final float NPC_HEIGHT = 1.95f;

    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(Registries.ENTITY_TYPE, CustomNPCsMod.MODID);

    public static final DeferredHolder<
        EntityType<?>,
        EntityType<CustomNpcEntity>
    > CUSTOM_NPC = ENTITY_TYPES.register("custom_npc", () ->
        EntityType.Builder
            .<CustomNpcEntity>of(
                (type, level) -> new CustomNpcEntity(type, level),
                CustomNpcEntity.getCategory()
            )
            .sized(NPC_WIDTH, NPC_HEIGHT)
            .build(
                ResourceLocation.fromNamespaceAndPath(
                    CustomNPCsMod.MODID,
                    "custom_npc"
                ).toString()
            )
    );

    /**
     * 将实体注册表绑定到 MOD EventBus。
     */
    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    /**
     * 提供默认属性，供 attribute 事件注册。
     */
    public static AttributeSupplier getDefaultAttributes() {
        return CustomNpcEntity.createAttributes().build();
    }
}
