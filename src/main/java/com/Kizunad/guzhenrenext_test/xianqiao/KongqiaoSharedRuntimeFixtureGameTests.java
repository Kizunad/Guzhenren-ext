package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoAttachments;
import com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData;
import com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.attachment.TweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.inventory.AttackInventory;
import com.Kizunad.guzhenrenext.kongqiao.inventory.GuchongFeedInventory;
import com.Kizunad.guzhenrenext.kongqiao.inventory.KongqiaoInventory;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.IGuEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoActiveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.network.PacketSyncNianTouUnlocks;
import com.Kizunad.guzhenrenext.kongqiao.network.PacketSyncTweakConfig;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData;
import com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouDataManager;
import com.Kizunad.guzhenrenext.kongqiao.service.GuRunningService;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoAptitudeTier;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoCapacityProfile;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoCapacityBridge;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjection;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjectionService;
import com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoService;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoData;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoDataManager;
import com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoUnlockService;
import com.Kizunad.guzhenrenext.network.ClientboundKongqiaoSyncPayload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public final class KongqiaoSharedRuntimeFixtureGameTests {

    private static final int TEST_TIMEOUT_TICKS = 160;
    private static final String FIXTURE_PROOF_BATCH = "kongqiao_shared_runtime_fixture_proof";
    private static final String LIFECYCLE_SMOKE_BATCH = "kongqiao_lifecycle_smoke_path";

    private static final String TEST_USAGE_ID =
        "guzhenrenext:task10_lifecycle_smoke_passive_usage";
    private static final String TEST_SHAZHAO_ID =
        "guzhenrenext:shazhao_active_task10_lifecycle_smoke";
    private static final double RECOVERY_EPSILON = 1.0E-6D;
    private static final double PRESSURE_CAP_PER_BONUS_ROW = 6.0D;
    private static final double REJECTION_TEST_BURST_PRESSURE = 200.0D;
    private static final double REJECTION_TEST_FATIGUE_DEBT = 200.0D;
    private static final int RECOVERY_TEST_OVERLOAD_TIER = 4;
    private static final long RECOVERY_TEST_LAST_DECAY_GAME_TIME = 100L;
    private static final long RECOVERY_TEST_CURRENT_GAME_TIME = 140L;
    private static final double RECOVERY_EXPECTED_MAX_AFTER_ONE_TICK = 198.0D;
    private static final int INJECTED_PASSIVE_USAGE_PRESSURE = 40;
    private static final int INJECTED_PASSIVE_USAGE_ACTIVE_COST = 20;
    private static final int INJECTED_SHAZHAO_COST = 18;
    private static final double PRESSURE_CAP_CANCI = 18.0D;
    private static final double PRESSURE_CAP_XIADENG = 28.0D;
    private static final double PRESSURE_CAP_ZHONGDENG = 40.0D;
    private static final double PRESSURE_CAP_SHANGDENG = 56.0D;
    private static final double PRESSURE_CAP_JUEPIN = 72.0D;

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = FIXTURE_PROOF_BATCH
    )
    public void provesLiveLevelSharedRuntimeFixtureCarriesRealOwnerAttachmentsAndInventories(
        final GameTestHelper helper
    ) throws Exception {
        final KongqiaoSharedRuntimeFixtureRuntimeHelper.SharedRuntimeFixture fixture =
            KongqiaoSharedRuntimeFixtureRuntimeHelper.newSharedRuntimeFixtureOnLiveLevel(
                helper.getLevel()
            );

        final ServerPlayer owner = fixture.owner();
        final KongqiaoData data = fixture.data();
        final NianTouUnlocks unlocks = fixture.unlocks();
        final TweakConfig tweakConfig = fixture.tweakConfig();
        final ActivePassives activePassives = fixture.activePassives();
        final KongqiaoInventory kongqiaoInventory = fixture.kongqiaoInventory();
        final AttackInventory attackInventory = fixture.attackInventory();
        final GuchongFeedInventory feedInventory = fixture.feedInventory();

        helper.assertTrue(owner != null, "shared fixture: 必须创建真实 owner 实体");
        helper.assertTrue(data != null, "shared fixture: owner 必须挂载 KongqiaoData");
        helper.assertTrue(unlocks != null, "shared fixture: owner 必须挂载 NianTouUnlocks");
        helper.assertTrue(tweakConfig != null, "shared fixture: owner 必须挂载 TweakConfig");
        helper.assertTrue(activePassives != null, "shared fixture: owner 必须挂载 ActivePassives");

        final KongqiaoData ownerData = KongqiaoAttachments.getData(owner);
        final NianTouUnlocks ownerUnlocks = KongqiaoAttachments.getUnlocks(owner);
        final TweakConfig ownerTweakConfig = KongqiaoAttachments.getTweakConfig(owner);
        final ActivePassives ownerActivePassives =
            KongqiaoAttachments.getActivePassives(owner);

        helper.assertTrue(
            ownerData == data,
            "shared fixture: data 必须来自同一个 owner 附件图"
        );
        helper.assertTrue(
            ownerUnlocks == unlocks,
            "shared fixture: unlocks 必须来自同一个 owner 附件图"
        );
        helper.assertTrue(
            ownerTweakConfig == tweakConfig,
            "shared fixture: tweakConfig 必须来自同一个 owner 附件图"
        );
        helper.assertTrue(
            ownerActivePassives == activePassives,
            "shared fixture: activePassives 必须来自同一个 owner 附件图"
        );

        helper.assertTrue(
            kongqiaoInventory != null,
            "shared fixture: KongqiaoData 必须持有真实 kongqiaoInventory"
        );
        helper.assertTrue(
            attackInventory != null,
            "shared fixture: KongqiaoData 必须持有真实 attackInventory"
        );
        helper.assertTrue(
            feedInventory != null,
            "shared fixture: KongqiaoData 必须持有真实 feedInventory"
        );

        helper.succeed();
    }

    @GameTest(
        template = "examplegametests.empty",
        timeoutTicks = TEST_TIMEOUT_TICKS,
        batch = LIFECYCLE_SMOKE_BATCH
    )
    public void provesFullLifecycleSmokePathRemainsCoherentOnOneLiveOwnerBackedGraph(
        final GameTestHelper helper
    ) throws Exception {
        TestLifecyclePassiveEffect.reset();
        TestLifecycleShazhaoActiveEffect.reset();
        registerLifecycleInjectedDataAndEffects();

        final KongqiaoSharedRuntimeFixtureRuntimeHelper.SharedRuntimeFixture fixture =
            KongqiaoSharedRuntimeFixtureRuntimeHelper.newSharedRuntimeFixtureOnLiveLevel(
                helper.getLevel()
            );
        final ServerPlayer owner = fixture.owner();
        final KongqiaoData data = fixture.data();
        final NianTouUnlocks unlocks = fixture.unlocks();
        final TweakConfig tweakConfig = fixture.tweakConfig();
        final ActivePassives activePassives = fixture.activePassives();
        final KongqiaoInventory inventory = fixture.kongqiaoInventory();

        assertSmokeFixtureReady(helper, owner, data, unlocks, tweakConfig, activePassives, inventory);

        final KongqiaoCapacityProfile rawCapacityProfile = buildSmokeRawCapacityProfile();
        verifySmokeActivationAndCapacity(helper, owner, data, inventory, rawCapacityProfile);

        final SmokeAppleUsageSetup appleUsageSetup =
            prepareAppleUsageForPassiveLifecycle(helper, unlocks, inventory);
        runPassiveRuntimeStage(
            helper,
            owner,
            data,
            activePassives,
            rawCapacityProfile,
            appleUsageSetup.apple(),
            appleUsageSetup.passiveUsage()
        );
        runDeriveStage(helper, data, unlocks, appleUsageSetup.appleId());
        runWheelPreferenceStage(helper, tweakConfig);
        runPressureRejectionStage(helper, data, rawCapacityProfile, appleUsageSetup.passiveUsage());
        runRecoveryStage(helper, data, rawCapacityProfile);

        final KongqiaoPressureProjection projectionAfterFlow =
            runRelogAndProjectionRoundTripStage(
                helper,
                owner,
                data,
                unlocks,
                tweakConfig,
                inventory,
                rawCapacityProfile
            );
        runPayloadRoundTripStage(helper, owner, data, unlocks, projectionAfterFlow);
        assertFinalAttachmentIdentity(helper, owner, data, unlocks, tweakConfig, activePassives);

        helper.succeed();
    }

    private static void assertSmokeFixtureReady(
        final GameTestHelper helper,
        final ServerPlayer owner,
        final KongqiaoData data,
        final NianTouUnlocks unlocks,
        final TweakConfig tweakConfig,
        final ActivePassives activePassives,
        final KongqiaoInventory inventory
    ) {
        helper.assertTrue(owner != null, "smoke: owner 不能为空");
        helper.assertTrue(data != null, "smoke: KongqiaoData 不能为空");
        helper.assertTrue(unlocks != null, "smoke: NianTouUnlocks 不能为空");
        helper.assertTrue(tweakConfig != null, "smoke: TweakConfig 不能为空");
        helper.assertTrue(activePassives != null, "smoke: ActivePassives 不能为空");
        helper.assertTrue(inventory != null, "smoke: KongqiaoInventory 不能为空");
    }

    private static KongqiaoCapacityProfile buildSmokeRawCapacityProfile() {
        return KongqiaoCapacityBridge.resolveFromRawVariables(
            Map.ofEntries(
                Map.entry("zuida_zhenyuan", 120.0D),
                Map.entry("shouyuan", 120.0D),
                Map.entry("zuida_jingli", 100.0D),
                Map.entry("zuida_hunpo", 100.0D),
                Map.entry("tizhi", 100.0D),
                Map.entry("zhuanshu", 5.0D),
                Map.entry("jieduan", 4.0D),
                Map.entry("earthQi", 100.0D),
                Map.entry("renqi", 100.0D),
                Map.entry("qiyun", 100.0D),
                Map.entry("qiyun_shangxian", 100.0D)
            )
        );
    }

    private static void verifySmokeActivationAndCapacity(
        final GameTestHelper helper,
        final ServerPlayer owner,
        final KongqiaoData data,
        final KongqiaoInventory inventory,
        final KongqiaoCapacityProfile rawCapacityProfile
    ) {
        data.setGameplayActivated(false);
        helper.assertTrue(
            KongqiaoService.requireGameplayActivatedData(owner) == null,
            "smoke/activation: 未激活时 requireGameplayActivatedData 必须拒绝"
        );
        data.setGameplayActivated(true);
        helper.assertTrue(
            KongqiaoService.requireGameplayActivatedData(owner) == data,
            "smoke/activation: 激活后 requireGameplayActivatedData 必须返回同一个 data"
        );

        inventory.getSettings().setUnlockedRows(rawCapacityProfile.totalRows());
        final int unlockedRows = inventory.getSettings().getUnlockedRows();
        helper.assertTrue(
            rawCapacityProfile.totalRows() == unlockedRows,
            "smoke/capacity: raw profile.totalRows 必须与 owner inventory settings 行数一致"
        );
        helper.assertTrue(
            rawCapacityProfile.aptitudeTier() == KongqiaoAptitudeTier.JUEPIN,
            "smoke/capacity: raw bridge 必须产出可验证的资质档位"
        );
    }

    private static SmokeAppleUsageSetup prepareAppleUsageForPassiveLifecycle(
        final GameTestHelper helper,
        final NianTouUnlocks unlocks,
        final KongqiaoInventory inventory
    ) {
        final ItemStack apple = new ItemStack(Items.APPLE);
        inventory.setItem(0, apple);
        final ResourceLocation appleId = ResourceLocation.parse("minecraft:apple");
        unlocks.unlock(appleId, TEST_USAGE_ID);
        helper.assertTrue(
            unlocks.isUsageUnlocked(appleId, TEST_USAGE_ID),
            "smoke/unlock: 同一 unlock 附件必须可读回已解锁 usage"
        );

        final NianTouData itemData = NianTouDataManager.getData(apple);
        final NianTouData.Usage passiveUsage = itemData == null || itemData.usages() == null
            ? null
            : itemData
                .usages()
                .stream()
                .filter(usage -> TEST_USAGE_ID.equals(usage.usageID()))
                .findFirst()
                .orElse(null);
        helper.assertTrue(passiveUsage != null, "smoke/passive: 必须能读取到 APPLE 的被动 usage");
        return new SmokeAppleUsageSetup(apple, appleId, passiveUsage);
    }

    private static void runPassiveRuntimeStage(
        final GameTestHelper helper,
        final ServerPlayer owner,
        final KongqiaoData data,
        final ActivePassives activePassives,
        final KongqiaoCapacityProfile rawCapacityProfile,
        final ItemStack apple,
        final NianTouData.Usage passiveUsage
    ) throws Exception {
        GuRunningService.handleContainerEquipChanges(owner, data.getKongqiaoInventory());
        final Object passiveRuntimeSnapshot = invokeEvaluatePassiveRuntimeSnapshot(
            rawCapacityProfile,
            data.getStabilityState().getBurstPressure(),
            data.getStabilityState().getFatigueDebt()
        );
        invokeRunPassiveUsageIfAllowed(
            activePassives,
            owner,
            apple,
            passiveUsage,
            GuEffectRegistry.get(TEST_USAGE_ID),
            passiveRuntimeSnapshot,
            true
        );
        helper.assertTrue(
            TestLifecyclePassiveEffect.ON_EQUIP_COUNT.get() > 0,
            "smoke/passive: 被动效果必须至少触发一次 onEquip"
        );
        helper.assertTrue(
            TestLifecyclePassiveEffect.ON_SECOND_COUNT.get() > 0,
            "smoke/passive: 被动效果必须至少触发一次 onSecond"
        );
        helper.assertTrue(
            activePassives.isActive(TEST_USAGE_ID),
            "smoke/passive: ActivePassives 必须在同一 owner 图中记录运行时激活"
        );
    }

    private static void runDeriveStage(
        final GameTestHelper helper,
        final KongqiaoData data,
        final NianTouUnlocks unlocks,
        final ResourceLocation appleId
    ) {
        final List<ShazhaoUnlockService.UnlockCandidate> candidates =
            ShazhaoUnlockService.listUnlockCandidates(unlocks);
        helper.assertTrue(
            !candidates.isEmpty(),
            "smoke/derive: listUnlockCandidates 在注入数据后不能为空"
        );

        final ResourceLocation shazhaoId = ResourceLocation.parse(TEST_SHAZHAO_ID);
        final ShazhaoUnlockService.UnlockCandidate lifecycleCandidate =
            candidates
                .stream()
                .filter(candidate -> TEST_SHAZHAO_ID.equals(candidate.data().shazhaoID()))
                .findFirst()
                .orElse(null);
        if (lifecycleCandidate == null) {
            helper.assertTrue(
                ShazhaoDataManager.get(shazhaoId) != null,
                "smoke/derive: 注入杀招数据必须可从 ShazhaoDataManager 读回"
            );
            helper.assertTrue(
                NianTouDataManager.getData(Items.APPLE) != null,
                "smoke/derive: 注入 APPLE 念头数据必须可从 NianTouDataManager 读回"
            );
            helper.assertTrue(
                unlocks.isUnlocked(appleId),
                "smoke/derive: unlock 附件必须记录 APPLE 已解锁状态"
            );
        }
        helper.assertTrue(
            lifecycleCandidate != null,
            "smoke/derive: 已解锁 APPLE 用途后必须出现目标杀招候选"
        );

        final double fatigueBeforeDerive = data.getStabilityState().getFatigueDebt();
        final ShazhaoUnlockService.DeriveAttemptResult deriveResult =
            ShazhaoUnlockService.resolveDeriveAttempt(
                unlocks,
                data.getStabilityState(),
                lifecycleCandidate,
                0.0D,
                999.0D,
                ignored -> {
                }
            );
        helper.assertTrue(deriveResult.success(), "smoke/derive: roll=0 必须命中推演成功");
        helper.assertTrue(
            unlocks.isShazhaoUnlocked(shazhaoId),
            "smoke/derive: 推演成功后杀招必须写回同一 unlock 附件"
        );
        helper.assertTrue(
            unlocks.getShazhaoMessage() != null
                && unlocks.getShazhaoMessage().startsWith("推演成功"),
            "smoke/derive: 推演成功消息必须写回同一 unlock 附件"
        );
        helper.assertTrue(
            data.getStabilityState().getFatigueDebt() > fatigueBeforeDerive,
            "smoke/derive: 推演必须在同一稳定态写入 fatigue debt"
        );
    }

    private static void runWheelPreferenceStage(
        final GameTestHelper helper,
        final TweakConfig tweakConfig
    ) {
        tweakConfig.addWheelSkill(TEST_SHAZHAO_ID, TweakConfig.DEFAULT_MAX_WHEEL_SKILLS);
        helper.assertTrue(
            tweakConfig.getWheelSkills().contains(TEST_SHAZHAO_ID),
            "smoke/wheel: TweakConfig 必须在同一 owner 图保存轮盘偏好"
        );
        final ByteBuf tweakBuffer = Unpooled.buffer();
        PacketSyncTweakConfig.STREAM_CODEC.encode(
            tweakBuffer,
            new PacketSyncTweakConfig(tweakConfig)
        );
        final PacketSyncTweakConfig decodedTweak =
            PacketSyncTweakConfig.STREAM_CODEC.decode(tweakBuffer);
        helper.assertTrue(
            decodedTweak.config().getWheelSkills().contains(TEST_SHAZHAO_ID),
            "smoke/wheel: PacketSyncTweakConfig 回环后必须保留轮盘偏好"
        );
    }

    private static void runPressureRejectionStage(
        final GameTestHelper helper,
        final KongqiaoData data,
        final KongqiaoCapacityProfile rawCapacityProfile,
        final NianTouData.Usage passiveUsage
    ) throws Exception {
        final int shazhaoCallsBeforeReject =
            TestLifecycleShazhaoActiveEffect.ON_ACTIVATE_COUNT.get();
        data.getStabilityState().setBurstPressure(REJECTION_TEST_BURST_PRESSURE);
        data.getStabilityState().setFatigueDebt(REJECTION_TEST_FATIGUE_DEBT);

        final double pressureCap =
            basePressureCapByAptitude(rawCapacityProfile.aptitudeTier())
                + Math.max(0, rawCapacityProfile.bonusRows()) * PRESSURE_CAP_PER_BONUS_ROW;
        final double currentEffectivePressure =
            data.getStabilityState().getBurstPressure() + data.getStabilityState().getFatigueDebt();
        final ResourceLocation shazhaoId = ResourceLocation.parse(TEST_SHAZHAO_ID);

        final Object rejected = invokeActivateResolvedShazhaoEffectForTests(
            ShazhaoDataManager.get(shazhaoId),
            ShazhaoEffectRegistry.get(shazhaoId),
            data.getStabilityState(),
            currentEffectivePressure,
            pressureCap
        );
        final Method activationSuccessMethod = rejected.getClass().getMethod("success");
        final Method activationFailureReasonMethod = rejected.getClass().getMethod("failureReason");
        final Enum<?> failureReason = (Enum<?>) activationFailureReasonMethod.invoke(rejected);
        helper.assertTrue(
            !(Boolean) activationSuccessMethod.invoke(rejected)
                && "PRESSURE_LIMIT".equals(failureReason.name()),
            "smoke/pressure: 超压时杀招主动触发必须返回 PRESSURE_LIMIT"
        );
        helper.assertTrue(
            TestLifecycleShazhaoActiveEffect.ON_ACTIVATE_COUNT.get() == shazhaoCallsBeforeReject,
            "smoke/pressure: PRESSURE_LIMIT 时不得执行杀招效果体"
        );

        final Object activeRejected = invokeActivateResolvedUsageForTests(
            passiveUsage,
            GuEffectRegistry.get(TEST_USAGE_ID),
            currentEffectivePressure,
            pressureCap
        );
        final Method activeFailureReasonMethod = activeRejected.getClass().getMethod("failureReason");
        final Enum<?> activeFailureReason = (Enum<?>) activeFailureReasonMethod.invoke(activeRejected);
        helper.assertTrue(
            "PRESSURE_LIMIT".equals(activeFailureReason.name()),
            "smoke/pressure: 主动用途在超压路径下同样必须拒绝"
        );
    }

    private static void runRecoveryStage(
        final GameTestHelper helper,
        final KongqiaoData data,
        final KongqiaoCapacityProfile rawCapacityProfile
    ) throws Exception {
        data.getStabilityState().setOverloadTier(RECOVERY_TEST_OVERLOAD_TIER);
        data.getStabilityState().setLastDecayGameTime(RECOVERY_TEST_LAST_DECAY_GAME_TIME);
        invokeApplyRecoveryTick(
            data.getStabilityState(),
            RECOVERY_TEST_CURRENT_GAME_TIME,
            rawCapacityProfile,
            data.getStabilityState().getSealedSlots().size()
        );
        helper.assertTrue(
            data.getStabilityState().getFatigueDebt()
                <= RECOVERY_EXPECTED_MAX_AFTER_ONE_TICK + RECOVERY_EPSILON,
            "smoke/recovery: 恢复 tick 后 fatigue debt 必须下降"
        );
        helper.assertTrue(
            data.getStabilityState().getBurstPressure()
                <= RECOVERY_EXPECTED_MAX_AFTER_ONE_TICK + RECOVERY_EPSILON,
            "smoke/recovery: 恢复 tick 后 burst pressure 必须下降"
        );
    }

    private static KongqiaoPressureProjection runRelogAndProjectionRoundTripStage(
        final GameTestHelper helper,
        final ServerPlayer owner,
        final KongqiaoData data,
        final NianTouUnlocks unlocks,
        final TweakConfig tweakConfig,
        final KongqiaoInventory inventory,
        final KongqiaoCapacityProfile rawCapacityProfile
    ) {
        final var provider = owner.level().registryAccess();
        final var dataTag = data.serializeNBT(provider);
        final var unlockTag = unlocks.serializeNBT(provider);
        final var tweakTag = tweakConfig.serializeNBT(provider);

        final KongqiaoData relogData = new KongqiaoData();
        relogData.deserializeNBT(provider, dataTag);
        relogData.bind(owner);
        final NianTouUnlocks relogUnlocks = new NianTouUnlocks();
        relogUnlocks.deserializeNBT(provider, unlockTag);
        final TweakConfig relogTweak = new TweakConfig();
        relogTweak.deserializeNBT(provider, tweakTag);

        helper.assertTrue(
            relogData.isGameplayActivated(),
            "smoke/relog: gameplayActivated 必须在原始态回环后保留"
        );
        helper.assertTrue(
            relogUnlocks.isShazhaoUnlocked(ResourceLocation.parse(TEST_SHAZHAO_ID)),
            "smoke/relog: shazhao 解锁必须在原始态回环后保留"
        );
        helper.assertTrue(
            relogTweak.getWheelSkills().contains(TEST_SHAZHAO_ID),
            "smoke/relog: 轮盘偏好必须在原始态回环后保留"
        );

        final KongqiaoPressureProjection projectionAfterFlow =
            KongqiaoPressureProjectionService.assemblePressureProjection(data);
        final KongqiaoPressureProjection projectionRoundTrip =
            KongqiaoPressureProjection.fromTag(projectionAfterFlow.toTag());
        helper.assertTrue(
            projectionRoundTrip.overloadTier() == projectionAfterFlow.overloadTier(),
            "smoke/projection: toTag/fromTag 回环后 overload tier 必须一致"
        );
        helper.assertTrue(
            projectionRoundTrip.effectivePressure() == projectionAfterFlow.effectivePressure(),
            "smoke/projection: toTag/fromTag 回环后 effectivePressure 必须一致"
        );
        helper.assertTrue(
            rawCapacityProfile.totalRows() == inventory.getSettings().getUnlockedRows(),
            "smoke/projection: 容量行数一致性由 raw profile 与 owner inventory 单独保证"
        );
        return projectionAfterFlow;
    }

    private static void runPayloadRoundTripStage(
        final GameTestHelper helper,
        final ServerPlayer owner,
        final KongqiaoData data,
        final NianTouUnlocks unlocks,
        final KongqiaoPressureProjection projectionAfterFlow
    ) {
        final ByteBuf unlockBuffer = Unpooled.buffer();
        PacketSyncNianTouUnlocks.STREAM_CODEC.encode(
            unlockBuffer,
            new PacketSyncNianTouUnlocks(unlocks)
        );
        final PacketSyncNianTouUnlocks decodedUnlockPayload =
            PacketSyncNianTouUnlocks.STREAM_CODEC.decode(unlockBuffer);
        helper.assertTrue(
            decodedUnlockPayload
                .data()
                .isShazhaoUnlocked(ResourceLocation.parse(TEST_SHAZHAO_ID)),
            "smoke/payload: PacketSyncNianTouUnlocks 回环后杀招解锁必须一致"
        );

        final var provider = owner.level().registryAccess();
        final var dataTag = data.serializeNBT(provider);
        final ClientboundKongqiaoSyncPayload syncPayload =
            new ClientboundKongqiaoSyncPayload(dataTag, projectionAfterFlow.toTag());
        final KongqiaoPressureProjection payloadProjection =
            KongqiaoPressureProjection.fromTag(syncPayload.projection());
        helper.assertTrue(
            payloadProjection.overloadTier() == projectionAfterFlow.overloadTier(),
            "smoke/payload: Clientbound payload 投影必须与服务端权威投影一致"
        );
        helper.assertTrue(
            payloadProjection.effectivePressure() == projectionAfterFlow.effectivePressure(),
            "smoke/payload: Clientbound payload effectivePressure 必须与权威投影一致"
        );
    }

    private static void assertFinalAttachmentIdentity(
        final GameTestHelper helper,
        final ServerPlayer owner,
        final KongqiaoData data,
        final NianTouUnlocks unlocks,
        final TweakConfig tweakConfig,
        final ActivePassives activePassives
    ) {
        helper.assertTrue(
            KongqiaoAttachments.getData(owner) == data,
            "smoke/final: owner->data 依旧必须是同一引用"
        );
        helper.assertTrue(
            KongqiaoAttachments.getUnlocks(owner) == unlocks,
            "smoke/final: owner->unlocks 依旧必须是同一引用"
        );
        helper.assertTrue(
            KongqiaoAttachments.getTweakConfig(owner) == tweakConfig,
            "smoke/final: owner->tweakConfig 依旧必须是同一引用"
        );
        helper.assertTrue(
            KongqiaoAttachments.getActivePassives(owner) == activePassives,
            "smoke/final: owner->activePassives 依旧必须是同一引用"
        );
    }

    private record SmokeAppleUsageSetup(
        ItemStack apple,
        ResourceLocation appleId,
        NianTouData.Usage passiveUsage
    ) {
    }

    private static void registerLifecycleInjectedDataAndEffects() {
        NianTouDataManager.register(
            new NianTouData(
                "minecraft:apple",
                List.of(
                    new NianTouData.Usage(
                        TEST_USAGE_ID,
                        "Task10 生命周期被动用途",
                        "用于生命周期共享图烟雾验证",
                        "task10 lifecycle passive usage",
                        INJECTED_PASSIVE_USAGE_PRESSURE,
                        INJECTED_PASSIVE_USAGE_ACTIVE_COST,
                        Map.of()
                    )
                )
            )
        );
        ShazhaoDataManager.register(
            new ShazhaoData(
                TEST_SHAZHAO_ID,
                "Task10 生命周期杀招",
                "用于生命周期共享图烟雾验证",
                "task10 lifecycle shazhao",
                INJECTED_SHAZHAO_COST,
                List.of("minecraft:apple"),
                Map.of()
            )
        );
        GuEffectRegistry.register(new TestLifecyclePassiveEffect());
        ShazhaoEffectRegistry.register(new TestLifecycleShazhaoActiveEffect());
    }

    private static Object invokeEvaluatePassiveRuntimeSnapshot(
        final KongqiaoCapacityProfile capacityProfile,
        final double burstPressure,
        final double fatigueDebt
    ) throws Exception {
        final Method evaluatePassiveRuntimeSnapshot =
            KongqiaoPressureProjectionService.class.getDeclaredMethod(
                "evaluatePassiveRuntimeSnapshot",
                java.util.Collection.class,
                KongqiaoCapacityProfile.class,
                double.class,
                double.class
            );
        evaluatePassiveRuntimeSnapshot.setAccessible(true);
        return evaluatePassiveRuntimeSnapshot.invoke(
            null,
            List.of(),
            capacityProfile,
            burstPressure,
            fatigueDebt
        );
    }

    private static Object invokeRunPassiveUsageIfAllowed(
        final ActivePassives activePassives,
        final ServerPlayer owner,
        final ItemStack stack,
        final NianTouData.Usage usage,
        final IGuEffect effect,
        final Object passiveRuntimeSnapshot,
        final boolean isSecond
    ) throws Exception {
        final Class<?> passiveRuntimeSnapshotClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjectionService$PassiveRuntimeSnapshot"
        );
        final Method runPassiveUsageIfAllowed = GuRunningService.class.getDeclaredMethod(
            "runPassiveUsageIfAllowed",
            ActivePassives.class,
            net.minecraft.world.entity.LivingEntity.class,
            ItemStack.class,
            NianTouData.Usage.class,
            IGuEffect.class,
            passiveRuntimeSnapshotClass,
            boolean.class
        );
        runPassiveUsageIfAllowed.setAccessible(true);
        return runPassiveUsageIfAllowed.invoke(
            null,
            activePassives,
            owner,
            stack,
            usage,
            effect,
            passiveRuntimeSnapshot,
            isSecond
        );
    }

    private static Object invokeActivateResolvedShazhaoEffectForTests(
        final ShazhaoData shazhaoData,
        final Object activeEffect,
        final KongqiaoData.StabilityState stabilityState,
        final double currentEffectivePressure,
        final double pressureCap
    ) throws Exception {
        final Class<?> activeEffectType = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.logic.IShazhaoActiveEffect"
        );
        final Class<?> shazhaoActiveServiceClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.service.ShazhaoActiveService"
        );
        final Method activateResolvedEffectForTests =
            shazhaoActiveServiceClass.getDeclaredMethod(
                "activateResolvedEffectForTests",
                ShazhaoData.class,
                activeEffectType,
                KongqiaoData.StabilityState.class,
                double.class,
                double.class
            );
        activateResolvedEffectForTests.setAccessible(true);
        return activateResolvedEffectForTests.invoke(
            null,
            shazhaoData,
            activeEffect,
            stabilityState,
            currentEffectivePressure,
            pressureCap
        );
    }

    private static Object invokeActivateResolvedUsageForTests(
        final NianTouData.Usage usage,
        final IGuEffect effect,
        final double currentEffectivePressure,
        final double pressureCap
    ) throws Exception {
        final Method activateResolvedUsageForTests = GuRunningService.class.getDeclaredMethod(
            "activateResolvedUsageForTests",
            NianTouData.Usage.class,
            IGuEffect.class,
            double.class,
            double.class
        );
        activateResolvedUsageForTests.setAccessible(true);
        return activateResolvedUsageForTests.invoke(
            null,
            usage,
            effect,
            currentEffectivePressure,
            pressureCap
        );
    }

    private static double basePressureCapByAptitude(
        final KongqiaoAptitudeTier aptitudeTier
    ) {
        if (aptitudeTier == null) {
            return 0.0D;
        }
        return switch (aptitudeTier) {
            case CANCI -> PRESSURE_CAP_CANCI;
            case XIADENG -> PRESSURE_CAP_XIADENG;
            case ZHONGDENG -> PRESSURE_CAP_ZHONGDENG;
            case SHANGDENG -> PRESSURE_CAP_SHANGDENG;
            case JUEPIN -> PRESSURE_CAP_JUEPIN;
        };
    }

    private static void invokeApplyRecoveryTick(
        final KongqiaoData.StabilityState stabilityState,
        final long currentGameTime,
        final KongqiaoCapacityProfile capacityProfile,
        final int sealedCount
    ) throws Exception {
        final int unlockedSlots = Math.max(1, capacityProfile.totalRows() * 9 - sealedCount);

        final Method applyRecoveryTick = GuRunningService.class.getDeclaredMethod(
            "applyRecoveryTick",
            KongqiaoData.StabilityState.class,
            long.class,
            KongqiaoCapacityProfile.class,
            List.class,
            int.class
        );
        applyRecoveryTick.setAccessible(true);
        applyRecoveryTick.invoke(
            null,
            stabilityState,
            currentGameTime,
            capacityProfile,
            List.of(),
            unlockedSlots
        );
    }

    private static final class TestLifecyclePassiveEffect implements IGuEffect {

        private static final AtomicInteger ON_EQUIP_COUNT = new AtomicInteger();
        private static final AtomicInteger ON_TICK_COUNT = new AtomicInteger();
        private static final AtomicInteger ON_SECOND_COUNT = new AtomicInteger();

        private static void reset() {
            ON_EQUIP_COUNT.set(0);
            ON_TICK_COUNT.set(0);
            ON_SECOND_COUNT.set(0);
        }

        @Override
        public String getUsageId() {
            return TEST_USAGE_ID;
        }

        @Override
        public void onEquip(
            final net.minecraft.world.entity.LivingEntity user,
            final ItemStack stack,
            final NianTouData.Usage usageInfo
        ) {
            ON_EQUIP_COUNT.incrementAndGet();
            final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
            if (actives != null) {
                actives.add(TEST_USAGE_ID);
            }
        }

        @Override
        public void onTick(
            final net.minecraft.world.entity.LivingEntity user,
            final ItemStack stack,
            final NianTouData.Usage usageInfo
        ) {
            ON_TICK_COUNT.incrementAndGet();
            final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
            if (actives != null) {
                actives.add(TEST_USAGE_ID);
            }
        }

        @Override
        public void onSecond(
            final net.minecraft.world.entity.LivingEntity user,
            final ItemStack stack,
            final NianTouData.Usage usageInfo
        ) {
            ON_SECOND_COUNT.incrementAndGet();
            final ActivePassives actives = KongqiaoAttachments.getActivePassives(user);
            if (actives != null) {
                actives.add(TEST_USAGE_ID);
            }
        }

        @Override
        public boolean onActivate(
            final net.minecraft.world.entity.LivingEntity user,
            final ItemStack stack,
            final NianTouData.Usage usageInfo
        ) {
            return true;
        }
    }

    private static final class TestLifecycleShazhaoActiveEffect
        implements IShazhaoActiveEffect {

        private static final AtomicInteger ON_ACTIVATE_COUNT = new AtomicInteger();

        private static void reset() {
            ON_ACTIVATE_COUNT.set(0);
        }

        @Override
        public String getShazhaoId() {
            return TEST_SHAZHAO_ID;
        }

        @Override
        public boolean onActivate(
            final ServerPlayer player,
            final ShazhaoData data
        ) {
            ON_ACTIVATE_COUNT.incrementAndGet();
            return true;
        }
    }
}
