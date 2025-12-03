package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.FurnaceAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.executor.ActionExecutor;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

/**
 * 烹饪目标：当背包中存在可烧炼的食物且有燃料时，批量烹饪提升食物价值。
 * <p>
 * 触发条件：
 * - 背包存在可烹饪的食物（烧炼结果可食用）
 * - 背包存在可用燃料
 * - 非危险状态（避免战斗时停留）
 */
public class CookGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "CookGoal: smelt edible items when fuel exists; stay safe; uses FurnaceAction and ActionExecutor.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float BASE_PRIORITY = 0.45f;
    private static final float HUNGER_BONUS = 0.25f;

    private boolean finished;

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        if (!hasCookableFood(mind, entity)) {
            return 0.0f;
        }
        float priority = BASE_PRIORITY;
        if (mind.getStatus().isHungry()) {
            priority += HUNGER_BONUS;
        }
        return Math.min(1.0f, priority);
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return hasCookableFood(mind, entity) && !isInDanger(mind, entity);
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        finished = false;
        MindLog.decision(
            MindLogLevel.INFO,
            "触发烹饪: {}",
            entity.getName().getString()
        );
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        if (finished) {
            return;
        }
        if (isInDanger(mind, entity)) {
            mind.getActionExecutor().stopCurrentPlan();
            finished = true;
            return;
        }

        ActionExecutor executor = mind.getActionExecutor();
        if (executor.isIdle()) {
            // 如果上一个动作失败，则停止避免无限重试
            if (executor.getLastActionStatus() == ActionStatus.FAILURE) {
                finished = true;
                return;
            }
            if (!hasCookableFood(mind, entity)) {
                finished = true;
                return;
            }
            executor.addAction(new FurnaceAction());
        }
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        finished = false;
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        return finished || !hasCookableFood(mind, entity);
    }

    @Override
    public String getName() {
        return "cook";
    }

    private boolean hasCookableFood(INpcMind mind, LivingEntity entity) {
        return FurnaceAction
            .findCookCandidate(mind.getInventory(), entity.level())
            .isPresent();
    }

    private boolean isInDanger(INpcMind mind, LivingEntity entity) {
        Object danger = mind.getCurrentWorldState(entity).getState(
            WorldStateKeys.IN_DANGER
        );
        return Boolean.TRUE.equals(danger);
    }
}
