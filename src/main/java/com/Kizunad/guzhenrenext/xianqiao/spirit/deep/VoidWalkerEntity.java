package com.Kizunad.guzhenrenext.xianqiao.spirit.deep;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;

public class VoidWalkerEntity extends EnderMan {

    private static final int PHASE_SHIFT_INTERVAL_TICKS = 30;
    private static final double PHASE_SHIFT_UPWARD_BOOST = 0.08D;

    private int phaseShiftTicker;
    private boolean carryBlockGoalRemoved;

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
