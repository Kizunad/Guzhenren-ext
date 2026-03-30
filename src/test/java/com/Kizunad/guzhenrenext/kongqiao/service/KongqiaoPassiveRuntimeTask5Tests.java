package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.testsupport.Task4RuntimeHarness;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class KongqiaoPassiveRuntimeTask5Tests {

    private static final double DELTA = 1.0E-6D;

    @Test
    void overloadForceDisablesRuntimePassivesWithoutMutatingPreferenceState()
        throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final String alphaUsageId = "guzhenren:test_passive_alpha";
        final String betaUsageId = "guzhenren:test_passive_beta";
        final Object config = api.newTweakConfig();
        final Object actives = api.newActivePassives();
        api.addActivePassive(actives, alphaUsageId);
        api.addActivePassive(actives, betaUsageId);
        final Object data = api.newKongqiaoData();
        final Object stabilityState = api.getStabilityState(data);
        final Task4RuntimeHarness.RuntimeApi.PassiveEffectSpy alphaSpy =
            api.newPassiveEffectSpy(alphaUsageId);
        final Task4RuntimeHarness.RuntimeApi.PassiveEffectSpy betaSpy =
            api.newPassiveEffectSpy(betaUsageId);

        final Object alphaUsage = api.newNianTouUsage(
            alphaUsageId,
            "测试被动甲",
            "Task 5 测试用途",
            "Task 5 测试用途",
            600,
            200,
            Map.of()
        );
        final Object betaUsage = api.newNianTouUsage(
            betaUsageId,
            "测试被动乙",
            "Task 5 测试用途",
            "Task 5 测试用途",
            800,
            350,
            Map.of()
        );
        final Object snapshot = api.evaluatePassiveRuntimeSnapshot(
            List.of(
                api.newPassiveRuntimeCandidate(
                    alphaUsageId,
                    api.computePassivePressureCore(alphaUsage)
                ),
                api.newPassiveRuntimeCandidate(
                    betaUsageId,
                    api.computePassivePressureCore(betaUsage)
                )
            ),
            api.newCapacityProfile("CANCI", 0, 0, 1, 0, 1, 0.0D),
            0.0D,
            0.0D
        );

        api.syncPassiveRuntimeState(actives, stabilityState, snapshot);
        assertTrue(
            api.runPassiveUsageIfAllowed(
                actives,
                alphaUsage,
                alphaSpy.proxy(),
                snapshot,
                true
            )
        );
        assertFalse(
            api.runPassiveUsageIfAllowed(
                actives,
                betaUsage,
                betaSpy.proxy(),
                snapshot,
                true
            )
        );

        assertTrue(api.isPassiveEnabled(config, alphaUsageId));
        assertTrue(api.isPassiveEnabled(config, betaUsageId));
        assertEquals(18.0D, api.passiveRuntimeSnapshotDouble(snapshot, "pressureCap"), DELTA);
        assertEquals(20.0D, api.passiveRuntimeSnapshotDouble(snapshot, "passivePressure"), DELTA);
        assertEquals(20.0D, api.passiveRuntimeSnapshotDouble(snapshot, "effectivePressure"), DELTA);
        assertEquals(2, api.passiveRuntimeSnapshotInt(snapshot, "overloadTier"));
        assertEquals(
            "passive_overload",
            api.passiveRuntimeSnapshotString(snapshot, "blockedReason")
        );
        assertEquals(
            Set.of(betaUsageId),
            api.passiveRuntimeSnapshotSet(snapshot, "forcedDisabledUsageIds")
        );
        assertEquals(
            Set.of(alphaUsageId),
            api.passiveRuntimeSnapshotSet(snapshot, "runnableUsageIds")
        );
        assertTrue(api.isActivePassive(actives, alphaUsageId));
        assertFalse(api.isActivePassive(actives, betaUsageId));
        assertEquals(1, alphaSpy.onTickCalls());
        assertEquals(1, alphaSpy.onSecondCalls());
        assertEquals(0, alphaSpy.onUnequipCalls());
        assertEquals(0, betaSpy.onTickCalls());
        assertEquals(0, betaSpy.onSecondCalls());
        assertEquals(1, betaSpy.onUnequipCalls());
        assertEquals(Set.of(betaUsageId), api.getForcedDisabledUsageIds(stabilityState));
        assertEquals(2, api.getOverloadTier(stabilityState));
    }

    @Test
    void stablePressureAllowsPassiveExecutionAndWritesNoForcedDisableMarkers()
        throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final String alphaUsageId = "guzhenren:test_passive_alpha";
        final String betaUsageId = "guzhenren:test_passive_beta";
        final Object config = api.newTweakConfig();
        final Object actives = api.newActivePassives();
        api.addActivePassive(actives, alphaUsageId);
        api.addActivePassive(actives, betaUsageId);
        final Object data = api.newKongqiaoData();
        final Object stabilityState = api.getStabilityState(data);
        final Task4RuntimeHarness.RuntimeApi.PassiveEffectSpy alphaSpy =
            api.newPassiveEffectSpy(alphaUsageId);
        final Task4RuntimeHarness.RuntimeApi.PassiveEffectSpy betaSpy =
            api.newPassiveEffectSpy(betaUsageId);

        final Object alphaUsage = api.newNianTouUsage(
            alphaUsageId,
            "测试被动甲",
            "Task 5 测试用途",
            "Task 5 测试用途",
            400,
            100,
            Map.of()
        );
        final Object betaUsage = api.newNianTouUsage(
            betaUsageId,
            "测试被动乙",
            "Task 5 测试用途",
            "Task 5 测试用途",
            600,
            150,
            Map.of()
        );
        final Object snapshot = api.evaluatePassiveRuntimeSnapshot(
            List.of(
                api.newPassiveRuntimeCandidate(
                    alphaUsageId,
                    api.computePassivePressureCore(alphaUsage)
                ),
                api.newPassiveRuntimeCandidate(
                    betaUsageId,
                    api.computePassivePressureCore(betaUsage)
                )
            ),
            api.newCapacityProfile("CANCI", 0, 0, 1, 0, 1, 0.0D),
            0.0D,
            0.0D
        );

        api.syncPassiveRuntimeState(actives, stabilityState, snapshot);
        assertTrue(
            api.runPassiveUsageIfAllowed(
                actives,
                alphaUsage,
                alphaSpy.proxy(),
                snapshot,
                true
            )
        );
        assertTrue(
            api.runPassiveUsageIfAllowed(
                actives,
                betaUsage,
                betaSpy.proxy(),
                snapshot,
                true
            )
        );

        assertTrue(api.isPassiveEnabled(config, alphaUsageId));
        assertTrue(api.isPassiveEnabled(config, betaUsageId));
        assertEquals(18.0D, api.passiveRuntimeSnapshotDouble(snapshot, "pressureCap"), DELTA);
        assertEquals(12.0D, api.passiveRuntimeSnapshotDouble(snapshot, "passivePressure"), DELTA);
        assertEquals(12.0D, api.passiveRuntimeSnapshotDouble(snapshot, "effectivePressure"), DELTA);
        assertEquals(0, api.passiveRuntimeSnapshotInt(snapshot, "overloadTier"));
        assertEquals("", api.passiveRuntimeSnapshotString(snapshot, "blockedReason"));
        assertEquals(Set.of(), api.passiveRuntimeSnapshotSet(snapshot, "forcedDisabledUsageIds"));
        assertEquals(
            Set.of(alphaUsageId, betaUsageId),
            api.passiveRuntimeSnapshotSet(snapshot, "runnableUsageIds")
        );
        assertTrue(api.isActivePassive(actives, alphaUsageId));
        assertTrue(api.isActivePassive(actives, betaUsageId));
        assertEquals(1, alphaSpy.onTickCalls());
        assertEquals(1, alphaSpy.onSecondCalls());
        assertEquals(0, alphaSpy.onUnequipCalls());
        assertEquals(1, betaSpy.onTickCalls());
        assertEquals(1, betaSpy.onSecondCalls());
        assertEquals(0, betaSpy.onUnequipCalls());
        assertEquals(Set.of(), api.getForcedDisabledUsageIds(stabilityState));
        assertEquals(0, api.getOverloadTier(stabilityState));
    }
}
