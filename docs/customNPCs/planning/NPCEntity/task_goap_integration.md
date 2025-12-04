# 任务与 GOAP 系统集成方案 (Task-GOAP Integration)

## 1. 核心理念
**"任务即动态目标状态" (Task as Dynamic Goal State)**。

任务系统不直接控制 NPC 移动或交互，而是通过以下两步影响 AI：
1.  **上下文注入 (Context Injection)**: 将任务参数（如“要杀谁”、“要什么物品”）写入 NPC 的 `MemoryModule`。
2.  **状态映射 (State Mapping)**: 将任务的完成条件翻译为 GOAP 规划器可理解的 `WorldState`（如 `target_dead: true` 或 `has_item_xxx: true`）。

这种设计使得一套通用的 GOAP 动作（移动、攻击、拾取）可以服务于无限种类的任务。

---

## 2. 架构组件

### 2.1 任务接口 (`ITask` & `ITaskObjective`)
任务由一个或多个目标（Objective）组成。

```java
public interface ITaskObjective {
    // 1. 获取该目标完成所需的 GOAP 世界状态
    // 例如：CollectItemObjective 返回 { "has_required_items": true }
    WorldState getDesiredWorldState(INpcMind mind);

    // 2. 任务开始时，将必要数据写入记忆
    // 例如：写入 WorldStateKeys.TARGET_ITEM_TYPE = Items.APPLE
    void injectContext(INpcMind mind);
    
    // 3. 检查任务是否在逻辑上完成（用于更新任务进度 UI，而非 GOAP 逻辑）
    boolean isCompleted(INpcMind mind);
}
```

### 2.2 桥接目标 (`PerformTaskGoal`)
这是 Utility AI 中的一个 Goal，负责连接 Task 和 GOAP。

*   **类型**: `PlanBasedGoal`
*   **优先级**: 
    *   默认: `50` (高于 Idle，低于 Survival/Combat)。
    *   如果任务紧急/限时: 动态提升至 `70+`。
*   **逻辑**:
    ```java
    @Override
    public boolean canRun() {
        return taskManager.hasActiveTask();
    }

    @Override
    public void start() {
        // 1. 注入上下文
        currentTask.getCurrentObjective().injectContext(mind);
        // 2. 获取目标状态并启动规划
        WorldState goalState = currentTask.getCurrentObjective().getDesiredWorldState(mind);
        this.submitPlan(planner.plan(currentWorldState, goalState));
    }
    ```

### 2.3 通用动作改造 (Context-Aware Actions)
GOAP 动作不再硬编码目标，而是从 Memory 中读取。

*   **旧方式**: `GatherAppleAction` (只找苹果)。
*   **新方式**: `GatherItemAction` (通用采集)。
    *   **Precondition**: 无（或 `knows_resource_location: true`）。
    *   **Context**: 从 `Memory` 读取 `TARGET_ITEM_TYPE`。
    *   **Execution**: 寻找并采集记忆中指定的物品类型。
    *   **Effect**: `has_required_items: true` (当背包数量 >= 记忆中的 `TARGET_ITEM_COUNT`)。

---

## 3. 实战案例分析

### 案例 A: "去村庄找村长谈话" (Move & Interact)

1.  **Task**: `TalkToNpcTask(targetUuid: "VillageHead_UUID")`
2.  **PerformTaskGoal**:
    *   **Inject**: 
        *   `Memory.put(TARGET_ENTITY_UUID, "VillageHead_UUID")`
    *   **Goal State**: 
        *   `{ "interaction_completed": true }`
3.  **GoapPlanner**:
    *   寻找能提供 `interaction_completed: true` 的动作 -> **`InteractWithEntityAction`**。
    *   `InteractWithEntityAction` 需要 `target_in_range: true`。
    *   寻找能提供 `target_in_range: true` 的动作 -> **`MoveToEntityAction`**。
    *   **Plan**: `[MoveToEntityAction, InteractWithEntityAction]`。

### 案例 B: "收集 10 个原木" (Resource Gathering)

1.  **Task**: `CollectItemTask(item: LOG, count: 10)`
2.  **PerformTaskGoal**:
    *   **Inject**: 
        *   `Memory.put(TARGET_ITEM_TYPE, LOG)`
        *   `Memory.put(TARGET_ITEM_COUNT, 10)`
    *   **Goal State**: 
        *   `{ "has_required_items": true }`
3.  **GoapPlanner**:
    *   寻找能提供 `has_required_items: true` 的动作 -> **`CheckInventoryAction`** (或者直接由 `GatherAction` 提供)。
    *   **`GatherItemAction`**:
        *   内部逻辑循环：搜索附近 Log -> 移动 -> 破坏 -> 拾取。
        *   每次拾取后检查：`Inventory.count(LOG) >= 10` ?
        *   如果满足，返回 `SUCCESS` 并设置 Effect `has_required_items: true`。

---

## 4. 状态冲突与中断处理

**问题**: 如果 NPC 正在做任务（比如砍树），突然被僵尸攻击怎么办？

**解决方案**: 依赖现有的 `UtilityGoalSelector` 机制。
1.  **CombatGoal (Priority 80)**: 拥有比 `PerformTaskGoal (Priority 50)` 更高的优先级。
2.  **中断**: 当 `DamageSensor` 触发，`NpcMind` 重新评估。
3.  **切换**: `CombatGoal` 抢占执行权，`PerformTaskGoal` 被 `stop()`。
4.  **恢复**: 战斗结束后，`CombatGoal` 结束，`PerformTaskGoal` 重新变为最高优先级，任务继续（GOAP 重新规划，断点续传）。

---

## 5. 开发清单 (Integration Checklist)

### Phase 1: 基础类
- [ ] **ITaskObjective**: 定义 `getDesiredWorldState` 接口。
- [ ] **TaskManager**: 管理任务列表和当前激活任务，集成到 `NpcMind`。
- [ ] **PerformTaskGoal**: 实现桥接逻辑。

### Phase 2: 通用动作泛化
- [ ] **Refactor Actions**: 改造现有动作支持上下文读取。
    - [ ] `MoveToTargetAction` -> 支持从 Memory 读取目标。
    - [ ] `InteractAction` -> 支持从 Memory 读取交互对象。
- [ ] **New WorldKeys**: 添加 `HAS_REQUIRED_ITEMS`, `INTERACTION_COMPLETED`, `TARGET_REACHED` 等通用状态键。

### Phase 3: 任务 UI 集成
- [ ] **Chat Options**: 在 `Dialogue` UI 中添加 "接受任务" 选项。
- [ ] **Dashboard**: 在 `Status` 面板显示当前任务进度。
