# Phase 3.3: 飞剑集群 (Sword Cluster)

## TL;DR

> **Quick Summary**: 实现“飞剑集群管理”系统，允许玩家在一个 GUI 界面中统一管理所有飞剑的出战/召回。引入“算力 (Computation)”资源限制，基于玩家的魂道/智道道痕等级动态计算上限。
>
> **Deliverables**:
> - `FlyingSwordClusterAttachment`：存储出战状态与算力数据
> - `FlyingSwordClusterMenu` / `Screen` (TinyUI)：集群管理界面（滚动列表、批量操作）
> - `FlyingSwordClusterService`：算力计算与出战逻辑
> - 配对共鸣系统基础（Tag Synergy）
>
> **Estimated Effort**: Large (6-8 hours)
> **Parallel Execution**: Backend || Frontend
> **Critical Path**: Attachment → Service → GUI

---

## Context

### Original Request
用户希望有“多个飞剑培养GUI”、“飞剑算法”、“飞剑配对”、“限制过多飞剑”。

### Design Decisions
- **Resource Limit**: 引入“算力 (Computation)”概念。
  - `Max = Base(10) + SoulDao * 0.1 + WisdomDao * 0.2`
  - `Cost = 1 + QualityTier * 2`
  - 使用 `DaoHenHelper` 获取道痕数据。
- **GUI**: 使用 **TinyUI** 实现。
  - 左侧：飞剑列表（Checkbox, Icon, Name, Level, Cost）。
  - 右侧：算力槽、Deploy/Recall 按钮。
  - 底部：Synergy Buff 显示。
- **Synergy**: 简单的 Tag 匹配系统。如果出战飞剑包含特定组合（如 3x Fire），给予 Buff。

### Metis Review
**Identified Gaps** (addressed):
1. **TinyUI 学习曲线**：需要先阅读 TinyUI 示例代码（Demo）。
2. **状态同步**：集群界面需要实时响应（比如勾选后算力条马上变），需要高效的网络同步。
3. **实体一致性**：Deploy 操作必须确保不会生成重复实体（检查 UUID）。

---

## Work Objectives

### Core Objective
实现多飞剑的统一管理与策略限制。

### Concrete Deliverables
- 后端逻辑（Attachment, Service, Synergy）
- 前端 GUI (TinyUI)
- 网络层

### Definition of Done
- [ ] `./gradlew checkstyleMain` → PASS
- [ ] 界面能正确列出所有存储中的飞剑
- [ ] 勾选飞剑能正常出战
- [ ] 算力不足时阻止出战
- [ ] 魂道/智道提升后，算力上限正确增加

### Must Have
- 算力限制逻辑
- TinyUI 滚动列表
- 批量出战/召回

### Must NOT Have (Guardrails)
- 暂时不实现太复杂的 Synergy 效果（只做框架）
- 不做 3D 实体预览（只显示 Item Icon）
- 不做 GameTest

---

## Execution Strategy

### Dependency Chain

```
Task 1: 定义 Cluster Attachment 与 Service (后端)
  ↓
Task 2: 实现算力计算与 DaoHen 对接 (逻辑)
  ↓
Task 3: TinyUI 界面实现 (前端)
  ↓
Task 4: 网络交互与 Sync (联调)
```

---

## TODOs

- [x] 1. 定义 Cluster Attachment 与 Service

  **What to do**:
  - 创建 `FlyingSwordClusterAttachment`：
    - `Set<UUID> activeSwords` (当前出战的飞剑 UUID)
    - `int maxComputation`
    - `int currentLoad`
  - 创建 `FlyingSwordClusterService`：
    - `calculateMaxComputation(player)`: 调用 `DaoHenHelper`。
    - `calculateCost(sword)`: 基于品质计算。
    - `deploy(player, uuid)`: 校验算力 -> 生成实体 -> 记录状态。
    - `recall(player, uuid)`: 销毁实体 -> 移除状态。

  **Acceptance Criteria**:
  - [ ] 逻辑能跑通

  **Commit**: YES
  - Message: `feat(flyingsword): add cluster attachment and logic`

---

- [ ] 2. 实现 Synergy (共鸣) 基础

  **What to do**:
  - 创建 `ClusterSynergyHelper`：
    - `evaluate(List<FlyingSwordEntity> activeSwords)` -> 返回 `List<Buff>`
    - 简单规则：如果有 3 把同名剑 -> 攻击力 +10%。
  - 在 `FlyingSwordEntity` 的属性计算中调用 Synergy 加成。

  **Commit**: YES
  - Message: `feat(flyingsword): add cluster synergy foundation`

---

- [ ] 3. TinyUI 界面实现

  **What to do**:
  - **Learning**: 阅读 `com.Kizunad.tinyUI.demo` 代码。
  - 创建 `FlyingSwordClusterScreen` (TinyUI)：
    - 使用 `ListView` 组件。
    - 自定义 `ListCell` 渲染飞剑信息。
    - 绑定数据模型 (`ObservableList`).
  - 创建 `ClusterMenu` (Container) 用于传递数据（或者直接用网络包 sync，TinyUI 可能不需要 Container）。

  **References**:
  - `src/main/java/com/Kizunad/tinyUI`

  **Commit**: YES
  - Message: `feat(flyingsword): add cluster GUI using TinyUI`

---

- [ ] 4. 网络与集成

  **What to do**:
  - 注册 `ServerboundClusterActionPayload` (Deploy/Recall)。
  - 注册 `ClientboundClusterStatePayload` (Sync list & computation)。
  - 绑定按键（如 'C' 或在 Forge Menu 加 Tab）。

  **Commit**: YES
  - Message: `feat(flyingsword): integrate cluster system`

---

## Success Criteria

### Verification Commands
```bash
./gradlew runGameTestServer
```
