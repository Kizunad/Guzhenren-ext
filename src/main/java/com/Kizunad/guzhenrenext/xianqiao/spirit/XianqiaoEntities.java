package com.Kizunad.guzhenrenext.xianqiao.spirit;

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
 * 仙窍实体注册表。
 */
public final class XianqiaoEntities {

    /** 地灵实体注册名。 */
    public static final String LAND_SPIRIT_ID = "land_spirit";

    /** 地灵实体宽度。 */
    private static final float LAND_SPIRIT_WIDTH = 0.6F;

    /** 地灵实体高度。 */
    private static final float LAND_SPIRIT_HEIGHT = 1.8F;

    /** 地灵客户端跟踪距离。 */
    private static final int LAND_SPIRIT_TRACKING_RANGE = 10;

    private XianqiaoEntities() {
    }

    /** 仙窍实体类型注册表。 */
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
        Registries.ENTITY_TYPE,
        GuzhenrenExt.MODID
    );

    /** 地灵实体类型。 */
    public static final DeferredHolder<EntityType<?>, EntityType<LandSpiritEntity>> LAND_SPIRIT =
        ENTITY_TYPES.register(
            LAND_SPIRIT_ID,
            () ->
                EntityType.Builder
                    .<LandSpiritEntity>of(LandSpiritEntity::new, MobCategory.MISC)
                    .sized(LAND_SPIRIT_WIDTH, LAND_SPIRIT_HEIGHT)
                    .clientTrackingRange(LAND_SPIRIT_TRACKING_RANGE)
                    .build(
                        ResourceLocation.fromNamespaceAndPath(
                            GuzhenrenExt.MODID,
                            LAND_SPIRIT_ID
                        ).toString()
                    )
        );

    /**
    * 注册实体类型。
    *
    * @param modEventBus 模组事件总线
    */
    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    /**
     * 地灵属性事件注册。
     */
    @EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static final class Attributes {

        private Attributes() {
        }

        /**
         * 为地灵注册属性集合。
         *
         * @param event 属性创建事件
         */
        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(LAND_SPIRIT.get(), LandSpiritEntity.createAttributes().build());
        }
    }
}
