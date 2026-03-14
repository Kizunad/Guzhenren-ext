package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingBlocks;
import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task19PD04CaveVinesGameTests {

    private static final String TASK19_PD04_BATCH = "task19_p_d04_cave_vines";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final int ASSERT_DELAY_TICKS = 2;
    private static final BlockPos SUCCESS_VINE_POS = new BlockPos(2, 2, 2);
    private static final BlockPos FAILURE_VINE_POS = new BlockPos(5, 2, 2);
    private static final double DROP_RADIUS = 0.75D;
    private static final double DROP_HEIGHT_MARGIN = 1.0D;
    private static final String EXPECTED_SUCCESS_NAME_TAG = "噬金藤·吞矿成精";

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK19_PD04_BATCH
    )
    public void testTask19PD04HappyPathShouldConsumeSingleAdjacentOreAndDropEssence(GameTestHelper helper) {
        BlockPos vinePos = helper.absolutePos(SUCCESS_VINE_POS);
        BlockPos oreAbove = vinePos.above();
        BlockPos oreBelow = vinePos.below();

        // given: 噬金藤上下都放置矿块，构造“可触发但应单次吞噬”的稳定夹具。
        helper.getLevel().setBlockAndUpdate(vinePos.above(2), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(oreAbove, Blocks.DIAMOND_ORE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(oreBelow, Blocks.IRON_ORE.defaultBlockState());

        // when: 在目标位置种植噬金藤。
        helper.getLevel().setBlockAndUpdate(vinePos, FarmingBlocks.CAVE_VINES.get().defaultBlockState());

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            BlockState vineState = helper.getLevel().getBlockState(vinePos);

            // then: 进入已吞噬状态 + 上方矿块被吞噬为石头 + 下方矿块保持原状（证明单次触发）。
            helper.assertTrue(
                vineState.hasProperty(CaveVines.BERRIES) && vineState.getValue(CaveVines.BERRIES),
                "happy path: 命中矿块后噬金藤应进入已吞噬状态（berries=true）"
            );
            helper.assertTrue(
                helper.getLevel().getBlockState(oreAbove).is(Blocks.STONE),
                "happy path: 噬金藤应吞噬上方矿块并转化为石头，体现可观察代价"
            );
            helper.assertTrue(
                helper.getLevel().getBlockState(oreBelow).is(Blocks.IRON_ORE),
                "happy path: 单次触发只应吞噬一个目标矿块，不应连带吞噬第二个矿块"
            );
            AABB dropArea = new AABB(
                vinePos.getX() - DROP_RADIUS,
                vinePos.getY() - DROP_HEIGHT_MARGIN,
                vinePos.getZ() - DROP_RADIUS,
                vinePos.getX() + 1 + DROP_RADIUS,
                vinePos.getY() + 1 + DROP_HEIGHT_MARGIN,
                vinePos.getZ() + 1 + DROP_RADIUS
            );
            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, dropArea);

            // then: 必须出现可观察结果（金髓屑 + 命名反馈）。
            helper.assertTrue(
                drops.stream().anyMatch(
                    entity -> entity.getItem().is(FarmingItems.JIN_SUI_XIE.get())
                        && entity.hasCustomName()
                        && EXPECTED_SUCCESS_NAME_TAG.equals(entity.getCustomName().getString())
                ),
                "happy path: 命中矿块后应产出带命名反馈的金髓屑，形成结果可观测路径"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK19_PD04_BATCH
    )
    public void testTask19PD04FailurePathShouldDoNothingWithoutOreAdjacency(GameTestHelper helper) {
        BlockPos vinePos = helper.absolutePos(FAILURE_VINE_POS);
        BlockPos abovePos = vinePos.above();
        BlockPos belowPos = vinePos.below();

        // given: 上下都不是矿块，构造 invalid_env。
        helper.getLevel().setBlockAndUpdate(vinePos.above(2), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(abovePos, Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(belowPos, Blocks.DIRT.defaultBlockState());

        // when: 放置噬金藤。
        helper.getLevel().setBlockAndUpdate(vinePos, FarmingBlocks.CAVE_VINES.get().defaultBlockState());

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            BlockState vineState = helper.getLevel().getBlockState(vinePos);

            // then: 不应触发吞噬，不应改变环境，不应产出结果物。
            helper.assertTrue(
                vineState.hasProperty(CaveVines.BERRIES) && !vineState.getValue(CaveVines.BERRIES),
                "failure path: 无矿块邻接时噬金藤不应进入已吞噬状态"
            );
            helper.assertTrue(
                helper.getLevel().getBlockState(abovePos).is(Blocks.STONE),
                "failure path: 无矿块邻接时上方方块不应被错误转化"
            );
            helper.assertTrue(
                helper.getLevel().getBlockState(belowPos).is(Blocks.DIRT),
                "failure path: 无矿块邻接时下方方块不应被错误转化"
            );
            AABB dropArea = new AABB(
                vinePos.getX() - DROP_RADIUS,
                vinePos.getY() - DROP_HEIGHT_MARGIN,
                vinePos.getZ() - DROP_RADIUS,
                vinePos.getX() + 1 + DROP_RADIUS,
                vinePos.getY() + 1 + DROP_HEIGHT_MARGIN,
                vinePos.getZ() + 1 + DROP_RADIUS
            );
            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, dropArea);
            helper.assertTrue(
                drops.stream().noneMatch(entity -> entity.getItem().is(FarmingItems.JIN_SUI_XIE.get())),
                "failure path: 无矿块邻接时不应产出金髓屑"
            );
            helper.succeed();
        });
    }
}
