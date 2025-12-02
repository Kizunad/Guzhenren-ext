# 目录简介

`src/main/java/com/Kizunad/customNPCs/ai/decision` 负责自定义 NPC 的长期目标决策，结合 Utility AI 与 GOAP（Goal-Oriented Action Planning）。核心思想：每个目标自行计算优先级，选择器按固定节奏评估并切换到最值得执行的目标，同时提供规划型目标的公共基类。

## 核心组件
- `IGoal`：目标接口，要求实现 `getPriority`、`canRun`、`start`、`tick`、`stop`、`isFinished`、`getName`。可选提供 `getDesiredState` 与 `getAvailableActions` 以接入 GOAP 规划。
- `UtilityGoalSelector`：Utility AI 选择器，管理注册目标并在 `tick` 中评估切换。使用滞后与冷却避免抖动；切换时会调用 `ActionExecutor.stopCurrentPlan()` 清理旧计划。
- `goals/`：具体目标实现与示例，包含 `IdleGoal`、`SatiateGoal`、`HealGoal`、`SurvivalGoal`、`FleeGoal`、`DefendGoal`、`EquipArmorGoal`、`PickUpItemGoal`、`SeekShelterGoal`、`WatchClosestEntityGoal`、`TestPlanGoal` 等，以及 GOAP 抽象基类 `PlanBasedGoal`。
- 常用扩展：`CookGoal`（虚拟熔炉烹饪），`HuntGoal`（安全时猎杀弱小目标，低于保命优先级），`CraftItemGoal`（仅供外部 Plan/LLM 注入手工计划，不参与 Utility 评分）。

## 选择器行为速览（UtilityGoalSelector）
- 评估节奏：`EVALUATION_INTERVAL=20`（每秒）定期评估；若当前无目标或目标终止会立即评估。
- 优先级计算：基础优先级乘以 `(1 + personalityModifier)`，个性系统可提升/降低某目标权重。
- 切换阈值：默认滞后 10%；刚切换的目标前 15 tick 需额外 15% 差距才被抢占；`SensorEventType.CRITICAL` 事件可跳过滞后立即切换。
- 冷却：目标被切换下线后（且存在多个目标）进入 40 tick 冷却，防止来回震荡。
- 调试/中断：`forceSwitchTo` 可跳过优先级直接切换，`forceReevaluate` 可在传感器事件时强制重评估。

## GOAP 支持（PlanBasedGoal）
- `start`：收集当前/目标状态，调用 `GoapPlanner` 生成动作序列并提交给 `ActionExecutor`。
- 失败重试：动作执行失败会触发 `replan`，最多重试 3 次；规划失败会使 `isFinished` 立即返回 true。
- 可扩展点：重写 `getCurrentState` 补充目标特定状态；子类需提供 `getDesiredState` 与 `getAvailableActions`，并实现自身的优先级与命名。

## 新增/扩展目标建议
1. 选择基类：简单行为实现 `IGoal`；需要规划的行为继承 `PlanBasedGoal` 以复用规划/重试逻辑。
2. 完善接口：`getName` 要稳定且与个性配置一致；`canRun` 里尽早过滤不可行条件；`getPriority` 只返回当前需要的权重（0-1）。
3. 生命周期：在 `start` 初始化状态/清理旧动作；`tick` 执行核心行为；`stop` 必要时清理计划或重置状态；`isFinished` 返回完成/失败条件。
4. 注册：在 NPC 思维初始化时创建实例并调用 `UtilityGoalSelector.registerGoal(...)`；根据传感器事件调用 `forceReevaluate` 让高紧急度目标及时接管。

## 调试与排查
- 日志：`MindLog` 分 `decision` 与 `planning` 频道，涵盖目标切换、滞后阻止、规划成功/失败等关键事件。
- 现象观察：若目标频繁切换，检查优先级计算是否过于接近或冷却未覆盖；若规划型目标卡住，查看 `replan` 重试与 `ActionExecutor` 的最后动作状态。
