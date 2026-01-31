# Bastion 持续迭代路线图（禁止依赖 runClient）

> 本文用于安排后续 Bastion 方向的持续迭代节奏。
>
> **约束**：不使用 `./gradlew runClient` 做验证；默认以 `compileJava + checkstyleMain` 与 `runGameTestServer`（GameTest）为准。

---

## 0. 当前已落地的基础（作为后续工作的地基）

### 0.1 节点体系现状
- `guzhenrenext:bastion_node`：**菌毯（Mycelium）**（贴地蔓延主网，轻量，无 BE）
- `guzhenrenext:bastion_anchor`：**Anchor（子核心/支撑节点）**（tier/dao/generated 属性承载，作为支点）
- 扩张服务（`BastionExpansionService`）：
  - 菌毯扩张：优先使用 frontier 采样，稀疏步长扩张
  - Anchor 自动生成：距离/数量触发 + 冷却 + 失败回退（不阻塞菌毯扩张）

### 0.2 资源池已切换为 effectiveNodes
- effectiveNodes = anchors * 10 + mycelium * 1
- poolGain 与 poolCap **两者都使用 effectiveNodes**

---

## 0.3 40 轮迭代路线图（主线 Backlog）

完整的迭代安排（10 回合整合版 + 30 回合扩展版）见：

- `docs/guzhenrenext/planning/bastion/Bastion_10plus30_Rounds.md`

建议使用方式：
- 前 10 回合：每回合是一个“整合式大回合”，适合一次性推进一个子系统闭环
- 后 30 回合：每回合扩展 1 个类型（守卫/节点/玩家玩法）+ 1 个对抗点 + 1 组测试
- 严格遵守 DoD：compile/checkstyle + GameTest（禁止 runClient）

---

## 1. 持续迭代的“主线”与节奏

> 核心原则：每一轮迭代都必须能通过 **自动化验证**。
> 不要求每轮都新增玩法，但必须：编译过、checkstyle 过、有可复现的测试/验证路径。

### 迭代主线（推荐顺序）

1) **稳定扩张形态**（菌毯贴地蔓延 + Anchor 支点）
2) **连通性与衰败**（不做实时 BFS；先做“离线/周期校验 + 可观察的衰败状态”）
3) **能源节点（第一种）**：光合能源（最易验证）
4) **守卫节点（第一种）**：孵化巢 or 哨站（二选一）
5) **光环节点（正/负极）**：先做一个简单版本（加成/压制）
6) **几丁质外壳（覆盖层）**：轻防护、易清理、可再生

---

## 2. 每轮迭代的“最小交付单元”（DoD）

每个 PR/迭代必须满足：

### 2.1 编译与规范
- `./gradlew compileJava` 通过
- `./gradlew checkstyleMain` 通过

### 2.2 自动化验证（二选一，优先 A）

A) 有对应 GameTest：
- `./gradlew runGameTestServer` 通过（至少包含本轮新增/改动的关键用例）

B) 若暂时无法写 GameTest（仅限“纯数据/纯配置调整”）：
- 提供可执行的最小验证脚本/命令（例如数据生成、json 校验、codec 解析 smoke test）

> 说明：不使用 runClient，因此“目测效果”只能作为补充，不作为交付标准。

---

## 3. 建议的 GameTest 分层（逐步补齐）

### 3.1 扩张与缓存一致性
- 创建一个最小基地（core+初始 anchor），跑若干 tick
- 断言：
  - 菌毯数量增长（BastionData.counts.totalMycelium）
  - Anchor 数量增长或不增长符合阈值与冷却
  - resourcePool 不为 NaN，且在上限内

### 3.2 破坏事件
- 破坏一个 generated Anchor：
  - totalAnchors 递减
  - 缓存移除
  - 不应导致 crash

### 3.3 逆转阵法结构
- 结构判定必须要求四向 Anchor
- 缺 Anchor 时应拒绝启动（返回 false/提示）

---

## 4. 未来两轮的具体推进建议（可直接照做）

### Round 1：配置化扩张拆分（对齐 BastionNodePlan）
目标：把当前硬编码的 Anchor 参数（buildCost/spacing/maxCount/triggerDistance/cooldown）外置到 bastion_type JSON。

TODO：
- `BastionTypeConfig.ExpansionConfig` 拆为 `MyceliumConfig + AnchorConfig`
- JSON 默认值补齐（`data/guzhenrenext/bastion_type/default.json`）
- `BastionExpansionService` 读取配置而不是 Constants
- GameTest：验证配置读取生效（改 default.json 后行为变化）

### Round 2：连通性（简化版）+ 衰败标记
目标：不做实时 BFS，先做“周期性抽样校验 + 失联标记”。

TODO：
- BastionData 增加一个轻量状态：`disconnected` 或 `decayStage`（可选）
- 每 N 秒在服务端跑一次小预算连通检查（从 Anchor 出发标记可达菌毯）
- 不可达区域：停止功能（未来）、并逐步衰败（先做计数衰减即可）
- GameTest：构造一段菌毯链，切断中间，验证远端进入衰败

---

## 5. 约束与注意事项

1) **禁止在 server 可达路径引用 client-only 类**（Screen/Minecraft/渲染）
2) **禁止用 @SuppressWarnings 绕过 Checkstyle**
3) **生成资源目录不直接改**（`src/generated/resources` 不手改）
4) 大改动前必须：
   - `python3 agent_workflow.py assign --agent <NAME> --project guzhenrenext --details "..."`
   - 完成后 `unlock`
