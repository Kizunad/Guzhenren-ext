package com.Kizunad.guzhenrenext.bastion.guardian;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionArcherGuardian;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionBerserkerGuardian;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionCasterGuardian;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionEvokerGuardian;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionIllusionerGuardian;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionHealerGuardian;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionIronGolemGuardian;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionPhantomGuardian;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionPillagerGuardian;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionRavagerGuardian;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionShieldGuardian;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionVexGuardian;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionVindicatorGuardian;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionWardenGuardian;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionWitherSkeletonGuardian;
import com.Kizunad.guzhenrenext.bastion.guardian.entity.BastionWitchGuardian;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 基地守卫实体注册。
 * <p>
 * 说明：这些实体类继承对应原版实体，以复用原版模型/动画；
 * 但它们在服务端拥有自定义“强度/技能/阵营规则”。</p>
 */
public final class BastionGuardianEntities {

    private BastionGuardianEntities() {
    }

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
        Registries.ENTITY_TYPE,
        GuzhenrenExt.MODID
    );

    // ===== 智道 =====
    public static final DeferredHolder<EntityType<?>, EntityType<BastionWitchGuardian>> BASTION_WITCH =
        registerMob("bastion_witch_guardian", BastionWitchGuardian::new);

    public static final DeferredHolder<EntityType<?>, EntityType<BastionHealerGuardian>> BASTION_HEALER =
        registerMob("bastion_healer_guardian", BastionHealerGuardian::new);

    public static final DeferredHolder<EntityType<?>, EntityType<BastionEvokerGuardian>> BASTION_EVOKER =
        registerMob("bastion_evoker_guardian", BastionEvokerGuardian::new);

    public static final DeferredHolder<EntityType<?>, EntityType<BastionIllusionerGuardian>> BASTION_ILLUSIONER =
        registerMob("bastion_illusioner_guardian", BastionIllusionerGuardian::new);

    // ===== 魂道 =====
    public static final DeferredHolder<EntityType<?>, EntityType<BastionPhantomGuardian>> BASTION_PHANTOM =
        registerMob("bastion_phantom_guardian", BastionPhantomGuardian::new);

    public static final DeferredHolder<EntityType<?>, EntityType<BastionVexGuardian>> BASTION_VEX =
        registerMob("bastion_vex_guardian", BastionVexGuardian::new);

    public static final DeferredHolder<EntityType<?>, EntityType<BastionWitherSkeletonGuardian>>
        BASTION_WITHER_SKELETON = registerMob("bastion_wither_skeleton_guardian", BastionWitherSkeletonGuardian::new);

    // ===== 木道 =====
    public static final DeferredHolder<EntityType<?>, EntityType<BastionVindicatorGuardian>> BASTION_VINDICATOR =
        registerMob("bastion_vindicator_guardian", BastionVindicatorGuardian::new);

    public static final DeferredHolder<EntityType<?>, EntityType<BastionShieldGuardian>> BASTION_SHIELD_GUARDIAN =
        registerMob("bastion_shield_guardian", BastionShieldGuardian::new);

    public static final DeferredHolder<EntityType<?>, EntityType<BastionBerserkerGuardian>> BASTION_BERSERKER_GUARDIAN =
        registerMob("bastion_berserker_guardian", BastionBerserkerGuardian::new);

    public static final DeferredHolder<EntityType<?>, EntityType<BastionPillagerGuardian>> BASTION_PILLAGER =
        registerMob("bastion_pillager_guardian", BastionPillagerGuardian::new);

    public static final DeferredHolder<EntityType<?>, EntityType<BastionArcherGuardian>> BASTION_ARCHER_GUARDIAN =
        registerMob("bastion_archer_guardian", BastionArcherGuardian::new);

    public static final DeferredHolder<EntityType<?>, EntityType<BastionCasterGuardian>> BASTION_CASTER_GUARDIAN =
        registerMob("bastion_caster_guardian", BastionCasterGuardian::new);

    public static final DeferredHolder<EntityType<?>, EntityType<BastionIronGolemGuardian>> BASTION_IRON_GOLEM =
        registerMob("bastion_iron_golem_guardian", BastionIronGolemGuardian::new);

    // ===== 力道 =====
    public static final DeferredHolder<EntityType<?>, EntityType<BastionRavagerGuardian>> BASTION_RAVAGER =
        registerMob("bastion_ravager_guardian", BastionRavagerGuardian::new);

    public static final DeferredHolder<EntityType<?>, EntityType<net.minecraft.world.entity.monster.hoglin.Hoglin>>
        BASTION_HOGLIN = registerMob("bastion_hoglin_guardian", net.minecraft.world.entity.monster.hoglin.Hoglin::new);

    public static final DeferredHolder<EntityType<?>, EntityType<BastionWardenGuardian>> BASTION_WARDEN =
        registerMob("bastion_warden_guardian", BastionWardenGuardian::new);

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    private static <T extends net.minecraft.world.entity.Mob> DeferredHolder<EntityType<?>, EntityType<T>> registerMob(
        String id,
        EntityType.EntityFactory<T> factory
    ) {
        // 注意：不同外观实体尺寸差异较大，但尺寸主要影响碰撞/交互。
        // 为避免过早绑定具体尺寸，这里统一使用“人形”尺寸，后续可按需求细分。
        return ENTITY_TYPES.register(id, () -> createDefaultMonsterType(factory, id));
    }

    private static <T extends net.minecraft.world.entity.Mob> EntityType<T> createDefaultMonsterType(
        EntityType.EntityFactory<T> factory,
        String id
    ) {
        // 默认走“人形”尺寸；少数非人形（phantom/ravager/warden）后续可按需细分。
        return EntityType.Builder
            .of(factory, MobCategory.MONSTER)
            .sized(EntityDimensions.DEFAULT_BB_WIDTH, EntityDimensions.DEFAULT_BB_HEIGHT)
            .clientTrackingRange(EntityDimensions.DEFAULT_TRACK_RANGE)
            .updateInterval(EntityDimensions.DEFAULT_UPDATE_INTERVAL)
            .build(ResourceLocation.fromNamespaceAndPath(GuzhenrenExt.MODID, id).toString());
    }

    /**
     * 统一尺寸常量，避免 MagicNumber。
     */
    private static final class EntityDimensions {
        static final float DEFAULT_BB_WIDTH = 0.6f;
        static final float DEFAULT_BB_HEIGHT = 1.95f;
        static final int DEFAULT_TRACK_RANGE = 8;
        static final int DEFAULT_UPDATE_INTERVAL = 1;

        private EntityDimensions() {
        }
    }
}
