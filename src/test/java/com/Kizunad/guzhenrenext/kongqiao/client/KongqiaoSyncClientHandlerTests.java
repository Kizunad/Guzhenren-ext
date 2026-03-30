package com.Kizunad.guzhenrenext.kongqiao.client;

import com.Kizunad.guzhenrenext.testsupport.Task4RuntimeHarness;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class KongqiaoSyncClientHandlerTests {

    private static final double DELTA = 1.0E-6D;

    @Test
    void applyAuthoritativeStateUpdatesRawAttachmentAndProjectionCache() throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object source = api.newKongqiaoData();
        final Object sourceStabilityState = api.getStabilityState(source);
        api.setBurstPressure(sourceStabilityState, 3.5D);
        api.setFatigueDebt(sourceStabilityState, 1.25D);
        api.setOverloadTier(sourceStabilityState, 2);
        api.setForcedDisabledUsageIds(
            sourceStabilityState,
            Set.of("usage.alpha")
        );
        api.setSealedSlots(sourceStabilityState, Set.of(2, 8));
        api.setLastDecayGameTime(sourceStabilityState, 320L);

        // 更新为 18 参数版本（Task 3 新增 6 个容量字段）
        final Object projection = api.newProjection(
            4.75D,     // totalPressure
            4.75D,     // effectivePressure
            0.0D,      // pressureCap
            0.0D,      // residentPressure
            0.0D,      // passivePressure
            0.0D,      // wheelReservePressure
            3.5D,      // burstPressure
            1.25D,     // fatigueDebt
            2,         // overloadTier
            "",        // blockedReason
            2,         // sealedSlotCount
            1,         // forcedDisabledCount
            // Task 3 新增的 6 个容量字段
            "",        // aptitudeTier
            0,         // apertureRank
            0,         // apertureStage
            0,         // baseRows
            0,         // bonusRows
            0          // totalRows
        );
        final Object target = api.newKongqiaoData();
        final Object targetStabilityState = api.getStabilityState(target);

        api.clearProjectionCache();
        api.applyAuthoritativeState(
            target,
            api.serializeData(source),
            projection
        );

        assertEquals(3.5D, api.getBurstPressure(targetStabilityState), DELTA);
        assertEquals(1.25D, api.getFatigueDebt(targetStabilityState), DELTA);
        assertEquals(2, api.getOverloadTier(targetStabilityState));
        assertEquals(
            Set.of("usage.alpha"),
            api.getForcedDisabledUsageIds(targetStabilityState)
        );
        assertEquals(Set.of(2, 8), api.getSealedSlots(targetStabilityState));
        assertEquals(320L, api.getLastDecayGameTime(targetStabilityState));
        assertTrue(api.objectsEqual(projection, api.currentProjection()));

        api.clearProjectionCache();
    }
}
