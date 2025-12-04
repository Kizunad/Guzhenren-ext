package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.actions.common.GatherMaterialAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.world.entity.LivingEntity;

    /**
     * 闲时收集材料目标：在安全且空闲时执行 GatherMaterialAction 累积材料点。
     */
    public class GatherMaterialGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "GatherMaterialGoal: low-priority idle goal; when safe and idle, run GatherMaterialAction to stockpile "
            + "crafting material.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float BASE_PRIORITY = 0.15F; // 略高于 idle
    private static final int COOLDOWN_TICKS = 200; // 10s 冷却，避免刷材料

    private long nextAllowedGameTime;

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        return canEngage(mind, entity) ? BASE_PRIORITY : 0.0F;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return canEngage(mind, entity);
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof CustomNpcEntity)) {
            return;
        }
        nextAllowedGameTime = entity.level().getGameTime() + COOLDOWN_TICKS;
        mind.getActionExecutor().addAction(new GatherMaterialAction());
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 行为完全由动作执行器驱动，这里无需附加逻辑
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        // 无需清理，动作由执行器管理
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        return mind.getActionExecutor().isIdle();
    }

    @Override
    public String getName() {
        return "gather_material";
    }

    private boolean canEngage(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof CustomNpcEntity)) {
            return false;
        }
        if (entity.level().isClientSide()) {
            return false;
        }
        if (entity.level().getGameTime() < nextAllowedGameTime) {
            return false;
        }
        if (mind.getMemory().hasMemory(WorldStateKeys.IN_DANGER)) {
            return false;
        }
        return mind.getActionExecutor().isIdle();
    }
}
