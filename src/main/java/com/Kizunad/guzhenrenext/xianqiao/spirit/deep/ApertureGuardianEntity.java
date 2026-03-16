package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.Level;

public class ApertureGuardianEntity extends IronGolem {

    private static final net.minecraft.resources.ResourceLocation PHASE_TWO_MODIFIER_ID =
        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
            "guzhenrenext",
            "aperture_guardian_phase_two_knockback"
        );
    private static final double PHASE_TWO_KNOCKBACK_BONUS = 1.5D;
    private static final double HALF_HEALTH_RATIO = 0.5D;

    private boolean phaseTwoActive;

    public ApertureGuardianEntity(EntityType<? extends IronGolem> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) {
            return;
        }
        boolean shouldActivatePhaseTwo = getHealth() <= getMaxHealth() * HALF_HEALTH_RATIO;
        if (shouldActivatePhaseTwo && !phaseTwoActive) {
            phaseTwoActive = true;
            applyKnockbackBoost(true);
            setCustomName(net.minecraft.network.chat.Component.literal("仙窍镇灵·二阶段"));
            setCustomNameVisible(true);
        } else if (!shouldActivatePhaseTwo && phaseTwoActive) {
            phaseTwoActive = false;
            applyKnockbackBoost(false);
            setCustomName(null);
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && phaseTwoActive && target instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
            double xBoost = livingEntity.getX() - getX();
            double zBoost = livingEntity.getZ() - getZ();
            livingEntity.knockback(1.0D, xBoost, zBoost);
        }
        return hit;
    }

    @Override
    protected void dropCustomDeathLoot(
        net.minecraft.server.level.ServerLevel level,
        net.minecraft.world.damagesource.DamageSource source,
        boolean causedByPlayer
    ) {
        super.dropCustomDeathLoot(level, source, causedByPlayer);
        spawnAtLocation(net.minecraft.world.item.Items.NETHER_STAR);
    }

    public boolean isPhaseTwoActive() {
        return phaseTwoActive;
    }

    private void applyKnockbackBoost(boolean enable) {
        AttributeInstance attackKnockback = getAttribute(Attributes.ATTACK_KNOCKBACK);
        if (attackKnockback == null) {
            return;
        }
        attackKnockback.removeModifier(PHASE_TWO_MODIFIER_ID);
        if (enable) {
            attackKnockback.addTransientModifier(
                new AttributeModifier(
                    PHASE_TWO_MODIFIER_ID,
                    PHASE_TWO_KNOCKBACK_BONUS,
                    AttributeModifier.Operation.ADD_VALUE
                )
            );
        }
    }
}
