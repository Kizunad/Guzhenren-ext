package com.Kizunad.guzhenrenext_test.xianqiao;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task34FourCategorySuccessGameTests {

    private static final String PLAN2_TASK34_SUCCESS_SMOKE_BATCH = "plan2_task34_success_smoke";
    private static final int TEST_TIMEOUT_TICKS = 260;

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_TASK34_SUCCESS_SMOKE_BATCH
    )
    public void testPlan2Task34SuccessSmokeCreature(GameTestHelper helper) {
        new Task11BDeepCreaturesGameTests()
            .testTask11BDeepCreaturesHappyPathShouldApplyMechanics(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_TASK34_SUCCESS_SMOKE_BATCH
    )
    public void testPlan2Task34SuccessSmokePlant(GameTestHelper helper) {
        new Task11AQingYaGrassGameTests()
            .testTask11AQingYaGrassHappyPathYieldsCropDrops(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_TASK34_SUCCESS_SMOKE_BATCH
    )
    public void testPlan2Task34SuccessSmokePill(GameTestHelper helper) {
        new Task13ShallowPillUseAggregateGameTests()
            .testPlan2PillShallowUseDs01XiaoHuanDan(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_TASK34_SUCCESS_SMOKE_BATCH
    )
    public void testPlan2Task34SuccessSmokeMaterial(GameTestHelper helper) {
        new Task23MaterialDeepBatch1GameTests()
            .testPlan2MaterialDeepBatch1Md01HappyPathShouldDropZhenQiaoXuanTieHe(helper);
    }
}
