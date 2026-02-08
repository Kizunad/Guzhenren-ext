package com.Kizunad.guzhenrenext.bastion.test;

import com.Kizunad.guzhenrenext.bastion.BastionBlocks;
import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.block.BastionCoreBlock;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.service.BastionExpansionService;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * Bastion 扩张阶段（Wave 1 & 2）验收测试。
 * <p>
 * 测试目标：
 * <ul>
 *   <li>表面爬行：菌毯不应在纯空气场中扩张</li>
 *   <li>镇地灯保护：6 格范围内应阻断菌毯扩张</li>
 *   <li>资源耗尽：资源不足时镇地灯保护应失效（用于验证回退语义）</li>
 * </ul>
 * </p>
 */
@GameTestHolder("guzhenrenext")
public class BastionExpansionTests {

    /** 通用模板名。 */
    private static final String TEMPLATE_NAMESPACE = "minecraft";
    private static final String EMPTY_TEMPLATE = "empty";

    /** 每秒 tick 数。 */
    private static final int TICKS_PER_SECOND = 20;

    /** 测试统一超时。 */
    private static final int TEST_TIMEOUT_TICKS = 10 * TICKS_PER_SECOND;

    /** 资源值：用于“仅够一次镇地灯拦截”的场景。 */
    private static final double ONE_RESOURCE = 1.0D;

    /** 镇地灯保护半径（与 BastionSavedData 常量语义保持一致）。 */
    private static final int LANTERN_RADIUS = 6;

    /** 扫描半径：用于检查测试区域内是否出现菌毯。 */
    private static final int SCAN_RADIUS = 8;

    /** 地基半宽。 */
    private static final int FLOOR_HALF_WIDTH = 8;

    /** 测试类型 ID：表面爬行。 */
    private static final String TEST_TYPE_SURFACE = "test_expansion_surface";

    /** 测试类型 ID：镇地灯保护。 */
    private static final String TEST_TYPE_LANTERN = "test_expansion_lantern";

    /** 测试类型 ID：资源耗尽。 */
    private static final String TEST_TYPE_DEPLETION = "test_expansion_depletion";

    /** 基地核心相对坐标。 */
    private static final BlockPos CORE_REL = new BlockPos(0, 4, 0);

    /**
     * 场景 1：悬空块测试（Surface Crawling）。
     * <p>
     * 构造纯空气场，只保留核心并封住上下方向，使候选主要落在“远离表面”的水平位置。
     * 期望：菌毯不应在空气中生成。
     * </p>
     */
    @GameTest(templateNamespace = TEMPLATE_NAMESPACE, template = EMPTY_TEMPLATE, timeoutTicks = TEST_TIMEOUT_TICKS)
    public void testSurfaceCrawling(GameTestHelper helper) {
        BastionTypeConfig previous = BastionTypeManager.get(TEST_TYPE_SURFACE);
        BastionTypeConfig testType = createExpansionTestType(
            TEST_TYPE_SURFACE,
            0.0D,
            1,
            2
        );
        BastionTypeManager.register(testType);

        try {
            placeCore(helper, CORE_REL);
            // 封住上下方向，避免垂直候选因为“贴核心面”而误通过。
            helper.setBlock(CORE_REL.above(), Blocks.STONE);
            helper.setBlock(CORE_REL.below(), Blocks.STONE);

            ServerLevel level = helper.getLevel();
            BastionSavedData savedData = BastionSavedData.get(level);
            BastionData bastion = createBastion(helper, level, TEST_TYPE_SURFACE, 20.0D);

            int expanded = BastionExpansionService.tryExpand(level, savedData, bastion, level.getGameTime());
            helper.assertValueEqual(expanded, 0, "悬空场景下不应生成菌毯");
            assertNoMyceliumAround(helper, helper.absolutePos(CORE_REL), SCAN_RADIUS);
            helper.succeed();
        } finally {
            if (previous != null) {
                BastionTypeManager.register(previous);
            }
        }
    }

    /**
     * 场景 2：镇地灯保护测试。
     * <p>
     * 在核心附近放置镇地灯，并将其加入运行时缓存。
     * 期望：灯笼 6 格半径内不应出现菌毯节点。
     * </p>
     */
    @GameTest(templateNamespace = TEMPLATE_NAMESPACE, template = EMPTY_TEMPLATE, timeoutTicks = TEST_TIMEOUT_TICKS)
    public void testLanternProtection(GameTestHelper helper) {
        BastionTypeConfig previous = BastionTypeManager.get(TEST_TYPE_LANTERN);
        BastionTypeConfig testType = createExpansionTestType(
            TEST_TYPE_LANTERN,
            0.0D,
            3,
            1
        );
        BastionTypeManager.register(testType);

        try {
            placeFlatFloor(helper, CORE_REL.getY() - 1, FLOOR_HALF_WIDTH);
            placeCore(helper, CORE_REL);

            ServerLevel level = helper.getLevel();
            BastionSavedData savedData = BastionSavedData.get(level);
            BastionData bastion = createBastion(helper, level, TEST_TYPE_LANTERN, 20.0D);

            BlockPos lanternRel = CORE_REL.relative(Direction.EAST, 2);
            BlockPos lanternAbs = helper.absolutePos(lanternRel);
            helper.setBlock(lanternRel, BastionBlocks.BASTION_WARDING_LANTERN.get().defaultBlockState());
            savedData.addLanternToCache(bastion.id(), lanternAbs);

            BastionData fresh = savedData.getBastion(bastion.id());
            int expanded = BastionExpansionService.tryExpand(level, savedData, fresh, level.getGameTime());
            helper.assertValueEqual(expanded, 0, "镇地灯保护范围内不应扩张成功");
            assertNoMyceliumInLanternRadius(helper, lanternAbs, LANTERN_RADIUS);
            helper.succeed();
        } finally {
            if (previous != null) {
                BastionTypeManager.register(previous);
            }
        }
    }

    /**
     * 场景 3：资源耗尽语义测试。
     * <p>
     * 测试流程：
     * <ol>
     *   <li>资源池=1 时，镇地灯保护可生效（第一次触发：扩张应被拦截）</li>
     *   <li>将资源扣至 0 后，保护失效</li>
     *   <li>再次触发扩张，期望成功（验证“资源不足则保护失效”的语义）</li>
     * </ol>
     * </p>
     */
    @GameTest(templateNamespace = TEMPLATE_NAMESPACE, template = EMPTY_TEMPLATE, timeoutTicks = TEST_TIMEOUT_TICKS)
    public void testResourceDepletion(GameTestHelper helper) {
        BastionTypeConfig previous = BastionTypeManager.get(TEST_TYPE_DEPLETION);
        BastionTypeConfig testType = createExpansionTestType(
            TEST_TYPE_DEPLETION,
            0.0D,
            1,
            1
        );
        BastionTypeManager.register(testType);

        try {
            placeFlatFloor(helper, CORE_REL.getY() - 1, FLOOR_HALF_WIDTH);
            placeCore(helper, CORE_REL);

            ServerLevel level = helper.getLevel();
            BastionSavedData savedData = BastionSavedData.get(level);
            BastionData bastion = createBastion(helper, level, TEST_TYPE_DEPLETION, ONE_RESOURCE);

            BlockPos lanternRel = CORE_REL.relative(Direction.EAST, 2);
            BlockPos lanternAbs = helper.absolutePos(lanternRel);
            helper.setBlock(lanternRel, BastionBlocks.BASTION_WARDING_LANTERN.get().defaultBlockState());
            savedData.addLanternToCache(bastion.id(), lanternAbs);

            BlockPos probeTargetAbs = helper.absolutePos(CORE_REL.relative(Direction.EAST, 1));
            boolean firstProtected = savedData.isProtectedByLantern(bastion.id(), probeTargetAbs);
            helper.assertTrue(firstProtected, "资源池=1 时，第一次触发应被镇地灯拦截");

            // 模拟一次成功拦截后的资源扣减（与扩张服务中的语义一致：每次拦截消耗 1 点资源）。
            BastionData depleted = bastion.withResourcePool(0.0D);
            savedData.updateBastion(depleted);

            boolean protectedAfterDepletion = savedData.isProtectedByLantern(depleted.id(), probeTargetAbs);
            helper.assertTrue(!protectedAfterDepletion, "资源耗尽后，镇地灯保护应失效");

            int expanded = BastionExpansionService.tryExpand(level, savedData, depleted, level.getGameTime());
            helper.assertTrue(expanded > 0, "资源耗尽导致保护失效后，再次触发应允许扩张成功");
            helper.succeed();
        } finally {
            if (previous != null) {
                BastionTypeManager.register(previous);
            }
        }
    }

    /**
     * 放置核心方块。
     */
    private static void placeCore(GameTestHelper helper, BlockPos coreRel) {
        helper.setBlock(
            coreRel,
            BastionBlocks.BASTION_CORE.get().defaultBlockState()
                .setValue(BastionCoreBlock.DAO, BastionDao.ZHI_DAO)
                .setValue(BastionCoreBlock.TIER, 1)
        );
    }

    /**
     * 铺设简易地基，降低“候选因无附着面而随机失败”的噪声。
     */
    private static void placeFlatFloor(GameTestHelper helper, int y, int halfWidth) {
        for (int x = -halfWidth; x <= halfWidth; x++) {
            for (int z = -halfWidth; z <= halfWidth; z++) {
                helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
            }
        }
    }

    /**
     * 创建并注册测试用基地数据。
     */
    private static BastionData createBastion(
            GameTestHelper helper,
            ServerLevel level,
            String bastionType,
            double resourcePool) {
        BastionSavedData savedData = BastionSavedData.get(level);
        BlockPos coreAbs = helper.absolutePos(CORE_REL);

        BastionData bastion = BastionData.create(
            coreAbs,
            level.dimension(),
            bastionType,
            BastionDao.ZHI_DAO,
            level.getGameTime()
        ).withResourcePool(resourcePool);

        savedData.addBastion(bastion);
        savedData.initializeFrontierFromCore(bastion.id(), coreAbs);
        return bastion;
    }

    /**
     * 断言：指定中心附近不应出现任何菌毯节点。
     */
    private static void assertNoMyceliumAround(GameTestHelper helper, BlockPos centerAbs, int radius) {
        ServerLevel level = helper.getLevel();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = centerAbs.offset(x, y, z);
                    if (level.getBlockState(pos).is(BastionBlocks.BASTION_NODE.get())) {
                        helper.fail("检测到悬空菌毯生成: " + pos.toShortString());
                        return;
                    }
                }
            }
        }
    }

    /**
     * 断言：镇地灯保护半径内不应出现菌毯。
     */
    private static void assertNoMyceliumInLanternRadius(GameTestHelper helper, BlockPos lanternAbs, int radius) {
        ServerLevel level = helper.getLevel();
        long radiusSqr = (long) radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = lanternAbs.offset(x, y, z);
                    if (lanternAbs.distSqr(pos) > radiusSqr) {
                        continue;
                    }
                    if (level.getBlockState(pos).is(BastionBlocks.BASTION_NODE.get())) {
                        helper.fail("镇地灯保护区内出现菌毯: " + pos.toShortString());
                        return;
                    }
                }
            }
        }
    }

    /**
     * 创建最小化扩张配置：只覆盖 expansion.mycelium，其他字段走默认值。
     */
    private static BastionTypeConfig createExpansionTestType(
            String id,
            double baseCost,
            int maxPerTick,
            int spacing) {
        String json = "{"
            + "\"id\":\"" + id + "\"," 
            + "\"display_name\":\"" + id + "\"," 
            + "\"primary_dao\":\"zhi_dao\"," 
            + "\"expansion\":{"
            + "\"mycelium\":{"
            + "\"base_cost\":" + baseCost + ","
            + "\"tier_multiplier\":1.0,"
            + "\"max_radius\":16,"
            + "\"max_per_tick\":" + maxPerTick + ","
            + "\"spacing\":" + spacing
            + "}"
            + "}"
            + "}";

        JsonElement element = JsonParser.parseString(json);
        return BastionTypeConfig.CODEC
            .parse(JsonOps.INSTANCE, element)
            .getOrThrow(err -> new IllegalStateException("BastionTypeConfig 解析失败: " + err));
    }
}
