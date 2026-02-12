package com.Kizunad.guzhenrenext_test.flyingsword;

import com.Kizunad.guzhenrenext.kongqiao.flyingsword.training.FlyingSwordTrainingAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.training.FlyingSwordTrainingService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class FlyingSwordTrainingServiceTests {

    private static final int TEST_TIMEOUT_TICKS = 40;
    private static final int SLOT_SWORD = 0;
    private static final int SLOT_FUEL = 1;
    private static final int INITIAL_FUEL = 5;
    private static final int EXPECTED_FUEL_AFTER_TICK = 4;
    private static final int EXPECTED_EXP_AFTER_TICK = 1;

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS)
    public void testTickShouldDecreaseFuelTime(GameTestHelper helper) {
        FlyingSwordTrainingAttachment training = new FlyingSwordTrainingAttachment();
        training.getInputSlots().setStackInSlot(
            SLOT_SWORD,
            new ItemStack(Items.IRON_SWORD)
        );

        Item primevalStoneItem = BuiltInRegistries.ITEM.get(
            ResourceLocation.fromNamespaceAndPath("guzhenren", "gucaiyuanshi")
        );
        helper.assertTrue(
            primevalStoneItem != Items.AIR,
            "测试前置失败：未找到 guzhenren:gucaiyuanshi"
        );
        training.getInputSlots().setStackInSlot(
            SLOT_FUEL,
            new ItemStack(primevalStoneItem)
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
}
