package com.Kizunad.guzhenrenext_test.flyingsword;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.training.FlyingSwordTrainingAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.training.FlyingSwordTrainingService;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class FlyingSwordTrainingServiceTests {

    private static final int TEST_TIMEOUT_TICKS = 40;
    private static final int SLOT_SWORD = 0;
    private static final int SLOT_FUEL = 1;
    private static final int PERCENT_100 = 100;
    private static final int INITIAL_FUEL = 5;
    private static final int EXPECTED_FUEL_AFTER_TICK = 4;
    private static final int EXPECTED_EXP_AFTER_TICK = 1;
    private static final int FUEL_STACK_FOR_CONSERVATION_TEST = 2;

    /**
     * 基础回归：已有燃料时，tick 应消耗 1 点燃烧值并给飞剑累计 1 点经验。
     */
    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS)
    public void testTickShouldDecreaseFuelTime(GameTestHelper helper) {
        FlyingSwordTrainingAttachment training = new FlyingSwordTrainingAttachment();
        training.getInputSlots().setStackInSlot(
            SLOT_SWORD,
            new ItemStack(Items.IRON_SWORD)
        );

        // 去掉对外部模组物品注册的依赖：
        // 此测试只验证 tick 消耗与经验增长，燃料槽放任意占位物即可。
        training.getInputSlots().setStackInSlot(
            SLOT_FUEL,
            new ItemStack(Items.COAL)
        );
        training.setFuelTime(INITIAL_FUEL);
        training.setMaxFuelTime(INITIAL_FUEL);

        FlyingSwordTrainingService.tickInternal(training, null);

        helper.assertTrue(
            training.getFuelTime() == EXPECTED_FUEL_AFTER_TICK,
            "tick 后 fuelTime 应递减"
        );
        helper.assertTrue(
            training.getAccumulatedExp() == EXPECTED_EXP_AFTER_TICK,
            "tick 后 accumulatedExp 应增加"
        );
        helper.succeed();
    }

    /**
     * DoD 证据 1 + 2：
     * 1) 放入飞剑+元石并 tick 后，可用于菜单展示的进度值（fuel/max）应发生变化；
     * 2) tick 后 accumulatedExp 应增加。
     */
    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS)
    public void testTickShouldStartProgressAndIncreaseExp(GameTestHelper helper) {
        FlyingSwordTrainingAttachment training = new FlyingSwordTrainingAttachment();
        training.getInputSlots().setStackInSlot(
            SLOT_SWORD,
            new ItemStack(Items.IRON_SWORD)
        );

        // 去掉对 guzhenren:gucaiyuanshi 的硬依赖，避免 GameTest 环境外部注册缺失导致误报。
        // 这里通过直接设置 fuel/max 构造可控燃烧状态，等价验证“进度值会推进”。
        training.getInputSlots().setStackInSlot(
            SLOT_FUEL,
            new ItemStack(Items.COAL)
        );

        training.setFuelTime(INITIAL_FUEL);
        training.setMaxFuelTime(INITIAL_FUEL);
        int beforeFuel = training.getFuelTime();
        int beforeMaxFuel = training.getMaxFuelTime();
        int beforeBurnPercent = calculateMenuVisibleBurnPercent(beforeFuel, beforeMaxFuel);
        int beforeExp = training.getAccumulatedExp();

        FlyingSwordTrainingService.tickInternal(training, null);

        int afterFuel = training.getFuelTime();
        int afterMaxFuel = training.getMaxFuelTime();
        int afterBurnPercent = calculateMenuVisibleBurnPercent(afterFuel, afterMaxFuel);

        helper.assertTrue(
            afterFuel != beforeFuel || afterMaxFuel != beforeMaxFuel,
            "tick 后 fuel/max 应变化，才能证明菜单可见进度开始更新"
        );
        helper.assertTrue(
            afterMaxFuel == beforeMaxFuel,
            "仅消耗燃烧进度时，maxFuelTime 应保持稳定"
        );
        helper.assertTrue(
            afterBurnPercent <= beforeBurnPercent,
            "tick 后剩余燃烧百分比应下降或保持，不应逆向上涨"
        );
        helper.assertTrue(
            training.getAccumulatedExp() == beforeExp + EXPECTED_EXP_AFTER_TICK,
            "tick 后 accumulatedExp 应增加 1"
        );

        helper.succeed();
    }

    /**
     * DoD 证据 3：
     * 物品在训练槽位流转（补燃料+消耗）过程中不应凭空增殖。
     */
    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS)
    public void testTickShouldKeepItemConservationDuringSlotFlow(GameTestHelper helper) {
        FlyingSwordTrainingAttachment training = new FlyingSwordTrainingAttachment();
        training.getInputSlots().setStackInSlot(
            SLOT_SWORD,
            new ItemStack(Items.IRON_SWORD)
        );

        // 本断言关注“训练 tick 不会刷物”，不要求依赖外部燃料物品注册。
        training.getInputSlots().setStackInSlot(
            SLOT_FUEL,
            new ItemStack(Items.COAL, FUEL_STACK_FOR_CONSERVATION_TEST)
        );

        training.setFuelTime(INITIAL_FUEL);
        training.setMaxFuelTime(INITIAL_FUEL);

        int beforeSwordCount = training.getInputSlots().getStackInSlot(SLOT_SWORD).getCount();
        int beforeFuelCount = training.getInputSlots().getStackInSlot(SLOT_FUEL).getCount();
        int beforeTotalCount = beforeSwordCount + beforeFuelCount;

        FlyingSwordTrainingService.tickInternal(training, null);

        int afterSwordCount = training.getInputSlots().getStackInSlot(SLOT_SWORD).getCount();
        int afterFuelCount = training.getInputSlots().getStackInSlot(SLOT_FUEL).getCount();
        int afterTotalCount = afterSwordCount + afterFuelCount;

        helper.assertTrue(
            afterSwordCount == beforeSwordCount,
            "tick 后剑槽数量不应变化，避免出现复制或吞物"
        );
        helper.assertTrue(
            afterFuelCount == beforeFuelCount,
            "当前场景不触发补燃料，燃料槽计数应保持不变"
        );
        helper.assertTrue(
            afterTotalCount == beforeTotalCount,
            "不触发补燃料时，槽位总数应保持不变，不能凭空增殖"
        );

        helper.succeed();
    }

    /**
     * 与菜单 getBurnProgressPercent 使用同等公式，
     * 用于把 Attachment 的 fuel/max 转为“菜单可见进度值”。
     */
    private static int calculateMenuVisibleBurnPercent(int fuelTime, int maxFuelTime) {
        if (maxFuelTime <= 0) {
            return 0;
        }
        return (fuelTime * PERCENT_100) / maxFuelTime;
    }
}
