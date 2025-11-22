package com.Kizunad.customNPCs_test;

import com.Kizunad.customNPCs_test.tests.GoalTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class NpcMindGameTests {

    private static final String EMPTY_TEMPLATE = "empty";

    @GameTest(template = EMPTY_TEMPLATE)
    public void testIdleGoal(GameTestHelper helper) {
        GoalTests.testIdleGoal(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testSurvivalGoal(GameTestHelper helper) {
        GoalTests.testSurvivalGoal(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testMemoryExpiration(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.MemoryTests.testMemoryExpiration(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testMemoryPersistence(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.MemoryTests.testMemoryPersistence(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testVisionSensor(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.SensorTests.testVisionSensor(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testVisionSensorNoEntities(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.SensorTests.testVisionSensorNoEntities(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testWatchClosestEntityGoal(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.GoalTests.testWatchClosestEntityGoal(helper);
    }
}
