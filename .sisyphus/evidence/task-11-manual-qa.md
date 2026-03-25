# Task 11 Manual QA Evidence

## Scope
This evidence file records the final manual/observable QA state for the `xianqiao-init-gameplay-redesign` worktree after Task 10 + Task 11 convergence and Final Wave remediation.

## User-approved QA scope adjustment
- Date: 2026-03-25
- User instruction: “不使用runclient测试，我默许你可以将所有功能备齐，就算完成。”
- Effect on gate: `runClient` is no longer treated as a required completion gate for this branch. Mechanical verification + code-path completeness are accepted as sufficient completion proof.

## Mechanical regression evidence
- Full serial regression log: `.sisyphus/evidence/task-11-final-regression.log`
- Result:
  - `./gradlew --no-daemon compileJava` ✅
  - `./gradlew --no-daemon checkstyleMain` ✅
  - `./gradlew --no-daemon test` ✅

## Legacy retirement evidence
- Source audit log: `.sisyphus/evidence/task-11-legacy-retirement.log`
- Confirmed by current source:
  - `/guzhenren enter_aperture` still exists as compatibility adapter
  - command path routes into `ApertureEntryRuntime.trigger(...)`
  - `ApertureCommand` no longer contains the old callable 2x2-only initialization implementation
  - runtime keeps only `ensureLandSpiritExistsFromSharedEntry(...)` as shared compatibility seam

## Observable manual smoke artifacts already captured
Even though `runClient` is no longer required by user approval, existing observable artifacts were preserved:
- `.sisyphus/evidence/task-11-manual-minecraft-window.png`
  - current worktree client window exists and is visible
- `.sisyphus/evidence/task-11-manual-after-continue.png`
  - current worktree reached Minecraft main menu after continuing past accessibility welcome screen
- `.sisyphus/evidence/task-11-manual-singleplayer.png`
  - screenshot captured after attempting Singleplayer navigation from current worktree session
- `.sisyphus/evidence/task-11-manual-runclient.log`
  - current worktree `runClient` launch log

## Manual smoke interpretation
- Verified from current worktree artifacts:
  - a real Minecraft client window was launched for this worktree
  - the welcome/accessibility screen was visible
  - after clicking Continue, the client reached the main menu
  - no startup crash was introduced by the xianqiao entry/runtime redesign during this observable session
- Not claimed as proven in this file:
  - full in-world Hub flow
  - in-world `/guzhenren enter_aperture` execution
  - already-initialized re-entry teleport in a live world
- Reason these are not required for completion now:
  - the user explicitly waived `runClient`-based completion testing
  - the functional seams are covered by source audit + regression tests + entry/runtime integration tests already green in repository verification

## Functional completeness covered by code/tests
The following completion claims rely on current green code/tests rather than further interactive smoke:
- Hub/non-command path exists through `ApertureHubScreen` + `ServerboundApertureEntryPayload` + `ApertureEntryRuntime`
- legacy command remains adapter-only and converges into the same shared runtime path
- phased bootstrap resume works with persisted `InitialTerrainPlan`
- bounded `r=1000` biome search is wired into planned-cell runtime materialization
- benming descriptor is no longer empty placeholder
- final world boundary writeback now persists planner boundary into `ApertureWorldData.ApertureInfo.min/maxChunk`

## Reviewer-facing conclusion
Given:
1. full mechanical regression is green,
2. legacy retirement evidence is archived,
3. current worktree has observable client-launch/menu artifacts, and
4. the user explicitly waived `runClient` completion as a hard requirement,

this branch should be considered to have sufficient QA evidence for Final Wave review under the user-approved scope.
