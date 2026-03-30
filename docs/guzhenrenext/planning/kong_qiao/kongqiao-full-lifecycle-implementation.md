# Kongqiao Full Lifecycle Implementation Plan

## TL;DR

> **Summary**: Turn kongqiao from a collection of working modules into a fully landable lifecycle system with explicit entry state, aptitude-based capacity, pressure and fatigue ownership, runtime enforcement, recovery flow, UI projection, and real verification. The goal is not to rewrite kongqiao, but to connect the already-existing menus, unlock flow, passive runtime, skill wheel, shazhao, and sync layers into one coherent long-term gameplay spine.
>
> **Deliverables**:
> - One end-to-end kongqiao lifecycle scope with explicit state ownership
> - Kongqiao pressure / overload / recovery runtime integrated into existing services
> - Clear split between unlock state, preference state, runtime active state, and stability state
> - UI updates across Kongqiao / NianTou / Tweak / Skill Wheel for pressure-aware operation
> - Verification plan covering relog, sync, unlock, active trigger, overload, and recovery
> **Effort**: XL
> **Parallel**: YES - 4 waves
> **Critical Path**: Task 1 → Task 2 → Task 3 → Task 5 → Task 6 → Task 8 → Task 10

## Context

### Original Request
- Focus on kongqiao first.
- Plan the full lifecycle thoroughly.
- Ensure the system can truly land end-to-end instead of stopping at isolated mechanics.

### Current Repo Reality
- Kongqiao already has substantial infrastructure:
  - attachment ownership
  - menu / UI entry
  - niantou identification
  - passive runtime
  - shazhao unlock + active/passive execution
  - sync payloads
- The largest missing pieces are not base framework, but lifecycle cohesion:
  - formal gameplay entry into kongqiao
  - aptitude-based capacity ownership
  - pressure / overload / fatigue runtime truth
  - recovery semantics
  - end-to-end verification

### Scope Decision
- Bastion is irrelevant to this plan.
- This plan is kongqiao-only.
- Xianqiao / ascension interactions may be referenced as upstream/downstream dependencies, but not expanded into a broader redesign here.

---

## Core Objective

Establish kongqiao as a complete gameplay lifecycle with one coherent implementation path covering:

1. entry / activation
2. capacity foundation
3. niantou unlock progression
4. passive runtime
5. skill wheel and active usage
6. shazhao derivation and activation
7. pressure / overload / fatigue
8. recovery
9. UI and sync
10. verification

---

## Must Have

- Kongqiao lifecycle is defined as a single end-to-end system, not separate feature notes.
- Pressure / overload / fatigue become server-authoritative state.
- Capacity is split into aptitude foundation + cultivation expansion.
- `TweakConfig` remains player preference only; it must not become the source of runtime truth.
- `NianTouUnlocks` remains unlock-state owner; runtime active state must stay separate.
- Existing unlock / UI / sync chains must be reused, not replaced without evidence.
- Every implementation slice includes verification.

## Must NOT Have

- No full rewrite of niantou / shazhao / skill wheel.
- No UI-local derivation of pressure truth.
- No pressure logic hidden in client-only screens.
- No expansion into unrelated flying sword or bastion scope.
- No human-only QA.

---

## Authoritative State Model

## 1. Ownership Layers

### 1.1 Kongqiao base owner
- `KongqiaoOwner`
- `KongqiaoData`

### 1.2 Unlock-state owner
- `NianTouUnlocks`
  - unlocked usages
  - unlocked shazhao
  - active identification process
  - derive message

### 1.3 Preference-state owner
- `TweakConfig`
  - passive enabled/disabled preference
  - wheel skill list/order

### 1.4 Runtime-active owner
- `ActivePassives`
  - currently active usage ids only

### 1.5 New stability-state owner
- add a new pressure/stability state under `KongqiaoData`
- recommended minimum fields:
  - `burstPressure`
  - `fatigueDebt`
  - `overloadTier`
  - `forcedDisabledUsageIds`
  - `sealedSlots`
  - `lastDecayGameTime`

### 1.6 New authoritative bridge owners (must be defined before capacity work)

The lifecycle plan may not consume aptitude and aperture-rank values from ad-hoc UI guesses or duplicated formulas.

Before any capacity/pressure implementation starts, define two canonical bridges:

- `KongqiaoAptitudeBridge`
  - sole owner for translating upstream aptitude signals into kongqiao gameplay aptitude tier
- `KongqiaoRealmBridge` or `KongqiaoApertureRankBridge`
  - sole owner for translating upstream progression into kongqiao aperture-rank / realm gap semantics

These bridges must explicitly document:

- upstream source file(s)
- fallback behavior when upstream data is incomplete
- server-only authority rule
- what downstream services may read from them

## 2. Key Rule

If a developer cannot answer “is this unlock state, preference state, active runtime state, or stability state?”, the implementation is drifting and must stop.

---

## Lifecycle Definition

## Stage A — Kongqiao Entry

### Current reality
- `KongqiaoAttachmentEvents` automatically creates the attachment for players on join.

### Required implementation target
- Distinguish technical attachment existence from gameplay activation.
- Add a gameplay-visible kongqiao entry/activation state.
- Define what unlocks the player-facing kongqiao loop.

### Landable requirement
- Menus and services may continue using attachments internally, but gameplay-facing availability must be gated by explicit state.

## Stage B — Capacity Establishment

### Current anchor
- `KongqiaoCapacityService`
- `KongqiaoSettings`
- `KongqiaoConstants`

### Required implementation target
- Replace “max zhenyuan directly equals total capacity” with:
  - aptitude base rows
  - zhenyuan/cultivation bonus rows

### Hard dependency
- Stage B cannot start until the plan defines the authoritative bridge for:
  - aptitude tier
  - aperture rank / realm gap

### Downstream effect
- `KongqiaoScreen` and sync payload must reflect this split clearly.

## Stage C — Niantou Identification

### Current anchor
- `NianTouMenu`
- `NianTouTickHandler`
- `NianTouUnlocks`

### Required implementation target
- Keep the current identification process but integrate:
  - pressure-aware cost logic
  - pause / failure / blocked messaging
  - better lifecycle visibility in UI

## Stage D — Passive Runtime

### Current anchor
- `GuRunningService`
- `ActivePassives`
- `TweakConfig`

### Required implementation target
- Before passive execution:
  - compute resident/passive pressure
  - determine overload tier
  - auto-disable unstable passives by priority

## Stage E — Shazhao Derivation and Passive Shazhao

### Current anchor
- `ShazhaoUnlockService`
- `ShazhaoRunningService`
- `NianTouScreen`

### Required implementation target
- Derivation must add fatigue debt
- Shazhao passive upkeep must count toward kongqiao pressure

## Stage F — Skill Wheel and Active Trigger

### Current anchor
- `TweakScreen`
- `SkillWheelScreen`
- `ServerboundSkillWheelSelectPayload`
- `GuRunningService.activateEffectWithResult`
- `ShazhaoActiveService`

### Required implementation target
- Wheel preload cost
- Active burst pressure
- explicit `PRESSURE_LIMIT` failure result
- high-overload restrictions on new wheel additions and active triggers

## Stage G — Overload, Collapse, Recovery

### Current reality
- Currently missing as a full chain.

### Required implementation target
- define overload tiers
- define passive shutdown order
- define sealed slot behavior
- define fatigue decay
- define recovery path and player-facing expectations

## Stage H — Sync and Projection

### Current anchor
- `KongqiaoSyncService`
- `ClientboundKongqiaoSyncPayload`
- `KongqiaoSyncClientHandler`
- `PacketSyncKongqiaoData`

### Required implementation target
- new stability state must sync through the same server-authoritative pathway
- no UI should infer overload locally

### Mandatory synced projection

In addition to persisted raw state, the server must project a computed pressure snapshot for client/UI use. The client must not reconstruct these numbers.

Minimum server-produced projection fields:

- `totalPressure`
- `pressureCap`
- `residentPressure`
- `passivePressure`
- `wheelReservePressure`
- `burstPressure`
- `fatigueDebt`
- `overloadTier`
- `blockedReason`
- `sealedSlotCount`

Recommended ownership:

- raw persisted state lives in `KongqiaoData`
- derived projection is computed by a dedicated server-side pressure service
- projection is serialized into the existing kongqiao sync path and rendered directly by UI

---

## Execution Strategy

### Wave 1 — State Boundary Lock
1. Add kongqiao lifecycle scope doc and implementation plan
2. Introduce kongqiao activation state concept
3. Introduce pressure/stability state model under `KongqiaoData`

### Wave 2 — Capacity + Pressure Computation
4. Split capacity into aptitude base + zhenyuan bonus
5. Add server-side pressure computation service and authoritative projection snapshot
6. Sync new state to client

### Wave 3 — Runtime Enforcement
7. Integrate passive runtime pressure enforcement in `GuRunningService`
8. Integrate active trigger pressure enforcement in `GuRunningService` and `ShazhaoActiveService`
9. Add derivation fatigue debt in `NianTouMenu` / `ShazhaoUnlockService` path

### Wave 4 — UI + Recovery + Verification
10. Update Kongqiao / NianTou / Tweak / Skill Wheel UI projections
11. Add overload / recovery behavior and persistence guarantees
12. Add verification suite and evidence

---

## TODOs

### 1. Freeze kongqiao lifecycle scope and state ownership

**What to do**: Document and codify the split between attachment existence, gameplay activation, unlock state, preference state, runtime active state, and stability state.

**References**:
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/KongqiaoOwner.java`
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/attachment/KongqiaoData.java`
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/attachment/NianTouUnlocks.java`
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/attachment/TweakConfig.java`
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/attachment/ActivePassives.java`

**Acceptance Criteria**:
- [ ] Each kongqiao state type has one owner.
- [ ] No state category is ambiguous.
- [ ] New stability state requirements are written before implementation.

### 2. Define and bridge kongqiao gameplay activation

**What to do**: Introduce an explicit gameplay activation concept so kongqiao is not purely “auto-granted and always ready”.

**References**:
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/attachment/KongqiaoAttachmentEvents.java`
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/service/KongqiaoService.java`

**Acceptance Criteria**:
- [ ] Technical attachment existence is distinguished from gameplay activation.
- [ ] UI/menu opening policy is documented against activation state.

### 3. Replace pure-zhenyuan capacity with aptitude+cultivation model

**What to do**: Rework capacity semantics while retaining current service structure.

**References**:
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/service/KongqiaoCapacityService.java`
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/inventory/KongqiaoSettings.java`
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/KongqiaoConstants.java`
- `docs/guzhenrenext/planning/kong_qiao/pressure.md`

**Acceptance Criteria**:
- [ ] Capacity model clearly distinguishes base vs bonus rows.
- [ ] UI can display both sources.
- [ ] The plan names the single authoritative bridge for aptitude tier.
- [ ] The plan names the single authoritative bridge for aperture-rank / realm gap.

**QA Scenarios**:
- [ ] Scenario: Capacity bridge resolves aptitude and rank from one source only
  - Tool: JUnit
  - Steps: Add/extend bridge tests to assert the same input snapshot always resolves to the same kongqiao aptitude tier and aperture-rank output.
  - Expected: No duplicate resolver path exists and no UI/service needs to derive aptitude or rank independently.
- [ ] Scenario: Capacity output distinguishes aptitude base from cultivation bonus
  - Tool: JUnit
  - Steps: Test several combinations of aptitude tier and max zhenyuan against resulting row counts.
  - Expected: Higher aptitude changes base rows even with identical zhenyuan; higher zhenyuan changes bonus rows without rewriting aptitude.

### 4. Add server-authoritative pressure/stability state to kongqiao data

**What to do**: Add persistent stability state to `KongqiaoData` and sync it.

**References**:
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/attachment/KongqiaoData.java`
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/service/KongqiaoSyncService.java`
- `src/main/java/com/Kizunad/guzhenrenext/network/ClientboundKongqiaoSyncPayload.java`

**Acceptance Criteria**:
- [ ] Stability state persists through relog/clone.
- [ ] Client receives new state through existing sync chain.
- [ ] A server-computed pressure projection snapshot exists for UI use.
- [ ] UI does not need to recompute total pressure or overload tier locally.

**QA Scenarios**:
- [ ] Scenario: Stability raw state survives clone/relog serialization
  - Tool: JUnit
  - Steps: Serialize/deserialize `KongqiaoData` with non-zero burst pressure, fatigue debt, overload tier, forced-disabled entries, and sealed slot info.
  - Expected: Restored state matches original values exactly.
- [ ] Scenario: Server sync includes computed projection fields
  - Tool: JUnit or payload contract test
  - Steps: Build a kongqiao state with known resident/passive/wheel/burst/fatigue inputs and assert the sync payload exposes the expected totals and breakdown fields.
  - Expected: Client can render authoritative totals without local recomputation.

### 5. Integrate pressure into passive runtime

**What to do**: Make passive execution pressure-aware and overload-aware.

**References**:
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/service/GuRunningService.java`
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/attachment/ActivePassives.java`
- `docs/guzhenrenext/planning/kong_qiao/limit.md`

**Acceptance Criteria**:
- [ ] Passive execution checks current stability before running.
- [ ] Overload can force-disable passives.
- [ ] Forced disable is not silently equivalent to preference toggle.

**QA Scenarios**:
- [ ] Scenario: Overload force-disables runtime passives without mutating player preference
  - Tool: JUnit
  - Steps: Set a passive enabled in `TweakConfig`, force overload in runtime state, tick `GuRunningService`, then inspect `ActivePassives` and preference state.
  - Expected: Runtime active set drops the passive, but `TweakConfig` still records the player preference as enabled.
- [ ] Scenario: Stable pressure allows passive execution
  - Tool: JUnit
  - Steps: Tick `GuRunningService` under non-overloaded projection with unlocked passive usages present.
  - Expected: Effect callbacks run and no forced disable markers are written.

### 6. Integrate fatigue into identification and shazhao derivation

**What to do**: Make long-form kongqiao operations create structural debt.

**References**:
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/menu/NianTouMenu.java`
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/handler/NianTouTickHandler.java`
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/shazhao/ShazhaoUnlockService.java`

**Acceptance Criteria**:
- [ ] Derivation/identification can contribute to stability debt where intended.
- [ ] UI can explain ongoing process cost and stability impact.

**QA Scenarios**:
- [ ] Scenario: Identification progress applies resource cost and fatigue debt together
  - Tool: JUnit
  - Steps: Start an unlock process, tick `NianTouTickHandler`, and inspect both resource deduction and updated fatigue/stability fields.
  - Expected: Progress advances only while costs are paid, and fatigue debt rises according to the configured rule.
- [ ] Scenario: Derive shazhao writes debt and unlock result coherently
  - Tool: JUnit
  - Steps: Trigger derive flow through `NianTouMenu` / `ShazhaoUnlockService` with deterministic random input.
  - Expected: Result message, unlock state, and fatigue update are mutually consistent.

### 7. Integrate pressure into active usage and shazhao activation

**What to do**: Add preload cost, burst pressure, and explicit pressure failure reasons.

**References**:
- `src/main/java/com/Kizunad/guzhenrenext/network/ServerboundSkillWheelSelectPayload.java`
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/service/GuRunningService.java`
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/service/ShazhaoActiveService.java`
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/network/ServerboundTweakConfigUpdatePayload.java`

**Acceptance Criteria**:
- [ ] Active trigger can fail with explicit pressure limit semantics.
- [ ] Wheel additions can be restricted by overload state.

**QA Scenarios**:
- [ ] Scenario: Pressure limit blocks active usage before effect execution
  - Tool: JUnit
  - Steps: Attempt `GuRunningService.activateEffectWithResult` under a projection that would exceed the allowed limit.
  - Expected: Result returns explicit pressure-limit failure and effect logic is not executed.
- [ ] Scenario: Pressure limit blocks shazhao activation with explicit reason
  - Tool: JUnit
  - Steps: Attempt `ShazhaoActiveService.activate` under overload with all normal unlock/material requirements satisfied.
  - Expected: Activation fails specifically due to pressure limit rather than condition mismatch.
- [ ] Scenario: Wheel modification obeys overload restriction
  - Tool: JUnit
  - Steps: Send `ServerboundTweakConfigUpdatePayload` add-wheel action while kongqiao is overloaded.
  - Expected: Server rejects the update and returns unchanged wheel configuration.

### 8. Project overload and stability to UI

**What to do**: Update screens to display current stability truth.

**References**:
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/client/ui/KongqiaoScreen.java`
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/client/ui/NianTouScreen.java`
- `src/main/java/com/Kizunad/guzhenrenext/kongqiao/client/ui/TweakScreen.java`
- `src/main/java/com/Kizunad/guzhenrenext/client/gui/SkillWheelScreen.java`

**Acceptance Criteria**:
- [ ] Pressure total and breakdown are visible.
- [ ] Overload tier is visible.
- [ ] Users can tell why a passive or active is blocked.

**QA Scenarios**:
- [ ] Scenario: KongqiaoScreen displays server-provided pressure totals and breakdown
  - Tool: UI projection test / JUnit screen state test
  - Steps: Feed a known projection snapshot into the screen model and assert displayed totals, cap, and breakdown strings.
  - Expected: Rendered values match the server snapshot exactly.
- [ ] Scenario: Tweak/SkillWheel UI surfaces blocked reason
  - Tool: UI projection test / JUnit
  - Steps: Provide a projection with a pressure-limit blocked reason and inspect rendered labels/buttons.
  - Expected: UI explains the blockage without inventing local fallback reasons.

### 9. Define overload consequences and recovery flow

**What to do**: Implement stable, persisted rules for overload penalties and recovery.

**References**:
- `docs/guzhenrenext/planning/kong_qiao/pressure.md`
- `docs/guzhenrenext/planning/kong_qiao/limit.md`

**Acceptance Criteria**:
- [ ] Sealed slots / fatigue decay / recovery behavior are explicitly defined.
- [ ] Recovery semantics persist across relog/restart.

**QA Scenarios**:
- [ ] Scenario: Recovery decay reduces fatigue without erasing unlock/preference state
  - Tool: JUnit
  - Steps: Simulate recovery ticks after overload and inspect fatigue, overload tier, unlocked usages, and tweak preferences.
  - Expected: Only stability values decay; unlock and preference data remain intact.
- [ ] Scenario: Sealed slots remain sealed across relog until recovery criteria are met
  - Tool: JUnit
  - Steps: Persist sealed slot state, deserialize, and advance recovery conditions.
  - Expected: Slot lock state is stable across reload and only clears through defined recovery rules.

### 10. Add end-to-end verification

**What to do**: Establish concrete verification targets for kongqiao lifecycle, even if current coverage is JUnit-heavy.

**Required Scenarios**:
- [ ] Kongqiao activation and menu availability
- [ ] Capacity recalculation and overflow handling
- [ ] Identification progression and unlock persistence
- [ ] Passive runtime under stable vs overloaded states
- [ ] Skill wheel / shazhao trigger with pressure limit rejection
- [ ] Relog/restart preservation of stability state
- [ ] Dedicated-server data sync for NianTou/Shazhao descriptions

**Evidence**:
- `.sisyphus/evidence/kongqiao-full-lifecycle-*.txt`

**QA Scenarios**:
- [ ] Scenario: Dedicated-server data sync makes niantou/shazhao descriptions visible after login
  - Tool: JUnit payload contract test + manual evidence if needed
  - Steps: Simulate login sync event, serialize `PacketSyncKongqiaoData`, and apply on client cache.
  - Expected: Client-side data managers contain the synchronized niantou/shazhao definitions.
- [ ] Scenario: Full lifecycle smoke path remains coherent
  - Tool: staged integration harness
  - Steps: Activate kongqiao gameplay, compute capacity, unlock one usage, run passive, derive one shazhao, add to wheel, hit pressure limit, recover, relog, and recheck state.
  - Expected: No split truth between unlocks, preferences, runtime actives, stability state, and UI projection.

---

## Verification Strategy

- Prefer JUnit where current anchors already exist.
- Add GameTest or equivalent integration harness for lifecycle transitions that require menu/network/runtime interaction.
- No “manual play once” acceptance.

---

## Final Rule

Kongqiao is complete only when a player can:

1. enter the system intentionally
2. see why they have the capacity they have
3. unlock and run niantou effects
4. derive and activate shazhao
5. feel overload consequences
6. recover from overload
7. relog without desync or state loss

If any one of those is missing, kongqiao is still only partially landed.
