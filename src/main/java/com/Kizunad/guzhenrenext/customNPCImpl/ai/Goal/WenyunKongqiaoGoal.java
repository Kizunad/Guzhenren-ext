package com.Kizunad.guzhenrenext.customNPCImpl.ai.Goal;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.guzhenrenext.customNPCImpl.util.GuCultivationHelper;
import com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper;
import net.guzhenren.network.GuzhenrenModVariables;
import net.minecraft.world.entity.LivingEntity;

/**
 * 温蕴空窍目标。
 * <p>
 * 当 NPC 闲置且真元充足时，消耗真元稳步提升
 * {@code gushi_xiulian_dangqian}，并在进度溢出后自动提升
 * 阶段与转数，实现“进度 -> 阶段 -> 转数”的循环。
 * </p>
 */
public class WenyunKongqiaoGoal extends AbstractGuzhenrenGoal {

    private static final float MIN_PRIORITY = 0.25F;
    private static final float MAX_PRIORITY = 0.6F;
    private static final int TICKS_PER_SECOND = 20;
    private static final int UPDATE_INTERVAL_TICKS = TICKS_PER_SECOND;
    private static final int MAX_SESSION_SECONDS = 45;
    private static final int COOLDOWN_SECONDS = 15;
    private static final int MAX_SESSION_TICKS =
        MAX_SESSION_SECONDS * TICKS_PER_SECOND;
    private static final int COOLDOWN_TICKS =
        COOLDOWN_SECONDS * TICKS_PER_SECOND;
    private static final double BASE_PROGRESS_PER_SECOND = 50.0D;
    private static final double PRIMEVAL_COST_PER_SECOND = 15.0D;
    private static final double MIN_PRIMEVAL_RATIO = 0.15D;
    private static final double STAGE_BONUS_PER_LEVEL = 0.1D;
    private static final double RANK_BONUS_PER_TURN = 0.05D;

    private int tickCounter;
    private int sessionTicks;
    private boolean finished;
    private long cooldownUntilGameTime;

    public WenyunKongqiaoGoal() {
        super("wenyun_kongqiao");
    }

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        if (!canRun(mind, entity)) {
            return 0.0F;
        }
        GuzhenrenModVariables.PlayerVariables vars =
            GuCultivationHelper.getVariables(entity);
        if (vars == null || GuCultivationHelper.isAtMaxRank(vars)) {
            return 0.0F;
        }
        double required = GuCultivationHelper.ensureRequirement(vars);
        if (required <= 0.0D) {
            return 0.0F;
        }
        if (!GuCultivationHelper.needsCultivation(vars)) {
            return MIN_PRIORITY;
        }
        double ratio = Math.min(1.0D, vars.gushi_xiulian_dangqian / required);
        float urgency = (float) (1.0D - ratio);
        return (
            MIN_PRIORITY +
            (MAX_PRIORITY - MIN_PRIORITY) * Math.max(0.0F, urgency)
        );
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
        return vars != null && !GuCultivationHelper.isAtMaxRank(vars);
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
        if (entity.level().isClientSide()) {
            return;
        }
        GuzhenrenModVariables.PlayerVariables vars =
            GuCultivationHelper.getVariables(entity);
        if (vars == null) {
            return;
        }
        GuCultivationHelper.ensureRequirement(vars);
        if (GuCultivationHelper.isAtMaxRank(vars)) {
            return;
        }

        sessionTicks++;
        if (sessionTicks >= MAX_SESSION_TICKS) {
            requestStopWithCooldown(entity);
            return;
        }

        if (!GuCultivationHelper.needsCultivation(vars)) {
            requestStopWithCooldown(entity);
            return;
        }

        if (!shouldConsumePrimeval(vars)) {
            requestStopWithCooldown(entity);
            return;
        }

        tickCounter++;
        if (tickCounter >= UPDATE_INTERVAL_TICKS) {
            tickCounter = 0;
            ZhenYuanHelper.modify(entity, -PRIMEVAL_COST_PER_SECOND);
            double gain = computeProgressGain(vars);
            GuCultivationHelper.addProgress(vars, gain);
        }

        GuCultivationHelper.tryBreakthrough(vars);

        if (!GuCultivationHelper.needsCultivation(vars)) {
            requestStopWithCooldown(entity);
        }
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        super.stop(mind, entity);
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        GuzhenrenModVariables.PlayerVariables vars =
            GuCultivationHelper.getVariables(entity);
        if (finished) {
            return true;
        }
        return vars == null || GuCultivationHelper.isAtMaxRank(vars);
    }

    private boolean shouldConsumePrimeval(
        GuzhenrenModVariables.PlayerVariables vars
    ) {
        double max = Math.max(0.0D, vars.zuida_zhenyuan);
        if (max <= 0.0D) {
            return false;
        }
        if (vars.zhenyuan < PRIMEVAL_COST_PER_SECOND) {
            return false;
        }
        double ratio = vars.zhenyuan / max;
        return ratio >= MIN_PRIMEVAL_RATIO;
    }

    private double computeProgressGain(
        GuzhenrenModVariables.PlayerVariables vars
    ) {
        double stageBonus =
            1.0D +
            (GuCultivationHelper.clampStage(vars.jieduan) - 1) *
            STAGE_BONUS_PER_LEVEL;
        double rankBonus =
            1.0D +
            (Math.max(1.0D, Math.floor(vars.zhuanshu)) - 1.0D) *
            RANK_BONUS_PER_TURN;
        return BASE_PROGRESS_PER_SECOND * stageBonus * rankBonus;
    }

    /**
     * 请求进入冷却期，避免长时间占用 AI。
     */
    private void requestStopWithCooldown(LivingEntity entity) {
        if (finished || entity == null) {
            finished = true;
            return;
        }
        finished = true;
        sessionTicks = 0;
        tickCounter = 0;
        cooldownUntilGameTime =
            entity.level().getGameTime() + COOLDOWN_TICKS;
    }
}
