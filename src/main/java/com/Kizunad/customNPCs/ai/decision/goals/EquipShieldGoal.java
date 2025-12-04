package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.common.EquipShieldAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

/**
 * 将现有盾牌装备到副手。
 */
public class EquipShieldGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "EquipShieldGoal: when a shield exists in inventory or mainhand and offhand lacks one, equip it to offhand.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float PRIORITY = 0.22F; // 高于制作/升级，以便先佩戴

    @Override
    public String getName() {
        return "equip_shield";
    }

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        return canRun(mind, entity) ? PRIORITY : 0.0F;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof CustomNpcEntity)) {
            return false;
        }
        if (mind.getMemory().hasMemory(WorldStateKeys.IN_DANGER)) {
            return false;
        }
        return needsShield(mind, entity);
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        mind.getActionExecutor().addAction(new EquipShieldAction());
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 具体执行由动作处理
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        // 无需额外清理
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        return !needsShield(mind, entity) || mind.getActionExecutor().isIdle();
    }

    private boolean needsShield(INpcMind mind, LivingEntity entity) {
        boolean offhandHasShield = entity
            .getItemInHand(InteractionHand.OFF_HAND)
            .is(Items.SHIELD);
        if (offhandHasShield) {
            return false;
        }
        boolean mainhandHasShield = entity
            .getItemInHand(InteractionHand.MAIN_HAND)
            .is(Items.SHIELD);
        if (mainhandHasShield) {
            return true;
        }
        return mind.getInventory().anyMatch(stack -> stack.is(Items.SHIELD));
    }
}
