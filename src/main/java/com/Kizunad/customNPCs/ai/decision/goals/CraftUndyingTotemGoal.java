package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.WorldStateKeys;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.interaction.MaterialValueManager;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.ai.llm.LlmPromptRegistry;
import com.Kizunad.customNPCs.ai.logging.MindLog;
import com.Kizunad.customNPCs.ai.logging.MindLogLevel;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.entity.CustomNpcEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 闲时制作不死图腾：缺少备用时消耗材料制作，最多保有 5 个。
 */
public class CraftUndyingTotemGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "CraftUndyingTotemGoal: when safe and below five totems, craft Totem of Undying using material points.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float PRIORITY = 0.2F; // 与基础制作目标接近，优先保证盾牌等先行
    private static final int COOLDOWN_TICKS = 200;
    private static final int MAX_TOTEMS = 5;

    private long nextAllowedGameTime;
    private boolean finished;

    @Override
    public String getName() {
        return "craft_undying_totem";
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

        int crafted = craftTotems(npc, mind);
        finished = true;

        if (crafted > 0) {
            MindLog.decision(
                MindLogLevel.INFO,
                "CraftUndyingTotemGoal 制作不死图腾 x{}，消耗 {} 材料，剩余 {}",
                crafted,
                crafted * getTotemCost(),
                npc.getMaterial()
            );
        } else {
            MindLog.decision(
                MindLogLevel.WARN,
                "CraftUndyingTotemGoal 材料不足/成本缺失或已达上限，跳过"
            );
        }
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 一次性制作，无需逐 tick 逻辑
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        // 无需清理
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof CustomNpcEntity)) {
            return true;
        }
        return finished || countTotems(mind, entity) >= MAX_TOTEMS;
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
        float cost = getTotemCost();
        if (cost <= 0.0F || npc.getMaterial() < cost) {
            return false;
        }
        int existing = countTotems(mind, entity);
        if (existing >= MAX_TOTEMS) {
            return false;
        }
        int maxByMaterial = (int) Math.floor(npc.getMaterial() / cost);
        return maxByMaterial > 0;
    }

    private int craftTotems(CustomNpcEntity npc, INpcMind mind) {
        float cost = getTotemCost();
        if (cost <= 0.0F) {
            return 0;
        }

        NpcInventory inventory = mind.getInventory();
        int existing = countTotems(mind, npc);
        int missing = Math.max(0, MAX_TOTEMS - existing);
        if (missing <= 0) {
            return 0;
        }

        int maxByMaterial = (int) Math.floor(npc.getMaterial() / cost);
        int craftable = Math.min(missing, maxByMaterial);
        if (craftable <= 0) {
            return 0;
        }

        int crafted = 0;
        for (int i = 0; i < craftable; i++) {
            if (npc.getMaterial() < cost) {
                break;
            }
            ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
            ItemStack leftover = inventory.addItem(totem);
            npc.setMaterial(npc.getMaterial() - cost);
            crafted++;

            if (!leftover.isEmpty()) {
                npc.spawnAtLocation(leftover);
                MindLog.execution(
                    MindLogLevel.WARN,
                    "CraftUndyingTotemGoal 背包已满，图腾已掉落"
                );
            }
        }
        return crafted;
    }

    private int countTotems(INpcMind mind, LivingEntity entity) {
        int count = 0;
        count += stackCountIfTotem(entity.getMainHandItem());
        count += stackCountIfTotem(entity.getOffhandItem());

        NpcInventory inventory = mind.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            count += stackCountIfTotem(inventory.getItem(i));
        }
        return count;
    }

    private int stackCountIfTotem(ItemStack stack) {
        return stack.is(Items.TOTEM_OF_UNDYING) ? stack.getCount() : 0;
    }

    private float getTotemCost() {
        return MaterialValueManager.getInstance().getMaterialValue(
            new ItemStack(Items.TOTEM_OF_UNDYING)
        );
    }
}
