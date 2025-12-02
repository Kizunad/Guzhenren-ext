package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.UseItemAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.ai.inventory.NpcInventory;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 回复Goal - 当血量低时,寻找高回复度物品(药水/金苹果等)进行治疗。
 * <p>
 * 该目标会在NPC血量低于阈值时触发,自动寻找并使用背包中的治疗物品。
 * 支持药水、金苹果和具有回复效果的食物。
 * </p>
 */
public class HealGoal implements IGoal {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        HealGoal.class
    );

    /** 触发治疗的血量阈值(50%) */
    private static final float HEAL_THRESHOLD = 0.5f;

    /** 认为已恢复健康的血量阈值(80%) */
    private static final float HEALTHY_THRESHOLD = 0.8f;

    /** 治疗记忆持续时间(tick) */
    private static final int HEALING_MEMORY_DURATION = 200;

    /** 主手槽位标识 */
    private static final int MAIN_HAND_SLOT = -1;

    /** 副手槽位标识 */
    private static final int OFF_HAND_SLOT = -2;

    /** 瞬间治疗效果基础分数 */
    private static final int INSTANT_HEAL_BASE_SCORE = 10;

    /** 瞬间治疗效果等级加成 */
    private static final int INSTANT_HEAL_AMPLIFIER_BONUS = 5;

    /** 再生效果基础分数 */
    private static final int REGENERATION_BASE_SCORE = 6;

    /** 再生效果等级加成 */
    private static final int REGENERATION_AMPLIFIER_BONUS = 2;

    /** 再生效果持续时间转换因子(tick转分数) */
    private static final double REGENERATION_DURATION_FACTOR = 40.0;

    /** 未知药水默认分数 */
    private static final int UNKNOWN_POTION_SCORE = 4;

    /** 金苹果治疗分数 */
    private static final int GOLDEN_APPLE_SCORE = 12;

    /** 附魔金苹果治疗分数 */
    private static final int ENCHANTED_GOLDEN_APPLE_SCORE = 20;

    /** 食物再生效果基础分数 */
    private static final int FOOD_REGENERATION_BASE_SCORE = 4;

    private UseItemAction currentAction = null;
    private ItemStack previousMainHand = ItemStack.EMPTY;
    private ItemStack previousOffHand = ItemStack.EMPTY;
    private int sourceSlot = -1;
    private ItemStack sourceStack = ItemStack.EMPTY;

    /**
     * 计算治疗目标的优先级。
     * <p>
     * 优先级与血量成反比:血量越低,优先级越高。
     * 如果血量高于阈值或没有可用的治疗物品,优先级为0。
     * </p>
     *
     * @param mind NPC的思维接口
     * @param entity NPC实体
     * @return 优先级值,范围[0.0, 1.0]
     */
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

    /**
     * 检查治疗目标是否可以运行。
     *
     * @param mind NPC的思维接口
     * @param entity NPC实体
     * @return 如果血量低于阈值且有可用治疗物品,返回true
     */
    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return (
            entity.getHealth() < entity.getMaxHealth() * HEAL_THRESHOLD &&
            findHealingItem(mind, entity) != null
        );
    }

    /**
     * 开始执行治疗目标。
     * 在短期记忆中记录正在治疗的状态。
     *
     * @param mind NPC的思维接口
     * @param entity NPC实体
     */
    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        mind
            .getMemory()
            .rememberShortTerm("is_healing", true, HEALING_MEMORY_DURATION);

        LOGGER.info(
            "[HealGoal] {} 开始治疗 | 当前血量: {}/{}",
            entity.getName().getString(),
            entity.getHealth(),
            entity.getMaxHealth()
        );
    }

    /**
     * 每tick执行一次的治疗逻辑。
     * <p>
     * 如果尚未开始使用物品,则寻找最佳治疗物品并开始使用。
     * 如果正在使用物品,则继续执行使用动作直到成功或失败。
     * </p>
     *
     * @param mind NPC的思维接口
     * @param entity NPC实体
     */
    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        if (currentAction == null) {
            HealingCandidate candidate = findHealingItem(mind, entity);
            if (candidate == null) {
                LOGGER.warn("[HealGoal] 找不到可用于治疗的物品");
                return;
            }
            prepareItem(candidate, mind, entity);
            currentAction = new UseItemAction(
                entity.getMainHandItem().getItem()
            );
            currentAction.start(mind, entity);
            LOGGER.debug(
                "[HealGoal] 开始使用治疗物品: {}",
                entity.getMainHandItem().getDescriptionId()
            );
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

    /**
     * 停止执行治疗目标。
     * 清除治疗记忆,停止当前动作,并恢复物品状态。
     *
     * @param mind NPC的思维接口
     * @param entity NPC实体
     */
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

    /**
     * 检查治疗目标是否已完成。
     *
     * @param mind NPC的思维接口
     * @param entity NPC实体
     * @return 如果血量已恢复到健康阈值或没有可用治疗物品,返回true
     */
    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        boolean healthRestored =
            entity.getHealth() >= entity.getMaxHealth() * HEALTHY_THRESHOLD;
        boolean noHealingItem = findHealingItem(mind, entity) == null;

        return healthRestored || noHealingItem;
    }

    /**
     * 获取目标名称。
     *
     * @return 目标名称"heal"
     */
    @Override
    public String getName() {
        return "heal";
    }

    /**
     * 寻找最佳治疗物品。
     * <p>
     * 搜索主手、副手和背包中的所有物品,
     * 根据治疗分数选择最佳的治疗物品。
     * </p>
     *
     * @param mind NPC的思维接口
     * @param entity NPC实体
     * @return 最佳治疗物品候选,如果没有找到返回null
     */
    private HealingCandidate findHealingItem(
        INpcMind mind,
        LivingEntity entity
    ) {
        if (!(entity instanceof Mob mob)) {
            return null;
        }
        HealingCandidate best = null;

        ItemStack main = mob.getMainHandItem();
        if (!main.isEmpty()) {
            double score = healingScore(main, mob);
            if (score > 0) {
                best = better(
                    best,
                    new HealingCandidate(main.copy(), MAIN_HAND_SLOT, score)
                );
            }
        }

        ItemStack off = mob.getOffhandItem();
        if (!off.isEmpty()) {
            double score = healingScore(off, mob);
            if (score > 0) {
                best = better(
                    best,
                    new HealingCandidate(off.copy(), OFF_HAND_SLOT, score)
                );
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
                best = better(
                    best,
                    new HealingCandidate(stack.copy(), i, score)
                );
            }
        }
        return best;
    }

    /**
     * 计算物品的治疗分数。
     * <p>
     * 根据物品类型和效果计算治疗价值:
     * - 药水:根据瞬间治疗和再生效果计算
     * - 金苹果:固定高分
     * - 食物:根据再生效果计算
     * </p>
     *
     * @param stack 物品堆
     * @param entity NPC实体
     * @return 治疗分数,分数越高表示治疗效果越好
     */
    private double healingScore(ItemStack stack, LivingEntity entity) {
        Item item = stack.getItem();
        if (
            item == Items.POTION ||
            item == Items.SPLASH_POTION ||
            item == Items.LINGERING_POTION
        ) {
            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            double score = 0;
            if (contents != null) {
                for (var effect : contents.getAllEffects()) {
                    if (
                        effect
                            .getEffect()
                            .equals(net.minecraft.world.effect.MobEffects.HEAL)
                    ) {
                        score +=
                            INSTANT_HEAL_BASE_SCORE +
                            effect.getAmplifier() *
                            INSTANT_HEAL_AMPLIFIER_BONUS;
                    }
                    if (
                        effect
                            .getEffect()
                            .equals(
                                net.minecraft.world.effect.MobEffects.REGENERATION
                            )
                    ) {
                        score +=
                            REGENERATION_BASE_SCORE +
                            effect.getAmplifier() *
                            REGENERATION_AMPLIFIER_BONUS +
                            effect.getDuration() / REGENERATION_DURATION_FACTOR;
                    }
                }
            } else {
                score = UNKNOWN_POTION_SCORE; // 兜底认为是正面治疗类药水
            }
            return score;
        }

        if (item == Items.GOLDEN_APPLE) {
            return GOLDEN_APPLE_SCORE;
        }
        if (item == Items.ENCHANTED_GOLDEN_APPLE) {
            return ENCHANTED_GOLDEN_APPLE_SCORE;
        }

        var food = stack.getFoodProperties(entity);
        if (food != null) {
            double score = 0;
            for (var pair : food.effects()) {
                if (
                    pair.effect() != null &&
                    pair
                        .effect()
                        .getEffect()
                        .equals(
                            net.minecraft.world.effect.MobEffects.REGENERATION
                        )
                ) {
                    score +=
                        FOOD_REGENERATION_BASE_SCORE +
                        pair.effect().getDuration() /
                        REGENERATION_DURATION_FACTOR;
                }
            }
            return score;
        }
        return 0;
    }

    /**
     * 准备使用治疗物品。
     * <p>
     * 保存当前手持物品状态,并将治疗物品移动到主手。
     * </p>
     *
     * @param candidate 选中的治疗物品候选
     * @param mind NPC的思维接口
     * @param entity NPC实体
     */
    private void prepareItem(
        HealingCandidate candidate,
        INpcMind mind,
        LivingEntity entity
    ) {
        previousMainHand = entity.getMainHandItem().copy();
        previousOffHand = entity.getOffhandItem().copy();
        sourceSlot = candidate.slot();
        sourceStack = candidate.stack();
        if (candidate.slot() >= 0) {
            ItemStack removed = mind
                .getInventory()
                .removeItem(candidate.slot());
            entity.setItemInHand(InteractionHand.MAIN_HAND, removed);
        } else if (candidate.slot() == OFF_HAND_SLOT) {
            entity.setItemInHand(
                InteractionHand.MAIN_HAND,
                entity.getOffhandItem()
            );
            entity.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        } else {
            // 主手已有合适物品
            entity.setItemInHand(
                InteractionHand.MAIN_HAND,
                entity.getMainHandItem()
            );
        }
    }

    /**
     * 使用物品后的清理工作。
     * <p>
     * 将主手中剩余的物品放回背包,并恢复之前的手持物品状态。
     * </p>
     *
     * @param mind NPC的思维接口
     * @param entity NPC实体
     */
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

    /**
     * 回滚物品状态。
     * <p>
     * 在使用失败或中断时,恢复所有物品到原始状态。
     * </p>
     *
     * @param mind NPC的思维接口
     * @param entity NPC实体
     */
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

    /**
     * 比较两个治疗物品候选,返回分数更高的一个。
     *
     * @param a 候选A
     * @param b 候选B
     * @return 分数更高的候选,如果其中一个为null则返回另一个
     */
    private HealingCandidate better(HealingCandidate a, HealingCandidate b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return b.score() > a.score() ? b : a;
    }

    /**
     * 治疗物品候选记录。
     *
     * @param stack 物品堆
     * @param slot 物品所在槽位(-1=主手, -2=副手, >=0=背包槽位)
     * @param score 治疗分数
     */
    private record HealingCandidate(ItemStack stack, int slot, double score) {}
}
