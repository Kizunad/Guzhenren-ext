# 决策协调策略方案 (Decision Coordination Strategies)

本文档详细阐述了 Utility AI (决策层) 与 GOAP (规划层) 之间的协调策略，旨在解决“目标切换时机”、“重规划逻辑”以及“决策与执行同步”等核心问题。

## 当前架构分析

*   **Utility Layer (`UtilityGoalSelector`)**: 负责 "做什么" (What)。每 20 ticks (1秒) 评估一次所有 `IGoal` 的优先级，选择得分最高的目标。
*   **Planning Layer (`GoapPlanner` / `PlanBasedGoal`)**: 负责 "怎么做" (How)。`PlanBasedGoal` 在启动时调用规划器生成动作序列，并提交给执行器。
*   **Execution Layer (`ActionExecutor`)**: 负责执行动作序列。

**存在的问题**:
1.  **反应迟钝**: 默认 1 秒的评估间隔可能导致 NPC 对突发威胁（如受到攻击）反应过慢。
2.  **缺乏重规划**: 一旦计划生成并提交，如果环境发生变化导致计划失效（例如门被锁上），当前实现缺乏自动重规划机制。
3.  **抖动风险**: 如果两个目标优先级相近，可能会在每次评估时频繁切换，导致 NPC 行为抽搐。

---

## 方案一：层级化异常处理 (Hierarchical Exception Handling)

**核心思想**: 保持 Utility 为主导，GOAP 负责局部容错。当动作执行失败时，优先在 GOAP 层尝试重规划；只有当重规划也失败时，才向上传递失败给 Utility 层，触发目标切换。

### 机制设计
1.  **执行反馈**: `ActionExecutor` 在动作失败时（返回 `FAILURE`），不应立即清空队列，而是通知当前的 `PlanBasedGoal`。
2.  **局部重规划**: `PlanBasedGoal` 捕获失败事件，基于当前最新的 `WorldState` 尝试重新调用 `GoapPlanner`。
    *   **重试限制**: 设置最大重规划次数 (如 3 次)，防止死循环。
3.  **失败上浮**: 如果重规划失败（找不到路径），`PlanBasedGoal` 标记自身为 `FAILED` 并停止。
4.  **Utility 响应**: `UtilityGoalSelector` 检测到当前目标停止（或失败），立即进行一次 `reevaluate`，选择新的最佳目标（可能是同一个目标的降级策略，也可能是完全不同的目标）。

**优点**: 逻辑清晰，关注点分离。
**缺点**: 无法处理“执行中出现更好机会”的情况，只能处理“执行失败”。

---

## 方案二：带中断的高频监控 (Interrupt-Driven Monitoring) ✅ **已实施**

> **实施日期**: 2025-11-26  
> **版本**: v1.0  
> **测试状态**: ✅ 39个GameTest全部通过 (23.74秒)  
> **架构文档**: [`decision_coordination_architecture.md`](file:///home/kiz/Code/java/Guzhenren-ext/docs/customNPCs/planning/NpcMind/decision_coordination_architecture.md)

**核心思想**: 引入"感知中断"和"高频检查",使 NPC 能在计划执行过程中响应高优先级的环境变化。

### 机制设计
1.  **感知中断 (Sensory Interrupts)**:
    *   在 `ISensor` 中引入 `ImpactLevel`。
    *   当 `DamageSensor` 或 `VisionSensor` 检测到高威胁/高价值事件时，调用 `UtilityGoalSelector.forceReevaluate()`。
    *   这允许 NPC 在 1 秒的评估周期内立即响应突发事件。
2.  **执行前检查 (Pre-Action Check)**:
    *   在 `ActionExecutor` 执行队列中的下一个动作之前，调用 `PlanBasedGoal.validateNextAction()`。
    *   如果当前世界状态不再满足下一个动作的 `Preconditions`，则触发**方案一**中的重规划逻辑。
3.  **滞后阈值 (Hysteresis)**:
    *   在 `UtilityGoalSelector` 中引入切换阈值。
    *   只有当 `NewGoal.Priority > CurrentGoal.Priority + Threshold` 时，才打断当前正在运行的目标。
    *   防止因优先级微小波动导致的频繁切换。

**优点**: 反应灵敏，行为更自然稳定。
**缺点**: 增加了系统复杂度，需要精细调整阈值。

---

## 方案三：连续规划与动态评估 (Continuous Planning)

**核心思想**: 将规划视为一个持续的过程，而非一次性计算。Utility 和 GOAP 深度融合。

### 机制设计
1.  **动态优先级**: `IGoal.getPriority()` 不仅依赖静态上下文，还依赖“当前计划的完成度”。
    *   例如，一个已经执行了 90% 的 `GatherItemGoal` 优先级应该比刚开始时更高（沉没成本/接近奖励），除非有极高威胁。
2.  **后台规划**:
    *   在动作执行期间，`PlanBasedGoal` 可以异步运行规划器，检查是否存在更优路径。
3.  **机会主义执行**:
    *   如果感知系统发现了一个满足当前计划后续步骤的捷径（例如，原本计划去 A 点拿钥匙开 B 门，结果发现 B 门已经被别人打开了），规划器应能识别并跳过中间步骤。

**优点**: 极其智能和流畅。
**缺点**: 实现难度极高，计算开销大，可能不适合当前的 Minecraft Mod 开发规模。

---

## 推荐实施路线

建议采用 **方案二 (带中断的高频监控)** 的简化版作为当前阶段的目标：

1.  **实现滞后 (Hysteresis)**: 修改 `UtilityGoalSelector`，添加 `switchingThreshold` (例如 5.0 或 10%)。
2.  **实现中断 (Interrupts)**: 在 `NpcMind` 中添加 `notifyEvent(EventType)` 接口，供 Sensor 调用，触发 `forceReevaluate`。
3.  **实现基础重规划**: 修改 `PlanBasedGoal`，当 `ActionExecutor` 报告动作失败时，尝试一次重规划。

### 代码变更预览

#### 1. UtilityGoalSelector.java
```java
private static final float HYSTERESIS_THRESHOLD = 0.1f; // 10%

private void reevaluate(INpcMind mind, LivingEntity entity) {
    // ... 计算 bestGoal ...
    
    // 只有当新目标优先级显著高于当前目标时才切换
    float currentPriority = currentGoal != null ? currentGoal.getPriority(mind, entity) : 0;
    if (bestPriority > currentPriority * (1.0f + HYSTERESIS_THRESHOLD)) {
        // 切换目标
    }
}
```

#### 2. PlanBasedGoal.java
```java
// 在 tick 中检查动作状态
@Override
public void tick(INpcMind mind, LivingEntity entity) {
    if (mind.getActionExecutor().getLastActionStatus() == ActionStatus.FAILURE) {
        if (retryCount < MAX_RETRIES) {
            replan(mind, entity);
            retryCount++;
        } else {
            this.planningFailed = true; // 最终失败，交回 Utility
        }
    }
}
```
---

## 实施状态总结

| 方案 | 状态 | 实施日期 | 核心组件 |
|------|------|----------|----------|
| 方案一 | ⚪ 未实施 | - | - |
| **方案二** | ✅ **已完成** | 2025-11-26 | SensorEventType, NpcMind, UtilityGoalSelector, PlanBasedGoal, ActionExecutor, DamageSensor, VisionSensor |
| 方案三 | ⚪ 未实施 | - | - |

### 方案二实施细节

**核心参数**:
- 中断冷却: 10 ticks (0.5秒)
- 滞后阈值: 10%
- 重规划重试: 最多3次
- 近距离威胁判定: < 5格

**测试覆盖**:
- ✅ InterruptMechanismTests (3个测试)
- ✅ HysteresisTests (3个测试)
- ✅ ReplanningTests (3个测试)
- ✅ 总计39个GameTest,100%通过率

**关键文件**:
- [`SensorEventType.java`](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/sensors/SensorEventType.java)
- [`NpcMind.java`](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/capabilities/mind/NpcMind.java)
- [`UtilityGoalSelector.java`](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/decision/UtilityGoalSelector.java)
- [`PlanBasedGoal.java`](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/decision/goals/PlanBasedGoal.java)
- [`ActionExecutor.java`](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/executor/ActionExecutor.java)
- [`DamageSensor.java`](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/sensors/DamageSensor.java)
- [`VisionSensor.java`](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/sensors/VisionSensor.java)

**详细设计文档**: [`strategy_option_2_detail.md`](file:///home/kiz/Code/java/Guzhenren-ext/docs/customNPCs/planning/NpcMind/strategy_option_2_detail.md)

---

## 推荐方案

**当前推荐**: 方案二 (已实施并验证)

方案二提供了最佳的反应性和稳定性平衡:
- ✅ 快速响应突发事件(CRITICAL中断)
- ✅ 防止决策抖动(滞后阈值)
- ✅ 自动适应环境变化(重规划)
- ✅ 决策-执行强同步(队列清理)

**未来展望**:
- 可选: 实施方案三的黑板架构,用于更复杂的多NPC协作场景
- 可选: 添加调试命令支持运行时参数调整
