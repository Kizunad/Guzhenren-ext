# NPC 指令与从属系统规划 (Command & Relationship System)

## 1. 核心理念
引入 **"从属关系" (Master-Servant Relationship)** 和 **"指令覆盖" (Command Override)** 机制。
玩家可以通过 **雇佣 (Hire)** 或 **压迫 (Oppress)** 获得 NPC 的控制权。
获得控制权后，玩家可下达指令（Command），该指令产生的 Goal 拥有极高优先级（高于日常，低于生存）。

### 优先级阶梯 (Priority Ladder) - 注: 这里的数值只是展示方法，具体参考实际代码
1.  **Survival (100)**: 逃跑、急救、甚至反抗压迫（"非绝对忠诚"）。
2.  **Command (90)**: 跟随、守卫、工作（玩家下达的强制指令）。
3.  **Daily/Utility (50-70)**: 吃饭、睡觉、闲逛、职业行为。

---

## 2. 关系状态 (Relationship & Memory)

### 2.1 核心记忆键 (Memory Keys)
*   `OWNER_UUID` (UUID): 主人的唯一标识。
*   `RELATIONSHIP_TYPE` (String):
    *   `HIRED`: 雇佣关系。需要定期支付报酬（食物/金钱），忠诚度较为稳定(逾期支付报酬/报酬不足会扣忠诚度，最终导致离开/叛变/打劫)。
    *   `OPPRESSED`: 压迫关系。无需报酬，但忠诚度随时间下降，低血量/高压力时可能逃跑或叛变(攻击主人)。
*   `CURRENT_COMMAND` (String): 当前执行的指令。
    *   `NONE` (默认): 自由行动，由 Utility AI 接管。
    *   `FOLLOW`: 跟随主人。
    *   `GUARD`: 原地守卫 / 守卫主人。
    *   `WORK`: 执行任务（Task System）。
    *   `SIT`: 原地待机（禁用移动）。

### 2.2 状态变更逻辑
*   **Hire**: `ChatOption` -> 扣除玩家资源 -> 写入 `OWNER_UUID` + `HIRED`。
*   **Oppress**: `ChatOption` -> 力量/威慑判定 -> 写入 `OWNER_UUID` + `OPPRESSED`。
*   **Dismiss/Revolt**: 指令清空，关系解除。

---

## 3. AI 决策层 (CommandGoal)

新建 `CommandGoal` (extends `PlanBasedGoal`)。

### 3.1 触发条件
```java
public boolean canRun() {
    return hasOwner(mind) && getCommand(mind) != CommandType.NONE;
}
```

### 3.2 优先级 注: 这里的数值只是展示方法，具体参考实际代码
```java
public float getPriority() {
    // 90.0 高于普通职业行为(50)，但低于 FleeGoal(逃命)
    return 90.0f; 
}
```

### 3.3 动态行为映射 (Dynamic Behavior)
`CommandGoal` 不写死逻辑，而是根据 `CURRENT_COMMAND` 委托给子系统：
*   **FOLLOW**:
    *   DesiredState: `near_owner: true`
    *   Action: `MoveToEntityAction(owner)`
*   **SIT**:
    *   Action: `WaitAction` (持续)
*   **WORK**:
    *   委托给 `TaskManager` (如果我们要深度结合，`CommandGoal` 可以直接包含 `PerformTaskGoal` 的逻辑，或者 `PerformTaskGoal` 提升优先级)。

---

## 4. 交互层设计 (UI & Chat)

### 4.1 对话选项 (Chat Options)
在 `DialoguePanel` 中动态注入选项：
*   若无主人：
    *   `[Hire] I need your help. (Open Hire screen, Hire NPC)` -> `Action: NPC_HIRE`
    *   `[Oppress] Kneel before me! (Strength check)` -> `Action: NPC_OPPRESS`
*   若玩家是主人：
    *   `[Orders] I have instructions...` -> 打开 `CommandScreen` (Owner Opts)。
    *   `[Dismiss] You are free.` -> 清除关系。

### 4.2 指令菜单 (Command Screen)
点击 **Owner Opts** 后打开的二级菜单。
*   **Follow**: 切换 Memory `CURRENT_COMMAND` = `FOLLOW`。
*   **Stay**: 切换 Memory `CURRENT_COMMAND` = `SIT`。
*   **Work**: 切换 Memory `CURRENT_COMMAND` = `WORK` (需先设定工作区域/任务)。

---

## 5. 开发计划 (Execution)

### Phase 1: 后端基础 (Backend)
1.  **Memory**: 确认 MemoryModule 支持 UUID 和 Enum 存储。
2.  **CommandEnum**: 定义 `NpcCommandType` (FOLLOW, SIT, NONE)。
3.  **CommandGoal**: 实现基础框架，优先支持 `FOLLOW`。

### Phase 2: 交互逻辑 (Interaction)
1.  **ChatHandler**: 在 `InteractActionPayload.handle` 中实现 `NPC_HIRE` 和 `NPC_OPPRESS`。
    *   简单起见，先不扣钱/判定，直接成功。
2.  **DialogueProvider**: 动态生成对话选项（检查 `OWNER_UUID`）。

### Phase 3: 指令 UI (Command UI)
1.  **Network**: 新增 `SetNpcCommandPayload` (C2S)。
2.  **UI**: 实现 `NpcCommandScreen` (简单的按钮列表)。
3.  **Link**: 将 `Owner Opts` 按钮连接到这个 Screen。
