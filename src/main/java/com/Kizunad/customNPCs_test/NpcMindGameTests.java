package com.Kizunad.customNPCs_test;

import com.Kizunad.customNPCs_test.utils.TestBatches;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class NpcMindGameTests {

    private static final String EMPTY_TEMPLATE = "empty";
    private static final String BATCH_BASE = TestBatches.BASE;
    private static final String BATCH_GOAP = TestBatches.GOAP;
    private static final String BATCH_REAL_API = TestBatches.REAL_API;
    private static final String BATCH_PERF = TestBatches.PERFORMANCE;
    private static final String BATCH_LLM = TestBatches.REAL_API;

    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP, timeoutTicks = 260)
    public void testCookGoalProducesCookedFood(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.goals.CookGoalTests.testCookGoalProducesCookedFood(
            helper
        );
    }

    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP, timeoutTicks = 220)
    public void testCraftItemGoalManualPlan(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.goals.CraftItemGoalTests.testCraftItemGoalManualPlan(
            helper
        );
    }

    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP, timeoutTicks = 240)
    public void testHuntGoalAttacksWeakerEntities(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.goals.HuntGoalTests.testHuntGoalAttacksWeakerEntities(
            helper
        );
    }

    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP)
    public void testRangedAttackPostFire(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.RangedAttackFixTests.testRangedAttackPostFire(helper);
    }

    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP)
    public void testRangedAttackStuckDetection(GameTestHelper helper) {
        com.Kizunad.customNPCs_test.tests.RangedAttackFixTests.testRangedAttackStuckDetection(helper);
    }


    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testIdleGoal(GameTestHelper helper) {
    //        GoalTests.testIdleGoal(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testSurvivalGoal(GameTestHelper helper) {
    //        GoalTests.testSurvivalGoal(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testMemoryExpiration(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.MemoryTests.testMemoryExpiration(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testMemoryPersistence(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.MemoryTests.testMemoryPersistence(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testVisionSensor(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.SensorTests.testVisionSensor(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testVisionSensorNoEntities(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.SensorTests.testVisionSensorNoEntities(helper);
    //    }

    // @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP, required = false)
    // public void testDefendGoalClearsThreatMemory(GameTestHelper helper) {
    //     com.Kizunad.customNPCs_test.tests.ThreatResponseTests.testDefendGoalClearsThreatMemory(
    //         helper
    //     );
    // }

    // @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP, required = false)
    // public void testFleeGoalClearsThreatMemory(GameTestHelper helper) {
    //     com.Kizunad.customNPCs_test.tests.ThreatResponseTests.testFleeGoalClearsThreatMemory(
    //         helper
    //     );
    // }

    // @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP, required = false)
    // public void testDefendGoalPrefersBlockClose(GameTestHelper helper) {
    //     com.Kizunad.customNPCs_test.tests.ThreatResponseTests.testDefendGoalPrefersBlockClose(
    //         helper
    //     );
    // }

    // @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP, required = false)
    // public void testDefendGoalPrefersRanged(GameTestHelper helper) {
    //     com.Kizunad.customNPCs_test.tests.ThreatResponseTests.testDefendGoalPrefersRanged(
    //         helper
    //     );
    // }

    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testDynamicScanInterval(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.SensorSchedulingTests.testDynamicScanInterval(helper);
    //    }

    //    @GameTest(template = "empty", batch = BATCH_BASE)
    //    public void testSafetySensorDetectsHazard(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.SafetyGuardrailTests.testSafetySensorDetectsHazard(
    //            helper
    //        );
    //    }

    //    @GameTest(template = "empty", batch = BATCH_BASE, required = false)
    //    public void testVisionSensorFriendFoe(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.SafetyGuardrailTests.testVisionSensorFriendFoe(
    //            helper
    //        );
    //    }

    // @GameTest(template = EMPTY_TEMPLATE, timeoutTicks = 120, batch = BATCH_BASE)
    // public void testMoveToBlockedPathFails(GameTestHelper helper) {
    //     com.Kizunad.customNPCs_test.tests.RobustnessTests.testMoveToBlockedPathFails(
    //         helper
    //     );
    // }

    // @GameTest(template = EMPTY_TEMPLATE, timeoutTicks = 120, batch = BATCH_BASE)
    // public void testMoveToTargetDespawnFails(GameTestHelper helper) {
    //     com.Kizunad.customNPCs_test.tests.RobustnessTests.testMoveToTargetDespawnFails(
    //         helper
    //     );
    // }

    // @GameTest(template = EMPTY_TEMPLATE, timeoutTicks = 120, batch = BATCH_BASE)
    // public void testMoveToTimeoutFails(GameTestHelper helper) {
    //     com.Kizunad.customNPCs_test.tests.RobustnessTests.testMoveToTimeoutFails(
    //         helper
    //     );
    // }

    // @GameTest(template = EMPTY_TEMPLATE, timeoutTicks = 120, batch = BATCH_PERF)
    // public void testPerformanceStress(GameTestHelper helper) {
    //     com.Kizunad.customNPCs_test.tests.ComplexScenarios.testPerformanceStress(
    //         helper
    //     );
    // }

    // @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP, timeoutTicks = 200)
    // public void testEquipArmorChoosesBest(GameTestHelper helper) {
    //     com.Kizunad.customNPCs_test.actions.ArmorActionsTests.testEquipArmorChoosesBest(
    //         helper
    //     );
    // }

    // @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP, timeoutTicks = 200)
    // public void testSatiateUsesLowerValueFood(GameTestHelper helper) {
    //     com.Kizunad.customNPCs_test.actions.HungerActionsTests.testSatiateUsesLowerValueFood(
    //         helper
    //     );
    // }

    // @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP, timeoutTicks = 200)
    // public void testSatiateSkipsWhenInDanger(GameTestHelper helper) {
    //     com.Kizunad.customNPCs_test.actions.HungerActionsTests.testSatiateSkipsWhenInDanger(
    //         helper
    //     );
    // }

    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testWatchClosestEntityGoal(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.GoalTests.testWatchClosestEntityGoal(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testActionQueue(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.ActionTests.testActionQueue(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testActionExecutorIdle(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.ActionTests.testActionExecutorIdle(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testWaitAction(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.ActionTests.testWaitAction(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testMoveToAction(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.ActionTests.testMoveToAction(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testMoveToEntity(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.ActionTests.testMoveToEntity(helper);
    //    }
    //
    //    // ==================== MoveToAction 扩展测试 ====================
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testMoveToCoordinateNearDistance(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests
    //            .testMoveToCoordinateNearDistance(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testMoveToActionCustomAcceptableDistance(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests
    //            .testMoveToActionCustomAcceptableDistance(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testMoveToActionAlreadyInRange(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests
    //            .testMoveToActionAlreadyInRange(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testMoveToActionInvalidCoordinatesNaN(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests
    //            .testMoveToActionInvalidCoordinatesNaN(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testMoveToActionInvalidCoordinatesExtremeY(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests
    //            .testMoveToActionInvalidCoordinatesExtremeY(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testMoveToActionTargetEntityDespawned(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests
    //            .testMoveToActionTargetEntityDespawned(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testMoveToActionTimeout(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests
    //            .testMoveToActionTimeout(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testMoveToActionSamePosition(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests
    //            .testMoveToActionSamePosition(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testMoveToActionExtremelyClose(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests
    //            .testMoveToActionExtremelyClose(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testMoveToActionSequentialMoves(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.MoveToActionExtendedTests
    //            .testMoveToActionSequentialMoves(helper);
    //    }
    //
    //    // ==================== GOAP 规划器测试 ====================
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP)
    //    public void testWorldStateMatch(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.PlannerTests.testWorldStateMatch(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP)
    //    public void testWorldStateApply(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.PlannerTests.testWorldStateApply(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP)
    //    public void testSimplePlan(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.PlannerTests.testSimplePlan(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP)
    //    public void testChainedPlan(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.PlannerTests.testChainedPlan(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_GOAP)
    //    public void testImpossiblePlan(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.PlannerTests.testImpossiblePlan(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, timeoutTicks = 300, batch = BATCH_GOAP)
    //    public void testGoapIntegration(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.PlannerTests.testGoapIntegration(helper);
    //    }
    //
    //    @GameTest(template = EMPTY_TEMPLATE, timeoutTicks = 400, batch = BATCH_REAL_API)
    //    public void testRealApiGoapIntegration(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.PlannerTests.testRealApiGoapIntegration(helper);
    //    }
    //
    //    // ==================== 性格系统测试 ====================
    //@GameTest(
    //template = EMPTY_TEMPLATE,
    //batch = BATCH_BASE,
    //timeoutTicks = 120,
    //required = false
    //)
    //public static void testMoveToFlat(GameTestHelper helper) {
    //com.Kizunad.customNPCs_test.tests.MoveToTuningTests.testMoveToFlat(
    //helper
    //);
    //}

    //@GameTest(
    //template = EMPTY_TEMPLATE,
    //batch = BATCH_BASE,
    //timeoutTicks = 200,
    //required = false
    //)
    //public static void testMoveToWithBlocks(GameTestHelper helper) {
    //com.Kizunad.customNPCs_test.tests.MoveToTuningTests.testMoveToWithBlocks(
    //helper
    //);
    //}

    //@GameTest(
    //template = EMPTY_TEMPLATE,
    //batch = BATCH_BASE,
    //timeoutTicks = 120,
    //required = false
    //)
    //public static void testMoveToFlat(GameTestHelper helper) {
    //com.Kizunad.customNPCs_test.tests.MoveToTuningTests.testMoveToFlat(
    //helper
    //);
    //}

    //@GameTest(
    //template = EMPTY_TEMPLATE,
    //batch = BATCH_BASE,
    //timeoutTicks = 200,
    //required = false
    //)
    //public static void testMoveToWithBlocks(GameTestHelper helper) {
    //com.Kizunad.customNPCs_test.tests.MoveToTuningTests.testMoveToWithBlocks(
    //helper
    //);
    //}

    // // ==================== MoveToAction 调参回归 ====================

    // @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE, timeoutTicks = 120, required = false)
    // public static void testMoveToFlat(GameTestHelper helper) {
    //     com.Kizunad.customNPCs_test.tests.MoveToTuningTests.testMoveToFlat(helper);
    // }

    // @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE, timeoutTicks = 200, required = false)
    // public static void testMoveToWithBlocks(GameTestHelper helper) {
    //     com.Kizunad.customNPCs_test.tests.MoveToTuningTests.testMoveToWithBlocks(
    //         helper
    //     );
    // }
    //    @GameTest(template = EMPTY_TEMPLATE, batch = BATCH_BASE)
    //    public void testPersonalityDrivenDecision(GameTestHelper helper) {
    //        com.Kizunad.customNPCs_test.tests.PersonalityTests.testPersonalityDrivenDecision(helper);
    //    }
}
