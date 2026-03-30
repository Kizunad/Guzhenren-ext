package com.Kizunad.guzhenrenext.kongqiao.handler;

import com.Kizunad.guzhenrenext.testsupport.Task4RuntimeHarness;
import java.lang.reflect.Method;
import java.util.function.DoubleConsumer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NianTouTickHandlerTask6Tests {

    private static final double DELTA = 1.0E-6D;

    @Test
    void advanceProcessIfAffordableConsumesCostAndAppendsLightFatigueDebt()
        throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object data = api.newKongqiaoData();
        final Object stabilityState = api.getStabilityState(data);
        api.setFatigueDebt(stabilityState, 1.5D);
        final Object unlocks = newUnlocks(data.getClass().getClassLoader());
        startProcess(
            unlocks,
            api.newResourceLocation("minecraft", "apple"),
            "guzhenren:test_usage",
            3,
            9
        );
        final double[] nianTouPool = {5.0D};

        final Object result = advanceProcessIfAffordable(
            unlocks,
            stabilityState,
            nianTouPool[0],
            3.0D,
            amount -> nianTouPool[0] -= amount
        );

        assertEquals(3.0D, doubleAccessor(result, "consumedNianTou"), DELTA);
        assertTrue(booleanAccessor(result, "progressed"));
        assertFalse(booleanAccessor(result, "completed"));
        assertEquals(4.0D, doubleAccessor(result, "fatigueDebtApplied"), DELTA);
        assertEquals(2.0D, nianTouPool[0], DELTA);
        assertEquals(2, remainingTicks(unlocks));
        assertEquals(5.5D, api.getFatigueDebt(stabilityState), DELTA);
    }

    @Test
    void advanceProcessIfAffordablePausesWhenNianTouCannotCoverCost()
        throws Exception {
        final Task4RuntimeHarness.RuntimeApi api = Task4RuntimeHarness.create();
        final Object data = api.newKongqiaoData();
        final Object stabilityState = api.getStabilityState(data);
        api.setFatigueDebt(stabilityState, 2.0D);
        final Object unlocks = newUnlocks(data.getClass().getClassLoader());
        startProcess(
            unlocks,
            api.newResourceLocation("minecraft", "apple"),
            "guzhenren:test_usage",
            2,
            8
        );
        final double[] nianTouPool = {1.5D};

        final Object result = advanceProcessIfAffordable(
            unlocks,
            stabilityState,
            nianTouPool[0],
            4.0D,
            amount -> nianTouPool[0] -= amount
        );

        assertEquals(0.0D, doubleAccessor(result, "consumedNianTou"), DELTA);
        assertFalse(booleanAccessor(result, "progressed"));
        assertFalse(booleanAccessor(result, "completed"));
        assertEquals(0.0D, doubleAccessor(result, "fatigueDebtApplied"), DELTA);
        assertEquals(1.5D, nianTouPool[0], DELTA);
        assertEquals(2, remainingTicks(unlocks));
        assertEquals(2.0D, api.getFatigueDebt(stabilityState), DELTA);
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

    private static Object advanceProcessIfAffordable(
        final Object unlocks,
        final Object stabilityState,
        final double currentNianTou,
        final double costPerTick,
        final DoubleConsumer consumer
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
        final Class<?> handlerClass = Class.forName(
            "com.Kizunad.guzhenrenext.kongqiao.handler.NianTouTickHandler",
            true,
            loader
        );
        final Method method = handlerClass.getDeclaredMethod(
            "advanceProcessIfAffordable",
            unlocksClass,
            stabilityStateClass,
            double.class,
            double.class,
            DoubleConsumer.class
        );
        method.setAccessible(true);
        return method.invoke(
            null,
            unlocks,
            stabilityState,
            currentNianTou,
            costPerTick,
            consumer
        );
    }

    private static int remainingTicks(final Object unlocks) throws Exception {
        final Object process = unlocks.getClass().getMethod("getCurrentProcess").invoke(unlocks);
        return process.getClass().getField("remainingTicks").getInt(process);
    }

    private static double doubleAccessor(final Object target, final String accessorName)
        throws Exception {
        final Method accessor = target.getClass().getDeclaredMethod(accessorName);
        accessor.setAccessible(true);
        return (double) accessor.invoke(target);
    }

    private static boolean booleanAccessor(final Object target, final String accessorName)
        throws Exception {
        final Method accessor = target.getClass().getDeclaredMethod(accessorName);
        accessor.setAccessible(true);
        return (boolean) accessor.invoke(target);
    }
}
