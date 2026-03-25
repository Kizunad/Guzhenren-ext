package com.Kizunad.guzhenrenext.xianqiao.entry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApertureEntryFlowIntegrationTests {

    private static final int EXPECTED_MAX_RESERVED_CHAOS_BLOCKS = 16;

    private static final int SCORE_80 = 80;

    private static final int SCORE_90 = 90;

    private static final Pattern FROM_PLAYER_VARIABLES_TRUE_TRUE_PATTERN = Pattern.compile(
        "fromPlayerVariables\\s*\\([^)]*true\\s*,\\s*true\\s*\\)",
        Pattern.DOTALL
    );

    private final DefaultApertureInitializationApplicationService service =
        new DefaultApertureInitializationApplicationService();

    @Test
    void stagedFlowMustStayFrozenBeforeAnyWorldMutationTaskStarts() {
        List<ApertureOpeningPhase> expectedFlow = List.of(
            ApertureOpeningPhase.LOGIN_INIT,
            ApertureOpeningPhase.CULTIVATION_PROGRESSION,
            ApertureOpeningPhase.RANK_FIVE_PEAK_CHECK,
            ApertureOpeningPhase.THREE_QI_READINESS_AND_BALANCE_CHECK,
            ApertureOpeningPhase.ASCENSION_ATTEMPT_CONFIRMATION_CONFIRMED,
            ApertureOpeningPhase.PROFILE_RESOLUTION,
            ApertureOpeningPhase.LAYOUT_PLANNING,
            ApertureOpeningPhase.TERRAIN_MATERIALIZATION,
            ApertureOpeningPhase.CORE_SPIRIT_BOUNDARY_FINALIZATION
        );

        assertEquals(
            expectedFlow,
            ApertureEntryFlowContract.stagedFlow(),
            "开窍阶段流必须先冻结，后续任务只允许在该顺序内实现，禁止回退到命令直进初始化语义"
        );
    }

    @Test
    void legacyCommandAdapterAndGameplayEntryMustConvergeSameApplicationServiceContract() {
        UUID playerId = UUID.randomUUID();
        CapturingExecution legacyExecution = new CapturingExecution();
        CapturingExecution gameplayExecution = new CapturingExecution();

        ApertureEntryGateway legacyGateway = new LegacyCommandApertureEntryGateway();
        ApertureEntryGateway gameplayGateway = new GameplayApertureEntryGateway();
        ApertureInitializationResult legacyResult = legacyGateway.trigger(
            confirmedRequest(playerId, ApertureEntryChannel.LEGACY_COMMAND),
            service
        );
        ApertureInitializationResult gameplayResult = gameplayGateway.trigger(
            confirmedRequest(playerId, ApertureEntryChannel.HUB_V1_GAMEPLAY),
            service
        );

        service.beginInitialization(
            confirmedRequest(playerId, ApertureEntryChannel.LEGACY_COMMAND),
            legacyExecution
        );
        service.beginInitialization(
            confirmedRequest(playerId, ApertureEntryChannel.HUB_V1_GAMEPLAY),
            gameplayExecution
        );

        assertEquals(
            ApertureInitializationResult.Status.INITIALIZATION_EXECUTED,
            legacyResult.status(),
            "legacy command 必须保留为适配器并转调初始化应用服务，而不是直接持有初始化执行细节"
        );
        assertEquals(
            ApertureInitializationResult.Status.INITIALIZATION_EXECUTED,
            gameplayResult.status(),
            "玩法入口必须与 legacy adapter 汇聚到同一应用服务，避免形成双轨初始化协议"
        );

        ApertureInitializationRequest legacyRequest = legacyExecution.requests().getFirst();
        ApertureInitializationRequest gameplayRequest = gameplayExecution.requests().getFirst();
        assertEquals(playerId, legacyRequest.playerId());
        assertEquals(playerId, gameplayRequest.playerId());
        assertEquals(
            legacyRequest.stagedFlow(),
            gameplayRequest.stagedFlow(),
            "两个入口的阶段定义必须一致，后续实现不能对某一路入口私自增删阶段"
        );
        assertEquals(
            legacyRequest.rankFivePeak(),
            gameplayRequest.rankFivePeak(),
            "五转巅峰触发门槛是统一规则，不能按入口分叉"
        );
        assertEquals(
            legacyRequest.threeQiReady(),
            gameplayRequest.threeQiReady(),
            "三气充足门槛是统一规则，不能按入口分叉"
        );
        assertEquals(
            legacyRequest.threeQiBalanced(),
            gameplayRequest.threeQiBalanced(),
            "三气平衡门槛是统一规则，不能按入口分叉"
        );
        assertEquals(
            legacyRequest.ascensionAttemptConfirmed(),
            gameplayRequest.ascensionAttemptConfirmed(),
            "CONFIRMED 语义必须统一为主动发起升仙尝试，而不是普通确认"
        );
        assertEquals(
            legacyRequest.snapshotFrozen(),
            gameplayRequest.snapshotFrozen(),
            "CONFIRMED 后输入快照冻结语义必须统一，避免实现期出现可变输入导致非确定性"
        );
        assertEquals(ApertureEntryChannel.LEGACY_COMMAND, legacyRequest.entryChannel());
        assertEquals(ApertureEntryChannel.HUB_V1_GAMEPLAY, gameplayRequest.entryChannel());
    }

    @Test
    void hardInvariantsMustBeEncodedAsArchitectureContract() {
        ApertureInitializationInvariantContract invariantContract =
            ApertureInitializationInvariantContract.defaults();

        assertTrue(
            invariantContract.chunkBoundaryTruth(),
            "边界真源必须持续锚定 center + min/maxChunk，后续实现不得把边界真源降级回半径语义"
        );
        assertTrue(
            invariantContract.seamCenterForEvenLayouts(),
            "偶数布局必须使用接缝中心，才能保证2x2与4x4扩展在中心向外拼接时保持一致定位"
        );
        assertTrue(
            invariantContract.symmetricFragmentExpansion(),
            "九天碎片v1必须保持对称矩形扩张兼容，防止出现单向偏移导致边界模型破坏"
        );
        assertTrue(
            invariantContract.triggerRequiresRankFivePeakAndThreeQiAndConfirmedAttempt(),
            "触发条件必须固定为五转巅峰+三气充足平衡+主动发起升仙尝试，禁止退化为普通UI确认"
        );
        assertEquals(
            EXPECTED_MAX_RESERVED_CHAOS_BLOCKS,
            invariantContract.maxReservedChaosBlocks(),
            "混沌预留上限必须冻结为16格（方块语义），后续实现不得误用为chunk单位"
        );
    }

    @Test
    void confirmedStageMustImplySnapshotFrozenForDeterministicPlanning() {
        CapturingExecution execution = new CapturingExecution();
        ApertureInitializationRequest request = confirmedRequest(UUID.randomUUID(), ApertureEntryChannel.HUB_V1_GAMEPLAY);
        service.beginInitialization(request, execution);

        ApertureInitializationRequest captured = execution.requests().getFirst();
        assertNotNull(captured);
        assertTrue(
            captured.ascensionAttemptConfirmed(),
            "CONFIRMED 的第一含义是玩家已经主动发起本次升仙尝试"
        );
        assertTrue(
            captured.snapshotFrozen(),
            "CONFIRMED 的第二含义是输入快照被冻结，规划阶段才能保证可复算与可恢复"
        );
    }

    @Test
    void underqualifiedPlayersMustBeRejectedAtUnifiedServiceBoundary() {
        CapturingExecution execution = new CapturingExecution();
        ApertureInitializationResult result = service.beginInitialization(
            new ApertureInitializationRequest(
                UUID.randomUUID(),
                ApertureEntryChannel.HUB_V1_GAMEPLAY,
                ApertureEntryFlowContract.stagedFlow(),
                SCORE_80,
                SCORE_80,
                SCORE_80,
                SCORE_90,
                false,
                true,
                true,
                true,
                true,
                false
            ),
            execution
        );

        assertEquals(ApertureInitializationResult.Status.REJECTED_NOT_RANK_FIVE_PEAK, result.status());
        assertTrue(result.message().contains("五转巅峰"));
        assertTrue(execution.requests().isEmpty(), "统一服务拒绝后不得继续触发初始化执行缝隙");
    }

    @Test
    void alreadyInitializedRequestsStillReuseSameServicePathAndExecution() {
        CapturingExecution execution = new CapturingExecution();
        ApertureInitializationRequest request = new ApertureInitializationRequest(
            UUID.randomUUID(),
            ApertureEntryChannel.LEGACY_COMMAND,
            ApertureEntryFlowContract.stagedFlow(),
            0,
            0,
            0,
            0,
            false,
            false,
            false,
            false,
            false,
            true
        );

        ApertureInitializationResult result = service.beginInitialization(request, execution);

        assertEquals(ApertureInitializationResult.Status.ALREADY_INITIALIZED, result.status());
        assertTrue(result.executionTriggered(), "已初始化请求也必须沿同一服务/执行缝隙进入传送路径");
        assertEquals(1, execution.requests().size());
    }

    @Test
    void commandSourceMustRouteThroughSharedRuntimePath() throws IOException {
        String source = readProjectSource(
            "src/main/java/com/Kizunad/guzhenrenext/xianqiao/command/ApertureCommand.java"
        );

        assertTrue(
            source.contains("ApertureEntryRuntime.trigger("),
            "legacy command 必须通过共享运行时入口收敛，而不能继续独占旧直进初始化主路径"
        );
        assertTrue(
            source.contains("ApertureEntryChannel.LEGACY_COMMAND"),
            "legacy command 必须显式标记为 LEGACY_COMMAND 渠道，确保统一服务可审计来源"
        );
    }

    @Test
    void hubV1SourceMustExposeGameplayTriggerAndRouteToSameRuntime() throws IOException {
        String screenSource = readProjectSource(
            "src/main/java/com/Kizunad/guzhenrenext/xianqiao/client/ApertureHubScreen.java"
        );
        String payloadSource = readProjectSource(
            "src/main/java/com/Kizunad/guzhenrenext/network/ServerboundApertureEntryPayload.java"
        );

        assertTrue(
            screenSource.contains("ServerboundApertureEntryPayload"),
            "Hub v1 界面必须暴露真实的服务端触发包，而不是继续停留在只读展示态"
        );
        assertTrue(
            screenSource.contains("PacketDistributor.sendToServer(new ServerboundApertureEntryPayload())"),
            "Hub v1 的最小玩法入口必须能把按钮操作发送到服务端"
        );
        assertTrue(
            payloadSource.contains("ApertureEntryRuntime.trigger("),
            "Hub v1 触发包必须与命令入口收敛到同一共享运行时路径"
        );
        assertTrue(
            payloadSource.contains("ApertureEntryChannel.HUB_V1_GAMEPLAY"),
            "Hub v1 触发包必须显式标记 gameplay 渠道，确保统一服务可区分入口来源"
        );
    }

    @Test
    void entryPathMustNotHardcodeConfirmedAttemptAsTrueTrue() throws IOException {
        String requestFactorySource = readProjectSource(
            "src/main/java/com/Kizunad/guzhenrenext/xianqiao/entry/ApertureInitializationRequestFactory.java"
        );
        String planningHelperSource = readProjectSource(
            "src/main/java/com/Kizunad/guzhenrenext/xianqiao/entry/AperturePrimaryPathPlanningHelper.java"
        );

        assertFalse(
            FROM_PLAYER_VARIABLES_TRUE_TRUE_PATTERN.matcher(requestFactorySource).find(),
            "请求工厂入口不得继续把 ascensionAttemptInitiated/snapshotFrozen 硬编码为 true,true"
        );
        assertFalse(
            FROM_PLAYER_VARIABLES_TRUE_TRUE_PATTERN.matcher(planningHelperSource).find(),
            "主路径规划 helper 入口不得继续把 ascensionAttemptInitiated/snapshotFrozen 硬编码为 true,true"
        );
        assertTrue(
            requestFactorySource.contains("resolveAscensionAttemptSeam(variables)"),
            "请求工厂必须显式读取玩家态接缝，不能把 CONFIRMED 语义硬编码为 true/true"
        );
        assertTrue(
            planningHelperSource.contains("resolveAscensionAttemptSeam(variables)"),
            "主路径规划 helper 必须显式读取玩家态接缝，不能把 CONFIRMED 语义硬编码为 true/true"
        );
    }

    private static ApertureInitializationRequest confirmedRequest(UUID playerId, ApertureEntryChannel channel) {
        return new ApertureInitializationRequest(
            playerId,
            channel,
            ApertureEntryFlowContract.stagedFlow(),
            SCORE_80,
            SCORE_80,
            SCORE_80,
            SCORE_90,
            true,
            true,
            true,
            true,
            true,
            false
        );
    }

    private static String readProjectSource(String relativePath) throws IOException {
        return Files.readString(Path.of(relativePath));
    }

    private static final class CapturingExecution
        implements ApertureInitializationApplicationService.ApertureInitializationExecution {

        private final List<ApertureInitializationRequest> requests = new ArrayList<>();

        @Override
        public void execute(ApertureInitializationRequest request) {
            this.requests.add(request);
        }

        private List<ApertureInitializationRequest> requests() {
            return requests;
        }
    }
}
