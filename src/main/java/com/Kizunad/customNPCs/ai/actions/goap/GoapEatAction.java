package com.Kizunad.customNPCs.ai.actions.goap;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.EatFromInventoryAction;
import com.Kizunad.customNPCs.ai.planner.IGoapAction;
import com.Kizunad.customNPCs.ai.planner.WorldState;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

/**
 * GOAP 进食动作,包装 EatFromInventoryAction。
 * <p>
 * 该动作用于GOAP规划系统,提供进食行为的前置条件和效果。
 * </p>
 */
public class GoapEatAction implements IGoapAction {

    /** 饥饿恢复状态的短期记忆持续时间(tick) */
    private static final int HUNGER_RESTORED_MEMORY_DURATION = 200;

    /** 前置条件 */
    private final WorldState preconditions;
    
    /** 效果 */
    private final WorldState effects;
    
    /** 实际执行的进食动作 */
    private final EatFromInventoryAction action = new EatFromInventoryAction();

    /**
     * 创建GOAP进食动作。
     * 设置前置条件为有食物且饥饿,效果为恢复饥饿度。
     */
    public GoapEatAction() {
        preconditions = new WorldState();
        preconditions.setState(WorldStateKeys.HAS_FOOD, true);
        preconditions.setState(WorldStateKeys.IS_HUNGRY, true);

        effects = new WorldState();
        effects.setState(WorldStateKeys.HUNGER_RESTORED, true);
        effects.setState(WorldStateKeys.IS_HUNGRY, false);
    }

    @Override
    public WorldState getPreconditions() {
        return preconditions;
    }

    @Override
    public WorldState getEffects() {
        return effects;
    }

    @Override
    public float getCost() {
        return 1.0f;
    }

    @Override
    public ActionStatus tick(INpcMind mind, LivingEntity entity) {
        ActionStatus status = action.tick(mind, entity);
        if (status == ActionStatus.SUCCESS) {
            mind
                .getMemory()
                .rememberShortTerm(WorldStateKeys.HUNGER_RESTORED, true, HUNGER_RESTORED_MEMORY_DURATION);
        }
        return status;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        action.start(mind, entity);
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        action.stop(mind, entity);
    }

    @Override
    public boolean canInterrupt() {
        return action.canInterrupt();
    }

    @Override
    public String getName() {
        return "goap_eat";
    }
}
