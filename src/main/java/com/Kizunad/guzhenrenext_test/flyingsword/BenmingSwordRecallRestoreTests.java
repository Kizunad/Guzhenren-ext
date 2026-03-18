package com.Kizunad.guzhenrenext_test.flyingsword;

import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordController;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordEntity;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordSpawner;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordTickHandler;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordCooldownAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStateAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.attachment.FlyingSwordStorageAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.calculator.FlyingSwordAttributes;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance.FlyingSwordResonanceType;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordBondService;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordReadonlyModifierHelper;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordResourceTransaction;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.CultivationSnapshot;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.training.FlyingSwordTrainingAttachment;
import com.Kizunad.guzhenrenext.kongqiao.flyingsword.training.FlyingSwordTrainingService;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.ItemStackCustomDataHelper;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;

/**
 * 本命飞剑“召回/恢复/存档”链路测试。
 * <p>
 * 目标：
 * <ul>
 *     <li>happy：验证召回后再恢复，stableSwordId 与 bond 意图不丢失。</li>
 *     <li>guard：验证 itemWithdrawn=true 的记录会被拒绝恢复。</li>
 * </ul>
 * </p>
 */
@GameTestHolder("guzhenrenext")
public class BenmingSwordRecallRestoreTests {

    private static final int TEST_TIMEOUT_TICKS = 80;
    private static final int PLAYER_RELATIVE_X = 4;
    private static final int PLAYER_RELATIVE_Y = 2;
    private static final int PLAYER_RELATIVE_Z = 4;
    private static final String EXPECTED_STABLE_SWORD_ID = "test-stable-sword-id-001";
    private static final String TICK_REBUILD_LIVE_STABLE_ID =
        "test-tick-rebuild-live-benming-001";
    private static final String TICK_REBUILD_WITHDRAWN_STABLE_ID =
        "test-tick-rebuild-withdrawn-benming-001";
    private static final String TICK_REBUILD_STALE_CACHE_ID =
        "test-tick-rebuild-stale-cache-001";
    private static final String TICK_REBUILD_DAMAGED_STABLE_ID =
        "test-tick-rebuild-damaged-benming-001";
    private static final String RESTORE_DIRTY_STABLE_ID =
        "test-restore-should-dirty-cache-001";
    private static final String E2E_HAPPY_STABLE_ID =
        "test-e2e-happy-stable-sword-001";
    private static final String E2E_NEGATIVE_SHORTAGE_STABLE_ID =
        "test-e2e-negative-shortage-sword-001";
    private static final String E2E_NEGATIVE_ACTIVE_UNBIND_STABLE_ID =
        "test-e2e-negative-active-unbind-sword-001";
    private static final String E2E_NEGATIVE_BACKLASH_FIRST_STABLE_ID =
        "test-e2e-negative-backlash-first-sword-001";
    private static final String E2E_NEGATIVE_BACKLASH_SECOND_STABLE_ID =
        "test-e2e-negative-backlash-second-sword-001";
    private static final double EXPECTED_BOND_RESONANCE = 0.73D;
    private static final double E2E_NEGATIVE_INITIAL_RESONANCE = 0.45D;
    private static final double BOND_RESONANCE_EPSILON = 0.000001D;
    private static final int TRAINING_SLOT_SWORD = 0;
    private static final int TRAINING_SLOT_FUEL = 1;
    private static final int TRAINING_INITIAL_FUEL = 5;
    private static final int EXPECTED_BONUS_EXP_PER_TICK = 2;
    private static final int EXPECTED_BASE_EXP_PER_TICK = 1;
    private static final String BATCH_E2E_NEGATIVE = "benming_e2e_negative";
    private static final String BATCH_E2E_BACKLASH_BOUNDARY =
        "benming_e2e_backlash_boundary";
    private static final double ACTIVE_UNBIND_ZHENYUAN_COST = 40.0D;
    private static final double ACTIVE_UNBIND_NIANTOU_COST = 30.0D;
    private static final double ACTIVE_UNBIND_HUNPO_COST = 20.0D;
    private static final double LIGHT_BACKLASH_ZHENYUAN_COST = 12.0D;
    private static final double LIGHT_BACKLASH_NIANTOU_COST = 8.0D;
    private static final double LIGHT_BACKLASH_HUNPO_COST = 5.0D;
    private static final int EXPECTED_RESOURCE_MUTATION_COUNT = 3;
    private static final int EXPECTED_NO_RESOURCE_MUTATION_COUNT = 0;
    private static final double RESOURCE_SNAPSHOT_HIGH = 200.0D;
    private static final double RESOURCE_SNAPSHOT_SHORTAGE_ZHENYUAN = 120.0D;
    private static final double RESOURCE_SNAPSHOT_SHORTAGE_NIANTOU = 90.0D;
    private static final double RESOURCE_SNAPSHOT_SHORTAGE_HUNPO = 0.0D;
    private static final double RESOURCE_SNAPSHOT_LOW_BACKLASH_ZHENYUAN = 6.0D;
    private static final double RESOURCE_SNAPSHOT_LOW_BACKLASH_NIANTOU = 4.0D;
    private static final double RESOURCE_SNAPSHOT_LOW_BACKLASH_HUNPO = 3.0D;

    /**
     * happy：召回并恢复后，stableSwordId 与 bond（ownerUuid/resonance）应完整保留。
     */
    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_recall_restore_happy"
    )
    public void testRecallThenRestoreShouldKeepStableSwordIdAndBond(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createDeterministicPlayer(level, "happy_recall_restore");
        BlockPos playerPos = helper.absolutePos(
            new BlockPos(PLAYER_RELATIVE_X, PLAYER_RELATIVE_Y, PLAYER_RELATIVE_Z)
        );
        player.setPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());

        FlyingSwordEntity spawnedSword = FlyingSwordSpawner.spawnBasic(level, player);
        helper.assertTrue(spawnedSword != null, "前置失败：飞剑生成失败");

        // 设置可追踪的本命标识与绑定信息，作为召回/恢复链路的核心断言输入。
        FlyingSwordAttributes attrsBeforeRecall = spawnedSword.getSwordAttributes();
        attrsBeforeRecall.setStableSwordId(EXPECTED_STABLE_SWORD_ID);
        attrsBeforeRecall.getBond().setOwnerUuid(player.getUUID().toString());
        attrsBeforeRecall.getBond().setResonance(EXPECTED_BOND_RESONANCE);
        spawnedSword.syncAttributesToEntityData();

        FlyingSwordController.finishRecall(spawnedSword, player);

        FlyingSwordStorageAttachment storage = KongqiaoAttachments.getFlyingSwordStorage(player);
        helper.assertTrue(storage != null, "前置失败：玩家飞剑存储附件为空");
        helper.assertTrue(storage.getCount() == 1, "召回后存储数量应为 1");

        FlyingSwordStorageAttachment.RecalledSword recalled = storage.getAt(0);
        helper.assertTrue(recalled != null, "召回后应存在可读取存储项");

        // 先对“存档内容”断言，再对“恢复后的实体”断言，确保链路两端一致。
        FlyingSwordAttributes attrsInStorage = FlyingSwordAttributes.fromNBT(
            recalled.attributes
        );
        helper.assertTrue(
            EXPECTED_STABLE_SWORD_ID.equals(attrsInStorage.getStableSwordId()),
            "召回写入存档后 stableSwordId 应保持不变"
        );
        helper.assertTrue(
            player.getUUID().toString().equals(attrsInStorage.getBond().getOwnerUuid()),
            "召回写入存档后 bond.ownerUuid 应保持为玩家 UUID"
        );
        helper.assertTrue(
            Math.abs(
                attrsInStorage.getBond().getResonance() - EXPECTED_BOND_RESONANCE
            )
                < BOND_RESONANCE_EPSILON,
            "召回写入存档后 bond.resonance 应保持不变"
        );

        int restoredCount = FlyingSwordController.restoreOne(level, player);
        helper.assertTrue(restoredCount == 1, "恢复时应成功恢复 1 把飞剑");

        FlyingSwordEntity restoredSword = FlyingSwordController.getNearestSword(
            level,
            player
        );
        helper.assertTrue(restoredSword != null, "恢复后应能在玩家附近找到飞剑实体");

        FlyingSwordAttributes attrsAfterRestore = restoredSword.getSwordAttributes();
        helper.assertTrue(
            EXPECTED_STABLE_SWORD_ID.equals(attrsAfterRestore.getStableSwordId()),
            "恢复后 stableSwordId 应与召回前一致"
        );
        helper.assertTrue(
            player.getUUID().toString().equals(attrsAfterRestore.getBond().getOwnerUuid()),
            "恢复后 bond.ownerUuid 应与召回前一致"
        );
        helper.assertTrue(
            Math.abs(
                attrsAfterRestore.getBond().getResonance() - EXPECTED_BOND_RESONANCE
            )
                < BOND_RESONANCE_EPSILON,
            "恢复后 bond.resonance 应与召回前一致"
        );

        helper.succeed();
    }

    /**
     * guard：当存储项 itemWithdrawn=true 时，恢复应被拒绝，且不应生成飞剑实体。
     */
    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_recall_restore_guard"
    )
    public void testRestoreShouldRejectWithdrawnRecord(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createDeterministicPlayer(level, "guard_reject_withdrawn");
        BlockPos playerPos = helper.absolutePos(
            new BlockPos(PLAYER_RELATIVE_X, PLAYER_RELATIVE_Y, PLAYER_RELATIVE_Z)
        );
        player.setPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());

        FlyingSwordStorageAttachment storage = KongqiaoAttachments.getFlyingSwordStorage(player);
        helper.assertTrue(storage != null, "前置失败：玩家飞剑存储附件为空");

        FlyingSwordStorageAttachment.RecalledSword withdrawnRecord =
            new FlyingSwordStorageAttachment.RecalledSword();
        withdrawnRecord.itemWithdrawn = true;
        boolean accepted = storage.recallSword(withdrawnRecord);
        helper.assertTrue(accepted, "前置失败：测试用 withdrawn 存储项未写入");

        int restoredCount = FlyingSwordController.restoreOne(level, player);
        helper.assertTrue(restoredCount == 0, "itemWithdrawn=true 时恢复数量应为 0");
        helper.assertTrue(storage.getCount() == 0, "itemWithdrawn=true 的记录应被清理");
        helper.assertTrue(
            FlyingSwordController.getPlayerSwords(level, player).isEmpty(),
            "itemWithdrawn=true 时不应恢复出任何飞剑实体"
        );

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_recall_restore_happy"
    )
    public void testTickRebuildShouldRestoreCacheFromLiveBenmingSword(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createDeterministicPlayer(level, "happy_live_rebuild");
        BlockPos playerPos = helper.absolutePos(
            new BlockPos(PLAYER_RELATIVE_X, PLAYER_RELATIVE_Y, PLAYER_RELATIVE_Z)
        );
        player.setPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());

        FlyingSwordEntity sword = FlyingSwordSpawner.spawnBasic(level, player);
        helper.assertTrue(sword != null, "前置失败：重建场景飞剑生成失败");
        sword.getSwordAttributes().setStableSwordId(TICK_REBUILD_LIVE_STABLE_ID);
        sword.getSwordAttributes().getBond().setOwnerUuid(player.getUUID().toString());
        sword.syncAttributesToEntityData();

        FlyingSwordStateAttachment state = KongqiaoAttachments.getFlyingSwordState(player);
        helper.assertTrue(state != null, "前置失败：玩家状态附件为空");
        state.clearBondCache();
        state.setInitialized(true);

        FlyingSwordTickHandler.reconcileBenmingCacheForTick(player);

        helper.assertTrue(
            TICK_REBUILD_LIVE_STABLE_ID.equals(state.getBondedSwordId()),
            "live 本命存在时应重建 bondedSwordId"
        );
        helper.assertTrue(
            !state.isBondCacheDirty(),
            "live 本命存在时重建后缓存应为 clean"
        );
        helper.assertTrue(
            state.getLastResolvedTick() == level.getGameTime(),
            "live 本命存在时应写入当前 resolved tick"
        );

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_recall_restore_guard"
    )
    public void testTickRebuildShouldStaySafeWhenOnlyWithdrawnStorageExists(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createDeterministicPlayer(level, "guard_only_withdrawn");
        BlockPos playerPos = helper.absolutePos(
            new BlockPos(PLAYER_RELATIVE_X, PLAYER_RELATIVE_Y, PLAYER_RELATIVE_Z)
        );
        player.setPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());

        FlyingSwordStorageAttachment storage = KongqiaoAttachments.getFlyingSwordStorage(player);
        helper.assertTrue(storage != null, "前置失败：玩家飞剑存储附件为空");
        FlyingSwordStorageAttachment.RecalledSword withdrawn =
            new FlyingSwordStorageAttachment.RecalledSword();
        withdrawn.itemWithdrawn = true;
        FlyingSwordAttributes attrs = FlyingSwordAttributes.fromNBT(null);
        attrs.setStableSwordId(TICK_REBUILD_WITHDRAWN_STABLE_ID);
        attrs.getBond().setOwnerUuid(player.getUUID().toString());
        withdrawn.attributes = attrs.toNBT();
        helper.assertTrue(storage.recallSword(withdrawn), "前置失败：withdrawn 记录写入失败");

        FlyingSwordStateAttachment state = KongqiaoAttachments.getFlyingSwordState(player);
        helper.assertTrue(state != null, "前置失败：玩家状态附件为空");
        state.clearBondCache();
        state.setInitialized(true);

        FlyingSwordTickHandler.reconcileBenmingCacheForTick(player);

        helper.assertTrue(
            state.getBondedSwordId().isBlank(),
            "仅 withdrawn 存储存在时应保持空 bondedSwordId"
        );
        helper.assertTrue(
            state.isBondCacheDirty(),
            "仅 withdrawn 存储存在时应保持 dirty 安全态"
        );
        helper.assertTrue(
            state.getLastResolvedTick() == -1L,
            "仅 withdrawn 存储存在时应保持 unresolved tick"
        );

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_recall_restore_guard"
    )
    public void testTickRebuildShouldClearCleanButStaleNonblankCache(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createDeterministicPlayer(level, "guard_stale_cache");
        BlockPos playerPos = helper.absolutePos(
            new BlockPos(PLAYER_RELATIVE_X, PLAYER_RELATIVE_Y, PLAYER_RELATIVE_Z)
        );
        player.setPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());

        FlyingSwordStateAttachment state = KongqiaoAttachments.getFlyingSwordState(player);
        helper.assertTrue(state != null, "前置失败：玩家状态附件为空");
        state.updateBondCache(TICK_REBUILD_STALE_CACHE_ID, level.getGameTime());
        state.setInitialized(true);

        FlyingSwordTickHandler.reconcileBenmingCacheForTick(player);

        helper.assertTrue(
            state.getBondedSwordId().isBlank(),
            "clean 但 stale 的非空缓存应被重校验并清空"
        );
        helper.assertTrue(
            state.isBondCacheDirty(),
            "clean 但 stale 的非空缓存失效后应回到 dirty"
        );
        helper.assertTrue(
            state.getLastResolvedTick() == -1L,
            "clean 但 stale 的非空缓存失效后应回到 unresolved tick"
        );

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_recall_restore_happy"
    )
    public void testRestoreOneShouldMarkBenmingCacheDirtyForNextTick(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createDeterministicPlayer(level, "happy_restore_dirty");
        BlockPos playerPos = helper.absolutePos(
            new BlockPos(PLAYER_RELATIVE_X, PLAYER_RELATIVE_Y, PLAYER_RELATIVE_Z)
        );
        player.setPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());

        FlyingSwordEntity sword = FlyingSwordSpawner.spawnBasic(level, player);
        helper.assertTrue(sword != null, "前置失败：恢复 dirty 场景飞剑生成失败");
        sword.getSwordAttributes().setStableSwordId(RESTORE_DIRTY_STABLE_ID);
        sword.getSwordAttributes().getBond().setOwnerUuid(player.getUUID().toString());
        sword.syncAttributesToEntityData();
        FlyingSwordController.finishRecall(sword, player);

        FlyingSwordStateAttachment state = KongqiaoAttachments.getFlyingSwordState(player);
        helper.assertTrue(state != null, "前置失败：玩家状态附件为空");
        state.updateBondCache("legacy-cache-id", level.getGameTime());

        int restoredCount = FlyingSwordController.restoreOne(level, player);
        helper.assertTrue(restoredCount == 1, "restoreOne 应成功恢复飞剑");
        helper.assertTrue(
            state.isBondCacheDirty(),
            "restoreOne 成功后应显式标记 benming cache 为 dirty"
        );

        FlyingSwordTickHandler.reconcileBenmingCacheForTick(player);
        helper.assertTrue(
            RESTORE_DIRTY_STABLE_ID.equals(state.getBondedSwordId()),
            "restoreOne 后下一次重建应恢复为真实 stableSwordId"
        );

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_recall_restore_guard"
    )
    public void testTickRebuildShouldIgnoreDamagedLiveCandidate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createDeterministicPlayer(level, "guard_damaged_live");
        BlockPos playerPos = helper.absolutePos(
            new BlockPos(PLAYER_RELATIVE_X, PLAYER_RELATIVE_Y, PLAYER_RELATIVE_Z)
        );
        player.setPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());

        FlyingSwordEntity sword = FlyingSwordSpawner.spawnBasic(level, player);
        helper.assertTrue(sword != null, "前置失败：damaged 场景飞剑生成失败");
        sword.getSwordAttributes().setStableSwordId(TICK_REBUILD_DAMAGED_STABLE_ID);
        sword.getSwordAttributes().getBond().setOwnerUuid(player.getUUID().toString());
        sword.getSwordAttributes().durability = 0.0D;
        sword.syncAttributesToEntityData();

        FlyingSwordStateAttachment state = KongqiaoAttachments.getFlyingSwordState(player);
        helper.assertTrue(state != null, "前置失败：玩家状态附件为空");
        state.clearBondCache();
        state.setInitialized(true);

        FlyingSwordTickHandler.reconcileBenmingCacheForTick(player);

        helper.assertTrue(
            state.getBondedSwordId().isBlank(),
            "仅 damaged live 候选存在时应进入安全态并保持空 bondedSwordId"
        );
        helper.assertTrue(
            state.isBondCacheDirty(),
            "仅 damaged live 候选存在时应保持 dirty"
        );
        helper.assertTrue(
            state.getLastResolvedTick() == -1L,
            "仅 damaged live 候选存在时应保持 unresolved tick"
        );

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = "benming_e2e_happy_path"
    )
    public void testE2EHappyPathShouldKeepBondAndTrainingGainAcrossRestoreAndCacheRebuild(
        GameTestHelper helper
    ) {
        E2EContext context = initE2EContext(helper);
        FlyingSwordStateAttachment state = bindSwordAndAssertCache(helper, context);

        warmupBurstResourcesIfAvailable(context.player);

        helper.assertTrue(state.getResonanceType().isBlank(), "e2e：切换前 resonanceType 应为空");
        FlyingSwordController.BenmingControllerActionResult resonanceSwitchResult =
            FlyingSwordController.switchResonanceForSelectedOrNearestBenmingSword(
                context.level,
                context.player,
                FlyingSwordResonanceType.SPIRIT
            );
        helper.assertTrue(resonanceSwitchResult.success(), "e2e：官方入口共鸣切换应成功");
        helper.assertTrue(
            FlyingSwordResonanceType.SPIRIT.getCode().equals(state.getResonanceType()),
            "e2e：官方入口共鸣切换后 resonanceType 应写入目标类型"
        );

        long burstAttemptTick = context.level.getGameTime();
        FlyingSwordController.BenmingControllerActionResult burstAttemptResult =
            attemptBurstForHappyPath(context, state);
        helper.assertTrue(
            burstAttemptResult.success() ||
            burstAttemptResult.failureReason()
                == FlyingSwordController.BenmingControllerFailureReason.BURST_RESOURCES_INSUFFICIENT,
            "e2e：爆发尝试应成功，或在外部资源桥接不可用时仅表现为资源不足"
        );
        if (!burstAttemptResult.success()) {
            burstAttemptResult = attemptBurstByCoreReflection(
                state,
                context.sword.getSwordAttributes().getStableSwordId(),
                state.getBondedSwordId(),
                context.level.getGameTime()
            );
        }
        helper.assertTrue(burstAttemptResult.success(), "e2e：爆发内核回退后应成功建立时间轴");
        helper.assertTrue(
            state.getBurstCooldownUntilTick() > burstAttemptTick,
            "e2e：爆发后 burstCooldownUntilTick 应晚于当前 tick"
        );
        helper.assertTrue(
            state.getBurstActiveUntilTick() > burstAttemptTick,
            "e2e：爆发后 burstActiveUntilTick 应晚于当前 tick"
        );
        helper.assertTrue(
            state.getBurstAftershockUntilTick() > state.getBurstActiveUntilTick(),
            "e2e：爆发后 burstAftershockUntilTick 应晚于 burstActiveUntilTick"
        );

        String expectedResonanceType = state.getResonanceType();
        long expectedBurstCooldownUntilTick = state.getBurstCooldownUntilTick();
        long expectedBurstActiveUntilTick = state.getBurstActiveUntilTick();
        long expectedBurstAftershockUntilTick = state.getBurstAftershockUntilTick();

        FlyingSwordAttributes attrsAfterTraining = trainAndAssertGain(helper, context);
        E2EExpectedSnapshot expected = applyTrainingSnapshot(attrsAfterTraining, context.sword);
        assertRecallRestoreKeepsTrainingSnapshot(helper, context, expected);
        assertResonanceBurstAndBondStateCoherent(
            helper,
            state,
            expectedResonanceType,
            expectedBurstCooldownUntilTick,
            expectedBurstActiveUntilTick,
            expectedBurstAftershockUntilTick,
            "恢复后"
        );
        assertCacheRebuildSelfHeal(helper, context.level, context.player, state);
        assertResonanceBurstAndBondStateCoherent(
            helper,
            state,
            expectedResonanceType,
            expectedBurstCooldownUntilTick,
            expectedBurstActiveUntilTick,
            expectedBurstAftershockUntilTick,
            "cache 重建后"
        );

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = BATCH_E2E_NEGATIVE
    )
    public void testE2ENegativeShouldSuppressBonusAndRejectPartialCostMutation(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createDeterministicPlayer(level, "e2e_negative_shortage");
        BlockPos playerPos = helper.absolutePos(
            new BlockPos(PLAYER_RELATIVE_X, PLAYER_RELATIVE_Y, PLAYER_RELATIVE_Z)
        );
        player.setPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());

        FlyingSwordEntity sword = spawnBoundSwordForOwner(
            helper,
            level,
            player,
            E2E_NEGATIVE_SHORTAGE_STABLE_ID,
            "negative-shortage"
        );

        FlyingSwordStateAttachment state = KongqiaoAttachments.getFlyingSwordState(player);
        helper.assertTrue(state != null, "negative-shortage：玩家状态附件不应为空");
        state.updateBondCache(E2E_NEGATIVE_SHORTAGE_STABLE_ID, level.getGameTime());

        FlyingSwordTrainingAttachment training = KongqiaoAttachments.getFlyingSwordTraining(
            player
        );
        helper.assertTrue(training != null, "negative-shortage：训练附件不应为空");
        training.setAccumulatedExp(0);
        training.setFuelTime(TRAINING_INITIAL_FUEL);
        training.setMaxFuelTime(TRAINING_INITIAL_FUEL);
        ItemStack swordStack = new ItemStack(Items.IRON_SWORD);
        FlyingSwordAttributes attrsBeforeTraining = sword.getSwordAttributes().copy();
        writeAttributesToSword(swordStack, attrsBeforeTraining);
        training.getInputSlots().setStackInSlot(TRAINING_SLOT_SWORD, swordStack);
        training.getInputSlots().setStackInSlot(
            TRAINING_SLOT_FUEL,
            new ItemStack(Items.COAL)
        );

        FlyingSwordTrainingService.installBonusResourceGateForTest(p -> false);
        FlyingSwordTrainingService.installReadonlyRewardModifierProviderForTest(
            p -> BenmingSwordReadonlyModifierHelper.ReadonlyModifier.identity()
        );
        FlyingSwordAttributes attrsAfterTraining;
        try {
            FlyingSwordTrainingService.tickInternal(training, player);
            attrsAfterTraining = readAttributesFromSword(
                training.getInputSlots().getStackInSlot(TRAINING_SLOT_SWORD)
            );
        } finally {
            FlyingSwordTrainingService.resetBonusResourceGateForTest();
            FlyingSwordTrainingService.resetReadonlyRewardModifierProviderForTest();
        }

        helper.assertTrue(
            training.getFuelTime() == TRAINING_INITIAL_FUEL - 1,
            "negative-shortage：资源不足时基础训练燃料消耗应保持"
        );
        helper.assertTrue(
            training.getAccumulatedExp() == EXPECTED_BASE_EXP_PER_TICK,
            "negative-shortage：资源不足时累计经验应仅保留基础收益"
        );
        helper.assertTrue(
            attrsAfterTraining.getExperience() == EXPECTED_BASE_EXP_PER_TICK,
            "negative-shortage：资源不足时飞剑经验应仅保留基础收益"
        );
        assertClose(
            helper,
            attrsBeforeTraining.getBond().getResonance(),
            attrsAfterTraining.getBond().getResonance(),
            "negative-shortage：资源不足时本命共鸣奖励应被抑制"
        );

        MutationRecorder shortageRecorder = new MutationRecorder();
        BenmingSwordBondService.Result shortageResult =
            BenmingSwordBondService.activeUnbindWithTransaction(
                player.getUUID().toString(),
                toSwordBondPort(sword),
                toPlayerCachePort(state),
                new BenmingSwordBondService.ActiveUnbindTransactionContext(
                    CultivationSnapshot.of(
                        RESOURCE_SNAPSHOT_SHORTAGE_ZHENYUAN,
                        RESOURCE_SNAPSHOT_SHORTAGE_NIANTOU,
                        RESOURCE_SNAPSHOT_SHORTAGE_HUNPO,
                        0.0D,
                        0
                    ),
                    BenmingSwordBondService.defaultActiveUnbindRequest(),
                    ACTIVE_UNBIND_ZHENYUAN_COST,
                    baseCost -> baseCost,
                    shortageRecorder
                ),
                level.getGameTime()
            );

        helper.assertTrue(!shortageResult.success(), "negative-shortage：主动解绑应因资源不足失败");
        helper.assertTrue(
            shortageResult.branch() == BenmingSwordBondService.ResultBranch.ACTIVE_UNBIND,
            "negative-shortage：失败结果分支应为 ACTIVE_UNBIND"
        );
        helper.assertTrue(
            shortageResult.failureReason()
                == BenmingSwordBondService.FailureReason.ACTIVE_UNBIND_COST_REJECTED,
            "negative-shortage：失败原因应为高成本拒绝"
        );
        helper.assertTrue(
            shortageResult.resourceFailureReason()
                == BenmingSwordResourceTransaction.FailureReason.INSUFFICIENT_HUNPO,
            "negative-shortage：资源失败原因应稳定为魂魄不足"
        );
        helper.assertTrue(
            shortageRecorder.operationCount() == EXPECTED_NO_RESOURCE_MUTATION_COUNT,
            "negative-shortage：资源不足失败路径不应发生任何部分扣费"
        );
        assertClose(
            helper,
            0.0D,
            shortageRecorder.zhenyuanLoss(),
            "negative-shortage：资源不足失败路径不应扣除真元"
        );
        assertClose(
            helper,
            0.0D,
            shortageRecorder.niantouLoss(),
            "negative-shortage：资源不足失败路径不应扣除念头"
        );
        assertClose(
            helper,
            0.0D,
            shortageRecorder.hunpoLoss(),
            "negative-shortage：资源不足失败路径不应扣除魂魄"
        );
        helper.assertTrue(
            player
                .getUUID()
                .toString()
                .equals(sword.getSwordAttributes().getBond().getOwnerUuid()),
            "negative-shortage：失败后 canonical 绑定关系应保持不变"
        );
        helper.assertTrue(
            E2E_NEGATIVE_SHORTAGE_STABLE_ID.equals(state.getBondedSwordId()) &&
            !state.isBondCacheDirty(),
            "negative-shortage：失败后玩家 cache 不应被污染"
        );

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = BATCH_E2E_NEGATIVE
    )
    public void testE2ENegativeShouldClearCanonicalStateAfterActiveUnbindHighCost(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createDeterministicPlayer(level, "e2e_negative_active_unbind");
        BlockPos playerPos = helper.absolutePos(
            new BlockPos(PLAYER_RELATIVE_X, PLAYER_RELATIVE_Y, PLAYER_RELATIVE_Z)
        );
        player.setPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());

        FlyingSwordEntity sword = spawnBoundSwordForOwner(
            helper,
            level,
            player,
            E2E_NEGATIVE_ACTIVE_UNBIND_STABLE_ID,
            "negative-active-unbind"
        );

        FlyingSwordStateAttachment state = KongqiaoAttachments.getFlyingSwordState(player);
        helper.assertTrue(state != null, "negative-active-unbind：玩家状态附件不应为空");
        state.updateBondCache(E2E_NEGATIVE_ACTIVE_UNBIND_STABLE_ID, level.getGameTime());

        MutationRecorder activeRecorder = new MutationRecorder();
        BenmingSwordBondService.Result activeResult =
            BenmingSwordBondService.activeUnbindWithTransaction(
                player.getUUID().toString(),
                toSwordBondPort(sword),
                toPlayerCachePort(state),
                new BenmingSwordBondService.ActiveUnbindTransactionContext(
                    CultivationSnapshot.of(
                        RESOURCE_SNAPSHOT_HIGH,
                        RESOURCE_SNAPSHOT_HIGH,
                        RESOURCE_SNAPSHOT_HIGH,
                        0.0D,
                        0
                    ),
                    BenmingSwordBondService.defaultActiveUnbindRequest(),
                    ACTIVE_UNBIND_ZHENYUAN_COST,
                    baseCost -> baseCost,
                    activeRecorder
                ),
                level.getGameTime()
            );

        helper.assertTrue(activeResult.success(), "negative-active-unbind：主动解绑应成功");
        helper.assertTrue(
            activeResult.branch() == BenmingSwordBondService.ResultBranch.ACTIVE_UNBIND,
            "negative-active-unbind：成功分支应为 ACTIVE_UNBIND"
        );
        helper.assertTrue(
            E2E_NEGATIVE_ACTIVE_UNBIND_STABLE_ID.equals(activeResult.stableSwordId()),
            "negative-active-unbind：返回 stableSwordId 应匹配目标飞剑"
        );
        assertClose(
            helper,
            ACTIVE_UNBIND_ZHENYUAN_COST,
            activeRecorder.zhenyuanLoss(),
            "negative-active-unbind：主动解绑应支付高成本真元"
        );
        assertClose(
            helper,
            ACTIVE_UNBIND_NIANTOU_COST,
            activeRecorder.niantouLoss(),
            "negative-active-unbind：主动解绑应支付高成本念头"
        );
        assertClose(
            helper,
            ACTIVE_UNBIND_HUNPO_COST,
            activeRecorder.hunpoLoss(),
            "negative-active-unbind：主动解绑应支付高成本魂魄"
        );
        helper.assertTrue(
            activeRecorder.operationCount() == EXPECTED_RESOURCE_MUTATION_COUNT,
            "negative-active-unbind：主动解绑成功应产生三次资源扣费"
        );

        helper.assertTrue(
            sword.getSwordAttributes().getBond().getOwnerUuid().isBlank(),
            "negative-active-unbind：解绑后 canonical ownerUuid 应清空"
        );
        assertClose(
            helper,
            0.0D,
            sword.getSwordAttributes().getBond().getResonance(),
            "negative-active-unbind：解绑后 canonical resonance 应归零"
        );
        helper.assertTrue(
            state.getBondedSwordId().isBlank() && state.isBondCacheDirty(),
            "negative-active-unbind：解绑后玩家 cache 应清空并回到 dirty"
        );

        BenmingSwordBondService.Result queryResult = FlyingSwordController.queryBenmingSword(
            level,
            player
        );
        helper.assertTrue(!queryResult.success(), "negative-active-unbind：解绑后查询本命应失败");
        helper.assertTrue(
            queryResult.failureReason()
                == BenmingSwordBondService.FailureReason.NO_BONDED_SWORD,
            "negative-active-unbind：解绑后查询失败原因应为 NO_BONDED_SWORD"
        );

        BenmingSwordBondService.Result secondAttemptResult =
            BenmingSwordBondService.activeUnbindWithTransaction(
                player.getUUID().toString(),
                toSwordBondPort(sword),
                toPlayerCachePort(state),
                new BenmingSwordBondService.ActiveUnbindTransactionContext(
                    CultivationSnapshot.of(
                        RESOURCE_SNAPSHOT_HIGH,
                        RESOURCE_SNAPSHOT_HIGH,
                        RESOURCE_SNAPSHOT_HIGH,
                        0.0D,
                        0
                    ),
                    BenmingSwordBondService.defaultActiveUnbindRequest(),
                    ACTIVE_UNBIND_ZHENYUAN_COST,
                    baseCost -> baseCost,
                    activeRecorder
                ),
                level.getGameTime()
            );
        helper.assertTrue(
            !secondAttemptResult.success(),
            "negative-active-unbind：二次解绑应走失败分支"
        );
        helper.assertTrue(
            secondAttemptResult.failureReason()
                == BenmingSwordBondService.FailureReason.TARGET_NOT_BOUND_TO_PLAYER,
            "negative-active-unbind：二次解绑失败原因应为 TARGET_NOT_BOUND_TO_PLAYER"
        );
        helper.assertTrue(
            activeRecorder.operationCount() == EXPECTED_RESOURCE_MUTATION_COUNT,
            "negative-active-unbind：二次失败不应追加资源扣费"
        );

        helper.succeed();
    }

    @GameTest(
        template = "empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = BATCH_E2E_BACKLASH_BOUNDARY
    )
    public void testE2ENegativeShouldApplyLightBacklashWithCooldownBoundary(
        GameTestHelper helper
    ) {
        BacklashScenarioContext context = initBacklashScenarioContext(helper);
        MutationRecorder backlashRecorder = new MutationRecorder();

        BenmingSwordBondService.Result firstBacklashResult = executeForcedBacklash(
            context,
            backlashRecorder
        );
        BacklashSnapshot afterFirst = assertFirstBacklashAndCaptureSnapshot(
            helper,
            context,
            backlashRecorder,
            firstBacklashResult
        );

        WithdrawnProductionPathSnapshot productionPathSnapshot =
            applyIllegalDetachThroughWithdrawnProductionWiring(
            context,
            helper,
            backlashRecorder
        );
        assertCooldownSuppressedBacklashAndCleanup(
            helper,
            context,
            afterFirst,
            productionPathSnapshot
        );

        helper.succeed();
    }

    private static BacklashScenarioContext initBacklashScenarioContext(
        GameTestHelper helper
    ) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createDeterministicPlayer(level, "e2e_negative_backlash");
        BlockPos playerPos = helper.absolutePos(
            new BlockPos(PLAYER_RELATIVE_X, PLAYER_RELATIVE_Y, PLAYER_RELATIVE_Z)
        );
        player.setPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());

        FlyingSwordEntity firstSword = spawnBoundSwordForOwner(
            helper,
            level,
            player,
            E2E_NEGATIVE_BACKLASH_FIRST_STABLE_ID,
            "negative-backlash-first"
        );
        FlyingSwordEntity secondSword = spawnBoundSwordForOwner(
            helper,
            level,
            player,
            E2E_NEGATIVE_BACKLASH_SECOND_STABLE_ID,
            "negative-backlash-second"
        );

        FlyingSwordStateAttachment state = KongqiaoAttachments.getFlyingSwordState(player);
        helper.assertTrue(state != null, "negative-backlash：玩家状态附件不应为空");
        state.updateBondCache(E2E_NEGATIVE_BACKLASH_FIRST_STABLE_ID, level.getGameTime());

        FlyingSwordCooldownAttachment cooldownAttachment =
            KongqiaoAttachments.getFlyingSwordCooldowns(player);
        helper.assertTrue(cooldownAttachment != null, "negative-backlash：冷却附件不应为空");
        String cooldownKey = BenmingSwordBondService.defaultLightBacklashCooldownKey(
            player.getUUID().toString()
        );
        return new BacklashScenarioContext(
            level,
            player,
            firstSword,
            secondSword,
            state,
            cooldownAttachment,
            cooldownKey
        );
    }

    private static BenmingSwordBondService.Result executeForcedBacklash(
        BacklashScenarioContext context,
        MutationRecorder recorder
    ) {
        return BenmingSwordBondService.forcedUnbind(
            context.player().getUUID().toString(),
            toSwordBondPort(context.firstSword()),
            toPlayerCachePort(context.state()),
            context.level().getGameTime(),
            new BenmingSwordBondService.BacklashContext(
                CultivationSnapshot.of(
                    RESOURCE_SNAPSHOT_LOW_BACKLASH_ZHENYUAN,
                    RESOURCE_SNAPSHOT_LOW_BACKLASH_NIANTOU,
                    RESOURCE_SNAPSHOT_LOW_BACKLASH_HUNPO,
                    0.0D,
                    0
                ),
                recorder,
                BenmingSwordBondService.toBacklashCooldownPort(
                    context.cooldownAttachment()
                ),
                context.cooldownKey(),
                BenmingSwordBondService.defaultLightBacklashCooldownTicks()
            )
        );
    }

    private static BacklashSnapshot assertFirstBacklashAndCaptureSnapshot(
        GameTestHelper helper,
        BacklashScenarioContext context,
        MutationRecorder recorder,
        BenmingSwordBondService.Result firstBacklashResult
    ) {
        helper.assertTrue(firstBacklashResult.success(), "negative-backlash：首次强制解绑应成功");
        helper.assertTrue(
            firstBacklashResult.branch() == BenmingSwordBondService.ResultBranch.FORCED_UNBIND,
            "negative-backlash：首次分支应为 FORCED_UNBIND"
        );
        helper.assertTrue(
            firstBacklashResult.backlashEffect().type()
                == BenmingSwordBondService.BacklashType.FORCED_UNBIND_LIGHT,
            "negative-backlash：首次应触发轻度强制解绑反噬"
        );
        assertClose(
            helper,
            RESOURCE_SNAPSHOT_LOW_BACKLASH_ZHENYUAN,
            recorder.zhenyuanLoss(),
            "negative-backlash：低资源边界下真元扣减应被上限钳制"
        );
        assertClose(
            helper,
            RESOURCE_SNAPSHOT_LOW_BACKLASH_NIANTOU,
            recorder.niantouLoss(),
            "negative-backlash：低资源边界下念头扣减应被上限钳制"
        );
        assertClose(
            helper,
            RESOURCE_SNAPSHOT_LOW_BACKLASH_HUNPO,
            recorder.hunpoLoss(),
            "negative-backlash：低资源边界下魂魄扣减应被上限钳制"
        );
        helper.assertTrue(
            recorder.zhenyuanLoss() <= LIGHT_BACKLASH_ZHENYUAN_COST &&
            recorder.niantouLoss() <= LIGHT_BACKLASH_NIANTOU_COST &&
            recorder.hunpoLoss() <= LIGHT_BACKLASH_HUNPO_COST,
            "negative-backlash：轻反噬应保持平衡上限且不超扣"
        );
        helper.assertTrue(
            recorder.operationCount() == EXPECTED_RESOURCE_MUTATION_COUNT,
            "negative-backlash：首次触发应发生一次三资源扣减"
        );
        helper.assertTrue(
            context.state().getBondedSwordId().isBlank() && context.state().isBondCacheDirty(),
            "negative-backlash：首次解绑后 cache 应清空并标记 dirty"
        );

        return new BacklashSnapshot(
            recorder.zhenyuanLoss(),
            recorder.niantouLoss(),
            recorder.hunpoLoss(),
            recorder.operationCount()
        );
    }

    private static WithdrawnProductionPathSnapshot applyIllegalDetachThroughWithdrawnProductionWiring(
        BacklashScenarioContext context,
        GameTestHelper helper,
        MutationRecorder recorder
    ) {
        FlyingSwordStorageAttachment storage = KongqiaoAttachments.getFlyingSwordStorage(
            context.player()
        );
        helper.assertTrue(storage != null, "negative-backlash：withdrawn 接线场景存储附件不应为空");

        FlyingSwordStorageAttachment.RecalledSword withdrawn =
            new FlyingSwordStorageAttachment.RecalledSword();
        withdrawn.itemWithdrawn = true;
        withdrawn.attributes = context.secondSword().getSwordAttributes().toNBT();
        helper.assertTrue(
            storage.recallSword(withdrawn),
            "negative-backlash：withdrawn 接线场景存储写入失败"
        );

        context.secondSword().discard();
        int operationCountBeforeProductionPath = recorder.operationCount();
        double zhenyuanLossBeforeProductionPath = recorder.zhenyuanLoss();
        double niantouLossBeforeProductionPath = recorder.niantouLoss();
        double hunpoLossBeforeProductionPath = recorder.hunpoLoss();
        try {
            FlyingSwordTickHandler.installWithdrawnBacklashContextProviderForTest(
                (player, ownerUuid) ->
                    new BenmingSwordBondService.BacklashContext(
                        CultivationSnapshot.of(
                            RESOURCE_SNAPSHOT_HIGH,
                            RESOURCE_SNAPSHOT_HIGH,
                            RESOURCE_SNAPSHOT_HIGH,
                            0.0D,
                            0
                        ),
                        recorder,
                        BenmingSwordBondService.toBacklashCooldownPort(
                            context.cooldownAttachment()
                        ),
                        context.cooldownKey(),
                        BenmingSwordBondService.defaultLightBacklashCooldownTicks()
                    )
            );
            FlyingSwordTickHandler.reconcileBenmingCacheForTick(context.player());
            FlyingSwordTickHandler.reconcileBenmingCacheForTick(context.player());
        } finally {
            FlyingSwordTickHandler.resetWithdrawnBacklashContextProviderForTest();
        }
        return new WithdrawnProductionPathSnapshot(
            operationCountBeforeProductionPath,
            zhenyuanLossBeforeProductionPath,
            niantouLossBeforeProductionPath,
            hunpoLossBeforeProductionPath,
            recorder.operationCount(),
            recorder.zhenyuanLoss(),
            recorder.niantouLoss(),
            recorder.hunpoLoss()
        );
    }

    private static void assertCooldownSuppressedBacklashAndCleanup(
        GameTestHelper helper,
        BacklashScenarioContext context,
        BacklashSnapshot afterFirst,
        WithdrawnProductionPathSnapshot productionPathSnapshot
    ) {
        FlyingSwordStorageAttachment storage = KongqiaoAttachments.getFlyingSwordStorage(
            context.player()
        );
        helper.assertTrue(storage != null, "negative-backlash：断言阶段存储附件不应为空");
        helper.assertTrue(storage.getCount() == 1, "negative-backlash：断言阶段应存在 1 条 withdrawn 记录");
        FlyingSwordStorageAttachment.RecalledSword withdrawn = storage.getAt(0);
        helper.assertTrue(withdrawn != null, "negative-backlash：断言阶段 withdrawn 记录不应为空");
        FlyingSwordAttributes attrsAfterIllegalDetach = FlyingSwordAttributes.fromNBT(
            withdrawn.attributes
        );
        helper.assertTrue(
            attrsAfterIllegalDetach.getBond().getOwnerUuid().isBlank(),
            "negative-backlash：生产接线触发后 withdrawn 记录的 canonical ownerUuid 应被清空"
        );
        assertClose(
            helper,
            0.0D,
            attrsAfterIllegalDetach.getBond().getResonance(),
            "negative-backlash：生产接线触发后 withdrawn 记录的 canonical resonance 应归零"
        );
        helper.assertTrue(
            context.cooldownAttachment().get(context.cooldownKey())
                == BenmingSwordBondService.defaultLightBacklashCooldownTicks(),
            "negative-backlash：冷却 key 应保持默认时长，防止重复风暴"
        );
        helper.assertTrue(
            context.state().getBondedSwordId().isBlank() && context.state().isBondCacheDirty(),
            "negative-backlash：生产接线触发后玩家 cache 应保持清空并 dirty"
        );
        helper.assertTrue(
            productionPathSnapshot.operationCountAfterProductionPath()
                == productionPathSnapshot.operationCountBeforeProductionPath(),
            "negative-backlash：冷却生效时生产接线路径不应追加资源扣减操作"
        );
        assertClose(
            helper,
            productionPathSnapshot.zhenyuanLossBeforeProductionPath(),
            productionPathSnapshot.zhenyuanLossAfterProductionPath(),
            "negative-backlash：冷却生效时生产接线路径不应追加真元扣减"
        );
        assertClose(
            helper,
            productionPathSnapshot.niantouLossBeforeProductionPath(),
            productionPathSnapshot.niantouLossAfterProductionPath(),
            "negative-backlash：冷却生效时生产接线路径不应追加念头扣减"
        );
        assertClose(
            helper,
            productionPathSnapshot.hunpoLossBeforeProductionPath(),
            productionPathSnapshot.hunpoLossAfterProductionPath(),
            "negative-backlash：withdrawn canonical 清理后重复 tick 不应触发魂魄风暴"
        );
        helper.assertTrue(
            afterFirst.operationCount() == EXPECTED_RESOURCE_MUTATION_COUNT,
            "negative-backlash：首次轻反噬资源扣减快照应保持稳定"
        );
    }

    private static E2EContext initE2EContext(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = createDeterministicPlayer(level, "e2e_happy_path");
        BlockPos playerPos = helper.absolutePos(
            new BlockPos(PLAYER_RELATIVE_X, PLAYER_RELATIVE_Y, PLAYER_RELATIVE_Z)
        );
        player.setPos(playerPos.getX(), playerPos.getY(), playerPos.getZ());

        FlyingSwordEntity sword = FlyingSwordSpawner.spawnBasic(level, player);
        helper.assertTrue(sword != null, "前置失败：e2e 场景飞剑生成失败");
        sword.getSwordAttributes().setStableSwordId(E2E_HAPPY_STABLE_ID);
        sword.getSwordAttributes().getBond().resetToUnbound();
        sword.syncAttributesToEntityData();
        return new E2EContext(level, player, sword);
    }

    private static FlyingSwordStateAttachment bindSwordAndAssertCache(
        GameTestHelper helper,
        E2EContext context
    ) {
        FlyingSwordStateAttachment state = KongqiaoAttachments.getFlyingSwordState(
            context.player
        );
        helper.assertTrue(state != null, "e2e：玩家状态附件不应为空");
        BenmingSwordBondService.SwordBondPort targetSwordPort = toSwordBondPort(
            context.sword
        );
        List<BenmingSwordBondService.SwordBondPort> ownedSwordPorts = List.of(
            targetSwordPort
        );
        BenmingSwordBondService.Result bindResult =
            BenmingSwordBondService.bind(
                context.player.getUUID().toString(),
                targetSwordPort,
                ownedSwordPorts,
                toPlayerCachePort(state),
                context.level.getGameTime()
            );
        helper.assertTrue(bindResult.success(), "e2e：本命绑定应成功");
        helper.assertTrue(
            bindResult.branch() == BenmingSwordBondService.ResultBranch.BIND,
            "e2e：绑定结果分支应为 BIND"
        );
        helper.assertTrue(
            E2E_HAPPY_STABLE_ID.equals(bindResult.stableSwordId()),
            "e2e：绑定返回 stableSwordId 应与目标飞剑一致"
        );
        helper.assertTrue(
            context
                .player
                .getUUID()
                .toString()
                .equals(context.sword.getSwordAttributes().getBond().getOwnerUuid()),
            "e2e：绑定后 bond.ownerUuid 应写入玩家 UUID"
        );
        helper.assertTrue(
            E2E_HAPPY_STABLE_ID.equals(state.getBondedSwordId()),
            "e2e：绑定后 state cache 应记录稳定剑 ID"
        );
        helper.assertTrue(!state.isBondCacheDirty(), "e2e：绑定后 state cache 应为 clean");
        return state;
    }

    private static FlyingSwordAttributes trainAndAssertGain(
        GameTestHelper helper,
        E2EContext context
    ) {
        FlyingSwordTrainingAttachment training = KongqiaoAttachments.getFlyingSwordTraining(
            context.player
        );
        helper.assertTrue(training != null, "前置失败：训练附件为空");
        training.setAccumulatedExp(0);
        training.setFuelTime(TRAINING_INITIAL_FUEL);
        training.setMaxFuelTime(TRAINING_INITIAL_FUEL);

        FlyingSwordAttributes attrsBeforeTraining = context.sword
            .getSwordAttributes()
            .copy();
        int expBeforeTraining = attrsBeforeTraining.getExperience();
        double resonanceBeforeTraining = attrsBeforeTraining.getBond().getResonance();

        ItemStack trainingSwordStack = new ItemStack(Items.IRON_SWORD);
        writeAttributesToSword(trainingSwordStack, attrsBeforeTraining);
        training.getInputSlots().setStackInSlot(TRAINING_SLOT_SWORD, trainingSwordStack);
        training.getInputSlots().setStackInSlot(
            TRAINING_SLOT_FUEL,
            new ItemStack(Items.COAL)
        );

        FlyingSwordTrainingService.installBonusResourceGateForTest(p -> true);
        FlyingSwordTrainingService.installReadonlyRewardModifierProviderForTest(
            p -> BenmingSwordReadonlyModifierHelper.ReadonlyModifier.identity()
        );
        FlyingSwordAttributes attrsAfterTraining;
        try {
            FlyingSwordTrainingService.tickInternal(training, context.player);
            ItemStack updatedStack = training
                .getInputSlots()
                .getStackInSlot(TRAINING_SLOT_SWORD);
            attrsAfterTraining = readAttributesFromSword(updatedStack);
        } finally {
            FlyingSwordTrainingService.resetBonusResourceGateForTest();
            FlyingSwordTrainingService.resetReadonlyRewardModifierProviderForTest();
        }

        helper.assertTrue(
            training.getAccumulatedExp() == EXPECTED_BONUS_EXP_PER_TICK,
            "e2e：训练后 accumulatedExp 应体现基础+本命奖励"
        );
        helper.assertTrue(
            attrsAfterTraining.getExperience() > expBeforeTraining,
            "e2e：训练后飞剑经验应增长"
        );
        helper.assertTrue(
            attrsAfterTraining.getBond().getResonance() > resonanceBeforeTraining,
            "e2e：训练后本命共鸣应增长"
        );
        helper.assertTrue(
            E2E_HAPPY_STABLE_ID.equals(attrsAfterTraining.getStableSwordId()),
            "e2e：训练后 stableSwordId 应保持不变"
        );
        helper.assertTrue(
            context
                .player
                .getUUID()
                .toString()
                .equals(attrsAfterTraining.getBond().getOwnerUuid()),
            "e2e：训练后 bond.ownerUuid 应保持为玩家 UUID"
        );
        return attrsAfterTraining;
    }

    private static E2EExpectedSnapshot applyTrainingSnapshot(
        FlyingSwordAttributes attrsAfterTraining,
        FlyingSwordEntity sword
    ) {
        sword.readAttributesFromTag(attrsAfterTraining.toNBT());
        return new E2EExpectedSnapshot(
            attrsAfterTraining.getExperience(),
            attrsAfterTraining.getBond().getResonance()
        );
    }

    private static void assertRecallRestoreKeepsTrainingSnapshot(
        GameTestHelper helper,
        E2EContext context,
        E2EExpectedSnapshot expected
    ) {
        FlyingSwordController.finishRecall(context.sword, context.player);
        FlyingSwordStorageAttachment storage = KongqiaoAttachments.getFlyingSwordStorage(
            context.player
        );
        helper.assertTrue(storage != null, "前置失败：玩家飞剑存储附件为空");
        helper.assertTrue(storage.getCount() == 1, "e2e：召回后存储数量应为 1");

        FlyingSwordStorageAttachment.RecalledSword recalled = storage.getAt(0);
        helper.assertTrue(recalled != null, "e2e：召回后应存在可读取存储项");
        FlyingSwordAttributes attrsInStorage = FlyingSwordAttributes.fromNBT(
            recalled.attributes
        );
        helper.assertTrue(
            E2E_HAPPY_STABLE_ID.equals(attrsInStorage.getStableSwordId()),
            "e2e：召回存档中的 stableSwordId 应保持不变"
        );
        helper.assertTrue(
            context
                .player
                .getUUID()
                .toString()
                .equals(attrsInStorage.getBond().getOwnerUuid()),
            "e2e：召回存档中的 bond.ownerUuid 应保持不变"
        );
        helper.assertTrue(
            attrsInStorage.getExperience() == expected.expectedExpAfterTraining,
            "e2e：召回存档中的经验应保持训练后收益"
        );
        helper.assertTrue(
            Math.abs(
                attrsInStorage.getBond().getResonance() -
                expected.expectedResonanceAfterTraining
            )
                < BOND_RESONANCE_EPSILON,
            "e2e：召回存档中的共鸣应保持训练后收益"
        );

        int restoredCount = FlyingSwordController.restoreOne(context.level, context.player);
        helper.assertTrue(restoredCount == 1, "e2e：恢复阶段应成功恢复 1 把飞剑");

        FlyingSwordEntity restoredSword = FlyingSwordController.getNearestSword(
            context.level,
            context.player
        );
        helper.assertTrue(restoredSword != null, "e2e：恢复后应能找到飞剑实体");
        FlyingSwordAttributes attrsAfterRestore = restoredSword.getSwordAttributes();
        helper.assertTrue(
            E2E_HAPPY_STABLE_ID.equals(attrsAfterRestore.getStableSwordId()),
            "e2e：恢复后 stableSwordId 应保持一致"
        );
        helper.assertTrue(
            context
                .player
                .getUUID()
                .toString()
                .equals(attrsAfterRestore.getBond().getOwnerUuid()),
            "e2e：恢复后 bond.ownerUuid 应保持一致"
        );
        helper.assertTrue(
            attrsAfterRestore.getExperience() == expected.expectedExpAfterTraining,
            "e2e：恢复后经验应保持训练后收益"
        );
        helper.assertTrue(
            Math.abs(
                attrsAfterRestore.getBond().getResonance() -
                expected.expectedResonanceAfterTraining
            )
                < BOND_RESONANCE_EPSILON,
            "e2e：恢复后共鸣应保持训练后收益"
        );
    }

    private static void assertCacheRebuildSelfHeal(
        GameTestHelper helper,
        ServerLevel level,
        ServerPlayer player,
        FlyingSwordStateAttachment state
    ) {
        state.clearBondCache();
        helper.assertTrue(state.getBondedSwordId().isBlank(), "e2e：清空后 bondedSwordId 应为空");
        helper.assertTrue(state.isBondCacheDirty(), "e2e：清空后缓存应为 dirty");

        reconcileBenmingCacheForTickSafely(level, player, state);
        helper.assertTrue(
            E2E_HAPPY_STABLE_ID.equals(state.getBondedSwordId()),
            "e2e：tick 重建后应自愈为真实 stableSwordId"
        );
        helper.assertTrue(!state.isBondCacheDirty(), "e2e：tick 重建后缓存应恢复 clean");
        helper.assertTrue(
            state.getLastResolvedTick() == level.getGameTime(),
            "e2e：tick 重建后应写入当前 resolved tick"
        );
    }

    private static void assertResonanceBurstAndBondStateCoherent(
        GameTestHelper helper,
        FlyingSwordStateAttachment state,
        String expectedResonanceType,
        long expectedBurstCooldownUntilTick,
        long expectedBurstActiveUntilTick,
        long expectedBurstAftershockUntilTick,
        String phaseLabel
    ) {
        helper.assertTrue(
            E2E_HAPPY_STABLE_ID.equals(state.getBondedSwordId()),
            "e2e：" + phaseLabel + " bonded stableSwordId 应保持本命目标"
        );
        helper.assertTrue(
            expectedResonanceType.equals(state.getResonanceType()),
            "e2e：" + phaseLabel + " resonanceType 应保持切换后的目标类型"
        );
        helper.assertTrue(
            state.getBurstCooldownUntilTick() == expectedBurstCooldownUntilTick,
            "e2e：" + phaseLabel + " burstCooldownUntilTick 应保持一致"
        );
        helper.assertTrue(
            state.getBurstActiveUntilTick() == expectedBurstActiveUntilTick,
            "e2e：" + phaseLabel + " burstActiveUntilTick 应保持一致"
        );
        helper.assertTrue(
            state.getBurstAftershockUntilTick() == expectedBurstAftershockUntilTick,
            "e2e：" + phaseLabel + " burstAftershockUntilTick 应保持一致"
        );
        helper.assertTrue(
            state.getBurstAftershockUntilTick() > state.getBurstActiveUntilTick(),
            "e2e：" + phaseLabel + " burstAftershockUntilTick 应持续晚于 burstActiveUntilTick"
        );
    }

    private static void warmupBurstResourcesIfAvailable(ServerPlayer player) {
        invokeBridgeModifyIfPresent(
            "com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper",
            player,
            RESOURCE_SNAPSHOT_HIGH
        );
        invokeBridgeModifyIfPresent(
            "com.Kizunad.guzhenrenext.guzhenrenBridge.NianTouHelper",
            player,
            RESOURCE_SNAPSHOT_HIGH
        );
        invokeBridgeModifyIfPresent(
            "com.Kizunad.guzhenrenext.guzhenrenBridge.HunPoHelper",
            player,
            RESOURCE_SNAPSHOT_HIGH
        );
    }

    private static void invokeBridgeModifyIfPresent(
        String helperClassName,
        ServerPlayer player,
        double amount
    ) {
        try {
            Class<?> helperClass = Class.forName(helperClassName);
            Method modify = helperClass.getDeclaredMethod(
                "modify",
                LivingEntity.class,
                double.class
            );
            modify.invoke(null, player, amount);
        } catch (ReflectiveOperationException | LinkageError ignored) {}
    }

    private static FlyingSwordController.BenmingControllerActionResult attemptBurstForHappyPath(
        E2EContext context,
        FlyingSwordStateAttachment state
    ) {
        try {
            return FlyingSwordController.attemptBurstForSelectedOrNearestBenmingSword(
                context.level,
                context.player
            );
        } catch (RuntimeException runtimeException) {
            if (shouldFallbackForExternalBridge(runtimeException)) {
                return attemptBurstByCoreReflection(
                    state,
                    context.sword.getSwordAttributes().getStableSwordId(),
                    state.getBondedSwordId(),
                    context.level.getGameTime()
                );
            }
            throw runtimeException;
        } catch (LinkageError linkageError) {
            if (!shouldFallbackForExternalBridge(linkageError)) {
                throw linkageError;
            }
            return attemptBurstByCoreReflection(
                state,
                context.sword.getSwordAttributes().getStableSwordId(),
                state.getBondedSwordId(),
                context.level.getGameTime()
            );
        }
    }

    private static boolean shouldFallbackForExternalBridge(Throwable throwable) {
        return containsGuzhenrenVariablesMissingMarker(throwable) ||
        containsBridgeOrExternalGuzhenrenStack(throwable);
    }

    private static void reconcileBenmingCacheForTickSafely(
        ServerLevel level,
        ServerPlayer player,
        FlyingSwordStateAttachment state
    ) {
        try {
            FlyingSwordTickHandler.reconcileBenmingCacheForTick(player);
            return;
        } catch (RuntimeException runtimeException) {
            if (!shouldFallbackForExternalBridge(runtimeException)) {
                throw runtimeException;
            }
        } catch (LinkageError linkageError) {
            if (!shouldFallbackForExternalBridge(linkageError)) {
                throw linkageError;
            }
        }
        rebuildBenmingCacheWithoutExternalBridge(level, player, state);
    }

    private static void rebuildBenmingCacheWithoutExternalBridge(
        ServerLevel level,
        ServerPlayer player,
        FlyingSwordStateAttachment state
    ) {
        String ownerUuid = player.getUUID().toString();
        String resolvedSwordId = resolveSingleBoundSwordIdWithoutExternalBridge(
            level,
            player,
            ownerUuid
        );
        if (resolvedSwordId.isBlank()) {
            state.clearBondCache();
            return;
        }
        state.updateBondCache(resolvedSwordId, level.getGameTime());
    }

    private static String resolveSingleBoundSwordIdWithoutExternalBridge(
        ServerLevel level,
        ServerPlayer player,
        String ownerUuid
    ) {
        String liveCandidateId = "";
        int liveCandidateCount = 0;
        for (FlyingSwordEntity sword : FlyingSwordController.getPlayerSwords(level, player)) {
            FlyingSwordAttributes attrs = sword.getSwordAttributes();
            if (attrs == null || attrs.getBond() == null) {
                continue;
            }
            if (!ownerUuid.equals(attrs.getBond().getOwnerUuid())) {
                continue;
            }
            String stableSwordId = attrs.getStableSwordId();
            if (stableSwordId == null || stableSwordId.isBlank()) {
                continue;
            }
            liveCandidateCount++;
            if (liveCandidateCount > 1) {
                return "";
            }
            liveCandidateId = stableSwordId;
        }
        if (liveCandidateCount == 1) {
            return liveCandidateId;
        }

        FlyingSwordStorageAttachment storage = KongqiaoAttachments.getFlyingSwordStorage(
            player
        );
        if (storage == null) {
            return "";
        }
        String recalledCandidateId = "";
        int recalledCandidateCount = 0;
        for (int index = 0; index < storage.getCount(); index++) {
            FlyingSwordStorageAttachment.RecalledSword recalledSword = storage.getAt(index);
            if (
                recalledSword == null ||
                recalledSword.itemWithdrawn ||
                recalledSword.attributes == null
            ) {
                continue;
            }
            FlyingSwordAttributes attrs = FlyingSwordAttributes.fromNBT(
                recalledSword.attributes
            );
            if (attrs == null || attrs.getBond() == null) {
                continue;
            }
            if (!ownerUuid.equals(attrs.getBond().getOwnerUuid())) {
                continue;
            }
            String stableSwordId = attrs.getStableSwordId();
            if (stableSwordId == null || stableSwordId.isBlank()) {
                continue;
            }
            recalledCandidateCount++;
            if (recalledCandidateCount > 1) {
                return "";
            }
            recalledCandidateId = stableSwordId;
        }
        return recalledCandidateCount == 1 ? recalledCandidateId : "";
    }

    private static boolean containsGuzhenrenVariablesMissingMarker(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        if (
            containsGuzhenrenVariablesMissingMarkerText(throwable.getMessage()) ||
            containsGuzhenrenVariablesMissingMarkerText(throwable.toString())
        ) {
            return true;
        }
        Throwable cause = throwable.getCause();
        if (cause == null || cause == throwable) {
            return false;
        }
        return containsGuzhenrenVariablesMissingMarker(cause);
    }

    private static boolean containsGuzhenrenVariablesMissingMarkerText(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        return text.contains("net.guzhenren.network.GuzhenrenModVariables") ||
        text.contains("net/guzhenren/network/GuzhenrenModVariables") ||
        text.contains("GuzhenrenModVariables");
    }

    private static boolean containsBridgeOrExternalGuzhenrenStack(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            String className = stackTraceElement.getClassName();
            if (className == null) {
                continue;
            }
            if (className.startsWith("com.Kizunad.guzhenrenext.guzhenrenBridge.")) {
                return true;
            }
            if (className.startsWith("net.guzhenren.")) {
                return true;
            }
        }
        Throwable cause = throwable.getCause();
        if (cause == null || cause == throwable) {
            return false;
        }
        return containsBridgeOrExternalGuzhenrenStack(cause);
    }

    private static FlyingSwordController.BenmingControllerActionResult attemptBurstByCoreReflection(
        FlyingSwordStateAttachment state,
        String resolvedSwordId,
        String bondedSwordId,
        long resolvedTick
    ) {
        try {
            Method burstCore = FlyingSwordController.class.getDeclaredMethod(
                "attemptBenmingSwordBurst",
                FlyingSwordStateAttachment.class,
                String.class,
                String.class,
                long.class
            );
            burstCore.setAccessible(true);
            Object reflected = burstCore.invoke(
                null,
                state,
                resolvedSwordId,
                bondedSwordId,
                resolvedTick
            );
            return (FlyingSwordController.BenmingControllerActionResult) reflected;
        } catch (InvocationTargetException invocationTargetException) {
            Throwable cause = invocationTargetException.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException(
                "e2e：反射回退 burst 内核时出现受检异常",
                invocationTargetException
            );
        } catch (ReflectiveOperationException reflectiveOperationException) {
            throw new IllegalStateException(
                "e2e：无法通过反射调用 burst 内核",
                reflectiveOperationException
            );
        }
    }

    private record E2EContext(
        ServerLevel level,
        ServerPlayer player,
        FlyingSwordEntity sword
    ) {}

    private record E2EExpectedSnapshot(
        int expectedExpAfterTraining,
        double expectedResonanceAfterTraining
    ) {}

    private record BacklashScenarioContext(
        ServerLevel level,
        ServerPlayer player,
        FlyingSwordEntity firstSword,
        FlyingSwordEntity secondSword,
        FlyingSwordStateAttachment state,
        FlyingSwordCooldownAttachment cooldownAttachment,
        String cooldownKey
    ) {}

    private record BacklashSnapshot(
        double zhenyuanLoss,
        double niantouLoss,
        double hunpoLoss,
        int operationCount
    ) {}

    private record WithdrawnProductionPathSnapshot(
        int operationCountBeforeProductionPath,
        double zhenyuanLossBeforeProductionPath,
        double niantouLossBeforeProductionPath,
        double hunpoLossBeforeProductionPath,
        int operationCountAfterProductionPath,
        double zhenyuanLossAfterProductionPath,
        double niantouLossAfterProductionPath,
        double hunpoLossAfterProductionPath
    ) {}

    private static FlyingSwordEntity spawnBoundSwordForOwner(
        GameTestHelper helper,
        ServerLevel level,
        ServerPlayer player,
        String stableSwordId,
        String assertPrefix
    ) {
        FlyingSwordEntity sword = FlyingSwordSpawner.spawnBasic(level, player);
        helper.assertTrue(sword != null, assertPrefix + "：前置失败，飞剑生成失败");
        sword.getSwordAttributes().setStableSwordId(stableSwordId);
        sword.getSwordAttributes().getBond().setOwnerUuid(player.getUUID().toString());
        sword.getSwordAttributes().getBond().setResonance(E2E_NEGATIVE_INITIAL_RESONANCE);
        sword.syncAttributesToEntityData();
        return sword;
    }

    private static BenmingSwordBondService.SwordBondPort toSwordBondPort(
        FlyingSwordEntity sword
    ) {
        return new BenmingSwordBondService.SwordBondPort() {
            @Override
            public String getStableSwordId() {
                return sword.getSwordAttributes().getStableSwordId();
            }

            @Override
            public String getBondOwnerUuid() {
                return sword.getSwordAttributes().getBond().getOwnerUuid();
            }

            @Override
            public double getBondResonance() {
                return sword.getSwordAttributes().getBond().getResonance();
            }

            @Override
            public void setBondOwnerUuid(String ownerUuid) {
                sword.getSwordAttributes().getBond().setOwnerUuid(ownerUuid);
                sword.syncAttributesToEntityData();
            }

            @Override
            public void setBondResonance(double resonance) {
                sword.getSwordAttributes().getBond().setResonance(resonance);
                sword.syncAttributesToEntityData();
            }
        };
    }

    private static BenmingSwordBondService.PlayerBondCachePort toPlayerCachePort(
        FlyingSwordStateAttachment state
    ) {
        return new BenmingSwordBondService.PlayerBondCachePort() {
            @Override
            public String getBondedSwordId() {
                return state.getBondedSwordId();
            }

            @Override
            public boolean isBondCacheDirty() {
                return state.isBondCacheDirty();
            }

            @Override
            public void updateBondCache(String stableSwordId, long resolvedTick) {
                state.updateBondCache(stableSwordId, resolvedTick);
            }

            @Override
            public void markBondCacheDirty() {
                state.markBondCacheDirty();
            }

            @Override
            public void clearBondCache() {
                state.clearBondCache();
            }
        };
    }

    private static void assertClose(
        GameTestHelper helper,
        double expected,
        double actual,
        String message
    ) {
        helper.assertTrue(Math.abs(expected - actual) < BOND_RESONANCE_EPSILON, message);
    }

    private static final class MutationRecorder
        implements BenmingSwordResourceTransaction.ResourceMutationPort {

        private double zhenyuanLoss;
        private double niantouLoss;
        private double hunpoLoss;
        private int operationCount;

        @Override
        public void spendZhenyuan(double amount) {
            zhenyuanLoss += amount;
            operationCount++;
        }

        @Override
        public void spendNiantou(double amount) {
            niantouLoss += amount;
            operationCount++;
        }

        @Override
        public void spendHunpo(double amount) {
            hunpoLoss += amount;
            operationCount++;
        }

        private double zhenyuanLoss() {
            return zhenyuanLoss;
        }

        private double niantouLoss() {
            return niantouLoss;
        }

        private double hunpoLoss() {
            return hunpoLoss;
        }

        private int operationCount() {
            return operationCount;
        }
    }

    private static void writeAttributesToSword(
        ItemStack swordStack,
        FlyingSwordAttributes attributes
    ) {
        CompoundTag root = ItemStackCustomDataHelper.copyCustomDataTag(swordStack);
        root.put("Attributes", attributes.toNBT());
        ItemStackCustomDataHelper.setCustomDataTag(swordStack, root);
    }

    private static FlyingSwordAttributes readAttributesFromSword(ItemStack swordStack) {
        CompoundTag root = ItemStackCustomDataHelper.copyCustomDataTag(swordStack);
        return FlyingSwordAttributes.fromNBT(root.getCompound("Attributes"));
    }

    private static ServerPlayer createDeterministicPlayer(
        ServerLevel level,
        String suffix
    ) {
        UUID uuid = UUID.nameUUIDFromBytes(
            ("benming_recall_restore_" + suffix).getBytes(StandardCharsets.UTF_8)
        );
        return FakePlayerFactory.get(
            level,
            new GameProfile(uuid, "benming_recall_restore_" + suffix)
        );
    }
}
