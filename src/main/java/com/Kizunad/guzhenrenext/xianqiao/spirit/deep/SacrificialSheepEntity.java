package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SacrificialSheepEntity extends Sheep {

    private static final int PRECIOUS_NUTRITION_STEP = 5;
    private static final int FEED_SUCCESS_PARTICLE_COUNT = 8;
    private static final double FEED_SUCCESS_PARTICLE_XZ_SPREAD = 0.2D;
    private static final double FEED_SUCCESS_PARTICLE_Y_SPREAD = 0.3D;
    private static final double FEED_SUCCESS_PARTICLE_SPEED = 0.01D;
    private static final float FEED_SUCCESS_SOUND_VOLUME = 0.8F;
    private static final float FEED_SUCCESS_SOUND_PITCH = 1.0F;
    private static final int MORALITY_COST_STEP = 1;

    private int spiritReserve;
    private int moralityOffsetFromSacrifice;
    private int successfulFeedCountForTest;
    private boolean successfulFeedNameTagVisibleForTest;
    private int successfulFeedParticleTriggerCountForTest;
    private int successfulFeedSoundTriggerCountForTest;

    public SacrificialSheepEntity(EntityType<? extends Sheep> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (isValuableFeed(itemStack)) {
            if (!level().isClientSide()) {
                handleSuccessfulFeed((ServerLevel) level());
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    public int getSpiritReserve() {
        return spiritReserve;
    }

    public int getSuccessfulFeedCountForTest() {
        return successfulFeedCountForTest;
    }

    public boolean wasSuccessfulFeedNameTagVisibleForTest() {
        return successfulFeedNameTagVisibleForTest;
    }

    public int getSuccessfulFeedParticleTriggerCountForTest() {
        return successfulFeedParticleTriggerCountForTest;
    }

    public int getSuccessfulFeedSoundTriggerCountForTest() {
        return successfulFeedSoundTriggerCountForTest;
    }

    public int getMoralityOffsetFromSacrificeForTest() {
        return moralityOffsetFromSacrifice;
    }

    public int consumeTribulationOffset() {
        int value = spiritReserve;
        spiritReserve = 0;
        return value;
    }

    private boolean isValuableFeed(ItemStack itemStack) {
        return itemStack.getMaxStackSize() == 1 || itemStack.getRarity() != net.minecraft.world.item.Rarity.COMMON;
    }

    private void handleSuccessfulFeed(ServerLevel serverLevel) {
        spiritReserve += PRECIOUS_NUTRITION_STEP;
        moralityOffsetFromSacrifice -= MORALITY_COST_STEP;
        successfulFeedCountForTest++;
        setCustomName(Component.literal("献祭羊·灵蕴" + spiritReserve));
        setCustomNameVisible(true);
        successfulFeedNameTagVisibleForTest = isCustomNameVisible();
        successfulFeedParticleTriggerCountForTest++;
        serverLevel.sendParticles(
            ParticleTypes.HAPPY_VILLAGER,
            getX(),
            getY() + getBbHeight() / 2.0D,
            getZ(),
            FEED_SUCCESS_PARTICLE_COUNT,
            FEED_SUCCESS_PARTICLE_XZ_SPREAD,
            FEED_SUCCESS_PARTICLE_Y_SPREAD,
            FEED_SUCCESS_PARTICLE_XZ_SPREAD,
            FEED_SUCCESS_PARTICLE_SPEED
        );
        successfulFeedSoundTriggerCountForTest++;
        serverLevel.playSound(
            null,
            blockPosition(),
            SoundEvents.SHEEP_AMBIENT,
            SoundSource.NEUTRAL,
            FEED_SUCCESS_SOUND_VOLUME,
            FEED_SUCCESS_SOUND_PITCH
        );
    }
}
