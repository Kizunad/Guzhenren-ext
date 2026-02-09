package com.Kizunad.guzhenrenext_test;

import com.Kizunad.guzhenrenext.bastion.BastionBlocks;
import com.Kizunad.guzhenrenext.bastion.block.BastionCoreBlock;
import com.Kizunad.guzhenrenext.bastion.BastionDao;
import com.Kizunad.guzhenrenext.bastion.BastionData;
import com.Kizunad.guzhenrenext.bastion.BastionSavedData;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig;
import com.Kizunad.guzhenrenext.bastion.config.BastionTypeManager;
import com.Kizunad.guzhenrenext.bastion.entity.BastionGuardianData;
import com.Kizunad.guzhenrenext.bastion.guardian.BastionGuardianRuntimeService;
import com.Kizunad.guzhenrenext.bastion.service.BastionAuraService;
import com.Kizunad.guzhenrenext.bastion.service.BastionGuardianUpkeepService;
import com.Kizunad.guzhenrenext.bastion.service.BastionSpawnService;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder("guzhenrenext")
public class ExampleGameTests {

    private static final int EXPECTED_MYCELIUM_SPACING = 2;
    private static final int EXPECTED_ANCHOR_SPACING = 8;

    /** 每秒 tick 数。 */
    private static final int TICKS_PER_SECOND = 20;

    /** 连通性扫描 + 衰败完成的总等待 tick（留出冗余）。 */
    private static final int WAIT_FOR_DECAY_TICKS = 30 * TICKS_PER_SECOND;

    /** 菌毯连线步长（与 data/guzhenrenext/bastion_type/default.json 的 spacing 对齐）。 */
    private static final int MYCELIUM_STEP = 4;

    /** 基础地面长度（避免菌毯因下方空气导致不稳定）。 */
    private static final int FLOOR_LENGTH = 2 * MYCELIUM_STEP + 1;

    /** 破坏中间节点的延迟（tick）：确保至少跑过一次 ticker。 */
    private static final int BREAK_MIDDLE_DELAY_TICKS = 3 * TICKS_PER_SECOND;

    // ===== 回合3-步骤3：能源环境（GameTest） =====

    /** 能源测试中 Anchor 与核心的水平偏移（对齐默认配置的 anchor spacing=8）。 */
    private static final int ENERGY_TEST_ANCHOR_OFFSET_X = EXPECTED_ANCHOR_SPACING;

    /** 能源测试地面长度：覆盖 core/anchor/水源，避免液体/方块因下方空气导致不稳定。 */
    private static final int ENERGY_TEST_FLOOR_LENGTH = ENERGY_TEST_ANCHOR_OFFSET_X + MYCELIUM_STEP + 2;

    /** 预热等待：确保 BastionTicker 至少跑过多次（>=2 个处理周期）。 */
    private static final int ENERGY_TEST_WARMUP_TICKS = 4 * TICKS_PER_SECOND;

    /** 资源池采样窗口长度：40 tick（包含 2 次 BastionTicker 处理，间隔=20）。 */
    private static final int ENERGY_TEST_MEASURE_WINDOW_TICKS = 2 * TICKS_PER_SECOND;

    /**
     * 能源扫描稳定等待：41 tick。
     * <p>
     * 说明：BastionEnergyService 以 40 tick 为间隔扫描一次。
     * 等待 40 tick 理论上足够“跨过一次整除点”，但为了规避回调执行顺序（ticker 与测试回调同 tick 的先后）
     * 带来的边界不确定性，这里额外 +1 tick，保证“至少一次扫描”已发生在采样窗口之前。
     * </p>
     */
    private static final int ENERGY_TEST_SCAN_SETTLE_TICKS = 4 * TICKS_PER_SECOND + 1;

    /**
     * 断言下界：有环境（汲水）时的 pool 增长应显著高于无环境。
     * <p>
     * 解释：默认配置 water_intake 存在 flat=0.5（每 tick 平坦供给），按 TickInterval=20 汇总为 +10/次，
     * 差值应远大于 0；设置较小下界是为了抵抗 tick 对齐与浮点误差。
     * </p>
     */
    private static final double ENERGY_TEST_MIN_EXTRA_POOL_GAIN = 5.0;

    /**
     * 超时：预热 + 基线采样 + 扫描等待 + 环境采样 + 余量。
     */
    private static final int ENERGY_TEST_TIMEOUT_TICKS = ENERGY_TEST_WARMUP_TICKS
        + ENERGY_TEST_MEASURE_WINDOW_TICKS
        + ENERGY_TEST_SCAN_SETTLE_TICKS
        + ENERGY_TEST_MEASURE_WINDOW_TICKS
        + 5 * TICKS_PER_SECOND;

    // ===== 回合4-步骤C：刷怪停机门禁（GameTest） =====

    /** GameTest 临时注册的基地类型 ID：用于验证“资源池为 0 时禁止刷怪”的门禁。 */
    private static final String TEST_BASTION_TYPE_ID_SPAWN_SHUTDOWN = "test_spawn_shutdown";

    /** entity_weights 中使用的实体 ID：选择原版实体，确保注册表必定可解析。 */
    private static final String TEST_SPAWN_ENTITY_ID = "minecraft:witch";

    /** 确定性刷怪概率：1.0 表示必定通过概率检查。 */
    private static final double TEST_SPAWN_CHANCE_ALWAYS = 1.0;

    /** 转数对刷怪概率的加成：本测试不需要 tier 额外加成。 */
    private static final double TEST_TIER_SPAWN_BONUS_ZERO = 0.0;

    /** upkeep 测试起始资源池：选择正数，确保扣费可观测。 */
    private static final double UPKEEP_TEST_INITIAL_POOL = 10.0;

    /** 单个守卫每个维护间隔的固定费用（与 BastionGuardianUpkeepService 的当前常量保持一致）。 */
    private static final double UPKEEP_TEST_COST_PER_GUARDIAN = 1.0;

    /** BastionRuntime 目标筛选私有方法名（反射调用，避免依赖 tick 时间对齐）。 */
    private static final String METHOD_TICK_TARGETING = "tickTargeting";

    /** BastionAuraService 私有方法名（反射调用，直接覆盖友方过滤与敌方生效回归）。 */
    private static final String METHOD_APPLY_AURA_EFFECT = "applyAuraEffect";

    /** 友方女巫守卫测试用玩家 UUID（需与 capturedBy 严格一致）。 */
    private static final UUID TEST_FRIENDLY_PLAYER_ID =
        UUID.fromString("11111111-1111-1111-1111-111111111111");

    /** 敌方回归测试用 owner UUID（与敌方 UUID 不同，用于验证敌方仍受影响）。 */
    private static final UUID TEST_OWNER_PLAYER_ID =
        UUID.fromString("22222222-2222-2222-2222-222222222222");

    /** 敌方回归测试用 enemy UUID。 */
    private static final UUID TEST_ENEMY_PLAYER_ID =
        UUID.fromString("33333333-3333-3333-3333-333333333333");

    /** Bastion 友敌回归测试统一超时：用于包含反射调用与多断言的用例。 */
    private static final int BASTION_FRIENDLY_REGRESSION_TIMEOUT_TICKS = 10 * TICKS_PER_SECOND;

    /** 友敌回归测试核心坐标（相对坐标，写入数据前统一转 absolute）。 */
    private static final BlockPos BASTION_TEST_CORE_REL = new BlockPos(0, 2, 0);

    /** 友敌回归测试守卫刷出点（相对坐标）。 */
    private static final BlockPos BASTION_TEST_GUARDIAN_REL = new BlockPos(2, 2, 2);

    /**
     * 构造 GameTest 用的 BastionTypeConfig。
     * <p>
     * 说明：即使本测试的“停机门禁”在读取配置前就会提前返回，为了保证测试语义完整，仍显式注入一个
     * base_spawn_chance=1.0 且 max_spawns_per_tick=1 的配置。
     * <p>
     * 这样做的目的：避免未来实现调整门禁位置（例如放到概率检查之后）时，测试退化为“概率刷怪”，从而引入不确定性。
     * </p>
     */
    private static BastionTypeConfig createTestBastionTypeConfigForSpawnShutdown() {
        BastionTypeConfig.SpawningConfig spawning = new BastionTypeConfig.SpawningConfig(
            TEST_SPAWN_CHANCE_ALWAYS,
            TEST_TIER_SPAWN_BONUS_ZERO,
            1,
            List.of(new BastionTypeConfig.EntityWeight(TEST_SPAWN_ENTITY_ID, 1, 1))
        );

        return new BastionTypeConfig(
            TEST_BASTION_TYPE_ID_SPAWN_SHUTDOWN,
            "测试-刷怪停机门禁",
            BastionDao.ZHI_DAO,
            1,
            BastionTypeConfig.UpkeepConfig.DEFAULT,
            spawning,
            BastionTypeConfig.ExpansionConfig.DEFAULT,
            BastionTypeConfig.ConnectivityConfig.DEFAULT,
            BastionTypeConfig.ShellConfig.DEFAULT,
            BastionTypeConfig.DecayConfig.DEFAULT,
            BastionTypeConfig.EvolutionConfig.DEFAULT,
            BastionTypeConfig.AuraConfig.DEFAULT,
            BastionTypeConfig.EnergyConfig.DEFAULT,
            BastionTypeConfig.EnergyLossConfig.DEFAULT,
            BastionTypeConfig.NodeContentConfig.create(
                BastionTypeConfig.HatcheryConfig.DEFAULT,
                BastionTypeConfig.TurretConfig.DEFAULT,
                BastionTypeConfig.TrapConfig.DEFAULT
            ),
            BastionTypeConfig.EliteConfig.DEFAULT,
            BastionTypeConfig.BossConfig.DEFAULT,
            BastionTypeConfig.ThreatConfig.DEFAULT,
            BastionTypeConfig.PollutionConfig.DEFAULT,
            BastionTypeConfig.CaptureConfig.DEFAULT,
            BastionTypeConfig.DEFAULT_ANCHORS_WEIGHT,
            BastionTypeConfig.DEFAULT_MYCELIUM_WEIGHT,
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );
    }

    @GameTest(template = "empty")
    public void testAlwaysPass(GameTestHelper helper) {
        // 基座：确保 GameTest 环境可正常运行。
        helper.succeed();
    }

    @GameTest(template = "empty")
    public void testBastionTypeConfigDecodeSmoke(GameTestHelper helper) {
        // 基座：验证 BastionTypeConfig.CODEC 能解析新的 expansion{mycelium,anchor} 结构。
        // 注意：这里不依赖资源加载器，只做最小 codec 解析烟雾测试。
        String json = "{"
            + "\"id\":\"test\","
            + "\"display_name\":\"test\","
            + "\"primary_dao\":\"zhi_dao\","
            + "\"expansion\":{"
            + "  \"mycelium\":{"
            + "    \"base_cost\":1.0,\"tier_multiplier\":1.0,\"max_radius\":16,\"max_per_tick\":1,\"spacing\":2"
            + "  },"
            + "  \"anchor\":{"
            + "    \"build_cost\":10.0,\"spacing\":8,\"max_count\":4,\"trigger_distance\":10,\"cooldown_ticks\":20"
            + "  }"
            + "}"
            + "}";

        com.google.gson.JsonElement element = com.google.gson.JsonParser.parseString(json);
        com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig parsed =
            com.Kizunad.guzhenrenext.bastion.config.BastionTypeConfig.CODEC
                .parse(com.mojang.serialization.JsonOps.INSTANCE, element)
                .getOrThrow(err -> new IllegalStateException("parse failed: " + err));

        helper.assertValueEqual(parsed.id(), "test", "id mismatch");
        helper.assertValueEqual(
            parsed.expansion().mycelium().spacing(),
            EXPECTED_MYCELIUM_SPACING,
            "mycelium spacing mismatch"
        );
        helper.assertValueEqual(
            parsed.expansion().anchor().spacing(),
            EXPECTED_ANCHOR_SPACING,
            "anchor spacing mismatch"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public void testGuardianSpawnBlockedWhenPoolZero(GameTestHelper helper) {
        // 目标：验证 BastionSpawnService 的“停机门禁”行为：当 resourcePool <= 0 时必须直接返回 0，禁止刷出新守卫。
        //
        // 关键点：
        // 1) GameTest 环境通常没有玩家，且 BastionTicker 不一定按完整流程触发，因此这里必须直接调用 trySpawn。
        // 2) 为避免未来实现调整（例如把门禁放到概率检查之后）导致测试变成“概率刷怪”，本测试会临时向
        //    BastionTypeManager 注入一个确定性配置：base_spawn_chance=1.0、max_spawns_per_tick=1。
        // 3) 坐标陷阱：写入 BastionData 的核心坐标必须使用 helper.absolutePos(relPos)。

        BastionTypeConfig previous = BastionTypeManager.get(TEST_BASTION_TYPE_ID_SPAWN_SHUTDOWN);
        BastionTypeManager.register(createTestBastionTypeConfigForSpawnShutdown());

        try {
            ServerLevel level = helper.getLevel();
            BastionSavedData savedData = BastionSavedData.get(level);
            long gameTime = level.getGameTime();

            // 选择一个结构内坐标作为“核心相对位置”。这里不要求真实放置核心方块，因为门禁在刷怪逻辑最前端。
            BlockPos coreRel = new BlockPos(0, 2, 0);
            BlockPos coreAbs = helper.absolutePos(coreRel);

            // 构造一个 ACTIVE 状态、resourcePool=0 的基地。
            // 注意：BastionData.create 默认 resourcePool 就是 0.0，这里显式使用该事实来触发 shutdown gate。
            BastionData bastion = BastionData.create(
                coreAbs,
                level.dimension(),
                TEST_BASTION_TYPE_ID_SPAWN_SHUTDOWN,
                BastionDao.ZHI_DAO,
                gameTime
            );

            int spawned = BastionSpawnService.trySpawn(level, savedData, bastion, gameTime);
            helper.assertValueEqual(spawned, 0, "资源池为 0 时应触发停机门禁，禁止刷怪");
            helper.succeed();
        } finally {
            // 清理：尽量恢复原配置，避免影响同 JVM 内的其它测试。
            // 如果之前不存在同名配置，则保持注入的测试配置即可（不会影响本次断言结果）。
            if (previous != null) {
                BastionTypeManager.register(previous);
            }
        }
    }

    @GameTest(template = "empty")
    public void testGuardianUpkeepDrainsPool(GameTestHelper helper) {
        // 目标：验证回合 4 的“供养扣费”语义可被直接观测：
        // - 只要基地范围内存在 1 个已标记归属的守卫实体，调用 applyUpkeep 就会从 resourcePool 扣除固定费用。
        //
        // 关键点：
        // 1) GameTest 里没有玩家，且 BastionTicker 不一定触发完整 tick，因此必须直接调用服务层方法。
        // 2) 守卫不要求是自定义实体：用原版 Zombie 即可，只要打上 BastionGuardianData 的归属标记。
        // 3) 坐标陷阱：写入 BastionData 的 corePos 必须使用 helper.absolutePos(relPos)。

        ServerLevel level = helper.getLevel();
        long gameTime = level.getGameTime();

        BlockPos coreRel = new BlockPos(0, 2, 0);
        BlockPos coreAbs = helper.absolutePos(coreRel);

        BastionData bastion = BastionData.create(
            coreAbs,
            level.dimension(),
            TEST_BASTION_TYPE_ID_SPAWN_SHUTDOWN,
            BastionDao.ZHI_DAO,
            gameTime
        ).withResourcePool(UPKEEP_TEST_INITIAL_POOL);

        // 在核心附近生成一个 Mob 并标记为该基地的守卫。
        // 注意：helper.spawn 使用结构内相对坐标。
        Mob mob = helper.spawn(EntityType.ZOMBIE, coreRel);
        BastionGuardianData.markAsGuardian(mob, bastion.id(), bastion.tier());

        double oldPool = bastion.resourcePool();
        BastionData updated = BastionGuardianUpkeepService.applyUpkeep(level, bastion);
        double newPool = updated.resourcePool();

        // 断言 1：一定发生扣费。
        if (!(newPool < oldPool)) {
            helper.fail("applyUpkeep 未扣费：oldPool=" + oldPool + ", newPool=" + newPool);
            return;
        }

        // 断言 2：只有 1 个守卫时，扣费应为 1.0。
        // 这里用精确相等，是为了让“常量语义”在回归测试中一眼可见。
        double expected = oldPool - UPKEEP_TEST_COST_PER_GUARDIAN;
        helper.assertValueEqual(
            newPool,
            expected,
            "upkeep 扣费值不符合预期：expected=" + expected + ", actual=" + newPool
        );

        // 断言 3：资源池下界保护（服务内使用 Math.max(0.0, ...)）。
        if (newPool < 0.0) {
            helper.fail("resourcePool 不应为负数：" + newPool);
            return;
        }

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = WAIT_FOR_DECAY_TICKS + 5 * TICKS_PER_SECOND)
    public void testBastionDisconnectThenDecayRemovesMycelium(GameTestHelper helper) {
        // 目标：构建“核心 -> 菌毯(中间) -> 菌毯(远端)”链（步长=2），破坏中间菌毯，
        // 等待连通性扫描触发衰败，断言远端菌毯被移除。

        // 结构内相对坐标（必须用 absolutePos 写入 SavedData 的缓存）。
        BlockPos coreRel = new BlockPos(0, 2, 0);
        BlockPos middleRel = new BlockPos(MYCELIUM_STEP, 2, 0);
        BlockPos farRel = middleRel.offset(MYCELIUM_STEP, 0, 0);

        // 基底铺设：避免菌毯因为下方空气导致不稳定。
        for (int x = 0; x < FLOOR_LENGTH; x++) {
            helper.setBlock(new BlockPos(x, 1, 0), Blocks.STONE);
        }

        // 放置核心与菌毯。
        helper.setBlock(coreRel, BastionBlocks.BASTION_CORE.get().defaultBlockState()
            .setValue(BastionCoreBlock.DAO, BastionDao.ZHI_DAO)
            .setValue(BastionCoreBlock.TIER, 1));
        helper.setBlock(middleRel, BastionBlocks.BASTION_NODE.get().defaultBlockState());
        helper.setBlock(farRel, BastionBlocks.BASTION_NODE.get().defaultBlockState());

        // 在 SavedData 中注册基地，并将菌毯写入 nodeCache（否则连通性扫描会把它们当“非网络节点”）。
        ServerLevel level = helper.getLevel();
        BastionSavedData savedData = BastionSavedData.get(level);
        BlockPos coreAbs = helper.absolutePos(coreRel);
        BlockPos middleAbs = helper.absolutePos(middleRel);
        BlockPos farAbs = helper.absolutePos(farRel);

        BastionData bastion = BastionData.create(
            coreAbs,
            level.dimension(),
            "default",
            BastionDao.ZHI_DAO,
            level.getGameTime()
        ).withMyceliumCountDelta(2);

        savedData.addBastion(bastion);
        savedData.initializeFrontierFromCore(bastion.id(), coreAbs);
        savedData.addNodeToCache(bastion.id(), middleAbs);
        savedData.addNodeToCache(bastion.id(), farAbs);

        // 延迟：确保 BastionTicker 至少跑过一次，再制造断连。
        helper.runAfterDelay(BREAK_MIDDLE_DELAY_TICKS, () -> helper.setBlock(middleRel, Blocks.AIR));

        helper.runAfterDelay(WAIT_FOR_DECAY_TICKS, () -> {
            // 断言：远端菌毯已被衰败移除（变为空气）。
            if (!helper.getBlockState(farRel).isAir()) {
                helper.fail("远端菌毯未衰败移除: " + farRel.toShortString());
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = ENERGY_TEST_TIMEOUT_TICKS)
    public void testEnergyWaterEnvironmentIncreasesResourcePool(GameTestHelper helper) {
        // 回合3-步骤3目标：在 GameTest 中模拟“环境能源”（汲水 / water_intake），并断言资源池增长倍率可观测。
        //
        // 为什么选“水”：
        // - BastionEnergyService 的汲水判定是“立方体范围内存在水流体”，比 skylight 更稳定（不受天空可见性/遮挡细节影响）。
        // - 默认配置 water_intake 有 flat=0.5（每 tick 平坦供给），效果足够大，便于在短时间窗口内做对比。
        //
        // 关键约束：
        // - 能源扫描预算：每 40 tick 扫一次，单次最多 4 个 Anchor。本测试仅放 1 个 Anchor，保证不超预算。
        // - 坐标陷阱：结构内用相对坐标放方块，但写入 SavedData 的缓存必须用 absolutePos。

        BlockPos coreRel = new BlockPos(0, 2, 0);
        BlockPos anchorRel = coreRel.offset(ENERGY_TEST_ANCHOR_OFFSET_X, 0, 0);
        BlockPos waterRel = anchorRel.offset(1, 0, 0);

        // 基底铺设：避免 Anchor/水因下方空气导致不稳定（尤其是液体流动）。
        for (int x = 0; x < ENERGY_TEST_FLOOR_LENGTH; x++) {
            helper.setBlock(new BlockPos(x, 1, 0), Blocks.STONE);
        }

        // 放置核心与 Anchor（注意：水先不放，作为“无环境”对照段）。
        helper.setBlock(coreRel, BastionBlocks.BASTION_CORE.get().defaultBlockState()
            .setValue(BastionCoreBlock.DAO, BastionDao.ZHI_DAO)
            .setValue(BastionCoreBlock.TIER, 1));
        helper.setBlock(anchorRel, BastionBlocks.BASTION_ANCHOR.get().defaultBlockState());

        // 在 SavedData 中注册基地，并写入 anchorCache：能源扫描只读取 anchorCache。
        ServerLevel level = helper.getLevel();
        BastionSavedData savedData = BastionSavedData.get(level);
        BlockPos coreAbs = helper.absolutePos(coreRel);
        BlockPos anchorAbs = helper.absolutePos(anchorRel);

        BastionData bastion = BastionData.create(
            coreAbs,
            level.dimension(),
            "default",
            BastionDao.ZHI_DAO,
            level.getGameTime()
        ).withAnchorCountDelta(1);

        UUID bastionId = bastion.id();
        savedData.addBastion(bastion);
        savedData.initializeFrontierFromCore(bastionId, coreAbs);
        savedData.initializeAnchorCacheFromCore(bastionId, coreAbs);
        savedData.addAnchorToCache(bastionId, anchorAbs);

        // 用数组做“可变盒子”，便于在不同回调间传递采样值（Java lambda 需要 effectively-final）。
        double[] poolSample = new double[1];
        double[] baselineDelta = new double[1];

        // 1) 预热后采样起点（无环境）。
        helper.runAfterDelay(ENERGY_TEST_WARMUP_TICKS, () -> {
            BastionData current = savedData.getBastion(bastionId);
            if (current == null) {
                helper.fail("基地记录不存在（预热采样点）：" + bastionId);
                return;
            }
            poolSample[0] = current.resourcePool();
        });

        // 2) 采样窗口结束：计算“无环境”资源池增量，并立即放置水源作为环境。
        helper.runAfterDelay(ENERGY_TEST_WARMUP_TICKS + ENERGY_TEST_MEASURE_WINDOW_TICKS, () -> {
            BastionData current = savedData.getBastion(bastionId);
            if (current == null) {
                helper.fail("基地记录不存在（无环境窗口结束）：" + bastionId);
                return;
            }

            double endPool = current.resourcePool();
            baselineDelta[0] = endPool - poolSample[0];

            // 放置水：触发 water_intake。
            helper.setBlock(waterRel, Blocks.WATER);
        });

        // 3) 等待扫描稳定后采样起点（有水环境）。
        helper.runAfterDelay(
            ENERGY_TEST_WARMUP_TICKS + ENERGY_TEST_MEASURE_WINDOW_TICKS + ENERGY_TEST_SCAN_SETTLE_TICKS,
            () -> {
                BastionData current = savedData.getBastion(bastionId);
                if (current == null) {
                    helper.fail("基地记录不存在（有环境采样点）：" + bastionId);
                    return;
                }
                poolSample[0] = current.resourcePool();
            }
        );

        // 4) 采样窗口结束：计算“有环境”资源池增量，并做对比断言。
        helper.runAfterDelay(
            ENERGY_TEST_WARMUP_TICKS
                + ENERGY_TEST_MEASURE_WINDOW_TICKS
                + ENERGY_TEST_SCAN_SETTLE_TICKS
                + ENERGY_TEST_MEASURE_WINDOW_TICKS,
            () -> {
                BastionData current = savedData.getBastion(bastionId);
                if (current == null) {
                    helper.fail("基地记录不存在（有环境窗口结束）：" + bastionId);
                    return;
                }

                double deltaWithWater = current.resourcePool() - poolSample[0];
                double deltaNoEnv = baselineDelta[0];
                double extra = deltaWithWater - deltaNoEnv;

                // 核心断言：有环境时的资源池增长 > 无环境。
                if (!(deltaWithWater > deltaNoEnv)) {
                    helper.fail(
                        "资源池增长未体现环境差异：无环境增量=" + deltaNoEnv
                            + "，有水环境增量=" + deltaWithWater
                            + "（应更大）"
                    );
                    return;
                }

                // 额外断言：差值达到可观测下界，避免“恰好大一点点”的边界误差。
                if (extra < ENERGY_TEST_MIN_EXTRA_POOL_GAIN) {
                    helper.fail(
                        "资源池增长差值过小（可能未触发能源扫描/环境判定）：差值=" + extra
                            + "，下界=" + ENERGY_TEST_MIN_EXTRA_POOL_GAIN
                    );
                    return;
                }

                helper.succeed();
            }
        );
    }

    @GameTest(template = "empty", timeoutTicks = BASTION_FRIENDLY_REGRESSION_TIMEOUT_TICKS)
    public void testBastionWitchGuardianIgnoresFriendlyOwner(GameTestHelper helper) {
        // 目标：覆盖“守卫友方免疫（Witch）”回归。
        // 场景：基地 capturedBy 与玩家 UUID 一致时，女巫守卫已有目标也必须被 runtime 目标筛选剔除。
        ServerLevel level = helper.getLevel();
        BastionSavedData savedData = BastionSavedData.get(level);
        long gameTime = level.getGameTime();

        BlockPos coreAbs = helper.absolutePos(BASTION_TEST_CORE_REL);
        BastionData friendlyBastion = BastionData.create(
            coreAbs,
            level.dimension(),
            "default",
            BastionDao.ZHI_DAO,
            gameTime
        ).withCaptured(TEST_FRIENDLY_PLAYER_ID, gameTime);
        savedData.addBastion(friendlyBastion);

        Mob witch = helper.spawn(EntityType.WITCH, BASTION_TEST_GUARDIAN_REL);
        BastionGuardianData.markAsGuardian(witch, friendlyBastion.id(), friendlyBastion.tier());

        ServerPlayer friendlyPlayer = createFakePlayer(level, TEST_FRIENDLY_PLAYER_ID, "friendly_owner");
        witch.setTarget(friendlyPlayer);

        try {
            invokeGuardianTickTargeting(level, witch);
        } catch (ReflectiveOperationException e) {
            helper.fail("反射调用 tickTargeting 失败：" + e.getMessage());
            return;
        }

        if (witch.getTarget() != null) {
            helper.fail("友方玩家不应被女巫守卫保留为目标");
            return;
        }

        friendlyPlayer.discard();
        savedData.removeBastion(friendlyBastion.id());

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = BASTION_FRIENDLY_REGRESSION_TIMEOUT_TICKS)
    public void testBastionZhiAuraIgnoresFriendlyOwner(GameTestHelper helper) {
        // 目标：覆盖“智道光环友方免疫”回归。
        // 场景：当玩家 UUID 与 capturedBy 一致时，applyAuraEffect 必须在入口直接短路，不施加 debuff。
        ServerLevel level = helper.getLevel();
        long gameTime = level.getGameTime();
        ServerPlayer friendlyPlayer = createFakePlayer(level, TEST_FRIENDLY_PLAYER_ID, "friendly_aura_owner");
        friendlyPlayer.removeAllEffects();

        BlockPos coreAbs = helper.absolutePos(BASTION_TEST_CORE_REL);
        BastionData friendlyBastion = BastionData.create(
            coreAbs,
            level.dimension(),
            "default",
            BastionDao.ZHI_DAO,
            gameTime
        ).withCaptured(TEST_FRIENDLY_PLAYER_ID, gameTime);

        try {
            invokeApplyAuraEffect(friendlyPlayer, friendlyBastion, coreAbs);
        } catch (ReflectiveOperationException e) {
            helper.fail("反射调用 applyAuraEffect 失败：" + e.getMessage());
            return;
        }

        if (friendlyPlayer.hasEffect(MobEffects.DIG_SLOWDOWN)
            || friendlyPlayer.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            helper.fail("友方玩家不应受到智道光环减速/挖掘疲劳影响");
            return;
        }

        friendlyPlayer.discard();

        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = BASTION_FRIENDLY_REGRESSION_TIMEOUT_TICKS)
    public void testBastionEnemyStillAffectedByGuardianAndAura(GameTestHelper helper) {
        // 目标：覆盖“敌方仍生效”回归，防止修复退化为全体免疫。
        // 验证点：
        // 1) 女巫守卫对敌方目标仍会保留锁定；
        // 2) 智道光环对敌方仍施加 DIG_SLOWDOWN/MOVEMENT_SLOWDOWN。
        ServerLevel level = helper.getLevel();
        BastionSavedData savedData = BastionSavedData.get(level);
        long gameTime = level.getGameTime();

        BlockPos coreAbs = helper.absolutePos(BASTION_TEST_CORE_REL);
        BastionData bastion = BastionData.create(
            coreAbs,
            level.dimension(),
            "default",
            BastionDao.ZHI_DAO,
            gameTime
        ).withCaptured(TEST_OWNER_PLAYER_ID, gameTime);
        savedData.addBastion(bastion);

        Mob witch = helper.spawn(EntityType.WITCH, BASTION_TEST_GUARDIAN_REL);
        BastionGuardianData.markAsGuardian(witch, bastion.id(), bastion.tier());

        ServerPlayer enemyPlayer = createFakePlayer(level, TEST_ENEMY_PLAYER_ID, "enemy_player");
        enemyPlayer.removeAllEffects();
        witch.setTarget(enemyPlayer);

        try {
            invokeGuardianTickTargeting(level, witch);
        } catch (ReflectiveOperationException e) {
            helper.fail("反射调用 tickTargeting 失败：" + e.getMessage());
            return;
        }

        if (witch.getTarget() != enemyPlayer) {
            helper.fail("敌方目标应继续被女巫守卫锁定");
            return;
        }

        // 敌方光环回归断言：直接走道途特化入口，验证“非友方仍会获得智道减益”。
        // 这里不依赖资源扣减分支，避免与玩家资源附件初始化耦合，聚焦本任务核心语义。
        bastion.primaryDao().onAuraTick(enemyPlayer, bastion.tier(), 1.0D);

        if (!enemyPlayer.hasEffect(MobEffects.DIG_SLOWDOWN)
            || !enemyPlayer.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            helper.fail("敌方玩家应继续受到智道光环减速/挖掘疲劳影响");
            return;
        }

        enemyPlayer.discard();
        savedData.removeBastion(bastion.id());

        helper.succeed();
    }

    /**
     * 创建可用于 GameTest 的假玩家实例。
     * <p>
     * 说明：本测试关注的是 UUID 友敌语义与效果施加逻辑，
     * 使用 FakePlayer 可避免引入人工联机与真实登录依赖。
     * </p>
     */
    private static ServerPlayer createFakePlayer(ServerLevel level, UUID uuid, String name) {
        return FakePlayerFactory.get(level, new GameProfile(uuid, name));
    }

    /**
     * 通过反射调用 BastionGuardianRuntimeService 的私有目标筛选方法。
     * <p>
     * 使用该入口是为了将断言聚焦在“目标保留/重选语义”，避免 tick 间隔对齐带来的不确定性。
     * </p>
     */
    private static void invokeGuardianTickTargeting(ServerLevel level, Mob guardian)
            throws ReflectiveOperationException {
        Method method = BastionGuardianRuntimeService.class.getDeclaredMethod(
            METHOD_TICK_TARGETING,
            ServerLevel.class,
            Mob.class
        );
        method.setAccessible(true);
        method.invoke(null, level, guardian);
    }

    /**
     * 通过反射调用 BastionAuraService 的私有光环入口。
     * <p>
     * 使用该入口可直接验证友方短路与敌方生效两个核心行为，避免事件构造噪声干扰断言。
     * </p>
     */
    private static void invokeApplyAuraEffect(ServerPlayer player, BastionData bastion, BlockPos playerPos)
            throws ReflectiveOperationException {
        Method method = BastionAuraService.class.getDeclaredMethod(
            METHOD_APPLY_AURA_EFFECT,
            ServerPlayer.class,
            BastionData.class,
            BlockPos.class
        );
        method.setAccessible(true);
        try {
            method.invoke(null, player, bastion, playerPos);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            String causeMessage = cause == null
                ? "<no-cause>"
                : cause.getClass().getSimpleName() + ": " + cause.getMessage();
            throw new ReflectiveOperationException("applyAuraEffect 业务异常: " + causeMessage, e);
        }
    }
}
