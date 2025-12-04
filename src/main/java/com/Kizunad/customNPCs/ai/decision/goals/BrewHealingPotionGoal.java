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
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

/**
 * 闲时酿造治疗 I 药水：消耗材料点和食物（优先熟猪排）酿造最多 3 瓶。
 */
public class BrewHealingPotionGoal implements IGoal {

    public static final String LLM_USAGE_DESC =
        "BrewHealingPotionGoal: when safe and with ingredients (prefer cooked porkchop) craft up to three healing " +
        "potions using material points.";

    static {
        LlmPromptRegistry.register(LLM_USAGE_DESC);
    }

    private static final float PRIORITY = 0.19F; // 低于盾牌制作，高于收集材料
    private static final int COOLDOWN_TICKS = 200;
    private static final int MAX_POTIONS = 3;

    private long nextAllowedGameTime;
    private boolean finished;

    @Override
    public String getName() {
        return "brew_healing_potion";
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

        int brewed = brewPotions(npc, mind);
        finished = true;

        if (brewed > 0) {
            MindLog.decision(
                MindLogLevel.INFO,
                "BrewHealingPotionGoal 炼制治疗药水 x{}，消耗 {} 材料，剩余 {}",
                brewed,
                brewed * getPotionMaterialCost(),
                npc.getMaterial()
            );
        } else {
            MindLog.decision(
                MindLogLevel.WARN,
                "BrewHealingPotionGoal 材料或食物不足/已达上限，跳过"
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
        return finished || countHealingPotions(mind, entity) >= MAX_POTIONS;
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
        float cost = getPotionMaterialCost();
        if (cost <= 0.0F || npc.getMaterial() < cost) {
            return false;
        }
        if (!hasIngredient(mind.getInventory())) {
            return false;
        }
        int missing = MAX_POTIONS - countHealingPotions(mind, entity);
        if (missing <= 0) {
            return false;
        }
        int maxByMaterial = (int) Math.floor(npc.getMaterial() / cost);
        return maxByMaterial > 0;
    }

    private int brewPotions(CustomNpcEntity npc, INpcMind mind) {
        float cost = getPotionMaterialCost();
        if (cost <= 0.0F) {
            return 0;
        }

        NpcInventory inventory = mind.getInventory();
        int existing = countHealingPotions(mind, npc);
        int missing = Math.max(0, MAX_POTIONS - existing);
        if (missing <= 0) {
            return 0;
        }

        int availableFood = countIngredients(inventory);
        int maxByMaterial = (int) Math.floor(npc.getMaterial() / cost);
        int craftable = Math.min(
            missing,
            Math.min(availableFood, maxByMaterial)
        );
        if (craftable <= 0) {
            return 0;
        }

        int brewed = 0;
        for (int i = 0; i < craftable; i++) {
            if (npc.getMaterial() < cost) {
                break;
            }
            ItemStack ingredient = consumeIngredient(inventory);
            if (ingredient.isEmpty()) {
                break;
            }

            ItemStack potion = createHealingPotion();
            ItemStack leftover = inventory.addItem(potion);
            npc.setMaterial(npc.getMaterial() - cost);
            brewed++;

            if (!leftover.isEmpty()) {
                npc.spawnAtLocation(leftover);
                MindLog.execution(
                    MindLogLevel.WARN,
                    "BrewHealingPotionGoal 背包已满，药水已掉落"
                );
            }
        }
        return brewed;
    }

    private boolean hasIngredient(NpcInventory inventory) {
        return countIngredients(inventory) > 0;
    }

    private int countIngredients(NpcInventory inventory) {
        int cooked = 0;
        int others = 0;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(Items.COOKED_PORKCHOP)) {
                cooked += stack.getCount();
            } else if (isUsableFood(stack)) {
                others += stack.getCount();
            }
        }
        return cooked + others;
    }

    private ItemStack consumeIngredient(NpcInventory inventory) {
        int slot = inventory.findFirstSlot(stack ->
            stack.is(Items.COOKED_PORKCHOP)
        );
        if (slot < 0) {
            slot = inventory.findFirstSlot(
                stack -> !stack.is(Items.COOKED_PORKCHOP) && isUsableFood(stack)
            );
        }
        if (slot < 0) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = inventory.getItem(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack consumed = stack.copyWithCount(1);
        stack.shrink(1);
        if (stack.isEmpty()) {
            inventory.setItem(slot, ItemStack.EMPTY);
        } else {
            inventory.setItem(slot, stack);
        }
        return consumed;
    }

    private boolean isUsableFood(ItemStack stack) {
        if (stack.getFoodProperties(null) == null) {
            return false;
        }
        Item item = stack.getItem();
        return (
            item != Items.GOLDEN_APPLE && item != Items.ENCHANTED_GOLDEN_APPLE
        );
    }

    private int countHealingPotions(INpcMind mind, LivingEntity entity) {
        int count = 0;
        count += stackCountIfHealing(entity.getMainHandItem());
        count += stackCountIfHealing(entity.getOffhandItem());

        NpcInventory inventory = mind.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            count += stackCountIfHealing(inventory.getItem(i));
        }
        return count;
    }

    private int stackCountIfHealing(ItemStack stack) {
        return isHealingPotion(stack) ? stack.getCount() : 0;
    }

    private boolean isHealingPotion(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        PotionContents contents = stack.getOrDefault(
            DataComponents.POTION_CONTENTS,
            PotionContents.EMPTY
        );
        return (
            contents.is(Potions.HEALING) || contents.is(Potions.STRONG_HEALING)
        );
    }

    private ItemStack createHealingPotion() {
        return PotionContents.createItemStack(Items.POTION, Potions.HEALING);
    }

    private float getPotionMaterialCost() {
        return MaterialValueManager.getInstance().getMaterialValue(
            createHealingPotion()
        );
    }
}
