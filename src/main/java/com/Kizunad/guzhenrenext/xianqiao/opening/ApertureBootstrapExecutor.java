package com.Kizunad.guzhenrenext.xianqiao.opening;

import java.util.Objects;

public final class ApertureBootstrapExecutor {

    public void execute(ExecutionContext context) {
        Objects.requireNonNull(context, "context");
        PersistedState currentState = context.readState();
        if (currentState.phase() == BootstrapPhase.COMPLETED) {
            return;
        }

        FrozenBootstrapPlan frozenPlan = ensureFrozenPlan(context, currentState);
        BootstrapPhase phase = context.readState().phase();
        if (phase == BootstrapPhase.UNINITIALIZED) {
            context.writeState(
                new PersistedState(
                    BootstrapPhase.PLANNED,
                    frozenPlan.openingSnapshot(),
                    Integer.valueOf(frozenPlan.layoutVersion()),
                    Long.valueOf(frozenPlan.planSeed())
                )
            );
            phase = BootstrapPhase.PLANNED;
        }

        if (phase == BootstrapPhase.PLANNED) {
            context.materializeCells(frozenPlan);
            context.writeState(
                new PersistedState(
                    BootstrapPhase.EXECUTING,
                    frozenPlan.openingSnapshot(),
                    Integer.valueOf(frozenPlan.layoutVersion()),
                    Long.valueOf(frozenPlan.planSeed())
                )
            );
            phase = BootstrapPhase.EXECUTING;
        }

        if (phase == BootstrapPhase.EXECUTING) {
            Object resolvedCenter = context.resolvePlatformCenterAfterSampling(frozenPlan);
            context.updateCenter(resolvedCenter);
            context.createInitialPlatform(resolvedCenter);
            context.spawnLandSpirit(resolvedCenter);
            context.finalizeWorldData();
        }
    }

    private static FrozenBootstrapPlan ensureFrozenPlan(ExecutionContext context, PersistedState state) {
        if (state.hasFrozenPlan()) {
            return new FrozenBootstrapPlan(
                state.openingSnapshot(),
                state.layoutVersion().intValue(),
                state.planSeed().longValue()
            );
        }
        FrozenBootstrapPlan resolvedPlan = Objects.requireNonNull(context.resolvePlanSnapshot(), "resolvedPlan");
        BootstrapPhase targetPhase = state.phase() == BootstrapPhase.COMPLETED
            ? BootstrapPhase.COMPLETED
            : BootstrapPhase.PLANNED;
        context.writeState(
            new PersistedState(
                targetPhase,
                resolvedPlan.openingSnapshot(),
                Integer.valueOf(resolvedPlan.layoutVersion()),
                Long.valueOf(resolvedPlan.planSeed())
            )
        );
        return resolvedPlan;
    }

    public enum BootstrapPhase {
        UNINITIALIZED,
        PLANNED,
        EXECUTING,
        COMPLETED
    }

    public record PersistedState(
        BootstrapPhase phase,
        AscensionConditionSnapshot openingSnapshot,
        Integer layoutVersion,
        Long planSeed
    ) {

        public PersistedState {
            phase = Objects.requireNonNull(phase, "phase");
        }

        public static PersistedState uninitialized() {
            return new PersistedState(BootstrapPhase.UNINITIALIZED, null, null, null);
        }

        public boolean hasFrozenPlan() {
            return openingSnapshot != null && layoutVersion != null && planSeed != null;
        }
    }

    public record FrozenBootstrapPlan(
        AscensionConditionSnapshot openingSnapshot,
        int layoutVersion,
        long planSeed,
        InitialTerrainPlan initialTerrainPlan
    ) {

        public FrozenBootstrapPlan(AscensionConditionSnapshot openingSnapshot, int layoutVersion, long planSeed) {
            this(openingSnapshot, layoutVersion, planSeed, null);
        }

        public FrozenBootstrapPlan {
            openingSnapshot = Objects.requireNonNull(openingSnapshot, "openingSnapshot");
            if (layoutVersion <= 0) {
                throw new IllegalArgumentException("layoutVersion 必须大于 0");
            }
        }

        public boolean hasInitialTerrainPlan() {
            return initialTerrainPlan != null;
        }
    }

    public interface ExecutionContext {

        PersistedState readState();

        void writeState(PersistedState state);

        FrozenBootstrapPlan resolvePlanSnapshot();

        void materializeCells(FrozenBootstrapPlan frozenPlan);

        Object resolvePlatformCenterAfterSampling(FrozenBootstrapPlan frozenPlan);

        void updateCenter(Object center);

        void createInitialPlatform(Object center);

        void spawnLandSpirit(Object center);

        void finalizeWorldData();
    }
}
