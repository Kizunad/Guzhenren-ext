package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 缺少盾牌时制作一面盾，消耗材料点。
 */
public class CraftShieldGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "CraftShieldGoal: when safe and lacking a shield, craft one shield using material points.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float PRIORITY = 0.2F; // 与盔甲制作相近
    private static final int COOLDOWN_TICKS = 200;
    private static final float MATERIAL_COST = 5.0F;

    private long nextAllowedGameTime;
    private boolean finished;

    @Override
    public String getName() {
        return "craft_shield";
    }

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        return canEngage(mind, entity) ? PRIORITY : 0.0F;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return canEngage(mind, entity);
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        finished = false;
        if (!(entity instanceof CustomNpcEntity npc)) {
            finished = true;
            return;
        }
        nextAllowedGameTime = entity.level().getGameTime() + COOLDOWN_TICKS;

        boolean crafted = craftShield(npc, mind);
        finished = true;

        if (crafted) {
            MindLog.decision(
                MindLogLevel.INFO,
                "CraftShieldGoal 制作盾牌成功，消耗 {} 材料，剩余 {}",
                MATERIAL_COST,
                npc.getMaterial()
            );
        } else {
            MindLog.decision(
                MindLogLevel.WARN,
                "CraftShieldGoal 材料不足或已拥有盾牌，跳过"
            );
        }
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 制作逻辑在 start 中一次性完成
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        // 无需清理
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof CustomNpcEntity npc)) {
            return true;
        }
        return finished || hasShield(mind, npc);
    }

    private boolean canEngage(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof CustomNpcEntity npc)) {
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
        if (npc.getMaterial() < MATERIAL_COST) {
            return false;
        }
        return !hasShield(mind, npc);
    }

    private boolean hasShield(INpcMind mind, CustomNpcEntity npc) {
        if (npc.getItemBySlot(EquipmentSlot.OFFHAND).is(Items.SHIELD)) {
            return true;
        }
        if (npc.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.SHIELD)) {
            return true;
        }
        return mind.getInventory().anyMatch(stack -> stack.is(Items.SHIELD));
    }

    private boolean craftShield(CustomNpcEntity npc, INpcMind mind) {
        if (npc.getMaterial() < MATERIAL_COST || hasShield(mind, npc)) {
            return false;
        }

        ItemStack stack = new ItemStack(Items.SHIELD);
        ItemStack remaining = mind.getInventory().addItem(stack);
        npc.setMaterial(npc.getMaterial() - MATERIAL_COST);

        if (!remaining.isEmpty()) {
            npc.spawnAtLocation(remaining);
            MindLog.execution(
                MindLogLevel.WARN,
                "CraftShieldGoal 背包已满，盾牌已掉落"
            );
        }
        return true;
    }
}
