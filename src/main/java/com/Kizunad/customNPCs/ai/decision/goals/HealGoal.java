package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.actions.ActionStatus;
import com.Kizunad.customNPCs.ai.actions.common.UseItemAction;
import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 回复Goal - 当血量低时，寻找并使用食物/药水恢复
 * <p>
 * 使用标准动作: {@link UseItemAction}
 * <p>
 * 优先级: 血量越低，优先级越高
 * 触发条件: 血量 < 50% 且背包中有食物
 */
public class HealGoal implements IGoal {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealGoal.class);

    private static final float HEAL_THRESHOLD = 0.5f; // 50%血量触发
    private static final float HEALTHY_THRESHOLD = 0.8f; // 80%血量认为已恢复
    private static final int HEALING_MEMORY_DURATION = 200; // 10秒

    private UseItemAction currentAction = null;

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        float healthPercentage = entity.getHealth() / entity.getMaxHealth();

        // 血量越低，优先级越高
        if (healthPercentage < HEAL_THRESHOLD) {
            // 有食物才有优先级
            if (findFoodInInventory(entity) != null) {
                return 1.0f - healthPercentage; // 血量10% -> 0.9优先级
            }
        }

        return 0.0f;
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        // 血量低且有食物
        return entity.getHealth() < entity.getMaxHealth() * HEAL_THRESHOLD
            && findFoodInInventory(entity) != null;
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
        // 如果动作未创建或已完成，创建新动作
        if (currentAction == null) {
            Item food = findFoodInInventory(entity);
            if (food != null) {
                currentAction = new UseItemAction(food);
                mind.getActionExecutor().addAction(currentAction);
                LOGGER.debug("[HealGoal] 开始使用食物: {}", food.getDescriptionId());
            } else {
                LOGGER.warn("[HealGoal] 找不到食物，无法治疗");
            }
        }

        // 检查动作状态
        if (currentAction != null) {
            ActionStatus status = currentAction.tick(mind, entity);
            if (status == ActionStatus.SUCCESS) {
                LOGGER.info("[HealGoal] 食物使用成功");
                currentAction = null; // 重置，准备下一次
            } else if (status == ActionStatus.FAILURE) {
                LOGGER.warn("[HealGoal] 食物使用失败");
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
        // 血量恢复到安全水平或没有食物时完成
        boolean healthRestored = entity.getHealth() >= entity.getMaxHealth() * HEALTHY_THRESHOLD;
        boolean noFood = findFoodInInventory(entity) == null;

        return healthRestored || noFood;
    }

    @Override
    public String getName() {
        return "heal";
    }

    /**
     * 在实体的背包中寻找食物
     * @param entity 实体
     * @return 食物物品，如果找不到返回null
     */
    private Item findFoodInInventory(LivingEntity entity) {
        if (!(entity instanceof Mob mob)) {
            return null;
        }

        // 检查主手和副手
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = mob.getItemInHand(hand);
            if (!stack.isEmpty() && stack.getFoodProperties(mob) != null) {
                return stack.getItem();
            }
        }

        return null;
    }
}
