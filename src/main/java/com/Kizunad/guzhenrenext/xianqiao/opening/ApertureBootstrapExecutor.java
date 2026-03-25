package com.Kizunad.guzhenrenext.xianqiao.opening;

import com.Kizunad.guzhenrenext.xianqiao.data.ApertureInitPhase;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureInitializationState;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureOpeningSnapshot;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureWorldData;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class ApertureBootstrapExecutor {

    private static final int MIN_LAYOUT_VERSION = 1;

    public enum PhaseBoundary {
        PLAN_RESOLVED,
        CELLS_MATERIALIZED,
        CORE_PLATFORM_SPIRIT_SPAWNED,
        WORLD_DATA_FINALIZED
    }

    public record BootstrapInput(
        ApertureOpeningSnapshot openingSnapshot,
        InitialTerrainPlan plan,
        int layoutVersion,
        long planSeed
    ) {

        public BootstrapInput {
            Objects.requireNonNull(openingSnapshot, "openingSnapshot");
            Objects.requireNonNull(plan, "plan");
            if (layoutVersion < MIN_LAYOUT_VERSION) {
                throw new IllegalArgumentException("layoutVersion 必须大于等于 1");
            }
        }
    }

    public record ExecutionResult(ApertureInitPhase finalPhase, Set<PhaseBoundary> reachedBoundaries) {

        public ExecutionResult {
            Objects.requireNonNull(finalPhase, "finalPhase");
            reachedBoundaries = Set.copyOf(Objects.requireNonNull(reachedBoundaries, "reachedBoundaries"));
        }
    }

    public interface InitializationStateStore {

        ApertureInitializationState getInitializationState(UUID owner);

        void setInitializationState(UUID owner, ApertureInitializationState initializationState);
    }

    public interface PlanStore {

        void saveResolvedPlan(UUID owner, int layoutVersion, long planSeed, InitialTerrainPlan plan);

        Optional<InitialTerrainPlan> loadResolvedPlan(UUID owner, int layoutVersion, long planSeed);
    }

    public interface WorldMutationOperations {

        boolean isCellsMaterialized(UUID owner, InitialTerrainPlan plan);

        void materializeCells(UUID owner, InitialTerrainPlan plan);

        boolean isCorePlatformSpawned(UUID owner, InitialTerrainPlan plan);

        void spawnCenterPlatformCore(UUID owner, InitialTerrainPlan plan);

        boolean isSpiritSpawned(UUID owner, InitialTerrainPlan plan);

        void spawnSpirit(UUID owner, InitialTerrainPlan plan);

        boolean isWorldDataFinalized(UUID owner, InitialTerrainPlan plan);

        void finalizeWorldData(UUID owner, InitialTerrainPlan plan);
    }

    public static final class WorldDataStateStore implements InitializationStateStore {

        private final ApertureWorldData worldData;

        public WorldDataStateStore(ApertureWorldData worldData) {
            this.worldData = Objects.requireNonNull(worldData, "worldData");
        }

        @Override
        public ApertureInitializationState getInitializationState(UUID owner) {
            return worldData.getInitializationState(owner);
        }

        @Override
        public void setInitializationState(UUID owner, ApertureInitializationState initializationState) {
            worldData.setInitializationState(owner, initializationState);
        }
    }

    public ExecutionResult execute(
        UUID owner,
        BootstrapInput bootstrapInput,
        InitializationStateStore stateStore,
        PlanStore planStore,
        WorldMutationOperations worldMutationOperations
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(stateStore, "stateStore");
        Objects.requireNonNull(planStore, "planStore");
        Objects.requireNonNull(worldMutationOperations, "worldMutationOperations");

        EnumSet<PhaseBoundary> reachedBoundaries = EnumSet.noneOf(PhaseBoundary.class);
        ApertureInitializationState initializationState = normalizeState(stateStore.getInitializationState(owner));
        ApertureInitPhase initPhase = resolveInitPhase(initializationState);

        if (initPhase == ApertureInitPhase.UNINITIALIZED) {
            BootstrapInput requiredInput = Objects.requireNonNull(
                bootstrapInput,
                "首次执行必须提供冻结快照与确定性规划输入"
            );
            planStore.saveResolvedPlan(
                owner,
                requiredInput.layoutVersion(),
                requiredInput.planSeed(),
                requiredInput.plan()
            );
            ApertureInitializationState plannedState = new ApertureInitializationState(
                ApertureInitPhase.PLANNED,
                requiredInput.openingSnapshot(),
                requiredInput.layoutVersion(),
                requiredInput.planSeed()
            );
            stateStore.setInitializationState(owner, plannedState);
            reachedBoundaries.add(PhaseBoundary.PLAN_RESOLVED);
            initializationState = plannedState;
            initPhase = ApertureInitPhase.PLANNED;
        }

        if (initPhase == ApertureInitPhase.COMPLETED) {
            reachedBoundaries.add(PhaseBoundary.WORLD_DATA_FINALIZED);
            return new ExecutionResult(ApertureInitPhase.COMPLETED, reachedBoundaries);
        }

        PlanningMetadata planningMetadata = extractPlanningMetadata(initializationState);
        InitialTerrainPlan resolvedPlan = resolvePlan(owner, planningMetadata, bootstrapInput, planStore);

        if (initPhase == ApertureInitPhase.PLANNED) {
            ApertureInitializationState executingState = new ApertureInitializationState(
                ApertureInitPhase.EXECUTING,
                initializationState.openingSnapshot(),
                planningMetadata.layoutVersion(),
                planningMetadata.planSeed()
            );
            stateStore.setInitializationState(owner, executingState);
            initializationState = executingState;
        }

        materializeCellsIfNeeded(owner, resolvedPlan, worldMutationOperations);
        reachedBoundaries.add(PhaseBoundary.CELLS_MATERIALIZED);

        spawnCorePlatformAndSpiritIfNeeded(owner, resolvedPlan, worldMutationOperations);
        reachedBoundaries.add(PhaseBoundary.CORE_PLATFORM_SPIRIT_SPAWNED);

        finalizeWorldDataIfNeeded(owner, resolvedPlan, worldMutationOperations);
        reachedBoundaries.add(PhaseBoundary.WORLD_DATA_FINALIZED);

        ApertureInitializationState completedState = new ApertureInitializationState(
            ApertureInitPhase.COMPLETED,
            initializationState.openingSnapshot(),
            planningMetadata.layoutVersion(),
            planningMetadata.planSeed()
        );
        stateStore.setInitializationState(owner, completedState);
        return new ExecutionResult(ApertureInitPhase.COMPLETED, reachedBoundaries);
    }

    private static ApertureInitializationState normalizeState(ApertureInitializationState rawState) {
        if (rawState == null) {
            return new ApertureInitializationState(ApertureInitPhase.UNINITIALIZED, null, null, null);
        }
        ApertureInitPhase normalizedPhase = rawState.initPhase() == null
            ? ApertureInitPhase.UNINITIALIZED
            : rawState.initPhase();
        if (normalizedPhase == ApertureInitPhase.UNINITIALIZED) {
            return new ApertureInitializationState(ApertureInitPhase.UNINITIALIZED, null, null, null);
        }
        return new ApertureInitializationState(
            normalizedPhase,
            rawState.openingSnapshot(),
            rawState.layoutVersion(),
            rawState.planSeed()
        );
    }

    private static ApertureInitPhase resolveInitPhase(ApertureInitializationState initializationState) {
        if (initializationState.initPhase() == null) {
            return ApertureInitPhase.UNINITIALIZED;
        }
        return initializationState.initPhase();
    }

    private static PlanningMetadata extractPlanningMetadata(ApertureInitializationState initializationState) {
        Integer layoutVersion = initializationState.layoutVersion();
        Long planSeed = initializationState.planSeed();
        if (layoutVersion == null || layoutVersion.intValue() < MIN_LAYOUT_VERSION || planSeed == null) {
            throw new IllegalStateException("phase 已进入规划/执行态但缺少 layoutVersion 或 planSeed");
        }
        return new PlanningMetadata(layoutVersion.intValue(), planSeed.longValue());
    }

    private static InitialTerrainPlan resolvePlan(
        UUID owner,
        PlanningMetadata planningMetadata,
        BootstrapInput bootstrapInput,
        PlanStore planStore
    ) {
        Optional<InitialTerrainPlan> storedPlan = planStore.loadResolvedPlan(
            owner,
            planningMetadata.layoutVersion(),
            planningMetadata.planSeed()
        );
        if (storedPlan.isPresent()) {
            return storedPlan.get();
        }
        if (
            bootstrapInput != null
                && bootstrapInput.layoutVersion() == planningMetadata.layoutVersion()
                && bootstrapInput.planSeed() == planningMetadata.planSeed()
        ) {
            return bootstrapInput.plan();
        }
        throw new IllegalStateException("无法恢复冻结规划：PlanStore 缺少计划且未提供匹配的回放输入");
    }

    private static void materializeCellsIfNeeded(
        UUID owner,
        InitialTerrainPlan resolvedPlan,
        WorldMutationOperations worldMutationOperations
    ) {
        if (worldMutationOperations.isCellsMaterialized(owner, resolvedPlan)) {
            return;
        }
        worldMutationOperations.materializeCells(owner, resolvedPlan);
    }

    private static void spawnCorePlatformAndSpiritIfNeeded(
        UUID owner,
        InitialTerrainPlan resolvedPlan,
        WorldMutationOperations worldMutationOperations
    ) {
        if (!worldMutationOperations.isCorePlatformSpawned(owner, resolvedPlan)) {
            worldMutationOperations.spawnCenterPlatformCore(owner, resolvedPlan);
        }
        if (!worldMutationOperations.isSpiritSpawned(owner, resolvedPlan)) {
            worldMutationOperations.spawnSpirit(owner, resolvedPlan);
        }
    }

    private static void finalizeWorldDataIfNeeded(
        UUID owner,
        InitialTerrainPlan resolvedPlan,
        WorldMutationOperations worldMutationOperations
    ) {
        if (worldMutationOperations.isWorldDataFinalized(owner, resolvedPlan)) {
            return;
        }
        worldMutationOperations.finalizeWorldData(owner, resolvedPlan);
    }

    private record PlanningMetadata(int layoutVersion, long planSeed) {
    }
}
