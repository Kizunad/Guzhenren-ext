package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class TreasureMinkEntity extends Fox {

    private static final int SEARCH_INTERVAL_TICKS = 20;

    private static final double SEARCH_RADIUS = 6.0D;

    private static final double PICKUP_DISTANCE_SQR = 2.25D;

    private static final int BURROW_DELAY_TICKS = 40;

    private static final int BURROW_PARTICLE_COUNT = 12;
    private static final double MOVE_TO_LOOT_SPEED = 1.2D;
    private static final double BURROW_Y_OFFSET = 0.3D;
    private static final double BURROW_PARTICLE_XZ_SPREAD = 0.2D;
    private static final double BURROW_PARTICLE_Y_SPREAD = 0.1D;
    private static final double BURROW_PARTICLE_SPEED = 0.01D;
    private static final float BURROW_SOUND_VOLUME = 0.8F;
    private static final float BURROW_SOUND_PITCH = 0.8F;
    private static final String SECURED_NAME_TAG_TEXT = "盗宝鼬·已得手";

    private int searchTicker;
    private int burrowTicks;
    private boolean valuableLootSecured;
    private int secureSuccessCountForTest;
    private int burrowTriggerCountForTest;
    private int burrowParticleTriggerCountForTest;
    private int burrowSoundTriggerCountForTest;
    private boolean secureNameTagVisibleForTest;
    private ItemStack lastSecuredLootSnapshotForTest = ItemStack.EMPTY;

    public TreasureMinkEntity(EntityType<? extends Fox> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) {
            return;
        }
        if (valuableLootSecured) {
            burrowTicks--;
            if (burrowTicks <= 0) {
                triggerBurrowAndDisappear();
            }
            return;
        }
        searchTicker++;
        if (searchTicker < SEARCH_INTERVAL_TICKS) {
            return;
        }
        searchTicker = 0;
        recognizeHeldValuableLootIfNeeded();
        if (valuableLootSecured) {
            return;
        }
        trySecureValuableLoot();
    }

    public boolean hasValuableLootSecured() {
        return valuableLootSecured;
    }

    public void forceSecureForTest() {
        valuableLootSecured = true;
        burrowTicks = BURROW_DELAY_TICKS;
    }

    public int getSecureSuccessCountForTest() {
        return secureSuccessCountForTest;
    }

    public int getBurrowTriggerCountForTest() {
        return burrowTriggerCountForTest;
    }

    public boolean wasSecureNameTagVisibleForTest() {
        return secureNameTagVisibleForTest;
    }

    public int getBurrowParticleTriggerCountForTest() {
        return burrowParticleTriggerCountForTest;
    }

    public int getBurrowSoundTriggerCountForTest() {
        return burrowSoundTriggerCountForTest;
    }

    public ItemStack getLastSecuredLootSnapshotForTest() {
        return lastSecuredLootSnapshotForTest.copy();
    }

    public boolean hasLocalMaterialLossCostSnapshotForTest() {
        return !lastSecuredLootSnapshotForTest.isEmpty();
    }

    public boolean hasBurrowedAwayWithLootForTest() {
        return burrowTriggerCountForTest > 0 && hasLocalMaterialLossCostSnapshotForTest();
    }

    private void trySecureValuableLoot() {
        List<ItemEntity> nearbyItems = level().getEntitiesOfClass(
            ItemEntity.class,
            getBoundingBox().inflate(SEARCH_RADIUS),
            item -> item.isAlive() && isValuable(item.getItem())
        );
        if (nearbyItems.isEmpty()) {
            return;
        }
        ItemEntity target = nearbyItems.stream()
            .min((left, right) -> Double.compare(distanceToSqr(left), distanceToSqr(right)))
            .orElse(null);
        if (target == null) {
            return;
        }
        getNavigation().moveTo(target, MOVE_TO_LOOT_SPEED);
        if (distanceToSqr(target) <= PICKUP_DISTANCE_SQR) {
            ItemStack securedLootSnapshot = target.getItem().copy();
            target.discard();
            handleRealSecureSuccess(securedLootSnapshot);
        }
    }

    private void recognizeHeldValuableLootIfNeeded() {
        if (valuableLootSecured) {
            return;
        }
        ItemStack heldStack = getMainHandItem();
        if (heldStack.isEmpty() || !isValuable(heldStack)) {
            return;
        }
        handleRealSecureSuccess(heldStack.copy());
    }

    private boolean isValuable(ItemStack stack) {
        if (stack.is(Items.DIAMOND)) {
            return true;
        }
        return stack.getRarity() == Rarity.RARE
            || stack.getRarity() == Rarity.EPIC
            || stack.getMaxStackSize() == 1;
    }

    private void triggerBurrowAndDisappear() {
        burrowTriggerCountForTest++;
        if (level() instanceof ServerLevel serverLevel) {
            burrowParticleTriggerCountForTest++;
            serverLevel.sendParticles(
                ParticleTypes.POOF,
                getX(),
                getY() + BURROW_Y_OFFSET,
                getZ(),
                BURROW_PARTICLE_COUNT,
                BURROW_PARTICLE_XZ_SPREAD,
                BURROW_PARTICLE_Y_SPREAD,
                BURROW_PARTICLE_XZ_SPREAD,
                BURROW_PARTICLE_SPEED
            );
            burrowSoundTriggerCountForTest++;
            serverLevel.playSound(
                null,
                blockPosition(),
                SoundEvents.FOX_SCREECH,
                SoundSource.HOSTILE,
                BURROW_SOUND_VOLUME,
                BURROW_SOUND_PITCH
            );
        }
        discard();
    }

    private void handleRealSecureSuccess(ItemStack securedLootSnapshot) {
        valuableLootSecured = true;
        burrowTicks = BURROW_DELAY_TICKS;
        secureSuccessCountForTest++;
        lastSecuredLootSnapshotForTest = securedLootSnapshot.isEmpty()
            ? ItemStack.EMPTY
            : securedLootSnapshot.copy();
        setCustomName(Component.literal(SECURED_NAME_TAG_TEXT));
        setCustomNameVisible(true);
        secureNameTagVisibleForTest = isCustomNameVisible();
    }
}
