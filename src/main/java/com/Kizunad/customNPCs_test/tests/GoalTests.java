package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;

/**
 * 目标系统测试逻辑
 */
public class GoalTests {

    public static void testIdleGoal(GameTestHelper helper) {
        // 生成一个僵尸
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, 2, 2, 2);
        
        // 验证它有 NpcMind
        helper.assertTrue(zombie.hasData(NpcMindAttachment.NPC_MIND), "Zombie should have NpcMind attachment");
        
        // 手动注册目标（GameTest 中不会触发 EntityJoinLevelEvent）
        INpcMind mind = zombie.getData(NpcMindAttachment.NPC_MIND);
        mind.getGoalSelector().registerGoal(new com.Kizunad.customNPCs.ai.decision.goals.IdleGoal());
        mind.getGoalSelector().registerGoal(new com.Kizunad.customNPCs.ai.decision.goals.SurvivalGoal());
        
        // 验证初始目标是 IdleGoal
        helper.succeedWhen(() -> {
            var currentGoal = mind.getGoalSelector().getCurrentGoal();
            
            helper.assertTrue(currentGoal != null, "Current goal should not be null");
            helper.assertTrue(currentGoal.getName().equals("idle"), 
                "Initial goal should be 'idle', but was '" + currentGoal.getName() + "'");
        });
    }

    public static void testSurvivalGoal(GameTestHelper helper) {
        // 生成一个僵尸
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, 2, 2, 2);
        
        // 手动注册目标
        INpcMind mind = zombie.getData(NpcMindAttachment.NPC_MIND);
        mind.getGoalSelector().registerGoal(new com.Kizunad.customNPCs.ai.decision.goals.IdleGoal());
        mind.getGoalSelector().registerGoal(new com.Kizunad.customNPCs.ai.decision.goals.SurvivalGoal());
        
        // 设置低血量 (1.0f < 30% of 20.0f)
        zombie.setHealth(1.0f);
        
        // 验证目标切换到 SurvivalGoal
        helper.succeedWhen(() -> {
            var currentGoal = mind.getGoalSelector().getCurrentGoal();
            
            helper.assertTrue(currentGoal != null, "Current goal should not be null");
            helper.assertTrue(currentGoal.getName().equals("survival"), 
                "Goal should switch to 'survival' when health is low, but was '" 
                + (currentGoal == null ? "null" : currentGoal.getName()) + "'");
        });
    }

    public static void testWatchClosestEntityGoal(GameTestHelper helper) {
        // 生成观察者
        Zombie observer = helper.spawn(EntityType.ZOMBIE, 2, 2, 2);
        INpcMind mind = observer.getData(NpcMindAttachment.NPC_MIND);
        
        // 手动注册组件
        mind.getSensorManager().registerSensor(new com.Kizunad.customNPCs.ai.sensors.VisionSensor());
        mind.getGoalSelector().registerGoal(new com.Kizunad.customNPCs.ai.decision.goals.IdleGoal());
        mind.getGoalSelector().registerGoal(new com.Kizunad.customNPCs.ai.decision.goals.WatchClosestEntityGoal());
        
        // 生成目标（在视野内）
        helper.spawn(EntityType.ZOMBIE, 5, 2, 2);
        
        // 等待传感器扫描并触发目标切换 (增加等待时间到 20 ticks)
        helper.runAtTickTime(20, () -> {
            // 调试：检查记忆是否已更新
            helper.assertTrue(mind.getMemory().hasMemory("nearest_entity"), 
                "Memory should contain 'nearest_entity' before goal switch");
                
            var currentGoal = mind.getGoalSelector().getCurrentGoal();
            
            helper.assertTrue(currentGoal != null, "Current goal should not be null");
            helper.assertTrue(currentGoal.getName().equals("watch_closest_entity"), 
                "Goal should switch to 'watch_closest_entity' when entity is visible, but was '" 
                + currentGoal.getName() + "'");
            
            helper.succeed();
        });
    }
}
