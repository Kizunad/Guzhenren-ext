package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkApi;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoType;
import com.Kizunad.guzhenrenext.xianqiao.spirit.XianqiaoEntities;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.ApertureGuardianEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.CalamityBeastEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.DaoDevouringMiteEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.MimicSlimeEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.MutatedSpiritFoxEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.SacrificialSheepEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.StoneVeinSentinelEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.SymbioticSpiritBeeEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.TreasureMinkEntity;
import com.Kizunad.guzhenrenext.xianqiao.spirit.deep.VoidWalkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class Task11BDeepCreaturesGameTests {

    private static final String TASK11B_BATCH = "task11b_deep_creatures";
    private static final String TASK17_CREATURE_DEEP_BATCH1_MAIN = "plan2.creature.deep.batch1.main";
    private static final String TASK17_CREATURE_DEEP_BATCH1_COST = "plan2.creature.deep.batch1.cost";
    private static final int TEST_TIMEOUT_TICKS = 260;
    private static final int CENTER_X = 4;
    private static final int CENTER_Y = 2;
    private static final int CENTER_Z = 4;
    private static final int ENTITY_SETTLE_TICKS = 80;
    private static final int FAILURE_ASSERT_DELAY_TICKS = 2;
    private static final int TASK17_MAIN_MINK_SECURE_ASSERT_DELAY_TICKS = 140;
    private static final int TASK17_MAIN_ASSERT_DELAY_TICKS = 220;
    private static final int TASK17_COST_ASSERT_DELAY_TICKS = 260;
    private static final float HALF_HEALTH_RATIO = 0.5F;
    private static final int OFFSET_TREASURE_MINK_X = -3;
    private static final int TASK17_MAIN_TREASURE_MINK_OFFSET_Z = -3;
    private static final int OFFSET_MUTATED_FOX_X = -1;
    private static final int OFFSET_SHEEP_X = 2;
    private static final int OFFSET_MITE_X = 4;
    private static final int OFFSET_STONE_SENTINEL_Z = 3;
    private static final int OFFSET_MIMIC_SLIME_X = 2;
    private static final int OFFSET_MIMIC_SLIME_Z = 3;
    private static final int OFFSET_VOID_WALKER_X = 4;
    private static final int OFFSET_VOID_WALKER_Z = 3;
    private static final int OFFSET_CALAMITY_BEAST_X = -2;
    private static final int OFFSET_CALAMITY_BEAST_Z = 3;
    private static final int OFFSET_BEE_X = -4;
    private static final int OFFSET_BEE_Z = 3;
    private static final int OFFSET_DEEP_PLANT_X = -4;
    private static final int OFFSET_DEEP_PLANT_Z = 5;
    private static final double TREASURE_DROP_X = 2.5D;
    private static final int DARK_AURA_INJECTION = 200;
    private static final double MITE_CHECK_RADIUS = 2.0D;
    private static final int EXPECTED_MITE_SPLIT_COUNT = 2;
    private static final double ENTITY_CENTER_OFFSET = 0.5D;
    private static final int OFFSET_FAILURE_MITE_X = 3;
    private static final int OFFSET_FAILURE_SLIME_X = 5;
    private static final int OFFSET_FAILURE_BEE_X = -3;
    private static final int OFFSET_FAILURE_BEE_Z = 2;
    private static final int FAILURE_DARK_AURA_DRAIN_LIMIT = 256;
    private static final int FAILURE_DARK_AURA_DRAIN_STEP = 256;
    private static final int FAILURE_AURA_CLEAR_RADIUS = 1;
    private static final double FAILURE_SPLIT_TARGET_OFFSET = 0.4D;
    private static final double FAILURE_SPLIT_TARGET_HALF_EXTENT = 0.35D;
    private static final String TASK17_MAIN_PLAYER_NAME = "task17_main_player";
    private static final String TASK17_COST_PLAYER_NAME = "task17_cost_player";
    private static final int TASK17_COST_EXPECTED_GUARDIAN_LOCAL_PRESSURE = 64;
    private static final int TASK17_COST_EXPECTED_MITE_AURA_DRAIN = 80;
    private static final int TASK17_COST_FAILURE_MITE_Z_OFFSET = 2;
    private static final float TASK17_COST_EXPECTED_STARVATION_DAMAGE = 2.0F;

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK17_CREATURE_DEEP_BATCH1_MAIN)
    public void testTask17CreatureDeepBatch1MainHappyPathShouldExposeExecutableFeedback(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(new BlockPos(CENTER_X, CENTER_Y, CENTER_Z));

        TreasureMinkEntity treasureMink =
            (TreasureMinkEntity) spawn(
                helper,
                center.offset(OFFSET_TREASURE_MINK_X, 0, TASK17_MAIN_TREASURE_MINK_OFFSET_Z),
                XianqiaoEntities.TREASURE_MINK.get()
            );
        prepareTask17MainTreasureMinkPen(helper, treasureMink.blockPosition());
        ItemEntity treasureDrop = new ItemEntity(
            helper.getLevel(),
            treasureMink.getX(),
            treasureMink.getY(),
            treasureMink.getZ(),
            new ItemStack(Items.DIAMOND)
        );
        treasureDrop.setNoGravity(true);
        treasureDrop.setDeltaMovement(0.0D, 0.0D, 0.0D);
        treasureDrop.setNoPickUpDelay();
        helper.getLevel().addFreshEntity(treasureDrop);
        ServerPlayer testPlayer = createIsolatedFakePlayer(helper, TASK17_MAIN_PLAYER_NAME);

        MutatedSpiritFoxEntity mutatedFox =
            (MutatedSpiritFoxEntity) spawn(
                helper,
                center.offset(OFFSET_MUTATED_FOX_X, 0, 0),
                XianqiaoEntities.MUTATED_SPIRIT_FOX.get()
            );
        testPlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Blocks.FERN));
        mutatedFox.mobInteract(testPlayer, net.minecraft.world.InteractionHand.MAIN_HAND);

        ApertureGuardianEntity guardian =
            (ApertureGuardianEntity) spawn(helper, center, XianqiaoEntities.APERTURE_GUARDIAN.get());
        guardian.setHealth(guardian.getMaxHealth() * HALF_HEALTH_RATIO - 1.0F);
        guardian.aiStep();
        guardian.setNoAi(true);

        SacrificialSheepEntity sacrificialSheep =
            (SacrificialSheepEntity) spawn(
                helper,
                center.offset(OFFSET_SHEEP_X, 0, 0),
                XianqiaoEntities.SACRIFICIAL_SHEEP.get()
            );
        testPlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.NETHER_STAR));
        sacrificialSheep.mobInteract(testPlayer, net.minecraft.world.InteractionHand.MAIN_HAND);

        DaoDevouringMiteEntity mite =
            (DaoDevouringMiteEntity) spawn(
                helper,
                center.offset(OFFSET_MITE_X, 0, 0),
                XianqiaoEntities.DAO_DEVOURING_MITE.get()
            );
        mite.setNoAi(true);
        DaoMarkApi.addAura(helper.getLevel(), mite.blockPosition(), DaoType.DARK, DARK_AURA_INJECTION);
        boolean splitTriggered = mite.runAuraCycleForTest();

        helper.runAfterDelay(TASK17_MAIN_MINK_SECURE_ASSERT_DELAY_TICKS, () ->
            assertTask17MainTreasureMinkSecurePhase(helper, treasureMink)
        );
        helper.runAfterDelay(TASK17_MAIN_ASSERT_DELAY_TICKS, () -> {
            assertTask17MainTreasureMinkBurrowFeedback(helper, treasureMink);
            assertTask17MainFeedbackForMutatedFox(helper, mutatedFox);
            assertTask17MainFeedbackForGuardian(helper, guardian);
            assertTask17MainFeedbackForSheep(helper, sacrificialSheep);
            assertTask17MainFeedbackForMite(helper, mite, splitTriggered);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK17_CREATURE_DEEP_BATCH1_COST)
    public void testTask17CreatureDeepBatch1CostShouldExposeExecutableCostSignals(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(new BlockPos(CENTER_X, CENTER_Y, CENTER_Z));

        TreasureMinkEntity treasureMink =
            (TreasureMinkEntity) spawn(
                helper,
                center.offset(OFFSET_TREASURE_MINK_X, 0, TASK17_MAIN_TREASURE_MINK_OFFSET_Z),
                XianqiaoEntities.TREASURE_MINK.get()
            );
        prepareTask17MainTreasureMinkPen(helper, treasureMink.blockPosition());
        ItemEntity treasureDrop = new ItemEntity(
            helper.getLevel(),
            treasureMink.getX(),
            treasureMink.getY(),
            treasureMink.getZ(),
            new ItemStack(Items.DIAMOND)
        );
        treasureDrop.setNoGravity(true);
        treasureDrop.setDeltaMovement(0.0D, 0.0D, 0.0D);
        treasureDrop.setNoPickUpDelay();
        helper.getLevel().addFreshEntity(treasureDrop);

        ServerPlayer testPlayer = createIsolatedFakePlayer(helper, TASK17_COST_PLAYER_NAME);

        MutatedSpiritFoxEntity mutatedFox =
            (MutatedSpiritFoxEntity) spawn(
                helper,
                center.offset(OFFSET_MUTATED_FOX_X, 0, 0),
                XianqiaoEntities.MUTATED_SPIRIT_FOX.get()
            );
        testPlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.LIGHTNING_ROD));
        mutatedFox.mobInteract(testPlayer, net.minecraft.world.InteractionHand.MAIN_HAND);
        mutatedFox.setNoAi(true);

        ApertureGuardianEntity guardian =
            (ApertureGuardianEntity) spawn(helper, center, XianqiaoEntities.APERTURE_GUARDIAN.get());
        guardian.setHealth(guardian.getMaxHealth() * HALF_HEALTH_RATIO - 1.0F);
        guardian.aiStep();
        guardian.setNoAi(true);

        SacrificialSheepEntity sacrificialSheep =
            (SacrificialSheepEntity) spawn(
                helper,
                center.offset(OFFSET_SHEEP_X, 0, 0),
                XianqiaoEntities.SACRIFICIAL_SHEEP.get()
            );
        testPlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.NETHER_STAR));
        sacrificialSheep.mobInteract(testPlayer, net.minecraft.world.InteractionHand.MAIN_HAND);

        DaoDevouringMiteEntity splitMite =
            (DaoDevouringMiteEntity) spawn(
                helper,
                center.offset(OFFSET_MITE_X, 0, 0),
                XianqiaoEntities.DAO_DEVOURING_MITE.get()
            );
        splitMite.setNoAi(true);
        DaoMarkApi.addAura(helper.getLevel(), splitMite.blockPosition(), DaoType.DARK, DARK_AURA_INJECTION);
        int preSplitDarkAura = DaoMarkApi.getAura(helper.getLevel(), splitMite.blockPosition(), DaoType.DARK);
        boolean splitTriggered = splitMite.runAuraCycleForTest();
        int postSplitDarkAura = DaoMarkApi.getAura(helper.getLevel(), splitMite.blockPosition(), DaoType.DARK);

        DaoDevouringMiteEntity starvingMite =
            (DaoDevouringMiteEntity) spawn(
                helper,
                center.offset(OFFSET_FAILURE_MITE_X, 0, TASK17_COST_FAILURE_MITE_Z_OFFSET),
                XianqiaoEntities.DAO_DEVOURING_MITE.get()
            );
        starvingMite.setNoAi(true);
        drainDarkAuraAroundForTest(helper, starvingMite.blockPosition(), FAILURE_AURA_CLEAR_RADIUS);
        int preStarvationDarkAura = DaoMarkApi.getAura(helper.getLevel(), starvingMite.blockPosition(), DaoType.DARK);
        float starvationHealthBefore = starvingMite.getHealth();
        java.util.Set<java.util.UUID> baselineStarvationMiteIds = collectMiteIdsNearExpectedSplitChild(
            helper,
            starvingMite
        );
        boolean starvationSplitTriggered = starvingMite.runAuraCycleForTest();
        int postStarvationDarkAura = DaoMarkApi.getAura(helper.getLevel(), starvingMite.blockPosition(), DaoType.DARK);

        helper.runAfterDelay(TASK17_COST_ASSERT_DELAY_TICKS, () -> {
            java.util.Set<java.util.UUID> currentStarvationMiteIds = collectMiteIdsNearExpectedSplitChild(
                helper,
                starvingMite
            );
            java.util.Set<java.util.UUID> newStarvationMiteIds = new java.util.HashSet<>(currentStarvationMiteIds);
            newStarvationMiteIds.removeAll(baselineStarvationMiteIds);
            assertTask17CostForTreasureMink(helper, treasureMink);
            assertTask17CostForMutatedFox(helper, mutatedFox);
            assertTask17CostForGuardian(helper, guardian);
            assertTask17CostForSheep(helper, sacrificialSheep);
            assertTask17CostForMiteSplit(helper, splitMite, splitTriggered, preSplitDarkAura, postSplitDarkAura);
            assertTask17CostForMiteStarvation(
                helper,
                starvingMite,
                starvationSplitTriggered,
                preStarvationDarkAura,
                postStarvationDarkAura,
                starvationHealthBefore,
                newStarvationMiteIds
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK11B_BATCH)
    public void testTask11BDeepCreaturesHappyPathShouldApplyMechanics(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(new BlockPos(CENTER_X, CENTER_Y, CENTER_Z));

        TreasureMinkEntity treasureMink =
            (TreasureMinkEntity) spawn(
                helper,
                center.offset(OFFSET_TREASURE_MINK_X, 0, 0),
                XianqiaoEntities.TREASURE_MINK.get()
            );
        helper.getLevel().addFreshEntity(
            new ItemEntity(
                helper.getLevel(),
                center.getX() - TREASURE_DROP_X,
                center.getY(),
                center.getZ(),
                new ItemStack(Items.DIAMOND)
            )
        );
        treasureMink.forceSecureForTest();

        MutatedSpiritFoxEntity mutatedFox =
            (MutatedSpiritFoxEntity) spawn(
                helper,
                center.offset(OFFSET_MUTATED_FOX_X, 0, 0),
                XianqiaoEntities.MUTATED_SPIRIT_FOX.get()
            );
        ServerPlayer testPlayer = FakePlayerFactory.getMinecraft(helper.getLevel());
        testPlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Blocks.FERN));
        mutatedFox.mobInteract(testPlayer, net.minecraft.world.InteractionHand.MAIN_HAND);

        ApertureGuardianEntity guardian =
            (ApertureGuardianEntity) spawn(helper, center, XianqiaoEntities.APERTURE_GUARDIAN.get());
        guardian.setHealth(guardian.getMaxHealth() * HALF_HEALTH_RATIO - 1.0F);
        guardian.aiStep();
        guardian.setNoAi(true);

        SacrificialSheepEntity sacrificialSheep =
            (SacrificialSheepEntity) spawn(
                helper,
                center.offset(OFFSET_SHEEP_X, 0, 0),
                XianqiaoEntities.SACRIFICIAL_SHEEP.get()
            );
        testPlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.NETHER_STAR));
        sacrificialSheep.mobInteract(testPlayer, net.minecraft.world.InteractionHand.MAIN_HAND);

        DaoDevouringMiteEntity mite =
            (DaoDevouringMiteEntity) spawn(
                helper,
                center.offset(OFFSET_MITE_X, 0, 0),
                XianqiaoEntities.DAO_DEVOURING_MITE.get()
            );
        mite.setNoAi(true);
        DaoMarkApi.addAura(helper.getLevel(), mite.blockPosition(), DaoType.DARK, DARK_AURA_INJECTION);
        int postDrainDarkAura = DaoMarkApi.getAura(helper.getLevel(), mite.blockPosition(), DaoType.DARK);
        boolean splitTriggered = mite.runAuraCycleForTest();

        StoneVeinSentinelEntity stoneSentinel =
            (StoneVeinSentinelEntity) spawn(
                helper,
                center.offset(0, 0, OFFSET_STONE_SENTINEL_Z),
                XianqiaoEntities.STONE_VEIN_SENTINEL.get()
            );
        BlockPos sentinelUnder = stoneSentinel.blockPosition().below();
        helper.getLevel().setBlockAndUpdate(sentinelUnder, Blocks.STONE.defaultBlockState());
        stoneSentinel.forceConversionForTest();

        MimicSlimeEntity mimicSlime =
            (MimicSlimeEntity) spawn(
                helper,
                center.offset(OFFSET_MIMIC_SLIME_X, 0, OFFSET_MIMIC_SLIME_Z),
                XianqiaoEntities.MIMIC_SLIME.get()
            );
        mimicSlime.setNoAi(true);
        helper.getLevel().setBlockAndUpdate(mimicSlime.blockPosition().below(), Blocks.LAVA.defaultBlockState());
        mimicSlime.forceElementScanForTest();

        VoidWalkerEntity voidWalker =
            (VoidWalkerEntity) spawn(
                helper,
                center.offset(OFFSET_VOID_WALKER_X, 0, OFFSET_VOID_WALKER_Z),
                XianqiaoEntities.VOID_WALKER.get()
            );
        voidWalker.forcePhaseStateForTest();

        CalamityBeastEntity calamityBeast =
            (CalamityBeastEntity) spawn(
                helper,
                center.offset(OFFSET_CALAMITY_BEAST_X, 0, OFFSET_CALAMITY_BEAST_Z),
                XianqiaoEntities.CALAMITY_BEAST.get()
            );

        SymbioticSpiritBeeEntity spiritBee =
            (SymbioticSpiritBeeEntity) spawn(
                helper,
                center.offset(OFFSET_BEE_X, 0, OFFSET_BEE_Z),
                XianqiaoEntities.SYMBIOTIC_SPIRIT_BEE.get()
            );
        BlockPos deepPlantPos = center.offset(OFFSET_DEEP_PLANT_X, 0, OFFSET_DEEP_PLANT_Z);
        helper.getLevel().setBlockAndUpdate(deepPlantPos.below(), Blocks.DIRT.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(deepPlantPos, Blocks.FERN.defaultBlockState());
        spiritBee.forceDeepInteractionScanForTest();

        helper.runAfterDelay(ENTITY_SETTLE_TICKS, () -> {
            stoneSentinel.forceConversionForTest();
            spiritBee.forceDeepInteractionScanForTest();
            helper.assertTrue(treasureMink.hasValuableLootSecured(), "21 盗宝鼬应完成夺宝标记");
            helper.assertTrue(mutatedFox.isThunderForm(), "22 喂食引雷草后应触发雷霆灵狐");
            helper.assertTrue(guardian.isPhaseTwoActive(), "23 仙窍镇灵半血后应进入二阶段");
            helper.assertTrue(sacrificialSheep.getSpiritReserve() > 0, "24 献祭羊应累积灵蕴值");
            int miteCount = helper.getLevel()
                .getEntitiesOfClass(DaoDevouringMiteEntity.class, mite.getBoundingBox().inflate(MITE_CHECK_RADIUS))
                .size();
            helper.assertTrue(splitTriggered, "25 噬道蛊虫在高道痕区域应命中分裂分支");
            helper.assertTrue(miteCount >= EXPECTED_MITE_SPLIT_COUNT, "25 噬道蛊虫在高道痕区域应触发分裂");
            helper.assertTrue(
                helper.getLevel().getBlockState(sentinelUnder).is(Blocks.COAL_ORE),
                "26 灵脉石人应将脚下石头转化为低级矿石"
            );
            helper.assertTrue(mimicSlime.getElementTag().equals("fire"), "27 拟态史莱姆应根据下方方块更新元素标签");
            helper.assertTrue(voidWalker.noPhysics, "28 虚空漫步者应处于虚化移动状态");
            helper.assertTrue(calamityBeast.isAlive(), "29 灾厄兽应可正常生成并存活");
            helper.assertTrue(spiritBee.getDeepInteractionCount() > 0, "30 共生灵蜂应检测并交互深度植物");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK11B_BATCH)
    public void testTask11BDeepCreaturesFailurePathShouldNotTriggerWhenConditionsMissing(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(new BlockPos(CENTER_X, CENTER_Y, CENTER_Z));

        MutatedSpiritFoxEntity fox =
            (MutatedSpiritFoxEntity) spawn(
                helper,
                center.offset(OFFSET_MUTATED_FOX_X, 0, 0),
                XianqiaoEntities.MUTATED_SPIRIT_FOX.get()
            );
        SacrificialSheepEntity sheep =
            (SacrificialSheepEntity) spawn(
                helper,
                center.offset(1, 0, 0),
                XianqiaoEntities.SACRIFICIAL_SHEEP.get()
            );
        DaoDevouringMiteEntity mite =
            (DaoDevouringMiteEntity) spawn(
                helper,
                center.offset(OFFSET_FAILURE_MITE_X, 0, 0),
                XianqiaoEntities.DAO_DEVOURING_MITE.get()
            );
        mite.setNoAi(true);
        MimicSlimeEntity slime =
            (MimicSlimeEntity) spawn(
                helper,
                center.offset(OFFSET_FAILURE_SLIME_X, 0, 0),
                XianqiaoEntities.MIMIC_SLIME.get()
            );
        slime.setNoAi(true);
        helper.getLevel().setBlockAndUpdate(slime.blockPosition().below(), Blocks.STONE.defaultBlockState());
        slime.forceElementScanForTest();
        SymbioticSpiritBeeEntity bee =
            (SymbioticSpiritBeeEntity) spawn(
                helper,
                center.offset(OFFSET_FAILURE_BEE_X, 0, OFFSET_FAILURE_BEE_Z),
                XianqiaoEntities.SYMBIOTIC_SPIRIT_BEE.get()
            );
        bee.forceDeepInteractionScanForTest();

        drainDarkAuraAroundForTest(helper, mite.blockPosition(), FAILURE_AURA_CLEAR_RADIUS);
        int postDrainDarkAura = DaoMarkApi.getAura(helper.getLevel(), mite.blockPosition(), DaoType.DARK);
        java.util.Set<java.util.UUID> baselineMiteIds = collectMiteIdsNearExpectedSplitChild(helper, mite);
        boolean splitTriggered = mite.runAuraCycleForTest();

        helper.runAfterDelay(FAILURE_ASSERT_DELAY_TICKS, () -> {
            helper.assertTrue(!fox.isThunderForm(), "failure: 变异灵狐缺少喂食条件时不能转化");
            helper.assertTrue(sheep.getSpiritReserve() == 0, "failure: 献祭羊未喂食时灵蕴应为 0");
            java.util.Set<java.util.UUID> currentMiteIds = collectMiteIdsNearExpectedSplitChild(helper, mite);
            java.util.Set<java.util.UUID> newMiteIds = new java.util.HashSet<>(currentMiteIds);
            newMiteIds.removeAll(baselineMiteIds);
            helper.assertTrue(postDrainDarkAura == 0, "failure: 失败路径前置校验异常，暗道痕未清空");
            helper.assertTrue(!splitTriggered, "failure: 道痕不足时噬道蛊虫不应命中分裂分支");
            helper.assertTrue(newMiteIds.isEmpty(), "failure: 道痕不足时噬道蛊虫不应分裂");
            helper.assertTrue(slime.getElementTag().equals("neutral"), "failure: 普通方块下拟态史莱姆应保持 neutral");
            helper.assertTrue(bee.getDeepInteractionCount() == 0, "failure: 无深度植物时灵蜂交互计数应为 0");
            helper.succeed();
        });
    }

    private static net.minecraft.world.entity.Entity spawn(
        GameTestHelper helper,
        BlockPos pos,
        net.minecraft.world.entity.EntityType<?> type
    ) {
        net.minecraft.world.entity.Entity entity = type.create(helper.getLevel());
        helper.assertTrue(entity != null, "Task11B: 深度生灵创建失败: " + type);
        entity.moveTo(pos.getX() + ENTITY_CENTER_OFFSET, pos.getY(), pos.getZ() + ENTITY_CENTER_OFFSET);
        helper.getLevel().addFreshEntity(entity);
        return entity;
    }

    private static void drainDarkAuraForTest(GameTestHelper helper, BlockPos position) {
        for (int i = 0; i < FAILURE_DARK_AURA_DRAIN_LIMIT; i++) {
            int currentAura = DaoMarkApi.getAura(helper.getLevel(), position, DaoType.DARK);
            if (currentAura <= 0) {
                return;
            }
            int consumeAmount = Math.min(currentAura, FAILURE_DARK_AURA_DRAIN_STEP);
            if (!DaoMarkApi.consumeAura(helper.getLevel(), position, DaoType.DARK, consumeAmount)) {
                return;
            }
        }
    }

    private static void drainDarkAuraAroundForTest(GameTestHelper helper, BlockPos center, int radius) {
        for (int offsetX = -radius; offsetX <= radius; offsetX++) {
            for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                drainDarkAuraForTest(helper, center.offset(offsetX, 0, offsetZ));
            }
        }
    }

    private static ServerPlayer createIsolatedFakePlayer(GameTestHelper helper, String token) {
        java.util.UUID playerId = java.util.UUID.nameUUIDFromBytes(
            token.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
        return FakePlayerFactory.get(
            helper.getLevel(),
            new com.mojang.authlib.GameProfile(playerId, token)
        );
    }

    private static void prepareTask17MainTreasureMinkPen(GameTestHelper helper, BlockPos minkPos) {
        helper.getLevel().setBlockAndUpdate(minkPos.below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(minkPos.north().below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(minkPos.south().below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(minkPos.east().below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(minkPos.west().below(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(minkPos.north(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(minkPos.south(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(minkPos.east(), Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(minkPos.west(), Blocks.STONE.defaultBlockState());
    }

    private static void assertTask17MainTreasureMinkSecurePhase(
        GameTestHelper helper,
        TreasureMinkEntity treasureMink
    ) {
        helper.assertTrue(treasureMink.hasValuableLootSecured(), "Task17 main C-D01: 盗宝鼬应完成真实夺宝");
        helper.assertTrue(
            treasureMink.getSecureSuccessCountForTest() == 1,
            "Task17 main C-D01: 盗宝鼬夺宝成功计数应为 1"
        );
        helper.assertTrue(
            treasureMink.wasSecureNameTagVisibleForTest(),
            "Task17 main C-D01: 盗宝鼬应记录成功 NameTag 可见"
        );
    }

    private static void assertTask17MainTreasureMinkBurrowFeedback(
        GameTestHelper helper,
        TreasureMinkEntity treasureMink
    ) {
        helper.assertTrue(
            treasureMink.getBurrowParticleTriggerCountForTest() == 1,
            "Task17 main C-D01: 盗宝鼬埋洞粒子触发计数应为 1"
        );
        helper.assertTrue(
            treasureMink.getBurrowSoundTriggerCountForTest() == 1,
            "Task17 main C-D01: 盗宝鼬原版音效触发计数应为 1"
        );
    }

    private static void assertTask17MainFeedbackForMutatedFox(
        GameTestHelper helper,
        MutatedSpiritFoxEntity mutatedFox
    ) {
        helper.assertTrue(mutatedFox.isThunderForm(), "Task17 main C-D02: 变异灵狐应进入雷霆形态");
        helper.assertTrue(
            mutatedFox.getThunderFormTransformCountForTest() == 1,
            "Task17 main C-D02: 变异灵狐转化成功计数应为 1"
        );
        helper.assertTrue(
            mutatedFox.wasThunderFormNameTagVisibleForTest(),
            "Task17 main C-D02: 变异灵狐应记录成功 NameTag 可见"
        );
        helper.assertTrue(
            mutatedFox.getThunderFormParticleTriggerCountForTest() == 1,
            "Task17 main C-D02: 变异灵狐粒子触发计数应为 1"
        );
        helper.assertTrue(
            mutatedFox.getThunderFormSoundTriggerCountForTest() == 1,
            "Task17 main C-D02: 变异灵狐原版音效触发计数应为 1"
        );
    }

    private static void assertTask17MainFeedbackForGuardian(GameTestHelper helper, ApertureGuardianEntity guardian) {
        helper.assertTrue(guardian.isPhaseTwoActive(), "Task17 main C-D03: 仙窍镇灵应进入二阶段");
        helper.assertTrue(
            guardian.getPhaseTwoActivationPulseCountForTest() == 1,
            "Task17 main C-D03: 仙窍镇灵二阶段激活计数应为 1"
        );
        helper.assertTrue(
            guardian.wasPhaseTwoNameTagVisibleForTest(),
            "Task17 main C-D03: 仙窍镇灵应记录二阶段 NameTag 可见"
        );
        helper.assertTrue(
            guardian.getPhaseTwoParticleTriggerCountForTest() == 1,
            "Task17 main C-D03: 仙窍镇灵粒子触发计数应为 1"
        );
        helper.assertTrue(
            guardian.getPhaseTwoSoundTriggerCountForTest() == 1,
            "Task17 main C-D03: 仙窍镇灵原版音效触发计数应为 1"
        );
    }

    private static void assertTask17MainFeedbackForSheep(
        GameTestHelper helper,
        SacrificialSheepEntity sacrificialSheep
    ) {
        helper.assertTrue(sacrificialSheep.getSpiritReserve() > 0, "Task17 main C-D04: 献祭羊应累积灵蕴");
        helper.assertTrue(
            sacrificialSheep.getSuccessfulFeedCountForTest() == 1,
            "Task17 main C-D04: 献祭羊成功喂食计数应为 1"
        );
        helper.assertTrue(
            sacrificialSheep.wasSuccessfulFeedNameTagVisibleForTest(),
            "Task17 main C-D04: 献祭羊应记录成功 NameTag 可见"
        );
        helper.assertTrue(
            sacrificialSheep.getSuccessfulFeedParticleTriggerCountForTest() == 1,
            "Task17 main C-D04: 献祭羊粒子触发计数应为 1"
        );
        helper.assertTrue(
            sacrificialSheep.getSuccessfulFeedSoundTriggerCountForTest() == 1,
            "Task17 main C-D04: 献祭羊原版音效触发计数应为 1"
        );
    }

    private static void assertTask17MainFeedbackForMite(
        GameTestHelper helper,
        DaoDevouringMiteEntity mite,
        boolean splitTriggered
    ) {
        helper.assertTrue(splitTriggered, "Task17 main C-D05: 噬道蛊虫应命中分裂分支");
        helper.assertTrue(
            mite.getSplitSuccessCountForTest() == 1,
            "Task17 main C-D05: 噬道蛊虫分裂成功计数应为 1"
        );
        helper.assertTrue(
            mite.wasSplitSuccessNameTagVisibleForTest(),
            "Task17 main C-D05: 噬道蛊虫应记录成功 NameTag 可见"
        );
        helper.assertTrue(
            mite.getSplitParticleTriggerCountForTest() == 1,
            "Task17 main C-D05: 噬道蛊虫粒子触发计数应为 1"
        );
        helper.assertTrue(
            mite.getSplitSoundTriggerCountForTest() == 1,
            "Task17 main C-D05: 噬道蛊虫原版音效触发计数应为 1"
        );
    }

    private static void assertTask17CostForTreasureMink(GameTestHelper helper, TreasureMinkEntity treasureMink) {
        helper.assertTrue(
            treasureMink.hasLocalMaterialLossCostSnapshotForTest(),
            "Task17 cost C-D01: 盗宝鼬应记录局部材料损失快照"
        );
        helper.assertTrue(
            treasureMink.getLastSecuredLootSnapshotForTest().is(Items.DIAMOND),
            "Task17 cost C-D01: 盗宝鼬应记录被卷走的钻石快照"
        );
        helper.assertTrue(
            treasureMink.hasBurrowedAwayWithLootForTest(),
            "Task17 cost C-D01: 盗宝鼬应带着赃物埋洞离场"
        );
    }

    private static void assertTask17CostForMutatedFox(GameTestHelper helper, MutatedSpiritFoxEntity mutatedFox) {
        helper.assertTrue(
            mutatedFox.isHostileMutationStateForTest(),
            "Task17 cost C-D02: 变异灵狐失败诱变后应进入敌对态"
        );
        helper.assertTrue(
            !mutatedFox.isThunderForm(),
            "Task17 cost C-D02: 失败诱变后不应仍然进入雷霆形态"
        );
        helper.assertTrue(
            mutatedFox.getHostileMutationTriggerCountForTest() == 1,
            "Task17 cost C-D02: 变异灵狐敌对态触发计数应为 1"
        );
        helper.assertTrue(
            mutatedFox.wasHostileTargetAssignedForTest(),
            "Task17 cost C-D02: 变异灵狐失败诱变后应锁定仇恨目标"
        );
        helper.assertTrue(
            mutatedFox.wasHostileMutationNameTagVisibleForTest(),
            "Task17 cost C-D02: 变异灵狐失败诱变后应记录敌对 NameTag 可见"
        );
    }

    private static void assertTask17CostForGuardian(GameTestHelper helper, ApertureGuardianEntity guardian) {
        helper.assertTrue(
            guardian.hasLocalPressureCostSnapshotForTest(),
            "Task17 cost C-D03: 仙窍镇灵应记录本地压力代价快照"
        );
        helper.assertTrue(
            guardian.getLastLocalPressureCostSnapshotForTest() == TASK17_COST_EXPECTED_GUARDIAN_LOCAL_PRESSURE,
            "Task17 cost C-D03: 仙窍镇灵本地压力代价快照应为 64"
        );
    }

    private static void assertTask17CostForSheep(GameTestHelper helper, SacrificialSheepEntity sacrificialSheep) {
        helper.assertTrue(sacrificialSheep.getSpiritReserve() > 0, "Task17 cost C-D04: 献祭羊应真实完成供奉");
        helper.assertTrue(
            sacrificialSheep.getMoralityOffsetFromSacrificeForTest() == -1,
            "Task17 cost C-D04: 献祭羊应累计一次局部道德代价"
        );
    }

    private static void assertTask17CostForMiteSplit(
        GameTestHelper helper,
        DaoDevouringMiteEntity splitMite,
        boolean splitTriggered,
        int preSplitDarkAura,
        int postSplitDarkAura
    ) {
        helper.assertTrue(splitTriggered, "Task17 cost C-D05: 噬道蛊虫应命中消耗道痕的分裂分支");
        helper.assertTrue(
            preSplitDarkAura - postSplitDarkAura == TASK17_COST_EXPECTED_MITE_AURA_DRAIN,
            "Task17 cost C-D05: 噬道蛊虫成功分裂应精确抽干 80 点暗道痕"
        );
        helper.assertTrue(
            splitMite.getSplitSuccessCountForTest() == 1,
            "Task17 cost C-D05: 噬道蛊虫成功分裂计数应为 1"
        );
    }

    private static void assertTask17CostForMiteStarvation(
        GameTestHelper helper,
        DaoDevouringMiteEntity starvingMite,
        boolean starvationSplitTriggered,
        int preStarvationDarkAura,
        int postStarvationDarkAura,
        float starvationHealthBefore,
        java.util.Set<java.util.UUID> newStarvationMiteIds
    ) {
        helper.assertTrue(preStarvationDarkAura == 0, "Task17 cost C-D05: 饥饿路径前置校验异常，暗道痕未清空");
        helper.assertTrue(!starvationSplitTriggered, "Task17 cost C-D05: 暗道痕不足时不应进入分裂分支");
        helper.assertTrue(
            postStarvationDarkAura == preStarvationDarkAura,
            "Task17 cost C-D05: 饥饿路径不应额外抽干暗道痕"
        );
        helper.assertTrue(
            starvingMite.getHealth() < starvationHealthBefore,
            "Task17 cost C-D05: 饥饿路径应触发真实饥饿伤害"
        );
        helper.assertTrue(newStarvationMiteIds.isEmpty(), "Task17 cost C-D05: 饥饿路径应 fail-closed，不得生成新个体");
    }

    private static java.util.Set<java.util.UUID> collectMiteIdsNearExpectedSplitChild(
        GameTestHelper helper,
        DaoDevouringMiteEntity anchorMite
    ) {
        AABB splitSpawnArea = new AABB(
            anchorMite.getX() + FAILURE_SPLIT_TARGET_OFFSET - FAILURE_SPLIT_TARGET_HALF_EXTENT,
            anchorMite.getY() - FAILURE_SPLIT_TARGET_HALF_EXTENT,
            anchorMite.getZ() + FAILURE_SPLIT_TARGET_OFFSET - FAILURE_SPLIT_TARGET_HALF_EXTENT,
            anchorMite.getX() + FAILURE_SPLIT_TARGET_OFFSET + FAILURE_SPLIT_TARGET_HALF_EXTENT,
            anchorMite.getY() + FAILURE_SPLIT_TARGET_HALF_EXTENT,
            anchorMite.getZ() + FAILURE_SPLIT_TARGET_OFFSET + FAILURE_SPLIT_TARGET_HALF_EXTENT
        );
        return helper.getLevel()
            .getEntitiesOfClass(
                DaoDevouringMiteEntity.class,
                splitSpawnArea
            )
            .stream()
            .map(net.minecraft.world.entity.Entity::getUUID)
            .collect(java.util.stream.Collectors.toSet());
    }
}
