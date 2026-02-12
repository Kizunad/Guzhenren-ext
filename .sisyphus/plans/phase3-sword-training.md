# Phase 3.2: 练剑系统 (Sword Training)

## TL;DR

> **Quick Summary**: 引入“练剑室”功能，允许玩家在 GUI 中挂机修炼飞剑。通过消耗“元石”和时间，提升飞剑经验、玩家剑道流派经验，并增加飞剑好感度。
>
> **Deliverables**:
> - `FlyingSwordTrainingAttachment`：持久化挂机数据
> - `FlyingSwordTrainingMenu` / `Screen`：练剑 GUI（原生容器界面）
> - `FlyingSwordTrainingService`：服务端 Tick 逻辑（消耗/产出）
> - 物品消耗配置（Tag: `guzhenren:primeval_stones`）
>
> **Estimated Effort**: Medium (4-6 hours)
> **Parallel Execution**: Backend (Data/Service) || Frontend (UI)
> **Critical Path**: Attachment → Service → UI

---

## Context

### Original Request
用户希望通过“花费精力，时间，挂机练剑”来提升剑道经验和飞剑经验。

### Design Decisions
- **GUI**: 使用原生 `AbstractContainerScreen`，因为涉及物品槽位（Slot）的交互和同步，比自定义 UI 库更稳健。
- **Consumption**: 消耗“元石”类物品（Fuel）。
- **Gain**:
  - **Sword Exp**: 每秒增加固定值（受剑道流派加成）。
  - **Player Dao Exp**: 少量增加剑道流派道痕。
  - **Affinity**: 小概率增加好感度（领悟剑意）。
- **Location**: 随身 GUI（在 `FlyingSwordForgeMenu` 中增加 Tab 入口，或独立快捷键）。鉴于 Forge 界面已很拥挤，建议注册**独立快捷键 'K'** (KeyMapping) 打开练剑界面。

### Metis Review
**Identified Gaps** (addressed):
1. 数据存储：挂机状态必须持久化，防止玩家下线丢失。
2. 物品安全性：飞剑在挂机时应从玩家背包移除，存入 Attachment 的 ItemHandler 中，避免刷物品。
3. 同步频率：Tick 逻辑每秒执行，但 GUI 同步只需每秒一次或进度变化时。

---

## Work Objectives

### Core Objective
实现一个完整的闭环挂机系统：放入飞剑+元石 → 持续消耗+增长 → 取出更强的飞剑。

### Concrete Deliverables
- 后端数据结构与 Tick 服务
- 前端 GUI
- 网络包（打开 GUI）

### Definition of Done
- [ ] `./gradlew checkstyleMain` → BUILD SUCCESSFUL
- [ ] 放入飞剑和元石后，进度条开始走
- [ ] 经验值确实增加
- [ ] 物品存取正常，无刷物品 bug

### Must Have
- Attachment 持久化
- 燃料消耗逻辑
- 经验产出逻辑
- 简单的进度条 UI

### Must NOT Have (Guardrails)
- 不做复杂的 3D 渲染场景（仅 Slot 交互）
- 不涉及多把剑同时挂机（Phase 3.2 仅单槽，简化逻辑）

---

## Execution Strategy

### Dependency Chain

```
Task 1: 定义 Attachment 与 Service (后端核心)
  ↓
Task 2: 实现 Menu 与 Screen (前端交互)
  ↓
Task 3: 网络与按键绑定 (入口)
```

---

## TODOs

- [x] 1. 定义 Attachment 与 Service

  **What to do**:
  - 创建 `FlyingSwordTrainingAttachment`：
    - `ItemStackHandler inputSlots` (2 slots: 0=Sword, 1=Fuel)
    - `int fuelTime` (当前燃料剩余时间)
    - `int maxFuelTime` (当前燃料总时间)
    - `int accumulatedExp` (本次挂机累计经验)
  - 注册 Attachment。
  - 创建 `FlyingSwordTrainingService.tick(ServerPlayer)`：
    - 检查 inputSlots[0] 是否为飞剑
    - 检查 fuelTime > 0 ? 消耗 fuelTime : 尝试消耗 inputSlots[1]
    - 如果有燃料且有剑：
      - 增加飞剑 NBT 经验
      - 增加 Attachment 统计数据
      - 概率增加好感度 (spiritData)
  - 实现 `FuelHelper`：定义哪些物品是燃料及其时间（默认 Tag `guzhenren:primeval_stones`）。

  **Acceptance Criteria**:
  - [x] 逻辑能跑通，测试代码调用 tick 能看到 fuelTime 减少
  - [x] 已按“每个 task 提交一次”策略为 Task1 准备独立提交

  **Commit**: YES
  - Message: `feat(flyingsword): add training attachment and service`

---

- [ ] 2. 实现 Menu 与 Screen

  **What to do**:
  - 创建 `FlyingSwordTrainingMenu` (extends AbstractContainerMenu)：
    - 绑定 Attachment 的 ItemHandler 到 Slot
    - 添加 PlayerInventory Slot
    - 同步数据：`fuelTime`, `maxFuelTime`, `accumulatedExp` (使用 ContainerData)
  - 创建 `FlyingSwordTrainingScreen` (extends AbstractContainerScreen)：
    - 绘制背景
    - 绘制火焰图标（根据 fuelTime 燃烧进度）
    - 绘制经验增长文字

  **References**:
  - `FlyingSwordForgeMenu.java` (参考写法)

  **Acceptance Criteria**:
  - [ ] GUI 能打开，能看到槽位
  - [ ] 进度条能动

  **Commit**: YES
  - Message: `feat(flyingsword): add training GUI`

---

- [ ] 3. 网络与按键绑定

  **What to do**:
  - 注册 KeyMapping `KEY_TRAINING` (默认 K)。
  - 注册网络包 `ServerboundOpenTrainingGuiPayload`。
  - Client 端监听按键 → 发包。
  - Server 端处理包 → `player.openMenu(...)`。

  **Acceptance Criteria**:
  - [ ] 按 K 键能打开界面

  **Commit**: YES
  - Message: `feat(flyingsword): bind training GUI to key K`


## Success Criteria

### Verification Commands
```bash
```
