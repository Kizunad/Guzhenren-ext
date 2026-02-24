# 仙窍扩展包一：生态与光阴长河 (Ecology & River of Time) 执行计划

## TL;DR
> **Summary**: 本计划在仙窍 6→10 转基础框架上，引入“资源点生态”与“光阴流速”两大核心经营玩法。通过多层级资源点（凡级、仙级阵法、天地秘境）和高风险的时间加速机制，将仙窍转化为复杂的资源生产与管理引擎。
> **Deliverables**: 
> 1) `ApertureWorldData` 扩展（时间流速、资源点注册表）。
> 2) 资源点基建与扫描逻辑（Tier 1 单方块 & Tier 2 多方块校验）。
> 3) 三大天地秘境（Tier 3）实现：荡魂山、三秋黄粱、市井，并深度桥接主模组属性（魂魄、寿元）。
> 4) 光阴长河逻辑倍率引擎（影响产出与灾劫倒计时）。
> **Effort**: Large
> **Parallel**: YES - 3 waves
> **Critical Path**: 数据与基建 (Wave 1) → 资源点与阵法实现 (Wave 2) → 天地秘境与属性桥接 (Wave 3)

## Context
### Original Request & Design Decisions
- **生态定位**：仙窍不仅是战场，更是资源工厂。道痕决定增幅，资源点决定产出。
- **时间流速（光阴长河）**：采用“逻辑倍率（Logic Multiplier）”而非真实 Tick 加速，以保障服务器 TPS 稳定。高流速带来高产出，但加速灾劫降临。
- **资源点分级**：
  - **Tier 1 (凡级)**：环境扫描型单方块（如：检测周围岩浆数量决定产出）。
  - **Tier 2 (仙级)**：多方块阵法结构（完整性校验 + 范围 Buff）。
  - **Tier 3 (天地秘境)**：全服唯一，不可破坏，依靠事件与光环（Aura）生效，不修改底层 Biome ID。
- **主模组桥接**：已确认主模组 `GuzhenrenModVariables.PlayerVariables` 中存在 `hunpo` (魂魄)、`zuida_hunpo` (魂魄上限)、`shouyuan` (寿元) 字段。Tier 3 秘境将直接操作这些硬核属性。

### 关键设定表

| 秘境 (Tier 3) | 核心定位 | 挂钩属性 (主模组) | 核心机制 |
|---|---|---|---|
| **荡魂山** | 魂道圣地 | `hunpo`, `zuida_hunpo` | 范围内持续扣血+扣魂；若魂魄达标则缓慢提升 `zuida_hunpo`；特产胆识蛊。 |
| **三秋黄粱** | 宙道禁地 | `shouyuan` | 解锁 `timeFlowRate > 3.0`；范围内寿命急剧流逝；产出光阴沙/年兽。 |
| **市井** | 人道社会 | (物品交换) | 投入食物维持人口；投入凡材自动加工为高级成品；繁荣度低会暴动。 |

## Work Objectives
### Core Objective
实现仙窍内的自动化资源生产与时间管理系统，并引入极具原著特色的顶级奇观（天地秘境）。

### Definition of Done
1. 玩家可在仙窍内部署 Tier 1 资源点，其产出受周围特定环境方块数量及当前仙窍 `timeFlowRate` 影响。
2. 玩家可通过特定仙蛊或指令调整仙窍 `timeFlowRate`，此倍率正确加速资源产出并成比例扣减灾劫倒计时。
3. 玩家靠近“荡魂山”中心坐标时，受特定频率的魂魄/生命打击，并能拾取生成的“胆识蛊”。
4. 玩家进入“三秋黄粱”范围时，主模组的 `shouyuan` 字段按比例快速下降。

### Must NOT Have (Guardrails)
- **绝不** 修改底层 Biome 数据来伪造天地秘境环境。
- **绝不** 使用 `TickRateManager` 或强制增加 TileEntity tick 数来实现时间加速（必须使用逻辑乘区）。
- **绝不** 在主模组 `guzhenren` 中写死这些逻辑，所有扩展必须在 `guzhenrenext` 或兼容层中完成。
- **绝不** 在每 tick 全量扫描大体积 NBT BoundingBox（必须分帧执行 + 预算控制）。
- **绝不** 允许 `PlayerAttributeBridge` 的异常（如版本不匹配导致的 NPE/LinkageError）上抛并导致崩溃；必须熔断并静默降级（No-op）。
- **绝不** 出现材质丢失（紫黑方块）。所有新增方块/物品在没有专属美术资源前，必须强制挂载合适的原版模型或现有材质作为占位符。

## Verification Strategy
- **QA Policy**: 必须编写指令或测试用例验证数据流转（特别是主模组变量的跨模组读写是否引发类加载问题）。
- **Evidence**: `.sisyphus/evidence/expansion1-task-{N}-{slug}.txt`

## Execution Strategy

### Parallel Execution Waves
Wave 1: 数据基建、光阴倍率引擎与跨模组接口。
Wave 2: Tier 1/2 资源点框架与具体方块实现。
Wave 3: 三大天地秘境实体化与核心事件监听。

---

## TODOs

### Wave 1: Infrastructure & Time Engine

- [ ] 1. 数据模型扩展与光阴引擎
  **What to do**:
  - 在 `ApertureWorldData` (或新增扩展数据类) 中增加字段：`double timeFlowRate` (默认 1.0，上限初定 3.0)，以及序列化逻辑。
  - 在 `TribulationManager` 中统一倒计时语义为 **逻辑剩余值 (`remainingTribulationTicks`)**。每次 tick 更新时，读取当前帧的 `timeFlowRate` 快照进行扣减（避免 mid-tick 改速）。
  - 提供指令 `/guzhenren_debug aperture time_rate set <value>` 用于测试。
  **Recommended Agent Profile**:
  - Category: `unspecified-high`
  **Parallelization**: Wave 1
  **Acceptance Criteria**:
  - [ ] 改变 `timeFlowRate` 后，灾劫倒计时的流逝速度成比例变化。

- [ ] 2. 主模组玩家属性读写接口封装
  **What to do**:
  - 在 `guzhenrenext` 中创建 `PlayerAttributeBridge` 工具类。
  - **Facade 设计**：采用反射或安全加载机制访问 `GuzhenrenModVariables`。若类找不到或字段变更，自动降级为 `NoopBridge`。
  - **熔断机制**：读写时捕获所有异常，写操作需做数值范围 Clamp（防负数），一旦异常立刻熔断，本周期内不再重试，防止刷屏。
  **Recommended Agent Profile**:
  - Category: `unspecified-high`
  **Parallelization**: Wave 1

### Wave 2: Resource Point Framework

- [ ] 3. 资源点基石 (Tier 1 & Tier 2 抽象)
  **What to do**:
  - 创建 `AbstractResourcePointBlockEntity`。
  - 包含核心逻辑：`calculateOutput()` = `(Base * EnvScore * DaoMarkBonus) * timeFlowRate`。
  - **环境扫描 (Tier 1)**：实现一个通用的范围扫描器（如 3x3x3 内数特定 tag 方块的数量），决定 `EnvScore`。
  - **阵法校验 (Tier 2)**：引入原版 NBT Structure (Template) 机制。
    - 设计 `StructureResourcePointBlock` (核心方块)。
    - **部署模式**：放置后计算完整 Bounding Box。逐 Chunk 检查 `hasChunkAt`。只有在**所有涵盖区块均已加载**时才生成结构，否则延迟重试。
    - **校验模式**：周期性扫描。如果跨越的 Chunk 卸载，标记为挂起(`PENDING_CHUNK`)并跳过。每 tick 设扫描数量预算（如每次对比 100 个方块），超额分帧。
    - 优势：可以直接在外部使用结构方块建造宏伟的资源点并导出 NBT，游戏内直接加载，极大地降低开发成本并提高表现力。
  - **数据驱动 (Data-Driven)**：实现 `ResourcePointType` 的 JSON 反序列化器，允许通过 Datapack (JSON) 快速定义纯“资源供给型”阵法（配置字段包括：绑定的 `.nbt` 路径、中心偏移、产出物品池、基础速率、道痕加成系数）。
  **Recommended Agent Profile**:
  - Category: `unspecified-high`
  **Parallelization**: Wave 2 | Blocked by: 1

- [ ] 4. 落地基础资源点与NBT结构生成
  **What to do**:
  - 注册并实现 `MicroSpringBlock` (微型元泉，Tier 1)：扫描周围的“水”或“冰”，按时间产出元石。
  - **结构脚本化**：在 `scripts/nbt_gen/` 目录下创建一个 Python 脚本（使用 `nbtlib` 库），通过代码批量/参数化生成阵法 NBT 结构文件（例如：根据输入参数自动生成 5x5、7x7、不同底座材质的结构）。
  - 执行该脚本生成一个测试用 NBT 结构文件（如 `tier2_formation.nbt`，导出到 `src/main/resources/data/guzhenrenext/structures/`）。
  - 创建并加载一个对应的 JSON 配置文件（如 `data/guzhenrenext/resource_points/tier2_formation.json`），绑定上述 NBT 结构并定义产出物。
  - 实现 `Tier2FormationBlock`，放置后通过 JSON 配置读取 NBT 并生成结构，随后进入周期性结构校验与产出模式。
  **Recommended Agent Profile**:
  - Category: `quick`
  **Parallelization**: Wave 2 | Blocked by: 3

### Wave 3: Secluded Domains (Tier 3)

> **注意**：Tier 3 秘境不需要真实的结构生成器，而是由管理员/特定机制在世界中划定一个 `BoundingBox`，并在该区域内注册事件监听。

- [ ] 5. 荡魂山核心逻辑 (Danghun Mountain)
  **What to do**:
  - 在 `ApertureWorldData` 中增加 `danghunMountainCenter` (BlockPos)。
  - 注册 `TickEvent.PlayerTickEvent` 或在仙窍主循环中扫描该中心 50 格内的玩家。
  - 触发频率：每 20 tick (1秒)。
  - 逻辑：读取玩家 `hunpo`。若 `< zuida_hunpo * 0.1`，造成 4.0 绝对伤害；否则 `hunpo -= 1.0` 且 `zuida_hunpo += 0.05`。
  - 战利品：在山体范围内随机坐标按极低概率生成 `胆识蛊` 掉落物。
  **Recommended Agent Profile**:
  - Category: `unspecified-high`
  **Parallelization**: Wave 3 | Blocked by: 2

- [ ] 6. 三秋黄粱核心逻辑 (Three Autumns Dream)
  **What to do**:
  - 在 `ApertureWorldData` 中增加 `timeTributaryCenter` (BlockPos)。
  - 若仙窍存在此秘境，允许 `timeFlowRate` 被设置为最高 `10.0`。
  - 在范围内：玩家 `shouyuan` 每秒减少 `0.01` (需根据实际单位换算，表现为极速衰老)。
  - 在范围内使用杀招/特定行为，提供进度翻倍 buff。
  **Recommended Agent Profile**:
  - Category: `unspecified-high`
  **Parallelization**: Wave 3 | Blocked by: 1, 2

- [ ] 7. 市井系统雏形 (City Well GUI)
  **What to do**:
  - 注册 `CityWellBlock` 及其 Menu。
  - **关键约束**：客户端 Screen 必须继承 `com.Kizunad.tinyUI.neoforge.TinyUIContainerScreen` 并使用 TinyUI 组件进行排版，禁止使用原版死板的坐标像素渲染。
  - 界面包含：人口值(模拟)、繁荣度、投入槽(放食物/原料)、产出槽。
  - 每隔一定 tick，消耗食物维持人口；消耗原料转化为产物（模拟异人加工）。
  **Recommended Agent Profile**:
  - Category: `visual-engineering`
  **Parallelization**: Wave 3
