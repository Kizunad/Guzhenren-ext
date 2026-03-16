package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoActiveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.network.PacketSyncNianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.service.ShazhaoActiveService;
import com.Kizunad.guzhenrenext.kongqiao.service.ShazhaoActiveService.ActivationFailureReason;
import com.Kizunad.guzhenrenext.kongqiao.service.ShazhaoActiveService.ActivationResult;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataManager;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoUnlockService;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.service.FragmentPlacementService;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * Task17 里程碑闭环 D：深度空窍联动综合验收。
 * <p>
 * 本测试聚焦 “仙窍内容 → 念头/杀招 → 仙窍反哺” 的服务端闭环，覆盖：
 * 1) 数据加载前置注入（NianTou/Shazhao DataManager）；
 * 2) 服务端解锁与激活判定（ShazhaoUnlockService + ShazhaoActiveService）；
 * 3) 仙窍反哺状态更新（ApertureWorldData 边界扩张）；
 * 4) 同步载荷编解码一致性（PacketSyncNianTouUnlocks）；
 * 5) 重复结算与不同步防回归断言。
 * </p>
 */
@GameTestHolder("guzhenrenext")
public class Task17LoopDGameTests {

    private static final String TASK17_LOOP_D_BATCH = "task17_loop_d";
    private static final int TEST_TIMEOUT_TICKS = 220;
    private static final int REQUIRED_SLOT_INDEX = 0;
    private static final int REQUIRED_UNLOCKED_ROWS = 1;
    private static final int EXPECTED_BOUNDARY_INCREMENT = 1;
    private static final int EXPECTED_SETTLEMENT_ONCE = 1;
    private static final int EXPECTED_ZERO_EFFECT_INVOCATION = 0;
    private static final int TEST_USAGE_DURATION = 80;
    private static final int TEST_USAGE_COST = 20;
    private static final int TEST_SHAZHAO_COST = 30;
    private static final String TEST_PLAYER_NAME_PREFIX = "task17_loop_d_player_";
    private static final String TEST_USAGE_ID = "guzhenrenext:test_task17_loop_d_usage";
    private static final String TEST_SHAZHAO_TITLE = "Task17 测试杀招";
    private static final String TEST_SHAZHAO_DESC = "用于 Task17 闭环验收";
    private static final String TEST_SHAZHAO_INFO = "触发后执行一次仙窍边界反哺";
    private static final String SYNC_MESSAGE = "task17_loop_d_sync_message";
    private static final ResourceLocation TEST_ITEM_ID = BuiltInRegistries.ITEM.getKey(Items.APPLE);
    private static final ResourceLocation TEST_SHAZHAO_ID =
        ResourceLocation.fromNamespaceAndPath("guzhenrenext", "shazhao_active_task17_loop_d");

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK17_LOOP_D_BATCH)
    public void testTask17LoopDHappyPathShouldLinkApertureContentToShazhaoAndFeedback(GameTestHelper helper) {
        TestLoopDShazhaoActiveEffect.resetCounters();
        registerTask17InjectedData();

        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, "happy");
        preparePlayerContext(level, player, true);

        NianTouUnlocks unlocks = requireUnlocks(helper, player, "happy path: 缺少 NianTouUnlocks 附件");
        unlocks.unlock(TEST_ITEM_ID, TEST_USAGE_ID);

        List<ShazhaoUnlockService.UnlockCandidate> candidates = ShazhaoUnlockService.listUnlockCandidates(unlocks);
        boolean containsTarget = candidates
            .stream()
            .anyMatch(candidate -> TEST_SHAZHAO_ID.toString().equals(candidate.data().shazhaoID()));
        helper.assertTrue(containsTarget, "happy path: 已具备仙窍内容时必须命中杀招候选链路");

        unlocks.unlockShazhao(TEST_SHAZHAO_ID);
        ApertureInfo beforeFeedback = requireApertureInfo(helper, level, player.getUUID(), "happy path: 反哺前仙窍信息缺失");

        ActivationResult firstActivation = ShazhaoActiveService.activate(player, TEST_SHAZHAO_ID.toString());
        ApertureInfo afterFirstFeedback = requireApertureInfo(
            helper,
            level,
            player.getUUID(),
            "happy path: 首次激活后仙窍信息缺失"
        );
        helper.assertTrue(
            firstActivation.success() && firstActivation.failureReason() == ActivationFailureReason.NONE,
            "happy path: 已解锁且条件满足时，服务端必须允许杀招激活"
        );
        helper.assertTrue(
            hasBoundaryExpandedByOneChunk(beforeFeedback, afterFirstFeedback),
            "happy path: 首次激活后必须触发仙窍反哺（边界四向各 +1 chunk）"
        );

        ActivationResult secondActivation = ShazhaoActiveService.activate(player, TEST_SHAZHAO_ID.toString());
        ApertureInfo afterSecondFeedback = requireApertureInfo(
            helper,
            level,
            player.getUUID(),
            "happy path: 二次激活后仙窍信息缺失"
        );
        helper.assertTrue(
            !secondActivation.success()
                && secondActivation.failureReason() == ActivationFailureReason.CONDITION_NOT_MET,
            "happy path: 重复触发必须被服务端拦截，防止重复结算"
        );
        helper.assertTrue(
            isBoundaryUnchanged(afterFirstFeedback, afterSecondFeedback),
            "happy path: 重复触发后仙窍边界必须保持不变"
        );
        helper.assertTrue(
            TestLoopDShazhaoActiveEffect.SETTLEMENT_COUNT.get() == EXPECTED_SETTLEMENT_ONCE,
            "happy path: 真实反哺结算应仅执行一次"
        );

        unlocks.setShazhaoMessage(SYNC_MESSAGE);
        assertSyncRoundTrip(helper, unlocks, TEST_SHAZHAO_ID, true, SYNC_MESSAGE);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK17_LOOP_D_BATCH)
    public void testTask17LoopDFailurePathShouldRejectUnsatisfiedOrDesyncedSettlement(GameTestHelper helper) {
        TestLoopDShazhaoActiveEffect.resetCounters();
        registerTask17InjectedData();

        ServerLevel level = helper.getLevel();
        ServerPlayer player = createTestPlayer(level, "failure");
        preparePlayerContext(level, player, false);

        NianTouUnlocks unlocks = requireUnlocks(helper, player, "failure path: 缺少 NianTouUnlocks 附件");
        ShazhaoData testShazhaoData = createTask17ShazhaoData();
        double chance = ShazhaoUnlockService.calculateUnlockChance(unlocks, testShazhaoData);
        helper.assertTrue(chance == 0.0D, "failure path: 未解锁念头用途时，杀招推演概率必须为 0");

        ApertureInfo beforeActivation = requireApertureInfo(helper, level, player.getUUID(), "failure path: 激活前仙窍信息缺失");

        ActivationResult notUnlocked = ShazhaoActiveService.activate(player, TEST_SHAZHAO_ID.toString());
        ApertureInfo afterNotUnlocked = requireApertureInfo(
            helper,
            level,
            player.getUUID(),
            "failure path: 未解锁判定后仙窍信息缺失"
        );
        helper.assertTrue(
            !notUnlocked.success() && notUnlocked.failureReason() == ActivationFailureReason.NOT_UNLOCKED,
            "failure path: 杀招未解锁时，服务端必须拒绝激活"
        );
        helper.assertTrue(
            isBoundaryUnchanged(beforeActivation, afterNotUnlocked),
            "failure path: 未解锁拒绝后不应产生任何反哺"
        );

        unlocks.unlockShazhao(TEST_SHAZHAO_ID);
        int effectCallsBefore = TestLoopDShazhaoActiveEffect.ACTIVATE_CALL_COUNT.get();
        ActivationResult missingRequiredItem = ShazhaoActiveService.activate(player, TEST_SHAZHAO_ID.toString());
        ApertureInfo afterMissingItem = requireApertureInfo(
            helper,
            level,
            player.getUUID(),
            "failure path: 条件不足判定后仙窍信息缺失"
        );
        helper.assertTrue(
            !missingRequiredItem.success()
                && missingRequiredItem.failureReason() == ActivationFailureReason.CONDITION_NOT_MET,
            "failure path: 缺少 required_items 时必须拒绝激活"
        );
        helper.assertTrue(
            isBoundaryUnchanged(afterNotUnlocked, afterMissingItem),
            "failure path: 条件不足拒绝后不应产生错误反哺"
        );
        helper.assertTrue(
            TestLoopDShazhaoActiveEffect.ACTIVATE_CALL_COUNT.get() == effectCallsBefore,
            "failure path: 条件不足分支不应进入杀招效果实现（防止服务端不同步）"
        );
        helper.assertTrue(
            TestLoopDShazhaoActiveEffect.SETTLEMENT_COUNT.get() == EXPECTED_ZERO_EFFECT_INVOCATION,
            "failure path: 失败分支不应执行任何结算"
        );

        unlocks.setShazhaoMessage(SYNC_MESSAGE);
        assertSyncRoundTrip(helper, unlocks, TEST_SHAZHAO_ID, true, SYNC_MESSAGE);
        helper.succeed();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, String suffix) {
        UUID uuid = UUID.nameUUIDFromBytes(
            (TASK17_LOOP_D_BATCH + "_" + suffix).getBytes(StandardCharsets.UTF_8)
        );
        String playerName = TEST_PLAYER_NAME_PREFIX + suffix;
        return FakePlayerFactory.get(level, new GameProfile(uuid, playerName));
    }

    /**
     * 准备测试玩家上下文：
     * 1) 确保拥有 NianTouUnlocks/KongqiaoData 附件；
     * 2) 确保仙窍世界数据已分配；
     * 3) 可选挂载 required_items（APPLE）以满足激活前置。
     */
    private static void preparePlayerContext(ServerLevel level, ServerPlayer player, boolean withRequiredItem) {
        ensurePlayerAttachments(player);
        ApertureWorldData.get(level).allocateAperture(player.getUUID());

        KongqiaoData kongqiaoData = KongqiaoAttachments.getData(player);
        kongqiaoData.getKongqiaoInventory().getSettings().setUnlockedRows(REQUIRED_UNLOCKED_ROWS);
        if (withRequiredItem) {
            kongqiaoData.getKongqiaoInventory().setItem(REQUIRED_SLOT_INDEX, new ItemStack(Items.APPLE));
        } else {
            kongqiaoData.getKongqiaoInventory().setItem(REQUIRED_SLOT_INDEX, ItemStack.EMPTY);
        }
    }

    private static void ensurePlayerAttachments(ServerPlayer player) {
        if (!player.hasData(KongqiaoAttachments.NIANTOU_UNLOCKS.get())) {
            player.setData(KongqiaoAttachments.NIANTOU_UNLOCKS.get(), new NianTouUnlocks());
        }
        if (!player.hasData(KongqiaoAttachments.KONGQIAO.get())) {
            KongqiaoData data = new KongqiaoData();
            data.bind(player);
            player.setData(KongqiaoAttachments.KONGQIAO.get(), data);
        }
    }

    /**
     * 以最小注入方式准备 Task17 必需数据：
     * 1) 注入一条念头用途数据，保证推演候选可计算；
     * 2) 注入一条主动杀招数据，保证激活判定可落到服务端逻辑；
     * 3) 注入测试专用效果实现，保证“反哺”可观测。
     */
    private static void registerTask17InjectedData() {
        NianTouDataManager.register(createTask17NianTouData());
        ShazhaoDataManager.register(createTask17ShazhaoData());
        ShazhaoEffectRegistry.register(new TestLoopDShazhaoActiveEffect());
    }

    private static NianTouData createTask17NianTouData() {
        NianTouData.Usage usage = new NianTouData.Usage(
            TEST_USAGE_ID,
            "Task17 测试用途",
            "用于深联动验收",
            "Task17 usage info",
            TEST_USAGE_DURATION,
            TEST_USAGE_COST,
            Map.of()
        );
        return new NianTouData(TEST_ITEM_ID.toString(), List.of(usage));
    }

    private static ShazhaoData createTask17ShazhaoData() {
        return new ShazhaoData(
            TEST_SHAZHAO_ID.toString(),
            TEST_SHAZHAO_TITLE,
            TEST_SHAZHAO_DESC,
            TEST_SHAZHAO_INFO,
            TEST_SHAZHAO_COST,
            List.of(TEST_ITEM_ID.toString()),
            Map.of()
        );
    }

    private static NianTouUnlocks requireUnlocks(GameTestHelper helper, ServerPlayer player, String message) {
        NianTouUnlocks unlocks = KongqiaoAttachments.getUnlocks(player);
        helper.assertTrue(unlocks != null, message);
        return unlocks;
    }

    private static ApertureInfo requireApertureInfo(
        GameTestHelper helper,
        ServerLevel level,
        UUID owner,
        String message
    ) {
        ApertureInfo info = ApertureWorldData.get(level).getAperture(owner);
        helper.assertTrue(info != null, message);
        return info;
    }

    private static boolean hasBoundaryExpandedByOneChunk(ApertureInfo before, ApertureInfo after) {
        return after.minChunkX() == before.minChunkX() - EXPECTED_BOUNDARY_INCREMENT
            && after.maxChunkX() == before.maxChunkX() + EXPECTED_BOUNDARY_INCREMENT
            && after.minChunkZ() == before.minChunkZ() - EXPECTED_BOUNDARY_INCREMENT
            && after.maxChunkZ() == before.maxChunkZ() + EXPECTED_BOUNDARY_INCREMENT;
    }

    private static boolean isBoundaryUnchanged(ApertureInfo left, ApertureInfo right) {
        return left.minChunkX() == right.minChunkX()
            && left.maxChunkX() == right.maxChunkX()
            && left.minChunkZ() == right.minChunkZ()
            && left.maxChunkZ() == right.maxChunkZ();
    }

    /**
     * 同步一致性断言：通过编解码回环验证 PacketSyncNianTouUnlocks 不丢失关键字段。
     */
    private static void assertSyncRoundTrip(
        GameTestHelper helper,
        NianTouUnlocks unlocks,
        ResourceLocation shazhaoId,
        boolean expectedUnlocked,
        String expectedMessage
    ) {
        ByteBuf buffer = Unpooled.buffer();
        PacketSyncNianTouUnlocks.STREAM_CODEC.encode(buffer, new PacketSyncNianTouUnlocks(unlocks));
        PacketSyncNianTouUnlocks decoded = PacketSyncNianTouUnlocks.STREAM_CODEC.decode(buffer);

        helper.assertTrue(
            decoded.data().isShazhaoUnlocked(shazhaoId) == expectedUnlocked,
            "sync path: 编解码后杀招解锁状态必须一致"
        );
        helper.assertTrue(
            expectedMessage.equals(decoded.data().getShazhaoMessage()),
            "sync path: 编解码后消息文本必须一致"
        );
    }

    /**
     * Task17 专用测试杀招效果：
     * 1) 首次激活调用 FragmentPlacementService 执行仙窍边界反哺；
     * 2) 同一玩家重复激活时直接拒绝，防止重复结算。
     */
    private static final class TestLoopDShazhaoActiveEffect implements IShazhaoActiveEffect {

        private static final String SETTLED_ONCE_FLAG = "task17_loop_d_settled_once";
        private static final AtomicInteger ACTIVATE_CALL_COUNT = new AtomicInteger();
        private static final AtomicInteger SETTLEMENT_COUNT = new AtomicInteger();

        @Override
        public String getShazhaoId() {
            return TEST_SHAZHAO_ID.toString();
        }

        @Override
        public boolean onActivate(ServerPlayer player, ShazhaoData data) {
            ACTIVATE_CALL_COUNT.incrementAndGet();
            if (player.getPersistentData().getBoolean(SETTLED_ONCE_FLAG)) {
                return false;
            }

            ApertureWorldData worldData = ApertureWorldData.get(player.serverLevel());
            ApertureInfo info = worldData.getAperture(player.getUUID());
            if (info == null) {
                return false;
            }

            boolean placed = FragmentPlacementService.placeFragment(player.serverLevel(), player, info);
            if (!placed) {
                return false;
            }

            player.getPersistentData().putBoolean(SETTLED_ONCE_FLAG, true);
            SETTLEMENT_COUNT.incrementAndGet();
            return true;
        }

        private static void resetCounters() {
            ACTIVATE_CALL_COUNT.set(0);
            SETTLEMENT_COUNT.set(0);
        }
    }
}
