package com.Kizunad.guzhenrenext.kongqiao.attachment;

import com.Kizunad.guzhenrenext.testsupport.Task4RuntimeHarness;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class KongqiaoDataStabilityStateTests {

    private static final double DELTA = 1.0E-6D;

    @Test
    void stabilityStateRoundTripsThroughTopLevelSerializeDeserialize() throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object data = api.newKongqiaoData();
        final Object stabilityState = api.getStabilityState(data);
        api.clearDirty(data);
        api.setBurstPressure(stabilityState, 12.5D);
        api.setFatigueDebt(stabilityState, 6.75D);
        api.setOverloadTier(stabilityState, 3);
        api.setForcedDisabledUsageIds(
            stabilityState,
            Set.of("gu.passive.alpha", "gu.passive.beta")
        );
        api.setSealedSlots(stabilityState, Set.of(1, 4, 7));
        api.setLastDecayGameTime(stabilityState, 2400L);

        assertTrue(api.isDirty(data));

        final Object serialized = api.serializeData(data);
        assertTrue(api.getAllKeys(serialized).contains("stability"));

        final Object restored = api.newKongqiaoData();
        api.deserializeData(restored, serialized);
        final Object restoredStabilityState = api.getStabilityState(restored);

        assertEquals(12.5D, api.getBurstPressure(restoredStabilityState), DELTA);
        assertEquals(6.75D, api.getFatigueDebt(restoredStabilityState), DELTA);
        assertEquals(3, api.getOverloadTier(restoredStabilityState));
        assertEquals(
            Set.of("gu.passive.alpha", "gu.passive.beta"),
            api.getForcedDisabledUsageIds(restoredStabilityState)
        );
        assertEquals(Set.of(1, 4, 7), api.getSealedSlots(restoredStabilityState));
        assertEquals(2400L, api.getLastDecayGameTime(restoredStabilityState));
    }

    @Test
    void missingStabilityTagFallsBackToDefaultRawState() throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object restored = api.newKongqiaoData();
        final Object stabilityState = api.getStabilityState(restored);
        api.setBurstPressure(stabilityState, 9.0D);
        api.setFatigueDebt(stabilityState, 3.0D);
        api.setOverloadTier(stabilityState, 2);
        api.setForcedDisabledUsageIds(stabilityState, Set.of("gu.passive.alpha"));
        api.setSealedSlots(stabilityState, Set.of(5));
        api.setLastDecayGameTime(stabilityState, 120L);

        api.deserializeData(restored, api.newCompoundTag());

        assertEquals(0.0D, api.getBurstPressure(stabilityState), DELTA);
        assertEquals(0.0D, api.getFatigueDebt(stabilityState), DELTA);
        assertEquals(0, api.getOverloadTier(stabilityState));
        assertEquals(Set.of(), api.getForcedDisabledUsageIds(stabilityState));
        assertEquals(Set.of(), api.getSealedSlots(stabilityState));
        assertEquals(0L, api.getLastDecayGameTime(stabilityState));
    }

    @Test
    void gameplayActivationRoundTripsThroughTopLevelSerializeDeserialize() throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object data = api.newKongqiaoData();
        api.clearDirty(data);
        api.setGameplayActivated(data, true);

        assertTrue(api.isDirty(data));

        final Object serialized = api.serializeData(data);
        assertTrue(api.getAllKeys(serialized).contains("gameplayActivated"));

        final Object restored = api.newKongqiaoData();
        api.deserializeData(restored, serialized);

        assertTrue(api.isGameplayActivated(restored));
    }

    @Test
    void missingGameplayActivationTagFallsBackToDisabled() throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object restored = api.newKongqiaoData();
        api.setGameplayActivated(restored, true);

        api.deserializeData(restored, api.newCompoundTag());

        assertFalse(api.isGameplayActivated(restored));
    }
}
