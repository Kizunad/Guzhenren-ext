# Update Checker Implementation Plan

## TL;DR

> **Quick Summary**: Enable standard NeoForge update checking for `guzhenrenext` by configuring `neoforge.mods.toml` and creating a GitHub-hosted `update.json`.
>
> **Deliverables**:
> - Modified `src/main/resources/META-INF/neoforge.mods.toml` (adds `updateJSONURL`)
> - New `update.json` file in project root (template)
> - Validation scripts to ensure correct configuration
>
> **Estimated Effort**: Quick (< 30 mins)
> **Parallel Execution**: NO - sequential
> **Critical Path**: Modify TOML → Create JSON → Validate

---

## Context

### Original Request
Implement NeoForge Update Checker for Guzhenren-ext to notify users of new versions.

### Interview Summary
**Key Discussions**:
- **Mechanism**: Standard NeoForge notification (no custom auto-downloader).
- **Target**: `GuzhenrenExt` main mod only (ignoring `customnpcs`/`tinyui`).
- **Hosting**: GitHub Raw file (`update.json` in root of `main` branch).

### Metis Review
**Identified Gaps** (addressed):
- **Target Specificity**: Explicitly target only the `[[mods]]` block for `guzhenrenext` to avoid affecting other mods.
- **Release Strategy**: Acknowledged risk of `main` branch reflecting unreleased versions; using `main` as requested but documenting the behavior.
- **Verification**: Added Python scripts to verify JSON syntax and TOML injection placement.

---

## Work Objectives

### Core Objective
Enable in-game update notifications for `guzhenrenext` users when a new version is pushed to GitHub.

### Concrete Deliverables
- `src/main/resources/META-INF/neoforge.mods.toml`: Added `updateJSONURL` field.
- `update.json`: JSON file conforming to NeoForge spec with current version info.

### Definition of Done
- [ ] `neoforge.mods.toml` contains valid `updateJSONURL` inside `guzhenrenext` block.
- [ ] `update.json` exists, is valid JSON, and contains `promos` for `1.21.1`.
- [ ] `./gradlew checkstyleMain` passes.

### Must Have
- `updateJSONURL` pointing to `https://raw.githubusercontent.com/Kizunad/Guzhenren-ext/main/update.json`.
- Correct `modId` targeting (`guzhenrenext`).

### Must NOT Have (Guardrails)
- Custom download logic or GUI.
- Changes to `customnpcs` or `tinyui` configurations.
- Auto-update mechanisms beyond standard notification.

---

## Verification Strategy (MANDATORY)

> **UNIVERSAL RULE: ZERO HUMAN INTERVENTION**
> ALL tasks must be verifiable via agent tools (grep, python, gradle).

### Test Decision
- **Infrastructure exists**: Yes (Gradle/Checkstyle)
- **Automated tests**: None needed for this config change.
- **Agent-Executed QA**: MANDATORY for file content verification.

### Agent-Executed QA Scenarios (MANDATORY)

#### Scenario 1: Verify TOML Injection
**Tool**: Bash (Python)
**Preconditions**: `neoforge.mods.toml` modified
**Steps**:
1. Run Python script to parse `neoforge.mods.toml`.
2. Locate `[[mods]]` block where `modId="guzhenrenext"`.
3. Assert `updateJSONURL` key exists in that block.
4. Assert `updateJSONURL` value is correct.
5. Assert `customnpcs` and `tinyui` blocks DO NOT have `updateJSONURL`.
**Expected Result**: OK output
**Evidence**: Terminal output

#### Scenario 2: Verify Update JSON Format
**Tool**: Bash (Python)
**Preconditions**: `update.json` created
**Steps**:
1. Run Python script to load `update.json`.
2. Assert valid JSON structure.
3. Assert `promos` object exists.
4. Assert keys for `1.21.1-latest` and `1.21.1-recommended` exist.
5. Assert `homepage` URL is present.
**Expected Result**: OK output
**Evidence**: Terminal output

---

## TODOs

- [x] 1. Add updateJSONURL to neoforge.mods.toml

  **What to do**:
  - Edit `src/main/resources/META-INF/neoforge.mods.toml`.
  - Find the `[[mods]]` block for `modId="guzhenrenext"`.
  - Add `updateJSONURL="https://raw.githubusercontent.com/Kizunad/Guzhenren-ext/main/update.json"`.
  - **CRITICAL**: Do NOT add it to global scope or other mod blocks.

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: [`dev-browser`] (not needed but default), `git-master` (optional)

  **References**:
  - `src/main/resources/META-INF/neoforge.mods.toml` - Target file.
  - Docs: `https://docs.neoforged.net/docs/misc/updatechecker` - Spec.

  **Acceptance Criteria**:
  - [ ] File contains `updateJSONURL` string.
  - [ ] `checkstyleMain` passes.
  - [ ] **QA Scenario**:
    ```python
    import sys, re
    content = open("src/main/resources/META-INF/neoforge.mods.toml").read()
    # Find guzhenrenext block
    match = re.search(r'modId="guzhenrenext".*?updateJSONURL="https://raw.githubusercontent.com/Kizunad/Guzhenren-ext/main/update.json"', content, re.DOTALL)
    assert match, "updateJSONURL not found in guzhenrenext block"
    print("QA: TOML Injection Verified")
    ```

- [x] 2. Create update.json template

  **What to do**:
  - Create `update.json` in project root.
  - Content should match NeoForge spec.
  - Populate `1.21.1-latest` and `1.21.1-recommended` with current version `${mod_version}` placeholder or static `1.0.0` (use `1.0.0` as initial value).
  - Set homepage to `https://github.com/Kizunad/Guzhenren-ext`.

  **Recommended Agent Profile**:
  - **Category**: `quick`

  **References**:
  - `gradle.properties` - For version info (`1.0.0`).
  - Docs: `https://docs.neoforged.net/docs/misc/updatechecker` - JSON format.

  **Acceptance Criteria**:
  - [ ] `update.json` exists in root.
  - [ ] Valid JSON syntax.
  - [ ] **QA Scenario**:
    ```python
    import json
    data = json.load(open("update.json"))
    assert "homepage" in data
    assert "1.21.1-latest" in data["promos"]
    print("QA: JSON Format Verified")
    ```

---

## Success Criteria

### Verification Commands
```bash
# Verify TOML syntax and structure
python3 -c 'import sys, re; c=open("src/main/resources/META-INF/neoforge.mods.toml").read(); assert "updateJSONURL" in c; print("TOML OK")'

# Verify JSON syntax
python3 -c 'import json; json.load(open("update.json")); print("JSON OK")'

# Check style
./gradlew checkstyleMain
```

### Final Checklist
- [ ] `neoforge.mods.toml` updated safely (no impact on other mods).
- [ ] `update.json` created and valid.
- [ ] No build errors.
