package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.testsupport.Task4RuntimeHarness;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class KongqiaoActivePressureTask7Tests {

    private static final double DELTA = 1.0E-6D;

    @Test
    void activeUsagePressureLimitRejectsBeforeEffectExecution() throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Task4RuntimeHarness.RuntimeApi.PassiveEffectSpy effectSpy =
            api.newPassiveEffectSpy("guzhenren:test_task7_active_usage");
        final Object usage = api.newNianTouUsage(
            "guzhenren:test_task7_active_usage",
            "Task7 主动用途",
            "Task7 主动用途描述",
            "Task7 主动用途信息",
            40,
            10,
            Map.of()
        );

        final Object result = api.activateResolvedUsageForTests(
            usage,
            effectSpy.proxy(),
            18.0D,
            18.0D
        );

        assertFalse(api.activationResultSuccess(result));
        assertEquals("PRESSURE_LIMIT", api.activationResultFailureReasonName(result));
        assertEquals(0, effectSpy.onActivateCalls());
    }

    @Test
    void shazhaoPressureLimitRejectsBeforeEffectExecution() throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object data = api.newKongqiaoData();
        final Object stabilityState = api.getStabilityState(data);
        api.setBurstPressure(stabilityState, 1.5D);
        final Task4RuntimeHarness.RuntimeApi.ShazhaoActiveEffectSpy effectSpy =
            api.newShazhaoActiveEffectSpy(
                "guzhenrenext:shazhao_active_task7_pressure_gate",
                true
            );
        final Object shazhaoData = api.newShazhaoData(
            "guzhenrenext:shazhao_active_task7_pressure_gate",
            "Task7 杀招",
            "Task7 杀招描述",
            "Task7 杀招信息",
            24,
            List.of("minecraft:apple"),
            Map.of()
        );

        final Object result = api.activateResolvedEffectForTests(
            shazhaoData,
            effectSpy.proxy(),
            stabilityState,
            15.0D,
            18.0D
        );

        assertFalse(api.activationResultSuccess(result));
        assertEquals("PRESSURE_LIMIT", api.activationResultFailureReasonName(result));
        assertEquals(0, effectSpy.onActivateCalls());
        assertEquals(1.5D, api.getBurstPressure(stabilityState), DELTA);
    }

    @Test
    void shazhaoSuccessWritesBurstPressureAfterActivation() throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object data = api.newKongqiaoData();
        final Object stabilityState = api.getStabilityState(data);
        api.setBurstPressure(stabilityState, 1.25D);
        final Task4RuntimeHarness.RuntimeApi.ShazhaoActiveEffectSpy effectSpy =
            api.newShazhaoActiveEffectSpy(
                "guzhenrenext:shazhao_active_task7_success",
                true
            );
        final Object shazhaoData = api.newShazhaoData(
            "guzhenrenext:shazhao_active_task7_success",
            "Task7 成功杀招",
            "Task7 成功杀招描述",
            "Task7 成功杀招信息",
            30,
            List.of("minecraft:apple"),
            Map.of()
        );

        final Object result = api.activateResolvedEffectForTests(
            shazhaoData,
            effectSpy.proxy(),
            stabilityState,
            10.0D,
            18.0D
        );

        assertTrue(api.activationResultSuccess(result));
        assertEquals("NONE", api.activationResultFailureReasonName(result));
        assertEquals(1, effectSpy.onActivateCalls());
        assertEquals(5.25D, api.getBurstPressure(stabilityState), DELTA);
    }
}
