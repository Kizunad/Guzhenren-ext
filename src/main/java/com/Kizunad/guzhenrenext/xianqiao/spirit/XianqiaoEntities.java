package com.Kizunad.guzhenrenext.xianqiao.spirit;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Sheep;
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

    /** 集群 NPC 实体注册名。 */
    public static final String CLUSTER_NPC_ID = "cluster_npc";

    /** 仙牛实体注册名。 */
    public static final String XIAN_COW_ID = "xian_cow";

    /** 仙鸡实体注册名。 */
    public static final String XIAN_CHICKEN_ID = "xian_chicken";

    /** 仙羊实体注册名。 */
    public static final String XIAN_SHEEP_ID = "xian_sheep";

    /** 地灵实体宽度。 */
    private static final float LAND_SPIRIT_WIDTH = 0.6F;

    /** 地灵实体高度。 */
    private static final float LAND_SPIRIT_HEIGHT = 1.8F;

    /** 地灵客户端跟踪距离。 */
    private static final int LAND_SPIRIT_TRACKING_RANGE = 10;

    /** 集群 NPC 实体宽度。 */
    private static final float CLUSTER_NPC_WIDTH = 0.6F;

    /** 集群 NPC 实体高度。 */
    private static final float CLUSTER_NPC_HEIGHT = 1.8F;

    /** 集群 NPC 客户端跟踪距离。 */
    private static final int CLUSTER_NPC_TRACKING_RANGE = 10;

    /** 仙牛客户端跟踪距离。 */
    private static final int XIAN_COW_TRACKING_RANGE = 10;

    /** 仙牛宽度（与原版牛一致）。 */
    private static final float XIAN_COW_WIDTH = 0.9F;

    /** 仙牛高度（与原版牛一致）。 */
    private static final float XIAN_COW_HEIGHT = 1.4F;

    /** 仙鸡客户端跟踪距离。 */
    private static final int XIAN_CHICKEN_TRACKING_RANGE = 10;

    /** 仙鸡宽度（与原版鸡一致）。 */
    private static final float XIAN_CHICKEN_WIDTH = 0.4F;

    /** 仙鸡高度（与原版鸡一致）。 */
    private static final float XIAN_CHICKEN_HEIGHT = 0.7F;

    /** 仙羊客户端跟踪距离。 */
    private static final int XIAN_SHEEP_TRACKING_RANGE = 10;

    /** 仙羊宽度（与原版羊一致）。 */
    private static final float XIAN_SHEEP_WIDTH = 0.9F;

    /** 仙羊高度（与原版羊一致）。 */
    private static final float XIAN_SHEEP_HEIGHT = 1.3F;

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

    /** 集群 NPC 实体类型。 */
    public static final DeferredHolder<EntityType<?>, EntityType<ClusterNpcEntity>> CLUSTER_NPC =
        ENTITY_TYPES.register(
            CLUSTER_NPC_ID,
            () ->
                EntityType.Builder
                    .<ClusterNpcEntity>of(ClusterNpcEntity::new, MobCategory.MISC)
                    .sized(CLUSTER_NPC_WIDTH, CLUSTER_NPC_HEIGHT)
                    .clientTrackingRange(CLUSTER_NPC_TRACKING_RANGE)
                    .build(
                        ResourceLocation.fromNamespaceAndPath(
                            GuzhenrenExt.MODID,
                            CLUSTER_NPC_ID
                        ).toString()
                    )
        );

    /** 仙牛实体类型。 */
    public static final DeferredHolder<EntityType<?>, EntityType<XianCowEntity>> XIAN_COW =
        ENTITY_TYPES.register(
            XIAN_COW_ID,
            () ->
                EntityType.Builder
                    .<XianCowEntity>of(XianCowEntity::new, MobCategory.CREATURE)
                    .sized(XIAN_COW_WIDTH, XIAN_COW_HEIGHT)
                    .clientTrackingRange(XIAN_COW_TRACKING_RANGE)
                    .build(
                        ResourceLocation.fromNamespaceAndPath(
                            GuzhenrenExt.MODID,
                            XIAN_COW_ID
                        ).toString()
                    )
        );

    /** 仙鸡实体类型。 */
    public static final DeferredHolder<EntityType<?>, EntityType<XianChickenEntity>> XIAN_CHICKEN =
        ENTITY_TYPES.register(
            XIAN_CHICKEN_ID,
            () ->
                EntityType.Builder
                    .<XianChickenEntity>of(XianChickenEntity::new, MobCategory.CREATURE)
                    .sized(XIAN_CHICKEN_WIDTH, XIAN_CHICKEN_HEIGHT)
                    .clientTrackingRange(XIAN_CHICKEN_TRACKING_RANGE)
                    .build(
                        ResourceLocation.fromNamespaceAndPath(
                            GuzhenrenExt.MODID,
                            XIAN_CHICKEN_ID
                        ).toString()
                    )
        );

    /** 仙羊实体类型。 */
    public static final DeferredHolder<EntityType<?>, EntityType<XianSheepEntity>> XIAN_SHEEP =
        ENTITY_TYPES.register(
            XIAN_SHEEP_ID,
            () ->
                EntityType.Builder
                    .<XianSheepEntity>of(XianSheepEntity::new, MobCategory.CREATURE)
                    .sized(XIAN_SHEEP_WIDTH, XIAN_SHEEP_HEIGHT)
                    .clientTrackingRange(XIAN_SHEEP_TRACKING_RANGE)
                    .build(
                        ResourceLocation.fromNamespaceAndPath(
                            GuzhenrenExt.MODID,
                            XIAN_SHEEP_ID
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
            event.put(CLUSTER_NPC.get(), ClusterNpcEntity.createAttributes().build());
            event.put(XIAN_COW.get(), Cow.createAttributes().build());
            event.put(XIAN_CHICKEN.get(), Chicken.createAttributes().build());
            event.put(XIAN_SHEEP.get(), Sheep.createAttributes().build());
        }
    }
}
