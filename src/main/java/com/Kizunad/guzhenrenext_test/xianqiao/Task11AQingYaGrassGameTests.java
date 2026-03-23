package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingBlocks;
import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task11AQingYaGrassGameTests {

    private static final String TASK11_QING_YA_GRASS_BATCH = "task11_qing_ya_grass";
    private static final int TEST_TIMEOUT_TICKS = 160;
    private static final int MAX_AGE = 7;
    private static final BlockPos HAPPY_CROP_POSITION = new BlockPos(2, 1, 2);
    private static final BlockPos FAILURE_CROP_POSITION = new BlockPos(4, 1, 2);

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK11_QING_YA_GRASS_BATCH
    )
    public void testTask11AQingYaGrassHappyPathYieldsCropDrops(GameTestHelper helper) {
        BlockPos cropPos = helper.absolutePos(HAPPY_CROP_POSITION);
        helper.getLevel().setBlockAndUpdate(cropPos.below(), Blocks.FARMLAND.defaultBlockState());
        CropBlock crop = FarmingBlocks.QING_YA_GRASS.get();
        BlockState matureState = crop.defaultBlockState().setValue(CropBlock.AGE, MAX_AGE);
        helper.getLevel().setBlockAndUpdate(cropPos, matureState);
        BlockState state = helper.getLevel().getBlockState(cropPos);
        BlockEntity blockEntity = state.hasBlockEntity() ? helper.getLevel().getBlockEntity(cropPos) : null;
        List<ItemStack> generatedDrops = Block.getDrops(state, helper.getLevel(), cropPos, blockEntity);
        Block.dropResources(state, helper.getLevel(), cropPos, blockEntity, null, ItemStack.EMPTY);
        helper.getLevel().destroyBlock(cropPos, false);
        helper.assertTrue(
            generatedDrops.stream().anyMatch(stack -> stack.is(FarmingItems.QING_YA_GRASS_ITEM.get())),
            "happy path: 成熟青亚草破坏后应掉落 guzhenrenext:qing_ya_grass, 实际掉落=" + generatedDrops
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK11_QING_YA_GRASS_BATCH
    )
    public void testTask11AQingYaGrassFailurePathRequiresFarmland(GameTestHelper helper) {
        BlockPos failurePos = helper.absolutePos(FAILURE_CROP_POSITION);
        helper.getLevel().setBlockAndUpdate(failurePos.below(), Blocks.STONE.defaultBlockState());
        BlockState qingYaState = FarmingBlocks.QING_YA_GRASS.get().defaultBlockState();
        helper.assertFalse(
            qingYaState.canSurvive(helper.getLevel(), failurePos),
            "failure path: 非耕地环境不应允许青亚草稳定成立"
        );
        helper.succeed();
    }
}
