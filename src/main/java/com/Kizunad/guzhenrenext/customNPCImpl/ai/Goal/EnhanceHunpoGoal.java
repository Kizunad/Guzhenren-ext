package com.Kizunad.guzhenrenext.customNPCImpl.ai.Goal;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.guzhenrenext.guzhenrenBridge.PlayerVariablesSyncHelper;
import com.Kizunad.guzhenrenext.customNPCImpl.util.GuCultivationHelper;
import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.entity.LivingEntity;

/**
 * 精炼魂魄目标。
 * <p>
 * 当魂魄充盈且环境安全时，消耗当前魂魄值以稳步提升
 * {@code zuida_hunpo}，让 NPC 在长线作战中拥有更高的魂魄上限。
 * 目标包含会话时长与冷却限制，避免长时间占用 AI。
 * </p>
 */
public class EnhanceHunpoGoal extends AbstractGuzhenrenGoal {

    private static final float MIN_PRIORITY = 0.2F;
    private static final float MAX_PRIORITY = 0.55F;
    private static final int TICKS_PER_SECOND = 20;
    private static final int UPDATE_INTERVAL_TICKS = TICKS_PER_SECOND;
    private static final int MAX_SESSION_SECONDS = 30;
    private static final int COOLDOWN_SECONDS = 20;
    private static final int MAX_SESSION_TICKS =
        MAX_SESSION_SECONDS * TICKS_PER_SECOND;
    private static final int COOLDOWN_TICKS =
        COOLDOWN_SECONDS * TICKS_PER_SECOND;
    private static final double MIN_RATIO_TO_REFINING = 0.75D;
    private static final double HUNPO_COST_PER_SECOND = 40.0D;
    private static final double BASE_CAPACITY_GAIN = 18.0D;
    private static final double TURN_CAPACITY_BONUS = 4.0D;
    private static final double EPSILON = 1.0E-4D;

    private int tickCounter;
    private int sessionTicks;
    private boolean finished;
    private long cooldownUntilGameTime;

    public EnhanceHunpoGoal() {
        super("enhance_hunpo");
    }

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        if (!canRun(mind, entity)) {
            return 0.0F;
        }
        GuzhenrenModVariables.PlayerVariables vars =
            GuCultivationHelper.getVariables(entity);
        if (vars == null) {
            return 0.0F;
        }
        double ratio = getHunpoRatio(vars);
        if (ratio < MIN_RATIO_TO_REFINING) {
            return 0.0F;
        }
        double normalized =
            Math.min(
                1.0D,
                (ratio - MIN_RATIO_TO_REFINING) /
                (1.0D - MIN_RATIO_TO_REFINING)
            );
        float weighted = (float) Math.max(0.0D, normalized);
        return MIN_PRIORITY + (MAX_PRIORITY - MIN_PRIORITY) * weighted;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        if (!super.canRun(mind, entity)) {
            return false;
        }
        if (entity == null || entity.level().isClientSide()) {
            return false;
        }
        if (entity.level().getGameTime() < cooldownUntilGameTime) {
            return false;
        }
        GuzhenrenModVariables.PlayerVariables vars =
            GuCultivationHelper.getVariables(entity);
        if (vars == null) {
            return false;
        }
        return hasRefinableHunpo(vars);
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        super.start(mind, entity);
        tickCounter = 0;
        sessionTicks = 0;
        finished = false;
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        GuzhenrenModVariables.PlayerVariables vars =
            GuCultivationHelper.getVariables(entity);
        if (vars == null) {
            requestStopWithCooldown(entity);
            return;
        }

        sessionTicks++;
        if (sessionTicks >= MAX_SESSION_TICKS) {
            requestStopWithCooldown(entity);
            return;
        }
        if (!hasRefinableHunpo(vars)) {
            requestStopWithCooldown(entity);
            return;
        }

        tickCounter++;
        if (tickCounter < UPDATE_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;

        if (!consumeHunpo(vars)) {
            requestStopWithCooldown(entity);
            return;
        }
        increaseCapacity(vars, computeCapacityGain(vars));

        if (!hasRefinableHunpo(vars)) {
            requestStopWithCooldown(entity);
        }
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        super.stop(mind, entity);
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        if (finished) {
            return true;
        }
        GuzhenrenModVariables.PlayerVariables vars =
            GuCultivationHelper.getVariables(entity);
        return vars == null;
    }

    private boolean hasRefinableHunpo(
        GuzhenrenModVariables.PlayerVariables vars
    ) {
        if (vars == null) {
            return false;
        }
        double ratio = getHunpoRatio(vars);
        return (
            ratio >= MIN_RATIO_TO_REFINING &&
            vars.hunpo >= HUNPO_COST_PER_SECOND - EPSILON
        );
    }

    private double getHunpoRatio(GuzhenrenModVariables.PlayerVariables vars) {
        double max = Math.max(EPSILON, vars.zuida_hunpo);
        double current = Math.max(0.0D, vars.hunpo);
        return Math.min(1.0D, current / max);
    }

    private boolean consumeHunpo(GuzhenrenModVariables.PlayerVariables vars) {
        double current = Math.max(0.0D, vars.hunpo);
        if (current < HUNPO_COST_PER_SECOND) {
            vars.hunpo = 0.0D;
            PlayerVariablesSyncHelper.markSyncDirty(vars);
            return false;
        }
        double next = current - HUNPO_COST_PER_SECOND;
        if (Double.compare(current, next) != 0) {
            vars.hunpo = next;
            PlayerVariablesSyncHelper.markSyncDirty(vars);
        }
        return true;
    }

    private double computeCapacityGain(
        GuzhenrenModVariables.PlayerVariables vars
    ) {
        double rank = Math.max(1.0D, Math.floor(vars.zhuanshu));
        double rankBonus = (rank - 1.0D) * TURN_CAPACITY_BONUS;
        return BASE_CAPACITY_GAIN + rankBonus;
    }

    private void increaseCapacity(
        GuzhenrenModVariables.PlayerVariables vars,
        double gain
    ) {
        if (gain <= 0.0D) {
            return;
        }
        double current = Math.max(0.0D, vars.zuida_hunpo);
        double next = current + gain;
        if (Double.compare(current, next) != 0) {
            vars.zuida_hunpo = next;
            PlayerVariablesSyncHelper.markSyncDirty(vars);
        }
    }

    /**
     * 请求进入冷却期，避免持续运行。
     */
    private void requestStopWithCooldown(LivingEntity entity) {
        if (finished || entity == null) {
            finished = true;
            return;
        }
        finished = true;
        sessionTicks = 0;
        tickCounter = 0;
        cooldownUntilGameTime = entity.level().getGameTime() + COOLDOWN_TICKS;
    }
}
