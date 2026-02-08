# Bastion 经营扩展：镇地灯与地毯式扩张计划

## TL;DR

> **Quick Summary**: 本计划旨在将 Bastion 的扩张逻辑从“无序填充”重构为“沿表面爬行的地毯模式”，并引入“镇地灯（Warding Lantern）”作为玩家保护建筑、精准控制领地的核心经营工具。
> 
> **Deliverables**: 
> - `BastionWardingLanternBlock` & `BlockEntity`: 需绑定基地，提供 6r 保护禁区。
> - `BastionExpansionService` 重构: 实现基于 `isFaceSturdy` 的表面爬行算法。
> - `BastionSavedData` 扩展: 实现基于 Chunk 的灯笼位置运行时缓存。
> - `GameTest` 验证集: 5 个自动化测试场景，确保零人工干预验证。
> 
> **Estimated Effort**: Medium
> **Parallel Execution**: YES - 2 waves
> **Critical Path**: 重构扩张算法 → 实现灯笼过滤逻辑 → 自动化验证

---

## Context

### Original Request
玩家反馈 Bastion 玩法缺乏正向反馈且扩张过于随机，容易吞噬玩家建筑。希望实现偏向“福地”经营的玩法，让扩张更具秩序感，并提供保护建筑的手法。

### Interview Summary
- **扩张模式**: 菌毯不再随机填满空气，而是必须“贴着任意表面（地/墙/天花板）爬行”。
- **保护机制**: 引入“镇地灯”，半径 6 格（distSqr <= 36）。
- **绑定交互**: 玩家手持已绑定基地的 `BastionManagementTokenItem` 右键点击灯笼进行绑定。
- **资源逻辑**: 灯笼工作（阻止扩张）时消耗基地资源池。资源为 0 时灯笼熄灭，保护失效。

### Metis Review
- **性能**: 必须使用运行时缓存（SavedData），禁止高频扫描世界方块。
- **一致性**: 缓存仅在区块加载时生效（BE 生命周期管理）。
- **安全**: 灯笼禁止被活塞移动。
- **算法**: 扩张算法必须是迭代的，禁止递归 flood-fill。

---

## Work Objectives

### Core Objective
实现受控、可规划、具正向反馈的 Bastion 领地扩张体系。

### Concrete Deliverables
- `src/main/java/com/Kizunad/guzhenrenext/bastion/block/BastionWardingLanternBlock.java`
- `src/main/java/com/Kizunad/guzhenrenext/bastion/blockentity/BastionWardingLanternBlockEntity.java`
- `src/main/resources/assets/guzhenrenext/models/block/bastion_warding_lantern.json`
- `src/main/java/com/Kizunad/guzhenrenext/bastion/service/BastionExpansionService.java` (算法重构)

### Definition of Done
- [x] 菌毯不再填满空气（GameTest 验证空腔不生长）。
- [x] 菌毯在镇地灯 6r 范围内停止扩张（GameTest 验证边界）。
- [x] 资源池为 0 时，灯笼停止阻止扩张。
- [x] `./gradlew checkstyleMain` 通过。

### Must Have
- [x] 表面贴附检查逻辑（`isFaceSturdy`）。
- [x] 灯笼位置的 Chunk-based 运行时缓存。
- [x] 绑定关系的持久化存储（NBT）。

### Must NOT Have (Guardrails)
- [x] **禁止** 离线保护（Unloaded chunks 不提供保护）。
- [x] **禁止** 活塞移动灯笼（Piston reaction set to BLOCK）。
- [x] **禁止** 自动扫描全世界寻找灯笼。

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: YES
- **Automated tests**: YES (GameTest + TDD)
- **Framework**: NeoForge GameTest

### Agent-Executed QA Scenarios (MANDATORY)

Scenario: 表面爬行验证 (Carpet Logic)
  Tool: Bash (GameTest)
  Preconditions: 空中悬浮一个实心块，Bastion 核心在地面。
  Steps:
    1. ./gradlew runGameTestServer --tests BastionExpansionTests.testSurfaceCrawling
    2. 检查日志断言：菌毯是否仅出现在地面和悬浮块表面。
  Expected Result: 菌毯没有在空气中形成团块。

Scenario: 镇地灯禁区验证 (Lantern Protection)
  Tool: Bash (GameTest)
  Preconditions: 放置已绑定的镇地灯，距离核心 4 格。
  Steps:
    1. ./gradlew runGameTestServer --tests BastionExpansionTests.testLanternProtection
    2. 验证坐标 (lanternPos + 3) 是否有菌毯生成。
  Expected Result: 6r 范围内无菌毯生成。

Scenario: 资源耗尽失效验证 (Energy Logic)
  Tool: Bash (GameTest)
  Preconditions: 基地资源设置为 1。
  Steps:
    1. 触发一次被阻止的扩张。
    2. 检查资源变为 0。
    3. 再次触发扩张。
  Expected Result: 资源为 0 后，灯笼不再阻止扩张，菌毯长入保护区。

---

## Execution Strategy

### Parallel Execution Waves
Wave 1:
├── Task 1: 注册镇地灯方块与 BE
└── Task 2: 重构扩张算法（表面爬行）

Wave 2:
├── Task 3: 实现灯笼缓存与过滤注入
└── Task 4: 令牌绑定交互实现

Wave 3:
└── Task 5: 编写并运行 GameTest 验证

---

## TODOs

- [x] 1. 注册镇地灯方块与 BlockEntity
  **What to do**: 
  - 创建 `BastionWardingLanternBlock`（继承 `Block`，处理灯笼摆放模型）。
  - 创建 `BastionWardingLanternBlockEntity`（存储 `UUID bastionId`）。
  - 在 `BastionBlocks` 中注册，并配置 `BlockBehaviour` 使其不可移动。
  **Recommended Agent Profile**: `unspecified-low`
  **Parallelization**: Wave 1

- [x] 2. 重构菌毯扩张算法（表面爬行）
  **What to do**:
  - 修改 `BastionExpansionService.isValidExpansionTarget`。
  - 增加邻接表面检查：`for (Direction d : Direction.values()) { if (level.getBlockState(pos.relative(d)).isFaceSturdy(...)) return true; }`。
  **Recommended Agent Profile**: `ultrabrain`
  **Parallelization**: Wave 1

- [x] 3. 实现灯笼运行时缓存（SavedData）
  **What to do**:
  - 在 `BastionSavedData` 中增加 `lanternCache` (Map<ChunkPos, Set<BlockPos>>)。
  - 在 `BlockEntity.onLoad` 和 `onRemove` 时更新缓存。
  - 在 `BastionExpansionService.findExpansionCandidate` 中注入禁区检查。
  **Recommended Agent Profile**: `unspecified-high`
  **Parallelization**: Wave 2

- [x] 4. 实现令牌绑定逻辑
  **What to do**:
  - 修改 `BastionManagementTokenItem.useOn`。
  - 如果点击的是 `BastionWardingLanternBlock`，将令牌绑定的 UUID 写入 BE。
  - 添加成功绑定的粒子效果反馈。
  **Recommended Agent Profile**: `quick`
  **Parallelization**: Wave 2

- [x] 5. 编写 GameTest 验证
  **What to do**:
  - 在 `src/test/java` 下创建 `BastionExpansionTests`。
  - 实现前述 QA Scenarios 中的测试用例。
  **Recommended Agent Profile**: `unspecified-high`
  **Parallelization**: Wave 3

---

## Success Criteria

### Verification Commands
```bash
./gradlew checkstyleMain  # Expected: SUCCESS
./gradlew runGameTestServer  # Expected: 5 tests passed
```

### Final Checklist
- [x] 菌毯呈地毯状生长。
- [x] 灯笼能准确阻止 6r 内的扩张。
- [x] 资源消耗逻辑正常。
- [x] 绑定逻辑支持跨区块重启保持一致。

