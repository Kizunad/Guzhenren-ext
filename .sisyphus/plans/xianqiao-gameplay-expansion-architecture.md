# 仙窍系统资料片级扩张计划（全系统深度联动，一次性开放）

## TL;DR
> **Summary**: 构建一个覆盖仙窍+主世界+空窍的资料片级玩法体系，采用“系统骨架先行、数据驱动优先、重档内容并行填充、里程碑闭环验收、最终一次性开放”的长周期路线。  
> **Deliverables**:
> - 作物子系统（从 0 到可规模化扩展）
> - 原版材质约束护栏（90项内容全原版资产复用）
> - 双入口触发（方块采集掉落 + 生物掉落）
> - 深度空窍联动（念头/杀招链路接入）
> - 重档内容包（30+植物/30+生物/12+结构/30+配方/5+核心循环，深浅双轨）
> - 全链路测试门禁与“可破坏升级”发布约束
> **Effort**: XL  
> **Parallel**: YES - 8 waves  
> **Critical Path**: Task 1 → Task 7 → Task 13A → Task 13B → Task 14 → Task 18 → Task 20

## Context
### Original Request
- 继续基于仙窍系统扩张玩法，覆盖配方、玩法、植物、生物、多方块结构。

### Interview Summary
- 交付形态：系统+内容完整一次产出，长周期（C 档）。
- 首发体量：重档（植物15+、生物8+、结构12+、配方120+、核心玩法5+）。
- 主循环优先级：种植炼制 > 灵兽培育 > 结构建造 > 事件挑战。
- 生效范围：全系统联动（仙窍+主世界+空窍）。
- 主世界入口：双入口并行（方块采集掉落 + 生物掉落）。
- 内容实现：优先数据驱动。
- 测试策略：tests-after（但关键框架波次设硬门禁）。
- 发布策略：里程碑开发，最终一次性开放。
- 存档策略：可破坏升级，且仅文档声明不兼容，不提供迁移工具。

### Metis Review (gaps addressed)
- 已纳入：先建作物框架，再上 20种浅度植物（纯数据驱动）与10种深度植物（带专属机制/BlockEntity），避免返工。
- 已纳入：双入口掉落作为基础设施前置 PoC（不是纯内容项）。
- 已纳入：所有新数据 schema 必须具备 Codec + Validator + Reload 统计日志。
- 已纳入：每个里程碑按“可玩闭环”验收，并执行统一门禁命令。
- 已纳入：不兼容升级需要明确发布前文档闸门，避免“静默损档”预期偏差。

## Work Objectives
### Core Objective
在不推翻现有仙窍骨架的前提下，搭建可规模化的数据驱动玩法生产线，并完成跨仙窍/主世界/空窍的深度联动内容首发。

### Deliverables
- D1: 作物系统框架（生长阶段、种植介质、采收、掉落、数据 schema）。
- D2: 双入口触发框架（采集掉落 + 生物掉落，具备开关/频控/日志）。
- D3: 生灵系统扩展（8+ 新生灵，含行为/产出/生态位）。
- D4: 结构与多方块系统扩展（12+ 结构模板与校验链）。
- D5: 配方体系扩展（至少 30 种丹药及关联扩展配方，涵盖 20种常规属性丹药（浅度）与 10种特异效果丹药（深度，强依赖专属Item逻辑），分层与依赖关系可验证）。
- D6: 深度空窍联动（念头/杀招消耗与产出闭环）。
- D7: 事件挑战链（灾劫/区域挑战与经济反馈）。
- D8: 发布门禁文档（不兼容声明、验证报告、上线准入清单）。

### Data-Driven Boundary Contract (v1)
- Reload 生效范围（可数据驱动）：掉落规则、作物成长参数、生灵行为参数、结构激活规则、配方分层图、空窍联动映射。
- 注册期固化范围（不可依赖 datapack 热增类型）：Block/Item/Entity/Menu/BlockEntityType 注册；本次采用“泛型实现 + 参数数据化”，不做运行时动态新增注册表类型。
- 每个新增 schema 必须包含 `schema_version`，版本不兼容时 fail-fast，并输出统一错误前缀 `[EXPANSION_SCHEMA_ERROR]`。
- 每次 reload 必须输出单行统计：`[EXPANSION_STATS] crops={N} creatures={N} structures={N} recipes={N} loops=5`，供 Task 20 自动校验规模达标。

### Core Loop Ledger (L0-L4, total=5)
- L0 入口导入循环：主世界双入口触发 → 首轮材料导入 → 进入仙窍生产起点（Task 4 主验收）。
- L1 种植炼制循环：入口材料 → 作物 → 炼制 → 空窍反馈（Task 14）。
- L2 灵兽结构循环：结构激活 → 生灵产出 → 结构维护/升级反馈（Task 15）。
- L3 事件经济循环：事件挑战 → 风险成本 → 经济奖惩反馈（Task 16）。
- L4 深联动循环：仙窍产物 → 念头/杀招消耗与解锁 → 仙窍反哺（Task 17）。

### Recipe Layering Contract (for T7/T13A)
- 图节点定义：每条配方 `recipe_id` 为一个节点（覆盖 crafting + alchemy）。
- 图边定义：若配方 A 的产物出现在配方 B 的输入中，则建立有向边 `A -> B`。
- 解锁承载：以分层配置承载（基础/进阶/高阶），并声明 `prerequisite_recipe_ids`。
- 校验规则：禁止循环依赖、禁止悬空依赖、禁止“无上游且非基础层”节点。
- 覆盖率口径：`上下游都可追踪的配方数 / 总配方数 >= 80%`。

### Definition of Done (verifiable conditions with commands)
- `./gradlew checkstyleMain` 通过。
- `./gradlew runGameTestServer` 通过（xianqiao 相关批次无失败）。
- `./gradlew build` 通过。
- 新增数据加载器在重载日志中输出成功计数，且无“无法加载”异常栈。
- 核心循环 L0~L4 均至少有 1 条可复现 GameTest 证据链。

### Must Have
- 全系统联动必须包含：主世界触发入口 + 仙窍生产消耗 + 空窍能力反馈。
- 数据驱动优先：内容资产默认 JSON 配置，非硬编码散落常量。
- 重档内容分配采用均衡策略，不偏科单一模块。
- 里程碑必须按玩法闭环验收，不以“仅数量达标”放行。

### Must NOT Have (guardrails, AI slop patterns, scope boundaries)
- 不将“可破坏升级”误读为“可容忍上线前未声明”。
- 不在缺少框架的情况下先批量灌内容（尤其植物与掉落系统）。
- 不引入 client-only 类到 server 可达路径。
- 不新增与目标无关的跨模块重构（tinyui/customnpcs 非必要改动）。
- 不允许引入任何自定义材质/模型文件（`.png`/`.ogg`），必须全量复用 vanilla 资产（利用 NameTag/Particles/SoundEvents 优化视觉）。
- 不允许在浅度波次中编写定制化 Java 逻辑代码（BlockEntity/Goal/Item类），浅度必须 100% 走数据配置。

## Verification Strategy
> ZERO HUMAN INTERVENTION — all verification is agent-executed.
- Test decision: tests-after（关键框架波次采用硬门禁）
- QA policy: 每个任务必须包含 happy + failure 场景
- Evidence: `.sisyphus/evidence/task-{N}-{slug}.{ext}`

### Balance & Performance Budget (default v1)
- 掉落频控：同玩家+同触发源默认冷却 `200 ticks`，并限制每分钟触发额外掉落次数 `<= 6`。
- 倍率上限：种族倍率硬上限 `<= 2.0`，道痕倍率硬上限 `<= 5.0`（与现有守护常量一致）。
- 事件奖惩：经济奖惩修正系数默认限制在 `[0.5, 1.5]`。
- 批处理预算：单次结算循环处理条目上限默认 `<= 16`（超出走下一循环）。
- 任一预算越界视为门禁失败，必须在 Task 18/20 证据中可追踪定位。

## Execution Strategy
### Parallel Execution Waves
> Target: 2-8 tasks per wave（架构前置波允许 2 个任务）. <2 per wave (except final) = under-splitting.
> Extract shared dependencies as Wave-1 tasks for max parallelism.

Wave 1: 架构基线与数据规范（schema/validator/reload pipeline）  
Wave 2: 作物系统框架 + 双入口触发框架  
Wave 3: 生灵系统与结构系统骨架  
Wave 4: 配方分层与经济回路骨架  
Wave 5: 深度空窍联动骨架（念头/杀招，单任务串行高风险波次）  
> **Content Manifest**: 详见 `.sisyphus/plans/xianqiao-gameplay-content-manifest.md`
Wave 6A: 浅度内容批量数据生成（20植物+20生物+20丹药，纯 JSON 与资源复用）
Wave 6B: 深度定制机制实现（10植物+10生物+10丹药，专属 BlockEntity/AI/使用逻辑）
Wave 6C: 多方块结构与核心配方图织网（12+结构与关联配方）  
Wave 7: 里程碑闭环群验收（L1/L2/L3/L4）  
Wave 8: 平衡收敛 + 发布门禁 + 准入审计

### Dependency Matrix (full, all tasks)
- T1 → T2,T3,T4,T5,T6,T7,T9
- T2 → T14
- T3 → T8,T10A,T14
- T4 → T14
- T5 → T8,T12,T15
- T6 → T8,T11A,T15
- T7 → T9,T10A,T13A,T14
- T8 → T9,T14,T16
- T9 → T14,T17
- T10A → T10B,T13A,T14
- T10B → T13B,T14
- T11A → T11B,T13A,T15
- T11B → T13B,T15
- T12 → T13A,T15,T16
- T13A → T14,T17
- T13B → T14,T17
- T14 → T18,T19,T20
- T15 → T18,T19,T20
- T16 → T18,T19,T20
- T17 → T18,T19,T20
- T18 → T20
- T19 → T20
- T20 → Final Verification Wave

### Agent Dispatch Summary (wave → task count → categories)
- Wave 1 → 2 tasks → ultrabrain / deep
- Wave 2 → 2 tasks → unspecified-high / deep
- Wave 3 → 2 tasks → unspecified-high / deep
- Wave 4 → 2 tasks → unspecified-high / deep
- Wave 5 → 1 task → deep
- Wave 6A/B/C → 7 tasks → unspecified-high / deep
- Wave 7 → 4 tasks → deep
- Wave 8 → 3 tasks → deep / writing / unspecified-high

## TODOs
> Implementation + Test = ONE task. Never separate.
> EVERY task MUST have: Agent Profile + Parallelization + QA Scenarios.

- [x] 1. 建立资料片级数据 schema 总线与命名规范

  **What to do**: 设计并落地四类核心数据 schema（植物、生灵、结构、多入口掉落）的统一命名规范、字段约束、版本字段与依赖关系；每类提供 Codec + Validator + Manager 接口约定文档。  
  **Must NOT do**: 不直接开写大批内容 JSON；不把 schema 规则散落到业务逻辑里。

  **Recommended Agent Profile**:
  - Category: `ultrabrain` — Reason: 需要高一致性 schema 设计与跨系统契约稳定性。
  - Skills: `[]`
  - Omitted: `frontend-ui-ux` — 无前端需求。

  **Parallelization**: Can Parallel: NO | Wave 1 | Blocks: 2,3,4,5,6,7,9 | Blocked By: none

  **References** (executor has NO interview context — be exhaustive):
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/kongqiao/niantou/NianTouDataLoader.java:23-71` — Codec + Validator + 注册流程。
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/kongqiao/shazhao/ShazhaoDataLoader.java:23-75` — 同构数据加载模式。
  
  **Acceptance Criteria** (agent-executable only):
  - [ ] 四类 schema 均有可编译的数据模型与解析入口（至少骨架实现）。
  - [ ] 每类 schema 均有独立 validator 并输出可读错误信息。

  **QA Scenarios** (MANDATORY — task incomplete without these):
  ```
  Scenario: schema 编译与加载器骨架通过
    Tool: Bash
    Steps: ./gradlew build
    Expected: 编译通过，无缺失类型/解析器错误
    Evidence: .sisyphus/evidence/task-1-schema-bus.txt

  Scenario: 规范校验链存在
    Tool: Bash
    Steps: ./gradlew checkstyleMain
    Expected: 代码规范通过，validator 类可被静态检查到
    Evidence: .sisyphus/evidence/task-1-schema-bus-error.txt
  ```

  **Commit**: NO | Message: `feat(xianqiao): establish expansion schema contracts` | Files: [xianqiao data schema classes]

- [x] 2. 在模组入口接入新增 ReloadListener 注册链

  **What to do**: 在 `GuzhenrenExt.onAddReloadListeners` 注入新增数据加载器（植物/生灵/结构扩展/掉落触发配置），并统一加载日志统计格式。  
  **Must NOT do**: 不破坏现有 `NianTouDataLoader` / `ShazhaoDataLoader` 顺序与行为。

  **Recommended Agent Profile**:
  - Category: `deep` — Reason: 入口稳定性与数据重载生命周期敏感。
  - Skills: `[]`
  - Omitted: `playwright` — 无浏览器动作。

  **Parallelization**: Can Parallel: YES | Wave 1 | Blocks: 14 | Blocked By: 1

  **References**:
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/GuzhenrenExt.java:87-92` — 当前 listener 注册入口。
  
  **Acceptance Criteria**:
  - [x] 新增 loader 在服务端重载阶段可触发。
  - [x] 日志包含每类数据加载数量统计。

  **QA Scenarios**:
  ```
  Scenario: reload listener 注册生效
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: latest.log 中出现新增 loader 的加载统计行
    Evidence: .sisyphus/evidence/task-2-reload-listeners.txt

  Scenario: 旧 loader 不回归
    Tool: Bash
    Steps: ./gradlew build
    Expected: 现有加载器链路保持可编译且运行不报错
    Evidence: .sisyphus/evidence/task-2-reload-listeners-error.txt
  ```

  **Commit**: NO | Message: `feat(core): register expansion reload listeners` | Files: [GuzhenrenExt.java, new loader files]

- [x] 3. 建立植物作物框架（从 0 到可扩展）

  **What to do**: 新建作物系统基础设施（作物基类、成长状态、种植判定、采收掉落、种子物品、注册入口），支持后续 15+ 植物数据化扩展。  
  **Must NOT do**: 不一次性塞入 15+ 植物；先保证框架闭环。

  **Recommended Agent Profile**:
  - Category: `unspecified-high` — Reason: 新子系统创建，跨 block/item/state/loot。
  - Skills: `[]`
  - Omitted: `quick` — 非小改。

  **Parallelization**: Can Parallel: YES | Wave 2 | Blocks: 8,10A,14 | Blocked By: 1

  **References**:
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/farming/FarmingBlocks.java:15-31` — farming 模块注册入口。
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/farming/FarmingItems.java:17-62` — farming 物品注册入口。
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/block/XianqiaoBlocks.java:27-142` — xianqiao block/item 双注册范式。

  **Acceptance Criteria**:
  - [ ] 作物可种植、可成长、可采收。
  - [ ] 至少 2 个示例作物跑通完整闭环。

  **QA Scenarios**:
  ```
  Scenario: 作物最小闭环
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: 新增作物 GameTest 通过（播种→成长→采收）
    Evidence: .sisyphus/evidence/task-3-crop-framework.txt

  Scenario: 注册与构建稳定
    Tool: Bash
    Steps: ./gradlew build
    Expected: 新增作物系统接入后构建通过
    Evidence: .sisyphus/evidence/task-3-crop-framework-error.txt
  ```

  **Commit**: NO | Message: `feat(xianqiao): introduce crop subsystem foundation` | Files: [new crop blocks/items/tests]

- [x] 4. 建立双入口掉落基础设施（方块采集 + 生物掉落）

  **What to do**: 实现主世界双入口触发框架：方块采集掉落与生物掉落统一走可配置策略（频率、权重、白名单）。输出审计日志，便于平衡追踪。  
  **Must NOT do**: 不写死掉落表在事件处理器常量里；不让掉落绕过配置层。

  **Recommended Agent Profile**:
  - Category: `deep` — Reason: 新基础设施，兼容性与性能风险高。
  - Skills: `[]`
  - Omitted: `artistry` — 常规工程问题。

  **Parallelization**: Can Parallel: YES | Wave 2 | Blocks: 14 | Blocked By: 1

  **References**:
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/tribulation/TribulationTickHandler.java` — xianqiao 侧掉落实体生成参考。
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/event/ChaosErosionHandler.java` — GAME 总线事件订阅范式。

  **Acceptance Criteria**:
  - [ ] 方块采集触发可独立生效且可配置开关。
  - [ ] 生物掉落触发可独立生效且可配置开关。
  - [x] 双入口并行时无重复掉落爆量问题（有频控/互斥策略）。

  **QA Scenarios**:
  ```
  Scenario: 双入口触发验证
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: 方块与生物两类触发测试均通过
    Evidence: .sisyphus/evidence/task-4-dual-drop-pipeline.txt

  Scenario: 性能与频控守护
    Tool: Bash
    Steps: ./gradlew build
    Expected: 频控/权重配置类编译通过，无死循环或重复注册
    Evidence: .sisyphus/evidence/task-4-dual-drop-pipeline-error.txt
  ```

  **Commit**: NO | Message: `feat(xianqiao): add dual-trigger overworld drop pipeline` | Files: [new drop handlers/config models/tests]

- [x] 5. 建立结构建造框架（玩家手搓多方块检测与激活）

  **What to do**: 在现有结构模板资源点机制基础上，新增“玩家手动搭建多方块→校验→激活→失效回退”框架，支持后续 12+ 结构统一扩展。  
  **Must NOT do**: 不仅依赖自动放置结构模板；必须支持手搓结构主链。

  **Recommended Agent Profile**:
  - Category: `unspecified-high` — Reason: 结构状态机与校验逻辑复杂。
  - Skills: `[]`
  - Omitted: `quick` — 非局部改动。

  **Parallelization**: Can Parallel: YES | Wave 3 | Blocks: 8,12,15 | Blocked By: 1

  **References**:
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/resource/ResourceControllerBlockEntity.java:230-334` — 当前已有的状态更新与乘算链路，以此为基准扩展结构。
    - Rule: 结构相关的数据模式将完全在 Task 1 中全新设计（保存至 `data/guzhenrenext/structure/`）。

  **Acceptance Criteria**:
  - [ ] 至少 2 种手搓结构可被识别并进入激活态。
  - [ ] 破坏关键块后结构立即失效并停止产出。

  **QA Scenarios**:
  ```
  Scenario: 手搓结构激活闭环
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: 结构搭建→激活→产出→拆毁失效测试通过
    Evidence: .sisyphus/evidence/task-5-multiblock-framework.txt

  Scenario: 结构误判防护
    Tool: Bash
    Steps: ./gradlew build
    Expected: 错误摆放不会误触发激活，相关守护逻辑编译通过
    Evidence: .sisyphus/evidence/task-5-multiblock-framework-error.txt
  ```

  **Commit**: NO | Message: `feat(xianqiao): add player-built multiblock framework` | Files: [resource/multiblock framework files]

- [x] 6. 建立生灵行为框架与生态位定义

  **What to do**: 扩展 `XianqiaoEntities` 与对应实体行为基类，定义生灵生态位（产出、环境偏好、危险性、驯化/交互）与数据化参数入口。  
  **Must NOT do**: 不直接硬编码 8+ 生灵全部行为；先抽象统一行为骨架。

  **Recommended Agent Profile**:
  - Category: `deep` — Reason: AI 行为、生态参数与产出耦合。
  - Skills: `[]`
  - Omitted: `playwright` — 无 UI 自动化。

  **Parallelization**: Can Parallel: YES | Wave 3 | Blocks: 8,11A,15 | Blocked By: 1

  **References**:
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/spirit/XianqiaoEntities.java:86-207` — 实体注册与属性绑定。
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/spirit/ClusterNpcEntity.java:196-320` — 行为/状态数据同步参考。

  **Acceptance Criteria**:
  - [ ] 生灵基础行为框架支持至少 3 种行为策略切换。
  - [ ] 至少 2 个新生灵作为示例接入并可稳定生成/交互。

  **QA Scenarios**:
  ```
  Scenario: 生灵行为骨架验证
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: 生灵生成、行为切换、基本交互测试通过
    Evidence: .sisyphus/evidence/task-6-creature-framework.txt

  Scenario: 实体注册回归
    Tool: Bash
    Steps: ./gradlew build
    Expected: 实体注册与属性事件无冲突，构建通过
    Evidence: .sisyphus/evidence/task-6-creature-framework-error.txt
  ```

  **Commit**: NO | Message: `feat(xianqiao): introduce creature ecology framework` | Files: [xianqiao spirit entity files]

- [x] 7. 配方体系分层改造（基础/进阶/高阶）

  **What to do**: 在现有炼丹与 crafting 基础上建立统一配方分层体系与依赖图（120+ 配方可扩展），明确解锁条件、产出等级与冲突检查。  
  **Must NOT do**: 不继续无序追加 recipe json；必须先有层级与依赖规则。

  **Recommended Agent Profile**:
  - Category: `unspecified-high` — Reason: 高数量配置与依赖一致性管理。
  - Skills: `[]`
  - Omitted: `quick` — 涉及体系设计。

  **Parallelization**: Can Parallel: YES | Wave 4 | Blocks: 9,10A,13A,14 | Blocked By: 1

  **References**:
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/alchemy/service/AlchemyService.java:441-676` — 分层配方定义示例。
  - Pattern: `src/main/resources/data/guzhenrenext/recipes/storage_gu.json` — 基础配方文件范式。
  - Pattern: `src/main/resources/data/guzhenrenext/recipes/cluster_npc_spawn_egg.json` — 无序配方范式。

  **Acceptance Criteria**:
  - [x] 配方层级规则可追踪且可机器校验（循环依赖/缺失依赖可检测）。
  - [x] 至少 20 条示例配方完成分层接入并通过加载。

  **QA Scenarios**:
  ```
  Scenario: 配方分层加载成功
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: 配方层级校验测试通过，无循环依赖错误
    Evidence: .sisyphus/evidence/task-7-recipe-layering.txt

  Scenario: 构建与规范回归
    Tool: Bash
    Steps: ./gradlew checkstyleMain && ./gradlew build
    Expected: 命令通过，配方体系改造未破坏现有构建
    Evidence: .sisyphus/evidence/task-7-recipe-layering-error.txt
  ```

  **Commit**: NO | Message: `feat(xianqiao): add layered recipe progression model` | Files: [alchemy/recipe data and validators]

- [x] 8. 建立资源经济核心回路（资源点×作物×生灵）

  **What to do**: 将 `ResourceControllerBlockEntity`、作物产出、生灵产出统一接入资源经济总线，定义基础产出、加成来源、衰减/上限规则与节流机制。  
  **Must NOT do**: 不让三条产线互相绕开平衡约束；不允许无限指数增长。

  **Recommended Agent Profile**:
  - Category: `deep` — Reason: 经济模型与性能稳定性并重。
  - Skills: `[]`
  - Omitted: `artistry` — 以稳态设计为主。

  **Parallelization**: Can Parallel: YES | Wave 4 | Blocks: 9,14,16 | Blocked By: 3,5,6

  **References**:
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/resource/ResourceControllerBlockEntity.java:230-334` — 进度/效率/时速乘算链。
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/spirit/ClusterNpcEntity.java:74-103` — 产出倍率与上限守护思路。

  **Acceptance Criteria**:
  - [ ] 统一经济回路可输出可解释的产出分解（来源与倍率）。
  - [ ] 在边界输入下（高道痕/高时速）仍满足产出上限约束。

  **QA Scenarios**:
  ```
  Scenario: 经济回路稳定性
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: 高倍率场景下产出仍被上限约束，测试通过
    Evidence: .sisyphus/evidence/task-8-economy-loop.txt

  Scenario: 经济回路编译回归
    Tool: Bash
    Steps: ./gradlew build
    Expected: 经济总线改造编译通过
    Evidence: .sisyphus/evidence/task-8-economy-loop-error.txt
  ```

  **Commit**: NO | Message: `feat(xianqiao): unify economy loop across systems` | Files: [resource/crop/spirit economy files]

- [x] 9. 深度接入空窍联动（念头/杀招）

  **What to do**: 设计并实现 xianqiao 内容与 kongqiao 念头/杀招的双向联动：xianqiao 产物可作为念头/杀招消耗或解锁条件，空窍状态反哺仙窍产出与事件。  
  **Must NOT do**: 不做仅数值挂钩的浅层联动；必须有可玩闭环。

  **Recommended Agent Profile**:
  - Category: `deep` — Reason: 跨系统契约复杂，风险高。
  - Skills: `[]`
  - Omitted: `quick` — 非简单映射。

  **Parallelization**: Can Parallel: NO | Wave 5 | Blocks: 14,17 | Blocked By: 1,7,8

  **References**:
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/kongqiao/niantou/NianTouDataLoader.java` — 念头数据驱动入口。
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/kongqiao/shazhao/ShazhaoDataLoader.java` — 杀招数据驱动入口。
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/network/GuzhenrenExtNetworking.java:32-127` — 已有跨系统网络通道。

  **Acceptance Criteria**:
  - [x] 至少 1 条“仙窍产出→念头/杀招解锁/强化→仙窍反哺”闭环打通。
  - [x] 联动数据配置可通过 reload 生效，并具备校验错误输出。

  **QA Scenarios**:
  ```
  Scenario: 深度联动闭环验证
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: 跨系统联动 GameTest 全通过
    Evidence: .sisyphus/evidence/task-9-kongqiao-deep-link.txt

  Scenario: 网络与数据一致性
    Tool: Bash
    Steps: ./gradlew build
    Expected: 联动网络载荷与服务端逻辑编译通过
    Evidence: .sisyphus/evidence/task-9-kongqiao-deep-link-error.txt
  ```

  **Commit**: NO | Message: `feat(xianqiao): integrate deep kongqiao linkage loops` | Files: [xianqiao/kongqiao/network related files]

- [x] 10A. 浅度植物包生成 (20种)

  **What to do**: 按照 `xianqiao-gameplay-content-manifest.md` 中的 1-20 号植物，生成对应的原版模型映射、JSON 配方与战利品表。
  **Must NOT do**: 严禁编写任何专有 BlockEntity 类。严禁引入 `.png`。

  **Recommended Agent Profile**:
  - Category: `unspecified-high`
  - Skills: `[]`
  - Omitted: `deep`

  **Parallelization**: Can Parallel: YES | Wave 6A | Blocks: 10B,13A,14 | Blocked By: 3,7

  **References**:
  - Pattern: `xianqiao-gameplay-content-manifest.md`
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/farming/FarmingBlocks.java`

  **Acceptance Criteria**:
  - [x] 20 种浅度植物数据加载无误，且全部成功复用原版材质（如 `wheat`, `poppy`）。

  **QA Scenarios**:
  ```
  Scenario: 浅度植物包加载验证
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: 植物相关测试通过，服务端无缺贴图或 json 解析报错
    Evidence: .sisyphus/evidence/task-10A-shallow-plants.txt
  ```
  **Commit**: NO | Message: `feat(xianqiao): add 20 shallow plant data` | Files: [data/farming]

- [x] 10B. 深度植物机制实现 (10种)

  **What to do**: 按照 manifest 中的 21-30 号植物机制（如食人花吸血、引雷草雷击），编写专属 `BlockEntity` 或事件劫持代码。
  **Must NOT do**: 严禁自创材质，必须通过粒子（Particles）或 NameTag 来做视觉区分。

  **Recommended Agent Profile**:
  - Category: `deep`
  - Skills: `[]`
  - Omitted: `quick`

  **Parallelization**: Can Parallel: YES | Wave 6B | Blocks: 13B,14 | Blocked By: 10A

  **References**:
  - Pattern: `xianqiao-gameplay-content-manifest.md`

  **Acceptance Criteria**:
  - [ ] 10 种深度植物各具备独立的 Java 逻辑代码。
  - [x] 无自定义渲染类或 `.png`。

  **QA Scenarios**:
  ```
  Scenario: 深度植物机制验证
    Tool: Bash
    Steps: ./gradlew build
    Expected: 深度植物类编译通过，并符合服务端运行标准
    Evidence: .sisyphus/evidence/task-10B-deep-plants.txt
  ```
  **Commit**: NO | Message: `feat(xianqiao): implement 10 deep plant mechanics` | Files: [src/.../farming/deep]

- [x] 11A. 浅度生灵包生成 (20种)

  **What to do**: 按照 manifest 的 1-20 号生灵，利用泛型 `ClusterNpcEntity` 配合 NBT/JSON 变体注册 20 种不同掉落与参数的生灵。
  **Must NOT do**: 不写任何新的 AI Goal。严禁自定义 `.png`。

  **Recommended Agent Profile**:
  - Category: `unspecified-high`
  - Skills: `[]`
  - Omitted: `deep`

  **Parallelization**: Can Parallel: YES | Wave 6A | Blocks: 11B,13A,15 | Blocked By: 6

  **References**:
  - Pattern: `xianqiao-gameplay-content-manifest.md`
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/spirit/XianqiaoEntities.java`

  **Acceptance Criteria**:
- [x] 20 种泛型变体注册成功，复用原版实体（如 `pig`, `wolf`）。

  **QA Scenarios**:
  ```
  Scenario: 浅度生灵包验证
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: 生灵注册表启动无误
    Evidence: .sisyphus/evidence/task-11A-shallow-creatures.txt
  ```
  **Commit**: NO | Message: `feat(xianqiao): add 20 shallow creature variants` | Files: [data/spirit]

- [x] 11B. 深度生灵机制实现 (10种)

  **What to do**: 按照 manifest 的 21-30 号生灵，编写特定的继承类与 AI Goals（如盗宝鼬下钻、仙窍镇灵阶段变形）。
  **Must NOT do**: 不写自定义 Render 类，所有视觉变化必须通过缩放属性、粒子或现有原版标签实现。

  **Recommended Agent Profile**:
  - Category: `deep`
  - Skills: `[]`
  - Omitted: `quick`

  **Parallelization**: Can Parallel: YES | Wave 6B | Blocks: 13B,15 | Blocked By: 11A

  **References**:
  - Pattern: `xianqiao-gameplay-content-manifest.md`

  **Acceptance Criteria**:
  - [x] 10 种深度生灵对应的 AI Goal 与交互逻辑实现完毕。

  **QA Scenarios**:
  ```
  Scenario: 深度生灵编译验证
    Tool: Bash
    Steps: ./gradlew build
    Expected: 包含所有新 AI 目标的实体代码编译通过
    Evidence: .sisyphus/evidence/task-11B-deep-creatures.txt
  ```
  **Commit**: NO | Message: `feat(xianqiao): implement 10 deep creature AIs` | Files: [src/.../spirit/deep]

- [x] 12. 重档内容填充：结构包（12+）

  **What to do**: 批量构建 12+ 结构内容（模板/手搓蓝图/激活条件/产出池），并与资源点与事件回路绑定。  
  **Must NOT do**: 不允许结构仅做装饰；每个结构必须定义功能位与失效条件。

  **Recommended Agent Profile**:
  - Category: `unspecified-high` — Reason: 结构数量大、校验复杂。
  - Skills: `[]`
  - Omitted: `quick` — 非轻量任务。

  **Parallelization**: Can Parallel: YES | Wave 6B | Blocks: 13A,15,16 | Blocked By: 5

  **References**:
  - Reference: 本任务强依赖 Task 5 中新建的结构校验框架逻辑。
  - Note: 数据文件将存放在 `src/main/resources/data/guzhenrenext/structure/` 下。

  **Acceptance Criteria**:
  - [ ] 结构条目数 ≥ 12，且全部具备激活与失效规则。
  - [ ] 至少 6 个结构接入资源产出链并可验证产出。

  **QA Scenarios**:
  ```
  Scenario: 结构包激活验证
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: 结构激活/失效/产出测试通过
    Evidence: .sisyphus/evidence/task-12-structure-content-pack.txt

  Scenario: 结构配置回归
    Tool: Bash
    Steps: ./gradlew build
    Expected: 结构模板与校验逻辑接入后构建通过
    Evidence: .sisyphus/evidence/task-12-structure-content-pack-error.txt
  ```

  **Commit**: NO | Message: `feat(xianqiao): add multiblock structure content pack` | Files: [resource structures/data]

- [x] 13A. 浅度丹药与核心配方网生成 (20种 + 配方)

  **What to do**: 按照 manifest 的 1-20 号丹药，复用原版图标（如 `apple`, `sugar`），只赋予常规 Buff。同时生成绑定前置浅度动植物产物的 100+ 条配方网。
  **Must NOT do**: 不写自定义 Item 类（继承自带的常规 Buff 逻辑即可）。

  **Recommended Agent Profile**:
  - Category: `unspecified-high`
  - Skills: `[]`
  - Omitted: `deep`

  **Parallelization**: Can Parallel: YES | Wave 6C | Blocks: 14,17 | Blocked By: 7, 10A, 11A, 12

  **References**:
  - Pattern: `xianqiao-gameplay-content-manifest.md`
  - Pattern: `src/main/resources/data/guzhenrenext/recipes/`

  **Acceptance Criteria**:
  - [x] 20 种浅度丹药配方及上下游依赖连通。

  **QA Scenarios**:
  ```
  Scenario: 浅度配方网连通性
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: Recipe validator 验证通过
    Evidence: .sisyphus/evidence/task-13A-shallow-recipes.txt
  ```
  **Commit**: NO | Message: `feat(xianqiao): add 20 shallow pills and recipe network` | Files: [data/recipes]

- [x] 13B. 深度丹药机制实现 (10种)

  **What to do**: 按照 manifest 21-30 号丹药（如生死造化丹、强制破境丹），编写专属 `Item` 类并重写 `finishUsingItem` 等逻辑，介入跨系统总线（Guzhenren API）。
  **Must NOT do**: 严禁脱离服务端逻辑（不可依赖 client 事件触发）。

  **Recommended Agent Profile**:
  - Category: `deep`
  - Skills: `[]`
  - Omitted: `quick`

  **Parallelization**: Can Parallel: YES | Wave 6B | Blocks: 14,17 | Blocked By: 10B, 11B

  **References**:
  - Pattern: `xianqiao-gameplay-content-manifest.md`
  - Pattern: `GuzhenrenVariableModifierService`

  **Acceptance Criteria**:
  - [x] 10 种深度丹药均具备独立 Java 逻辑，能产生复杂的空窍/状态副作用。

  **QA Scenarios**:
  ```
  Scenario: 深度丹药副作用挂载
    Tool: Bash
    Steps: ./gradlew build
    Expected: 所有自定义 Item 编译通过并完成注册映射
    Evidence: .sisyphus/evidence/task-13B-deep-pills.txt
  ```
  **Commit**: NO | Message: `feat(xianqiao): implement 10 deep pill mechanics` | Files: [src/.../item/deep]

- [x] 14. 里程碑闭环 A：种植炼制主循环验收

  **What to do**: 打通“主世界双入口材料→仙窍种植→炼制配方→空窍反馈”的首条核心闭环，并形成里程碑 A 证据包。  
  **Must NOT do**: 不以局部通过替代闭环通过。

  **Recommended Agent Profile**:
  - Category: `deep` — Reason: 跨系统首条闭环验收。
  - Skills: `[]`
  - Omitted: `quick` — 需系统级验证。

  **Parallelization**: Can Parallel: NO | Wave 7 | Blocks: 18,19,20 | Blocked By: 2,3,4,7,8,9,10A,10B,13A,13B

  **References**:
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext_test/xianqiao/ClusterFarmingTest.java` — xianqiao 闭环测试写法。
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext_test/xianqiao/TerrainSamplingGameTests.java` — 多场景 GameTest 风格。

  **Acceptance Criteria**:
  - [x] 闭环 A 全链路 GameTest 通过。
  - [ ] 产出与消耗平衡在阈值范围内（无爆量）。

  **QA Scenarios**:
  ```
  Scenario: 闭环 A 全链路
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: 闭环 A 相关测试全部通过
    Evidence: .sisyphus/evidence/task-14-loop-a.txt

  Scenario: 闭环 A 构建守门
    Tool: Bash
    Steps: ./gradlew checkstyleMain && ./gradlew build
    Expected: 规范与构建均通过
    Evidence: .sisyphus/evidence/task-14-loop-a-error.txt
  ```

  **Commit**: NO | Message: `test(xianqiao): validate loop A planting-alchemy pipeline` | Files: [xianqiao tests]

- [x] 15. 里程碑闭环 B：灵兽培育 + 结构建造联动验收

  **What to do**: 打通“结构激活→生灵生态产出→结构升级/维护反馈”闭环，并验证破坏/失效恢复路径。  
  **Must NOT do**: 不忽略失败路径（结构破坏、生灵缺失、产出停滞）。

  **Recommended Agent Profile**:
  - Category: `deep`
  - Skills: `[]`
  - Omitted: `frontend-ui-ux`

  **Parallelization**: Can Parallel: YES | Wave 7 | Blocks: 18,19,20 | Blocked By: 5,6,11A,11B,12

  **References**:
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/resource/ResourceControllerBlockEntity.java` — 结合 Task 5 改造后的控制器。
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/spirit/ClusterNpcEntity.java:196-320` — 生灵行为状态与产出联动。

  **Acceptance Criteria**:
  - [x] 闭环 B GameTest 全通过。
  - [x] 结构/生灵任一节点失效都可被正确检测并回退。

  **QA Scenarios**:
  ```
  Scenario: 闭环 B 联动验证
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: 闭环 B 测试通过
    Evidence: .sisyphus/evidence/task-15-loop-b.txt

  Scenario: 失效回退验证
    Tool: Bash
    Steps: ./gradlew build
    Expected: 失效回退相关逻辑编译并测试可达
    Evidence: .sisyphus/evidence/task-15-loop-b-error.txt
  ```

  **Commit**: NO | Message: `test(xianqiao): validate loop B creature-structure coupling` | Files: [xianqiao tests]

- [x] 16. 里程碑闭环 C：事件挑战与经济反馈验收

  **What to do**: 打通“事件挑战（灾劫/区域事件）→资源风险→经济补偿/惩罚”的闭环，验证高压场景稳定性。  
  **Must NOT do**: 不只测成功路线；必须覆盖失败惩罚路径。

  **Recommended Agent Profile**:
  - Category: `deep`
  - Skills: `[]`
  - Omitted: `quick`

  **Parallelization**: Can Parallel: YES | Wave 7 | Blocks: 18,19,20 | Blocked By: 8,12

  **References**:
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/tribulation/TribulationManager.java`
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/tribulation/TribulationTickHandler.java`

  **Acceptance Criteria**:
  - [x] 事件挑战闭环测试通过（成功/失败路径）。
  - [x] 奖惩与经济回路的数值影响符合预设范围。

  **QA Scenarios**:
  ```
  Scenario: 事件挑战闭环
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: 事件挑战测试通过
    Evidence: .sisyphus/evidence/task-16-loop-c.txt

  Scenario: 惩罚路径回归
    Tool: Bash
    Steps: ./gradlew build
    Expected: 惩罚路径逻辑构建通过且可被测试调用
    Evidence: .sisyphus/evidence/task-16-loop-c-error.txt
  ```

  **Commit**: NO | Message: `test(xianqiao): validate loop C event-economy feedback` | Files: [tribulation/event tests]

- [x] 17. 里程碑闭环 D：深度空窍联动综合验收

  **What to do**: 验证“仙窍内容→念头/杀招→仙窍反哺”深度联动的稳定闭环，包括数据加载、网络同步、服务端判定一致性。  
  **Must NOT do**: 不将其降级为纯数值映射测试；必须覆盖真实联动链。

  **Recommended Agent Profile**:
  - Category: `deep`
  - Skills: `[]`
  - Omitted: `quick`

  **Parallelization**: Can Parallel: YES | Wave 7 | Blocks: 18,19,20 | Blocked By: 9,13A,13B

  **References**:
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/network/GuzhenrenExtNetworking.java:32-127`
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/kongqiao/niantou/NianTouDataLoader.java`
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/kongqiao/shazhao/ShazhaoDataLoader.java`

  **Acceptance Criteria**:
  - [x] 深度联动闭环全通过（数据+网络+服务端判定）。
  - [x] 无跨系统状态不同步或重复结算问题。

  **QA Scenarios**:
  ```
  Scenario: 深度联动综合链路
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: 深度联动相关测试全部通过
    Evidence: .sisyphus/evidence/task-17-loop-d.txt

  Scenario: 同步一致性回归
    Tool: Bash
    Steps: ./gradlew build
    Expected: 网络载荷与服务端逻辑构建通过
    Evidence: .sisyphus/evidence/task-17-loop-d-error.txt
  ```

  **Commit**: NO | Message: `test(xianqiao): validate loop D deep kongqiao linkage` | Files: [xianqiao/kongqiao tests]

- [x] 18. 做平衡收敛与性能守门（重档内容）

  **What to do**: 对重档内容进行统一平衡收敛（产出速率、掉落率、事件强度、联动收益），并设置性能守门（tick 预算、批处理策略）。  
  **Must NOT do**: 不用“感觉调参”替代证据化调参。

  **Recommended Agent Profile**:
  - Category: `deep`
  - Skills: `[]`
  - Omitted: `artistry`

  **Parallelization**: Can Parallel: YES | Wave 8 | Blocks: 20 | Blocked By: 14,15,16,17

  **References**:
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/resource/ResourceControllerBlockEntity.java:230-334` — 效率乘算与上限护栏。
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/spirit/ClusterNpcEntity.java:74-103` — 倍率上限守护。
  - Pattern: `src/main/java/com/Kizunad/guzhenrenext/xianqiao/tribulation/TribulationManager.java` — 事件强度调度参数。

  **Acceptance Criteria**:
  - [x] 关键经济指标在预设区间内。
  - [x] 关键场景无明显 tick 爆量风险（有上限守护与节流机制）。

  **QA Scenarios**:
  ```
  Scenario: 平衡收敛验证
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: 经济与事件相关回归测试通过
    Evidence: .sisyphus/evidence/task-18-balance-convergence.txt

  Scenario: 性能守门验证
    Tool: Bash
    Steps: ./gradlew build
    Expected: 性能守护逻辑构建通过，关键限幅常量可追踪
    Evidence: .sisyphus/evidence/task-18-balance-convergence-error.txt
  ```

  **Commit**: NO | Message: `chore(xianqiao): tune balance and performance guardrails` | Files: [xianqiao tuning files]

- [x] 19. 发布前文档闸门：不兼容升级声明与运营说明

  **What to do**: 编写并校验发布文档：明确“本版本与旧存档不兼容”，给出升级建议、回滚建议与风险提示；不提供迁移工具。  
  **Must NOT do**: 不允许模糊表述；必须在发布说明置顶。

  **Recommended Agent Profile**:
  - Category: `writing`
  - Skills: `[]`
  - Omitted: `deep`

  **Parallelization**: Can Parallel: YES | Wave 8 | Blocks: 20 | Blocked By: 14,15,16,17

  **References**:
  - Pattern: `docs/HowToTest.md` — 现有文档风格与可执行导向。
  - Pattern: `AGENTS.md`（根目录）— 仓库约束与命令规范。

  **Acceptance Criteria**:
  - [x] 发布文档含明确不兼容声明、无迁移工具说明、风险提示。
  - [x] 文档中的验证命令可直接执行。

  **QA Scenarios**:
  ```
  Scenario: 文档完整性检查
    Tool: Bash
    Steps: ./gradlew build
    Expected: 文档更新不影响构建流程，命令描述与当前任务一致
    Evidence: .sisyphus/evidence/task-19-release-doc-gate.txt

  Scenario: 声明一致性核对
    Tool: Bash
    Steps: ./gradlew checkstyleMain
    Expected: 代码规范通过，文档条目与计划口径一致
    Evidence: .sisyphus/evidence/task-19-release-doc-gate-error.txt
  ```

  **Commit**: NO | Message: `docs(release): declare intentional save incompatibility` | Files: [docs/release notes]

- [x] 20. 终版集成门禁：全量回归与一次性开放准入审计

  **What to do**: 执行全量准入审计：闭环覆盖率、内容规模达标、门禁命令三连通过、发布材料齐全；输出最终准入结论。  
  **Must NOT do**: 不以局部通过代替全量准入。

  **Recommended Agent Profile**:
  - Category: `unspecified-high`
  - Skills: `[]`
  - Omitted: `quick`

  **Parallelization**: Can Parallel: NO | Wave 8 | Blocks: Final Verification Wave | Blocked By: 14,15,16,17,18,19

  **References**:
  - Command: `./gradlew checkstyleMain`
  - Command: `./gradlew runGameTestServer`
  - Command: `./gradlew build`

  **Acceptance Criteria**:
  - [x] 三条准入命令全部通过。
  - [ ] 内容规模达到重档目标。
- [x] 五条核心循环（L0~L4）全部闭环通过。

  **QA Scenarios**:
  ```
  Scenario: 一次性开放准入三连
    Tool: Bash
    Steps: ./gradlew checkstyleMain && ./gradlew runGameTestServer && ./gradlew build
    Expected: 三条命令全部退出码 0
    Evidence: .sisyphus/evidence/task-20-release-readiness.txt

  Scenario: 准入失败拦截
    Tool: Bash
    Steps: ./gradlew runGameTestServer
    Expected: 任一关键闭环失败则准入判定失败，并输出失败清单
    Evidence: .sisyphus/evidence/task-20-release-readiness-error.txt
  ```

  **Commit**: NO | Message: `chore(release): run final expansion readiness audit` | Files: [none]

## Final Verification Wave (4 parallel agents, ALL must APPROVE)
- [x] F1. Plan Compliance Audit — oracle
- [x] F1b. Vanilla Asset Guardrail Audit — oracle (Reject if any custom .png/.ogg found)
- [x] F2. Code Quality Review — unspecified-high
- [x] F3. Automated Scenario QA — unspecified-high (+ playwright if UI)
- [x] F4. Scope Fidelity Check — deep

## Commit Strategy
- 计划阶段不提交。
- 执行阶段按闭环分批提交：
  - 批次 A（框架层）：T1~T9
  - 批次 B（内容层）：T10A/B, T11A/B, T12, T13A/B
  - 批次 C（闭环与门禁）：T14~T20
- 提交信息建议：
  - `feat(xianqiao): establish expansion framework foundations`
  - `feat(xianqiao): add heavy content packs for expansion release`
  - `chore(release): finalize closure tests and readiness gates`

## Success Criteria
- 全系统深度联动可运行：主世界入口、仙窍生产、空窍反馈三端闭环全部通过。
- 重档体量达标：植物15+、生物8+、结构12+、配方120+、核心循环5+。
- 一次性开放准入三连通过（checkstyleMain / runGameTestServer / build）。
- 发布文档明确声明不兼容且与实际发布策略一致。
