# Xianqiao Companion Document Expansion

## TL;DR
> **Summary**: Complete the Xianqiao reader-facing companion document chain by adding three Chinese markdown companions focused on five preparation objectives, three-Qi harmonization, and recovery-resource/alchemy support, then align existing navigation so reviewers can understand the whole gameplay arc without opening the main implementation plan first.
> **Deliverables**:
> - Three new companion docs under `.sisyphus/drafts/`
> - Updated overview / recovery / UI companion navigation and stale-reference cleanup
> - Canonical terminology alignment across five objectives, Tian/Di/Ren Qi semantics, and recovery-chain framing
> - Read/Grep-based evidence for structure, navigation, and scope-guardrail verification
> **Effort**: Medium
> **Parallel**: YES - 2 waves
> **Critical Path**: Task 1 → Tasks 2 / 3 / 4 → Task 5 → Task 6 → Task 7

## Context
### Original Request
- Continue the existing Xianqiao / ascension documentation work.
- Keep `plans` as implementation references rather than the primary reading entry.
- Prioritize direct gameplay-review reading material that makes the hardcore ascension loop easy to assess.

### Interview Summary
- Existing companion docs already cover overview, tribulation, hazard/enemy pressure, recovery review, and UI / Hub review.
- The remaining obvious gaps are three Chinese reader-facing artifacts:
  1. five preparation objectives breakdown
  2. three-Qi harmonization review
  3. recovery resources / alchemy chain draft
- The user chose to prioritize filling all three companion docs first, rather than entering code implementation or widening the work into feature execution.
- This wave is documentation-only and must stay inside `.sisyphus/` markdown artifacts.

### Metis Review (gaps addressed)
- Lock exact filenames, document intent, and canonical terminology before writing to avoid rename churn and cross-doc contradiction.
- Keep the new docs reader-facing; do not drift into implementation-heavy runtime/schema prose from the main plan.
- Split creation, navigation cleanup, and consistency verification into separate tasks so the set does not still read like a future-work list after writing.
- Treat the recovery-resource/alchemy artifact explicitly as a **draft**, while the other two remain **review / breakdown** companions.

## Work Objectives
### Core Objective
Produce a decision-complete documentation plan that fills the remaining Xianqiao companion-document gaps, so a reviewer can read the ascension journey, preparation pressure, Qi balance logic, and failure recovery support chain entirely through Chinese companion docs before consulting the implementation plan.

### Deliverables
- `.sisyphus/drafts/xianqiao-five-preparation-objectives-zh.md`
- `.sisyphus/drafts/xianqiao-three-qi-harmonization-review-zh.md`
- `.sisyphus/drafts/xianqiao-recovery-resources-alchemy-chain-draft-zh.md`
- Updated navigation in `.sisyphus/drafts/xianqiao-ascension-overview-zh.md`
- Updated sibling references in `.sisyphus/drafts/xianqiao-recovery-review-zh.md`
- Updated sibling references in `.sisyphus/drafts/xianqiao-ui-hub-review-zh.md`
- Consistency / scope-guardrail verification evidence under `.sisyphus/evidence/`

### Definition of Done (verifiable conditions with commands)
- `read .sisyphus/drafts/xianqiao-five-preparation-objectives-zh.md` confirms the file exists and contains `## 文档定位`, `## 推荐阅读顺序`, the five-objective table, and `## 配套文档导航`.
- `read .sisyphus/drafts/xianqiao-three-qi-harmonization-review-zh.md` confirms the file exists and contains explicit sections for Qi acquisition vs harmonization, imbalance/edge cases, and companion navigation.
- `read .sisyphus/drafts/xianqiao-recovery-resources-alchemy-chain-draft-zh.md` confirms the file exists and contains a draft-status disclaimer, staged recovery-support model, resource categories, alchemy role, and open questions.
- `grep "xianqiao-five-preparation-objectives-zh.md|xianqiao-three-qi-harmonization-review-zh.md|xianqiao-recovery-resources-alchemy-chain-draft-zh.md" .sisyphus/drafts/*.md` shows the new docs are wired into the overview/recovery/UI reading chain with exact file paths.
- `grep "可以补一份《三气调和中文审阅版》|《前置准备五目标中文拆解版》：把五类准备目标分别做细|《恢复资源与丹药链中文草案》：恢复到底靠什么资源和炼丹链支撑" .sisyphus/drafts/*.md` returns no stale future-work references for the three completed docs.
- `grep "HEAVENLY_TIMING|EARTH_VEIN_SURVEY|HUMAN_CONFLICT|RITUAL_MATERIALS|TRIBULATION_SITE_BOUND" .sisyphus/drafts/*.md` shows the canonical objective names appear consistently where required.
- No edits are made outside `.sisyphus/**/*.md` and `.sisyphus/evidence/*`.

### Must Have
- Exactly three new companion docs, using the filenames locked in this plan.
- Chinese reader-facing prose that explains gameplay feeling, pressure, and interpretation rather than implementation details.
- Canonical five-objective terminology preserved exactly:
  1. `HEAVENLY_TIMING`
  2. `EARTH_VEIN_SURVEY`
  3. `HUMAN_CONFLICT`
  4. `RITUAL_MATERIALS`
  5. `TRIBULATION_SITE_BOUND`
- Canonical Tian / Di / Ren Qi roles preserved:
  - Tian Qi = timing / omen / heavenly pressure
  - Di Qi = earth vein / terrain / dangerous field acquisition
  - Ren Qi = human conflict / reputation / social consequence
- The three-Qi doc must distinguish **获取** from **调和** and explain why raw totals alone are insufficient.
- The recovery-resource/alchemy doc must remain explicitly draft-grade and preserve the framing that recovery is expensive, dangerous, staged, and not solvable entirely inside the aperture.
- Overview / recovery / UI companion docs must stop presenting these three documents as missing future additions.
- The main implementation plan must remain the last-resort implementation reference, not the primary reading entry.

### Must NOT Have (guardrails, AI slop patterns, scope boundaries)
- No Java, JSON, data schema, test code, or gameplay implementation edits.
- No new mechanics that contradict or materially extend the existing overview / recovery / UI docs or main redesign plan Tasks 6, 7, and 12.
- No rename churn after filenames are locked.
- No implementation-heavy prose centered on classes such as `ApertureWorldData`, `XianqiaoUiProjection`, `AscensionThreeQiEvaluator`, or `GameTest` inside the new companion docs.
- No navigation that makes `.sisyphus/plans/xianqiao-acquisition-redesign.md` the first or required reading entry.
- No stale “可以补 / 下一步补 / 待补” wording that still describes the three new docs as unfinished future work after this wave completes.

## Verification Strategy
> ZERO HUMAN INTERVENTION — all verification is agent-executed.
- Test decision: **none** + `read` / `grep` markdown verification
- QA policy: Every task includes agent-executed happy-path and edge/failure-path checks
- Evidence: `.sisyphus/evidence/task-{N}-{slug}.txt`

## Execution Strategy
### Parallel Execution Waves
> Target: 5-8 tasks per wave. <3 per wave (except final) = under-splitting.
> Shared vocabulary and filenames must be frozen first; then the three new companion docs can be drafted in parallel.

Wave 1: terminology / filename freeze, five-objectives doc, three-Qi doc, recovery-resource/alchemy draft

Wave 2: overview chain update, sibling stale-reference cleanup, terminology + scope consistency pass

### Dependency Matrix (full, all tasks)
| Task | Depends On | Unlocks |
|---|---|---|
| 1 | — | 2, 3, 4, 5, 6, 7 |
| 2 | 1 | 5, 6, 7 |
| 3 | 1 | 5, 6, 7 |
| 4 | 1 | 5, 6, 7 |
| 5 | 1, 2, 3, 4 | 7 |
| 6 | 1, 2, 3, 4 | 7 |
| 7 | 1, 2, 3, 4, 5, 6 | Final Verification |

### Agent Dispatch Summary (wave → task count → categories)
- Wave 1 → 4 tasks → `writing`, `quick`
- Wave 2 → 3 tasks → `writing`, `quick`, `unspecified-low`

## TODOs
> Implementation + Test = ONE task. Never separate.
> EVERY task MUST have: Agent Profile + Parallelization + QA Scenarios.

- [ ] 1. Freeze filenames, doc intent, and canonical terminology before drafting

  **What to do**: Create a short authoring matrix inside the active draft or working notes that locks the exact three target filenames, the confidence/tone for each document, and the canonical wording to reuse across the set. Freeze the five preparation objective identifiers exactly as already established, freeze the high-level Tian/Di/Ren Qi role sentences, and freeze the recovery-support framing that recovery is staged, expensive, externally pressured, and not solvable entirely inside the aperture. This task is complete only when later doc-writing tasks can copy from one canonical vocabulary source without inventing alternate labels.
  **Must NOT do**: Do not begin full companion-doc prose before filenames and terminology are frozen. Do not introduce alternative objective names, renamed file slugs, or a tone shift that makes the recovery-support document sound final when it is intentionally a draft.

  **Recommended Agent Profile**:
  - Category: `quick` — Reason: small but critical scope lock that prevents cross-doc churn
  - Skills: `[]` — no extra skill required
  - Omitted: `playwright` — documentation-only task

  **Parallelization**: Can Parallel: NO | Wave 1 | Blocks: 2, 3, 4, 5, 6, 7 | Blocked By: —

  **References** (executor has NO interview context — be exhaustive):
  - Pattern: `.sisyphus/drafts/xianqiao-ascension-overview-zh.md:85-98` — canonical five-objective list and player-facing interpretation
  - Pattern: `.sisyphus/drafts/xianqiao-ascension-overview-zh.md:63-68` — canonical Tian/Di/Ren Qi meaning as three distinct risks
  - Pattern: `.sisyphus/drafts/xianqiao-recovery-review-zh.md:20-39` — recovery line framing as painful but not hopeless
  - Pattern: `.sisyphus/drafts/xianqiao-ui-hub-review-zh.md:11-17, 393-396` — current companion reading-chain style and future-doc placeholders to normalize against
  - API/Type: `.sisyphus/plans/xianqiao-acquisition-redesign.md:423-505` — authoritative semantics for Task 6 and Task 7
  - API/Type: `.sisyphus/plans/xianqiao-acquisition-redesign.md:678-717` — authoritative recovery-chain semantics for Task 12

  **Acceptance Criteria** (agent-executable only):
  - [ ] The exact filenames for all three new docs are frozen and written down verbatim.
  - [ ] The five canonical preparation objective identifiers are listed once and match the overview and main plan exactly.
  - [ ] The canonical Tian/Di/Ren Qi roles are written in reusable wording for downstream docs.
  - [ ] The recovery-resource/alchemy document is explicitly marked as a draft in the frozen authoring notes.

  **QA Scenarios** (MANDATORY — task incomplete without these):
  ```
  Scenario: Authoring matrix locks filenames and canonical vocabulary before drafting starts
    Tool: Read
    Steps: Read the active draft/working note and verify exact filenames, objective IDs, Qi roles, and recovery-draft intent are all written explicitly
    Expected: Later writing tasks can reuse one canonical source without ambiguity or alternate naming
    Evidence: .sisyphus/evidence/task-1-companion-doc-freeze.txt

  Scenario: No alternate filenames or renamed objective labels appear in the working note
    Tool: Grep
    Steps: Search `.sisyphus/drafts/*.md` for near-duplicate filename slugs and non-canonical objective spellings before drafting new files
    Expected: Search confirms there is no conflicting naming baseline that would cause doc churn
    Evidence: .sisyphus/evidence/task-1-companion-doc-freeze-error.txt
  ```

  **Commit**: YES | Message: `docs(xianqiao): freeze companion doc expansion vocabulary` | Files: `.sisyphus/drafts/*.md`, `.sisyphus/evidence/*`

- [ ] 2. Write the five-preparation-objectives companion doc as a player-facing qualification breakdown

  **What to do**: Create `.sisyphus/drafts/xianqiao-five-preparation-objectives-zh.md` as a Chinese companion document that explains why the five-objective layer exists, why “修为到了” is still not enough, and how each objective maps to a specific kind of preparation pressure. The document must include: 文档定位, 推荐阅读顺序, a summary table of all five objectives, one dedicated subsection per objective, the relation between five objectives and three-Qi/confirmation readiness, a short “容易误解的点” section, and 配套文档导航. The writing must stay at the level of player feeling and gameplay review, not implementation state-machine detail.
  **Must NOT do**: Do not collapse the doc into a copy of the overview table. Do not explain these objectives as mere hidden flags or back-end checklist booleans. Do not imply that any one objective can substitute for the others.

  **Recommended Agent Profile**:
  - Category: `writing` — Reason: reader-facing clarity and tone are the core deliverables
  - Skills: `[]`
  - Omitted: `frontend-ui-ux` — this doc explains the system, not the interface layout

  **Parallelization**: Can Parallel: YES | Wave 1 | Blocks: 5, 6, 7 | Blocked By: 1

  **References** (executor has NO interview context — be exhaustive):
  - Pattern: `.sisyphus/drafts/xianqiao-ascension-overview-zh.md:85-98` — exact five-objective table already shown in the main overview
  - Pattern: `.sisyphus/drafts/xianqiao-ascension-overview-zh.md:38-50` — where five objectives sit inside the full ascension journey
  - Pattern: `.sisyphus/drafts/xianqiao-ui-hub-review-zh.md:21-35` — companion-doc tone for explaining “what problem this layer solves” to the player
  - API/Type: `.sisyphus/plans/xianqiao-acquisition-redesign.md:423-463` — Task 6 canonical semantics: explicit completion rules, blockers, and why objectives cannot be replaced by Qi totals alone

  **Acceptance Criteria** (agent-executable only):
  - [ ] `.sisyphus/drafts/xianqiao-five-preparation-objectives-zh.md` exists.
  - [ ] The document contains `## 文档定位`, `## 推荐阅读顺序`, one table containing all five objective IDs, five dedicated objective subsections, and `## 配套文档导航`.
  - [ ] The document explicitly states that five objectives are qualification gates and not equivalent to “三气够了”.
  - [ ] The document explains how objective completion and confirmation readiness relate without implying instant confirmation.

  **QA Scenarios** (MANDATORY — task incomplete without these):
  ```
  Scenario: Five-objectives companion doc contains all required sections and canonical objective names
    Tool: Read
    Steps: Read `.sisyphus/drafts/xianqiao-five-preparation-objectives-zh.md` and verify the title, section headings, summary table, five objective subsections, and navigation block
    Expected: The file reads like a complete standalone breakdown of ascension qualification pressure
    Evidence: .sisyphus/evidence/task-2-five-objectives-doc.txt

  Scenario: Five-objectives companion doc does not collapse the system into Qi-only readiness
    Tool: Grep
    Steps: Search the new file for wording that explicitly distinguishes preparation objectives from three-Qi totals and search for missing objective IDs
    Expected: The file preserves all five objective IDs and explicitly rejects a Qi-only interpretation
    Evidence: .sisyphus/evidence/task-2-five-objectives-doc-error.txt
  ```

  **Commit**: YES | Message: `docs(xianqiao): add five preparation objectives companion doc` | Files: `.sisyphus/drafts/xianqiao-five-preparation-objectives-zh.md`, `.sisyphus/evidence/*`

- [ ] 3. Write the three-Qi harmonization companion doc as a balance-and-pressure review

  **What to do**: Create `.sisyphus/drafts/xianqiao-three-qi-harmonization-review-zh.md` as a Chinese review doc explaining why Tian/Di/Ren Qi are three distinct pressure directions rather than three progress bars. The document must include: 文档定位, 推荐阅读顺序, canonical roles of Tian/Di/Ren Qi, the difference between “获取” and “调和”, what imbalance means in player terms, how five objectives feed or constrain harmonization, how confirmation readiness can still be blocked when totals are high, a short UI/feedback implications section, failure/edge interpretation examples, and 配套文档导航.
  **Must NOT do**: Do not leave the document at abstract flavor level only. Do not suggest that raw totals alone should authorize confirmation. Do not write UI wireframe or code-class detail into this companion review.

  **Recommended Agent Profile**:
  - Category: `writing` — Reason: the doc must translate technical game-logic semantics into clear player-facing language
  - Skills: `[]`
  - Omitted: `playwright` — no interface automation required

  **Parallelization**: Can Parallel: YES | Wave 1 | Blocks: 5, 6, 7 | Blocked By: 1

  **References** (executor has NO interview context — be exhaustive):
  - Pattern: `.sisyphus/drafts/xianqiao-ascension-overview-zh.md:43-45, 63-68` — canonical three-Qi stages and the “three different risks” framing
  - Pattern: `.sisyphus/drafts/xianqiao-ui-hub-review-zh.md:169-182, 324-324` — current UI-facing language around three-Qi and harmonization period
  - API/Type: `.sisyphus/plans/xianqiao-acquisition-redesign.md:465-505` — Task 7 canonical semantics for multi-source acquisition, imbalance, harmonization, and blockers
  - API/Type: `.sisyphus/plans/xianqiao-acquisition-redesign.md:507-530` — confirmation remains gated even after readiness logic, so the doc must not blur harmonization with final commit

  **Acceptance Criteria** (agent-executable only):
  - [ ] `.sisyphus/drafts/xianqiao-three-qi-harmonization-review-zh.md` exists.
  - [ ] The document explicitly separates acquisition from harmonization.
  - [ ] The document includes at least one section covering imbalance / overcap / shortage / delayed harmonization interpretation.
  - [ ] The document preserves canonical Tian/Di/Ren Qi roles and explains why totals alone are insufficient.

  **QA Scenarios** (MANDATORY — task incomplete without these):
  ```
  Scenario: Three-Qi companion doc covers acquisition, harmonization, imbalance, and navigation as required
    Tool: Read
    Steps: Read `.sisyphus/drafts/xianqiao-three-qi-harmonization-review-zh.md` and verify the required headings and explanatory sections exist
    Expected: The file reads as a complete player-facing explanation of why three-Qi readiness is about balance, not just amount
    Evidence: .sisyphus/evidence/task-3-three-qi-doc.txt

  Scenario: Three-Qi companion doc does not imply that high raw totals automatically unlock confirmation
    Tool: Grep
    Steps: Search the new file for explicit wording that totals alone are insufficient and for sections describing imbalance or blocked readiness
    Expected: The file preserves the canonical “enough totals can still be blocked” interpretation
    Evidence: .sisyphus/evidence/task-3-three-qi-doc-error.txt
  ```

  **Commit**: YES | Message: `docs(xianqiao): add three qi harmonization companion doc` | Files: `.sisyphus/drafts/xianqiao-three-qi-harmonization-review-zh.md`, `.sisyphus/evidence/*`

- [ ] 4. Write the recovery-resources/alchemy-chain companion draft as the support-path bridge for failed survivors

  **What to do**: Create `.sisyphus/drafts/xianqiao-recovery-resources-alchemy-chain-draft-zh.md` as a Chinese draft-grade companion document that explains what material / alchemy support is needed to make recovery heavy but not hopeless. The document must include: 文档定位, an explicit draft-status disclaimer, 推荐阅读顺序, why recovery needs its own support chain, staged support across 保命期 / 稳定期 / 修补期 / 重建期, resource-category grouping, why at least part of recovery must depend on dangerous external acquisition, what role alchemy plays without trivializing recovery, a few draft chain examples without hard-committing final recipes, open questions / intentionally unsettled areas, and 配套文档导航.
  **Must NOT do**: Do not turn this document into a finalized recipe spreadsheet. Do not imply that all recovery can be completed safely inside the aperture. Do not erase uncertainty by pretending every resource tier is already design-final.

  **Recommended Agent Profile**:
  - Category: `writing` — Reason: the task is exploratory but still needs coherent player-facing framing
  - Skills: `[]`
  - Omitted: `frontend-ui-ux` — this is support-economy framing, not interface design

  **Parallelization**: Can Parallel: YES | Wave 1 | Blocks: 5, 6, 7 | Blocked By: 1

  **References** (executor has NO interview context — be exhaustive):
  - Pattern: `.sisyphus/drafts/xianqiao-recovery-review-zh.md:20-39, 309-322` — canonical recovery framing: painful, prolonged, but not hopeless
  - Pattern: `.sisyphus/drafts/xianqiao-ascension-overview-zh.md:47-50, 139-162` — where failure recovery sits in the overall loop and why it must still hurt
  - API/Type: `.sisyphus/plans/xianqiao-acquisition-redesign.md:678-717` — Task 12 canonical semantics for staged, expensive recovery using alchemy, rare materials, and external high-risk acquisition
  - API/Type: `.sisyphus/plans/xianqiao-acquisition-redesign.md:719-749` — external pressure constraints that make recovery-resource acquisition dangerous rather than safe farming

  **Acceptance Criteria** (agent-executable only):
  - [ ] `.sisyphus/drafts/xianqiao-recovery-resources-alchemy-chain-draft-zh.md` exists.
  - [ ] The document contains a draft-status disclaimer and an explicit open-questions section.
  - [ ] The document covers all four recovery-support stages: 保命期, 稳定期, 修补期, 重建期.
  - [ ] The document states that recovery cannot be fully resolved inside the aperture without at least one dangerous external acquisition leg.

  **QA Scenarios** (MANDATORY — task incomplete without these):
  ```
  Scenario: Recovery-resource/alchemy draft contains draft framing, staged support model, and navigation block
    Tool: Read
    Steps: Read `.sisyphus/drafts/xianqiao-recovery-resources-alchemy-chain-draft-zh.md` and verify the disclaimer, four-stage structure, resource/alchemy sections, open questions, and navigation headings
    Expected: The file reads like a deliberate draft companion rather than an unfinished placeholder
    Evidence: .sisyphus/evidence/task-4-recovery-chain-doc.txt

  Scenario: Recovery-resource/alchemy draft does not trivialize recovery into safe internal farming only
    Tool: Grep
    Steps: Search the new file for explicit wording about dangerous external acquisition and for the absence of “all inside aperture” framing
    Expected: The file preserves expensive, risky, staged recovery as the canonical reading
    Evidence: .sisyphus/evidence/task-4-recovery-chain-doc-error.txt
  ```

  **Commit**: YES | Message: `docs(xianqiao): add recovery resources alchemy companion draft` | Files: `.sisyphus/drafts/xianqiao-recovery-resources-alchemy-chain-draft-zh.md`, `.sisyphus/evidence/*`

- [ ] 5. Update the overview companion so the primary reading chain includes the three new docs explicitly

  **What to do**: Edit `.sisyphus/drafts/xianqiao-ascension-overview-zh.md` so the primary reading order and companion-navigation section now explicitly include the three new documents in the right places. Keep the overview as the first reading entry, keep the main redesign plan last, and ensure the inserted references support a natural reading path: overview → five objectives → three-Qi review → tribulation / hazard / recovery / UI → main plan. If the overview currently explains concepts that are now expanded elsewhere, add brief “see companion doc” signposts without bloating the overview.
  **Must NOT do**: Do not demote the overview from first entry. Do not turn the overview into a link dump. Do not reorder the chain so the main plan appears before the companion set.

  **Recommended Agent Profile**:
  - Category: `writing` — Reason: reading-order curation is a content-design task, not a mechanical link patch only
  - Skills: `[]`
  - Omitted: `playwright` — markdown-only task

  **Parallelization**: Can Parallel: NO | Wave 2 | Blocks: 7 | Blocked By: 1, 2, 3, 4

  **References** (executor has NO interview context — be exhaustive):
  - Pattern: `.sisyphus/drafts/xianqiao-ascension-overview-zh.md:3-15` — existing “首要阅读入口” and reading-order structure to preserve
  - Pattern: `.sisyphus/drafts/xianqiao-ascension-overview-zh.md:190-198` — existing navigation table that must be expanded
  - Pattern: `.sisyphus/drafts/xianqiao-recovery-review-zh.md:11-17` — sibling docs already use ordered reading lists that the overview should remain consistent with
  - API/Type: `.sisyphus/plans/xianqiao-acquisition-redesign.md:43-55` — the main plan stays implementation reference and should remain last in reader-facing navigation

  **Acceptance Criteria** (agent-executable only):
  - [ ] The overview reading-order list includes exact paths to all three new companion docs.
  - [ ] The overview still identifies itself as the primary reading entry.
  - [ ] The main implementation plan remains last in the reading chain.
  - [ ] The overview contains short signposts directing readers to the new companion docs where appropriate.

  **QA Scenarios** (MANDATORY — task incomplete without these):
  ```
  Scenario: Overview companion exposes a complete reading chain including the three new docs
    Tool: Read
    Steps: Read `.sisyphus/drafts/xianqiao-ascension-overview-zh.md` and verify the reading order and navigation table include all new files while keeping the overview first and the main plan last
    Expected: A new reader can follow the companion chain without consulting the implementation plan first
    Evidence: .sisyphus/evidence/task-5-overview-nav.txt

  Scenario: Overview companion does not accidentally promote the main plan ahead of the companion set
    Tool: Grep
    Steps: Search the overview file for its ordered reading list and confirm the main plan appears only as the final implementation reference
    Expected: Navigation preserves the “companion docs first, plan last” rule
    Evidence: .sisyphus/evidence/task-5-overview-nav-error.txt
  ```

  **Commit**: YES | Message: `docs(xianqiao): expand overview companion navigation` | Files: `.sisyphus/drafts/xianqiao-ascension-overview-zh.md`, `.sisyphus/evidence/*`

- [ ] 6. Clean up sibling companion docs so they stop describing the new files as future work

  **What to do**: Update `.sisyphus/drafts/xianqiao-recovery-review-zh.md`, `.sisyphus/drafts/xianqiao-ui-hub-review-zh.md`, and `.sisyphus/drafts/xianqiao-companion-docs-continuation.md` so they no longer present the three new docs as missing future additions. Replace future-tense placeholders with exact file-path references and concise explanations of what each new companion contributes. If needed, lightly refresh nearby wording so the companion set reads as complete, but keep each document’s original focus intact.
  **Must NOT do**: Do not rewrite large sections unrelated to navigation cleanup. Do not add new future-work placeholders for the same topics under alternate names. Do not create contradictions between review-grade docs and the intentionally draft-grade recovery-resource/alchemy doc.

  **Recommended Agent Profile**:
  - Category: `quick` — Reason: targeted stale-reference and navigation normalization work
  - Skills: `[]`
  - Omitted: `writing` — large prose invention is not the main need here

  **Parallelization**: Can Parallel: YES | Wave 2 | Blocks: 7 | Blocked By: 1, 2, 3, 4

  **References** (executor has NO interview context — be exhaustive):
  - Pattern: `.sisyphus/drafts/xianqiao-recovery-review-zh.md:335-338` — stale future-work list that must be normalized
  - Pattern: `.sisyphus/drafts/xianqiao-ui-hub-review-zh.md:393-396` — stale future-work list that must be normalized
  - Pattern: `.sisyphus/drafts/xianqiao-companion-docs-continuation.md` — active continuation note must reflect that scope is now planned, not hypothetical
  - Pattern: `.sisyphus/drafts/xianqiao-ascension-overview-zh.md:8-14, 190-198` — target navigation style for exact path references

  **Acceptance Criteria** (agent-executable only):
  - [ ] Recovery review no longer lists the new companion docs as “next additions”.
  - [ ] UI / Hub review no longer lists the new companion docs as “可以补一份…”.
  - [ ] Continuation draft reflects the finalized three-doc scope without open future-tense ambiguity.
  - [ ] All updated references use exact file paths.

  **QA Scenarios** (MANDATORY — task incomplete without these):
  ```
  Scenario: Recovery/UI/continuation docs no longer advertise the three target docs as missing future work
    Tool: Grep
    Steps: Search `.sisyphus/drafts/*.md` for the old future-work phrases naming the three target docs and verify they are gone or replaced with exact file references
    Expected: The companion set no longer reads like the three docs are still pending
    Evidence: .sisyphus/evidence/task-6-sibling-nav-cleanup.txt

  Scenario: Sibling-doc cleanup does not introduce alternate filenames or contradictory tone labels
    Tool: Read
    Steps: Read the updated recovery/UI/continuation files and confirm the three target docs are referenced with exact filenames and appropriate review/draft wording
    Expected: Navigation cleanup is precise and consistent with the frozen authoring matrix
    Evidence: .sisyphus/evidence/task-6-sibling-nav-cleanup-error.txt
  ```

  **Commit**: YES | Message: `docs(xianqiao): align sibling companion references` | Files: `.sisyphus/drafts/xianqiao-recovery-review-zh.md`, `.sisyphus/drafts/xianqiao-ui-hub-review-zh.md`, `.sisyphus/drafts/xianqiao-companion-docs-continuation.md`, `.sisyphus/evidence/*`

- [ ] 7. Run the final terminology, navigation, and scope-guardrail verification pass across the companion set

  **What to do**: Perform a final repository-wide markdown verification pass across `.sisyphus/drafts/*.md` using `read` and `grep`. Confirm the exact filenames exist, the overview reading chain is complete, the stale future-work phrases are gone, the five objective identifiers remain canonical, the three-Qi doc explicitly separates acquisition from harmonization, and the recovery-resource/alchemy draft keeps its draft disclaimer plus external-risk framing. Capture evidence files for structure checks, navigation cleanup, terminology consistency, and scope guardrails. If any doc drifts into implementation-heavy class/API language or recenters the main plan as required reading, fix the wording before closing the wave.
  **Must NOT do**: Do not rely on a human eyeball review as the only validation path. Do not leave “consistency” as an implied concept without explicit grep/read assertions. Do not ship with stale placeholder language still present anywhere in `.sisyphus/drafts/*.md`.

  **Recommended Agent Profile**:
  - Category: `unspecified-low` — Reason: systematic verification and cleanup rather than creative drafting
  - Skills: `[]`
  - Omitted: `playwright` — non-UI markdown QA only

  **Parallelization**: Can Parallel: NO | Wave 2 | Blocks: Final Verification | Blocked By: 1, 2, 3, 4, 5, 6

  **References** (executor has NO interview context — be exhaustive):
  - Pattern: `.sisyphus/drafts/xianqiao-ascension-overview-zh.md` — primary reading-chain authority
  - Pattern: `.sisyphus/drafts/xianqiao-five-preparation-objectives-zh.md` — new qualification-breakdown doc to verify
  - Pattern: `.sisyphus/drafts/xianqiao-three-qi-harmonization-review-zh.md` — new Qi-balance doc to verify
  - Pattern: `.sisyphus/drafts/xianqiao-recovery-resources-alchemy-chain-draft-zh.md` — new recovery-support draft to verify
  - Pattern: `.sisyphus/drafts/xianqiao-recovery-review-zh.md` and `.sisyphus/drafts/xianqiao-ui-hub-review-zh.md` — sibling nav cleanup targets
  - API/Type: `.sisyphus/plans/xianqiao-acquisition-redesign.md:423-505, 678-717` — semantic anchor for preparation objectives, three-Qi logic, and recovery-chain framing

  **Acceptance Criteria** (agent-executable only):
  - [ ] All three target files exist and contain their required section headings.
  - [ ] Overview / recovery / UI / continuation docs contain current exact-file references rather than future-work placeholders.
  - [ ] The five canonical objective IDs appear consistently where referenced.
  - [ ] The three-Qi doc explicitly distinguishes 获取 from 调和 and retains blocked-readiness semantics.
  - [ ] The recovery-resource/alchemy draft explicitly contains both a draft disclaimer and external-risk framing.
  - [ ] No new companion doc drifts into implementation-heavy class/test-path explanation.

  **QA Scenarios** (MANDATORY — task incomplete without these):
  ```
  Scenario: Read/Grep verification proves the companion set is complete, cross-linked, and terminology-consistent
    Tool: Grep
    Steps: Search `.sisyphus/drafts/*.md` for the three exact filenames, the five canonical objective IDs, acquisition vs harmonization wording, and draft-disclaimer language; then read any file with missing/ambiguous matches
    Expected: Search and follow-up reads confirm the companion set is complete and internally consistent
    Evidence: .sisyphus/evidence/task-7-companion-doc-final-verification.txt

  Scenario: Final verification catches and rejects stale placeholder language or implementation-heavy drift
    Tool: Grep
    Steps: Search `.sisyphus/drafts/*.md` for stale phrases such as `可以补一份`, `下一步最适合继续补`, and for implementation-heavy markers such as `src/main/java`, `GameTest`, or class-path centric prose inside the three new docs
    Expected: No stale placeholders remain for the completed docs, and the new companions stay reader-facing rather than implementation-spec oriented
    Evidence: .sisyphus/evidence/task-7-companion-doc-final-verification-error.txt
  ```

  **Commit**: YES | Message: `docs(xianqiao): verify companion document set consistency` | Files: `.sisyphus/drafts/*.md`, `.sisyphus/evidence/*`

## Final Verification Wave (4 parallel agents, ALL must APPROVE)
- [ ] F1. Companion-Set Compliance Audit — oracle
- [ ] F2. Documentation Quality Review — writing
- [ ] F3. Read/Grep QA Replay — unspecified-high
- [ ] F4. Scope Fidelity Check — deep

## Commit Strategy
- Prefer atomic documentation commits by intent:
  1. filenames + terminology freeze / authoring matrix
  2. five-objectives companion doc
  3. three-Qi harmonization companion doc
  4. recovery resources / alchemy chain draft
  5. navigation cleanup + stale-reference removal + consistency normalization
- If fewer commits are required, the minimum acceptable grouping is:
  - Commit A: the three new docs
  - Commit B: overview/recovery/UI navigation cleanup plus consistency pass

## Success Criteria
- A reviewer can understand the ascension preparation spine, three-Qi balance logic, and recovery-support chain without opening the implementation plan first.
- The three new companion docs read as part of one coherent Chinese document set rather than three isolated add-ons.
- Existing overview / recovery / UI docs no longer advertise the three new docs as future work.
- Canonical names and meanings stay stable across the companion set.
- The work remains strictly documentation-only within `.sisyphus/`.
