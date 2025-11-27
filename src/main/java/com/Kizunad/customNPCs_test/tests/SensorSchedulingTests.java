package com.Kizunad.customNPCs_test.tests;

import com.Kizunad.customNPCs.capabilities.mind.INpcMind;
import com.Kizunad.customNPCs_test.overrides.TestVisionSensor;
import com.Kizunad.customNPCs_test.utils.NpcTestHelper;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.monster.Zombie;

public class SensorSchedulingTests {

    public static void testDynamicScanInterval(GameTestHelper helper) {
        // 创建观察者
        Zombie observer = com.Kizunad.customNPCs_test.utils.TestEntityFactory
            .createTestZombie(helper, new net.minecraft.core.BlockPos(2, 2, 2));
        INpcMind mind = NpcTestHelper.getMind(helper, observer);

        // 只使用局部范围的 VisionSensor，避免读取其他测试实例
        mind.getSensorManager().removeSensor("vision");
        TestVisionSensor visionSensor = new TestVisionSensor(6.0);
        mind.getSensorManager().registerSensor(visionSensor);

        // 持续驱动思维
        NpcTestHelper.tickMind(helper, observer);

        helper.startSequence()
            .thenExecute(() -> {
                // 初始应为默认 10
                if (visionSensor.getScanInterval() != 10) {
                    throw new GameTestAssertException(
                        "Initial scan interval should be 10, but was " +
                        visionSensor.getScanInterval()
                    );
                }
            })
            // 等待空闲态（降频至 20）
            // 需要传感器至少运行一次以确认周围没有实体
            .thenWaitUntil(() -> {
                if (visionSensor.getScanInterval() != 20) {
                    throw new GameTestAssertException(
                        "Scan interval should be 20 (idle), but was " +
                        visionSensor.getScanInterval()
                    );
                }
            })
            .thenExecute(() -> {
                // 生成敌对实体
                com.Kizunad.customNPCs_test.utils.TestEntityFactory
                    .createTestZombie(helper, new net.minecraft.core.BlockPos(4, 2, 4));
            })
            // 等待威胁检测完成（升频至 2）。当前可能在低频睡眠，需要等待。
            .thenWaitUntil(() -> {
                if (visionSensor.getScanInterval() != 2) {
                    throw new GameTestAssertException(
                        "Scan interval should be 2 (threat), but was " +
                        visionSensor.getScanInterval()
                    );
                }
            })
            .thenSucceed();
    }
}
