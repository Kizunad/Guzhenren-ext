package com.Kizunad.guzhenrenext_test.xianqiao;

import com.Kizunad.guzhenrenext.xianqiao.ascension.contract.AscensionAttemptStage;
import com.Kizunad.guzhenrenext.xianqiao.daomark.DaoMarkDiffusionService;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInfo;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.ApertureInitializationState;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData.AscensionAttemptState;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("guzhenrenext")
@PrefixGameTestTemplate(false)
public class Task6FailureRecoveryGameTests {

    private static final String TASK6_BATCH = "task6_failure_recovery";
    private static final int TEST_TIMEOUT_TICKS = 240;
    private static final int APERTURE_RADIUS_BLOCKS = 64;
    private static final int BLOCKED_RECOVERY_CHECK_DELAY_TICKS = 5;
    private static final int RECOVERY_COMPLETION_CHECK_DELAY_TICKS = 10;

    private static final UUID OWNER_UUID_A = UUID.fromString("00000000-0000-0000-0000-000000060001");
    private static final UUID OWNER_UUID_B = UUID.fromString("00000000-0000-0000-0000-000000060002");

    private static final String OWNER_NAME_A = "task6_owner_a";
    private static final String OWNER_NAME_B = "task6_owner_b";

    @GameTest(template = "examplegametests.empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK6_BATCH)
    public void testTask6FailureAftermathShouldPersistAndKeepPenaltyIdempotent(GameTestHelper helper) {
        ServerLevel level = resolveAuthoritativeLevel(helper.getLevel());
        ServerPlayer owner = createTestPlayer(level, OWNER_UUID_A, OWNER_NAME_A);
        ApertureWorldData worldData = prepareApertureContext(level, owner);
        UUID ownerUuid = owner.getUUID();

        worldData.recordFailureAftermath(ownerUuid, AscensionAttemptStage.FAILED_DEATH);
        worldData.markFailurePenaltyApplied(ownerUuid);
        worldData.recordFailureAftermath(ownerUuid, AscensionAttemptStage.FAILED_DEATH);
        worldData.markFailurePenaltyApplied(ownerUuid);

        helper.succeedWhen(() -> {
            AscensionAttemptState attemptState = ApertureWorldData.get(resolveAuthoritativeLevel(helper.getLevel()))
                .getAscensionAttemptState(ownerUuid);
            helper.assertTrue(
                attemptState.stage() == AscensionAttemptStage.FAILED_DEATH,
                "失败后果未持久化为 FAILED_DEATH"
            );
            helper.assertTrue(attemptState.failurePenaltyApplied(), "失败惩罚应用标记未持久化");
            helper.assertTrue(
                attemptState.failurePenaltyApplicationCount() == 1,
                "重复失败/重入后惩罚应用次数不应超过 1"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "examplegametests.empty", timeoutTicks = TEST_TIMEOUT_TICKS, batch = TASK6_BATCH)
    public void testTask6RecoveryShouldStayBlockedUntilExternalRiskLegCompletes(GameTestHelper helper) {
        ServerLevel level = resolveAuthoritativeLevel(helper.getLevel());
        ServerPlayer owner = createTestPlayer(level, OWNER_UUID_B, OWNER_NAME_B);
        ApertureWorldData worldData = prepareApertureContext(level, owner);
        UUID ownerUuid = owner.getUUID();

        worldData.recordFailureAftermath(ownerUuid, AscensionAttemptStage.FAILED_SEVERE_INJURY);
        worldData.markFailurePenaltyApplied(ownerUuid);
        worldData.markInternalRecoveryComplete(ownerUuid);
        worldData.completeRecoveryIfEligible(ownerUuid);

        helper.runAfterDelay(BLOCKED_RECOVERY_CHECK_DELAY_TICKS, () -> {
            AscensionAttemptState blockedState = ApertureWorldData.get(resolveAuthoritativeLevel(helper.getLevel()))
                .getAscensionAttemptState(ownerUuid);
            helper.assertTrue(
                blockedState.stage() == AscensionAttemptStage.FAILED_SEVERE_INJURY,
                "缺少外部险行恢复腿时不应提前回到 APERTURE_FORMING"
            );
            helper.assertTrue(blockedState.internalRecoveryCompleted(), "内养恢复腿应已持久化完成");
            helper.assertTrue(
                !blockedState.externalRiskRecoveryCompleted(),
                "外部险行恢复腿在未完成前不应被误标为完成"
            );
            ApertureWorldData authoritativeWorldData = ApertureWorldData.get(
                resolveAuthoritativeLevel(helper.getLevel())
            );
            authoritativeWorldData.markExternalRiskRecoveryComplete(ownerUuid);
            authoritativeWorldData.completeRecoveryIfEligible(ownerUuid);
        });

        helper.runAfterDelay(RECOVERY_COMPLETION_CHECK_DELAY_TICKS, () -> {
            AscensionAttemptState recoveredState = ApertureWorldData.get(resolveAuthoritativeLevel(helper.getLevel()))
                .getAscensionAttemptState(ownerUuid);
            helper.assertTrue(
                recoveredState.stage() == AscensionAttemptStage.APERTURE_FORMING,
                "补齐外部险行恢复腿后应可回到 APERTURE_FORMING"
            );
            helper.assertTrue(recoveredState.failurePenaltyApplied(), "恢复后失败惩罚真相不应被静默抹掉");
            helper.assertTrue(
                recoveredState.failurePenaltyApplicationCount() == 1,
                "恢复闭环不应重复叠加失败惩罚计数"
            );
            helper.succeed();
        });
    }

    private static ApertureWorldData prepareApertureContext(ServerLevel level, ServerPlayer owner) {
        ServerLevel overworldLevel = level.getServer().overworld();
        ApertureWorldData overworldData = ApertureWorldData.get(overworldLevel);
        overworldData.allocateAperture(owner.getUUID());

        ServerLevel apertureLevel = level.getServer().getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        if (apertureLevel == null) {
            apertureLevel = overworldLevel;
        }
        ApertureWorldData worldData = ApertureWorldData.get(apertureLevel);
        ApertureInfo info = worldData.getOrAllocate(owner.getUUID());
        worldData.updateBoundaryByRadius(owner.getUUID(), APERTURE_RADIUS_BLOCKS);
        worldData.setInitializationState(
            owner.getUUID(),
            new ApertureInitializationState(
                ApertureWorldData.InitPhase.COMPLETED,
                null,
                null,
                null,
                AscensionAttemptState.defaultForPhase(ApertureWorldData.InitPhase.COMPLETED),
                null
            )
        );
        worldData.updateTribulationTick(owner.getUUID(), info.nextTribulationTick());
        return worldData;
    }

    private static ServerLevel resolveAuthoritativeLevel(ServerLevel fallbackLevel) {
        ServerLevel apertureLevel = fallbackLevel.getServer().getLevel(DaoMarkDiffusionService.APERTURE_DIMENSION);
        return apertureLevel != null ? apertureLevel : fallbackLevel.getServer().overworld();
    }

    private static ServerPlayer createTestPlayer(ServerLevel level, UUID playerUuid, String playerName) {
        return FakePlayerFactory.get(level, new com.mojang.authlib.GameProfile(playerUuid, playerName));
    }
}
