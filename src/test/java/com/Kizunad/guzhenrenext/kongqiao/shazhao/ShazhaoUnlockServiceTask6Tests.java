package com.Kizunad.guzhenrenext.kongqiao.shazhao;

import com.Kizunad.guzhenrenext.testsupport.Task4RuntimeHarness;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ShazhaoUnlockServiceTask6Tests {

    private static final double DELTA = 1.0E-6D;

    @Test
    void resolveDeriveAttemptSuccessUnlocksShazhaoAndAppendsMediumFatigueDebt()
        throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object data = api.newKongqiaoData();
        final Object stabilityState = api.getStabilityState(data);
        api.setFatigueDebt(stabilityState, 1.0D);
        final Object unlocks = newUnlocks(data.getClass().getClassLoader());
        final int[] nianTouPool = {20};

        final Object result = resolveDeriveAttempt(
            unlocks,
            stabilityState,
            newUnlockCandidate(
                api.newShazhaoData(
                    "guzhenren:test_success",
                    "星火杀招",
                    "Task 6 成功测试",
                    "威能 {power}",
                    12,
                    List.of("minecraft:apple"),
                    Map.of("power", "2.0")
                ),
                0.75D
            ),
            0.25D,
            nianTouPool[0],
            amount -> nianTouPool[0] -= amount
        );

        assertTrue(booleanAccessor(result, "success"));
        assertEquals(0, intAccessor(result, "nianTouCostConsumed"));
        assertEquals(8.0D, doubleAccessor(result, "fatigueDebtApplied"), DELTA);
        assertEquals(20, nianTouPool[0]);
        assertTrue(
            isShazhaoUnlocked(
                unlocks,
                api.newResourceLocation("guzhenren", "test_success")
            )
        );
        assertEquals("推演成功：星火杀招 | 威能 2.0", getShazhaoMessage(unlocks));
        assertEquals(9.0D, api.getFatigueDebt(stabilityState), DELTA);
    }

    @Test
    void resolveDeriveAttemptFailureConsumesNianTouAndAppendsMediumFatigueDebt()
        throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object data = api.newKongqiaoData();
        final Object stabilityState = api.getStabilityState(data);
        final Object unlocks = newUnlocks(data.getClass().getClassLoader());
        final int[] nianTouPool = {10};

        final Object result = resolveDeriveAttempt(
            unlocks,
            stabilityState,
            newUnlockCandidate(
                api.newShazhaoData(
                    "guzhenren:test_failure_cost",
                    "失手杀招",
                    "Task 6 失败测试",
                    "失败信息",
                    6,
                    List.of("minecraft:apple"),
                    Map.of()
                ),
                0.35D
            ),
            0.9D,
            nianTouPool[0],
            amount -> nianTouPool[0] -= amount
        );

        assertFalse(booleanAccessor(result, "success"));
        assertEquals(6, intAccessor(result, "nianTouCostConsumed"));
        assertEquals(8.0D, doubleAccessor(result, "fatigueDebtApplied"), DELTA);
        assertEquals(4, nianTouPool[0]);
        assertFalse(
            isShazhaoUnlocked(
                unlocks,
                api.newResourceLocation("guzhenren", "test_failure_cost")
            )
        );
        assertEquals("推演失败，消耗 6 念头", getShazhaoMessage(unlocks));
        assertEquals(8.0D, api.getFatigueDebt(stabilityState), DELTA);
    }

    @Test
    void resolveDeriveAttemptInsufficientNianTouKeepsFailureMessageAndStillWritesDebt()
        throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object data = api.newKongqiaoData();
        final Object stabilityState = api.getStabilityState(data);
        api.setFatigueDebt(stabilityState, 2.5D);
        final Object unlocks = newUnlocks(data.getClass().getClassLoader());
        final int[] nianTouPool = {3};

        final Object result = resolveDeriveAttempt(
            unlocks,
            stabilityState,
            newUnlockCandidate(
                api.newShazhaoData(
                    "guzhenren:test_failure_shortage",
                    "匮乏杀招",
                    "Task 6 念头不足测试",
                    "失败信息",
                    9,
                    List.of("minecraft:apple"),
                    Map.of()
                ),
                0.20D
            ),
            0.8D,
            nianTouPool[0],
            amount -> nianTouPool[0] -= amount
        );

        assertFalse(booleanAccessor(result, "success"));
        assertEquals(0, intAccessor(result, "nianTouCostConsumed"));
        assertEquals(8.0D, doubleAccessor(result, "fatigueDebtApplied"), DELTA);
        assertEquals(3, nianTouPool[0]);
        assertFalse(
            isShazhaoUnlocked(
                unlocks,
                api.newResourceLocation("guzhenren", "test_failure_shortage")
            )
        );
        assertEquals("念头不足，无法推演杀招", getShazhaoMessage(unlocks));
        assertEquals(10.5D, api.getFatigueDebt(stabilityState), DELTA);
    }

    private static Object newUnlocks(final ClassLoader loader) throws Exception {
        final Class<?> unlocksClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks",
            true,
            loader
        );
        return unlocksClass.getConstructor().newInstance();
    }

    private static Object newUnlockCandidate(final Object shazhaoData, final double chance)
        throws Exception {
        final Class<?> candidateClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoUnlockService$UnlockCandidate",
            true,
            shazhaoData.getClass().getClassLoader()
        );
        final Constructor<?> constructor = candidateClass.getDeclaredConstructor(
            shazhaoData.getClass(),
            double.class
        );
        constructor.setAccessible(true);
        return constructor.newInstance(shazhaoData, chance);
    }

    private static Object resolveDeriveAttempt(
        final Object unlocks,
        final Object stabilityState,
        final Object candidate,
        final double roll,
        final double currentNianTou,
        final IntConsumer consumer
    ) throws Exception {
        final ClassLoader loader = unlocks.getClass().getClassLoader();
        final Class<?> unlocksClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.attachment.NianTouUnlocks",
            true,
            loader
        );
        final Class<?> stabilityStateClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.attachment.KongqiaoData$StabilityState",
            true,
            loader
        );
        final Class<?> candidateClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoUnlockService$UnlockCandidate",
            true,
            loader
        );
        final Class<?> serviceClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.shazhao.ShazhaoUnlockService",
            true,
            loader
        );
        final Method method = serviceClass.getMethod(
            "resolveDeriveAttempt",
            unlocksClass,
            stabilityStateClass,
            candidateClass,
            double.class,
            double.class,
            IntConsumer.class
        );
        return method.invoke(
            null,
            unlocks,
            stabilityState,
            candidate,
            roll,
            currentNianTou,
            consumer
        );
    }

    private static boolean isShazhaoUnlocked(final Object unlocks, final Object shazhaoId)
        throws Exception {
        final Method method = unlocks.getClass().getMethod(
            "isShazhaoUnlocked",
            shazhaoId.getClass()
        );
        return (boolean) method.invoke(unlocks, shazhaoId);
    }

    private static String getShazhaoMessage(final Object unlocks) throws Exception {
        return (String) unlocks.getClass().getMethod("getShazhaoMessage").invoke(unlocks);
    }

    private static double doubleAccessor(final Object target, final String accessorName)
        throws Exception {
        final Method accessor = target.getClass().getDeclaredMethod(accessorName);
        accessor.setAccessible(true);
        return (double) accessor.invoke(target);
    }

    private static int intAccessor(final Object target, final String accessorName)
        throws Exception {
        final Method accessor = target.getClass().getDeclaredMethod(accessorName);
        accessor.setAccessible(true);
        return (int) accessor.invoke(target);
    }

    private static boolean booleanAccessor(final Object target, final String accessorName)
        throws Exception {
        final Method accessor = target.getClass().getDeclaredMethod(accessorName);
        accessor.setAccessible(true);
        return (boolean) accessor.invoke(target);
    }
}
