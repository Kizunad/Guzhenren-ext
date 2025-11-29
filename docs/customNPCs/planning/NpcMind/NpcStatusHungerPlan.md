# NpcStatus 饥饿系统规划（不改 NpcMind 内部存储）

## 背景与目标
- 现状：仅有 `hunger_restored` 状态占位，`HealGoal` 把食物当治疗，未建饥饿/饱和模型。
- 目标：新增独立的 `NpcStatus`（或同等命名）组件管理饥饿数据，`NpcMind` 只读取并写入世界状态，保持原子动作理念。

## 设计要点
- **状态组件**：`ai/status/NpcStatus` 保存 hunger/saturation/exhaustion，提供 `tick`, `eat`, `addExhaustion`，NBT 序列化；阈值（饥饿/濒饿）从配置读取。
- **Mind 集成**：`INpcMind#getStatus` 暴露；`NpcMind.tick` 调用 `status.tick(level, entity)`；`getCurrentWorldState` 输出 `hunger_percent`、`is_hungry`、`hunger_critical`、`hunger_restored` 等键。
- **状态键**：`WorldStateKeys` 添加饥饿相关常量（如 `HUNGER_PERCENT`、`IS_HUNGRY`、`HUNGER_CRITICAL`）。
- **动作/Goal**：
  - `EatFromInventoryAction`：从 `NpcInventory` 选择可食用物，临时切主/副手使用，回收/回滚未用完的堆。
  - `GoapEatAction`（包装上者）：前置 `has_food`、`item_usable`，效果 `hunger_restored`，代价随饥饿度降低。
  - `SatiateGoal`：基于饥饿阈值触发，优先级随饥饿度提升。
  - `AcquireFoodGoal`（可选）：无食物且饥饿时，规划拾取/交换获取食物，复用现有拾取动作。
- **感知/记忆**：简单方案直接由 `NpcStatus`/Inventory 计算 `has_food` 与阈值状态；如需可拆出 `StatusSensor` 写入记忆。
- **兼容性**：保持与装备/拾取动作的 Inventory 交互一致；序列化使用 `HolderLookup.Provider`。

## 工作步骤（KISS/YAGNI 顺序）
1) 建 `NpcStatus` 类 + NBT + 配置阈值；在 `NpcMind` 中持有并在 tick 调用，但不放饥饿逻辑到 Memory。  
2) 扩充 `WorldStateKeys` 与 `NpcMind.getCurrentWorldState` 映射饥饿状态。  
3) 实现 `EatFromInventoryAction` + `GoapEatAction`，支持从背包取食、回填 `hunger_restored`。  
4) 新建 `SatiateGoal`（以及可选 `AcquireFoodGoal`）并在 `NpcMindRegistry` 注册。  
5) GameTest：设置饥饿值衰减，放入可食物品，验证会进食并恢复；确保不饿时不消费。  
6) 文档/日志：补充行为日志开关与使用说明（可放 docs 或 README 小节）。

## 开放问题
- 饥饿衰减策略：沿用玩家 `FoodData` 规则还是精简版（固定速率 + 运动消耗）？  
- 饱和/再生：是否启用生命回复或仅阻止饥饿伤害？  
- 多来源食物：是否支持药水/特殊食物带状态效果的优先级？  
- 行为冲突：进食时对战斗/移动的中断策略需要明确（允不允许打断）。

## 开放问题决策（精简方案）
- **衰减模型**：采用精简版，固定每 N tick 降饥饿（可配置），战斗/疾跑/挖掘时增加 exhaustion 叠加额外衰减；不完全复刻玩家 `FoodData`，避免耦合。  
- **生命回复**：启用，但简化——当饥饿值 ≥ 80% 时按固定间隔小幅回复生命；饥饿为 0 时按固定间隔掉血（可配置）。  
- **特殊食物**：在 `EatFromInventoryAction` 中保留 `FoodProperties` 附带效果（药水、状态），评分时优先饱食度，其次效果正负权重，诅咒/负面效果降低评分。  
- **可打断性与战斗优先级**：进食动作标记可被打断；`SatiateGoal`/`GoapEatAction` 在 `WorldStateKeys.IN_DANGER` 或已有攻击目标时不触发/自动停止，确保攻击/防御类目标优先级更高。

## 架构图（Mermaid）
```mermaid
graph TD
    subgraph Entity["LivingEntity (NPC)"]
        E1["属性: Health/Effects"]
        E2["行为: move/attack/use"]
    end

    subgraph Mind["NpcMind"]
        M1["MemoryModule"]
        M2["PersonalityModule"]
        M3["UtilityGoalSelector"]
        M4["ActionExecutor"]
        M5["SensorManager"]
        M6["NpcInventory"]
        M7["NpcStatus\n(hunger/saturation/exhaustion\nheal/starve tick, NBT)"]
    end

    subgraph WorldState
        W1["hunger_percent"]
        W2["is_hungry"]
        W3["hunger_critical"]
        W4["hunger_restored"]
        W5["armor_* / threat / item_* ..."]
    end

    subgraph GoalsActions["GOAP/Goals & Actions"]
        G1["SatiateGoal"]
        G2["AcquireFoodGoal (可选)"]
        A1["GoapEatAction"]
        A2["EatFromInventoryAction"]
        A3["Other Actions\n(Move/Attack/Pickup etc.)"]
    end

    E1 -->|tick| M5
    M5 -->|感知结果| M1
    E1 -->|tick| M7
    M7 -->|更新| M1
    M7 -->|状态| WorldState
    M1 -->|读取记忆| WorldState
    WorldState --> M3
    M3 -->|计划/选择| M4
    M4 -->|执行| GoalsActions
    G1 -->|触发: is_hungry && !IN_DANGER| A1
    A1 -->|封装| A2
    A2 -->|取食| M6
    A2 -->|调用 eat(...)| M7
    A2 -->|短期记忆 HUNGER_RESTORED| M1
    A3 -->|消耗/运动| M7
    M7 -->|回血/掉血| E1

    M4 -->|行为| E2
    E2 -->|消耗/战斗| M7
```
