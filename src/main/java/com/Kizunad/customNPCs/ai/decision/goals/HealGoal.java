package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.UseItemAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 回复Goal - 当血量低时，寻找高回复度物品（药水/金苹果等）进行治疗。
 */
public class HealGoal implements IGoal {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealGoal.class);

    private static final float HEAL_THRESHOLD = 0.5f; // 50% 血量触发
    private static final float HEALTHY_THRESHOLD = 0.8f; // 80% 血量认为已恢复
    private static final int HEALING_MEMORY_DURATION = 200; // 10秒

    private static final int MAIN_HAND_SLOT = -1;
    private static final int OFF_HAND_SLOT = -2;

    private UseItemAction currentAction = null;
    private ItemStack previousMainHand = ItemStack.EMPTY;
    private ItemStack previousOffHand = ItemStack.EMPTY;
    private int sourceSlot = -1;
    private ItemStack sourceStack = ItemStack.EMPTY;

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        float healthPercentage = entity.getHealth() / entity.getMaxHealth();
        if (healthPercentage >= HEAL_THRESHOLD) {
            return 0.0f;
        }
        HealingCandidate candidate = findHealingItem(mind, entity);
        if (candidate == null) {
            return 0.0f;
        }
        return 1.0f - healthPercentage;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return entity.getHealth() < entity.getMaxHealth() * HEAL_THRESHOLD
            && findHealingItem(mind, entity) != null;
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        mind.getMemory().rememberShortTerm(
            "is_healing",
            true,
            HEALING_MEMORY_DURATION
        );

        LOGGER.info(
            "[HealGoal] {} 开始治疗 | 当前血量: {}/{}",
            entity.getName().getString(),
            entity.getHealth(),
            entity.getMaxHealth()
        );
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        if (currentAction == null) {
            HealingCandidate candidate = findHealingItem(mind, entity);
            if (candidate == null) {
                LOGGER.warn("[HealGoal] 找不到可用于治疗的物品");
                return;
            }
            prepareItem(candidate, mind, entity);
            currentAction = new UseItemAction(entity.getMainHandItem().getItem());
            currentAction.start(mind, entity);
            LOGGER.debug("[HealGoal] 开始使用治疗物品: {}", entity.getMainHandItem().getDescriptionId());
        }

        if (currentAction != null) {
            ActionStatus status = currentAction.tick(mind, entity);
            if (status == ActionStatus.SUCCESS) {
                LOGGER.info("[HealGoal] 治疗物品使用成功");
                cleanupAfterUse(mind, entity);
                currentAction = null;
            } else if (status == ActionStatus.FAILURE) {
                LOGGER.warn("[HealGoal] 治疗物品使用失败");
                rollback(mind, entity);
                currentAction = null;
            }
        }
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        mind.getMemory().forget("is_healing");

        if (currentAction != null) {
            currentAction.stop(mind, entity);
            currentAction = null;
            rollback(mind, entity);
        }

        LOGGER.info(
            "[HealGoal] {} 停止治疗 | 最终血量: {}/{}",
            entity.getName().getString(),
            entity.getHealth(),
            entity.getMaxHealth()
        );
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        boolean healthRestored = entity.getHealth() >= entity.getMaxHealth() * HEALTHY_THRESHOLD;
        boolean noHealingItem = findHealingItem(mind, entity) == null;

        return healthRestored || noHealingItem;
    }

    @Override
    public String getName() {
        return "heal";
    }

    private HealingCandidate findHealingItem(INpcMind mind, LivingEntity entity) {
        if (!(entity instanceof Mob mob)) {
            return null;
        }
        HealingCandidate best = null;

        ItemStack main = mob.getMainHandItem();
        if (!main.isEmpty()) {
            double score = healingScore(main, mob);
            if (score > 0) {
                best = better(best, new HealingCandidate(main.copy(), MAIN_HAND_SLOT, score));
            }
        }

        ItemStack off = mob.getOffhandItem();
        if (!off.isEmpty()) {
            double score = healingScore(off, mob);
            if (score > 0) {
                best = better(best, new HealingCandidate(off.copy(), OFF_HAND_SLOT, score));
            }
        }

        NpcInventory inventory = mind.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            double score = healingScore(stack, mob);
            if (score > 0) {
                best = better(best, new HealingCandidate(stack.copy(), i, score));
            }
        }
        return best;
    }

    private double healingScore(ItemStack stack, LivingEntity entity) {
        Item item = stack.getItem();
        if (item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION) {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            double score = 0;
            if (contents != null) {
                for (var effect : contents.getAllEffects()) {
                    if (effect.getEffect().equals(net.minecraft.world.effect.MobEffects.HEAL)) {
                        score += 10 + effect.getAmplifier() * 5;
                    }
                    if (effect.getEffect().equals(net.minecraft.world.effect.MobEffects.REGENERATION)) {
                        score += 6 + effect.getAmplifier() * 2 + effect.getDuration() / 40.0;
                    }
                }
            } else {
                score = 4; // 兜底认为是正面治疗类药水
            }
            return score;
        }

        if (item == Items.GOLDEN_APPLE) {
            return 12;
        }
        if (item == Items.ENCHANTED_GOLDEN_APPLE) {
            return 20;
        }

        var food = stack.getFoodProperties(entity);
        if (food != null) {
            double score = 0;
            for (var pair : food.effects()) {
                if (pair.effect() != null && pair.effect().getEffect().equals(net.minecraft.world.effect.MobEffects.REGENERATION)) {
                    score += 4 + pair.effect().getDuration() / 40.0;
                }
            }
            return score;
        }
        return 0;
    }

    private void prepareItem(HealingCandidate candidate, INpcMind mind, LivingEntity entity) {
        previousMainHand = entity.getMainHandItem().copy();
        previousOffHand = entity.getOffhandItem().copy();
        sourceSlot = candidate.slot();
        sourceStack = candidate.stack();
        if (candidate.slot() >= 0) {
            ItemStack removed = mind.getInventory().removeItem(candidate.slot());
            entity.setItemInHand(InteractionHand.MAIN_HAND, removed);
        } else if (candidate.slot() == OFF_HAND_SLOT) {
            entity.setItemInHand(InteractionHand.MAIN_HAND, entity.getOffhandItem());
            entity.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        } else {
            // 主手已有合适物品
            entity.setItemInHand(InteractionHand.MAIN_HAND, entity.getMainHandItem());
        }
    }

    private void cleanupAfterUse(INpcMind mind, LivingEntity entity) {
        ItemStack hand = entity.getItemInHand(InteractionHand.MAIN_HAND);
        if (!hand.isEmpty()) {
            ItemStack leftover = mind.getInventory().addItem(hand);
            if (!leftover.isEmpty()) {
                entity.spawnAtLocation(leftover);
            }
        }
        entity.setItemInHand(InteractionHand.MAIN_HAND, previousMainHand);
        entity.setItemInHand(InteractionHand.OFF_HAND, previousOffHand);
    }

    private void rollback(INpcMind mind, LivingEntity entity) {
        ItemStack hand = entity.getItemInHand(InteractionHand.MAIN_HAND);
        if (!hand.isEmpty()) {
            ItemStack leftover = mind.getInventory().addItem(hand);
            if (!leftover.isEmpty()) {
                entity.spawnAtLocation(leftover);
            }
        }
        entity.setItemInHand(InteractionHand.MAIN_HAND, previousMainHand);
        entity.setItemInHand(InteractionHand.OFF_HAND, previousOffHand);
        if (sourceSlot >= 0 && !sourceStack.isEmpty()) {
            ItemStack leftover = mind.getInventory().addItem(sourceStack);
            if (!leftover.isEmpty()) {
                entity.spawnAtLocation(leftover);
            }
        }
    }

    private HealingCandidate better(HealingCandidate a, HealingCandidate b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return b.score() > a.score() ? b : a;
    }

    private record HealingCandidate(ItemStack stack, int slot, double score) {}
}
