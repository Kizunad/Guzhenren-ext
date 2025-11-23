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

    @GameTest(template = EMPTY_TEMPLATE)
    public void testActionQueue(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.ActionTests.testActionQueue(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testActionExecutorIdle(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.ActionTests.testActionExecutorIdle(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testWaitAction(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.ActionTests.testWaitAction(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testMoveToAction(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.ActionTests.testMoveToAction(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testMoveToEntity(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.ActionTests.testMoveToEntity(helper);
    }

    // ==================== MoveToAction 扩展测试 ====================
    
    @GameTest(template = EMPTY_TEMPLATE)
    public void testMoveToCoordinate_NearDistance(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests.testMoveToCoordinate_NearDistance(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testMoveToAction_CustomAcceptableDistance(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests.testMoveToAction_CustomAcceptableDistance(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testMoveToAction_AlreadyInRange(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests.testMoveToAction_AlreadyInRange(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testMoveToAction_InvalidCoordinates_NaN(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests.testMoveToAction_InvalidCoordinates_NaN(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testMoveToAction_InvalidCoordinates_ExtremeY(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests.testMoveToAction_InvalidCoordinates_ExtremeY(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testMoveToAction_TargetEntityDespawned(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests.testMoveToAction_TargetEntityDespawned(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testMoveToAction_Timeout(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests.testMoveToAction_Timeout(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testMoveToAction_SamePosition(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests.testMoveToAction_SamePosition(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testMoveToAction_ExtremelyClose(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests.testMoveToAction_ExtremelyClose(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE)
    public void testMoveToAction_SequentialMoves(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests.testMoveToAction_SequentialMoves(helper);
    }
    
    // ==================== GOAP 规划器测试 ====================
    
    @GameTest(template = EMPTY_TEMPLATE)
    public void testWorldStateMatch(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.PlannerTests.testWorldStateMatch(helper);
    }
    
    @GameTest(template = EMPTY_TEMPLATE)
    public void testWorldStateApply(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.PlannerTests.testWorldStateApply(helper);
    }
    
    @GameTest(template = EMPTY_TEMPLATE)
    public void testSimplePlan(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.PlannerTests.testSimplePlan(helper);
    }
    
    @GameTest(template = EMPTY_TEMPLATE)
    public void testChainedPlan(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.PlannerTests.testChainedPlan(helper);
    }
    
    @GameTest(template = EMPTY_TEMPLATE)
    public void testImpossiblePlan(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.PlannerTests.testImpossiblePlan(helper);
    }
    
    @GameTest(template = EMPTY_TEMPLATE)
    public void testGoapIntegration(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.PlannerTests.testGoapIntegration(helper);
    }
    
    @GameTest(template = EMPTY_TEMPLATE)
    public void testRealApiGoapIntegration(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.PlannerTests.testRealApiGoapIntegration(helper);
    }
}

