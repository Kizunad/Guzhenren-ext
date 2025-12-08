# 蛊真人 AI 扩展模块开发指南

本模块 (`com.Kizunad.guzhenrenext.customNPCImpl.ai`) 旨在为 `customNPCs` 模组提供“蛊真人”相关的 AI 逻辑扩展。通过 `NpcMindRegistry` 机制，将特定的 Action（动作）、Goal（目标）和 Sensor（传感器）注入到通用 NPC 的 AI 系统中，实现业务逻辑与核心框架的解耦。

## 1. 目录结构

*   **`Action/`**: 存放具体的执行动作。
    *   `AbstractGuzhenrenAction`: 蛊真人动作基类，提供通用工具方法（如真元检查）。
*   **`Goal/`** *(待创建)*: 存放 AI 的长期决策目标。
*   **`Sensor/`** *(待创建)*: 存放环境感知逻辑。
*   **`Registery.java`**: 注册中心，负责将上述组件注册到 `customNPCs` 核心。

## 2. 开发流程

### 2.1 添加新的 Action (动作)

Action 是 NPC 执行的最小单元（如“打坐”、“炼蛊”、“攻击”）。

1.  在 `Action/` 目录下新建类，继承 `AbstractGuzhenrenAction`。
2.  实现 `tickInternal(INpcMind mind, Mob mob)` 方法：
    *   返回 `RUNNING`: 动作仍在进行。
    *   返回 `SUCCESS`: 动作完成。
    *   返回 `FAILURE`: 动作失败。
3.  **关键点**: 在 `tickInternal` 中调用蛊真人模组的 API（例如 Capability）来操作真元、背包或蛊虫。
4.  在 `Registery.java` 的 `registerActions()` 中注册。

### 2.2 添加新的 Sensor (传感器)

Sensor 负责收集环境信息并写入 NPC 的记忆 (`MemoryModule`)，供 Goal 决策使用。

1.  在 `Sensor/` 目录下新建类，实现 `com.Kizunad.customNPCs.ai.sensors.ISensor` 接口。
2.  实现 `sense(INpcMind mind, LivingEntity entity, ServerLevel level)` 方法：
    *   扫描周围环境（如：检测附近的炼蛊台、检测敌人身上的蛊虫 BUFF、检测真元浓度）。
    *   使用 `mind.getMemory().rememberShortTerm(...)` 将信息存入记忆。
3.  **示例思路**:
    *   `PrimevalEssenceSensor`: 检测当前真元百分比，写入 `primeval_essence_pct`。
    *   `GuWormSensor`: 扫描周围掉落的野生蛊虫。
4.  在 `Registery.java` 的 `registerSensors()` 中注册。

### 2.3 添加新的 Goal (目标)

Goal 负责决策 NPC "想做什么"。

1.  在 `Goal/` 目录下新建类。
    *   **简单行为**: 实现 `com.Kizunad.customNPCs.ai.decision.IGoal`。
    *   **复杂规划**: 继承 `com.Kizunad.customNPCs.ai.decision.goals.PlanBasedGoal` (GOAP)。
2.  核心方法：
    *   `getPriority()`: 返回 0.0~1.0 的优先级。例如：真元越低，`CultivateGoal`（修炼目标）的优先级越高。
    *   `canRun()`: 检查前置条件（如：是否在战斗中、是否有空窍）。
    *   `start()`: 初始化动作，通常调用 `mind.getActionExecutor().submitPlan(...)` 或 `addAction(...)`。
3.  **示例思路**:
    *   `CultivateGoal`: 当真元 < 30% 且安全时，执行 `CultivateAction`。
    *   `RefineGuGoal`: 当拥有配方材料时，寻找炼蛊台并执行炼蛊动作序列。
4.  在 `Registery.java` 的 `registerGoals()` 中注册。

## 3. 注册与生效

所有新增组件必须在 `Registery.java` 中注册才能生效：

```java
public class Registery {
    // ...
    private static void registerActions() {
        NpcMindRegistry.registerAction("my_new_action", MyNewAction::new);
    }
    
    private static void registerGoals() {
        NpcMindRegistry.registerGoal("cultivate_goal", CultivateGoal::new);
    }
    // ...
}
```

确保在 Mod 主类 (`GuzhenrenExt.java`) 的初始化阶段调用了 `Registery.registerAll()`。

## 4. 常用 API 提示

*   **访问记忆**: `mind.getMemory().getShortTerm("key", Type.class, defaultVal)`
*   **访问状态**: `mind.getStatus()` (原版状态) 或通过 Capability 获取蛊真人状态。
*   **执行动作**: `mind.getActionExecutor().addAction(new MyAction())`
*   **日志**: 推荐使用 `MindLog` 或 `LOGGER` 进行调试。
