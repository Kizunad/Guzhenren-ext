package com.Kizunad.guzhenrenext_test.xianqiao;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task37FailureEdgeSmokeGameTests {

    private static final String PLAN2_TASK37_FAILURE_SMOKE_BATCH = "plan2_task37_failure_smoke";
    private static final int TEST_TIMEOUT_TICKS = 260;

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_TASK37_FAILURE_SMOKE_BATCH
    )
    public void testPlan2Task37FailureSmokeCreature(GameTestHelper helper) {
        new Task11BDeepCreaturesGameTests()
            .testTask11BDeepCreaturesFailurePathShouldNotTriggerWhenConditionsMissing(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_TASK37_FAILURE_SMOKE_BATCH
    )
    public void testPlan2Task37FailureSmokePlant(GameTestHelper helper) {
        new Task11ShallowPlantAggregateGameTests()
            .testPlan2PlantShallowInvalidEnvPs01QingYaGrass(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_TASK37_FAILURE_SMOKE_BATCH
    )
    public void testPlan2Task37FailureSmokePill(GameTestHelper helper) {
        new Task14ShallowPillRecipeGameTests()
            .testPlan2PillShallowInvalidRecipeShouldProduceNoOutput(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_TASK37_FAILURE_SMOKE_BATCH
    )
    public void testPlan2Task37FailureSmokeMaterial(GameTestHelper helper) {
        new Task24MaterialDeepBatch2GameTests()
            .testPlan2MaterialDeepBatch2Md06FailurePathShouldNotDropOrCollapseWhenPreconditionMissing(helper);
    }
}
