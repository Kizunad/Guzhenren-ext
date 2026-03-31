package com.Kizunad.guzhenrenext.entity.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

final class GoapMindCoreTest {

    private static final int HIGH_PRIORITY = 20;
    private static final int LOW_PRIORITY = 10;

    @Test
    void testRegisterGoalActionSensor() {
        GoapMindCore core = new GoapMindCore();

        core.registerGoal("idle", LOW_PRIORITY, Map.of("isSafe", true));
        core.registerAction(
            "wait",
            Map.of(),
            Map.of("isSafe", true),
            () -> {
            }
        );
        core.registerSensor("safety", () -> core.setWorldState("isSafe", true));

        assertEquals(1, core.getRegisteredGoalNames().size());
        assertEquals(1, core.getRegisteredActionNames().size());
        assertEquals(1, core.getRegisteredSensorNames().size());
        assertTrue(core.getRegisteredGoalNames().contains("idle"));
        assertTrue(core.getRegisteredActionNames().contains("wait"));
        assertTrue(core.getRegisteredSensorNames().contains("safety"));
    }

    @Test
    void testTickFrequencyPlanOnlyEveryTwentyTicks() {
        GoapMindCore core = new GoapMindCore();
        AtomicInteger sensorRuns = new AtomicInteger();
        AtomicInteger actionRuns = new AtomicInteger();

        core.registerSensor("hunger", () -> {
            sensorRuns.incrementAndGet();
            core.setWorldState("isHungry", true);
        });
        core.registerGoal("eat", HIGH_PRIORITY, Map.of("isHungry", true));
        core.registerAction(
            "eatAction",
            Map.of(),
            Map.of("isHungry", true),
            actionRuns::incrementAndGet
        );

        core.tick(19L);
        assertEquals(0, sensorRuns.get());
        assertEquals(0, actionRuns.get());
        assertNull(core.getCurrentAction());

        core.tick(20L);
        assertEquals(1, sensorRuns.get());
        assertEquals(1, actionRuns.get());
        assertEquals("eatAction", core.getCurrentAction());

        core.tick(21L);
        assertEquals(1, sensorRuns.get());
        assertEquals(1, actionRuns.get());

        core.tick(40L);
        assertEquals(2, sensorRuns.get());
        assertEquals(2, actionRuns.get());
    }

    @Test
    void testSensorDoesNotRunMoreFrequentlyThanConfiguredInterval() {
        GoapMindCore core = new GoapMindCore();
        AtomicInteger sensorRuns = new AtomicInteger();
        AtomicInteger actionRuns = new AtomicInteger();

        core.registerSensor("slowSensor", 40L, () -> {
            sensorRuns.incrementAndGet();
            core.setWorldState("isReady", true);
        });
        core.registerGoal("readyGoal", HIGH_PRIORITY, Map.of("isReady", true));
        core.registerAction(
            "readyAction",
            Map.of(),
            Map.of("isReady", true),
            actionRuns::incrementAndGet
        );

        core.tick(20L);
        assertEquals(1, sensorRuns.get());
        assertEquals(1, actionRuns.get());

        core.tick(40L);
        assertEquals(1, sensorRuns.get());
        assertEquals(2, actionRuns.get());

        core.tick(60L);
        assertEquals(2, sensorRuns.get());
        assertEquals(3, actionRuns.get());
    }

    @Test
    void testWorldStateSetAndGet() {
        GoapMindCore core = new GoapMindCore();

        assertFalse(core.getWorldState("hasEnemy"));

        core.setWorldState("hasEnemy", true);
        assertTrue(core.getWorldState("hasEnemy"));

        core.setWorldState("hasEnemy", false);
        assertFalse(core.getWorldState("hasEnemy"));
    }

    @Test
    void testSelectHighestPrioritySatisfiedGoal() {
        GoapMindCore core = new GoapMindCore();
        AtomicInteger lowGoalActionRuns = new AtomicInteger();
        AtomicInteger highGoalActionRuns = new AtomicInteger();

        core.setWorldState("lowReady", true);
        core.setWorldState("highReady", true);

        core.registerGoal("lowGoal", LOW_PRIORITY, Map.of("lowReady", true));
        core.registerGoal("highGoal", HIGH_PRIORITY, Map.of("highReady", true));

        core.registerAction(
            "lowAction",
            Map.of(),
            Map.of("lowReady", true),
            lowGoalActionRuns::incrementAndGet
        );
        core.registerAction(
            "highAction",
            Map.of(),
            Map.of("highReady", true),
            highGoalActionRuns::incrementAndGet
        );

        core.tick(20L);

        assertEquals(0, lowGoalActionRuns.get());
        assertEquals(1, highGoalActionRuns.get());
        assertEquals("highAction", core.getCurrentAction());
    }

    @Test
    void testActionExecutionWithPreconditionsAndEffects() {
        GoapMindCore core = new GoapMindCore();
        AtomicInteger actionRuns = new AtomicInteger();

        core.setWorldState("nearForge", true);
        core.setWorldState("hasWeapon", true);
        core.registerGoal("craftWeapon", HIGH_PRIORITY, Map.of("hasWeapon", true));
        core.registerAction(
            "craftWeaponAction",
            Map.of("nearForge", true),
            Map.of("hasWeapon", true),
            actionRuns::incrementAndGet
        );

        core.tick(20L);

        assertEquals(1, actionRuns.get());
        assertTrue(core.getWorldState("hasWeapon"));
        assertEquals("craftWeaponAction", core.getCurrentAction());
    }

    @Test
    void testGoalEvaluationCacheReusedWhenStateUnchanged() {
        GoapMindCore core = new GoapMindCore();
        AtomicInteger actionRuns = new AtomicInteger();

        core.setWorldState("isReady", true);
        core.registerGoal("ready", HIGH_PRIORITY, Map.of("isReady", true));
        core.registerAction(
            "readyAction",
            Map.of(),
            Map.of("isReady", true),
            actionRuns::incrementAndGet
        );

        core.tick(20L);
        assertEquals(1L, core.getGoalEvaluationComputationCount());
        assertEquals(1, actionRuns.get());

        core.tick(40L);
        assertEquals(1L, core.getGoalEvaluationComputationCount());
        assertEquals(2, actionRuns.get());
    }

    @Test
    void testGoalEvaluationCacheInvalidatesWhenWorldStateChanges() {
        GoapMindCore core = new GoapMindCore();

        core.registerGoal("goalA", LOW_PRIORITY, Map.of("flagA", true));
        core.registerGoal("goalB", HIGH_PRIORITY, Map.of("flagB", true));
        core.registerAction("actionA", Map.of(), Map.of("flagA", true), () -> {
        });
        core.registerAction("actionB", Map.of(), Map.of("flagB", true), () -> {
        });

        core.setWorldState("flagA", true);
        core.tick(20L);
        assertEquals(1L, core.getGoalEvaluationComputationCount());
        assertEquals("actionA", core.getCurrentAction());

        core.tick(40L);
        assertEquals(1L, core.getGoalEvaluationComputationCount());
        assertEquals("actionA", core.getCurrentAction());

        core.setWorldState("flagA", false);
        core.setWorldState("flagB", true);

        core.tick(60L);
        assertEquals(2L, core.getGoalEvaluationComputationCount());
        assertEquals("actionB", core.getCurrentAction());
    }
}
