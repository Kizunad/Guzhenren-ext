package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.testsupport.Task4RuntimeHarness;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class KongqiaoPressureProjectionServiceTests {

    private static final double DELTA = 1.0E-6D;

    @Test
    void assembleProjectionUsesCurrentTruthfulSubsetAndRoundTripsThroughTag() throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object data = api.newKongqiaoData();
        final Object stabilityState = api.getStabilityState(data);
        api.setBurstPressure(stabilityState, 8.5D);
        api.setFatigueDebt(stabilityState, 2.25D);
        api.setOverloadTier(stabilityState, 4);
        api.setSealedSlots(stabilityState, Set.of(0, 6));
        api.setForcedDisabledUsageIds(
            stabilityState,
            Set.of("passive.core", "passive.optional")
        );

        final Object projection = api.assembleProjection(data);

        assertEquals(10.75D, api.projectionDouble(projection, "totalPressure"), DELTA);
        assertEquals(
            10.75D,
            api.projectionDouble(projection, "effectivePressure"),
            DELTA
        );
        assertEquals(0.0D, api.projectionDouble(projection, "pressureCap"), DELTA);
        assertEquals(
            0.0D,
            api.projectionDouble(projection, "residentPressure"),
            DELTA
        );
        assertEquals(0.0D, api.projectionDouble(projection, "passivePressure"), DELTA);
        assertEquals(
            0.0D,
            api.projectionDouble(projection, "wheelReservePressure"),
            DELTA
        );
        assertEquals(8.5D, api.projectionDouble(projection, "burstPressure"), DELTA);
        assertEquals(2.25D, api.projectionDouble(projection, "fatigueDebt"), DELTA);
        assertEquals(4, api.projectionInt(projection, "overloadTier"));
        assertEquals("", api.projectionString(projection, "blockedReason"));
        assertEquals(2, api.projectionInt(projection, "sealedSlotCount"));
        assertEquals(2, api.projectionInt(projection, "forcedDisabledCount"));

        // Task 3 验证：容量字段应为空（因为 assembleProjection(data) 不含 entity）
        assertEquals("", api.projectionString(projection, "aptitudeTier"));
        assertEquals(0, api.projectionInt(projection, "apertureRank"));
        assertEquals(0, api.projectionInt(projection, "baseRows"));
        assertEquals(0, api.projectionInt(projection, "bonusRows"));
        assertEquals(0, api.projectionInt(projection, "totalRows"));

        final Object projectionRoundTrip = api.projectionFromTag(
            api.projectionToTag(projection)
        );
        assertTrue(api.objectsEqual(projection, projectionRoundTrip));
    }

    /**
     * 验证 Task 3 容量字段的创建和往返序列化。
     */
    @Test
    void capacityFieldsRoundTripThroughTag() throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();

        // 直接构造带容量字段的投影
        final Object projection = api.newProjection(
            // 压力字段
            10.0D,   // totalPressure
            10.0D,   // effectivePressure
            30.0D,   // pressureCap
            2.0D,    // residentPressure
            3.0D,    // passivePressure
            1.0D,    // wheelReservePressure
            2.5D,    // burstPressure
            1.5D,    // fatigueDebt
            2,        // overloadTier
            "overloaded", // blockedReason
            3,        // sealedSlotCount
            1,        // forcedDisabledCount
            // Task 3 容量字段
            "绝品",   // aptitudeTier
            5,        // apertureRank
            5,        // apertureStage
            5,        // baseRows
            4,        // bonusRows
            9         // totalRows
        );

        // 验证容量字段读取
        assertEquals("绝品", api.projectionString(projection, "aptitudeTier"));
        assertEquals(5, api.projectionInt(projection, "apertureRank"));
        assertEquals(5, api.projectionInt(projection, "apertureStage"));
        assertEquals(5, api.projectionInt(projection, "baseRows"));
        assertEquals(4, api.projectionInt(projection, "bonusRows"));
        assertEquals(9, api.projectionInt(projection, "totalRows"));

        // 验证往返序列化
        final Object projectionRoundTrip = api.projectionFromTag(
            api.projectionToTag(projection)
        );
        assertTrue(api.objectsEqual(projection, projectionRoundTrip));
    }
}
