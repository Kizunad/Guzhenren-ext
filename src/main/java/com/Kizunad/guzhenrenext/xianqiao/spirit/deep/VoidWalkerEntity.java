package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import com.Kizunad.guzhenrenext.xianqiao.service.ApertureBoundaryService;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VoidWalkerEntity extends EnderMan {

    private static final int PHASE_SHIFT_INTERVAL_TICKS = 30;
    private static final double PHASE_SHIFT_UPWARD_BOOST = 0.08D;
    private static final int VOID_EDGE_OUTPUT_COUNT = 1;
    private static final double VOID_EDGE_EROSION_COST = 2.0D;
    private static final long VOID_EDGE_MAX_OUTSIDE_DISTANCE_SQUARED = 64L;
    private static final double VOID_EDGE_OUTPUT_X_OFFSET = 4.0D;
    private static final double VOID_EDGE_OUTPUT_Y_OFFSET = 1.0D;
    private static final double VOID_EDGE_OUTPUT_Z_OFFSET = 0.5D;

    private int phaseShiftTicker;
    private boolean carryBlockGoalRemoved;
    private static final Map<UUID, Double> TEST_HUNPO_OVERRIDE = new HashMap<>();

    public VoidWalkerEntity(EntityType<? extends EnderMan> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) {
            return;
        }
        if (!carryBlockGoalRemoved) {
            removeCarryBlockGoals();
        }
        noPhysics = true;
        phaseShiftTicker++;
        if (phaseShiftTicker >= PHASE_SHIFT_INTERVAL_TICKS) {
            phaseShiftTicker = 0;
            setDeltaMovement(getDeltaMovement().add(0.0D, PHASE_SHIFT_UPWARD_BOOST, 0.0D));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            noPhysics = true;
        }
    }

    public void forcePhaseStateForTest() {
        if (!level().isClientSide()) {
            noPhysics = true;
            if (!carryBlockGoalRemoved) {
                removeCarryBlockGoals();
            }
        }
    }

    public boolean forceKillByPlayerSourceForTest(ServerPlayer killer) {
        if (level().isClientSide() || !(level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (!isAlive()) {
            return false;
        }
        DamageSource playerSource = serverLevel.damageSources().playerAttack(killer);
        boolean hurtApplied = hurt(playerSource, Float.MAX_VALUE);
        if (isAlive()) {
            setHealth(0.0F);
            die(playerSource);
        }
        return hurtApplied || !isAlive();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean causedByPlayer) {
        super.dropCustomDeathLoot(level, source, causedByPlayer);
        ServerPlayer killer = null;
        if (source.getEntity() instanceof ServerPlayer sourceEntityPlayer) {
            killer = sourceEntityPlayer;
        }
        if (killer == null && source.getDirectEntity() instanceof ServerPlayer directEntityPlayer) {
            killer = directEntityPlayer;
        }
        if (killer == null) {
            return;
        }
        ApertureInfo apertureInfo = ApertureWorldData.get(level).getAperture(killer.getUUID());
        if (apertureInfo == null) {
            return;
        }
        long outsideDistanceSquared = ApertureBoundaryService.getOutsideDistanceSquared(apertureInfo, blockPosition());
        if (outsideDistanceSquared <= 0L || outsideDistanceSquared > VOID_EDGE_MAX_OUTSIDE_DISTANCE_SQUARED) {
            return;
        }
        if (!consumeOwnerHunPo(level, killer.getUUID(), VOID_EDGE_EROSION_COST)) {
            return;
        }
        ItemEntity output = new ItemEntity(
            level,
            getX() + VOID_EDGE_OUTPUT_X_OFFSET,
            getY() + VOID_EDGE_OUTPUT_Y_OFFSET,
            getZ() + VOID_EDGE_OUTPUT_Z_OFFSET,
            new ItemStack(XianqiaoItems.KONG_SHI_HEI_JING.get(), VOID_EDGE_OUTPUT_COUNT)
        );
        level.addFreshEntity(output);
    }

    private static boolean consumeOwnerHunPo(ServerLevel level, UUID ownerUUID, double cost) {
        double safeCost = Math.max(0.0D, cost);
        if (safeCost <= 0.0D) {
            return true;
        }
        double currentAmount = readOwnerHunPo(level, ownerUUID);
        if (currentAmount < safeCost) {
            return false;
        }
        if (TEST_HUNPO_OVERRIDE.containsKey(ownerUUID)) {
            TEST_HUNPO_OVERRIDE.put(ownerUUID, Math.max(0.0D, currentAmount - safeCost));
        }
        @Nullable ServerPlayer ownerPlayer = findOwnerPlayer(level, ownerUUID);
        if (ownerPlayer != null) {
            HunPoHelper.modify(ownerPlayer, -safeCost);
        }
        return true;
    }

    private static double readOwnerHunPo(ServerLevel level, UUID ownerUUID) {
        if (TEST_HUNPO_OVERRIDE.containsKey(ownerUUID)) {
            return TEST_HUNPO_OVERRIDE.get(ownerUUID);
        }
        @Nullable ServerPlayer ownerPlayer = findOwnerPlayer(level, ownerUUID);
        if (ownerPlayer == null) {
            return 0.0D;
        }
        return HunPoHelper.getAmount(ownerPlayer);
    }

    @Nullable
    private static ServerPlayer findOwnerPlayer(ServerLevel level, UUID ownerUUID) {
        @Nullable ServerPlayer listedOwner = level.getServer().getPlayerList().getPlayer(ownerUUID);
        if (listedOwner != null) {
            return listedOwner;
        }
        for (ServerPlayer player : level.players()) {
            if (player.getUUID().equals(ownerUUID)) {
                return player;
            }
        }
        return null;
    }

    public static void seedHunPoAmountForTest(ServerPlayer owner, double amount) {
        double safeAmount = Math.max(0.0D, amount);
        TEST_HUNPO_OVERRIDE.put(owner.getUUID(), safeAmount);
        try {
            double currentAmount = HunPoHelper.getAmount(owner);
            HunPoHelper.modify(owner, safeAmount - currentAmount);
        } catch (Throwable ignored) {
        }
    }

    public static double readHunPoAmountForTest(ServerLevel level, UUID ownerUUID) {
        return readOwnerHunPo(level, ownerUUID);
    }

    private void removeCarryBlockGoals() {
        goalSelector.getAvailableGoals().removeIf(this::isCarryBlockGoal);
        targetSelector.getAvailableGoals().removeIf(this::isCarryBlockGoal);
        carryBlockGoalRemoved = true;
    }

    private boolean isCarryBlockGoal(WrappedGoal wrappedGoal) {
        String className = wrappedGoal.getGoal().getClass().getName();
        return className.contains("EndermanLeaveBlockGoal") || className.contains("EndermanTakeBlockGoal");
    }
}
