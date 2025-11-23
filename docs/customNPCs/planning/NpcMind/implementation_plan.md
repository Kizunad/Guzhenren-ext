# 实施计划 - NPC 行为执行层 (Action & Execution)

## 目标描述
构建 NPC 的行为执行层，将高层决策（Goals）与底层具体操作（Actions）解耦。
引入 `ActionExecutor` 和原子 `IAction`，使 NPC 能够执行序列化的动作链（如：移动 -> 交互 -> 等待），为未来的复杂规划（GOAP）打下基础。

## 提议的修改

### 1. 动作系统核心 (Action System)
#### [NEW] `src/main/java/com/Kizunad/customNPCs/ai/actions/ActionStatus.java`
- 枚举：`RUNNING`, `SUCCESS`, `FAILURE`。

#### [NEW] `src/main/java/com/Kizunad/customNPCs/ai/actions/IAction.java`
- 原子动作接口。
- `ActionStatus tick(INpcMind mind, LivingEntity entity)`: 执行动作逻辑。
- `void start(INpcMind mind, LivingEntity entity)`: 初始化。
- `void stop(INpcMind mind, LivingEntity entity)`: 清理。
- `boolean canInterrupt()`: 是否可被中断。

### 2. 执行器 (Executor)
#### [NEW] `src/main/java/com/Kizunad/customNPCs/ai/executor/ActionExecutor.java`
- 管理动作队列 (`Queue<IAction>`)。
- `void startPlan(List<IAction> actions)`: 开始新的一组动作。
- `void stopCurrentPlan()`: 停止当前动作。
- `void tick(INpcMind mind, LivingEntity entity)`: 驱动当前动作，处理状态转换（完成 -> 下一个，失败 -> 清空）。

### 3. 基础动作实现 (Basic Actions)
#### [NEW] `src/main/java/com/Kizunad/customNPCs/ai/actions/base/MoveToAction.java`
- 移动到指定坐标或实体。
- 使用原版 `PathNavigation`。

#### [NEW] `src/main/java/com/Kizunad/customNPCs/ai/actions/base/LookAtAction.java`
- 注视指定坐标或实体。

#### [NEW] `src/main/java/com/Kizunad/customNPCs/ai/actions/base/WaitAction.java`
- 等待指定 tick 数。

### 4. 集成到 NpcMind
#### [MODIFY] `src/main/java/com/Kizunad/customNPCs/capabilities/mind/INpcMind.java`
- 添加 `getActionExecutor()` 方法。

#### [MODIFY] `src/main/java/com/Kizunad/customNPCs/capabilities/mind/NpcMind.java`
- 实例化并持有 `ActionExecutor`。
- 在 `tick()` 中调用 `executor.tick()`。

### 5. 验证与测试
#### [NEW] `src/main/java/com/Kizunad/customNPCs/ai/decision/goals/TestPlanGoal.java`
- 一个测试用的 Goal，优先级手动控制。
- `start()` 时提交一个动作序列：`[LookAt(Player), Wait(20), MoveTo(Player), Wait(20)]`。

## 验证计划

### 自动化测试 (GameTests)
- **`ActionTests` 类**：
    - `testActionQueue`: 提交两个 `WaitAction`，验证是否按顺序执行。
    - `testMoveAction`: 验证 NPC 是否移动到了目标点。

### 手动验证
- 使用 `/mind debug` 查看当前正在执行的 Action。
- 观察 NPC 是否按顺序执行动作链。


### 7. 复杂场景集成测试 (Complex Integration Scenarios)
为了验证感知、规划和执行系统的协同工作能力，以及评估性能，我们将构建以下综合测试场景。
**注意：所有实现必须基于真实的 Minecraft 逻辑，严禁使用模拟或 Mock 数据。**

#### [NEW] `src/main/java/com/Kizunad/customNPCs_test/tests/ComplexScenarios.java`

**场景 A: "搜寻者" (The Gatherer)**
- **目标**: NPC 需要获得一个特定的物品（例如：木棍）。
- **环境**: NPC 出生点无物品。在距离 10-15 格处放置一个掉落物 `ItemEntity`。
- **测试流程**:
    1.  **感知**: `VisionSensor` 扫描并发现掉落物，存入 `Memory`。
    2.  **状态更新**: `NpcMind` 将记忆转换为 WorldState (`known_item_location: pos`).
    3.  **规划**: `GoapPlanner` 生成计划: `[MoveToLocation, PickUpItem]`.
    4.  **执行**: NPC 移动并拾取物品。
- **前置需求**:
    - **`PickUpItemAction` (GOAP Action)**:
        - `Preconditions`: `at_location: item_pos`
        - `Effects`: `has_item: true`
        - **实现逻辑**: 检查距离 < 2.0，调用 `entity.take(itemEntity, count)` 动画，直接操作 `entity.getInventory().add(item)` (如果实体有背包) 或 `entity.setItemInHand(item)`。
    - **`GatherItemGoal` (PlanBasedGoal)**:
        - `DesiredState`: `has_item: true`
        - 负责调用 Planner 并执行返回的 Action 队列。

**场景 B: "搬运工" (The Courier)**
- **目标**: 将物品从 A 点搬运到 B 点。
- **环境**: A 点有物品，B 点为空。
- **测试流程**:
    1.  **规划**: 初始状态 `has_item: false`, `item_at_target: false`. 目标 `item_at_target: true`.
    2.  **计划生成**: `[MoveTo(A), PickUp, MoveTo(B), Drop]`.
    3.  **执行**: 验证完整序列的执行。
- **前置需求**:
    - **`DropItemAction` (GOAP Action)**:
        - `Preconditions`: `has_item: true`, `at_location: target_pos`
        - `Effects`: `item_at_target: true`, `has_item: false`
        - **实现逻辑**: 调用 `entity.spawnAtLocation(item)` 生成掉落物，从背包/手中移除物品。

**场景 C: "压力测试" (Performance Stress Test)**
- **目标**: 评估规划器和感知系统在复杂环境下的性能。
- **环境**: 
    - 放置 50 个干扰实体（村民/僵尸）。
    - 放置 20 个掉落物。
    - 迷宫地形（增加寻路成本）。
- **指标**:
    - **Planning Time**: `GoapPlanner.plan()` 的耗时 (ms).
    - **Tick Lag**: `NpcMind.tick()` 对服务器 tick 的影响.
    - **Memory Usage**: 记忆模块条目数量对性能的影响.

## 性能评估计划
1.  **基准测试**: 在空超平坦世界运行场景 A，记录基准耗时。
2.  **压力测试**: 在场景 C 环境下运行场景 A，记录耗时增长。
3.  **分析**: 如果规划耗时超过 50ms (1 tick)，则需要优化 (如：分帧规划、限制搜索深度)。
