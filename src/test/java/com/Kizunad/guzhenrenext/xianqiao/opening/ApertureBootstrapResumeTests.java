package com.Kizunad.guzhenrenext.xianqiao.opening;

import com.Kizunad.guzhenrenext.xianqiao.data.ApertureInitPhase;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureInitializationState;
import com.Kizunad.guzhenrenext.xianqiao.data.ApertureOpeningSnapshot;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ApertureBootstrapResumeTests {

    private static final int LAYOUT_VERSION = 4;

    private static final long PLAN_SEED = 20260325L;

    private static final int SCORE_80 = 80;

    private static final int SCORE_90 = 90;

    private static final String BIOME_PLAINS = "minecraft:plains";

    private final ApertureBootstrapExecutor executor = new ApertureBootstrapExecutor();

    @Test
    void repeatedExecutionAfterPartialCompletionMustNotDuplicateTerrainCoreSpiritOrFinalization() {
        UUID owner = UUID.randomUUID();
        InMemoryStateStore stateStore = new InMemoryStateStore();
        InMemoryPlanStore planStore = new InMemoryPlanStore();
        RecordingWorldMutations worldMutations = new RecordingWorldMutations();
        ApertureBootstrapExecutor.BootstrapInput bootstrapInput = bootstrapInput();

        worldMutations.failAfterCellsOnce = true;
        assertThrows(
            IllegalStateException.class,
            () -> executor.execute(owner, bootstrapInput, stateStore, planStore, worldMutations)
        );

        assertEquals(1, worldMutations.materializeCalls);
        assertEquals(0, worldMutations.spawnCoreCalls);
        assertEquals(0, worldMutations.spawnSpiritCalls);
        assertEquals(0, worldMutations.finalizeCalls);
        assertEquals(ApertureInitPhase.EXECUTING, stateStore.getInitializationState(owner).initPhase());

        ApertureBootstrapExecutor.ExecutionResult resumed = executor.execute(
            owner,
            null,
            stateStore,
            planStore,
            worldMutations
        );

        assertEquals(ApertureInitPhase.COMPLETED, resumed.finalPhase());
        assertEquals(1, worldMutations.materializeCalls);
        assertEquals(1, worldMutations.spawnCoreCalls);
        assertEquals(1, worldMutations.spawnSpiritCalls);
        assertEquals(1, worldMutations.finalizeCalls);
        assertEquals(ApertureInitPhase.COMPLETED, stateStore.getInitializationState(owner).initPhase());

        executor.execute(owner, null, stateStore, planStore, worldMutations);
        assertEquals(1, worldMutations.materializeCalls);
        assertEquals(1, worldMutations.spawnCoreCalls);
        assertEquals(1, worldMutations.spawnSpiritCalls);
        assertEquals(1, worldMutations.finalizeCalls);
    }

    @Test
    void resumeFromMidPhaseMustReachSameFinalResultAsUninterruptedExecution() {
        UUID owner = UUID.randomUUID();
        ApertureBootstrapExecutor.BootstrapInput bootstrapInput = bootstrapInput();

        InMemoryStateStore uninterruptedState = new InMemoryStateStore();
        InMemoryPlanStore uninterruptedPlanStore = new InMemoryPlanStore();
        RecordingWorldMutations uninterruptedWorld = new RecordingWorldMutations();
        executor.execute(owner, bootstrapInput, uninterruptedState, uninterruptedPlanStore, uninterruptedWorld);

        InMemoryStateStore resumedState = new InMemoryStateStore();
        InMemoryPlanStore resumedPlanStore = new InMemoryPlanStore();
        RecordingWorldMutations resumedWorld = new RecordingWorldMutations();
        resumedWorld.failAfterCoreSpawnOnce = true;

        assertThrows(
            IllegalStateException.class,
            () -> executor.execute(owner, bootstrapInput, resumedState, resumedPlanStore, resumedWorld)
        );
        assertEquals(ApertureInitPhase.EXECUTING, resumedState.getInitializationState(owner).initPhase());

        executor.execute(owner, null, resumedState, resumedPlanStore, resumedWorld);

        assertEquals(
            uninterruptedState.getInitializationState(owner),
            resumedState.getInitializationState(owner),
            "恢复执行后的 phase/snapshot/layoutVersion/planSeed 必须与不中断路径一致"
        );
        assertEquals(uninterruptedWorld.materializeCalls, resumedWorld.materializeCalls);
        assertEquals(uninterruptedWorld.spawnCoreCalls, resumedWorld.spawnCoreCalls);
        assertEquals(uninterruptedWorld.spawnSpiritCalls, resumedWorld.spawnSpiritCalls);
        assertEquals(uninterruptedWorld.finalizeCalls, resumedWorld.finalizeCalls);
        assertIterableEquals(uninterruptedWorld.events, resumedWorld.events);
    }

    @Test
    void executorMustUseTask5PhaseStateInsteadOfLegacyBooleanGate() {
        UUID owner = UUID.randomUUID();
        InMemoryStateStore stateStore = new InMemoryStateStore();
        InMemoryPlanStore planStore = new InMemoryPlanStore();
        RecordingWorldMutations worldMutations = new RecordingWorldMutations();
        ApertureBootstrapExecutor.BootstrapInput bootstrapInput = bootstrapInput();

        stateStore.setInitializationState(
            owner,
            new ApertureInitializationState(
                ApertureInitPhase.PLANNED,
                bootstrapInput.openingSnapshot(),
                bootstrapInput.layoutVersion(),
                bootstrapInput.planSeed()
            )
        );
        planStore.saveResolvedPlan(owner, bootstrapInput.layoutVersion(), bootstrapInput.planSeed(), bootstrapInput.plan());

        ApertureBootstrapExecutor.ExecutionResult result = executor.execute(
            owner,
            null,
            stateStore,
            planStore,
            worldMutations
        );

        Set<ApertureBootstrapExecutor.PhaseBoundary> expectedReached = EnumSet.of(
            ApertureBootstrapExecutor.PhaseBoundary.CELLS_MATERIALIZED,
            ApertureBootstrapExecutor.PhaseBoundary.CORE_PLATFORM_SPIRIT_SPAWNED,
            ApertureBootstrapExecutor.PhaseBoundary.WORLD_DATA_FINALIZED
        );
        assertEquals(ApertureInitPhase.COMPLETED, result.finalPhase());
        assertEquals(expectedReached, result.reachedBoundaries());
        assertEquals(0, worldMutations.planResolveSideEffectCounter);
        assertTrue(worldMutations.materializeCalls > 0);
    }

    @Test
    void persistedPlanAndPhaseMustSupportResumeAcrossNewStoreViews() {
        UUID owner = UUID.randomUUID();
        PersistentBootstrapStorage persistentStorage = new PersistentBootstrapStorage();
        ApertureBootstrapExecutor.BootstrapInput bootstrapInput = bootstrapInput();

        InMemoryStateStore firstRunStateStore = new InMemoryStateStore(persistentStorage.states);
        InMemoryPlanStore firstRunPlanStore = new InMemoryPlanStore(persistentStorage.plans);
        RecordingWorldMutations firstRunWorldMutations = new RecordingWorldMutations();
        firstRunWorldMutations.failAfterCellsOnce = true;

        assertThrows(
            IllegalStateException.class,
            () -> executor.execute(owner, bootstrapInput, firstRunStateStore, firstRunPlanStore, firstRunWorldMutations)
        );
        assertEquals(ApertureInitPhase.EXECUTING, firstRunStateStore.getInitializationState(owner).initPhase());
        assertTrue(firstRunPlanStore.loadResolvedPlan(owner, LAYOUT_VERSION, PLAN_SEED).isPresent());

        InMemoryStateStore resumedStateStore = new InMemoryStateStore(persistentStorage.states);
        InMemoryPlanStore resumedPlanStore = new InMemoryPlanStore(persistentStorage.plans);
        RecordingWorldMutations resumedWorldMutations = new RecordingWorldMutations();
        resumedWorldMutations.restoreCellsMaterialized();

        ApertureBootstrapExecutor.ExecutionResult resumedResult = executor.execute(
            owner,
            null,
            resumedStateStore,
            resumedPlanStore,
            resumedWorldMutations
        );

        assertEquals(ApertureInitPhase.COMPLETED, resumedResult.finalPhase());
        assertEquals(ApertureInitPhase.COMPLETED, resumedStateStore.getInitializationState(owner).initPhase());
        assertTrue(resumedPlanStore.loadResolvedPlan(owner, LAYOUT_VERSION, PLAN_SEED).isPresent());
        assertEquals(0, resumedWorldMutations.materializeCalls);
        assertEquals(1, resumedWorldMutations.spawnCoreCalls);
        assertEquals(1, resumedWorldMutations.spawnSpiritCalls);
        assertEquals(1, resumedWorldMutations.finalizeCalls);
    }

    private static ApertureBootstrapExecutor.BootstrapInput bootstrapInput() {
        return new ApertureBootstrapExecutor.BootstrapInput(snapshot(), plan(), LAYOUT_VERSION, PLAN_SEED);
    }

    private static ApertureOpeningSnapshot snapshot() {
        return new ApertureOpeningSnapshot(5, 5, SCORE_80, SCORE_80, SCORE_80, SCORE_90, true, true);
    }

    private static InitialTerrainPlan plan() {
        List<InitialTerrainPlan.PlannedTerrainCell> cells = List.of(
            new InitialTerrainPlan.PlannedTerrainCell(
                0,
                0,
                0,
                0,
                0,
                0,
                true,
                BIOME_PLAINS,
                List.of(BIOME_PLAINS)
            )
        );
        return new InitialTerrainPlan(
            InitialTerrainPlan.LayoutTier.ONE_BY_ONE,
            1,
            1,
            new InitialTerrainPlan.CoreAnchor(0.5, 0.5, InitialTerrainPlan.CoreAnchorSemantics.ODD_CENTER_CELL),
            new InitialTerrainPlan.TeleportAnchor(
                0,
                0,
                0,
                0,
                0.5,
                0.5,
                InitialTerrainPlan.TeleportAnchorSemantics.ODD_CENTER_CELL
            ),
            new InitialTerrainPlan.LayoutOrigin(0, 0, InitialTerrainPlan.LayoutOriginSemantics.NORTHWEST_CORNER_CHUNK),
            new InitialTerrainPlan.InitialChunkBoundary(0, 0, 0, 0),
            new InitialTerrainPlan.RingParameters(16, 8, 16, 17),
            BiomeInferenceService.BiomeFallbackPolicy.STABLE_HASH_POOL,
            cells
        );
    }

    private static final class InMemoryStateStore implements ApertureBootstrapExecutor.InitializationStateStore {

        private final Map<UUID, ApertureInitializationState> states;

        private InMemoryStateStore() {
            this(new HashMap<>());
        }

        private InMemoryStateStore(Map<UUID, ApertureInitializationState> states) {
            this.states = states;
        }

        @Override
        public ApertureInitializationState getInitializationState(UUID owner) {
            return states.getOrDefault(owner, new ApertureInitializationState(ApertureInitPhase.UNINITIALIZED, null,
                null, null));
        }

        @Override
        public void setInitializationState(UUID owner, ApertureInitializationState initializationState) {
            states.put(owner, initializationState);
        }
    }

    private static final class InMemoryPlanStore implements ApertureBootstrapExecutor.PlanStore {

        private final Map<PlanKey, InitialTerrainPlan> plans;

        private InMemoryPlanStore() {
            this(new HashMap<>());
        }

        private InMemoryPlanStore(Map<PlanKey, InitialTerrainPlan> plans) {
            this.plans = plans;
        }

        @Override
        public void saveResolvedPlan(UUID owner, int layoutVersion, long planSeed, InitialTerrainPlan plan) {
            plans.put(new PlanKey(owner, layoutVersion, planSeed), plan);
        }

        @Override
        public Optional<InitialTerrainPlan> loadResolvedPlan(UUID owner, int layoutVersion, long planSeed) {
            return Optional.ofNullable(plans.get(new PlanKey(owner, layoutVersion, planSeed)));
        }
    }

    private static final class RecordingWorldMutations implements ApertureBootstrapExecutor.WorldMutationOperations {

        private boolean cellsMaterialized;
        private boolean coreSpawned;
        private boolean spiritSpawned;
        private boolean worldDataFinalized;
        private int materializeCalls;
        private int spawnCoreCalls;
        private int spawnSpiritCalls;
        private int finalizeCalls;
        private int planResolveSideEffectCounter;
        private boolean failAfterCellsOnce;
        private boolean failAfterCoreSpawnOnce;
        private final List<String> events = new ArrayList<>();

        private void restoreCellsMaterialized() {
            cellsMaterialized = true;
        }

        @Override
        public boolean isCellsMaterialized(UUID owner, InitialTerrainPlan plan) {
            return cellsMaterialized;
        }

        @Override
        public void materializeCells(UUID owner, InitialTerrainPlan plan) {
            materializeCalls++;
            cellsMaterialized = true;
            events.add("cells");
            if (failAfterCellsOnce) {
                failAfterCellsOnce = false;
                throw new IllegalStateException("模拟 cells 落地后中断");
            }
        }

        @Override
        public boolean isCorePlatformSpawned(UUID owner, InitialTerrainPlan plan) {
            return coreSpawned;
        }

        @Override
        public void spawnCenterPlatformCore(UUID owner, InitialTerrainPlan plan) {
            spawnCoreCalls++;
            coreSpawned = true;
            events.add("core");
            if (failAfterCoreSpawnOnce) {
                failAfterCoreSpawnOnce = false;
                throw new IllegalStateException("模拟 core 落地后中断");
            }
        }

        @Override
        public boolean isSpiritSpawned(UUID owner, InitialTerrainPlan plan) {
            return spiritSpawned;
        }

        @Override
        public void spawnSpirit(UUID owner, InitialTerrainPlan plan) {
            spawnSpiritCalls++;
            spiritSpawned = true;
            events.add("spirit");
        }

        @Override
        public boolean isWorldDataFinalized(UUID owner, InitialTerrainPlan plan) {
            return worldDataFinalized;
        }

        @Override
        public void finalizeWorldData(UUID owner, InitialTerrainPlan plan) {
            finalizeCalls++;
            worldDataFinalized = true;
            events.add("finalize");
        }
    }

    private record PlanKey(UUID owner, int layoutVersion, long planSeed) {
    }

    private static final class PersistentBootstrapStorage {

        private final Map<UUID, ApertureInitializationState> states = new HashMap<>();

        private final Map<PlanKey, InitialTerrainPlan> plans = new HashMap<>();
    }
}
