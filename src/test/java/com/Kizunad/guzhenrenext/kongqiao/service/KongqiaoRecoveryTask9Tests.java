package com.Kizunad.guzhenrenext.kongqiao.service;

import com.Kizunad.guzhenrenext.testsupport.Task4RuntimeHarness;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class KongqiaoRecoveryTask9Tests {

    private static final double DELTA = 1.0E-6D;

    @Test
    void recoveryTickDecaysFatigueAndBurstByLastDecayGameTimeCadence() throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object data = api.newKongqiaoData();
        final Object stabilityState = api.getStabilityState(data);
        api.setFatigueDebt(stabilityState, 5.0D);
        api.setBurstPressure(stabilityState, 4.0D);
        api.setLastDecayGameTime(stabilityState, 100L);

        applyRecoveryTick(stabilityState, 119L, capacityProfile(api), List.of(), 0);
        assertEquals(5.0D, api.getFatigueDebt(stabilityState), DELTA);
        assertEquals(4.0D, api.getBurstPressure(stabilityState), DELTA);
        assertEquals(100L, api.getLastDecayGameTime(stabilityState));

        applyRecoveryTick(stabilityState, 141L, capacityProfile(api), List.of(), 0);
        assertEquals(3.0D, api.getFatigueDebt(stabilityState), DELTA);
        assertEquals(2.0D, api.getBurstPressure(stabilityState), DELTA);
        assertEquals(140L, api.getLastDecayGameTime(stabilityState));
        assertEquals(0, api.getOverloadTier(stabilityState));
    }

    @Test
    void collapseEdgeSealsExactlyOneHighestUnlockedSlotDeterministically()
        throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object data = api.newKongqiaoData();
        final Object stabilityState = api.getStabilityState(data);
        api.setFatigueDebt(stabilityState, 6.0D);
        api.setBurstPressure(stabilityState, 4.0D);

        applyRecoveryTick(stabilityState, 100L, null, List.of(), 18);

        assertEquals(Set.of(17), api.getSealedSlots(stabilityState));
    }

    @Test
    void sealedSlotsPersistAcrossSerializeDeserializeUntilRecoveryCriteriaAreMet()
        throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object data = api.newKongqiaoData();
        final Object stabilityState = api.getStabilityState(data);
        api.setSealedSlots(stabilityState, Set.of(17));
        api.setFatigueDebt(stabilityState, 3.0D);
        api.setBurstPressure(stabilityState, 2.0D);
        api.setOverloadTier(stabilityState, 4);
        api.setLastDecayGameTime(stabilityState, 200L);

        final Object restored = api.newKongqiaoData();
        api.deserializeData(restored, api.serializeData(data));
        final Object restoredStabilityState = api.getStabilityState(restored);
        assertEquals(Set.of(17), api.getSealedSlots(restoredStabilityState));

        applyRecoveryTick(restoredStabilityState, 240L, null, List.of(), 18);
        assertEquals(Set.of(17, 16), api.getSealedSlots(restoredStabilityState));

        applyRecoveryTick(restoredStabilityState, 260L, null, List.of(), 18);
        assertEquals(Set.of(), api.getSealedSlots(restoredStabilityState));
        assertEquals(0.0D, api.getFatigueDebt(restoredStabilityState), DELTA);
        assertEquals(0.0D, api.getBurstPressure(restoredStabilityState), DELTA);
        assertEquals(0, api.getOverloadTier(restoredStabilityState));
    }

    @Test
    void recoveryDecayDoesNotMutateUnlockOrPreferenceState() throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object data = api.newKongqiaoData();
        final Object stabilityState = api.getStabilityState(data);
        api.setFatigueDebt(stabilityState, 3.0D);
        api.setBurstPressure(stabilityState, 2.0D);
        api.setLastDecayGameTime(stabilityState, 80L);

        final Object config = api.newTweakConfig();
        addWheelSkill(config, "guzhenren:active_alpha", 8);
        final List<String> wheelSkillsBefore = api.wheelSkills(config);
        final boolean passiveEnabledBefore = api.isPassiveEnabled(
            config,
            "guzhenren:test_passive_alpha"
        );

        final Object unlocks = newUnlocks(data.getClass().getClassLoader());
        startProcess(
            unlocks,
            api.newResourceLocation("guzhenren", "test_item"),
            "guzhenren:test_passive_alpha",
            5,
            12
        );
        final int remainingBefore = remainingTicks(unlocks);

        applyRecoveryTick(stabilityState, 120L, capacityProfile(api), List.of(), 0);

        assertEquals(1.0D, api.getFatigueDebt(stabilityState), DELTA);
        assertEquals(0.0D, api.getBurstPressure(stabilityState), DELTA);
        assertEquals(wheelSkillsBefore, api.wheelSkills(config));
        assertEquals(
            passiveEnabledBefore,
            api.isPassiveEnabled(config, "guzhenren:test_passive_alpha")
        );
        assertEquals(remainingBefore, remainingTicks(unlocks));
    }

    @Test
    void sealedSlotsReduceCollapsedPassiveProjectionMeaningfully() throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object alphaUsage = usage(api, "guzhenren:passive_alpha", 400, 100);
        final Object betaUsage = usage(api, "guzhenren:passive_beta", 600, 150);
        final List<Object> slotCandidates = List.of(
            newSlotPassiveRuntimeCandidate(
                alphaUsage.getClass().getClassLoader(),
                0,
                "guzhenren:passive_alpha",
                api.computePassivePressureCore(alphaUsage)
            ),
            newSlotPassiveRuntimeCandidate(
                alphaUsage.getClass().getClassLoader(),
                17,
                "guzhenren:passive_beta",
                api.computePassivePressureCore(betaUsage)
            )
        );

        final Object withoutSeal = collapseSlotRuntimeCandidatesByUsage(
            alphaUsage.getClass().getClassLoader(),
            slotCandidates,
            Set.of()
        );
        final Object withSeal = collapseSlotRuntimeCandidatesByUsage(
            alphaUsage.getClass().getClassLoader(),
            slotCandidates,
            Set.of(17)
        );

        final Object withoutSealSnapshot = api.evaluatePassiveRuntimeSnapshot(
            (java.util.Collection<?>) withoutSeal,
            capacityProfile(api),
            0.0D,
            0.0D
        );
        final Object withSealSnapshot = api.evaluatePassiveRuntimeSnapshot(
            (java.util.Collection<?>) withSeal,
            capacityProfile(api),
            0.0D,
            0.0D
        );

        assertTrue(
            api.passiveRuntimeSnapshotDouble(withoutSealSnapshot, "passivePressure")
                > api.passiveRuntimeSnapshotDouble(withSealSnapshot, "passivePressure")
        );
        assertEquals(
            Set.of("guzhenren:passive_alpha"),
            api.passiveRuntimeSnapshotSet(withSealSnapshot, "runnableUsageIds")
        );
    }

    @Test
    void sealedSlotIsSkippedByPassiveRuntimeExecutionPath() throws Exception {
        final List<Integer> runnableSlots = collectRunnablePassiveRuntimeSlots(
            Task4RuntimeHarness.create().newKongqiaoData().getClass().getClassLoader(),
            18,
            Set.of(17)
        );

        assertEquals(17, runnableSlots.size());
        assertTrue(runnableSlots.contains(16));
        assertTrue(!runnableSlots.contains(17));
    }

    @Test
    void sealedSlotTransitionsThroughUnequipThenNaturalReequipSemantics()
        throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final ClassLoader loader = api.newKongqiaoData().getClass().getClassLoader();

        assertEquals(
            "NO_CHANGE",
            determineEquipTransitionByVisibility(loader, false, false, true)
        );
        assertEquals(
            "UNEQUIP_ONLY",
            determineEquipTransitionByVisibility(loader, true, false, false)
        );
        assertEquals(
            "NO_CHANGE",
            determineEquipTransitionByVisibility(loader, false, false, true)
        );
        assertEquals(
            "EQUIP_ONLY",
            determineEquipTransitionByVisibility(loader, false, true, false)
        );
    }

    @Test
    void sealedSlotUnequipClearsStaleRuntimeActiveUsageIds() throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object actives = api.newActivePassives();
        final Object usage = usage(
            api,
            "guzhenren:guiqigu_passive_evasion",
            400,
            120
        );
        api.addActivePassive(actives, "guzhenren:guiqigu_passive_evasion");

        triggerUnequipCleanup(
            api.newKongqiaoData().getClass().getClassLoader(),
            actives,
            usage
        );

        assertTrue(
            !api.isActivePassive(actives, "guzhenren:guiqigu_passive_evasion")
        );
    }

    private static Object capacityProfile(final Task4RuntimeHarness.RuntimeApi api)
        throws Exception {
        return api.newCapacityProfile("CANCI", 0, 0, 1, 0, 1, 0.0D);
    }

    private static Object usage(
        final Task4RuntimeHarness.RuntimeApi api,
        final String usageId,
        final int durationCost,
        final int totalCost
    ) throws Exception {
        return api.newNianTouUsage(
            usageId,
            usageId,
            "Task 9 测试用途",
            "Task 9 测试用途",
            durationCost,
            totalCost,
            Map.of()
        );
    }

    private static void applyRecoveryTick(
        final Object stabilityState,
        final long currentGameTime,
        final Object capacityProfile,
        final List<?> slotPassiveRuntimeCandidates,
        final int unlockedSlotCount
    ) throws Exception {
        final ClassLoader loader = stabilityState.getClass().getClassLoader();
        final Class<?> stabilityStateClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData$StabilityState",
            true,
            loader
        );
        final Class<?> capacityProfileClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoCapacityProfile",
            true,
            loader
        );
        final Class<?> slotCandidateClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjectionService$SlotPassiveRuntimeCandidate",
            true,
            loader
        );
        final Class<?> serviceClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.service.GuRunningService",
            true,
            loader
        );
        final Method method = serviceClass.getDeclaredMethod(
            "applyRecoveryTick",
            stabilityStateClass,
            long.class,
            capacityProfileClass,
            List.class,
            int.class
        );
        method.setAccessible(true);
        method.invoke(
            null,
            stabilityState,
            currentGameTime,
            capacityProfile,
            slotPassiveRuntimeCandidates,
            unlockedSlotCount
        );
    }

    private static Object newSlotPassiveRuntimeCandidate(
        final ClassLoader loader,
        final int slot,
        final String usageId,
        final int passivePressure
    ) throws Exception {
        final Class<?> slotCandidateClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjectionService$SlotPassiveRuntimeCandidate",
            true,
            loader
        );
        final Constructor<?> constructor = slotCandidateClass.getDeclaredConstructor(
            int.class,
            String.class,
            int.class
        );
        constructor.setAccessible(true);
        return constructor.newInstance(slot, usageId, passivePressure);
    }

    private static Object collapseSlotRuntimeCandidatesByUsage(
        final ClassLoader loader,
        final List<?> slotCandidates,
        final Set<Integer> sealedSlots
    ) throws Exception {
        final Class<?> serviceClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.service.KongqiaoPressureProjectionService",
            true,
            loader
        );
        final Method method = serviceClass.getDeclaredMethod(
            "collapseSlotRuntimeCandidatesByUsage",
            java.util.Collection.class,
            Set.class
        );
        method.setAccessible(true);
        return method.invoke(null, slotCandidates, sealedSlots);
    }

    private static String determineEquipTransitionByVisibility(
        final ClassLoader loader,
        final boolean hadVisibleStack,
        final boolean hasVisibleStack,
        final boolean sameVisibleItem
    ) throws Exception {
        final Class<?> serviceClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.service.GuRunningService",
            true,
            loader
        );
        final Method method = serviceClass.getDeclaredMethod(
            "determineEquipTransitionByVisibility",
            boolean.class,
            boolean.class,
            boolean.class
        );
        method.setAccessible(true);
        final Object transition = method.invoke(
            null,
            hadVisibleStack,
            hasVisibleStack,
            sameVisibleItem
        );
        return ((Enum<?>) transition).name();
    }

    private static void triggerUnequipCleanup(
        final ClassLoader loader,
        final Object actives,
        final Object usage
    ) throws Exception {
        final Class<?> activePassivesClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.attachment.ActivePassives",
            true,
            loader
        );
        final Class<?> usageClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.niantou.NianTouData$Usage",
            true,
            loader
        );
        final Class<?> serviceClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.service.GuRunningService",
            true,
            loader
        );
        final Method method = serviceClass.getDeclaredMethod(
            "cleanupUnequippedRuntimeActiveState",
            activePassivesClass,
            usageClass
        );
        method.setAccessible(true);
        method.invoke(null, actives, usage);
    }

    private static List<Integer> collectRunnablePassiveRuntimeSlots(
        final ClassLoader loader,
        final int slotCount,
        final Set<Integer> sealedSlots
    ) throws Exception {
        final Class<?> serviceClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.service.GuRunningService",
            true,
            loader
        );
        final Method method = serviceClass.getDeclaredMethod(
            "collectRunnablePassiveRuntimeSlots",
            int.class,
            Set.class
        );
        method.setAccessible(true);
        return (List<Integer>) method.invoke(null, slotCount, sealedSlots);
    }

    private static Object newUnlocks(final ClassLoader loader) throws Exception {
        final Class<?> unlocksClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks",
            true,
            loader
        );
        return unlocksClass.getConstructor().newInstance();
    }

    private static void startProcess(
        final Object unlocks,
        final Object itemId,
        final String usageId,
        final int totalTicks,
        final int totalCost
    ) throws Exception {
        final Method method = unlocks.getClass().getMethod(
            "startProcess",
            itemId.getClass(),
            String.class,
            int.class,
            int.class
        );
        method.invoke(unlocks, itemId, usageId, totalTicks, totalCost);
    }

    private static int remainingTicks(final Object unlocks) throws Exception {
        final Object process = unlocks.getClass().getMethod("getCurrentProcess").invoke(unlocks);
        return process.getClass().getField("remainingTicks").getInt(process);
    }

    private static void addWheelSkill(
        final Object config,
        final String usageId,
        final int maxSize
    ) throws Exception {
        config.getClass()
            .getMethod("addWheelSkill", String.class, int.class)
            .invoke(config, usageId, maxSize);
    }
}
