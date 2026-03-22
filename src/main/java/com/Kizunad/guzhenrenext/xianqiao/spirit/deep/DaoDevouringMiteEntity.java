package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkApi;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.level.Level;

public class DaoDevouringMiteEntity extends Endermite {

    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final int SPLIT_AURA_COST = 80;
    private static final int STARVATION_DAMAGE = 2;
    private static final String SPLIT_SUCCESS_NAME = "吞痕分裂";
    private static final int SPLIT_PARTICLE_COUNT = 8;
    private static final double SPLIT_OFFSET_X = 0.4D;
    private static final double SPLIT_OFFSET_Z = 0.4D;
    private static final double SPLIT_PARTICLE_XZ_SPREAD = 0.2D;
    private static final double SPLIT_PARTICLE_Y_SPREAD = 0.15D;
    private static final double SPLIT_PARTICLE_SPEED = 0.02D;
    private static final float SPLIT_SOUND_VOLUME = 0.8F;
    private static final float SPLIT_SOUND_PITCH = 1.1F;

    private int auraCheckTicker;
    private boolean splitBranchTriggeredForTest;
    private int splitSuccessCountForTest;
    private boolean splitSuccessNameTagVisibleForTest;
    private int splitParticleTriggerCountForTest;
    private int splitSoundTriggerCountForTest;

    public DaoDevouringMiteEntity(EntityType<? extends Endermite> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) {
            return;
        }
        auraCheckTicker++;
        if (auraCheckTicker < CHECK_INTERVAL_TICKS) {
            return;
        }
        auraCheckTicker = 0;
        processAuraCycle();
    }

    public void forceAuraCheckForTest() {
        auraCheckTicker = CHECK_INTERVAL_TICKS;
    }

    public boolean runAuraCycleForTest() {
        if (level().isClientSide()) {
            return false;
        }
        return processAuraCycle();
    }

    public boolean hasSplitBranchTriggeredForTest() {
        return splitBranchTriggeredForTest;
    }

    public int getSplitSuccessCountForTest() {
        return splitSuccessCountForTest;
    }

    public boolean wasSplitSuccessNameTagVisibleForTest() {
        return splitSuccessNameTagVisibleForTest;
    }

    public int getSplitParticleTriggerCountForTest() {
        return splitParticleTriggerCountForTest;
    }

    public int getSplitSoundTriggerCountForTest() {
        return splitSoundTriggerCountForTest;
    }

    private boolean processAuraCycle() {
        if (DaoMarkApi.consumeAura(level(), blockPosition(), DaoType.DARK, SPLIT_AURA_COST)) {
            splitBranchTriggeredForTest = true;
            splitOnce();
            return true;
        }
        hurt(damageSources().starve(), STARVATION_DAMAGE);
        return false;
    }

    private void splitOnce() {
        net.minecraft.world.entity.Entity child = getType().create(level());
        if (!(child instanceof DaoDevouringMiteEntity miteChild)) {
            return;
        }
        miteChild.moveTo(getX() + SPLIT_OFFSET_X, getY(), getZ() + SPLIT_OFFSET_Z, getYRot(), getXRot());
        if (!level().addFreshEntity(miteChild)) {
            return;
        }
        splitSuccessCountForTest++;
        setCustomName(Component.literal(SPLIT_SUCCESS_NAME));
        setCustomNameVisible(true);
        splitSuccessNameTagVisibleForTest = isCustomNameVisible();
        setCustomNameVisible(false);
        setCustomName(null);
        if (level() instanceof ServerLevel serverLevel) {
            emitSplitSuccessFeedback(serverLevel);
        }
    }

    private void emitSplitSuccessFeedback(ServerLevel serverLevel) {
        splitParticleTriggerCountForTest++;
        serverLevel.sendParticles(
            ParticleTypes.PORTAL,
            getX(),
            getY() + getBbHeight() / 2.0D,
            getZ(),
            SPLIT_PARTICLE_COUNT,
            SPLIT_PARTICLE_XZ_SPREAD,
            SPLIT_PARTICLE_Y_SPREAD,
            SPLIT_PARTICLE_XZ_SPREAD,
            SPLIT_PARTICLE_SPEED
        );
        splitSoundTriggerCountForTest++;
        serverLevel.playSound(
            null,
            blockPosition(),
            SoundEvents.ENDERMITE_AMBIENT,
            SoundSource.HOSTILE,
            SPLIT_SOUND_VOLUME,
            SPLIT_SOUND_PITCH
        );
    }
}
