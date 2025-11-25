package com.Kizunad.customNPCs.ai.decision.goals;

import com.Kizunad.customNPCs.ai.decision.IGoal;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import net.minecraft.world.entity.LivingEntity;

/**
 * 生存目标 - 当血量低时优先级提升
 * <p>
 * 示例目标：演示如何根据 NPC 状态计算优先级
 */
public class SurvivalGoal implements IGoal {

    private static final float LOW_HEALTH_THRESHOLD = 0.3f; // 30% 血量以下认为是危险
    private static final float HEALTHY_THRESHOLD = 0.8f; // 80% 血量认为是安全
    private static final int SURVIVAL_MODE_MEMORY_DURATION = 200; // 10 秒（200 ticks）

    @Override
    public float getPriority(INpcMind mind, LivingEntity entity) {
        float healthPercentage = entity.getHealth() / entity.getMaxHealth();

        // 血量越低，优先级越高
        if (healthPercentage < LOW_HEALTH_THRESHOLD) {
            return 1.0f - healthPercentage; // 血量 10% -> 0.9 优先级
        }

        return 0.0f; // 血量充足，不需要生存目标
    }

    @Override
    public boolean canRun(INpcMind mind, LivingEntity entity) {
        return (
            entity.getHealth() < entity.getMaxHealth() * LOW_HEALTH_THRESHOLD
        );
    }

    @Override
    public void start(INpcMind mind, LivingEntity entity) {
        // 记录进入生存模式
        mind
            .getMemory()
            .rememberShortTerm(
                "in_survival_mode",
                true,
                SURVIVAL_MODE_MEMORY_DURATION
            );

        // FUTURE: 这里可以添加实际的生存行为，如：
        // - 寻找食物
        // - 寻找治疗物品
        // - 逃跑
        System.out.println(
            "[SurvivalGoal] NPC " +
                entity.getName().getString() +
                " 进入生存模式！血量: " +
                entity.getHealth()
        );
    }

    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // FUTURE: 执行生存行为
        // 简单演示：原地不动（实际应该寻找掩体或食物）
    }

    @Override
    public void stop(INpcMind mind, LivingEntity entity) {
        mind.getMemory().forget("in_survival_mode");
        System.out.println(
            "[SurvivalGoal] NPC " +
                entity.getName().getString() +
                " 退出生存模式"
        );
    }

    @Override
    public boolean isFinished(INpcMind mind, LivingEntity entity) {
        // 当血量恢复到安全水平时完成
        return entity.getHealth() >= entity.getMaxHealth() * HEALTHY_THRESHOLD;
    }

    @Override
    public String getName() {
        return "survival";
    }
}
