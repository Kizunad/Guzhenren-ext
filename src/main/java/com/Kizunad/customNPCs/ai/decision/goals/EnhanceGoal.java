package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.IAction;
import com.Kizunad.customNPCs.ai.actions.common.EnhanceAttributeAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;

/**
 * 成长目标：在积累足够经验时优先分配属性点。
 * <p>
 * 特性：
 * - 优先级随经验提升而线性提高，经验越多越偏向执行。
 * - 仅在安全状态且经验满足最低消耗（10 点）时运行。
 * - 选择当前增益最低的方向进行强化，降低收益差异。
 */
public class EnhanceGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "EnhanceGoal: when holding >=10 experience and safe, spend it via EnhanceAttributeAction " +
        "preferring attributes with lowest current bonus.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final int MIN_EXPENSE = 10;
    private static final int MAX_EXPENSE = 100;
    private static final float BASE_PRIORITY = 0.18F;
    private static final float PRIORITY_GAIN = 0.70F; // 经验越多越倾向执行，最高约 0.53

    private IAction plannedAction;

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        if (!canEngage(mind, entity)) {
            return 0.0F;
        }
        int exp = ((CustomNpcEntity) entity).getExperience();
        float factor = Math.min(1.0F, exp / MAX_EXPENSE);
        return BASE_PRIORITY + PRIORITY_GAIN * factor;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return canEngage(mind, entity);
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        CustomNpcEntity npc = (CustomNpcEntity) entity;
        EnhanceAttributeAction.AttributeDirection direction = pickDirection(
            npc
        );
        plannedAction = new EnhanceAttributeAction(direction);
        mind.getActionExecutor().submitPlan(List.of(plannedAction));
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 交由 ActionExecutor 执行，无额外逻辑
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        mind.getActionExecutor().stopCurrentPlan();
        plannedAction = null;
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        if (!canEngage(mind, entity)) {
            return true;
        }
        return mind.getActionExecutor().isIdle();
    }

    @Override
    public String getName() {
        return "enhance";
    }

    private boolean canEngage(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof CustomNpcEntity npc)) {
            return false;
        }
        if (npc.getExperience() < MIN_EXPENSE) {
            return false;
        }
        return !mind.getMemory().hasMemory(WorldStateKeys.IN_DANGER);
    }

    /**
     * 选择当前增益最低的方向，避免某一项收益过高。
     */
    private EnhanceAttributeAction.AttributeDirection pickDirection(
        CustomNpcEntity npc
    ) {
        float strength = npc.getStrengthBonus();
        float health = npc.getHealthBonus();
        float speed = npc.getSpeedBonus();
        float defense = npc.getDefenseBonus();
        float sensor = npc.getSensorBonus();

        float min = strength;
        EnhanceAttributeAction.AttributeDirection dir =
            EnhanceAttributeAction.AttributeDirection.STRENGTH;

        if (health < min) {
            min = health;
            dir = EnhanceAttributeAction.AttributeDirection.HEALTH;
        }
        if (speed < min) {
            min = speed;
            dir = EnhanceAttributeAction.AttributeDirection.SPEED;
        }
        if (defense < min) {
            min = defense;
            dir = EnhanceAttributeAction.AttributeDirection.DEFENSE;
        }
        if (sensor < min) {
            dir = EnhanceAttributeAction.AttributeDirection.SENSOR;
        }
        return dir;
    }
}
