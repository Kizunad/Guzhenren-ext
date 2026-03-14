package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingBlocks;
import com.Kizunad.guzhenrenext.xianqiao.farming.LightningAttractingFernBlock;
import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task19PD02LightningFernGameTests {

    private static final String TASK19_PD02_BATCH = "task19_p_d02_lightning_fern";
    private static final int TEST_TIMEOUT_TICKS = 120;
    private static final int ASSERT_DELAY_TICKS = 2;
    private static final int BLOCK_SET_UPDATE_FLAGS = 3;
    private static final BlockPos HAPPY_FERN_POS = new BlockPos(2, 2, 2);
    private static final BlockPos FAILURE_FERN_POS = new BlockPos(5, 2, 2);
    private static final double SCAN_RADIUS = 0.9D;
    private static final double SCAN_HEIGHT_MARGIN = 1.0D;
    private static final int PROOF_DROP_MIN_COUNT = 1;

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK19_PD02_BATCH
    )
    public void testTask19PD02HappyPathShouldTriggerLightningAndDropProofItem(GameTestHelper helper) {
        BlockPos fernPos = helper.absolutePos(HAPPY_FERN_POS);
        LightningAttractingFernBlock fernBlock = FarmingBlocks.LIGHTNING_ATTRACTING_FERN.get();

        // given: 搭建“下方可存活、上方无遮挡”的开放天空夹具，避免天气随机导致波动。
        helper.getLevel().setBlockAndUpdate(fernPos.below(), Blocks.DIRT.defaultBlockState());
        helper.getLevel().setBlock(fernPos.above(), Blocks.AIR.defaultBlockState(), BLOCK_SET_UPDATE_FLAGS);
        helper.getLevel().setBlockAndUpdate(fernPos, fernBlock.defaultBlockState());

        // when: 通过服务端显式入口触发一次引雷反应，形成确定性 happy path。
        boolean triggered = fernBlock.triggerLightningReactionForTest(helper.getLevel(), fernPos);

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            AABB scanArea = createScanArea(fernPos);
            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, scanArea);
            List<LightningBolt> lightningBolts = helper.getLevel().getEntitiesOfClass(LightningBolt.class, scanArea);

            // then: 必须命中触发，并产出雷萤砂证明物，同时方块发生可观察变异。
            helper.assertTrue(
                triggered,
                "happy path: 开放天空环境下，引雷草应允许服务端显式触发"
            );
            helper.assertTrue(
                helper.getLevel().getBlockState(fernPos).is(Blocks.DEAD_BUSH),
                "happy path: 引雷反应后应变异为 dead_bush，提供稳定可观察状态"
            );
            helper.assertTrue(
                !lightningBolts.isEmpty(),
                "happy path: 触发后应生成至少一个 LightningBolt，证明雷击效果已发生"
            );
            helper.assertTrue(
                countItems(drops, XianqiaoItems.LEI_YING_SHA.get()) >= PROOF_DROP_MIN_COUNT,
                "happy path: 触发后应掉落带命名反馈的雷萤砂（LEI_YING_SHA）"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK19_PD02_BATCH
    )
    public void testTask19PD02FailurePathShouldRejectBlockedSkyAndDropNothing(GameTestHelper helper) {
        BlockPos fernPos = helper.absolutePos(FAILURE_FERN_POS);
        LightningAttractingFernBlock fernBlock = FarmingBlocks.LIGHTNING_ATTRACTING_FERN.get();

        // given: 在引雷草正上方放置石头，构造“天空被遮挡”的非法环境门槛。
        helper.getLevel().setBlockAndUpdate(fernPos.below(), Blocks.DIRT.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(fernPos.above(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(fernPos, fernBlock.defaultBlockState());

        // when: 同样调用显式触发入口，验证门槛失配时必须拒绝触发。
        boolean triggered = fernBlock.triggerLightningReactionForTest(helper.getLevel(), fernPos);

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            AABB scanArea = createScanArea(fernPos);
            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, scanArea);
            List<LightningBolt> lightningBolts = helper.getLevel().getEntitiesOfClass(LightningBolt.class, scanArea);

            // then: 非法环境下不得命中触发，不得掉落雷萤砂，也不得出现雷击实体。
            helper.assertFalse(
                triggered,
                "failure path: 上方遮挡时，引雷草应拒绝触发，保持确定性失败"
            );
            helper.assertTrue(
                helper.getLevel().getBlockState(fernPos).is(fernBlock),
                "failure path: 门槛失配时不应发生变异，方块应保持为引雷草"
            );
            helper.assertTrue(
                lightningBolts.isEmpty(),
                "failure path: 上方遮挡时不应生成 LightningBolt"
            );
            helper.assertTrue(
                countItems(drops, XianqiaoItems.LEI_YING_SHA.get()) == 0,
                "failure path: 上方遮挡时不应产出 LEI_YING_SHA"
            );
            helper.succeed();
        });
    }

    private static int countItems(List<ItemEntity> drops, Item targetItem) {
        return drops.stream()
            .filter(entity -> entity.getItem().is(targetItem))
            .mapToInt(entity -> entity.getItem().getCount())
            .sum();
    }

    private static AABB createScanArea(BlockPos centerPos) {
        return new AABB(
            centerPos.getX() - SCAN_RADIUS,
            centerPos.getY() - SCAN_HEIGHT_MARGIN,
            centerPos.getZ() - SCAN_RADIUS,
            centerPos.getX() + 1 + SCAN_RADIUS,
            centerPos.getY() + 1 + SCAN_HEIGHT_MARGIN,
            centerPos.getZ() + 1 + SCAN_RADIUS
        );
    }
}
