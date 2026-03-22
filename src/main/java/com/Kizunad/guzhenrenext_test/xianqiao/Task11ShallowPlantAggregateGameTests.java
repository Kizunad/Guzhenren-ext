package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingBlocks;
import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task11ShallowPlantAggregateGameTests {

    private static final String PLAN2_PLANT_SHALLOW_GROWTH_BATCH = "plan2.plant.shallow.growth";
    private static final String PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH = "plan2.plant.shallow.invalid_env";
    private static final int TEST_TIMEOUT_TICKS = 160;
    private static final int MAX_AGE = 7;
    private static final BlockPos HAPPY_CROP_POSITION = new BlockPos(2, 1, 2);
    private static final BlockPos FAILURE_CROP_POSITION = new BlockPos(4, 1, 2);

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs01QingYaGrass(GameTestHelper helper) {
        assertGrowthAndHarvest(helper, FarmingBlocks.QING_YA_GRASS.get(), FarmingItems.QING_YA_GRASS_ITEM.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs02NingXueGen(GameTestHelper helper) {
        assertGrowthAndHarvest(helper, FarmingBlocks.NING_XUE_GEN.get(), FarmingItems.NING_XUE_GEN_ITEM.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs03JuYuanFlower(GameTestHelper helper) {
        assertGrowthAndHarvest(helper, FarmingBlocks.JU_YUAN_FLOWER.get(), FarmingItems.JU_YUAN_FLOWER_ITEM.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs04XiSuiVine(GameTestHelper helper) {
        assertGrowthAndHarvest(helper, FarmingBlocks.XI_SUI_VINE.get(), FarmingItems.XI_SUI_VINE_ITEM.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs05TiePiBamboo(GameTestHelper helper) {
        assertGrowthAndHarvest(helper, FarmingBlocks.TIE_PI_BAMBOO.get(), FarmingItems.TIE_PI_BAMBOO_ITEM.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs06HuoLingZhiMushroom(GameTestHelper helper) {
        assertGrowthAndHarvest(
            helper,
            FarmingBlocks.HUO_LING_ZHI_MUSHROOM.get(),
            FarmingItems.HUO_LING_ZHI_MUSHROOM_ITEM.get()
        );
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs07BingXinGrass(GameTestHelper helper) {
        assertGrowthAndHarvest(helper, FarmingBlocks.BING_XIN_GRASS.get(), FarmingItems.BING_XIN_GRASS_ITEM.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs08HuanDuMushroom(GameTestHelper helper) {
        assertGrowthAndHarvest(
            helper,
            FarmingBlocks.HUAN_DU_MUSHROOM.get(),
            FarmingItems.HUAN_DU_MUSHROOM_ITEM.get()
        );
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs09YingTaiLichen(GameTestHelper helper) {
        assertGrowthAndHarvest(
            helper,
            FarmingBlocks.YING_TAI_LICHEN.get(),
            FarmingItems.YING_TAI_LICHEN_ITEM.get()
        );
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs10CiVine(GameTestHelper helper) {
        assertGrowthAndHarvest(helper, FarmingBlocks.CI_VINE.get(), FarmingItems.CI_VINE_ITEM.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs11JianYeGrass(GameTestHelper helper) {
        assertGrowthAndHarvest(helper, FarmingBlocks.JIAN_YE_GRASS.get(), FarmingItems.JIAN_YE_GRASS_ITEM.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs12ChenShuiLilyPad(GameTestHelper helper) {
        assertGrowthAndHarvest(
            helper,
            FarmingBlocks.CHEN_SHUI_LILY_PAD.get(),
            FarmingItems.CHEN_SHUI_LILY_PAD_ITEM.get()
        );
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs13DiLongBerryBush(GameTestHelper helper) {
        assertGrowthAndHarvest(
            helper,
            FarmingBlocks.DI_LONG_BERRY_BUSH.get(),
            FarmingItems.DI_LONG_BERRY_BUSH_ITEM.get()
        );
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs14FengXinZi(GameTestHelper helper) {
        assertGrowthAndHarvest(helper, FarmingBlocks.FENG_XIN_ZI.get(), FarmingItems.FENG_XIN_ZI_ITEM.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs15LeiGuSapling(GameTestHelper helper) {
        assertGrowthAndHarvest(helper, FarmingBlocks.LEI_GU_SAPLING.get(), FarmingItems.LEI_GU_SAPLING_ITEM.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs16ShiYinGrass(GameTestHelper helper) {
        assertGrowthAndHarvest(helper, FarmingBlocks.SHI_YIN_GRASS.get(), FarmingItems.SHI_YIN_GRASS_ITEM.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs17ChunYangFlower(GameTestHelper helper) {
        assertGrowthAndHarvest(
            helper,
            FarmingBlocks.CHUN_YANG_FLOWER.get(),
            FarmingItems.CHUN_YANG_FLOWER_ITEM.get()
        );
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs18YanShouCocoa(GameTestHelper helper) {
        assertGrowthAndHarvest(
            helper,
            FarmingBlocks.YAN_SHOU_COCOA.get(),
            FarmingItems.YAN_SHOU_COCOA_ITEM.get()
        );
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs19WangYouGrass(GameTestHelper helper) {
        assertGrowthAndHarvest(helper, FarmingBlocks.WANG_YOU_GRASS.get(), FarmingItems.WANG_YOU_GRASS_ITEM.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_GROWTH_BATCH
    )
    public void testPlan2PlantShallowGrowthPs20SheYanMelonStem(GameTestHelper helper) {
        assertGrowthAndHarvest(
            helper,
            FarmingBlocks.SHE_YAN_MELON_STEM.get(),
            FarmingItems.SHE_YAN_MELON_STEM_ITEM.get()
        );
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs01QingYaGrass(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.QING_YA_GRASS.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs02NingXueGen(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.NING_XUE_GEN.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs03JuYuanFlower(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.JU_YUAN_FLOWER.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs04XiSuiVine(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.XI_SUI_VINE.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs05TiePiBamboo(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.TIE_PI_BAMBOO.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs06HuoLingZhiMushroom(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.HUO_LING_ZHI_MUSHROOM.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs07BingXinGrass(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.BING_XIN_GRASS.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs08HuanDuMushroom(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.HUAN_DU_MUSHROOM.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs09YingTaiLichen(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.YING_TAI_LICHEN.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs10CiVine(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.CI_VINE.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs11JianYeGrass(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.JIAN_YE_GRASS.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs12ChenShuiLilyPad(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.CHEN_SHUI_LILY_PAD.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs13DiLongBerryBush(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.DI_LONG_BERRY_BUSH.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs14FengXinZi(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.FENG_XIN_ZI.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs15LeiGuSapling(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.LEI_GU_SAPLING.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs16ShiYinGrass(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.SHI_YIN_GRASS.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs17ChunYangFlower(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.CHUN_YANG_FLOWER.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs18YanShouCocoa(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.YAN_SHOU_COCOA.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs19WangYouGrass(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.WANG_YOU_GRASS.get());
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_PLANT_SHALLOW_INVALID_ENV_BATCH
    )
    public void testPlan2PlantShallowInvalidEnvPs20SheYanMelonStem(GameTestHelper helper) {
        assertInvalidEnvironment(helper, FarmingBlocks.SHE_YAN_MELON_STEM.get());
    }

    private static void assertGrowthAndHarvest(GameTestHelper helper, CropBlock crop, Item expectedItem) {
        BlockPos cropPos = helper.absolutePos(HAPPY_CROP_POSITION);
        helper.getLevel().setBlockAndUpdate(cropPos.below(), Blocks.FARMLAND.defaultBlockState());
        BlockState matureState = crop.defaultBlockState().setValue(CropBlock.AGE, MAX_AGE);
        helper.assertTrue(
            matureState.canSurvive(helper.getLevel(), cropPos),
            "happy path: 成熟浅度植物在耕地上应可存活"
        );
        helper.getLevel().setBlockAndUpdate(cropPos, matureState);
        BlockState state = helper.getLevel().getBlockState(cropPos);
        BlockEntity blockEntity = state.hasBlockEntity() ? helper.getLevel().getBlockEntity(cropPos) : null;
        List<ItemStack> generatedDrops = Block.getDrops(state, helper.getLevel(), cropPos, blockEntity);
        Block.dropResources(state, helper.getLevel(), cropPos, blockEntity, null, ItemStack.EMPTY);
        helper.getLevel().destroyBlock(cropPos, false);
        helper.assertTrue(
            generatedDrops.stream().anyMatch(stack -> stack.is(expectedItem)),
            "happy path: 成熟浅度植物破坏后应掉落对应作物入口, 实际掉落=" + generatedDrops
        );
        helper.succeed();
    }

    private static void assertInvalidEnvironment(GameTestHelper helper, CropBlock crop) {
        BlockPos failurePos = helper.absolutePos(FAILURE_CROP_POSITION);
        helper.getLevel().setBlockAndUpdate(failurePos.below(), Blocks.STONE.defaultBlockState());
        BlockState cropState = crop.defaultBlockState();
        helper.assertFalse(
            cropState.canSurvive(helper.getLevel(), failurePos),
            "failure path: 非耕地环境不应允许浅度植物稳定成立"
        );
        helper.succeed();
    }
}
