package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.alchemy.effect.DeepPillEffectState;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkDiffusionService;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingBlocks;
import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import com.Kizunad.guzhenrenext.xianqiao.spirit.XianqiaoEntities;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.CalamityBeastEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.StoneVeinSentinelEntity;
import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task24MaterialDeepBatch2GameTests {

    private static final String PLAN2_MATERIAL_DEEP_BATCH2_MAIN = "plan2.material.deep.batch2.main";
    private static final int TEST_TIMEOUT_TICKS = 160;
    private static final int ASSERT_DELAY_TICKS = 2;
    private static final int MIN_REQUIRED_DROP_COUNT = 1;
    private static final int COLLAPSE_OFFSET_X = 1;
    private static final double ENTITY_CENTER_OFFSET = 0.5D;
    private static final double DROP_SCAN_RADIUS = 1.5D;
    private static final double DROP_SCAN_HEIGHT_MARGIN = 1.5D;
    private static final double MD08_DROP_SCAN_RADIUS = 32.0D;
    private static final double MD08_DROP_SCAN_HEIGHT_MARGIN = 6.0D;
    private static final int MD09_EXTRA_DEBT_TICKS = 1200;
    private static final float EXPECTED_REFORGE_WEAK_TIME_SPEED = 0.25F;
    private static final float EXPECTED_NORMAL_TIME_SPEED = 1.0F;
    private static final float TIME_SPEED_EPSILON = 0.001F;
    private static final float MD08_LETHAL_DAMAGE = 999.0F;
    private static final float MD08_NON_LETHAL_DAMAGE = 1.0F;
    private static final float MD10_LETHAL_DAMAGE = 999.0F;
    private static final int MD08_EXPECTED_COUNT_ZERO = 0;
    private static final int MD08_EXPECTED_COUNT_ONE = 1;
    private static final int MD10_EXPECTED_COUNT_ONE = 1;
    private static final int MD10_EXPECTED_COUNT_ZERO = 0;
    private static final long MD10_TRIBULATION_FUTURE_BUFFER_TICKS = 2000L;
    private static final long MD10_HIGH_PRESSURE_ADVANCE_TICKS = 1200L;
    private static final double MD08_POST_TRIGGER_MOVE_X_OFFSET = 8.0D;
    private static final double MD08_MAX_HUNPO_BEFORE_SEED = 9.0D;
    private static final String MD08_PLAYER_VARIABLES_TAG = "guzhenren:player_variables";
    private static final String MD08_VAR_MAX_HUNPO = "zuida_hunpo";
    private static final long MD09_HAPPY_REVERSAL_END = 60L;
    private static final long MD09_HAPPY_WEAK_END = 120L;
    private static final long MD09_HAPPY_DURING_WINDOW_TICK = 59L;
    private static final long MD09_HAPPY_AFTER_END_TICK = 61L;
    private static final long MD09_FAILURE_REVERSAL_END = 80L;
    private static final long MD09_FAILURE_WEAK_END = 80L;
    private static final long MD09_FAILURE_DURING_WINDOW_TICK = 79L;
    private static final long MD09_FAILURE_AFTER_END_TICK = 81L;
    private static final UUID MD09_HAPPY_PLAYER_UUID = UUID.fromString("e1c59413-9983-4fb3-a58c-62f1d840f2af");
    private static final String MD09_HAPPY_PLAYER_NAME = "task24_md09_happy";
    private static final UUID MD09_FAILURE_PLAYER_UUID = UUID.fromString("7e1738ac-0dff-450d-8e0f-cc9a19053f4c");
    private static final String MD09_FAILURE_PLAYER_NAME = "task24_md09_failure";
    private static final UUID MD08_HAPPY_PLAYER_UUID = UUID.fromString("0d0f3b33-c4b2-4fce-b493-5fdb45ce30f8");
    private static final String MD08_HAPPY_PLAYER_NAME = "task24_md08_happy";
    private static final UUID MD08_FAILURE_PLAYER_UUID = UUID.fromString("17f727f4-3f0d-4688-85d8-17dc9fbcab86");
    private static final String MD08_FAILURE_PLAYER_NAME = "task24_md08_failure";
    private static final UUID MD10_HAPPY_PLAYER_UUID = UUID.fromString("80a70634-19b1-4f4e-9f89-2accfcf75377");
    private static final String MD10_HAPPY_PLAYER_NAME = "task24_md10_happy";
    private static final UUID MD10_FAILURE_PLAYER_UUID = UUID.fromString("cd7371ee-47d0-4b96-88ea-f27ad7e3fbc7");
    private static final String MD10_FAILURE_PLAYER_NAME = "task24_md10_failure";
    private static final UUID MD10_FAILURE_BOUND_OWNER_UUID =
        UUID.fromString("15a68b34-b56e-4d8a-8062-5f3cb8dbe822");
    private static final BlockPos MD06_HAPPY_SENTINEL_POS = new BlockPos(2, 2, 2);
    private static final BlockPos MD06_FAILURE_SENTINEL_POS = new BlockPos(6, 2, 2);
    private static final BlockPos MD07_HAPPY_SUCCESS_VINE_POS = new BlockPos(10, 2, 2);
    private static final BlockPos MD07_HAPPY_DEGRADE_VINE_POS = new BlockPos(14, 2, 2);
    private static final BlockPos MD07_FAILURE_VINE_POS = new BlockPos(18, 2, 2);
    private static final BlockPos MD09_HAPPY_PLAYER_POS = new BlockPos(22, 2, 2);
    private static final BlockPos MD09_FAILURE_PLAYER_POS = new BlockPos(26, 2, 2);
    private static final BlockPos MD08_HAPPY_PLAYER_POS = new BlockPos(30, 2, 2);
    private static final BlockPos MD08_FAILURE_PLAYER_POS = new BlockPos(34, 2, 2);
    private static final BlockPos MD10_HAPPY_CALAMITY_BEAST_POS = new BlockPos(38, 2, 2);
    private static final BlockPos MD10_FAILURE_CALAMITY_BEAST_POS = new BlockPos(42, 2, 2);

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH2_MAIN
    )
    public void testPlan2MaterialDeepBatch2Md06HappyPathShouldDropDiMaiLongJingAndTriggerCollapseRisk(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        StoneVeinSentinelEntity sentinel = createStoneVeinSentinel(helper, MD06_HAPPY_SENTINEL_POS);
        sentinel.setNoAi(true);
        sentinel.setNoGravity(true);

        BlockPos sentinelPos = helper.absolutePos(MD06_HAPPY_SENTINEL_POS);
        helper.assertTrue(
            sentinel.blockPosition().equals(sentinelPos),
            "Task24/M-D06: happy path 灵脉石人坐标夹具必须与断言锚点一致"
        );
        BlockPos underPos = sentinelPos.below();
        BlockPos collapsePos = underPos.offset(COLLAPSE_OFFSET_X, 0, 0);
        level.setBlockAndUpdate(collapsePos.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(underPos, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(collapsePos, Blocks.COBBLESTONE.defaultBlockState());

        sentinel.forceConversionForTest();

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            sentinel.forceConversionForTest();
            AABB dropArea = createDropScanArea(sentinelPos);
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, dropArea);
            int dropCount = countItems(drops, XianqiaoItems.DI_MAI_LONG_JING.get());

            helper.assertTrue(
                level.getBlockState(underPos).is(Blocks.COAL_ORE),
                "happy path: 灵脉石人矿化链命中后必须将脚下石块转化为矿化核心"
            );
            helper.assertTrue(
                level.getBlockState(collapsePos).is(Blocks.GRAVEL),
                "happy path: 产出 M-D06 时必须出现固定塌陷副作用，体现地脉塌陷风险"
            );
            helper.assertTrue(
                dropCount >= MIN_REQUIRED_DROP_COUNT,
                "happy path: 灵脉石人矿化链命中后必须掉落 M-D06 地脉龙晶"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH2_MAIN
    )
    public void testPlan2MaterialDeepBatch2Md06FailurePathShouldNotDropOrCollapseWhenPreconditionMissing(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        StoneVeinSentinelEntity sentinel = createStoneVeinSentinel(helper, MD06_FAILURE_SENTINEL_POS);
        sentinel.setNoAi(true);
        sentinel.setNoGravity(true);

        BlockPos sentinelPos = helper.absolutePos(MD06_FAILURE_SENTINEL_POS);
        helper.assertTrue(
            sentinel.blockPosition().equals(sentinelPos),
            "Task24/M-D06: failure path 灵脉石人坐标夹具必须与断言锚点一致"
        );
        BlockPos underPos = sentinelPos.below();
        BlockPos collapsePos = underPos.offset(COLLAPSE_OFFSET_X, 0, 0);
        level.setBlockAndUpdate(underPos, Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(collapsePos, Blocks.STONE.defaultBlockState());

        sentinel.forceConversionForTest();

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            sentinel.forceConversionForTest();
            AABB dropArea = createDropScanArea(sentinelPos);
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, dropArea);
            int dropCount = countItems(drops, XianqiaoItems.DI_MAI_LONG_JING.get());

            helper.assertTrue(
                level.getBlockState(underPos).is(Blocks.DIRT),
                "failure path: 未满足矿化前置时，脚下方块状态必须保持不变"
            );
            helper.assertTrue(
                level.getBlockState(collapsePos).is(Blocks.STONE),
                "failure path: 未满足矿化前置时不得触发塌陷副作用"
            );
            helper.assertTrue(
                dropCount == 0,
                "failure path: 未满足矿化前置时不得掉落 M-D06 地脉龙晶"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH2_MAIN
    )
    public void testPlan2MaterialDeepBatch2Md07HappyPathShouldDropWanXiangJinShaAndExposeDowngradeLoss(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        BlockPos successVinePos = helper.absolutePos(MD07_HAPPY_SUCCESS_VINE_POS);
        BlockPos successUpOrePos = successVinePos.above();
        BlockPos successDownOrePos = successVinePos.below();
        level.setBlockAndUpdate(successVinePos.above(2), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(successUpOrePos, Blocks.GOLD_ORE.defaultBlockState());
        level.setBlockAndUpdate(successDownOrePos, Blocks.IRON_ORE.defaultBlockState());
        level.setBlockAndUpdate(successVinePos, FarmingBlocks.CAVE_VINES.get().defaultBlockState());

        BlockPos degradeVinePos = helper.absolutePos(MD07_HAPPY_DEGRADE_VINE_POS);
        BlockPos degradeUpOrePos = degradeVinePos.above();
        BlockPos degradeDownOrePos = degradeVinePos.below();
        level.setBlockAndUpdate(degradeVinePos.above(2), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(degradeUpOrePos, Blocks.GOLD_ORE.defaultBlockState());
        level.setBlockAndUpdate(degradeDownOrePos, Blocks.DIAMOND_ORE.defaultBlockState());
        level.setBlockAndUpdate(degradeVinePos, FarmingBlocks.CAVE_VINES.get().defaultBlockState());

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            BlockState successVineState = level.getBlockState(successVinePos);
            helper.assertTrue(
                successVineState.hasProperty(CaveVines.BERRIES) && successVineState.getValue(CaveVines.BERRIES),
                "happy path: 多矿精炼成功后噬金藤必须进入已吞噬状态"
            );
            helper.assertTrue(
                level.getBlockState(successUpOrePos).is(Blocks.STONE),
                "happy path: 主矿位必须被吞噬并降为石头"
            );
            helper.assertTrue(
                level.getBlockState(successDownOrePos).is(Blocks.COBBLESTONE),
                "happy path: 多矿副产链必须消耗次矿并降级为圆石，体现固定代价"
            );

            helper.assertTrue(
                level.getBlockState(degradeUpOrePos).is(Blocks.STONE),
                "happy path: 失败降级子场景中主矿位也必须被吞噬"
            );
            helper.assertTrue(
                level.getBlockState(degradeDownOrePos).is(Blocks.COBBLESTONE),
                "happy path: 非精炼矿也必须被降级损耗，不能零代价试错"
            );

            int successDropCount = countItems(
                level.getEntitiesOfClass(ItemEntity.class, createDropScanArea(successVinePos)),
                XianqiaoItems.WAN_XIANG_JIN_SHA.get()
            );
            int degradeDropCount = countItems(
                level.getEntitiesOfClass(ItemEntity.class, createDropScanArea(degradeVinePos)),
                XianqiaoItems.WAN_XIANG_JIN_SHA.get()
            );

            helper.assertTrue(
                successDropCount >= MIN_REQUIRED_DROP_COUNT,
                "happy path: 金矿+催化矿的多矿转化必须产出 M-D07 万象金砂"
            );
            helper.assertTrue(
                degradeDropCount == 0,
                "happy path: 多矿但催化条件不成立时不得产出 M-D07，只允许出现降级损耗"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH2_MAIN
    )
    public void testPlan2MaterialDeepBatch2Md07FailurePathShouldFailClosedWithoutMultiOrePrecondition(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        BlockPos vinePos = helper.absolutePos(MD07_FAILURE_VINE_POS);
        BlockPos upOrePos = vinePos.above();
        BlockPos downPos = vinePos.below();

        level.setBlockAndUpdate(vinePos.above(2), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(upOrePos, Blocks.GOLD_ORE.defaultBlockState());
        level.setBlockAndUpdate(downPos, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(vinePos, FarmingBlocks.CAVE_VINES.get().defaultBlockState());

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            BlockState vineState = level.getBlockState(vinePos);
            helper.assertTrue(
                vineState.hasProperty(CaveVines.BERRIES) && vineState.getValue(CaveVines.BERRIES),
                "failure path: 单矿吞噬仍可触发 P-D04 基础链路，保证主锚点行为不回退"
            );
            helper.assertTrue(
                level.getBlockState(upOrePos).is(Blocks.STONE),
                "failure path: 单矿路径下仅允许主矿位被吞噬"
            );
            helper.assertTrue(
                level.getBlockState(downPos).is(Blocks.STONE),
                "failure path: 不满足多矿前置时次位不得被误降级"
            );

            int md07DropCount = countItems(
                level.getEntitiesOfClass(ItemEntity.class, createDropScanArea(vinePos)),
                XianqiaoItems.WAN_XIANG_JIN_SHA.get()
            );
            helper.assertTrue(
                md07DropCount == 0,
                "failure path: 缺少多矿前置时必须 fail-closed，不得产出 M-D07 万象金砂"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH2_MAIN
    )
    public void testPlan2MaterialDeepBatch2Md08HappyPathShouldDropOneResidualSoulCrystalAndApplySoulCapPenalty(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, MD08_HAPPY_PLAYER_UUID, MD08_HAPPY_PLAYER_NAME);
        BlockPos playerPos = helper.absolutePos(MD08_HAPPY_PLAYER_POS);
        player.moveTo(
            playerPos.getX() + ENTITY_CENTER_OFFSET,
            playerPos.getY(),
            playerPos.getZ() + ENTITY_CENTER_OFFSET
        );
        prepareMd08DamageFixture(player);
        seedMaxHunPoForTest(player, MD08_MAX_HUNPO_BEFORE_SEED);
        DeepPillEffectState.grantNearDeathToken(player);

        boolean consumed = DeepPillEffectState.consumeNearDeathTokenOnLethalDamage(player, MD08_LETHAL_DAMAGE);
        helper.assertTrue(consumed, "happy path: 致死伤害条件必须消费 D-D01 保命令牌");
        DeepPillEffectState.settleNearDeathResidualSoul(player);
        player.moveTo(
            playerPos.getX() + ENTITY_CENTER_OFFSET + MD08_POST_TRIGGER_MOVE_X_OFFSET,
            playerPos.getY(),
            playerPos.getZ() + ENTITY_CENTER_OFFSET
        );
        boolean settleAgain = DeepPillEffectState.settleNearDeathResidualSoul(player);
        helper.assertTrue(!settleAgain, "happy path: 同一次令牌消费事件不得重复结晶");

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            int worldDropCount = countItems(
                level.getEntitiesOfClass(
                    ItemEntity.class,
                    createWideDropScanArea(playerPos, MD08_DROP_SCAN_RADIUS, MD08_DROP_SCAN_HEIGHT_MARGIN)
                ),
                XianqiaoItems.YOU_HUN_NING_PO_SHI.get()
            );
            int inventoryDropCount = countInventoryItems(player, XianqiaoItems.YOU_HUN_NING_PO_SHI.get());
            int md08DropCount = worldDropCount + inventoryDropCount;
            int crystalCount = DeepPillEffectState.dumpDebugState(player)
                .getInt(DeepPillEffectState.KEY_RESIDUAL_SOUL_CRYSTAL_COUNT);
            int penaltyCount = DeepPillEffectState.dumpDebugState(player)
                .getInt(DeepPillEffectState.KEY_RESIDUAL_SOUL_PENALTY_COUNT);
            int consumeCount = DeepPillEffectState.dumpDebugState(player)
                .getInt(DeepPillEffectState.KEY_NEAR_DEATH_CONSUME_COUNT);
            int lastConsumeCount = DeepPillEffectState.dumpDebugState(player)
                .getInt(DeepPillEffectState.KEY_RESIDUAL_SOUL_LAST_CONSUME_COUNT);
            double maxHunPoBefore = DeepPillEffectState.dumpDebugState(player)
                .getDouble(DeepPillEffectState.KEY_RESIDUAL_SOUL_LAST_MAX_HUNPO_BEFORE);
            double maxHunPoAfter = DeepPillEffectState.dumpDebugState(player)
                .getDouble(DeepPillEffectState.KEY_RESIDUAL_SOUL_LAST_MAX_HUNPO_AFTER);

            helper.assertTrue(
                consumeCount == MD08_EXPECTED_COUNT_ONE,
                "happy path: D-D01 保命令牌消费计数必须为 1"
            );
            helper.assertTrue(
                crystalCount == MD08_EXPECTED_COUNT_ONE,
                "happy path: 残魂结晶计数必须精确记录为 1 次"
            );
            helper.assertTrue(
                penaltyCount == MD08_EXPECTED_COUNT_ONE,
                "happy path: 魂魄上限代价计数必须精确记录为 1 次"
            );
            helper.assertTrue(
                md08DropCount == MD08_EXPECTED_COUNT_ONE,
                "happy path: D-D01 消耗触发后必须稳定产出且仅产出 1 个 M-D08 幽魂凝魄石"
                    + "（world=" + worldDropCount
                    + ", inventory=" + inventoryDropCount
                    + ", crystalCount=" + crystalCount
                    + ", consumeCount=" + consumeCount + "）"
            );
            helper.assertTrue(
                lastConsumeCount == MD08_EXPECTED_COUNT_ONE,
                "happy path: 防重标记应锚定到本次消费事件"
            );
            helper.assertTrue(
                maxHunPoAfter < maxHunPoBefore,
                "happy path: 触发 M-D08 后应可观测到魂魄上限下降代价"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH2_MAIN
    )
    public void testPlan2MaterialDeepBatch2Md08FailurePathShouldNotDropOrPenaltyWithoutTokenConsume(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, MD08_FAILURE_PLAYER_UUID, MD08_FAILURE_PLAYER_NAME);
        BlockPos playerPos = helper.absolutePos(MD08_FAILURE_PLAYER_POS);
        player.moveTo(
            playerPos.getX() + ENTITY_CENTER_OFFSET,
            playerPos.getY(),
            playerPos.getZ() + ENTITY_CENTER_OFFSET
        );
        prepareMd08DamageFixture(player);
        seedMaxHunPoForTest(player, MD08_MAX_HUNPO_BEFORE_SEED);

        boolean consumed = DeepPillEffectState.consumeNearDeathTokenOnLethalDamage(player, MD08_NON_LETHAL_DAMAGE);
        helper.assertTrue(!consumed, "failure path: 非致死伤害不得消费 D-D01 保命令牌");

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            int worldDropCount = countItems(
                level.getEntitiesOfClass(
                    ItemEntity.class,
                    createWideDropScanArea(playerPos, MD08_DROP_SCAN_RADIUS, MD08_DROP_SCAN_HEIGHT_MARGIN)
                ),
                XianqiaoItems.YOU_HUN_NING_PO_SHI.get()
            );
            int inventoryDropCount = countInventoryItems(player, XianqiaoItems.YOU_HUN_NING_PO_SHI.get());
            int md08DropCount = worldDropCount + inventoryDropCount;
            int crystalCount = DeepPillEffectState.dumpDebugState(player)
                .getInt(DeepPillEffectState.KEY_RESIDUAL_SOUL_CRYSTAL_COUNT);
            int penaltyCount = DeepPillEffectState.dumpDebugState(player)
                .getInt(DeepPillEffectState.KEY_RESIDUAL_SOUL_PENALTY_COUNT);
            int consumeCount = DeepPillEffectState.dumpDebugState(player)
                .getInt(DeepPillEffectState.KEY_NEAR_DEATH_CONSUME_COUNT);
            int lastConsumeCount = DeepPillEffectState.dumpDebugState(player)
                .getInt(DeepPillEffectState.KEY_RESIDUAL_SOUL_LAST_CONSUME_COUNT);

            helper.assertTrue(
                md08DropCount == MD08_EXPECTED_COUNT_ZERO,
                "failure path: 未消费 D-D01 令牌时必须 fail-closed，不能产出 M-D08"
            );
            helper.assertTrue(
                crystalCount == MD08_EXPECTED_COUNT_ZERO,
                "failure path: 未消费令牌时不能伪造残魂结晶计数"
            );
            helper.assertTrue(
                penaltyCount == MD08_EXPECTED_COUNT_ZERO,
                "failure path: 未消费令牌时不能伪造魂魄上限惩罚"
            );
            helper.assertTrue(
                consumeCount == MD08_EXPECTED_COUNT_ZERO,
                "failure path: 未服用保命丹时消费计数必须保持 0"
            );
            helper.assertTrue(
                lastConsumeCount == MD08_EXPECTED_COUNT_ZERO,
                "failure path: 未触发事件时防重标记不得前进"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH2_MAIN
    )
    public void testPlan2MaterialDeepBatch2Md09HappyPathShouldReforgeShardWhenValidReversalWindowEnds(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, MD09_HAPPY_PLAYER_UUID, MD09_HAPPY_PLAYER_NAME);
        prepareApertureContext(level, player);
        BlockPos playerPos = helper.absolutePos(MD09_HAPPY_PLAYER_POS);
        player.moveTo(
            playerPos.getX() + ENTITY_CENTER_OFFSET,
            playerPos.getY(),
            playerPos.getZ() + ENTITY_CENTER_OFFSET
        );
        player.getPersistentData().putLong(DeepPillEffectState.KEY_REVERSAL_END, MD09_HAPPY_REVERSAL_END);
        player.getPersistentData().putLong(DeepPillEffectState.KEY_REVERSAL_WEAK_END, MD09_HAPPY_WEAK_END);

        DeepPillEffectState.tickPlayer(player, MD09_HAPPY_DURING_WINDOW_TICK);
        DeepPillEffectState.tickPlayer(player, MD09_HAPPY_AFTER_END_TICK);

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            int md09DropCount = countItems(
                level.getEntitiesOfClass(ItemEntity.class, createDropScanArea(playerPos)),
                XianqiaoItems.SHI_SHA_LIU_LI.get()
            );
            long expectedWeakEnd = MD09_HAPPY_WEAK_END + MD09_EXTRA_DEBT_TICKS;
            long actualWeakEnd = DeepPillEffectState.dumpDebugState(player)
                .getLong(DeepPillEffectState.KEY_REVERSAL_WEAK_END);
            long actualReforgeEnd = DeepPillEffectState.dumpDebugState(player)
                .getLong(DeepPillEffectState.KEY_REVERSAL_REFORGE_END);
            float currentTimeSpeed = readApertureTimeSpeed(level, player);

            helper.assertTrue(
                md09DropCount == MIN_REQUIRED_DROP_COUNT,
                "happy path: 有效逆时窗口结束后必须稳定重铸 1 个 M-D09 时砂琉璃"
            );
            helper.assertTrue(
                actualWeakEnd == expectedWeakEnd,
                "happy path: 回收重铸后必须叠加固定时停债务并延长虚弱窗口"
            );
            helper.assertTrue(
                actualReforgeEnd == MD09_HAPPY_REVERSAL_END,
                "happy path: 逆时窗口结束刻应被记录为已回收，避免重复产出"
            );
            helper.assertTrue(
                Math.abs(currentTimeSpeed - EXPECTED_REFORGE_WEAK_TIME_SPEED) <= TIME_SPEED_EPSILON,
                "happy path: 回收重铸后时间流速必须保持在虚弱态，作为可见代价"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH2_MAIN
    )
    public void testPlan2MaterialDeepBatch2Md09FailurePathShouldNotReforgeOrFabricateDebtWhenWeakWindowMissing(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, MD09_FAILURE_PLAYER_UUID, MD09_FAILURE_PLAYER_NAME);
        prepareApertureContext(level, player);
        BlockPos playerPos = helper.absolutePos(MD09_FAILURE_PLAYER_POS);
        player.moveTo(
            playerPos.getX() + ENTITY_CENTER_OFFSET,
            playerPos.getY(),
            playerPos.getZ() + ENTITY_CENTER_OFFSET
        );
        player.getPersistentData().putLong(DeepPillEffectState.KEY_REVERSAL_END, MD09_FAILURE_REVERSAL_END);
        player.getPersistentData().putLong(DeepPillEffectState.KEY_REVERSAL_WEAK_END, MD09_FAILURE_WEAK_END);

        DeepPillEffectState.tickPlayer(player, MD09_FAILURE_DURING_WINDOW_TICK);
        DeepPillEffectState.tickPlayer(player, MD09_FAILURE_AFTER_END_TICK);

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            int md09DropCount = countItems(
                level.getEntitiesOfClass(ItemEntity.class, createDropScanArea(playerPos)),
                XianqiaoItems.SHI_SHA_LIU_LI.get()
            );
            long actualWeakEnd = DeepPillEffectState.dumpDebugState(player)
                .getLong(DeepPillEffectState.KEY_REVERSAL_WEAK_END);
            long actualReforgeEnd = DeepPillEffectState.dumpDebugState(player)
                .getLong(DeepPillEffectState.KEY_REVERSAL_REFORGE_END);
            float currentTimeSpeed = readApertureTimeSpeed(level, player);

            helper.assertTrue(
                md09DropCount == 0,
                "failure path: 缺失逆时虚弱窗口前置时必须 fail-closed，不得重铸 M-D09"
            );
            helper.assertTrue(
                actualWeakEnd == MD09_FAILURE_WEAK_END,
                "failure path: 前置缺失时不得伪造额外时间债务或篡改虚弱窗口"
            );
            helper.assertTrue(
                actualReforgeEnd == 0L,
                "failure path: 前置缺失时不得写入逆时回收完成标记"
            );
            helper.assertTrue(
                Math.abs(currentTimeSpeed - EXPECTED_NORMAL_TIME_SPEED) <= TIME_SPEED_EPSILON,
                "failure path: 前置缺失时不得遗留额外减速副作用"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH2_MAIN
    )
    public void testPlan2MaterialDeepBatch2Md10HappyPathShouldDropExactlyOneDaoYuanMuKuangAndAdvanceTribulationTick(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        ServerPlayer killer = createTestPlayer(level, MD10_HAPPY_PLAYER_UUID, MD10_HAPPY_PLAYER_NAME);
        prepareApertureContext(level, killer);

        ServerLevel apertureLevel = level.getServer().getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        ServerLevel tribulationLevel = apertureLevel != null ? apertureLevel : level;
        ApertureWorldData apertureData = ApertureWorldData.get(tribulationLevel);
        long seededTribulationTick = tribulationLevel.getGameTime()
            + MD10_HIGH_PRESSURE_ADVANCE_TICKS
            + MD10_TRIBULATION_FUTURE_BUFFER_TICKS;
        long expectedAdvancedTick = seededTribulationTick - MD10_HIGH_PRESSURE_ADVANCE_TICKS;
        apertureData.updateTribulationTick(MD10_HAPPY_PLAYER_UUID, seededTribulationTick);
        ApertureInfo beforeInfo = apertureData.getAperture(MD10_HAPPY_PLAYER_UUID);
        helper.assertTrue(beforeInfo != null, "Task24/M-D10: happy path 仙窍信息缺失");
        helper.assertTrue(
            beforeInfo.nextTribulationTick() == seededTribulationTick,
            "Task24/M-D10: happy path 前置灾劫刻度应精确锚定到夹具值"
        );

        BlockPos beastPos = helper.absolutePos(MD10_HAPPY_CALAMITY_BEAST_POS);
        CalamityBeastEntity calamityBeast = createCalamityBeast(helper, MD10_HAPPY_CALAMITY_BEAST_POS);
        calamityBeast.setNoAi(true);
        calamityBeast.bindTribulationOwner(MD10_HAPPY_PLAYER_UUID);

        boolean hurtApplied = calamityBeast.hurt(level.damageSources().playerAttack(killer), MD10_LETHAL_DAMAGE);
        helper.assertTrue(hurtApplied, "happy path: 玩家致死伤害应命中 C-D09 灾厄兽");

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            int worldDropCount = countItems(
                level.getEntitiesOfClass(ItemEntity.class, createDropScanArea(beastPos)),
                XianqiaoItems.DAO_YUAN_MU_KUANG.get()
            );
            int inventoryDropCount = countInventoryItems(killer, XianqiaoItems.DAO_YUAN_MU_KUANG.get());
            int md10DropCount = worldDropCount + inventoryDropCount;

            ApertureInfo afterInfo = apertureData.getAperture(MD10_HAPPY_PLAYER_UUID);
            helper.assertTrue(afterInfo != null, "happy path: 产出后仙窍信息不应丢失");

            helper.assertTrue(
                md10DropCount == MD10_EXPECTED_COUNT_ONE,
                "happy path: 严格 guard 命中后必须且仅能产出 1 个 M-D10 道源母矿"
                    + "（world=" + worldDropCount + ", inventory=" + inventoryDropCount + "）"
            );
            helper.assertTrue(
                afterInfo.nextTribulationTick() == expectedAdvancedTick,
                "happy path: 命中 M-D10 backup-source 后应精确前推高压灾劫代价"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH2_MAIN
    )
    public void testPlan2MaterialDeepBatch2Md10FailurePathShouldNotDropOrAdvanceWhenOwnerGuardMismatch(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        ServerPlayer killer = createTestPlayer(level, MD10_FAILURE_PLAYER_UUID, MD10_FAILURE_PLAYER_NAME);
        prepareApertureContext(level, killer);

        ServerLevel apertureLevel = level.getServer().getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        ServerLevel tribulationLevel = apertureLevel != null ? apertureLevel : level;
        ApertureWorldData apertureData = ApertureWorldData.get(tribulationLevel);
        long seededTribulationTick = tribulationLevel.getGameTime()
            + MD10_HIGH_PRESSURE_ADVANCE_TICKS
            + MD10_TRIBULATION_FUTURE_BUFFER_TICKS;
        apertureData.allocateAperture(MD10_FAILURE_BOUND_OWNER_UUID);
        apertureData.updateTribulationTick(MD10_FAILURE_BOUND_OWNER_UUID, seededTribulationTick);
        ApertureInfo beforeInfo = apertureData.getAperture(MD10_FAILURE_BOUND_OWNER_UUID);
        helper.assertTrue(beforeInfo != null, "Task24/M-D10: failure path 绑定 owner 仙窍信息缺失");

        BlockPos beastPos = helper.absolutePos(MD10_FAILURE_CALAMITY_BEAST_POS);
        CalamityBeastEntity calamityBeast = createCalamityBeast(helper, MD10_FAILURE_CALAMITY_BEAST_POS);
        calamityBeast.setNoAi(true);
        calamityBeast.bindTribulationOwner(MD10_FAILURE_BOUND_OWNER_UUID);

        boolean hurtApplied = calamityBeast.hurt(level.damageSources().playerAttack(killer), MD10_LETHAL_DAMAGE);
        helper.assertTrue(hurtApplied, "failure path: 需先完成击杀以验证 guard fail-closed");

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            int worldDropCount = countItems(
                level.getEntitiesOfClass(ItemEntity.class, createDropScanArea(beastPos)),
                XianqiaoItems.DAO_YUAN_MU_KUANG.get()
            );
            int inventoryDropCount = countInventoryItems(killer, XianqiaoItems.DAO_YUAN_MU_KUANG.get());
            int md10DropCount = worldDropCount + inventoryDropCount;

            ApertureInfo afterInfo = apertureData.getAperture(MD10_FAILURE_BOUND_OWNER_UUID);
            helper.assertTrue(afterInfo != null, "failure path: guard 拒绝后 owner 仙窍信息不应丢失");

            helper.assertTrue(
                md10DropCount == MD10_EXPECTED_COUNT_ZERO,
                "failure path: owner guard 不匹配时必须 fail-closed，不能产出 M-D10"
            );
            helper.assertTrue(
                afterInfo.nextTribulationTick() == beforeInfo.nextTribulationTick(),
                "failure path: owner guard 不匹配时不得前推高压灾劫代价"
            );
            helper.succeed();
        });
    }

    private static StoneVeinSentinelEntity createStoneVeinSentinel(GameTestHelper helper, BlockPos relativePos) {
        StoneVeinSentinelEntity sentinel = XianqiaoEntities.STONE_VEIN_SENTINEL.get().create(helper.getLevel());
        helper.assertTrue(sentinel != null, "Task24/M-D06: 灵脉石人创建失败");
        BlockPos absolutePos = helper.absolutePos(relativePos);
        sentinel.moveTo(
            absolutePos.getX() + ENTITY_CENTER_OFFSET,
            absolutePos.getY(),
            absolutePos.getZ() + ENTITY_CENTER_OFFSET
        );
        boolean spawned = helper.getLevel().addFreshEntity(sentinel);
        helper.assertTrue(spawned, "Task24/M-D06: 灵脉石人加入世界失败");
        return sentinel;
    }

    private static CalamityBeastEntity createCalamityBeast(GameTestHelper helper, BlockPos relativePos) {
        CalamityBeastEntity calamityBeast = XianqiaoEntities.CALAMITY_BEAST.get().create(helper.getLevel());
        helper.assertTrue(calamityBeast != null, "Task24/M-D10: 灾厄兽创建失败");
        BlockPos absolutePos = helper.absolutePos(relativePos);
        calamityBeast.moveTo(
            absolutePos.getX() + ENTITY_CENTER_OFFSET,
            absolutePos.getY(),
            absolutePos.getZ() + ENTITY_CENTER_OFFSET
        );
        boolean spawned = helper.getLevel().addFreshEntity(calamityBeast);
        helper.assertTrue(spawned, "Task24/M-D10: 灾厄兽加入世界失败");
        return calamityBeast;
    }

    private static AABB createDropScanArea(BlockPos centerPos) {
        return new AABB(
            centerPos.getX() - DROP_SCAN_RADIUS,
            centerPos.getY() - DROP_SCAN_HEIGHT_MARGIN,
            centerPos.getZ() - DROP_SCAN_RADIUS,
            centerPos.getX() + 1 + DROP_SCAN_RADIUS,
            centerPos.getY() + 1 + DROP_SCAN_HEIGHT_MARGIN,
            centerPos.getZ() + 1 + DROP_SCAN_RADIUS
        );
    }

    private static int countItems(List<ItemEntity> drops, Item targetItem) {
        return drops.stream()
            .filter(entity -> entity.getItem().is(targetItem))
            .mapToInt(entity -> entity.getItem().getCount())
            .sum();
    }

    private static int countInventoryItems(ServerPlayer player, Item targetItem) {
        int mainCount = player.getInventory().items.stream()
            .filter(stack -> stack.is(targetItem))
            .mapToInt(ItemStack::getCount)
            .sum();
        int offhandCount = player.getInventory().offhand.stream()
            .filter(stack -> stack.is(targetItem))
            .mapToInt(ItemStack::getCount)
            .sum();
        int armorCount = player.getInventory().armor.stream()
            .filter(stack -> stack.is(targetItem))
            .mapToInt(ItemStack::getCount)
            .sum();
        return mainCount + offhandCount + armorCount;
    }

    private static AABB createWideDropScanArea(BlockPos centerPos, double radius, double heightMargin) {
        return new AABB(
            centerPos.getX() - radius,
            centerPos.getY() - heightMargin,
            centerPos.getZ() - radius,
            centerPos.getX() + 1 + radius,
            centerPos.getY() + 1 + heightMargin,
            centerPos.getZ() + 1 + radius
        );
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID uuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(uuid, playerName));
    }

    private static void prepareApertureContext(ServerLevel level, ServerPlayer player) {
        ApertureWorldData.get(level).allocateAperture(player.getUUID());
        ServerLevel apertureLevel = level.getServer().getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        if (apertureLevel != null) {
            ApertureWorldData.get(apertureLevel).allocateAperture(player.getUUID());
        }
    }

    private static float readApertureTimeSpeed(ServerLevel level, ServerPlayer player) {
        ServerLevel apertureLevel = level.getServer().getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        ServerLevel effectiveLevel = apertureLevel != null ? apertureLevel : level;
        ApertureInfo info = ApertureWorldData.get(effectiveLevel).getAperture(player.getUUID());
        return info == null ? EXPECTED_NORMAL_TIME_SPEED : info.timeSpeed();
    }

    private static void seedMaxHunPoForTest(ServerPlayer player, double maxHunPo) {
        CompoundTag persistentData = player.getPersistentData();
        CompoundTag variables = persistentData.getCompound(MD08_PLAYER_VARIABLES_TAG).copy();
        variables.putDouble(MD08_VAR_MAX_HUNPO, maxHunPo);
        persistentData.put(MD08_PLAYER_VARIABLES_TAG, variables);
    }

    private static void prepareMd08DamageFixture(ServerPlayer player) {
        player.getAbilities().invulnerable = false;
        player.setAbsorptionAmount(0.0F);
        player.setHealth(player.getMaxHealth());
    }
}
