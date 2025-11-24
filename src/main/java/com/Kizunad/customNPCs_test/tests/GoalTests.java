package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;

/**
 * 目标系统测试逻辑
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class GoalTests {

    public static void testIdleGoal(GameTestHelper helper) {
        // 使用工厂创建僵尸
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        
        // 验证它有 NpcMind
        helper.assertTrue(zombie.hasData(NpcMindAttachment.NPC_MIND), "Zombie should have NpcMind attachment");
        
        // 手动注册目标
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        mind.getGoalSelector().registerGoal(new com.Kizunad.customNPCs.ai.decision.goals.IdleGoal());
        mind.getGoalSelector().registerGoal(new com.Kizunad.customNPCs.ai.decision.goals.SurvivalGoal());
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        // 验证初始目标是 IdleGoal
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> {
                var currentGoal = mind.getGoalSelector().getCurrentGoal();
                return currentGoal != null && currentGoal.getName().equals("idle");
            },
            20,
            "Initial goal should be 'idle'"
        );
    }

    public static void testSurvivalGoal(GameTestHelper helper) {
        // 使用工厂创建僵尸
        Zombie zombie = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createSimpleTestNPC(helper, new net.minecraft.core.BlockPos(2, 2, 2), EntityType.ZOMBIE);
        
        // 手动注册目标
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, zombie);
        mind.getGoalSelector().registerGoal(new com.Kizunad.customNPCs.ai.decision.goals.IdleGoal());
        mind.getGoalSelector().registerGoal(new com.Kizunad.customNPCs.ai.decision.goals.SurvivalGoal());
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, zombie);
        
        // 设置低血量 (1.0f < 30% of 20.0f)
        zombie.setHealth(1.0f);
        
        // 验证目标切换到 SurvivalGoal
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> {
                var currentGoal = mind.getGoalSelector().getCurrentGoal();
                return currentGoal != null && currentGoal.getName().equals("survival");
            },
            20,
            "Goal should switch to 'survival' when health is low"
        );
    }

    public static void testWatchClosestEntityGoal(GameTestHelper helper) {
        // 使用工厂创建观察者（带视觉传感器）
        Zombie observer = com.Kizunad.customNPCs_test.utils.TestEntityFactory.createTestZombie(helper, new net.minecraft.core.BlockPos(2, 2, 2));
        INpcMind mind = com.Kizunad.customNPCs_test.utils.NpcTestHelper.getMind(helper, observer);
        
        // 手动注册目标
        mind.getGoalSelector().registerGoal(new com.Kizunad.customNPCs.ai.decision.goals.IdleGoal());
        mind.getGoalSelector().registerGoal(new com.Kizunad.customNPCs.ai.decision.goals.WatchClosestEntityGoal());
        
        // 启动 Mind tick
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.tickMind(helper, observer);
        
        // 生成目标（在视野内）
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.spawnTaggedEntity(
            helper,
            EntityType.ZOMBIE,
            new net.minecraft.core.BlockPos(5, 2, 2));
        
        // 等待传感器扫描并触发目标切换
        com.Kizunad.customNPCs_test.utils.NpcTestHelper.waitForCondition(
            helper,
            () -> {
                // 调试：检查记忆是否已更新
                if (!mind.getMemory().hasMemory("nearest_entity")) {
                    return false;
                }
                
                var currentGoal = mind.getGoalSelector().getCurrentGoal();
                return currentGoal != null && currentGoal.getName().equals("watch_closest_entity");
            },
            40, // 增加等待时间
            "Goal should switch to 'watch_closest_entity' when entity is visible"
        );
    }
}
