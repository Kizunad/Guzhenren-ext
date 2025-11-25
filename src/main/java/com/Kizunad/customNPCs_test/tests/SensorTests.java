package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.ai.sensors.VisionSensor;
import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.utils.NpcTestHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.monster.Zombie;

/**
 * 传感器系统测试逻辑
 */
public class SensorTests {

    private static final int SPAWN_OFFSET = 5;
    private static final int TICK_DELAY = 10;

    @GameTest(template = "customnpcs:empty_3x3x3")
    public static void testVisionSensor(GameTestHelper helper) {
        Zombie observer = com.Kizunad.customNPCs_test.utils.TestEntityFactory
            .createTestZombie(helper, new net.minecraft.core.BlockPos(1, 2, 1));
        Zombie target = com.Kizunad.customNPCs_test.utils.TestEntityFactory
            .createTestZombie(helper, new net.minecraft.core.BlockPos(3, 2, 3));
        
        INpcMind mind = NpcTestHelper.getMind(helper, observer);
        
        // 驱动 Mind
        NpcTestHelper.tickMind(helper, observer);
        
        // 验证
        NpcTestHelper.waitForCondition(helper, () -> {
            if (!mind.getMemory().hasMemory("visible_entities_count")) {
                return false;
            }
            int count = mind.getMemory().getShortTerm("visible_entities_count", Integer.class, -1);
            return count > 0;
        }, 100, "Vision sensor should detect entities");
    }

    @GameTest(template = "customnpcs:empty_3x3x3")
    public static void testVisionSensorNoEntities(GameTestHelper helper) {
        // 使用工厂创建观察者僵尸
        Zombie observer = com.Kizunad.customNPCs_test.utils.TestEntityFactory
            .createTestZombie(helper, new net.minecraft.core.BlockPos(2, 2, 2));
        INpcMind mind = NpcTestHelper.getMind(helper, observer);
        
        // 替换为短距离视觉传感器（避免看到其他测试中的实体）
        mind.getSensorManager().removeSensor("vision");
        mind.getSensorManager().registerSensor(new VisionSensor(2.0));
        
        // 驱动 Mind (这将触发传感器)
        NpcTestHelper.tickMind(helper, observer);
        
        // 验证：应该没有看到任何实体
        NpcTestHelper.waitForAssertion(helper, () -> {
            // 确保传感器已运行
            if (!mind.getMemory().hasMemory("visible_entities_count")) {
                throw new GameTestAssertException("Vision sensor memory not present");
            }
            
            int count = mind.getMemory().getShortTerm("visible_entities_count", Integer.class, -1);
            if (count != 0) {
                throw new GameTestAssertException("Expected 0 visible entities, but found " + count);
            }

            // 验证没有记录最近实体信息
            if (mind.getMemory().hasMemory("nearest_entity")) {
                throw new GameTestAssertException(
                    "Memory should not contain nearest_entity when no entities are visible");
            }
        }, "Vision sensor should detect 0 entities");
    }
}
