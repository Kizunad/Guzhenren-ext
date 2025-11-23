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
