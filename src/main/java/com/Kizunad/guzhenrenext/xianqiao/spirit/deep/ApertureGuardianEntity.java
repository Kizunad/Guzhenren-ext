package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
    private static final String PHASE_TWO_NAME = "仙窍镇灵·二阶段";
    private static final double PHASE_TWO_KNOCKBACK_BONUS = 1.5D;
    private static final double HALF_HEALTH_RATIO = 0.5D;
    private static final int PHASE_TWO_PARTICLE_COUNT = 16;
    private static final double PHASE_TWO_PARTICLE_XZ_SPREAD = 0.4D;
    private static final double PHASE_TWO_PARTICLE_Y_SPREAD = 0.6D;
    private static final double PHASE_TWO_PARTICLE_SPEED = 0.02D;
    private static final float PHASE_TWO_SOUND_VOLUME = 1.0F;
    private static final float PHASE_TWO_SOUND_PITCH = 0.9F;
    private static final int PHASE_TWO_LOCAL_PRESSURE_COST = 64;

    private boolean phaseTwoActive;
    private boolean phaseTwoFeedbackTriggeredForTest;
    private boolean phaseTwoNameTagVisibleForTest;
    private int phaseTwoActivationPulseCountForTest;
    private int phaseTwoParticleTriggerCountForTest;
    private int phaseTwoSoundTriggerCountForTest;
    private long lastPhaseTwoActivationGameTimeForTest = -1L;
    private int lastLocalPressureCostSnapshotForTest;

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
            activatePhaseTwo();
        } else if (!shouldActivatePhaseTwo && phaseTwoActive) {
            deactivatePhaseTwo();
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
        if (source.getEntity() instanceof net.minecraft.server.level.ServerPlayer) {
            spawnAtLocation(XianqiaoItems.ZHEN_QIAO_XUAN_TIE_HE.get());
        }
    }

    public boolean isPhaseTwoActive() {
        return phaseTwoActive;
    }

    public boolean hasPhaseTwoFeedbackTriggeredForTest() {
        return phaseTwoFeedbackTriggeredForTest;
    }

    public boolean wasPhaseTwoNameTagVisibleForTest() {
        return phaseTwoNameTagVisibleForTest;
    }

    public int getPhaseTwoActivationPulseCountForTest() {
        return phaseTwoActivationPulseCountForTest;
    }

    public int getPhaseTwoParticleTriggerCountForTest() {
        return phaseTwoParticleTriggerCountForTest;
    }

    public int getPhaseTwoSoundTriggerCountForTest() {
        return phaseTwoSoundTriggerCountForTest;
    }

    public long getLastPhaseTwoActivationGameTimeForTest() {
        return lastPhaseTwoActivationGameTimeForTest;
    }

    public int getLastLocalPressureCostSnapshotForTest() {
        return lastLocalPressureCostSnapshotForTest;
    }

    public boolean hasLocalPressureCostSnapshotForTest() {
        return lastLocalPressureCostSnapshotForTest > 0;
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

    private void activatePhaseTwo() {
        phaseTwoActive = true;
        applyKnockbackBoost(true);
        setCustomName(Component.literal(PHASE_TWO_NAME));
        setCustomNameVisible(true);
        phaseTwoNameTagVisibleForTest = isCustomNameVisible();
        if (level() instanceof ServerLevel serverLevel) {
            emitPhaseTwoActivationFeedback(serverLevel);
        }
    }

    private void deactivatePhaseTwo() {
        phaseTwoActive = false;
        applyKnockbackBoost(false);
        setCustomName(null);
    }

    private void emitPhaseTwoActivationFeedback(ServerLevel serverLevel) {
        phaseTwoFeedbackTriggeredForTest = true;
        phaseTwoActivationPulseCountForTest++;
        phaseTwoParticleTriggerCountForTest++;
        phaseTwoSoundTriggerCountForTest++;
        lastPhaseTwoActivationGameTimeForTest = serverLevel.getGameTime();
        lastLocalPressureCostSnapshotForTest = PHASE_TWO_LOCAL_PRESSURE_COST;
        serverLevel.sendParticles(
            ParticleTypes.ENCHANT,
            getX(),
            getY() + HALF_HEALTH_RATIO,
            getZ(),
            PHASE_TWO_PARTICLE_COUNT,
            PHASE_TWO_PARTICLE_XZ_SPREAD,
            PHASE_TWO_PARTICLE_Y_SPREAD,
            PHASE_TWO_PARTICLE_XZ_SPREAD,
            PHASE_TWO_PARTICLE_SPEED
        );
        serverLevel.playSound(
            null,
            blockPosition(),
            SoundEvents.BEACON_ACTIVATE,
            SoundSource.HOSTILE,
            PHASE_TWO_SOUND_VOLUME,
            PHASE_TWO_SOUND_PITCH
        );
    }
}
