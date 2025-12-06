# NPC 任务与委托系统架构 (Task & Quest System Architecture)

## 1. 概述 (Overview)
本系统旨在构建一个**统一且可扩展**的任务架构，既支持 **NPC 自身的行为驱动 (GOAP)**，也支持 **NPC 向玩家发布委托 (Quests)**。
核心理念是：**任务 (Task)** 是对“目标”的抽象描述，而 **执行者 (Executor)** 可能是 NPC（通过 GOAP）或 玩家（通过手动操作）。

## 2. 核心架构 (Core Architecture)

### 2.1 数据模型：Task (任务)
一个任务对象包含以下核心组件：
*   **ID**: 唯一标识符 (ResourceLocation)。
*   **Info**: 标题、描述、图标。
*   **Type**: 任务类型 (Main, Side, Daily, Hidden)。
*   **Objectives (目标)**: 一个或多个 `ITaskObjective`，定义任务完成的条件。
*   **Rewards (奖励)**: 一个或多个 `ITaskReward`，定义完成后的回报。
*   **State**: 任务当前状态 (Active, Completed, Failed)。

### 2.2 任务管理器 (TaskManager)
每个实体（无论是 Player 还是 CustomNpcEntity）都拥有一个 `TaskManager` Capability/DataHolder。
*   **功能**:
    *   接取任务 (Accept)。
    *   监听事件并更新目标进度 (OnEvent)。
    *   检查完成状态 (CheckComplete)。
    *   发放奖励 (DistributeReward)。
*   **持久化**: 需要保存任务 ID、当前进度 (Progress)、完成历史。

---

## 3. 可扩展的目标系统 (Extensible Objectives)

接口 `ITaskObjective` 定义了目标的行为。

```java
public interface ITaskObjective {
    boolean isCompleted(Object progressData);
    float getProgressPercentage();
    void onEvent(TaskEventType type, Object context); // 事件驱动更新
    
    // GOAP 桥接 (针对 NPC 执行者)
    WorldState getDesiredWorldState(); 
}
```

### 3.1 典型目标类型
1.  **KillEntityObjective (击杀特定实体)**
    *   **特性**: 绑定目标 Entity 的 UUID。
    *   **场景**: “悬赏击杀特定名字的强盗首领”。
    *   **机制**: 
        *   该实体可能拥有 `PersistenceRequired` 标签（不刷新）。
        *   监听 `LivingDeathEvent`，校验死者 UUID。
2.  **SubmitItemObjective (提交物品)**
    *   **特性**: 需求特定 Item、NBT、数量。
    *   **场景**: “收集 10 个铁矿石给铁匠”。
    *   **机制**: 
        *   **UI**: 在对话/任务界面提供“提交”槽位。
        *   **Interaction**: 右键 NPC 触发物品检测。
3.  **AttributeObjective (属性达成 - 玩家特有)**
    *   **特性**: 达到特定 HP 上限、等级、或自定义属性（如修仙境界）。
    *   **场景**: “修炼至筑基期”。

---

## 4. 多样化的奖励系统 (Diverse Rewards)

接口 `ITaskReward` 定义奖励发放逻辑。

```java
public interface ITaskReward {
    void apply(ServerPlayer player, CustomNpcEntity npc);
}
```

### 4.1 奖励类型
1.  **ItemReward**: 发放物品（直接进入背包或掉落）。
2.  **AttributeReward**: 
    *   永久增加玩家最大生命值 (`generic.max_health`)。
    *   增加攻击力、防御力等。
    *   修改自定义属性（如 Mana, Stamina）。
3.  **SpecialEquipReward**: 发放带有特殊 NBT 或自定义词条的装备（如“传家宝剑”）。
4.  **ReputationReward**: 增加 NPC 好感度或阵营声望。
5.  **CommandReward**: 执行一条服务端指令（通用性兜底）。

---

## 5. 玩家交互流程 (Player Interaction Flow)

### 5.1 发布任务 (The Quest Giver)
NPC 作为任务发布者，其数据中需存储 `AvailableQuests` 列表。
*   **对话界面 (Dialogue UI)**:
    *   若有可用任务，显示带有“！”或特殊图标的选项。
    *   点击后弹出 **任务详情页 (Task Detail UI)**。
*   **任务详情页 (参考 Taskman.drawio)**:
    *   左侧：任务描述、发布者立绘。
    *   右侧：目标清单 (0/10)、奖励预览。
    *   底部：[接受 (Accept)] [拒绝 (Decline)]。

### 5.2 任务追踪与提交 (Tracking & Submission)
*   **提交物品**:
    *   若任务包含 `SubmitItemObjective`，与该 NPC 对话时，选项中会出现 `[Submit] Iron Ore (5/10)`。
    *   或者打开专用 GUI 拖入物品。
*   **击杀确认**:
    *   击杀指定 UUID 怪物后，系统自动发送 Toast 通知 ("Target Eliminated!")。
    *   任务状态更新为 `Return to NPC`。

---

## 6. Task-GOAP 桥接 (针对 NPC 执行任务)

如果任务是分配给 **NPC 自己** 的（例如 Owner 命令 NPC 去打怪），复用同一套 `Task` 数据结构，但执行路径不同：

*   **组件**: `PerformTaskGoal` (AI Goal)。
*   **逻辑**:
    1.  NPC 获取当前 Task 的 `ITaskObjective`。
    2.  调用 `objective.getDesiredWorldState()` 获取 GOAP 目标状态（例如 `target_is_dead: true`）。
    3.  **GOAP Planner** 计算动作序列 (Find -> Move -> Attack)。
    4.  NPC 执行动作。

**优势**: 一套任务定义，两套执行逻辑（玩家手动玩，NPC 自动玩）。

---

## 7. 开发路线图 (Implementation Roadmap) - 重点：必须遵守：暂时仅实现NPC -> 玩家 发布任务。玩家 -> NPC 发布任务暂时预留接口。

### Phase 1: 核心数据结构
- [ ] 定义 `Task`, `Objective`, `Rpeward` 基础类与 JSON 序列化结构。
- [ ] 实现 `TaskManager` Capability。

### Phase 2: 玩家任务 UI
- [ ] 实现任务接取对话选项。
- [ ] 实现任务详情 GUI (`TaskDetailScreen`)。
- [ ] 实现 HUD 任务追踪器 (可选)。

### Phase 3: 基础目标与奖励实现
- [ ] 实现 `KillEntityObjective` (UUID 追踪)。
- [ ] 实现 `SubmitItemObjective` (物品校验)。
- [ ] 实现 `ItemReward` 和 `AttributeReward`。

### Phase 4: 事件监听与触发
- [ ] 注册 Forge/NeoForge 事件监听器 (Death, Pickup, Tick) 更新任务进度。

### Phase 5: NPC 智能集成 (GOAP Bridge)
- [ ] 完善 `PerformTaskGoal`，使其能解析 `KillEntityObjective` 为 AI 目标。
