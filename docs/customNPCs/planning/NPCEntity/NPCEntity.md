# 自定义 NPC 实体预计划（草案）

## 目标与范围
- 定义专属 `CustomNpcEntity`，完全使用自研 AI（NpcMind + Sensor + UtilityGoalSelector + ActionExecutor），避免原版 Goal 干扰。
- 在世界中可存活、战斗、逃跑、交互（交易/任务接口预留），支持命令生成与（可选）自然生成。
- 保持原版级别的寻路流畅度，优先复用原版导航，必要时调优 MoveToAction/PathNavigation。

## 设计要点
1) 实体定义与注册
   - 新建 `CustomNpcEntity extends PathfinderMob`（或合适基类），注册 EntityType/Renderer/Attributes（生命、攻击、速度、护甲、防御）。
   - 构造/初始化时清空 `goalSelector/targetSelector`，只运行自有行动体系。
   - 指定正确的 `PathNavigation`（地面/飞行），确保与 MoveToAction 配合。

2) AI 集成
   - 绑定 `NpcMind`、`SensorManager`、`UtilityGoalSelector`、`ActionExecutor`，实体 tick 中驱动 mind.tick。
   - 复用现有 Sensors（Vision/Damage/Safety）与 Goals（Defend/Flee/...），移除原版抢占。
   - 调整 MoveToAction 参数（超时、重算、速度因子）以保障流畅寻路。

3) 生存能力
   - 属性基线：血量、攻击力、移速、护甲（含盾牌防御）。
   - 进食/回血：接入已有食物检测，初期可用被动回血或 TODO 占位。
   - 伤害抗性：火焰/跌落保护（override fall）、环境安全传感器沿用。
   - 威胁响应：Vision/Damage/Safety → triggerInterrupt → UtilityGoalSelector → Defend/Flee/Ranged/Melee。

4) 交互与装备
   - 默认装备：主/副手、护甲、掉落规则；支持弓/弩/盾/药水。
   - 交易接口：预留方法/数据结构，不落地具体交易表。
   - 任务接口：预留事件/数据槽，未来挂任务系统。

5) 生成与持久化
   - 命令生成：更新 `spawn_test_entity` 使用自定义 NPC，附默认装备。
   - 自然生成：配置可选（默认关闭或仅命令），避免污染世界；预留权重/群系配置。
   - 持久化：`saveAdditional/readAdditional` 同步 mind/memory/inventory/cap。

6) 测试与调试
   - GameTest：基础存活不崩溃；威胁响应（逃跑/防御/远程）；命令生成与状态持久；接口存在性（交易/任务占位）。
   - 命令：`inspect`/`spawn_test_entity` 适配新实体；必要的 force goal/action 调试。

## 开发顺序（建议）
1. 实体骨架与注册（EntityType + Renderer + Attributes + 清空原版 AI）。
2. 接入 NpcMind 驱动（tick 绑定、cap/序列化、导航确认）。
3. 命令生成改造为自定义 NPC，默认装备（远程/盾牌可选）。
4. 自然生成开关/配置（可默认禁用，留配置项）。
5. 持久化与 Cap 写入。
6. GameTest 编写/跑通：生成、威胁响应、持久化、接口存在性。
