package com.Kizunad.guzhenrenext.bastion.guardian;

import com.Kizunad.guzhenrenext.GuzhenrenExt;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

/**
 * 基地守卫实体属性注册。
 * <p>
 * 注意：此处只注册“可用的属性集”，具体数值由 BastionGuardianStatsService 在运行时覆盖。</p>
 */
@EventBusSubscriber(modid = GuzhenrenExt.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class BastionGuardianEntityAttributes {

    private BastionGuardianEntityAttributes() {
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        // 注意：属性创建事件要求“每个 EntityType 对应一份独立的 AttributeSupplier”。
        // 强度（血量/攻击/护甲/速度等）由 BastionGuardianStatsService 在运行时覆盖。
        // 但 AttributeSupplier 必须至少包含该实体原版逻辑会读取的属性（例如铁傀儡会读取 ATTACK_DAMAGE）。

        event.put(
            BastionGuardianEntities.BASTION_WITCH.get(),
            net.minecraft.world.entity.monster.Witch.createAttributes().build()
        );
        event.put(
            BastionGuardianEntities.BASTION_HEALER.get(),
            net.minecraft.world.entity.monster.Witch.createAttributes().build()
        );
        event.put(
            BastionGuardianEntities.BASTION_EVOKER.get(),
            net.minecraft.world.entity.monster.Evoker.createAttributes().build()
        );
        event.put(
            BastionGuardianEntities.BASTION_ILLUSIONER.get(),
            net.minecraft.world.entity.monster.Illusioner.createAttributes().build()
        );

        event.put(
            BastionGuardianEntities.BASTION_PHANTOM.get(),
            net.minecraft.world.entity.Mob.createMobAttributes()
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE)
                .build()
        );
        event.put(
            BastionGuardianEntities.BASTION_VEX.get(),
            net.minecraft.world.entity.monster.Vex.createAttributes().build()
        );
        event.put(
            BastionGuardianEntities.BASTION_WITHER_SKELETON.get(),
            net.minecraft.world.entity.monster.WitherSkeleton.createAttributes().build()
        );

        event.put(
            BastionGuardianEntities.BASTION_VINDICATOR.get(),
            net.minecraft.world.entity.monster.Vindicator.createAttributes().build()
        );
        event.put(
            BastionGuardianEntities.BASTION_SHIELD_GUARDIAN.get(),
            net.minecraft.world.entity.monster.Vindicator.createAttributes().build()
        );
        event.put(
            BastionGuardianEntities.BASTION_BERSERKER_GUARDIAN.get(),
            net.minecraft.world.entity.monster.Vindicator.createAttributes().build()
        );
        event.put(
            BastionGuardianEntities.BASTION_PILLAGER.get(),
            net.minecraft.world.entity.monster.Pillager.createAttributes().build()
        );
        event.put(
            BastionGuardianEntities.BASTION_ARCHER_GUARDIAN.get(),
            net.minecraft.world.entity.monster.Skeleton.createAttributes().build()
        );
        event.put(
            BastionGuardianEntities.BASTION_CASTER_GUARDIAN.get(),
            net.minecraft.world.entity.monster.Blaze.createAttributes().build()
        );
        event.put(
            BastionGuardianEntities.BASTION_IRON_GOLEM.get(),
            net.minecraft.world.entity.animal.IronGolem.createAttributes().build()
        );

        event.put(
            BastionGuardianEntities.BASTION_RAVAGER.get(),
            net.minecraft.world.entity.monster.Ravager.createAttributes().build()
        );
        event.put(
            BastionGuardianEntities.BASTION_HOGLIN.get(),
            net.minecraft.world.entity.monster.hoglin.Hoglin.createAttributes().build()
        );
        event.put(
            BastionGuardianEntities.BASTION_WARDEN.get(),
            net.minecraft.world.entity.monster.warden.Warden.createAttributes().build()
        );
    }
}
