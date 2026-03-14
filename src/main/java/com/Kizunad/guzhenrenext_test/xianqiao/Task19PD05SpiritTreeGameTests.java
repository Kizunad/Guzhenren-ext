package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkDiffusionService;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingBlocks;
import com.Kizunad.guzhenrenext.xianqiao.farming.SpiritGatheringTreeBlock;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task19PD05SpiritTreeGameTests {

    private static final String TASK19_PD05_BATCH = "task19_p_d05_spirit_tree";
    private static final int TEST_TIMEOUT_TICKS = 140;
    private static final int ASSERT_DELAY_TICKS = 2;
    private static final double FIXED_NIAN_TOU_COST = 1.0D;
    private static final double HAPPY_NIAN_TOU_SEED = 5.0D;
    private static final double FLOAT_EPSILON = 0.0001D;
    private static final int APERTURE_RADIUS_BLOCKS = 64;
    private static final int TREE_Y = 2;
    private static final int OWNER_SPAWN_Y_OFFSET = 1;
    private static final BlockPos HAPPY_TREE_POS = new BlockPos(2, TREE_Y, 2);
    private static final BlockPos FAILURE_TREE_POS = new BlockPos(6, TREE_Y, 2);
    private static final BlockPos CROP_OFFSET = new BlockPos(1, 0, 0);
    private static final int INITIAL_CROP_AGE = 0;
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("2406f22f-2da4-4a59-8e15-bf460b228385");
    private static final UUID FAILURE_PLAYER_UUID = UUID.fromString("0f5f37a1-7e47-41fe-b8f0-2bc0fcf10746");

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK19_PD05_BATCH
    )
    public void testTask19PD05HappyPathShouldAdvanceOneCropAndConsumeFixedNianTou(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos treePos = helper.absolutePos(HAPPY_TREE_POS);
        BlockPos cropPos = treePos.offset(CROP_OFFSET);
        SpiritGatheringTreeBlock treeBlock = FarmingBlocks.SPIRIT_GATHERING_TREE.get();

        placeTreeAndCropFixture(level, treePos, cropPos);
        ServerPlayer owner = createTestPlayer(level, HAPPY_PLAYER_UUID, "task19_pd05_happy_player");
        owner.teleportTo(treePos.getX(), treePos.getY() + OWNER_SPAWN_Y_OFFSET, treePos.getZ());
        prepareApertureContext(level, owner, treePos);
        SpiritGatheringTreeBlock.seedNianTouAmountForTest(owner, HAPPY_NIAN_TOU_SEED);

        CropBlock cropBlock = (CropBlock) FarmingBlocks.QING_YA_GRASS.get();
        int ageBefore = cropBlock.getAge(level.getBlockState(cropPos));
        double nianTouBefore = SpiritGatheringTreeBlock.readNianTouAmountForTest(owner);
        boolean triggered = treeBlock.triggerGrowthPulseForTest(level, treePos);

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            int ageAfter = cropBlock.getAge(level.getBlockState(cropPos));
            double nianTouAfter = SpiritGatheringTreeBlock.readNianTouAmountForTest(owner);
            helper.assertTrue(triggered, "happy path: owner 可解析且念头充足时，聚灵树应触发成功");
            helper.assertTrue(
                ageAfter >= ageBefore + 1,
                "happy path: 单次触发应让半径内一个浅层作物至少前进 1 个生长阶段"
            );
            helper.assertTrue(
                Math.abs((nianTouBefore - FIXED_NIAN_TOU_COST) - nianTouAfter) <= FLOAT_EPSILON,
                "happy path: 触发成功后应扣除固定 1.0 念头"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = TASK19_PD05_BATCH
    )
    public void testTask19PD05FailurePathShouldKeepCropAndNianTouWhenOwnerLacksCost(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos treePos = helper.absolutePos(FAILURE_TREE_POS);
        BlockPos cropPos = treePos.offset(CROP_OFFSET);
        SpiritGatheringTreeBlock treeBlock = FarmingBlocks.SPIRIT_GATHERING_TREE.get();

        placeTreeAndCropFixture(level, treePos, cropPos);
        ServerPlayer owner = createTestPlayer(level, FAILURE_PLAYER_UUID, "task19_pd05_failure_player");
        owner.teleportTo(treePos.getX(), treePos.getY() + OWNER_SPAWN_Y_OFFSET, treePos.getZ());
        prepareApertureContext(level, owner, treePos);
        SpiritGatheringTreeBlock.seedNianTouAmountForTest(owner, 0.0D);

        CropBlock cropBlock = (CropBlock) FarmingBlocks.QING_YA_GRASS.get();
        int ageBefore = cropBlock.getAge(level.getBlockState(cropPos));
        double nianTouBefore = SpiritGatheringTreeBlock.readNianTouAmountForTest(owner);
        boolean triggered = treeBlock.triggerGrowthPulseForTest(level, treePos);
        int ageAfter = cropBlock.getAge(level.getBlockState(cropPos));
        double nianTouAfter = SpiritGatheringTreeBlock.readNianTouAmountForTest(owner);
        helper.assertFalse(triggered, "failure path: 念头不足时聚灵树不应触发加速");
        helper.assertTrue(ageAfter == ageBefore, "failure path: 念头不足时作物生长阶段必须保持不变");
        helper.assertTrue(
            Math.abs(nianTouAfter - nianTouBefore) <= FLOAT_EPSILON,
            "failure path: 念头不足时不应发生扣费"
        );
        helper.succeed();
    }

    private static void placeTreeAndCropFixture(ServerLevel level, BlockPos treePos, BlockPos cropPos) {
        level.setBlockAndUpdate(treePos.below(), Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(treePos.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(treePos, FarmingBlocks.SPIRIT_GATHERING_TREE.get().defaultBlockState());

        level.setBlockAndUpdate(cropPos.below(), Blocks.FARMLAND.defaultBlockState());
        BlockState cropState = FarmingBlocks.QING_YA_GRASS.get()
            .defaultBlockState()
            .setValue(CropBlock.AGE, INITIAL_CROP_AGE);
        level.setBlockAndUpdate(cropPos, cropState);
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }

    private static void prepareApertureContext(ServerLevel level, ServerPlayer player, BlockPos center) {
        ApertureWorldData levelData = ApertureWorldData.get(level);
        levelData.allocateAperture(player.getUUID());
        levelData.updateCenter(player.getUUID(), center);
        levelData.updateBoundaryByRadius(player.getUUID(), APERTURE_RADIUS_BLOCKS);

        ServerLevel apertureLevel = level.getServer().getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        if (apertureLevel != null) {
            ApertureWorldData apertureData = ApertureWorldData.get(apertureLevel);
            apertureData.allocateAperture(player.getUUID());
            apertureData.updateCenter(player.getUUID(), center);
            apertureData.updateBoundaryByRadius(player.getUUID(), APERTURE_RADIUS_BLOCKS);
        }
    }
}
