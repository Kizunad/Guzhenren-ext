# Action 模块说明

## 目录与职责
- `IAction.java` / `ActionStatus` / `ActionResult` / `AbstractStandardAction`：动作生命周期与统一基类。
- `base/`：最小基元动作（如 `MoveToAction`、`LookAtAction`、`WaitAction`、`TeleportToAction`），通用能力、不绑定具体业务。
- `common/`：常见业务动作（攻击、交互、拾取/丢弃、用物品、格挡、远程攻击、吃东西等）。
- `goap/`：GOAP 规划下的动作实现，与规划器协同使用。
- `interfaces/`：动作能力接口（攻击、交互、用物品等类型标识）。
- `config/ActionConfig.java`：动作默认参数集中配置。
- `util/NavigationUtil.java`：导航、到达判定与卡住检测的通用工具。

## 动作生命周期（IAction）
1. `start(INpcMind, LivingEntity)`：初始化状态。
2. `tick(INpcMind, LivingEntity)`：每 tick 执行，返回 `RUNNING/SUCCESS/FAILURE`。需要更丰富原因时可重写 `tickWithReason`。
3. `stop(INpcMind, LivingEntity)`：结束清理，无论成功或失败均会调用。
4. `canInterrupt()`：是否允许更高优先级打断。
5. `getName()`：调试/日志用标识。

`AbstractStandardAction` 已内置超时、重试、目标解析、日志节流等能力，优先继承以减少重复逻辑。

## 新增标准 Action 流程（非 GOAP）
1. **选位置**：基础能力放入 `base/`，具体业务放入 `common/`。
2. **选基类**：简单动作实现 `IAction`；需要超时/重试/目标解析时继承 `AbstractStandardAction`。
3. **状态与参数**：避免魔法数，提取为 `private static final` 常量；涉及全局默认值放入 `ActionConfig` 并提供 getter。
4. **服务端校验**：涉及世界操作时确认 `instanceof ServerLevel`，失败时日志并返回 `FAILURE`。
5. **日志**：使用 `MindLog.execution(level, ...)` 或 `LOGGER`（在基类场景）输出关键节点；名称由 `getName()` 提供。
6. **生命周期一致性**：`start` 初始化计数器、冷却；`tick` 只做工作与状态机；`stop` 释放或停用导航等。
7. **中断策略**：根据动作特性返回 `canInterrupt()`，确保与上层调度一致。

## 新增 GOAP Action 流程
1. **选位置**：文件放入 `goap/`，实现 `IGoapAction`。
2. **前置/效果/代价**：实现 `getPreconditions()`、`getEffects()`、`getCost()`，只写当前需要的状态键（YAGNI），命名与现有 `WorldState` 约定保持一致。
3. **执行逻辑**：`tick/start/stop` 与普通动作相同，可复用已有基础能力（如导航、物品使用等）。
4. **状态校验**：在 `tick` 内确保依赖实体/物品有效，否则返回 `FAILURE`；必要时使用 `MindLog` 说明原因，方便规划调试。
5. **可中断性**：根据动作性质决定 `canInterrupt()`，便于规划器在执行期做切换。

## 调试与质量
- 行长 120、禁止通配符导入，遵守 Checkstyle（参见 `config/checkstyle/checkstyle.xml`）。
- 日志等级：INFO 记录开始/结束，WARN 用于失败或兜底行为，DEBUG 保留高频信息（可搭配配置开关）。
- 若新增配置项：在 `ActionConfig` 增加字段与 getter，并考虑默认值命名一致性。
