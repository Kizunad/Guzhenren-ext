package com.Kizunad.guzhenrenext_test.xianqiao;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task13ShallowPillUseAggregateGameTests {

    private static final String PLAN2_PILL_SHALLOW_USE_BATCH = "plan2.pill.shallow.use";
    private static final int TEST_TIMEOUT_TICKS = 200;

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs01XiaoHuanDan(GameTestHelper helper) {
        new Task13AXiaoHuanDanGameTests()
            .testTask13AXiaoHuanDanHappyPathShouldHealImmediately(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs02JuQiSan(GameTestHelper helper) {
        new Task13AJuQiSanGameTests()
            .testTask13AJuQiSanShouldRestoreZhenYuanAndConsumeOne(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs03CuiTiDan(GameTestHelper helper) {
        new Task13ACuiTiDanGameTests()
            .testTask13ACuiTiDanHappyPathShouldApplyDamageBoost(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs04JiFengDan(GameTestHelper helper) {
        new Task13AJiFengDanGameTests().testTask13AJiFengDanShouldApplyMovementSpeed(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs05TieGuDan(GameTestHelper helper) {
        new Task13ATieGuDanGameTests()
            .testTask13ATieGuDanHappyPathShouldApplyDamageResistance(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs06BiDuDan(GameTestHelper helper) {
        new Task13ABiDuDanGameTests()
            .testTask13ABiDuDanHappyPathShouldRemovePoison(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs07PoHuanDan(GameTestHelper helper) {
        new Task13APoHuanDanGameTests()
            .testTask13APoHuanDanHappyPathShouldClearBlindnessAndConfusion(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs08GuiXiDan(GameTestHelper helper) {
        new Task13AGuiXiDanGameTests()
            .testTask13AGuiXiDanHappyPathShouldApplyWaterBreathing(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs09BiHuoDan(GameTestHelper helper) {
        new Task13ABiHuoDanGameTests()
            .testTask13ABiHuoDanHappyPathShouldApplyFireResistance(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs10YeShiDan(GameTestHelper helper) {
        new Task13AYeShiDanGameTests()
            .testTask13AYeShiDanShouldApplyNightVisionAndConsumeOne(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs11BaoShiDan(GameTestHelper helper) {
        new Task13ABaoShiDanGameTests()
            .testTask13ABaoShiDanHappyPathShouldRestoreFoodAndConsumeItem(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs12QingShenDan(GameTestHelper helper) {
        new Task13AQingShenDanGameTests()
            .testTask13AQingShenDanHappyPathShouldApplySlowFalling(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs13YinXiDan(GameTestHelper helper) {
        new Task13AYinXiDanGameTests()
            .testTask13AYinXiDanHappyPathShouldApplyInvisibility(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs14KuangBaoDan(GameTestHelper helper) {
        new Task13AKuangBaoDanGameTests()
            .testTask13AKuangBaoDanHappyPathShouldApplyDamageBoost(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs15NingShenDan(GameTestHelper helper) {
        new Task13ANingShenDanGameTests()
            .testTask13ANingShenDanHappyPathShouldApplyLuck(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs16ShouLiangWan(GameTestHelper helper) {
        new Task13AShouLiangWanGameTests().testTask13AShouLiangWanHappyPathShouldSetNearbyAnimalInLoveAndConsumeOne(
            helper
        );
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs17LingZhiYe(GameTestHelper helper) {
        new Task13ALingZhiYeGameTests()
            .testTask13ALingZhiYeHappyPathShouldWriteGrowthAccelerationState(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs18BiGuDan(GameTestHelper helper) {
        new Task13ABiGuDanGameTests()
            .testTask13ABiGuDanHappyPathShouldRestoreFoodAndConsumeOneItem(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs19QuShouSan(GameTestHelper helper) {
        new Task13AQuShouSanGameTests()
            .testTask13AQuShouSanHappyPathShouldPushNearbyMonsterAndConsumeOne(helper);
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PILL_SHALLOW_USE_BATCH
    )
    public void testPlan2PillShallowUseDs20XunMaiDan(GameTestHelper helper) {
        new Task13AXunMaiDanGameTests()
            .testTask13AXunMaiDanHappyPathShouldApplyGlowingAndConsumeOne(helper);
    }
}
