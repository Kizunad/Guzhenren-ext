# NPC Testing Framework Refactoring Plan

> **Based on**: `docs/Testing/TestFramework_Issues_Report.md`
> **Target**: Robust, Scalable, and Deterministic NPC Testing

---

## 📅 Phase 1: Stabilization (Weeks 1-2)
**Goal**: Fix current test failures and establish a reliable baseline.

### 1.1 Fix Critical GameTest Failures
- [ ] **Refactor `testTheGatherer` & `testTheCourier`**
    - [ ] Remove conflicting `succeedOnTickWhen` clauses.
    - [ ] Implement a single `succeedWhen` with internal state tracking or tick counting.
- [ ] **Fix `testPerformanceStress`**
    - [ ] Replace `succeedOnTickWhen` with manual `helper.onEachTick` counting to ensure precise timing.
- [ ] **Standardize Entity Initialization**
    - [ ] Ensure all test entities have `NpcMind` explicitly attached.
    - [ ] Create a shared `TestEntityFactory` to handle spawning with correct attributes and attachments.

### 1.2 Create Essential Test Utilities
- [ ] **Develop `NpcTestHelper`**
    - [ ] `spawnNPCWithMind(helper, pos)`: Factory method for initialized NPCs.
    - [ ] `waitForCondition(helper, condition, timeout)`: Polling mechanism for async states.
    - [ ] `assertGoal(npc, goalClass)`: Helper assertion for current goal.

---

## 🚀 Phase 2: Enhancement (Months 1-2)
**Goal**: Enable testing of complex, asynchronous behaviors and improve data management.

### 2.1 Asynchronous Testing Support
- [ ] **Implement Async Assertions**
    - [ ] Create a wrapper around `GameTestHelper` to support `await().atMost(time).until(condition)`.
    - [ ] Support testing of multi-tick actions (e.g., pathfinding, long interactions).
- [ ] **Deterministic Behavior Control**
    - [ ] Inject fixed Random seeds into `NpcMind` and `GoalSelector` during tests.
    - [ ] Disable time-based priority noise in test mode to ensure reproducible goal selection.

### 2.2 Test Data Management
- [ ] **Migrate Structure Files**
    - [ ] Convert binary `.nbt` structure files to `.snbt` for version control.
    - [ ] Standardize naming convention: `customnpcs.test.[testname].snbt`.
- [ ] **Environment Builder DSL**
    - [ ] Create a fluent API for defining test scenarios in code (reducing reliance on complex structure files for simple tests).

---

## 🛠️ Phase 3: Framework Evolution (Months 2-3)
**Goal**: Build a dedicated testing framework and performance monitoring.

### 3.1 Custom NPC Test Framework
- [ ] **Core Architecture**
    - [ ] `TestRunner`: Custom runner extending/wrapping GameTest to provide lifecycle hooks (`beforeEach`, `afterEach`).
    - [ ] `TestContext`: Isolated context for each test execution.
- [ ] **Advanced Assertions**
    - [ ] `NPCAssertions`: Fluent assertions for inventory, memory, and emotional state.
    - [ ] `GoalAssertions`: Verify goal history and sub-goal completion.

### 3.2 Performance Monitoring
- [ ] **Integrate Profiling**
    - [ ] Add `Profiler` hooks into `SensorSystem` and `GoalPlanner`.
    - [ ] Create `@PerformanceTest` annotation for automated regression testing.
    - [ ] Generate basic performance reports (execution time, memory delta).

---

## 🔮 Phase 4: Advanced Capabilities (Months 3-6)
**Goal**: Multi-entity coordination and deep observability.

### 4.1 Complex Scenario Testing
- [ ] **Multi-NPC Coordination**
    - [ ] Implement synchronization primitives for testing multiple NPCs (e.g., `Barrier`).
    - [ ] Test scenarios for resource sharing and role distribution.
- [ ] **Dynamic Environment Tests**
    - [ ] Simulate world events (block breaks, weather) and verify NPC adaptation.

### 4.2 Observability & Debugging
- [ ] **Decision Tracing**
    - [ ] Record "Black Box" data: Input Sensors -> Decision Logic -> Output Goal.
    - [ ] Generate visual traces for failed tests.
- [ ] **Visual Debugger**
    - [ ] (Optional) Web-based or In-Game UI to step through recorded test sessions.

---

## 📋 Action Items Checklist

### Immediate Actions
- [ ] Create `docs/customNPCs/testing/best_practices.md` to document the new `NpcTestHelper` usage.
- [ ] Refactor `ComplexScenarios.java` to use the new patterns.

### Review Schedule
- **Weekly**: Review test stability and flaky test rates.
- **Monthly**: Assess progress on Framework Evolution (Phase 3).
