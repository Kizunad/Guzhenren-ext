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

### 6. 规划层 (Planner Layer - GOAP)
为了让 NPC 能够自动组合动作以达成复杂目标（如：为了"炼丹"，需要先"收集材料"，再"寻找丹炉"），我们将实现简化版的 GOAP (Goal Oriented Action Planning)。

#### [NEW] `src/main/java/com/Kizunad/customNPCs/ai/planner/WorldState.java`
- 描述世界状态的原子事实集合。
- `Map<String, Object> states` (例如: "has_food": true, "is_safe": false)。
- 支持 `match(WorldState other)`: 检查当前状态是否满足目标状态的要求。
- 支持 `apply(WorldState effects)`: 应用动作产生的效果。

#### [NEW] `src/main/java/com/Kizunad/customNPCs/ai/planner/IGoapAction.java`
- 扩展或包装 `IAction`。
- `WorldState getPreconditions()`: 执行此动作需要满足的前置条件。
- `WorldState getEffects()`: 执行此动作后产生的状态变化。
- `float getCost()`: 动作代价（用于 A* 寻路权重）。

#### [NEW] `src/main/java/com/Kizunad/customNPCs/ai/planner/GoapPlanner.java`
- 核心规划器。
- `Queue<IAction> plan(WorldState start, WorldState goal, List<IGoapAction> availableActions)`。
- 使用 A* 算法搜索从 `start` 到 `goal` 的动作路径。

#### [MODIFY] `src/main/java/com/Kizunad/customNPCs/ai/decision/IGoal.java`
- 添加 `WorldState getDesiredState()`: 目标期望达成的最终状态。
- 添加 `boolean isPlanBased()`: 标识此目标是否需要动态规划。

#### [NEW] `src/main/java/com/Kizunad/customNPCs/ai/decision/goals/PlanBasedGoal.java`
- `IGoal` 的抽象基类，自动处理规划逻辑。
- 在 `start()` 中调用 `GoapPlanner` 生成动作队列。
- 将生成的队列提交给 `ActionExecutor` 执行。

## 验证计划 (Planner)

### 自动化测试
- **`PlannerTests` 类**：
    - `testSimplePlan`: 
        - 初始状态: `has_apple=false`
        - 目标状态: `has_apple=true`
        - 动作: `GetAppleAction` (pre: none, eff: has_apple=true)
        - 验证: 规划出 `[GetAppleAction]`。
    - `testChainedPlan`:
        - 初始: `has_wood=false`, `has_planks=false`
        - 目标: `has_planks=true`
        - 动作1: `ChopWood` (eff: has_wood=true)
        - 动作2: `CraftPlanks` (pre: has_wood=true, eff: has_planks=true)
        - 验证: 规划出 `[ChopWood, CraftPlanks]`。
