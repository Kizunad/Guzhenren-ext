package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs.capabilities.mind.NpcMindAttachment;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;

/**
 * 传感器系统测试逻辑
 */
public class SensorTests {

    private static final int SPAWN_OFFSET = 5;
    private static final int TICK_DELAY = 10;

    public static void testVisionSensor(GameTestHelper helper) {
        // 生成观察者僵尸
        Zombie observer = helper.spawn(EntityType.ZOMBIE, 2, 2, 2);
        INpcMind mind = observer.getData(NpcMindAttachment.NPC_MIND);
        
        // 手动注册传感器
        mind.getSensorManager().registerSensor(new com.Kizunad.customNPCs.ai.sensors.VisionSensor());
        
        // 生成目标僵尸（在视野范围内）
        Zombie target = helper.spawn(EntityType.ZOMBIE, SPAWN_OFFSET, 2, 2);
        
        // 等待几个 tick 让传感器扫描
        helper.runAtTickTime(TICK_DELAY, () -> {
            // 验证记忆中存储了感知数据
            helper.assertTrue(mind.getMemory().hasMemory("visible_entities_count"), 
                "Memory should contain visible_entities_count");
            
            Object count = mind.getMemory().getMemory("visible_entities_count");
            helper.assertTrue(count instanceof Integer, 
                "visible_entities_count should be an Integer");
            
            int visibleCount = (Integer) count;
            helper.assertTrue(visibleCount > 0, 
                "Should see at least one entity, but saw " + visibleCount);
            
            // 验证最近实体信息
            helper.assertTrue(mind.getMemory().hasMemory("nearest_entity"), 
                "Memory should contain nearest_entity");
            helper.assertTrue(mind.getMemory().hasMemory("nearest_entity_type"), 
                "Memory should contain nearest_entity_type");
            helper.assertTrue(mind.getMemory().hasMemory("nearest_entity_distance"), 
                "Memory should contain nearest_entity_distance");
            
            // 验证最近实体是目标僵尸
            String nearestUUID = (String) mind.getMemory().getMemory("nearest_entity");
            helper.assertTrue(nearestUUID.equals(target.getUUID().toString()), 
                "Nearest entity should be the target zombie");
            
            helper.succeed();
        });
    }

    public static void testVisionSensorNoEntities(GameTestHelper helper) {
        // 生成一个孤立的僵尸（周围没有其他实体）
        Zombie observer = helper.spawn(EntityType.ZOMBIE, 2, 2, 2);
        INpcMind mind = observer.getData(NpcMindAttachment.NPC_MIND);
        
        // 等待几个 tick 让传感器扫描
        helper.runAtTickTime(TICK_DELAY, () -> {
            // 验证记忆中记录为 0 个可见实体
            if (mind.getMemory().hasMemory("visible_entities_count")) {
                Object count = mind.getMemory().getMemory("visible_entities_count");
                helper.assertTrue(count instanceof Integer, 
                    "visible_entities_count should be an Integer");
                
                int visibleCount = (Integer) count;
                helper.assertTrue(visibleCount == 0, 
                    "Should see no entities, but saw " + visibleCount);
            }
            
            // 验证没有记录最近实体信息
            helper.assertTrue(!mind.getMemory().hasMemory("nearest_entity"), 
                "Memory should not contain nearest_entity when no entities are visible");
            
            helper.succeed();
        });
    }
}
