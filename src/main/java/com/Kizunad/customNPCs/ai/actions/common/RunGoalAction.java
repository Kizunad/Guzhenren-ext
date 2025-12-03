package com.Kizunad.customNPCs.ai.actions.common;

import com.Kizunad.customNPCs.ai.NpcMindRegistry;
import com.Kizunad.customNPCs.ai.actions.AbstractStandardAction;
import com.Kizunad.customNPCs.ai.actions.ActionResult;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.decision.UtilityGoalSelector;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 目标桥接动作：将 LLM/计划中出现的 Goal 名称映射为真正的 Utility 目标执行。
 * <p>
 * 行为：
 * - 确保目标已注册（若缺失则按注册表创建并注册）。
 * - 在安全可运行时强制切换到目标，并等待目标完成。
 * - 如果目标无法运行/不存在则返回失败，便于上层重试或更换计划。
 */
public class RunGoalAction extends AbstractStandardAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        RunGoalAction.class
    );
    private static final int GOAL_TIMEOUT_TICKS =
        CONFIG.getDefaultTimeoutTicks() * 2;

    private final String goalName;
    private IGoal goalInstance;
    private String lastFailureReason = "";

    public RunGoalAction(String goalName) {
        super(
            "Goal:" + goalName,
            null,
            GOAL_TIMEOUT_TICKS,
            CONFIG.getDefaultMaxRetries(),
            CONFIG.getDefaultNavRange()
        );
        this.goalName = goalName;
    }

    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        UtilityGoalSelector selector = mind.getGoalSelector();
        IGoal goal = ensureGoal(selector);
        if (goal == null) {
            lastFailureReason = "目标未注册: " + goalName;
            LOGGER.warn("[RunGoalAction] {}", lastFailureReason);
            return ActionStatus.FAILURE;
        }

        if (!goal.canRun(mind, mob)) {
            lastFailureReason = "目标当前不可运行";
            return ActionStatus.FAILURE;
        }

        IGoal current = selector.getCurrentGoal();
        if (current != goal) {
            selector.forceSwitchTo(mind, mob, goal);
            return ActionStatus.RUNNING;
        }

        if (goal.isFinished(mind, mob)) {
            return ActionStatus.SUCCESS;
        }

        return ActionStatus.RUNNING;
    }

    @Override
    public ActionResult tickWithReason(INpcMind mind, LivingEntity entity) {
        ActionStatus status = tick(mind, entity);
        if (status == ActionStatus.FAILURE) {
            return new ActionResult(status, lastFailureReason);
        }
        return new ActionResult(status);
    }

    @Override
    protected void onStop(INpcMind mind, LivingEntity entity) {
        lastFailureReason = "";
    }

    @Override
    public boolean canInterrupt() {
        return true;
    }

    private IGoal ensureGoal(UtilityGoalSelector selector) {
        if (goalInstance != null) {
            return goalInstance;
        }
        goalInstance = selector.getGoalByName(goalName);
        if (goalInstance != null) {
            return goalInstance;
        }

        IGoal created = NpcMindRegistry.createGoal(goalName);
        if (created != null) {
            if (!selector.containsGoal(goalName)) {
                selector.registerGoal(created);
            }
            goalInstance = created;
        }
        return goalInstance;
    }
}
