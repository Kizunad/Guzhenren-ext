package com.Kizunad.guzhenrenext.xianqiao.opening;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ApertureBootstrapResumeTests {

    private static final int LAYOUT_VERSION = 1;

    private static final long PLAN_SEED = 2026032607L;

    private static final TestCenter RESOLVED_CENTER = new TestCenter(160, 96, 32);

    @Test
    void retryFromExecutingMustConvergeWithoutRematerializeOrDoubleFinalize() {
        ApertureBootstrapExecutor executor = new ApertureBootstrapExecutor();
        AscensionConditionSnapshot frozenSnapshot = snapshot("benming:task7-retry");
        TestContext context = TestContext.uninitialized(
            frozenSnapshot,
            LAYOUT_VERSION,
            PLAN_SEED,
            RESOLVED_CENTER
        );
        context.failUpdateCenterOnce = true;

        assertThrows(RuntimeException.class, () -> executor.execute(context));
        assertEquals(ApertureBootstrapExecutor.BootstrapPhase.EXECUTING, context.state.phase());
        assertEquals(1, context.materializeCalls);
        assertEquals(0, context.finalizeCalls);
        assertEquals(1, context.resolvePlanCalls);

        executor.execute(context);
        assertEquals(ApertureBootstrapExecutor.BootstrapPhase.COMPLETED, context.state.phase());
        assertEquals(1, context.materializeCalls);
        assertEquals(1, context.finalizeCalls);
        assertEquals(1, context.spawnSpiritCalls);
        assertEquals(1, context.resolvePlanCalls);

        executor.execute(context);
        assertEquals(ApertureBootstrapExecutor.BootstrapPhase.COMPLETED, context.state.phase());
        assertEquals(1, context.materializeCalls);
        assertEquals(1, context.finalizeCalls);
        assertEquals(1, context.spawnSpiritCalls);
    }

    @Test
    void plannedWithFrozenStateMustResumeWithoutResolvingNewPlan() {
        ApertureBootstrapExecutor executor = new ApertureBootstrapExecutor();
        AscensionConditionSnapshot frozenSnapshot = snapshot("benming:task7-planned");
        InitialTerrainPlan plannedLayout = testPlan();
        ApertureBootstrapExecutor.PersistedState planned = new ApertureBootstrapExecutor.PersistedState(
            ApertureBootstrapExecutor.BootstrapPhase.PLANNED,
            frozenSnapshot,
            Integer.valueOf(LAYOUT_VERSION),
            Long.valueOf(PLAN_SEED)
        );
        TestContext context = TestContext.withState(
            planned,
            frozenSnapshot,
            LAYOUT_VERSION,
            PLAN_SEED,
            RESOLVED_CENTER,
            plannedLayout
        );

        executor.execute(context);

        assertEquals(ApertureBootstrapExecutor.BootstrapPhase.COMPLETED, context.state.phase());
        assertEquals(0, context.resolvePlanCalls);
        assertEquals(1, context.materializeCalls);
        assertEquals(1, context.spawnSpiritCalls);
        assertSame(frozenSnapshot, context.state.openingSnapshot());
        assertEquals(plannedLayout, context.materializedPlan);
    }

    @Test
    void executingWithFrozenStateMustSkipMaterializeAndComplete() {
        ApertureBootstrapExecutor executor = new ApertureBootstrapExecutor();
        AscensionConditionSnapshot frozenSnapshot = snapshot("benming:task7-executing");
        InitialTerrainPlan plannedLayout = testPlan();
        ApertureBootstrapExecutor.PersistedState executing = new ApertureBootstrapExecutor.PersistedState(
            ApertureBootstrapExecutor.BootstrapPhase.EXECUTING,
            frozenSnapshot,
            Integer.valueOf(LAYOUT_VERSION),
            Long.valueOf(PLAN_SEED)
        );
        TestContext context = TestContext.withState(
            executing,
            frozenSnapshot,
            LAYOUT_VERSION,
            PLAN_SEED,
            RESOLVED_CENTER,
            plannedLayout
        );

        executor.execute(context);

        assertEquals(ApertureBootstrapExecutor.BootstrapPhase.COMPLETED, context.state.phase());
        assertEquals(0, context.materializeCalls);
        assertEquals(1, context.spawnSpiritCalls);
        assertEquals(1, context.finalizeCalls);
    }

    @Test
    void frozenPlanMayCarryDeterministicLayoutForMainMaterializePath() {
        ApertureBootstrapExecutor executor = new ApertureBootstrapExecutor();
        AscensionConditionSnapshot frozenSnapshot = snapshot("benming:task11-layout");
        InitialTerrainPlan plannedLayout = testPlan();
        TestContext context = TestContext.uninitialized(
            frozenSnapshot,
            LAYOUT_VERSION,
            PLAN_SEED,
            RESOLVED_CENTER,
            plannedLayout
        );

        executor.execute(context);

        assertEquals(ApertureBootstrapExecutor.BootstrapPhase.COMPLETED, context.state.phase());
        assertEquals(1, context.materializeCalls);
        assertEquals(plannedLayout, context.materializedPlan);
        assertEquals(1, context.resolvePlanCalls);
    }

    private static AscensionConditionSnapshot snapshot(String token) {
        return new AscensionConditionSnapshot(
            1.0D,
            AscensionConditionSnapshot.BenmingGuFallbackState.RESOLVED,
            token,
            Map.of("tudao", 100.0D),
            AscensionConditionSnapshot.DaoMarkCoverageState.COMPLETE,
            100.0D,
            100.0D,
            AscensionConditionSnapshot.AptitudeResourceState.HEALTHY,
            90.0D,
            90.0D,
            80.0D,
            100.0D,
            80.0D,
            100.0D,
            70.0D,
            5.0D,
            5.0D,
            8.0D,
            12.0D,
            20.0D,
            30.0D,
            40.0D,
            50.0D,
            60.0D,
            70.0D,
            true
        );
    }

    private static InitialTerrainPlan testPlan() {
        InitialTerrainPlan.AnchorPoint seamCenter = new InitialTerrainPlan.AnchorPoint(160, 90, 32);
        InitialTerrainPlan.AnchorPoint layoutOrigin = new InitialTerrainPlan.AnchorPoint(144, 90, 16);
        InitialTerrainPlan.PlannedCell first = new InitialTerrainPlan.PlannedCell(
            1,
            0,
            0,
            0,
            layoutOrigin,
            new InitialTerrainPlan.ChunkCoord(9, 1),
            List.of(BiomeInferenceService.BiomeKey.minecraft("desert"))
        );
        InitialTerrainPlan.PlannedCell second = new InitialTerrainPlan.PlannedCell(
            2,
            0,
            0,
            1,
            layoutOrigin.offset(16, 0, 0),
            new InitialTerrainPlan.ChunkCoord(10, 1),
            List.of(BiomeInferenceService.BiomeKey.minecraft("plains"))
        );
        InitialTerrainPlan.PlannedCell third = new InitialTerrainPlan.PlannedCell(
            3,
            0,
            1,
            0,
            layoutOrigin.offset(0, 0, 16),
            new InitialTerrainPlan.ChunkCoord(9, 2),
            List.of(BiomeInferenceService.BiomeKey.minecraft("savanna"))
        );
        InitialTerrainPlan.PlannedCell fourth = new InitialTerrainPlan.PlannedCell(
            4,
            0,
            1,
            1,
            layoutOrigin.offset(16, 0, 16),
            new InitialTerrainPlan.ChunkCoord(10, 2),
            List.of(BiomeInferenceService.BiomeKey.minecraft("badlands"))
        );
        return new InitialTerrainPlan(
            InitialTerrainPlan.LayoutTier.TWO_BY_TWO,
            seamCenter,
            layoutOrigin,
            seamCenter,
            seamCenter.offset(0, 1, 0),
            new InitialTerrainPlan.ChunkBoundary(9, 10, 1, 2),
            new InitialTerrainPlan.ZoneParameters(0, 8, 16, 16),
            List.of(first, second, third, fourth)
        );
    }

    private static final class TestContext implements ApertureBootstrapExecutor.ExecutionContext {

        private final ApertureBootstrapExecutor.FrozenBootstrapPlan resolvedPlan;

        private final TestCenter resolvedCenter;

        private ApertureBootstrapExecutor.PersistedState state;

        private int resolvePlanCalls;

        private int materializeCalls;

        private int updateCenterCalls;

        private int createPlatformCalls;

        private int spawnSpiritCalls;

        private int finalizeCalls;

        private InitialTerrainPlan materializedPlan;

        private boolean failUpdateCenterOnce;

        private TestContext(
            ApertureBootstrapExecutor.PersistedState state,
            AscensionConditionSnapshot snapshot,
            int layoutVersion,
            long planSeed,
            TestCenter resolvedCenter,
            InitialTerrainPlan initialTerrainPlan
        ) {
            this.state = state;
            this.resolvedPlan = new ApertureBootstrapExecutor.FrozenBootstrapPlan(
                snapshot,
                layoutVersion,
                planSeed,
                initialTerrainPlan
            );
            this.resolvedCenter = resolvedCenter;
        }

        private static TestContext uninitialized(
            AscensionConditionSnapshot snapshot,
            int layoutVersion,
            long planSeed,
            TestCenter resolvedCenter
        ) {
            return uninitialized(snapshot, layoutVersion, planSeed, resolvedCenter, null);
        }

        private static TestContext uninitialized(
            AscensionConditionSnapshot snapshot,
            int layoutVersion,
            long planSeed,
            TestCenter resolvedCenter,
            InitialTerrainPlan initialTerrainPlan
        ) {
            return new TestContext(
                ApertureBootstrapExecutor.PersistedState.uninitialized(),
                snapshot,
                layoutVersion,
                planSeed,
                resolvedCenter,
                initialTerrainPlan
            );
        }

        private static TestContext withState(
            ApertureBootstrapExecutor.PersistedState state,
            AscensionConditionSnapshot snapshot,
            int layoutVersion,
            long planSeed,
            TestCenter resolvedCenter,
            InitialTerrainPlan initialTerrainPlan
        ) {
            return new TestContext(state, snapshot, layoutVersion, planSeed, resolvedCenter, initialTerrainPlan);
        }

        @Override
        public ApertureBootstrapExecutor.PersistedState readState() {
            return state;
        }

        @Override
        public void writeState(ApertureBootstrapExecutor.PersistedState state) {
            this.state = state;
        }

        @Override
        public ApertureBootstrapExecutor.FrozenBootstrapPlan resolvePlanSnapshot() {
            resolvePlanCalls++;
            return resolvedPlan;
        }

        @Override
        public void materializeCells(ApertureBootstrapExecutor.FrozenBootstrapPlan frozenPlan) {
            materializeCalls++;
            if (frozenPlan.hasInitialTerrainPlan()) {
                materializedPlan = frozenPlan.initialTerrainPlan();
            } else {
                materializedPlan = resolvedPlan.initialTerrainPlan();
            }
        }

        @Override
        public Object resolvePlatformCenterAfterSampling(ApertureBootstrapExecutor.FrozenBootstrapPlan frozenPlan) {
            return resolvedCenter;
        }

        @Override
        public void updateCenter(Object center) {
            assertEquals(resolvedCenter, center);
            updateCenterCalls++;
            if (failUpdateCenterOnce) {
                failUpdateCenterOnce = false;
                throw new RuntimeException("fail once");
            }
        }

        @Override
        public void createInitialPlatform(Object center) {
            assertEquals(resolvedCenter, center);
            createPlatformCalls++;
        }

        @Override
        public void spawnLandSpirit(Object center) {
            assertEquals(resolvedCenter, center);
            spawnSpiritCalls++;
        }

        @Override
        public void finalizeWorldData() {
            finalizeCalls++;
            state = new ApertureBootstrapExecutor.PersistedState(
                ApertureBootstrapExecutor.BootstrapPhase.COMPLETED,
                state.openingSnapshot(),
                state.layoutVersion(),
                state.planSeed()
            );
        }
    }

    private record TestCenter(int x, int y, int z) {
    }
}
