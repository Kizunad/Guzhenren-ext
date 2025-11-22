# 实施计划 - NPC 思维基础架构

## 目标描述
初始化自主 NPC 的"大脑"。这包括创建一个 Capability 来存储 NPC 的心智状态（记忆、目标），并使用 Utility AI 方法实现核心的"感知-思考-行动"循环。

## 提议的修改

### 核心 Capability
#### [新建] `src/main/java/com/kiz/guzhenren_ext/capabilities/mind/INpcMind.java`
- 定义 NPC 大脑的契约接口。
- 方法：`getMemory()`、`getGoalSelector()`、`tick(ServerLevel, LivingEntity)`。

#### [新建] `src/main/java/com/kiz/guzhenren_ext/capabilities/mind/NpcMind.java`
- `INpcMind` 的默认实现。
- 存储 `MemoryModule` 和 `UtilityGoalSelector`。
- 将状态序列化/反序列化到 NBT。

#### [新建] `src/main/java/com/kiz/guzhenren_ext/capabilities/mind/NpcMindProvider.java`
- Capability provider，用于将 `NpcMind` 附加到实体。

### 记忆系统
#### [新建] `src/main/java/com/kiz/guzhenren_ext/ai/memory/MemoryModule.java`
- 管理短期和长期记忆条目。
- 支持过期条目（例如："上次看到敌人的位置"10 秒后过期）。

### 决策核心（Utility AI）
#### [新建] `src/main/java/com/kiz/guzhenren_ext/ai/decision/IGoal.java`
- 目标接口。
- `float getPriority(NpcMind mind)`：计算效用分数（0.0 - 1.0）。
- `void start()`、`void tick()`、`void stop()`、`boolean isFinished()`。

#### [新建] `src/main/java/com/kiz/guzhenren_ext/ai/decision/UtilityGoalSelector.java`
- 管理已注册的 `IGoal` 列表。
- `tick()`：定期重新评估优先级，如果发现更高优先级的目标则切换当前活动目标。

### 事件注册
#### [修改] `src/main/java/com/kiz/guzhenren_ext/events/ModEvents.java`（或类似文件）
- 注册 `NpcMind` capability。
- 将 capability 附加到 `EntityGuzhenren`（以及潜在的其他生物）。

## 验证计划

### 自动测试
- **单元测试 `UtilityGoalSelector`**：
    - 创建一个虚拟的 `NpcMind`。
    - 注册两个模拟目标：`GoalA`（优先级 0.5）和 `GoalB`（优先级 0.8）。
    - 调用 `tick()` 并验证 `GoalB` 被选中。
    - 将 `GoalB` 优先级改为 0.2，调用 `tick()`，验证 `GoalA` 被选中。

### 手动验证
- **调试命令**：
    - 创建命令 `/guzhenren mind <entity_uuid>` 来打印 NPC 的当前目标和记忆。
    - 生成一个 NPC。
    - 使用命令验证它有一个 Mind 和一个活动目标（例如"Idle"）。
