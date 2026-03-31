package com.Kizunad.guzhenrenext.entity.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GoapMindCore {

    public static final long TICKS_PER_SECOND = 20L;

    private static final long NEVER_RUN_TICK = Long.MIN_VALUE;

    private final Map<String, Boolean> worldState;
    private final Map<String, GoalSpec> goals;
    private final Map<String, ActionSpec> actions;
    private final Map<String, SensorSpec> sensors;
    private final Deque<ActionSpec> planCache;

    private String currentAction;
    private long worldStateRevision;
    private long goalRegistryRevision;
    private boolean hasCachedGoalResult;
    private long cachedGoalWorldStateRevision;
    private long cachedGoalRegistryRevision;
    private GoalSpec cachedGoal;
    private long goalEvaluationComputationCount;

    public GoapMindCore() {
        this.worldState = new HashMap<>();
        this.goals = new LinkedHashMap<>();
        this.actions = new LinkedHashMap<>();
        this.sensors = new LinkedHashMap<>();
        this.planCache = new ArrayDeque<>();
        this.currentAction = null;
        this.worldStateRevision = 0L;
        this.goalRegistryRevision = 0L;
        this.hasCachedGoalResult = false;
        this.cachedGoalWorldStateRevision = 0L;
        this.cachedGoalRegistryRevision = 0L;
        this.cachedGoal = null;
        this.goalEvaluationComputationCount = 0L;
    }

    public void registerGoal(
        String goalName,
        int priority,
        Map<String, Boolean> conditions
    ) {
        validateName(goalName, "goalName");
        Objects.requireNonNull(conditions, "conditions 不能为空");
        ensureUniqueName(goals, goalName, "Goal");
        goals.put(goalName, new GoalSpec(priority, copyConditions(conditions)));
        goalRegistryRevision++;
    }

    public void registerAction(
        String actionName,
        Map<String, Boolean> preconditions,
        Map<String, Boolean> effects,
        Runnable executor
    ) {
        validateName(actionName, "actionName");
        Objects.requireNonNull(preconditions, "preconditions 不能为空");
        Objects.requireNonNull(effects, "effects 不能为空");
        Objects.requireNonNull(executor, "executor 不能为空");
        ensureUniqueName(actions, actionName, "Action");
        actions.put(
            actionName,
            new ActionSpec(
                actionName,
                copyConditions(preconditions),
                copyConditions(effects),
                executor
            )
        );
    }

    public void registerSensor(String sensorName, Runnable updater) {
        registerSensor(sensorName, TICKS_PER_SECOND, updater);
    }

    public void registerSensor(String sensorName, long intervalTicks, Runnable updater) {
        validateName(sensorName, "sensorName");
        validateSensorInterval(intervalTicks);
        Objects.requireNonNull(updater, "updater 不能为空");
        ensureUniqueName(sensors, sensorName, "Sensor");
        sensors.put(sensorName, new SensorSpec(intervalTicks, updater));
    }

    public void tick(long currentTick) {
        if (currentTick % TICKS_PER_SECOND != 0L) {
            return;
        }

        runSensors(currentTick);
        replanAndExecute();
    }

    public void setWorldState(String key, boolean value) {
        validateName(key, "key");
        Boolean oldValue = worldState.get(key);
        worldState.put(key, value);
        if (!Objects.equals(oldValue, value)) {
            worldStateRevision++;
        }
    }

    public boolean getWorldState(String key) {
        validateName(key, "key");
        return worldState.getOrDefault(key, Boolean.FALSE);
    }

    public String getCurrentAction() {
        return currentAction;
    }

    long getGoalEvaluationComputationCount() {
        return goalEvaluationComputationCount;
    }

    private void runSensors(long currentTick) {
        for (SensorSpec sensor : sensors.values()) {
            if (sensor.shouldRunAt(currentTick)) {
                sensor.updater.run();
                sensor.markRun(currentTick);
            }
        }
    }

    private void replanAndExecute() {
        planCache.clear();

        GoalSpec targetGoal = selectHighestPriorityGoal();
        if (targetGoal == null) {
            currentAction = null;
            return;
        }

        ActionSpec action = selectActionForGoal(targetGoal);
        if (action == null) {
            currentAction = null;
            return;
        }

        planCache.add(action);
        executePlan();
    }

    private GoalSpec selectHighestPriorityGoal() {
        if (
            hasCachedGoalResult &&
            cachedGoalWorldStateRevision == worldStateRevision &&
            cachedGoalRegistryRevision == goalRegistryRevision
        ) {
            return cachedGoal;
        }

        goalEvaluationComputationCount++;
        GoalSpec bestGoal = null;
        int bestPriority = Integer.MIN_VALUE;

        for (GoalSpec goal : goals.values()) {
            if (!matchesConditions(goal.conditions)) {
                continue;
            }

            if (bestGoal == null || goal.priority > bestPriority) {
                bestGoal = goal;
                bestPriority = goal.priority;
            }
        }

        hasCachedGoalResult = true;
        cachedGoalWorldStateRevision = worldStateRevision;
        cachedGoalRegistryRevision = goalRegistryRevision;
        cachedGoal = bestGoal;
        return bestGoal;
    }

    private ActionSpec selectActionForGoal(GoalSpec goal) {
        for (ActionSpec action : actions.values()) {
            if (!matchesConditions(action.preconditions)) {
                continue;
            }
            if (effectsCoverGoal(action.effects, goal.conditions)) {
                return action;
            }
        }
        return null;
    }

    private void executePlan() {
        ActionSpec action = planCache.pollFirst();
        if (action == null) {
            currentAction = null;
            return;
        }

        currentAction = action.name;
        action.executor.run();
        applyEffects(action.effects);
    }

    private boolean matchesConditions(Map<String, Boolean> conditions) {
        for (Map.Entry<String, Boolean> entry : conditions.entrySet()) {
            Boolean currentValue = worldState.get(entry.getKey());
            if (!Objects.equals(currentValue, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private boolean effectsCoverGoal(
        Map<String, Boolean> effects,
        Map<String, Boolean> goalConditions
    ) {
        for (Map.Entry<String, Boolean> goalEntry : goalConditions.entrySet()) {
            if (!effects.containsKey(goalEntry.getKey())) {
                return false;
            }
            if (
                !Objects.equals(
                    effects.get(goalEntry.getKey()),
                    goalEntry.getValue()
                )
            ) {
                return false;
            }
        }
        return true;
    }

    private void applyEffects(Map<String, Boolean> effects) {
        for (Map.Entry<String, Boolean> effectEntry : effects.entrySet()) {
            setWorldState(effectEntry.getKey(), effectEntry.getValue());
        }
    }

    private void validateSensorInterval(long intervalTicks) {
        if (intervalTicks <= 0L) {
            throw new IllegalArgumentException("intervalTicks 必须大于 0");
        }
    }

    private Map<String, Boolean> copyConditions(Map<String, Boolean> conditions) {
        Map<String, Boolean> copied = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : conditions.entrySet()) {
            validateName(entry.getKey(), "conditionKey");
            Objects.requireNonNull(entry.getValue(), "conditionValue 不能为空");
            copied.put(entry.getKey(), entry.getValue());
        }
        return copied;
    }

    private void validateName(String name, String argName) {
        Objects.requireNonNull(name, argName + " 不能为空");
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException(argName + " 不能为空字符串");
        }
    }

    private void ensureUniqueName(
        Map<String, ?> registry,
        String name,
        String typeName
    ) {
        if (registry.containsKey(name)) {
            throw new IllegalArgumentException(typeName + " 已注册: " + name);
        }
    }

    public List<String> getRegisteredGoalNames() {
        return new ArrayList<>(goals.keySet());
    }

    public List<String> getRegisteredActionNames() {
        return new ArrayList<>(actions.keySet());
    }

    public List<String> getRegisteredSensorNames() {
        return new ArrayList<>(sensors.keySet());
    }

    private static final class GoalSpec {

        private final int priority;
        private final Map<String, Boolean> conditions;

        private GoalSpec(int priority, Map<String, Boolean> conditions) {
            this.priority = priority;
            this.conditions = conditions;
        }
    }

    private static final class ActionSpec {

        private final String name;
        private final Map<String, Boolean> preconditions;
        private final Map<String, Boolean> effects;
        private final Runnable executor;

        private ActionSpec(
            String name,
            Map<String, Boolean> preconditions,
            Map<String, Boolean> effects,
            Runnable executor
        ) {
            this.name = name;
            this.preconditions = preconditions;
            this.effects = effects;
            this.executor = executor;
        }
    }

    private static final class SensorSpec {

        private final long intervalTicks;
        private final Runnable updater;
        private long lastRunTick;

        private SensorSpec(long intervalTicks, Runnable updater) {
            this.intervalTicks = intervalTicks;
            this.updater = updater;
            this.lastRunTick = NEVER_RUN_TICK;
        }

        private boolean shouldRunAt(long currentTick) {
            return lastRunTick == NEVER_RUN_TICK || currentTick - lastRunTick >= intervalTicks;
        }

        private void markRun(long currentTick) {
            this.lastRunTick = currentTick;
        }
    }
}
