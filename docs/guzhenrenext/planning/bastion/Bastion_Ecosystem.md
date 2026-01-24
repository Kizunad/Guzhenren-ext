# 可迭代敌对基地生态（Bastion）—— 落地修订版（v2）

> 目的：把“基地 core 坐标 + 方块扩张 + 方块越多越强/越能刷怪 + 随时间进化到高转”的设想，
> 修订为**符合本仓库现状**、可在 NeoForge 1.21.1 里稳定实现、且长期可迭代的数据驱动方案。

## 0. 关键结论（TL;DR）

1. **资源对接不新造系统**：真元/念头/精力/魂魄/道痕直接复用桥接类：
   - `com.Kizunad.guzhenrenext.guzhenrenBridge.ZhenYuanHelper`
   - `NianTouHelper` / `JingLiHelper` / `HunPoHelper` / `DaoHenHelper`
2. **持久化只存“最小状态”**：不要把 BFS 的 `visited/frontier` 大集合、怪物 UUID 列表、全部节点坐标写进存档。
   - SavedData 只存：核心坐标、转数、进化进度、节点计数缓存、半径/游标、离线累积时间、资源池等。
   - “前沿/visited/实体追踪”等一律视为**运行态缓存**（可重建/可丢失）。
3. **Tick 调度用 LevelTickEvent + 固定预算**：本仓库尚未形成统一 TPS API 使用方式（未见 `getAverageTps` 现成封装）。
   - MVP 采用“每 tick 预算 N 次操作 + 距离玩家分级 tick + 每 X tick 才处理一次”。
4. **扩张算法推荐‘受限随机邻接生长’**（可选 BFS）：目标是连续扩张但不需要持久化 visited。
5. **念头<0 沉默与现状不一致**：当前 `NianTouHelper.modify` 会把念头钳到 `>=0`。
   - MVP 用“自定义/原版负面效果 + 资源扣除”表达压制。
   - 若未来要严格实现“念头<0 持续沉默”，需要调整 `NianTouHelper` 语义或增加额外判定层（见 §8.4）。

---

## 1. 项目约束与现状（必须对齐）

### 1.1 仓库形态

- 主 mod：`guzhenrenext`（本设计默认落地在这里）
- `customnpcs` / `customNPCImpl` 在主编译中被排除（见 `build.gradle`），
  因此 Bastion 系统**不要依赖 CustomNPCs 的 AI/实体**。

### 1.2 资源体系对接点（已存在）

| 资源 | 读/写入口 | 备注 |
|---|---|---|
| 真元 | `ZhenYuanHelper.getAmount/modify/hasEnough` | 另有 `calculateGuCostDenominator` 可复用“按转数/阶段折算” |
| 念头 | `NianTouHelper.getAmount/modify` | 当前实现将值钳到 `>=0` |
| 精力 | `JingLiHelper.getAmount/modify` | 无 `hasEnough`，需自行比较 |
| 魂魄 | `HunPoHelper.getAmount/modify/checkAndKill` | 可用 `modify` 扣到 0，必要时触发处死 |
| 道痕 | `DaoHenHelper.getDaoHen/addDaoHen/setDaoHen` | `addDaoHen` 可用负数扣除（内部 clamp） |

### 1.3 数据驱动模式（可复用的范式）

本仓库已有成熟做法：

- `NianTouDataLoader extends SimpleJsonResourceReloadListener`
- `NianTouData` 使用 `Codec/RecordCodecBuilder` 定义 JSON schema

Bastion 配置建议完全沿用该风格（见 §10）。

---

## 2. 核心玩法定义（需求冻结）

### 2.1 基地核心与活动范围

- 世界中会自然生成/被触发生成“基地核心（Core）”，核心具有 `coord`（世界坐标）。
- 基地在核心周围活动：
  - 扩张“节点方块（Node）”
  - 刷出“基地守卫（Mob）”
  - 对靠近玩家产生资源压制/负面效果

### 2.2 方块与转数

- Node 分为 **1~6 转**（未来拓展到 9 转）。
- Node 具有**道途属性**（至少：智道/魂道/木道/力道；可扩展更多）。
- Node 作为资源来源：
  - 1 转：资源量级 ~ 0.1
  - 5 转：资源量级 ~ 100
  - 6 转：产出对应道痕 ~ 0.1~1

### 2.3 基地强度与刷怪

- **Node 越多**：基地越强、刷怪越多。
- **高转更强**：高转的 Node 和高转的 Mob 都应明显压制低转。

### 2.4 进化

- 基地随世界时间自然进化：1 → 6 转，越到后面越慢。
- 允许拓展到 9 转（曲线与门槛可数据驱动）。

---

## 3. 模块拆分（面向实现的目录与职责）

> 说明：这里给出的是“实现时应该有哪些类/模块”，不是要求一次性做完。

### 3.1 运行时服务（Server-only）

- `BastionTicker`：tick 入口（`LevelTickEvent.Post`），负责调度预算与分级 tick。
- `BastionWorldService`：核心逻辑编排（进化、扩张、刷怪、反制状态）。
- `BastionExpansionService`：扩张算法实现（§6）。
- `BastionSpawnService`：刷怪与上限控制（§8）。
- `BastionPurificationService`：玩家反制（§9）。

### 3.2 数据与配置

- `BastionConfig`：JSON 配置结构（Codec）。
- `BastionConfigLoader`：`SimpleJsonResourceReloadListener` 加载配置。
- `BastionSavedData`：世界持久化（按维度存储）。

### 3.3 方块/物品

- `BastionCoreBlock`：核心方块（MVP 建议核心方块可被破坏，作为反制点）。

**核心被破坏后的处理流程**：

```text
ON_CORE_DESTROYED(bastion):
  1. bastion.state = DESTROYED
  2. bastion.destroyedAtGameTime = currentGameTime
  3. 停止扩张和刷怪（等价：expansionEnabled=false, spawnEnabled=false）
  4. 现有守卫尽量允许自然消失（不再强制 persistenceRequired）

TICK_DESTROYED_BASTION(bastion, gameTime):
  elapsed = gameTime - bastion.destroyedAtGameTime

  if elapsed < 600:  // 30 秒缓冲期：给玩家反馈/避免突兀清空
    return

  if elapsed < 1800:  // 60 秒节点衰减窗口（预期值）
    // 重要：不依赖“全量节点坐标列表”。
    // 只对已加载 chunk 做预算分帧清理：每 tick 最多尝试清理 N 个方块位置。
    cleanupBudget = config.cleanupBudgetPerTick  // 例如 32
    decayBySampling(corePos, maxRadius, cleanupBudget)
    return

  // 90 秒后：从 SavedData 中移除基地记录。
  // 注意：未加载 chunk 中的节点无法被这里直接清理。
  // 这些节点依赖“节点自检消散”在 chunk 重新加载时最终清理。
  BastionSavedData.remove(bastion.id)
```

> 说明：这里的“60 秒衰减窗口”是**预期体验**，实际清理速度受 chunk 加载与预算影响。
> 设计目标是“最终一致”（最终能清干净），而不是“90 秒内必清空所有方块”。

- `BastionNodeBlock`：节点方块（可用 blockstate 表达 tier/dao，或拆分多 block）。

**MVP 推荐方案**：单 Block + BlockState 属性（DAO 限制 4~8 种）

```java
public static final IntegerProperty TIER = IntegerProperty.create("tier", 1, 6);
public static final EnumProperty<BastionDao> DAO = EnumProperty.create("dao", BastionDao.class);
```

约束（很重要）：

- `DAO` **建议只放 4~8 种**（例如：`ZHI/HUN/MU/LI` + 预留 1~4 个扩展位）。
- 如果未来要支持大量道途：不要硬塞进 blockstate。
  - 方案 1：每个道途一个 NodeBlock（仍保留 `TIER` blockstate）
  - 方案 2：视觉不区分道途，仅通过掉落表/事件逻辑区分（DAO 存在于 bastion 配置中）

优点：
- 减少注册 block 数量，方便批量处理
- 通过 BlockState 即可读取 tier/dao，无需 BlockEntity
- loot table 可通过 blockstate 条件区分不同 tier 的掉落

> 注：渲染/UI/模型属于后续迭代，本设计不要求第一版就完成。

---

## 4. 持久化数据设计（落地修订重点）

### 4.1 为什么要“最小持久化”

Claude 初稿倾向把 `visited/frontier/spawnedEntities` 等大集合写入 `SavedData`。
在 Minecraft 里这会导致：

- 存档膨胀（visited 很容易上万/十万）
- 重启/加载卡顿（反序列化成本高）
- 高频写盘（怪物 UUID 集合频繁变化）

因此：**SavedData 只存可验证且必要的“状态”，不存运行态缓存。**

### 4.2 BastionSavedData（建议字段）

每个基地（Bastion）持久化最小字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| id | UUID | 基地唯一 id |
| state | Enum | 基地状态（ACTIVE / SEALED / DESTROYED） |
| corePos | BlockPos | 核心坐标 |
| bastionType | ResourceLocation/String | 指向配置（bastion type id） |
| primaryDao | String | 道途 id（如 `zhi_dao`） |
| tier | int | 当前转数 |
| evolutionProgress | double | 本转到下一转进度（0~1） |
| totalNodes | int | 节点总数（增量缓存） |
| nodesByTier | Map<int,int> | 各转数节点计数（增量缓存） |
| growthRadius | int | 当前扩张半径（或壳层半径） |
| growthCursor | long/int | 扩张游标（用于确定性随机序列，见下方说明） |
| resourcePool | double | 基地能量池（用于“资源池驱动扩张”） |
| sealedUntilGameTime | long | 封印截止时间（0 表示未封印） |
| destroyedAtGameTime | long | 核心被破坏的时间（0 表示未破坏，用于衰减计时） |
| lastGameTime | long | 上次处理的世界时间 |
| offlineAccumTicks | long | chunk 未加载时累计的离线 tick（仅计时，不做放方块） |

#### 4.2.1 状态机不变量与优先级（实现必须遵守）

为了避免“封印中又销毁/又进化”等矛盾，本系统约定：

- `DESTROYED` 为终态：进入后不可回到 `ACTIVE/SEALED`。
- `SEALED` 与 `sealedUntilGameTime` 必须一致：
  - 若 `gameTime < sealedUntilGameTime` 且 `state != DESTROYED`，则视为封印中。
  - 建议实现中将 `SEALED` 视作“派生状态”（由时间判断），或至少在 tick 时强制纠正。
- 字段有效性：
  - `destroyedAtGameTime` 只在 `state == DESTROYED` 时有意义，其他状态应为 `0`。

优先级（从高到低）：

1) `DESTROYED`
2) `SEALED`（仅当未 DESTROYED 且 `gameTime < sealedUntilGameTime`）
3) `ACTIVE`

**baseSeed + growthCursor 语义（强制明确）**：

```text
baseSeed（不强制持久化）：
  用于让同一基地的扩张“天然可复现”。建议从 worldSeed 与 bastionId 导出：
  baseSeed = hash64(worldSeed, bastionId)

growthCursor（持久化）：
  扩张步进游标（不是 seed）。每次“成功放置节点”后 growthCursor += 1。

随机取值：
  rand = hash64(baseSeed, growthCursor)
  用 rand 派生方向/概率/候选等。

这样重启后：同一 growthCursor 对应的随机结果一致。
```

**nodesByTier 增量更新时机**：

- 放置节点时：`nodesByTier.merge(tier, 1, Integer::sum); totalNodes++;`
- 破坏节点时：从 BlockState 读取 tier，然后 `nodesByTier.merge(tier, -1, Integer::sum); totalNodes--;`
- 节点方块的 `onRemove` 回调需要通知 `BastionSavedData` 进行计数更新

#### 4.2.2 节点归属规则（MVP 采用方案 A：基地间距/不重叠）

为了让“破坏节点时 nodesByTier--”在不存全量节点坐标/不存节点归属表的情况下仍然正确，
MVP 采用**不重叠约束**：

- 生成 BastionCore 时强制满足：
  - `distance(coreA, coreB) >= 2 * maxRadius + buffer`
  - `buffer` 建议 16~32
- 因此每个节点都只可能属于一个核心的影响范围。

实现归属方式（推荐）：

1. 用 chunk 索引快速找“附近核心列表”（例如同 chunk 与周围 1~2 圈 chunk）。
2. 取距离最近且 `dist <= maxRadius` 的核心作为归属。

> 如果未来允许基地重叠，需要升级到“方案 B：不确定时标记 needsRecount，再分帧重算计数”。

明确不持久化：

- `visited`（扩张搜索痕迹）
- 大型 `frontier` 队列（最多保留小窗口/游标即可）
- `spawnedEntities` UUID 集合（改为“按 tag 扫描计数”）
- 全量 node 坐标列表（除非未来必须做精确净化/结构编辑；MVP 不需要）

### 4.3 运行态缓存（可丢失）

建议为每个基地维护一份内存态结构（服务器重启后可重建）：

- `frontierSample: List<BlockPos>`（最多 64~128）
- `recentSpawnCountCache`（定期刷新）
- `recentPlayerDistance`（用于优先级）

---

## 5. Tick 调度（预算、分级、边界）

### 5.1 Tick 入口建议

参考 `customNPCs/handler/NpcSpawningHandler#onLevelTick(LevelTickEvent.Post)` 的写法：

- 只处理 `ServerLevel`
- 可按维度过滤（MVP 先 Overworld）
- 通过固定间隔降低频率（例如每 20 tick 处理一次）

### 5.2 固定预算（MVP）

由于当前工程未形成统一的 TPS 获取封装（`getAverageTps` 未见现成），MVP 采用固定预算：

- `BUDGET_EXPAND_PER_TICK = 32`（最多放 32 个节点尝试）
- `BUDGET_SPAWN_CHECK_PER_TICK = 8`（最多处理 8 个基地的刷怪检查）

分级 tick：

- **Full tick**：有玩家在 128 格内 → 扩张 + 刷怪 + 进化
- **Light tick**：无玩家但 chunk 已加载 → 只进化（减速）+ 资源池
- **Unloaded**：chunk 未加载 → 只累积 `offlineAccumTicks`（不放方块、不刷怪）

> 可选增强：后续若需要 TPS 自适应，可用 `MinecraftServer` 的平均 tick time
> （mspt）推导退化等级，但不作为 v1 硬要求。

---

## 6. 扩张算法（落地版）

### 6.1 MVP 推荐：受限随机邻接生长（不需要 visited 持久化）

目标：看起来像“从核心向外蔓延”，但不维护巨大 visited。

**核心思路**：

1. 维护一个小的 `frontierSample`（最近成功放置的节点位置）。
2. 每次扩张从 sample 里随机取一个点，尝试在其 6 邻域放一个新节点。
3. 失败就换点/换方向，最多尝试 `N_TRIES` 次。
4. 受限于 `maxRadius` 与 `maxNodes`。

伪代码：

```text
EXPAND_STEP(bastion, budget):
  placed = 0
  tries = 0
  while placed < budget and tries < budget * 8:
    seed = pick(frontierSample or corePos)
    dir = randomDir6()
    pos = seed + dir
    if dist(corePos, pos) > maxRadius: continue
    if not replaceable(pos): continue
    if not hasAdjacentNode(pos): continue  // 保证连续蔓延
    placeNode(pos, pickNodeTier(bastion), bastion.primaryDao)
    updateCounts(+1)
    frontierSample.add(pos); trim(frontierSample, 128)
    placed++
    tries++
  return placed
```

### 6.2 可选：BFS 前沿扩张（运行态 visited）

- BFS 可以让形态更“团块化”，但 visited 只能放内存。
- 重启后 BFS 状态丢失时，允许退化为“随机邻接生长”继续扩张。

### 6.3 可选：资源池驱动扩张

- `resourcePool` 随时间累积（与 `totalNodes/tier` 相关）
- 放置节点消耗 pool，高转成本更高
- 好处：天然反滚雪球 + 玩家清理节点会降低扩张能力

**资源池累积与上限（必须有，防滚雪球）**：

```text
基础定义：
  tierFactor = 1.5^(bastionTier-1)

资源池增长（每 tick）：
  gain = totalNodes * tierFactor * 0.01

资源池上限（线性随节点数增长）：
  cap = totalNodes * 10.0

倍率衰减（可乘）：
  - 无玩家在场：gain *= noPlayerMultiplier（默认 0.5 或更低）
  - 封印中：gain *= sealedPoolMultiplier（默认 0 或 0.1）
  - 低 TPS：gain *= lowTpsMultiplier（后续扩展，可选）

最终：
  resourcePool = min(cap, resourcePool + gain)

放置节点消耗（超线性增长，避免高转快速铺满）：
  cost = baseCost * 2.5^(nodeTier-1)
  baseCost = 10.0
  1转节点: 10, 2转: 25, 3转: 62.5, 4转: 156, 5转: 391, 6转: 977
```

---

## 7. 进化系统（1→6，未来 9）

### 7.1 曲线与门槛

默认曲线可参考（可配置）：

| 跃迁 | 基础时长（tick） | 现实时间 | 节点门槛（建议） |
|---|---:|---:|---:|
| 1→2 | 72,000 | 1h | 50 |
| 2→3 | 216,000 | 3h | 200 |
| 3→4 | 648,000 | 9h | 500 |
| 4→5 | 1,944,000 | 27h | 1,000 |
| 5→6 | 5,832,000 | 81h | 2,000 |

通用公式（可作为配置生成器逻辑）：

```text
time(n->n+1) = baseTime * 3^(n-1)
minNodes(nextTier) = floor(50 * 1.8^(nextTier-2))
```

### 7.2 无人区减速

- 若 128 格内无玩家：进化速度乘以 `0.1`（可配置）
- 目的：避免“远离玩家的基地离线自动满转”，同时防止玩家利用卸载 chunk 彻底冻结

### 7.3 高转抑制（可选）

- 256 格内存在更高转基地：低转基地进化/扩张系数下降（例如每差 1 转 -30%）
- 目的：世界威胁集中化，避免地图上到处同时堆满高转

### 7.4 封印状态与进化的交互

进化逻辑中需检查封印状态：

```text
TICK_EVOLUTION(bastion, gameTime):
  if bastion.sealedUntilGameTime > gameTime:
    // 封印中，可选：完全跳过 或 应用 sealEvolutionMultiplier（如 0.1）
    evolutionMultiplier = config.sealEvolutionMultiplier  // 默认 0，即完全暂停
  else:
    evolutionMultiplier = 1.0

  // ... 正常进化逻辑，乘以 evolutionMultiplier
```

封印对“扩张/刷怪/资源池”的推荐交互（同样数据驱动）：

```text
IF SEALED:
  expansionEnabled = false   // 或 expansionChance *= sealedExpansionMultiplier
  spawnEnabled = false
  evolutionMultiplier = sealEvolutionMultiplier  // 默认 0
  poolGainMultiplier = sealedPoolMultiplier      // 默认 0 或 0.1
```

### 7.5 chunk 未加载策略（建议 COMPENSATE-ONLY-TIMER）

**落地约束**：chunk 未加载时不能可靠放方块/刷怪/读方块状态。

推荐策略：

- UNLOADED 时只累积 `offlineAccumTicks`
- chunk 重新加载后：
  - 以 `offlineAccumTicks` 的一部分（如 25%）补偿进化/资源池
  - 扩张与刷怪仍按在线预算分帧执行（避免“加载瞬间爆发”）

---

## 8. 强度映射与刷怪规则

### 8.1 强度分（PowerScore）

用节点计数缓存计算强度（避免全量扫描）：

```text
weighted = Σ count[tier] * (tierWeightBase)^(tier-1)   // tierWeightBase 默认 3
tierFactor = 2^(bastionTier-1)
rawScore = weighted * tierFactor
normalized = clamp(log10(rawScore+1)/7, 0, 1)
```

### 8.2 刷怪上限（双重上限）

- 单基地上限：`mobCapPerBastion`（默认 20，硬上限 50）
- 全局上限：`mobCapGlobal`（默认 200）
- 距离衰减：核心 128 格外不刷（或只 light tick）

**实现建议（重要修订）**：

- 不维护 `spawnedEntities: Set<UUID>` 持久化
- 采用“给怪物打 tag/记 bastionId，然后每 N 秒扫描半径内数量”的方式统计

### 8.3 怪物数值（建议作为 JSON 表）

本项目数值方向（来自需求）：6 转极强（HP ~ 1,000,000；护甲 ~ 100；攻击 ~ 2000）。

建议用“基础表 + 强度倍率”组合：

```text
finalHP = min(baseHP[tier] * (1 + normalized*0.5), hardCapHP)
finalArmor = min(baseArmor[tier] + normalized*10, hardCapArmor)
finalDamage = min(baseDamage[tier] * (1 + normalized*0.3), hardCapDamage)
```

> 注意：数值应以你们的战斗系统实际表现为准（是否有穿透/减伤/魂魄判定等）。

### 8.4 资源压制/负面效果（对齐现状）

#### 现状差异：念头不会变负

`NianTouHelper.modify` 当前实现：`newValue = max(0, original + amount)`。

因此 v1 推荐的“沉默/封禁”表现：

- 扣念头（到 0 为止）
- 同时施加可见负面效果（例如：`MobEffects.WEAKNESS`、`DIG_SLOWDOWN`、`DARKNESS` 等）

若未来要实现“念头<0 持续沉默”的设定，需要：

1) 调整 `NianTouHelper` 允许负数（会影响全局语义，风险较高）或
2) 引入额外字段/附件存“念头赤字”，并在 tick 中按赤字施加封禁

---

## 9. 玩家反制（落地到现有 Helper）

### 9.1 反制 1：物理破坏（MVP 必做）

- 玩家挖掉 Node
- 成本：扣精力/真元/念头（任选其一，具体由配置决定）
- 收益：掉落物 + 直接给资源（通过 Helper 写入 PlayerVariables）

**实现语义对齐**：

- 精力检查：`JingLiHelper.getAmount(player) >= cost`
- 扣精力：`JingLiHelper.modify(player, -cost)`
- 扣道痕：`DaoHenHelper.addDaoHen(player, type, -cost)`

### 9.2 反制 2：净化仪式（后续）

- 使用消耗品（符箓/阵基）+ 多资源消耗
- 批量净化半径内节点（并获得更高收益倍率）

### 9.3 反制 3：封印隔离（后续）

- 使基地在一段时间内停止扩张与刷怪（`sealedUntilGameTime`）
- 封印期间不产出/不进化或减速（由配置决定）

### 9.4 反制 4：道途克制（后续）

- 玩家使用克制道途时，对基地怪物/节点造成额外伤害或降低其抗性
- 克制判定可绑定玩家道痕门槛（`DaoHenHelper.getDaoHen >= threshold`）

---

## 10. 数据驱动（JSON）第一版草案

### 10.1 目录建议

参考现有 `data/guzhenrenext/niantou/*.json` 的加载方式：

```text
data/guzhenrenext/bastion/
  bastion_types/*.json
  tier_params.json
  purification_rules.json
  mob_profiles/*.json
```

### 10.2 bastion_types（基地类型）最小字段

```json
{
  "bastion_type": "zhi_dao_watchtower",
  "primary_dao": "zhi_dao",
  "initial_tier": 1,
  "max_tier": 6,
  "max_radius": 64,
  "expansion": {
    "strategy": "neighbor_growth",
    "budget_per_tick": 32,
    "chance": 0.30
  },
  "spawning": {
    "spawn_radius": 32,
    "cooldown_ticks": 200,
    "mob_cap_base": 8,
    "mob_cap_hard": 50
  }
}
```

### 10.3 tier_params（转数参数）建议字段

```json
{
  "tiers": {
    "1": {"evolution_time_ticks": 72000, "min_nodes_next": 50},
    "2": {"evolution_time_ticks": 216000, "min_nodes_next": 200}
  },
  "limits": {
    "hard_cap_hp": 2000000,
    "hard_cap_armor": 100,
    "hard_cap_damage": 5000,
    "mob_cap_global": 200
  }
}
```

> 注意：资源产出建议拆成“节点掉落表 + 直接写入资源”两层，
> 第一版可只做“破坏节点直接写入资源（Helper.modify）”。

---

## 11. MVP（第一版可交付范围）

### 11.1 目标

在不引入 CustomNPCs 的前提下，实现“看得见、能打、会长”的闭环：

- 1 种基地类型（智道）
- 3 阶段（1~3 转）
- 1 种核心方块 + 1 种节点方块（或 3 个 tier 变体）
- 1 种守卫（基于 vanilla zombie，打 tag + 调整属性）
- 反制：物理破坏节点（消耗精力，产出念头/少量道痕）

### 11.2 MVP 验收标准

- 世界能生成核心（或通过命令生成）
- 核心周围节点会缓慢扩张（预算受限，不会卡服）
- 节点数量上升会提升刷怪数量/强度
- 玩家能清理节点并获得对应资源（通过 Helper 写入）
- 基地能随时间进化到 3 转（可配置缩短时间以便测试）

---

## 12. 风险点与规避（必须读）

1. **存档膨胀**：不要持久化 visited/全量 node 坐标/实体 UUID 集合。
2. **爆发式扩张**：chunk 重新加载时补偿必须分帧、有上限（例如最多补偿 24h）。
3. **刷怪过量**：必须同时有“单基地 cap + 全局 cap + 距离衰减”。
4. **资源刷取漏洞**：
   - 破坏节点的收益建议随“基地警戒/反击”动态变化
   - 或者对同一玩家同一基地设置收益衰减/冷却
5. **数值溢出与体验**：6 转 2000 伤害/100 万血是极端值，必须确保存在有效反制（封印/克制/团队协作）。

---

## 13. 后续扩展路线（到 6 转/9 转）

1. 4~6 转：补齐节点 tier 变体 + 更强的守卫技能（资源抽取/沉默/领域效果）
2. 多道途：增加魂道/木道/力道节点及对应掉落与 debuff
3. 结构生成：用结构/地形特征生成核心（替代纯随机方块）
4. 9 转：只需要扩展 tier_params（时间/门槛/上限）与 mob_profiles
5. UI/可视化：小地图标记、进度条、警戒度提示等（客户端工作后置）
