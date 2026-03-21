package com.Kizunad.guzhenrenext.network;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ServerboundBenmingSwordActionPayloadTests {


    private static final int TEST_MAGIC_4 = 4;
    private static final int TEST_MAGIC_6 = 6;
    private static final long TEST_BASE_TICK = 100L;
    private static final String BURST_COOLDOWN_DEDUPE_KEY =
        "controller.BURST_ATTEMPT.BURST_COOLDOWN_ACTIVE";
    private static final String BURST_RESOURCE_DEDUPE_KEY =
        "controller.BURST_ATTEMPT.BURST_RESOURCES_INSUFFICIENT";
    private static final String GUIDE_AFTER_BOND_KEY =
        "message.guzhenrenext.flyingsword.benming.guide.after_bond";
    private static final String GUIDE_BOND_FAIL_NEXT_STEP_KEY =
        "message.guzhenrenext.flyingsword.benming.guide.bond_fail_next_step";
    private static final String GUIDE_RESONANCE_FIRST_CHOICE_KEY =
        "message.guzhenrenext.flyingsword.benming.guide.resonance_first_choice";
    private static final String GUIDE_OVERLOAD_FIRST_WARNING_KEY =
        "message.guzhenrenext.flyingsword.benming.guide.overload_first_warning";

    private static final String PAYLOAD_CLASS_NAME =
        "com.Kizunad.guzhenrenext.network.ServerboundBenmingSwordActionPayload";
    private static final String CONTROLLER_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.FlyingSwordController";
    private static final String BOND_SERVICE_CLASS_NAME =
        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.ops.BenmingSwordBondService";
    private static final String RESONANCE_TYPE_CLASS_RESOURCE =
        "com/Kizunad/guzhenrenext/kongqiao/flyingsword/resonance/FlyingSwordResonanceType.class";
    private static final String NBT_TAG_CLASS_RESOURCE = "net/minecraft/nbt/Tag.class";
    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main");
    private static final Path MAIN_RESOURCES = Path.of("build/resources/main");
    private static final Path ARTIFACT_MANIFEST =
        Path.of("build/tmp/createMinecraftArtifacts/nfrt_artifact_manifest.properties");

    @Test
    void executeDelegatesBondRoutesToExecutor() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object executor = api.newExecutorProxy(
            api.newBondSuccessResult("QUERY", "stable-query"),
            api.newBondSuccessResult("RITUAL_BIND", "stable-bind"),
            api.newBondSuccessResult("ACTIVE_UNBIND", "stable-unbind"),
            api.newBondSuccessResult("FORCED_UNBIND", "stable-forced"),
            api.newControllerSuccessResult("RESONANCE_SWITCH", "stable-resonance", "defense", 0L),
            api.newControllerSuccessResult("BURST_ATTEMPT", "stable-burst", "spirit", 1240L)
        );

        final Object query = api.execute("QUERY", executor);
        final Object bind = api.execute("RITUAL_BIND", executor);
        final Object activeUnbind = api.execute("ACTIVE_UNBIND", executor);

        assertEquals("QUERY", api.bondBranch(query));
        assertEquals("RITUAL_BIND", api.bondBranch(bind));
        assertEquals("ACTIVE_UNBIND", api.bondBranch(activeUnbind));
        assertEquals(1, api.executorCallCount(executor, "query"));
        assertEquals(1, api.executorCallCount(executor, "ritualBind"));
        assertEquals(1, api.executorCallCount(executor, "activeUnbind"));
        assertEquals(0, api.executorCallCount(executor, "forcedUnbind"));
        assertEquals(0, api.executorCallCount(executor, "switchResonance"));
        assertEquals(0, api.executorCallCount(executor, "attemptBurst"));
    }

    @Test
    void feedbackExposesBondRouteMessagesToPlayer() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final String successMessage = api.feedbackForBondResult(
            api.newBondSuccessResult("RITUAL_BIND", "stable-bind")
        );
        final String failureMessage = api.feedbackForBondResult(
            api.newBondFailureResult(
                "ACTIVE_UNBIND",
                "TARGET_NOT_BOUND_TO_PLAYER",
                "stable-bind"
            )
        );

        assertEquals("[本命飞剑] 仪式缔结成功：stable-bind", successMessage);
        assertEquals("[本命飞剑] 目标并非你的本命飞剑：stable-bind", failureMessage);
    }

    @Test
    void feedbackExposesResonanceAndBurstMessagesToPlayer() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final String resonanceMessage = api.feedbackForControllerResult(
            api.newControllerSuccessResult(
                "RESONANCE_SWITCH",
                "stable-benming",
                "defense",
                0L
            )
        );
        final String burstMessage = api.feedbackForControllerResult(
            api.newControllerFailureResult(
                "BURST_ATTEMPT",
                "BURST_COOLDOWN_ACTIVE",
                "stable-benming",
                "defense",
                1260L
            )
        );

        assertEquals("[本命飞剑] 已切换本命共鸣：稳（御）", resonanceMessage);
        assertEquals("[本命飞剑] 本命爆发仍在冷却中，请稍后再试。", burstMessage);
    }

    @Test
    void feedbackKeepsBurstUnavailableReasonsReadableAndDistinct()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        final String cooldownMessage = api.feedbackForControllerResult(
            api.newControllerFailureResult(
                "BURST_ATTEMPT",
                "BURST_COOLDOWN_ACTIVE",
                "stable-benming",
                "defense",
                1260L
            )
        );
        final String resourceMessage = api.feedbackForControllerResult(
            api.newControllerFailureResult(
                "BURST_ATTEMPT",
                "BURST_RESOURCES_INSUFFICIENT",
                "stable-benming",
                "defense",
                1260L
            )
        );
        final String overloadMessage = api.feedbackForControllerResult(
            api.newControllerFailureResult(
                "BURST_ATTEMPT",
                "BURST_OVERLOAD_BLOCKED",
                "stable-benming",
                "defense",
                1260L
            )
        );
        final String illegalStateMessage = api.feedbackForControllerResult(
            api.newControllerFailureResult(
                "BURST_ATTEMPT",
                "BOND_STATE_INVALID",
                "stable-benming",
                "defense",
                1260L
            )
        );

        assertEquals("[本命飞剑] 本命爆发仍在冷却中，请稍后再试。", cooldownMessage);
        assertEquals("[本命飞剑] 本命爆发所需资源不足，无法触发。", resourceMessage);
        assertEquals("[本命飞剑] 当前过载过高，无法触发本命爆发。", overloadMessage);
        assertEquals("[本命飞剑] 本命缔结状态异常，无法执行当前操作。", illegalStateMessage);
        assertEquals(
            TEST_MAGIC_4,
            new HashSet<>(
                List.of(
                    cooldownMessage,
                    resourceMessage,
                    overloadMessage,
                    illegalStateMessage
                )
            ).size()
        );
    }

    @Test
    void executeRoutesResonanceAndBurstThroughControllerChannel() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object executor = api.newExecutorProxy(
            api.newBondSuccessResult("QUERY", "stable-query"),
            api.newBondSuccessResult("RITUAL_BIND", "stable-bind"),
            api.newBondSuccessResult("ACTIVE_UNBIND", "stable-unbind"),
            api.newBondSuccessResult("FORCED_UNBIND", "stable-forced"),
            api.newControllerSuccessResult("RESONANCE_SWITCH", "stable-resonance", "spirit", 0L),
            api.newControllerFailureResult(
                "BURST_ATTEMPT",
                "BURST_COOLDOWN_ACTIVE",
                "stable-burst",
                "spirit",
                1280L
            )
        );

        final Object resonanceFeedback = api.execute("SWITCH_RESONANCE", executor);
        final Object burstFeedback = api.execute("BURST_ATTEMPT", executor);

        assertEquals("spirit", api.controllerResonanceType(resonanceFeedback));
        assertEquals(
            "BURST_COOLDOWN_ACTIVE",
            api.controllerFailureReason(burstFeedback)
        );
        assertEquals(1, api.executorCallCount(executor, "switchResonance"));
        assertEquals(1, api.executorCallCount(executor, "attemptBurst"));
        assertEquals(
            "[本命飞剑] 本命爆发仍在冷却中，请稍后再试。",
            api.feedbackMessage(burstFeedback)
        );
    }

    @Test
    void burstFailureFeedbackDeliveryThrottleSuppressesImmediateRepeats() {
        final Map<String, Long> cooldowns = new HashMap<>();

        assertTrue(
            BenmingFeedbackDeliveryThrottleLogic.shouldSendWithCooldowns(
                BURST_COOLDOWN_DEDUPE_KEY,
                TEST_BASE_TICK,
                cooldowns
            )
        );
        assertFalse(
            BenmingFeedbackDeliveryThrottleLogic.shouldSendWithCooldowns(
                BURST_COOLDOWN_DEDUPE_KEY,
                TEST_BASE_TICK,
                cooldowns
            )
        );
        assertTrue(
            BenmingFeedbackDeliveryThrottleLogic.shouldSendWithCooldowns(
                BURST_RESOURCE_DEDUPE_KEY,
                TEST_BASE_TICK,
                cooldowns
            )
        );
        assertTrue(
            BenmingFeedbackDeliveryThrottleLogic.shouldSendWithCooldowns(
                BURST_COOLDOWN_DEDUPE_KEY,
                TEST_BASE_TICK + BenmingFeedbackDeliveryThrottleLogic.cooldownWindowTicks(),
                cooldowns
            )
        );
    }

    @Test
    void ritualFeedbackBypassesBurstThrottleAndKeepsConfirmationReadable()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Method ritualBondFactory = api.actionFeedbackClass.getDeclaredMethod(
            "bond",
            api.bondResultClass
        );
        ritualBondFactory.setAccessible(true);
        final Object ritualSuccessFeedback = ritualBondFactory.invoke(
            null,
            api.newBondSuccessResult("RITUAL_BIND", "stable-bind")
        );
        final Object ritualFailureFeedback = ritualBondFactory.invoke(
            null,
            api.newBondFailureResult(
                "RITUAL_BIND",
                "RITUAL_RESOURCES_INSUFFICIENT",
                "stable-bind"
            )
        );
        final String ritualSuccessMessage = api.feedbackMessage(ritualSuccessFeedback);
        final String ritualFailureMessage = api.feedbackMessage(ritualFailureFeedback);

        assertEquals(
            "[本命飞剑] 仪式缔结成功：stable-bind",
            ritualSuccessMessage
        );
        assertEquals(
            "[本命飞剑] 仪式资源不足，无法完成本命缔结。",
            ritualFailureMessage
        );
    }

    @Test
    void firstGuideDeliveryHooksIntoSuccessfulBindAndResonanceBehavior()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object bindFeedback = api.execute(
            "RITUAL_BIND",
            api.newExecutorProxy(
                api.newBondSuccessResult("QUERY", "stable-query"),
                api.newBondSuccessResult("RITUAL_BIND", "stable-bind"),
                api.newBondSuccessResult("ACTIVE_UNBIND", "stable-unbind"),
                api.newBondSuccessResult("FORCED_UNBIND", "stable-forced"),
                api.newControllerSuccessResult(
                    "RESONANCE_SWITCH",
                    "stable-resonance",
                    "defense",
                    0L
                ),
                api.newControllerSuccessResult(
                    "BURST_ATTEMPT",
                    "stable-burst",
                    "spirit",
                    1240L
                )
            )
        );
        final Object resonanceFeedback = api.execute(
            "SWITCH_RESONANCE",
            api.newExecutorProxy(
                api.newBondSuccessResult("QUERY", "stable-query"),
                api.newBondSuccessResult("RITUAL_BIND", "stable-bind"),
                api.newBondSuccessResult("ACTIVE_UNBIND", "stable-unbind"),
                api.newBondSuccessResult("FORCED_UNBIND", "stable-forced"),
                api.newControllerSuccessResult(
                    "RESONANCE_SWITCH",
                    "stable-resonance",
                    "defense",
                    0L
                ),
                api.newControllerSuccessResult(
                    "BURST_ATTEMPT",
                    "stable-burst",
                    "spirit",
                    1240L
                )
            )
        );

        assertEquals(
            GUIDE_AFTER_BOND_KEY,
            api.consumeFirstGuideTopic(bindFeedback, new HashSet<>())
        );
        assertEquals(
            GUIDE_RESONANCE_FIRST_CHOICE_KEY,
            api.consumeFirstGuideTopic(resonanceFeedback, new HashSet<>())
        );
    }

    @Test
    void firstGuideDeliveryRoutesBondFailureAndOverloadWarningToHelp()
        throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Set<String> seenTopics = new HashSet<>();
        final Object bondFailureFeedback = api.execute(
            "RITUAL_BIND",
            api.newExecutorProxy(
                api.newBondSuccessResult("QUERY", "stable-query"),
                api.newBondFailureResult(
                    "RITUAL_BIND",
                    "TARGET_NOT_BOUND_TO_PLAYER",
                    "unstable-bind"
                ),
                api.newBondSuccessResult("ACTIVE_UNBIND", "stable-unbind"),
                api.newBondSuccessResult("FORCED_UNBIND", "stable-forced"),
                api.newControllerSuccessResult(
                    "RESONANCE_SWITCH",
                    "stable-resonance",
                    "defense",
                    0L
                ),
                api.newControllerSuccessResult(
                    "BURST_ATTEMPT",
                    "stable-burst",
                    "spirit",
                    1240L
                )
            )
        );
        final Object overloadWarningFeedback = api.execute(
            "BURST_ATTEMPT",
            api.newExecutorProxy(
                api.newBondSuccessResult("QUERY", "stable-query"),
                api.newBondSuccessResult("RITUAL_BIND", "stable-bind"),
                api.newBondSuccessResult("ACTIVE_UNBIND", "stable-unbind"),
                api.newBondSuccessResult("FORCED_UNBIND", "stable-forced"),
                api.newControllerSuccessResult(
                    "RESONANCE_SWITCH",
                    "stable-resonance",
                    "defense",
                    0L
                ),
                api.newControllerFailureResult(
                    "BURST_ATTEMPT",
                    "BURST_OVERLOAD_BLOCKED",
                    "stable-burst",
                    "spirit",
                    1240L
                )
            )
        );

        final String bondFailureTopic = api.consumeFirstGuideTopic(
            bondFailureFeedback,
            seenTopics
        );
        final String bondFailureRepeat = api.consumeFirstGuideTopic(
            bondFailureFeedback,
            seenTopics
        );
        final String overloadWarningTopic = api.consumeFirstGuideTopic(
            overloadWarningFeedback,
            seenTopics
        );
        final String overloadWarningRepeat = api.consumeFirstGuideTopic(
            overloadWarningFeedback,
            seenTopics
        );

        assertEquals(GUIDE_BOND_FAIL_NEXT_STEP_KEY, bondFailureTopic, "bond=" + bondFailureTopic);
        assertEquals(null, bondFailureRepeat, "bondRepeat=" + bondFailureRepeat);
        assertEquals(
            GUIDE_OVERLOAD_FIRST_WARNING_KEY,
            overloadWarningTopic,
            "overload=" + overloadWarningTopic
        );
        assertEquals(null, overloadWarningRepeat, "overloadRepeat=" + overloadWarningRepeat);
    }

    @Test
    void firstGuideDeliveryOnlyEmitsEachTopicOncePerSeenState() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Set<String> seenTopics = new HashSet<>();
        final Object bindFeedback = api.execute(
            "RITUAL_BIND",
            api.newExecutorProxy(
                api.newBondSuccessResult("QUERY", "stable-query"),
                api.newBondSuccessResult("RITUAL_BIND", "stable-bind"),
                api.newBondSuccessResult("ACTIVE_UNBIND", "stable-unbind"),
                api.newBondSuccessResult("FORCED_UNBIND", "stable-forced"),
                api.newControllerSuccessResult(
                    "RESONANCE_SWITCH",
                    "stable-resonance",
                    "defense",
                    0L
                ),
                api.newControllerSuccessResult(
                    "BURST_ATTEMPT",
                    "stable-burst",
                    "spirit",
                    1240L
                )
            )
        );
        final Object burstFailureFeedback = api.execute(
            "BURST_ATTEMPT",
            api.newExecutorProxy(
                api.newBondSuccessResult("QUERY", "stable-query"),
                api.newBondSuccessResult("RITUAL_BIND", "stable-bind"),
                api.newBondSuccessResult("ACTIVE_UNBIND", "stable-unbind"),
                api.newBondSuccessResult("FORCED_UNBIND", "stable-forced"),
                api.newControllerSuccessResult(
                    "RESONANCE_SWITCH",
                    "stable-resonance",
                    "defense",
                    0L
                ),
                api.newControllerFailureResult(
                    "BURST_ATTEMPT",
                    "BURST_OVERLOAD_BLOCKED",
                    "stable-burst",
                    "spirit",
                    1240L
                )
            )
        );

        assertEquals(
            GUIDE_AFTER_BOND_KEY,
            api.consumeFirstGuideTopic(bindFeedback, seenTopics)
        );
        assertEquals(null, api.consumeFirstGuideTopic(bindFeedback, seenTopics));
        assertEquals(
            GUIDE_OVERLOAD_FIRST_WARNING_KEY,
            api.consumeFirstGuideTopic(burstFailureFeedback, seenTopics)
        );
        assertEquals(null, api.consumeFirstGuideTopic(burstFailureFeedback, seenTopics));
    }


    @Test
    void feedbackTreatsInvalidRouteAsExplicitPlayerFacingFailure() throws Exception {
        final RuntimeApi api = RuntimeApi.create();
        final Object invalidBondFeedback = api.execute(null, null);
        final Object invalidControllerFeedback = api.execute("BURST_ATTEMPT", null);

        assertEquals("QUERY", api.bondBranch(invalidBondFeedback));
        assertEquals("INVALID_REQUEST", api.bondFailureReason(invalidBondFeedback));
        assertEquals("[本命飞剑] 未能查询到有效的本命目标。", api.feedbackMessage(invalidBondFeedback));
        assertEquals(
            "INVALID_REQUEST",
            api.controllerFailureReason(invalidControllerFeedback)
        );
        assertEquals(
            "[本命飞剑] 未能触发本命爆发尝试。",
            api.feedbackMessage(invalidControllerFeedback)
        );
    }

    @Test
    void resolveNextResonanceTypeCyclesDeterministically() throws Exception {
        final RuntimeApi api = RuntimeApi.create();

        assertEquals("DEFENSE", api.resolveNextResonanceType("offense"));
        assertEquals("DEFENSE", api.resolveNextResonanceType(""));
    }

    @Test
    void networkingRegistersDedicatedBenmingPayload() throws Exception {
        final String source = java.nio.file.Files.readString(
            java.nio.file.Path.of(
                "src/main/java/com/Kizunad/guzhenrenext/network/GuzhenrenExtNetworking.java"
            )
        );

        assertTrue(source.contains("ServerboundBenmingSwordActionPayload.TYPE"));
        assertTrue(source.contains("ServerboundBenmingSwordActionPayload.STREAM_CODEC"));
        assertTrue(source.contains("ServerboundBenmingSwordActionPayload::handle"));
    }

    private static final class RuntimeApi {

        private static Path cachedMinecraftJarPath;

        private final Class<?> payloadClass;
        private final Class<?> actionEnumClass;
        private final Class<?> actionFeedbackClass;
        private final Class<?> routingHelperClass;
        private final Class<?> executorInterfaceClass;
        private final Class<?> bondResultClass;
        private final Class<?> bondBranchEnumClass;
        private final Class<?> bondFailureReasonEnumClass;
        private final Class<?> controllerResultClass;
        private final Class<?> controllerActionEnumClass;
        private final Class<?> controllerFailureReasonEnumClass;
        private final Class<?> firstGuideDeliveryClass;
        private final Class<?> resonanceStateViewClass;
        private final Class<?> resonanceTypeClass;
        private final Method executeMethod;
        private final Method consumeFirstGuideTopicMethod;
        private final Method feedbackMethod;
        private final Method resolveNextResonanceTypeMethod;
        private final Method controllerSuccessMethod;
        private final Method controllerFailureMethod;

        private RuntimeApi(final RuntimeApiDeps deps) throws ReflectiveOperationException {
            this.payloadClass = deps.payloadClass();
            this.actionEnumClass = deps.actionEnumClass();
            this.actionFeedbackClass = deps.actionFeedbackClass();
            this.routingHelperClass = deps.routingHelperClass();
            this.executorInterfaceClass = deps.executorInterfaceClass();
            this.bondResultClass = deps.bondResultClass();
            this.bondBranchEnumClass = deps.bondBranchEnumClass();
            this.bondFailureReasonEnumClass = deps.bondFailureReasonEnumClass();
            this.controllerResultClass = deps.controllerResultClass();
            this.controllerActionEnumClass = deps.controllerActionEnumClass();
            this.controllerFailureReasonEnumClass = deps.controllerFailureReasonEnumClass();
            this.firstGuideDeliveryClass = deps.firstGuideDeliveryClass();
            this.resonanceStateViewClass = deps.resonanceStateViewClass();
            this.resonanceTypeClass = deps.resonanceTypeClass();
            this.executeMethod = routingHelperClass.getDeclaredMethod(
                "execute",
                actionEnumClass,
                executorInterfaceClass
            );
            this.consumeFirstGuideTopicMethod = firstGuideDeliveryClass.getDeclaredMethod(
                "consumeTopic",
                actionFeedbackClass,
                Set.class
            );
            this.feedbackMethod = routingHelperClass.getDeclaredMethod(
                "feedback",
                actionFeedbackClass
            );
            this.resolveNextResonanceTypeMethod = routingHelperClass.getDeclaredMethod(
                "resolveNextResonanceType",
                resonanceStateViewClass
            );
            this.controllerSuccessMethod = controllerResultClass.getMethod(
                "success",
                controllerActionEnumClass,
                String.class,
                String.class,
                long.class
            );
            this.controllerFailureMethod = controllerResultClass.getMethod(
                "failure",
                controllerActionEnumClass,
                controllerFailureReasonEnumClass,
                String.class,
                String.class,
                long.class
            );
            this.controllerSuccessMethod.setAccessible(true);
            this.controllerFailureMethod.setAccessible(true);
            this.executeMethod.setAccessible(true);
            this.consumeFirstGuideTopicMethod.setAccessible(true);
            this.feedbackMethod.setAccessible(true);
            this.resolveNextResonanceTypeMethod.setAccessible(true);
        }

        static RuntimeApi create() throws Exception {
            final URLClassLoader loader = buildRuntimeClassLoader();
            final Class<?> payloadClass = Class.forName(PAYLOAD_CLASS_NAME, true, loader);
            final String resonanceStateViewClassName = PAYLOAD_CLASS_NAME.replace(
                "ServerboundBenmingSwordActionPayload",
                "BenmingActionRoutingHelper$ResonanceStateView"
            );
            return new RuntimeApi(
                new RuntimeApiDeps(
                    payloadClass,
                    Class.forName("com.Kizunad.guzhenrenext.network.BenmingAction", true, loader),
                    Class.forName("com.Kizunad.guzhenrenext.network.BenmingActionFeedback", true, loader),
                    Class.forName("com.Kizunad.guzhenrenext.network.BenmingActionRoutingHelper", true, loader),
                    Class.forName("com.Kizunad.guzhenrenext.network.BenmingActionExecutor", true, loader),
                    Class.forName(BOND_SERVICE_CLASS_NAME + "$Result", true, loader),
                    Class.forName(BOND_SERVICE_CLASS_NAME + "$ResultBranch", true, loader),
                    Class.forName(BOND_SERVICE_CLASS_NAME + "$FailureReason", true, loader),
                    Class.forName(
                        CONTROLLER_CLASS_NAME + "$BenmingControllerActionResult",
                        true,
                        loader
                    ),
                    Class.forName(
                        CONTROLLER_CLASS_NAME + "$BenmingControllerAction",
                        true,
                        loader
                    ),
                    Class.forName(
                        CONTROLLER_CLASS_NAME + "$BenmingControllerFailureReason",
                        true,
                        loader
                    ),
                    Class.forName(
                        "com.Kizunad.guzhenrenext.network.BenmingFirstGuideDelivery",
                        true,
                        loader
                    ),
                    Class.forName(resonanceStateViewClassName, true, loader),
                    Class.forName(
                        "com.Kizunad.guzhenrenext.kongqiao.flyingsword.resonance.FlyingSwordResonanceType",
                        true,
                        loader
                    )
                )
            );
        }

        private record RuntimeApiDeps(
            Class<?> payloadClass,
            Class<?> actionEnumClass,
            Class<?> actionFeedbackClass,
            Class<?> routingHelperClass,
            Class<?> executorInterfaceClass,
            Class<?> bondResultClass,
            Class<?> bondBranchEnumClass,
            Class<?> bondFailureReasonEnumClass,
            Class<?> controllerResultClass,
            Class<?> controllerActionEnumClass,
            Class<?> controllerFailureReasonEnumClass,
            Class<?> firstGuideDeliveryClass,
            Class<?> resonanceStateViewClass,
            Class<?> resonanceTypeClass
        ) {}

        Object execute(final String actionName, final Object executor) throws Exception {
            final Object action = actionName == null
                ? null
                : enumConstant(actionEnumClass, actionName);
            return executeMethod.invoke(null, action, executor);
        }

        String consumeFirstGuideTopic(
            final Object actionFeedback,
            final Set<String> seenTopics
        ) throws Exception {
            return (String) consumeFirstGuideTopicMethod.invoke(
                null,
                actionFeedback,
                seenTopics
            );
        }

        Object newBondSuccessResult(final String branchName, final String stableSwordId)
            throws Exception {
            final Method successMethod = bondResultClass.getMethod(
                "success",
                bondBranchEnumClass,
                String.class
            );
            successMethod.setAccessible(true);
            return successMethod.invoke(
                null,
                enumConstant(bondBranchEnumClass, branchName),
                stableSwordId
            );
        }

        Object newBondFailureResult(
            final String branchName,
            final String failureReasonName,
            final String stableSwordId
        ) throws Exception {
            final Method failureMethod = bondResultClass.getMethod(
                "failure",
                bondBranchEnumClass,
                bondFailureReasonEnumClass,
                String.class
            );
            failureMethod.setAccessible(true);
            return failureMethod.invoke(
                null,
                enumConstant(bondBranchEnumClass, branchName),
                enumConstant(bondFailureReasonEnumClass, failureReasonName),
                stableSwordId
            );
        }

        Object newExecutorProxy(
            final Object queryResult,
            final Object bindResult,
            final Object activeUnbindResult,
            final Object forcedUnbindResult,
            final Object resonanceResult,
            final Object burstResult
        ) {
            final ExecutorState state = new ExecutorState(
                queryResult,
                bindResult,
                activeUnbindResult,
                forcedUnbindResult,
                resonanceResult,
                burstResult
            );
            return Proxy.newProxyInstance(
                payloadClass.getClassLoader(),
                new Class<?>[] {executorInterfaceClass},
                new ExecutorHandler(state)
            );
        }

        int executorCallCount(final Object executor, final String methodName) {
            final InvocationHandler handler = Proxy.getInvocationHandler(executor);
            return ((ExecutorHandler) handler).state.callCount(methodName);
        }

        Object newControllerSuccessResult(
            final String actionName,
            final String stableSwordId,
            final String resonanceType,
            final long burstCooldownUntilTick
        ) throws Exception {
            return controllerSuccessMethod.invoke(
                null,
                enumConstant(controllerActionEnumClass, actionName),
                stableSwordId,
                resonanceType,
                burstCooldownUntilTick
            );
        }

        Object newControllerFailureResult(
            final String actionName,
            final String failureReasonName,
            final String stableSwordId,
            final String resonanceType,
            final long burstCooldownUntilTick
        ) throws Exception {
            return controllerFailureMethod.invoke(
                null,
                enumConstant(controllerActionEnumClass, actionName),
                enumConstant(controllerFailureReasonEnumClass, failureReasonName),
                stableSwordId,
                resonanceType,
                burstCooldownUntilTick
            );
        }

        String feedbackForBondResult(final Object bondResult) throws Exception {
            final Method bondFactoryMethod = actionFeedbackClass.getDeclaredMethod(
                "bond",
                bondResultClass
            );
            bondFactoryMethod.setAccessible(true);
            final Object bondFeedback = bondFactoryMethod.invoke(null, bondResult);
            return (String) feedbackMethod.invoke(null, bondFeedback);
        }

        String feedbackForControllerResult(final Object controllerResult) throws Exception {
            final Method controllerFactoryMethod = actionFeedbackClass.getDeclaredMethod(
                "controller",
                controllerResultClass
            );
            controllerFactoryMethod.setAccessible(true);
            final Object controllerFeedback = controllerFactoryMethod.invoke(
                null,
                controllerResult
            );
            return (String) feedbackMethod.invoke(null, controllerFeedback);
        }

        String feedbackMessage(final Object actionFeedback) throws Exception {
            final Method messageMethod = actionFeedbackClass.getDeclaredMethod("message");
            messageMethod.setAccessible(true);
            return (String) messageMethod.invoke(actionFeedback);
        }

        String bondBranch(final Object actionFeedback) throws Exception {
            final Method bondResultAccessor = actionFeedbackClass.getDeclaredMethod(
                "bondResult"
            );
            bondResultAccessor.setAccessible(true);
            final Object bondResult = bondResultAccessor.invoke(actionFeedback);
            final Method branchMethod = bondResultClass.getDeclaredMethod("branch");
            branchMethod.setAccessible(true);
            return ((Enum<?>) branchMethod.invoke(bondResult)).name();
        }

        String bondFailureReason(final Object actionFeedback) throws Exception {
            final Method bondResultAccessor = actionFeedbackClass.getDeclaredMethod(
                "bondResult"
            );
            bondResultAccessor.setAccessible(true);
            final Object bondResult = bondResultAccessor.invoke(actionFeedback);
            final Method failureReasonMethod = bondResultClass.getDeclaredMethod(
                "failureReason"
            );
            failureReasonMethod.setAccessible(true);
            return ((Enum<?>) failureReasonMethod.invoke(bondResult)).name();
        }

        String controllerResonanceType(final Object actionFeedback) throws Exception {
            final Method controllerResultAccessor = actionFeedbackClass.getDeclaredMethod(
                "controllerResult"
            );
            controllerResultAccessor.setAccessible(true);
            final Object controllerResult = controllerResultAccessor.invoke(actionFeedback);
            final Method resonanceTypeAccessor = controllerResultClass.getDeclaredMethod(
                "resonanceType"
            );
            resonanceTypeAccessor.setAccessible(true);
            return (String) resonanceTypeAccessor.invoke(controllerResult);
        }

        String controllerFailureReason(final Object actionFeedback) throws Exception {
            final Method controllerResultAccessor = actionFeedbackClass.getDeclaredMethod(
                "controllerResult"
            );
            controllerResultAccessor.setAccessible(true);
            final Object controllerResult = controllerResultAccessor.invoke(actionFeedback);
            final Method failureReasonAccessor = controllerResultClass.getDeclaredMethod(
                "failureReason"
            );
            failureReasonAccessor.setAccessible(true);
            return ((Enum<?>) failureReasonAccessor.invoke(controllerResult)).name();
        }

        String resolveNextResonanceType(final String rawResonanceType) throws Exception {
            final Object stateView = Proxy.newProxyInstance(
                payloadClass.getClassLoader(),
                new Class<?>[] {resonanceStateViewClass},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "toString" -> "ResonanceStateViewProxy";
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == (args == null ? null : args[0]);
                            default -> throw new UnsupportedOperationException(method.getName());
                        };
                    }
                    return rawResonanceType;
                }
            );
            final Object resonanceType = resolveNextResonanceTypeMethod.invoke(null, stateView);
            return ((Enum<?>) resonanceType).name();
        }

        private static Object enumConstant(final Class<?> enumClass, final String name)
            throws ReflectiveOperationException {
            final Method valueOfMethod = Enum.class.getMethod("valueOf", Class.class, String.class);
            return valueOfMethod.invoke(null, enumClass.asSubclass(Enum.class), name);
        }

        private static URLClassLoader buildRuntimeClassLoader() throws IOException {
            final List<URL> urls = new ArrayList<>();
            urls.add(MAIN_CLASSES.toAbsolutePath().toUri().toURL());
            if (MAIN_RESOURCES.toAbsolutePath().toFile().exists()) {
                urls.add(MAIN_RESOURCES.toAbsolutePath().toUri().toURL());
            }

            final Properties props = new Properties();
            try (InputStream input = Files.newInputStream(ARTIFACT_MANIFEST.toAbsolutePath())) {
                props.load(input);
            }
            for (String key : props.stringPropertyNames()) {
                final String jarPath = props.getProperty(key);
                if (jarPath == null || jarPath.isBlank()) {
                    continue;
                }
                final Path absoluteJarPath = Path.of(jarPath).toAbsolutePath();
                if (absoluteJarPath.toFile().exists()) {
                    urls.add(absoluteJarPath.toUri().toURL());
                }
            }
            urls.add(resolveMinecraftRuntimeJar().toUri().toURL());
            return new URLClassLoader(
                urls.toArray(new URL[0]),
                ClassLoader.getPlatformClassLoader()
            );
        }

        private static synchronized Path resolveMinecraftRuntimeJar() throws IOException {
            if (cachedMinecraftJarPath != null && cachedMinecraftJarPath.toFile().exists()) {
                return cachedMinecraftJarPath;
            }

            final List<Path> searchRoots = List.of(
                Path.of(
                    System.getProperty("user.home"),
                    ".gradle",
                    "caches",
                    "neoformruntime",
                    "intermediate_results"
                ),
                Path.of(
                    System.getProperty("user.home"),
                    ".gradle",
                    "caches",
                    "fabric-loom",
                    "minecraftMaven"
                )
            );
            for (Path root : searchRoots) {
                final Path matched = findJarContainingResource(root, NBT_TAG_CLASS_RESOURCE);
                if (matched != null) {
                    cachedMinecraftJarPath = matched;
                    return matched;
                }
            }
            throw new IOException("未找到包含 net.minecraft.nbt.Tag 的运行时 Jar");
        }

        private static Path findJarContainingResource(final Path root, final String resource)
            throws IOException {
            if (root == null || !root.toFile().exists()) {
                return null;
            }
            try (var stream = Files.walk(root, TEST_MAGIC_6)) {
                final List<Path> candidates = stream
                    .filter(path -> path.toString().endsWith(".jar"))
                    .toList();
                for (Path candidate : candidates) {
                    if (jarContainsResource(candidate, resource)) {
                        return candidate;
                    }
                }
            }
            return null;
        }

        private static boolean jarContainsResource(final Path jarPath, final String resource) {
            try (var jar = new java.util.jar.JarFile(jarPath.toFile())) {
                return jar.getEntry(resource) != null
                    || jar.getEntry(RESONANCE_TYPE_CLASS_RESOURCE) != null;
            } catch (IOException ignored) {
                return false;
            }
        }
    }

    private static final class ExecutorState {

        private final Object queryResult;
        private final Object bindResult;
        private final Object activeUnbindResult;
        private final Object forcedUnbindResult;
        private final Object resonanceResult;
        private final Object burstResult;
        private int queryCalls;
        private int bindCalls;
        private int activeUnbindCalls;
        private int forcedUnbindCalls;
        private int resonanceCalls;
        private int burstCalls;

        private ExecutorState(
            final Object queryResult,
            final Object bindResult,
            final Object activeUnbindResult,
            final Object forcedUnbindResult,
            final Object resonanceResult,
            final Object burstResult
        ) {
            this.queryResult = queryResult;
            this.bindResult = bindResult;
            this.activeUnbindResult = activeUnbindResult;
            this.forcedUnbindResult = forcedUnbindResult;
            this.resonanceResult = resonanceResult;
            this.burstResult = burstResult;
        }

        private int callCount(final String methodName) {
            return switch (methodName) {
                case "query" -> queryCalls;
                case "ritualBind" -> bindCalls;
                case "activeUnbind" -> activeUnbindCalls;
                case "forcedUnbind" -> forcedUnbindCalls;
                case "switchResonance" -> resonanceCalls;
                case "attemptBurst" -> burstCalls;
                default -> 0;
            };
        }
    }

    private static final class ExecutorHandler implements InvocationHandler {

        private final ExecutorState state;

        private ExecutorHandler(final ExecutorState state) {
            this.state = state;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "BenmingActionExecutorProxy";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == (args == null ? null : args[0]);
                    default -> throw new UnsupportedOperationException(method.getName());
                };
            }
            return switch (method.getName()) {
                case "query" -> {
                    state.queryCalls++;
                    yield state.queryResult;
                }
                case "ritualBind" -> {
                    state.bindCalls++;
                    yield state.bindResult;
                }
                case "activeUnbind" -> {
                    state.activeUnbindCalls++;
                    yield state.activeUnbindResult;
                }
                case "forcedUnbind" -> {
                    state.forcedUnbindCalls++;
                    yield state.forcedUnbindResult;
                }
                case "switchResonance" -> {
                    state.resonanceCalls++;
                    yield state.resonanceResult;
                }
                case "attemptBurst" -> {
                    state.burstCalls++;
                    yield state.burstResult;
                }
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }
    }
}
