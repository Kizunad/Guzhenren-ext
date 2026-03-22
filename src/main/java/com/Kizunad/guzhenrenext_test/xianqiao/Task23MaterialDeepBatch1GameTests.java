package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.alchemy.blockentity.AlchemyFurnaceBlockEntity;
import com.Kizunad.guzhenrenext.xianqiao.alchemy.service.AlchemyService;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.farming.FarmingBlocks;
import com.Kizunad.guzhenrenext.xianqiao.item.XianqiaoItems;
import com.Kizunad.guzhenrenext.xianqiao.service.ApertureBoundaryService;
import com.Kizunad.guzhenrenext.xianqiao.spirit.XianqiaoEntities;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.ApertureGuardianEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.VoidWalkerEntity;
import com.Kizunad.guzhenrenext.xianqiao.tribulation.TribulationManager;
import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task23MaterialDeepBatch1GameTests {

    private static final String PLAN2_MATERIAL_DEEP_BATCH1_MAIN = "plan2.material.deep.batch1.main";
    private static final int TEST_TIMEOUT_TICKS = 160;
    private static final int ASSERT_DELAY_TICKS = 2;
    private static final int BLOCK_SET_UPDATE_FLAGS = 3;
    private static final float LETHAL_DAMAGE = 999.0F;
    private static final int MIN_REQUIRED_DROP_COUNT = 1;
    private static final double ENTITY_CENTER_OFFSET = 0.5D;
    private static final double DROP_SCAN_RADIUS = 1.5D;
    private static final double DROP_SCAN_HEIGHT_MARGIN = 1.5D;
    private static final double FLOAT_EPSILON = 0.0001D;
    private static final long NIGHT_TIME_TICK = 13000L;
    private static final long DAYLIGHT_TIME_TICK = 1000L;
    private static final int MD03_REQUIRED_TICK_CALLS = 81;
    private static final int MD05_INPUT_STACK_COUNT = 48;
    private static final int MD04_BOUNDARY_RADIUS_BLOCKS = 64;
    private static final int MD04_OUTSIDE_OFFSET_BLOCKS = 4;
    private static final int CHUNK_SIZE_BLOCKS = 16;
    private static final int CHUNK_MAX_OFFSET = CHUNK_SIZE_BLOCKS - 1;
    private static final int MD02_TRIGGER_TICK_CALLS = 1;
    private static final int MD03_TRIGGER_EXTRA_TICKS = 2;
    private static final int MD03_TOTAL_TICK_CALLS = MD03_REQUIRED_TICK_CALLS + MD03_TRIGGER_EXTRA_TICKS;
    private static final int EXTENDED_ASSERT_DELAY_TICKS = ASSERT_DELAY_TICKS + 2;
    private static final int MD03_MINIMUM_STRIKE_DAMAGE = 3;
    private static final long MD04_MAX_OUTSIDE_DISTANCE_SQUARED = 64L;
    private static final double MD04_DROP_SCAN_RADIUS = 4.0D;
    private static final double MD04_DROP_SCAN_HEIGHT_MARGIN = 2.0D;
    private static final double MD02_QIYUN_SEED = 9.0D;
    private static final double MD04_HUNPO_SEED = 9.0D;
    private static final double MD05_HUNPO_SEED = 12.0D;
    private static final BlockPos MD01_HAPPY_GUARDIAN_POS = new BlockPos(2, 2, 2);
    private static final BlockPos MD01_BYPASS_GUARD_GUARDIAN_POS = new BlockPos(6, 2, 2);
    private static final BlockPos MD02_HAPPY_CORE_POS = new BlockPos(10, 2, 2);
    private static final BlockPos MD02_BYPASS_CORE_POS = new BlockPos(14, 2, 2);
    private static final BlockPos MD03_HAPPY_CORE_POS = new BlockPos(2, 2, 6);
    private static final BlockPos MD03_BYPASS_CORE_POS = new BlockPos(6, 2, 6);
    private static final BlockPos MD04_HAPPY_CORE_POS = new BlockPos(10, 2, 6);
    private static final BlockPos MD04_BYPASS_CORE_POS = new BlockPos(14, 2, 6);
    private static final BlockPos MD05_HAPPY_FURNACE_POS = new BlockPos(2, 2, 10);
    private static final BlockPos MD05_BYPASS_GUARD_FURNACE_POS = new BlockPos(6, 2, 10);
    private static final UUID HAPPY_PLAYER_UUID = UUID.fromString("9bcaa2b2-cce2-4354-8e4b-a4ac7fbe37ea");
    private static final UUID MD02_HAPPY_OWNER_UUID = UUID.fromString("ca1d8feb-70be-4f47-9a57-32f90e0fa541");
    private static final UUID MD02_BYPASS_OWNER_UUID = UUID.fromString("90fb7f0d-65b6-4f2c-aa44-be3dc82fb0f2");
    private static final UUID MD03_HAPPY_OWNER_UUID = UUID.fromString("4f298689-86e5-4e7f-a8d5-f2422f0a8306");
    private static final UUID MD03_BYPASS_OWNER_UUID = UUID.fromString("3ca0f5e4-d8db-4401-a43f-a0f2614fd530");
    private static final UUID MD04_HAPPY_OWNER_UUID = UUID.fromString("271fef0a-4e2d-4e04-abb4-3ecf0d96cb64");
    private static final UUID MD04_BYPASS_OWNER_UUID = UUID.fromString("0d5338f1-c95f-44e4-b7b2-f4f37cebf748");
    private static final UUID MD05_HAPPY_REFINER_UUID = UUID.fromString("4fdb8ab8-b85b-4d3d-bcc8-ec9533dca0b7");

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH1_MAIN
    )
    public void testPlan2MaterialDeepBatch1Md01HappyPathShouldDropZhenQiaoXuanTieHe(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ApertureGuardianEntity guardian = createGuardian(helper, MD01_HAPPY_GUARDIAN_POS);
        guardian.setNoAi(true);

        ServerPlayer killer = FakePlayerFactory.get(
            level,
            new GameProfile(HAPPY_PLAYER_UUID, "task23_md01_happy_player")
        );
        boolean hurtApplied = guardian.hurt(level.damageSources().playerAttack(killer), LETHAL_DAMAGE);
        helper.assertTrue(hurtApplied, "happy path: 玩家致死伤害应命中仙窍镇灵");

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            AABB dropArea = createDropScanArea(helper.absolutePos(MD01_HAPPY_GUARDIAN_POS));
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, dropArea);
            int dropCount = countItems(drops, XianqiaoItems.ZHEN_QIAO_XUAN_TIE_HE.get());
            helper.assertTrue(
                dropCount >= MIN_REQUIRED_DROP_COUNT,
                "happy path: 击杀 C-D03 仙窍镇灵后必须掉落 M-D01 镇窍玄铁核"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH1_MAIN
    )
    public void testPlan2MaterialDeepBatch1Md01BypassGuardShouldBlockNonPlayerKillLoot(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ApertureGuardianEntity guardian = createGuardian(helper, MD01_BYPASS_GUARD_GUARDIAN_POS);
        guardian.setNoAi(true);

        boolean hurtApplied = guardian.hurt(level.damageSources().magic(), LETHAL_DAMAGE);
        helper.assertTrue(hurtApplied, "bypass guard: 非玩家致死伤害应能终结实体以验证掉落门槛");

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            AABB dropArea = createDropScanArea(helper.absolutePos(MD01_BYPASS_GUARD_GUARDIAN_POS));
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, dropArea);
            int dropCount = countItems(drops, XianqiaoItems.ZHEN_QIAO_XUAN_TIE_HE.get());
            helper.assertTrue(
                dropCount == 0,
                "bypass guard: 非玩家击杀路径必须被阻断，不能掉落镇窍玄铁核"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH1_MAIN
    )
    public void testPlan2MaterialDeepBatch1Md02HappyPathShouldDropNiMaiXingYunHeAndConsumeQiyun(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        level.setDayTime(NIGHT_TIME_TICK);
        BlockPos corePos = helper.absolutePos(MD02_HAPPY_CORE_POS);

        ApertureWorldData worldData = ApertureWorldData.get(level);
        worldData.allocateAperture(MD02_HAPPY_OWNER_UUID);
        worldData.updateCenter(MD02_HAPPY_OWNER_UUID, corePos);
        ApertureInfo apertureInfo = requireApertureInfo(
            helper,
            worldData,
            MD02_HAPPY_OWNER_UUID,
            "Task23/M-D02: happy path 仙窍信息缺失"
        );

        ServerPlayer owner = createTestPlayer(level, MD02_HAPPY_OWNER_UUID, "task23_md02_happy_owner");
        TribulationManager.seedQiyunAmountForTest(owner, MD02_QIYUN_SEED);
        double qiyunBefore = TribulationManager.readQiyunAmountForTest(level, MD02_HAPPY_OWNER_UUID);

        TribulationManager manager = createStrikeStageManager(MD02_HAPPY_OWNER_UUID);
        for (int i = 0; i < MD02_TRIGGER_TICK_CALLS; i++) {
            manager.tick(level, apertureInfo);
        }

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            AABB scanArea = createDropScanArea(corePos);
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, scanArea);
            int dropCount = countItems(drops, XianqiaoItems.NI_MAI_XING_YUN_HE.get());
            double qiyunAfter = TribulationManager.readQiyunAmountForTest(level, MD02_HAPPY_OWNER_UUID);

            helper.assertTrue(
                dropCount >= MIN_REQUIRED_DROP_COUNT,
                "happy path: 夜间星陨事件必须产出 M-D02 逆脉星陨核"
            );
            helper.assertTrue(
                qiyunAfter + FLOAT_EPSILON < qiyunBefore,
                "happy path: 触发 M-D02 时必须扣减气运，不能无代价产出"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH1_MAIN
    )
    public void testPlan2MaterialDeepBatch1Md02BypassGuardShouldBlockDaytimePath(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        level.setDayTime(DAYLIGHT_TIME_TICK);
        BlockPos corePos = helper.absolutePos(MD02_BYPASS_CORE_POS);

        ApertureWorldData worldData = ApertureWorldData.get(level);
        worldData.allocateAperture(MD02_BYPASS_OWNER_UUID);
        worldData.updateCenter(MD02_BYPASS_OWNER_UUID, corePos);
        ApertureInfo apertureInfo = requireApertureInfo(
            helper,
            worldData,
            MD02_BYPASS_OWNER_UUID,
            "Task23/M-D02: bypass path 仙窍信息缺失"
        );

        ServerPlayer owner = createTestPlayer(level, MD02_BYPASS_OWNER_UUID, "task23_md02_bypass_owner");
        TribulationManager.seedQiyunAmountForTest(owner, MD02_QIYUN_SEED);
        double qiyunBefore = TribulationManager.readQiyunAmountForTest(level, MD02_BYPASS_OWNER_UUID);

        TribulationManager manager = createStrikeStageManager(MD02_BYPASS_OWNER_UUID);
        for (int i = 0; i < MD02_TRIGGER_TICK_CALLS; i++) {
            manager.tick(level, apertureInfo);
        }

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            AABB scanArea = createDropScanArea(corePos);
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, scanArea);
            int dropCount = countItems(drops, XianqiaoItems.NI_MAI_XING_YUN_HE.get());
            double qiyunAfter = TribulationManager.readQiyunAmountForTest(level, MD02_BYPASS_OWNER_UUID);

            helper.assertTrue(
                dropCount == 0,
                "bypass guard: 白天路径必须阻断 M-D02 产出，不能跳过夜空前置"
            );
            helper.assertTrue(
                Math.abs(qiyunAfter - qiyunBefore) <= FLOAT_EPSILON,
                "bypass guard: 未触发事件时不得扣减气运"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH1_MAIN
    )
    public void testPlan2MaterialDeepBatch1Md03HappyPathShouldDropTianLeiCiMuAfterConductorStrikes(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        BlockPos corePos = helper.absolutePos(MD03_HAPPY_CORE_POS);
        BlockPos conductorPos = corePos.above();

        ApertureWorldData worldData = ApertureWorldData.get(level);
        worldData.allocateAperture(MD03_HAPPY_OWNER_UUID);
        worldData.updateCenter(MD03_HAPPY_OWNER_UUID, corePos);
        ApertureInfo apertureInfo = requireApertureInfo(
            helper,
            worldData,
            MD03_HAPPY_OWNER_UUID,
            "Task23/M-D03: happy path 仙窍信息缺失"
        );

        level.setBlock(corePos, Blocks.IRON_BLOCK.defaultBlockState(), BLOCK_SET_UPDATE_FLAGS);
        level.setBlock(conductorPos, Blocks.LIGHTNING_ROD.defaultBlockState(), BLOCK_SET_UPDATE_FLAGS);

        TribulationManager manager = createStrikeStageManager(MD03_HAPPY_OWNER_UUID);
        for (int i = 0; i < MD03_TOTAL_TICK_CALLS; i++) {
            level.setBlock(corePos, Blocks.IRON_BLOCK.defaultBlockState(), BLOCK_SET_UPDATE_FLAGS);
            level.setBlock(conductorPos, Blocks.LIGHTNING_ROD.defaultBlockState(), BLOCK_SET_UPDATE_FLAGS);
            manager.tick(level, apertureInfo);
        }

        helper.assertTrue(
            manager.getDamageAccumulated() >= MD03_MINIMUM_STRIKE_DAMAGE,
            "happy path: M-D03 核心导体路径应至少完成 3 次雷击计数"
        );
        helper.assertTrue(
            manager.isStrikeCoreOutputProducedForTest(),
            "happy path: 核心导体连续雷击后必须触发 M-D03 产出开关"
        );
        int immediateLightningCount = level.getEntitiesOfClass(LightningBolt.class, createDropScanArea(corePos)).size();
        helper.assertTrue(
            immediateLightningCount >= MIN_REQUIRED_DROP_COUNT,
            "happy path: 核心导体路径必须出现真实 LightningBolt，不能是伪造掉落"
        );

        helper.runAfterDelay(EXTENDED_ASSERT_DELAY_TICKS, () -> {
            AABB scanArea = createMd04DropScanArea(corePos);
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, scanArea);
            int dropCount = countItems(drops, XianqiaoItems.TIAN_LEI_CI_MU.get());

            helper.assertTrue(
                dropCount >= MIN_REQUIRED_DROP_COUNT,
                "happy path: 核心导体连续雷击后必须产出 M-D03 天雷磁母"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH1_MAIN
    )
    public void testPlan2MaterialDeepBatch1Md03BypassGuardShouldBlockNoConductorPath(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos corePos = helper.absolutePos(MD03_BYPASS_CORE_POS);
        BlockPos conductorPos = corePos.above();

        ApertureWorldData worldData = ApertureWorldData.get(level);
        worldData.allocateAperture(MD03_BYPASS_OWNER_UUID);
        worldData.updateCenter(MD03_BYPASS_OWNER_UUID, corePos);
        ApertureInfo apertureInfo = requireApertureInfo(
            helper,
            worldData,
            MD03_BYPASS_OWNER_UUID,
            "Task23/M-D03: bypass path 仙窍信息缺失"
        );

        level.setBlock(conductorPos, Blocks.AIR.defaultBlockState(), BLOCK_SET_UPDATE_FLAGS);

        TribulationManager manager = createStrikeStageManager(MD03_BYPASS_OWNER_UUID);
        for (int i = 0; i < MD03_REQUIRED_TICK_CALLS; i++) {
            manager.tick(level, apertureInfo);
        }
        for (int i = 0; i < MD03_TRIGGER_EXTRA_TICKS; i++) {
            manager.tick(level, apertureInfo);
        }

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            AABB scanArea = createDropScanArea(conductorPos);
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, scanArea);
            int dropCount = countItems(drops, XianqiaoItems.TIAN_LEI_CI_MU.get());
            List<LightningBolt> lightningBolts = level.getEntitiesOfClass(LightningBolt.class, scanArea);

            helper.assertTrue(
                dropCount == 0,
                "bypass guard: 缺失核心导体时必须阻断 M-D03 产出，不能走低风险旁路"
            );
            helper.assertTrue(
                lightningBolts.isEmpty(),
                "bypass guard: 缺失核心导体时不应在核心附近出现雷击实体"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH1_MAIN
    )
    public void testPlan2MaterialDeepBatch1Md04HappyPathShouldDropKongShiHeiJingAndConsumeHunPo(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos corePos = helper.absolutePos(MD04_HAPPY_CORE_POS);

        ApertureWorldData worldData = ApertureWorldData.get(level);
        worldData.allocateAperture(MD04_HAPPY_OWNER_UUID);
        worldData.updateCenter(MD04_HAPPY_OWNER_UUID, corePos);
        worldData.updateBoundaryByRadius(MD04_HAPPY_OWNER_UUID, 0);
        requireApertureInfo(helper, worldData, MD04_HAPPY_OWNER_UUID, "Task23/M-D04: happy path 仙窍信息缺失");

        ServerPlayer owner = createTestPlayer(level, MD04_HAPPY_OWNER_UUID, "task23_md04_happy_owner");
        VoidWalkerEntity.seedHunPoAmountForTest(owner, MD04_HUNPO_SEED);
        double hunpoBefore = VoidWalkerEntity.readHunPoAmountForTest(level, MD04_HAPPY_OWNER_UUID);

        ApertureInfo apertureInfo = requireApertureInfo(
            helper,
            worldData,
            MD04_HAPPY_OWNER_UUID,
            "Task23/M-D04: happy path 仙窍信息缺失"
        );
        int maxInsideX = apertureInfo.maxChunkX() * CHUNK_SIZE_BLOCKS + CHUNK_MAX_OFFSET;
        int outsideX = maxInsideX + MD04_OUTSIDE_OFFSET_BLOCKS;
        BlockPos killPos = new BlockPos(outsideX, corePos.getY(), corePos.getZ());
        long outsideDistanceSquared = ApertureBoundaryService.getOutsideDistanceSquared(apertureInfo, killPos);
        helper.assertTrue(
            outsideDistanceSquared > 0L && outsideDistanceSquared <= MD04_MAX_OUTSIDE_DISTANCE_SQUARED,
            "Task23/M-D04: happy path 击杀点必须位于边界外且越界距离平方在合法窗口内"
        );
        level.getChunkAt(killPos);

        VoidWalkerEntity walker = createVoidWalker(helper, killPos);
        walker.setNoAi(true);
        walker.forcePhaseStateForTest();
        boolean hurtApplied = walker.forceKillByPlayerSourceForTest(owner);
        helper.assertTrue(hurtApplied, "happy path: 玩家致死伤害应命中虚行者以触发标准掉落链路");
        BlockPos dropPos = killPos.offset(MD04_OUTSIDE_OFFSET_BLOCKS, 1, 0);

        helper.runAfterDelay(EXTENDED_ASSERT_DELAY_TICKS, () -> {
            level.getChunkAt(dropPos);
            AABB dropArea = createMd04DropScanArea(dropPos);
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, dropArea);
            int dropCount = countItems(drops, XianqiaoItems.KONG_SHI_HEI_JING.get());
            double hunpoAfter = VoidWalkerEntity.readHunPoAmountForTest(level, MD04_HAPPY_OWNER_UUID);

            helper.assertTrue(
                dropCount >= MIN_REQUIRED_DROP_COUNT,
                "happy path: 边界外裂隙位置击杀虚行者必须产出 M-D04 空蚀黑晶"
            );
            helper.assertTrue(
                hunpoAfter + FLOAT_EPSILON < hunpoBefore,
                "happy path: 产出 M-D04 时必须扣减魂魄，体现侵蚀代价"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH1_MAIN
    )
    public void testPlan2MaterialDeepBatch1Md04BypassGuardShouldBlockInsideBoundaryPath(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos corePos = helper.absolutePos(MD04_BYPASS_CORE_POS);

        ApertureWorldData worldData = ApertureWorldData.get(level);
        worldData.allocateAperture(MD04_BYPASS_OWNER_UUID);
        worldData.updateCenter(MD04_BYPASS_OWNER_UUID, corePos);
        worldData.updateBoundaryByRadius(MD04_BYPASS_OWNER_UUID, MD04_BOUNDARY_RADIUS_BLOCKS);
        requireApertureInfo(helper, worldData, MD04_BYPASS_OWNER_UUID, "Task23/M-D04: bypass path 仙窍信息缺失");

        ServerPlayer owner = createTestPlayer(level, MD04_BYPASS_OWNER_UUID, "task23_md04_bypass_owner");
        VoidWalkerEntity.seedHunPoAmountForTest(owner, MD04_HUNPO_SEED);
        double hunpoBefore = VoidWalkerEntity.readHunPoAmountForTest(level, MD04_BYPASS_OWNER_UUID);

        BlockPos killPos = helper.absolutePos(MD04_BYPASS_CORE_POS);
        VoidWalkerEntity walker = createVoidWalker(helper, killPos);
        walker.setNoAi(true);
        walker.forcePhaseStateForTest();
        boolean hurtApplied = walker.forceKillByPlayerSourceForTest(owner);
        helper.assertTrue(hurtApplied, "bypass guard: 玩家致死伤害应命中虚行者以验证边界阻断");
        BlockPos dropPos = killPos;

        helper.runAfterDelay(ASSERT_DELAY_TICKS, () -> {
            AABB dropArea = createMd04DropScanArea(dropPos);
            List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, dropArea);
            int dropCount = countItems(drops, XianqiaoItems.KONG_SHI_HEI_JING.get());
            double hunpoAfter = VoidWalkerEntity.readHunPoAmountForTest(level, MD04_BYPASS_OWNER_UUID);

            helper.assertTrue(
                dropCount == 0,
                "bypass guard: 边界内路径必须阻断 M-D04 产出，不能绕过裂隙前置"
            );
            helper.assertTrue(
                Math.abs(hunpoAfter - hunpoBefore) <= FLOAT_EPSILON,
                "bypass guard: 未触发裂隙采集时不应扣减魂魄"
            );
            helper.succeed();
        });
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH1_MAIN
    )
    public void testPlan2MaterialDeepBatch1Md05HappyPathShouldProduceJiuZhuanSuiJingOnLegalRefineFailure(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        AlchemyFurnaceBlockEntity furnace = placeAlchemyFurnace(helper, MD05_HAPPY_FURNACE_POS);
        injectMd05RefineInputs(furnace);
        ServerPlayer refiner = createTestPlayer(level, MD05_HAPPY_REFINER_UUID, "task23_md05_happy_refiner");
        AlchemyService.seedHunPoAmountForTest(refiner, MD05_HUNPO_SEED);
        double hunpoBefore = AlchemyService.readHunPoAmountForTest(refiner);
        AlchemyService.forceNextRefineFailureForTest(refiner);

        boolean attempted = AlchemyService.tryRefine(furnace, refiner);
        helper.assertTrue(attempted, "happy path: 主材与辅材完整时应发起合法炼制尝试");

        ItemStack output = furnace.getItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT);
        helper.assertTrue(
            output.is(XianqiaoItems.JIU_ZHUAN_SUI_JING.get()),
            "happy path: 合法炼制尝试进入失败分支时必须产出 M-D05 九转髓晶"
        );
        int mainRemaining = furnace.getItem(AlchemyFurnaceBlockEntity.SLOT_MAIN).getCount();
        helper.assertTrue(
            mainRemaining < MD05_INPUT_STACK_COUNT,
            "happy path: 合法炼制失败后必须消耗输入主材，不能无代价产出九转髓晶"
        );
        double hunpoAfter = AlchemyService.readHunPoAmountForTest(refiner);
        helper.assertTrue(
            hunpoAfter + FLOAT_EPSILON < hunpoBefore,
            "happy path: 失败产出九转髓晶时必须扣减魂魄"
        );
        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = PLAN2_MATERIAL_DEEP_BATCH1_MAIN
    )
    public void testPlan2MaterialDeepBatch1Md05BypassGuardShouldBlockNoKeyInputPath(GameTestHelper helper) {
        AlchemyFurnaceBlockEntity furnace = placeAlchemyFurnace(helper, MD05_BYPASS_GUARD_FURNACE_POS);
        furnace.setItem(AlchemyFurnaceBlockEntity.SLOT_AUX_1, new ItemStack(Items.COBBLESTONE, MD05_INPUT_STACK_COUNT));

        boolean attempted = AlchemyService.tryRefine(furnace);
        helper.assertTrue(!attempted, "bypass guard: 缺失主材时不应发起合法炼制尝试");

        ItemStack output = furnace.getItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT);
        helper.assertTrue(
            output.isEmpty(),
            "bypass guard: 无合法尝试路径必须保持无产出，不能旁路产出九转髓晶"
        );
        helper.succeed();
    }

    private static ApertureGuardianEntity createGuardian(GameTestHelper helper, BlockPos relativePos) {
        ApertureGuardianEntity guardian = XianqiaoEntities.APERTURE_GUARDIAN.get().create(helper.getLevel());
        helper.assertTrue(guardian != null, "Task23/M-D01: 仙窍镇灵创建失败");
        BlockPos absolutePos = helper.absolutePos(relativePos);
        guardian.moveTo(
            absolutePos.getX() + ENTITY_CENTER_OFFSET,
            absolutePos.getY(),
            absolutePos.getZ() + ENTITY_CENTER_OFFSET
        );
        helper.getLevel().addFreshEntity(guardian);
        return guardian;
    }

    private static VoidWalkerEntity createVoidWalker(GameTestHelper helper, BlockPos absolutePos) {
        VoidWalkerEntity walker = XianqiaoEntities.VOID_WALKER.get().create(helper.getLevel());
        helper.assertTrue(walker != null, "Task23/M-D04: 虚行者创建失败");
        walker.moveTo(
            absolutePos.getX() + ENTITY_CENTER_OFFSET,
            absolutePos.getY(),
            absolutePos.getZ() + ENTITY_CENTER_OFFSET
        );
        boolean spawned = helper.getLevel().addFreshEntity(walker);
        helper.assertTrue(spawned, "Task23/M-D04: 虚行者加入世界失败");
        return walker;
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new GameProfile(playerUuid, playerName));
    }

    private static AlchemyFurnaceBlockEntity placeAlchemyFurnace(GameTestHelper helper, BlockPos relativePos) {
        BlockPos absolutePos = helper.absolutePos(relativePos);
        helper.getLevel().setBlock(
            absolutePos,
            FarmingBlocks.ALCHEMY_FURNACE.get().defaultBlockState(),
            BLOCK_SET_UPDATE_FLAGS
        );
        BlockEntity blockEntity = helper.getLevel().getBlockEntity(absolutePos);
        helper.assertTrue(blockEntity instanceof AlchemyFurnaceBlockEntity, "Task23/M-D05: 炼丹炉方块实体创建失败");
        return (AlchemyFurnaceBlockEntity) blockEntity;
    }

    private static void injectMd05RefineInputs(AlchemyFurnaceBlockEntity furnace) {
        furnace.setItem(AlchemyFurnaceBlockEntity.SLOT_MAIN, new ItemStack(Items.WHEAT, MD05_INPUT_STACK_COUNT));
        furnace.setItem(AlchemyFurnaceBlockEntity.SLOT_OUTPUT, ItemStack.EMPTY);
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

    private static AABB createMd04DropScanArea(BlockPos centerPos) {
        return new AABB(
            centerPos.getX() - MD04_DROP_SCAN_RADIUS,
            centerPos.getY() - MD04_DROP_SCAN_HEIGHT_MARGIN,
            centerPos.getZ() - MD04_DROP_SCAN_RADIUS,
            centerPos.getX() + 1 + MD04_DROP_SCAN_RADIUS,
            centerPos.getY() + 1 + MD04_DROP_SCAN_HEIGHT_MARGIN,
            centerPos.getZ() + 1 + MD04_DROP_SCAN_RADIUS
        );
    }

    private static int countItems(List<ItemEntity> drops, Item targetItem) {
        return drops.stream()
            .filter(entity -> entity.getItem().is(targetItem))
            .mapToInt(entity -> entity.getItem().getCount())
            .sum();
    }

    private static TribulationManager createStrikeStageManager(UUID owner) {
        TribulationManager manager = new TribulationManager(owner);
        manager.startTribulation();
        manager.advanceState();
        return manager;
    }

    private static ApertureInfo requireApertureInfo(
        GameTestHelper helper,
        ApertureWorldData worldData,
        UUID owner,
        String errorMessage
    ) {
        ApertureInfo info = worldData.getAperture(owner);
        helper.assertTrue(info != null, errorMessage);
        return info;
    }
}
