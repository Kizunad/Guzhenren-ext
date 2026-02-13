package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.service.ApertureRegionCopier;
import com.Kizunad.guzhenrenext.xianqiao.service.OverworldTerrainSampler;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class TerrainSamplingGameTests {

    private static final int TEST_TIMEOUT_TICKS = 80;
    private static final int CHUNK_SIZE = 16;
    private static final int SAMPLE_BELOW_SURFACE = 4;
    private static final int SOURCE_ANCHOR_RELATIVE_X = 16;
    private static final int SOURCE_ANCHOR_RELATIVE_Y = 6;
    private static final int SOURCE_ANCHOR_RELATIVE_Z = 16;
    private static final int TARGET_ANCHOR_RELATIVE_X = 48;
    private static final int TARGET_ANCHOR_RELATIVE_Y = 6;
    private static final int TARGET_ANCHOR_RELATIVE_Z = 48;
    private static final int BLOCK_SET_FLAGS = Block.UPDATE_CLIENTS;
    private static final int RANDOM_SEED = 20260213;
    private static final int SUPPORT_LAYER_OFFSET = 1;
    private static final int BOTTOM_LAYER_Y_OFFSET = 0;
    private static final int NON_BOTTOM_LAYER_Y_OFFSET = 1;
    private static final int OFFSET_ONE = 1;
    private static final int OFFSET_TWO = 2;
    private static final int OFFSET_THREE = 3;
    private static final int OFFSET_FOUR = 4;
    private static final int OFFSET_FIVE = 5;
    private static final int CHEST_SOURCE_RELATIVE_X = 6;
    private static final int CHEST_SOURCE_RELATIVE_Y = 4;
    private static final int CHEST_SOURCE_RELATIVE_Z = 6;
    private static final int CHEST_TARGET_RELATIVE_X = 10;
    private static final int CHEST_TARGET_RELATIVE_Y = 4;
    private static final int CHEST_TARGET_RELATIVE_Z = 10;
    private static final int CHEST_SLOT_INDEX = 0;
    private static final int DIAMOND_COUNT = 1;

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS)
    public void testOverworldSamplerShouldReplaceBottomFluidAndFallingBlocks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos sourceAnchor = helper.absolutePos(
            new BlockPos(SOURCE_ANCHOR_RELATIVE_X, SOURCE_ANCHOR_RELATIVE_Y, SOURCE_ANCHOR_RELATIVE_Z)
        );
        BlockPos targetAnchor = helper.absolutePos(
            new BlockPos(TARGET_ANCHOR_RELATIVE_X, TARGET_ANCHOR_RELATIVE_Y, TARGET_ANCHOR_RELATIVE_Z)
        );

        int sourceMinX = alignToChunkMin(sourceAnchor.getX());
        int sourceMinZ = alignToChunkMin(sourceAnchor.getZ());
        int centerX = sourceMinX + CHUNK_SIZE / 2;
        int centerZ = sourceMinZ + CHUNK_SIZE / 2;
        int surfaceY = sourceAnchor.getY() + SAMPLE_BELOW_SURFACE;
        int sourceMinY = surfaceY - SAMPLE_BELOW_SURFACE;

        // given: 固定高度基准，确保采样底层可复现。
        level.setBlock(new BlockPos(centerX, surfaceY, centerZ), Blocks.STONE.defaultBlockState(), BLOCK_SET_FLAGS);

        // given: 底层样本（流体/重力/普通方块）。
        BlockPos sourceBottomFluidPos = new BlockPos(sourceMinX + OFFSET_ONE, sourceMinY, sourceMinZ + OFFSET_ONE);
        BlockPos sourceBottomFallingPos = new BlockPos(sourceMinX + OFFSET_TWO, sourceMinY, sourceMinZ + OFFSET_TWO);
        BlockPos sourceBottomNormalPos = new BlockPos(sourceMinX + OFFSET_THREE, sourceMinY, sourceMinZ + OFFSET_THREE);

        // given: 非底层样本，验证“非底层保留原状”。
        BlockPos sourceUpperFluidPos = new BlockPos(
            sourceMinX + OFFSET_FOUR,
            sourceMinY + NON_BOTTOM_LAYER_Y_OFFSET,
            sourceMinZ + OFFSET_FOUR
        );
        BlockPos sourceUpperFallingPos = new BlockPos(
            sourceMinX + OFFSET_FIVE,
            sourceMinY + NON_BOTTOM_LAYER_Y_OFFSET,
            sourceMinZ + OFFSET_FIVE
        );

        // given: 为砂子提供支撑，避免下落引入不稳定。
        level.setBlock(
            sourceBottomFallingPos.below(SUPPORT_LAYER_OFFSET),
            Blocks.STONE.defaultBlockState(),
            BLOCK_SET_FLAGS
        );
        level.setBlock(
            sourceUpperFallingPos.below(SUPPORT_LAYER_OFFSET),
            Blocks.STONE.defaultBlockState(),
            BLOCK_SET_FLAGS
        );

        level.setBlock(sourceBottomFluidPos, Blocks.WATER.defaultBlockState(), BLOCK_SET_FLAGS);
        level.setBlock(sourceBottomFallingPos, Blocks.SAND.defaultBlockState(), BLOCK_SET_FLAGS);
        level.setBlock(sourceBottomNormalPos, Blocks.DIRT.defaultBlockState(), BLOCK_SET_FLAGS);
        level.setBlock(sourceUpperFluidPos, Blocks.WATER.defaultBlockState(), BLOCK_SET_FLAGS);
        level.setBlock(sourceUpperFallingPos, Blocks.SAND.defaultBlockState(), BLOCK_SET_FLAGS);

        // when: 调用粗采样并写入目标锚点。
        boolean result = OverworldTerrainSampler.sampleAndPlace(
            level,
            level,
            targetAnchor,
            level.getBiome(new BlockPos(centerX, surfaceY, centerZ)),
            sourceAnchor,
            RandomSource.create(RANDOM_SEED)
        );

        helper.assertTrue(result, "显式源锚点粗采样应成功");

        // then: 按“源相对偏移 -> 目标锚点”检查替换与保留行为。
        BlockPos targetBottomFluidPos = targetAnchor.offset(OFFSET_ONE, BOTTOM_LAYER_Y_OFFSET, OFFSET_ONE);
        BlockPos targetBottomFallingPos = targetAnchor.offset(OFFSET_TWO, BOTTOM_LAYER_Y_OFFSET, OFFSET_TWO);
        BlockPos targetBottomNormalPos = targetAnchor.offset(OFFSET_THREE, BOTTOM_LAYER_Y_OFFSET, OFFSET_THREE);
        BlockPos targetUpperFluidPos = targetAnchor.offset(OFFSET_FOUR, NON_BOTTOM_LAYER_Y_OFFSET, OFFSET_FOUR);
        BlockPos targetUpperFallingPos = targetAnchor.offset(OFFSET_FIVE, NON_BOTTOM_LAYER_Y_OFFSET, OFFSET_FIVE);

        helper.assertTrue(level.getBlockState(targetBottomFluidPos).is(Blocks.STONE), "底层流体应替换为石头");
        helper.assertTrue(level.getBlockState(targetBottomFallingPos).is(Blocks.STONE), "底层重力方块应替换为石头");
        helper.assertTrue(level.getBlockState(targetBottomNormalPos).is(Blocks.DIRT), "底层普通方块应保留原状");
        helper.assertTrue(level.getBlockState(targetUpperFluidPos).is(Blocks.WATER), "非底层流体应保留原状");
        helper.assertTrue(level.getBlockState(targetUpperFallingPos).is(Blocks.SAND), "非底层重力方块应保留原状");

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS)
    public void testApertureRegionCopierShouldPreserveChestNbt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos sourcePos = helper.absolutePos(
            new BlockPos(CHEST_SOURCE_RELATIVE_X, CHEST_SOURCE_RELATIVE_Y, CHEST_SOURCE_RELATIVE_Z)
        );
        BlockPos targetAnchor = helper.absolutePos(
            new BlockPos(CHEST_TARGET_RELATIVE_X, CHEST_TARGET_RELATIVE_Y, CHEST_TARGET_RELATIVE_Z)
        );

        // given: 源区域箱子写入钻石 x1。
        level.setBlock(sourcePos, Blocks.CHEST.defaultBlockState(), BLOCK_SET_FLAGS);
        ChestBlockEntity sourceChest = getChestBlockEntity(level, sourcePos, helper, "源箱子创建失败");
        ItemStack expectedDiamond = new ItemStack(Items.DIAMOND, DIAMOND_COUNT);
        sourceChest.setItem(CHEST_SLOT_INDEX, expectedDiamond.copy());
        sourceChest.setChanged();

        // when: 复制 1x1x1 区域到目标锚点。
        ApertureRegionCopier.copyRegion(level, sourcePos, sourcePos, targetAnchor);

        // then: 目标箱子及槽位物品保持与源一致。
        helper.assertTrue(level.getBlockState(targetAnchor).is(Blocks.CHEST), "目标位置应为箱子方块");
        ChestBlockEntity targetChest = getChestBlockEntity(level, targetAnchor, helper, "目标箱子不存在或类型不匹配");
        ItemStack actualStack = targetChest.getItem(CHEST_SLOT_INDEX);

        helper.assertTrue(actualStack.is(Items.DIAMOND), "目标箱子槽位 0 物品应为钻石");
        helper.assertTrue(actualStack.getCount() == DIAMOND_COUNT, "目标箱子槽位 0 钻石数量应为 1");

        helper.succeed();
    }

    private static ChestBlockEntity getChestBlockEntity(
        ServerLevel level,
        BlockPos pos,
        GameTestHelper helper,
        String failMessage
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        helper.assertTrue(blockEntity instanceof ChestBlockEntity, failMessage);
        return (ChestBlockEntity) blockEntity;
    }

    private static int alignToChunkMin(int value) {
        return Math.floorDiv(value, CHUNK_SIZE) * CHUNK_SIZE;
    }
}
