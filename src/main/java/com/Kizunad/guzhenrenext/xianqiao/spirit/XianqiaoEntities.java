package com.Kizunad.guzhenrenext.xianqiao.spirit;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.ApertureGuardianEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.CalamityBeastEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.DaoDevouringMiteEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.MimicSlimeEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.MutatedSpiritFoxEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.SacrificialSheepEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.StoneVeinSentinelEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.SymbioticSpiritBeeEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.TreasureMinkEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.VoidWalkerEntity;
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

    public static final String TREASURE_MINK_ID = "treasure_mink";

    public static final String MUTATED_SPIRIT_FOX_ID = "mutated_spirit_fox";

    public static final String APERTURE_GUARDIAN_ID = "aperture_guardian";

    public static final String SACRIFICIAL_SHEEP_ID = "sacrificial_sheep";

    public static final String DAO_DEVOURING_MITE_ID = "dao_devouring_mite";

    public static final String STONE_VEIN_SENTINEL_ID = "stone_vein_sentinel";

    public static final String MIMIC_SLIME_ID = "mimic_slime";

    public static final String VOID_WALKER_ID = "void_walker";

    public static final String CALAMITY_BEAST_ID = "calamity_beast";

    public static final String SYMBIOTIC_SPIRIT_BEE_ID = "symbiotic_spirit_bee";

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

    private static final int DEEP_CREATURE_TRACKING_RANGE = 10;

    private static final float FOX_WIDTH = 0.6F;
    private static final float FOX_HEIGHT = 0.7F;
    private static final float GOLEM_WIDTH = 1.4F;
    private static final float GOLEM_HEIGHT = 2.7F;
    private static final float ENDERMITE_WIDTH = 0.4F;
    private static final float ENDERMITE_HEIGHT = 0.3F;
    private static final float SNOW_GOLEM_WIDTH = 0.7F;
    private static final float SNOW_GOLEM_HEIGHT = 1.9F;
    private static final float SLIME_WIDTH = 0.8F;
    private static final float SLIME_HEIGHT = 0.8F;
    private static final float ENDERMAN_WIDTH = 0.6F;
    private static final float ENDERMAN_HEIGHT = 2.9F;
    private static final float RAVAGER_WIDTH = 1.95F;
    private static final float RAVAGER_HEIGHT = 2.2F;
    private static final float BEE_WIDTH = 0.7F;
    private static final float BEE_HEIGHT = 0.6F;

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

    public static final DeferredHolder<EntityType<?>, EntityType<TreasureMinkEntity>> TREASURE_MINK =
        ENTITY_TYPES.register(
            TREASURE_MINK_ID,
            () ->
                EntityType.Builder
                    .<TreasureMinkEntity>of(TreasureMinkEntity::new, MobCategory.CREATURE)
                    .sized(FOX_WIDTH, FOX_HEIGHT)
                    .clientTrackingRange(DEEP_CREATURE_TRACKING_RANGE)
                    .build(
                        ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, TREASURE_MINK_ID).toString()
                    )
        );

    public static final DeferredHolder<EntityType<?>, EntityType<MutatedSpiritFoxEntity>> MUTATED_SPIRIT_FOX =
        ENTITY_TYPES.register(
            MUTATED_SPIRIT_FOX_ID,
            () ->
                EntityType.Builder
                    .<MutatedSpiritFoxEntity>of(MutatedSpiritFoxEntity::new, MobCategory.CREATURE)
                    .sized(FOX_WIDTH, FOX_HEIGHT)
                    .clientTrackingRange(DEEP_CREATURE_TRACKING_RANGE)
                    .build(
                        ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, MUTATED_SPIRIT_FOX_ID).toString()
                    )
        );

    public static final DeferredHolder<EntityType<?>, EntityType<ApertureGuardianEntity>> APERTURE_GUARDIAN =
        ENTITY_TYPES.register(
            APERTURE_GUARDIAN_ID,
            () ->
                EntityType.Builder
                    .<ApertureGuardianEntity>of(ApertureGuardianEntity::new, MobCategory.MONSTER)
                    .sized(GOLEM_WIDTH, GOLEM_HEIGHT)
                    .clientTrackingRange(DEEP_CREATURE_TRACKING_RANGE)
                    .build(
                        ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, APERTURE_GUARDIAN_ID).toString()
                    )
        );

    public static final DeferredHolder<EntityType<?>, EntityType<SacrificialSheepEntity>> SACRIFICIAL_SHEEP =
        ENTITY_TYPES.register(
            SACRIFICIAL_SHEEP_ID,
            () ->
                EntityType.Builder
                    .<SacrificialSheepEntity>of(SacrificialSheepEntity::new, MobCategory.CREATURE)
                    .sized(XIAN_SHEEP_WIDTH, XIAN_SHEEP_HEIGHT)
                    .clientTrackingRange(DEEP_CREATURE_TRACKING_RANGE)
                    .build(
                        ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, SACRIFICIAL_SHEEP_ID).toString()
                    )
        );

    public static final DeferredHolder<EntityType<?>, EntityType<DaoDevouringMiteEntity>> DAO_DEVOURING_MITE =
        ENTITY_TYPES.register(
            DAO_DEVOURING_MITE_ID,
            () ->
                EntityType.Builder
                    .<DaoDevouringMiteEntity>of(DaoDevouringMiteEntity::new, MobCategory.MONSTER)
                    .sized(ENDERMITE_WIDTH, ENDERMITE_HEIGHT)
                    .clientTrackingRange(DEEP_CREATURE_TRACKING_RANGE)
                    .build(
                        ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, DAO_DEVOURING_MITE_ID).toString()
                    )
        );

    public static final DeferredHolder<EntityType<?>, EntityType<StoneVeinSentinelEntity>> STONE_VEIN_SENTINEL =
        ENTITY_TYPES.register(
            STONE_VEIN_SENTINEL_ID,
            () ->
                EntityType.Builder
                    .<StoneVeinSentinelEntity>of(StoneVeinSentinelEntity::new, MobCategory.CREATURE)
                    .sized(SNOW_GOLEM_WIDTH, SNOW_GOLEM_HEIGHT)
                    .clientTrackingRange(DEEP_CREATURE_TRACKING_RANGE)
                    .build(
                        ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, STONE_VEIN_SENTINEL_ID).toString()
                    )
        );

    public static final DeferredHolder<EntityType<?>, EntityType<MimicSlimeEntity>> MIMIC_SLIME =
        ENTITY_TYPES.register(
            MIMIC_SLIME_ID,
            () ->
                EntityType.Builder
                    .<MimicSlimeEntity>of(MimicSlimeEntity::new, MobCategory.MONSTER)
                    .sized(SLIME_WIDTH, SLIME_HEIGHT)
                    .clientTrackingRange(DEEP_CREATURE_TRACKING_RANGE)
                    .build(
                        ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, MIMIC_SLIME_ID).toString()
                    )
        );

    public static final DeferredHolder<EntityType<?>, EntityType<VoidWalkerEntity>> VOID_WALKER =
        ENTITY_TYPES.register(
            VOID_WALKER_ID,
            () ->
                EntityType.Builder
                    .<VoidWalkerEntity>of(VoidWalkerEntity::new, MobCategory.MONSTER)
                    .sized(ENDERMAN_WIDTH, ENDERMAN_HEIGHT)
                    .clientTrackingRange(DEEP_CREATURE_TRACKING_RANGE)
                    .build(
                        ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, VOID_WALKER_ID).toString()
                    )
        );

    public static final DeferredHolder<EntityType<?>, EntityType<CalamityBeastEntity>> CALAMITY_BEAST =
        ENTITY_TYPES.register(
            CALAMITY_BEAST_ID,
            () ->
                EntityType.Builder
                    .<CalamityBeastEntity>of(CalamityBeastEntity::new, MobCategory.MONSTER)
                    .sized(RAVAGER_WIDTH, RAVAGER_HEIGHT)
                    .clientTrackingRange(DEEP_CREATURE_TRACKING_RANGE)
                    .build(
                        ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, CALAMITY_BEAST_ID).toString()
                    )
        );

    public static final DeferredHolder<EntityType<?>, EntityType<SymbioticSpiritBeeEntity>> SYMBIOTIC_SPIRIT_BEE =
        ENTITY_TYPES.register(
            SYMBIOTIC_SPIRIT_BEE_ID,
            () ->
                EntityType.Builder
                    .<SymbioticSpiritBeeEntity>of(SymbioticSpiritBeeEntity::new, MobCategory.CREATURE)
                    .sized(BEE_WIDTH, BEE_HEIGHT)
                    .clientTrackingRange(DEEP_CREATURE_TRACKING_RANGE)
                    .build(
                        ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, SYMBIOTIC_SPIRIT_BEE_ID).toString()
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
            event.put(TREASURE_MINK.get(), net.minecraft.world.entity.animal.Fox.createAttributes().build());
            event.put(
                MUTATED_SPIRIT_FOX.get(),
                net.minecraft.world.entity.animal.Fox.createAttributes().build()
            );
            event.put(
                APERTURE_GUARDIAN.get(),
                net.minecraft.world.entity.animal.IronGolem.createAttributes().build()
            );
            event.put(SACRIFICIAL_SHEEP.get(), Sheep.createAttributes().build());
            event.put(
                DAO_DEVOURING_MITE.get(),
                net.minecraft.world.entity.monster.Endermite.createAttributes().build()
            );
            event.put(
                STONE_VEIN_SENTINEL.get(),
                net.minecraft.world.entity.animal.SnowGolem.createAttributes().build()
            );
            event.put(MIMIC_SLIME.get(), net.minecraft.world.entity.Mob.createMobAttributes().build());
            event.put(VOID_WALKER.get(), net.minecraft.world.entity.monster.EnderMan.createAttributes().build());
            event.put(CALAMITY_BEAST.get(), net.minecraft.world.entity.monster.Ravager.createAttributes().build());
            event.put(
                SYMBIOTIC_SPIRIT_BEE.get(),
                net.minecraft.world.entity.animal.Bee.createAttributes().build()
            );
        }
    }
}
