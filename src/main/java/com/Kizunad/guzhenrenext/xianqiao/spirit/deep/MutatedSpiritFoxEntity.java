package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class MutatedSpiritFoxEntity extends Fox {

    private static final int LIGHTNING_BOLT_COUNT = 2;
    private static final int THUNDER_FORM_PARTICLE_COUNT = 12;
    private static final String THUNDER_FORM_NAME = "雷霆灵狐";
    private static final String HOSTILE_MUTATION_NAME = "失控灵狐";
    private static final double THUNDER_FORM_PARTICLE_XZ_SPREAD = 0.3D;
    private static final double THUNDER_FORM_PARTICLE_Y_SPREAD = 0.2D;
    private static final double THUNDER_FORM_PARTICLE_SPEED = 0.02D;
    private static final float THUNDER_FORM_SOUND_VOLUME = 0.8F;
    private static final float THUNDER_FORM_SOUND_PITCH = 1.1F;

    private boolean thunderForm;
    private boolean hostileMutationState;
    private int thunderFormTransformCountForTest;
    private boolean thunderFormNameTagVisibleForTest;
    private int thunderFormParticleTriggerCountForTest;
    private int thunderFormSoundTriggerCountForTest;
    private int visualLightningSpawnCountForTest;
    private long lastThunderFormTransformGameTimeForTest = -1L;
    private int hostileMutationTriggerCountForTest;
    private boolean hostileMutationNameTagVisibleForTest;
    private boolean hostileTargetAssignedForTest;
    private long lastHostileMutationGameTimeForTest = -1L;

    public MutatedSpiritFoxEntity(EntityType<? extends Fox> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!thunderForm && !hostileMutationState && isThunderCatalyst(itemStack)) {
            if (!level().isClientSide()) {
                transformToThunderForm();
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (!thunderForm && !hostileMutationState && isFailedMutationCatalyst(itemStack)) {
            if (!level().isClientSide()) {
                enterHostileMutationState(player);
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public boolean isThunderForm() {
        return thunderForm;
    }

    public boolean isHostileMutationStateForTest() {
        return hostileMutationState;
    }

    public int getThunderFormTransformCountForTest() {
        return thunderFormTransformCountForTest;
    }

    public boolean wasThunderFormNameTagVisibleForTest() {
        return thunderFormNameTagVisibleForTest;
    }

    public int getThunderFormParticleTriggerCountForTest() {
        return thunderFormParticleTriggerCountForTest;
    }

    public int getThunderFormSoundTriggerCountForTest() {
        return thunderFormSoundTriggerCountForTest;
    }

    public int getVisualLightningSpawnCountForTest() {
        return visualLightningSpawnCountForTest;
    }

    public long getLastThunderFormTransformGameTimeForTest() {
        return lastThunderFormTransformGameTimeForTest;
    }

    public int getHostileMutationTriggerCountForTest() {
        return hostileMutationTriggerCountForTest;
    }

    public boolean wasHostileMutationNameTagVisibleForTest() {
        return hostileMutationNameTagVisibleForTest;
    }

    public boolean wasHostileTargetAssignedForTest() {
        return hostileTargetAssignedForTest;
    }

    public long getLastHostileMutationGameTimeForTest() {
        return lastHostileMutationGameTimeForTest;
    }

    private boolean isThunderCatalyst(ItemStack itemStack) {
        return itemStack.is(Blocks.FERN.asItem());
    }

    private boolean isFailedMutationCatalyst(ItemStack itemStack) {
        return itemStack.is(Items.LIGHTNING_ROD);
    }

    private void transformToThunderForm() {
        thunderForm = true;
        thunderFormTransformCountForTest++;
        setCustomName(Component.literal(THUNDER_FORM_NAME));
        setCustomNameVisible(true);
        thunderFormNameTagVisibleForTest = isCustomNameVisible();
        setGlowingTag(true);
        if (level() instanceof ServerLevel serverLevel) {
            lastThunderFormTransformGameTimeForTest = serverLevel.getGameTime();
            emitThunderFormFeedback(serverLevel);
            spawnThunderFormLightningVisuals(serverLevel);
        }
    }

    private void enterHostileMutationState(Player player) {
        hostileMutationState = true;
        hostileMutationTriggerCountForTest++;
        setTarget(player);
        setLastHurtByMob(player);
        hostileTargetAssignedForTest = getTarget() == player;
        setCustomName(Component.literal(HOSTILE_MUTATION_NAME));
        setCustomNameVisible(true);
        hostileMutationNameTagVisibleForTest = isCustomNameVisible();
        if (level() instanceof ServerLevel serverLevel) {
            lastHostileMutationGameTimeForTest = serverLevel.getGameTime();
        }
    }

    private void emitThunderFormFeedback(ServerLevel serverLevel) {
        thunderFormParticleTriggerCountForTest++;
        serverLevel.sendParticles(
            ParticleTypes.ELECTRIC_SPARK,
            getX(),
            getY() + getBbHeight() / 2.0D,
            getZ(),
            THUNDER_FORM_PARTICLE_COUNT,
            THUNDER_FORM_PARTICLE_XZ_SPREAD,
            THUNDER_FORM_PARTICLE_Y_SPREAD,
            THUNDER_FORM_PARTICLE_XZ_SPREAD,
            THUNDER_FORM_PARTICLE_SPEED
        );
        thunderFormSoundTriggerCountForTest++;
        serverLevel.playSound(
            null,
            blockPosition(),
            SoundEvents.LIGHTNING_BOLT_IMPACT,
            SoundSource.WEATHER,
            THUNDER_FORM_SOUND_VOLUME,
            THUNDER_FORM_SOUND_PITCH
        );
    }

    private void spawnThunderFormLightningVisuals(ServerLevel serverLevel) {
        for (int i = 0; i < LIGHTNING_BOLT_COUNT; i++) {
            LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (lightningBolt != null) {
                lightningBolt.moveTo(getX(), getY(), getZ());
                lightningBolt.setVisualOnly(true);
                serverLevel.addFreshEntity(lightningBolt);
                visualLightningSpawnCountForTest++;
            }
        }
    }
}
