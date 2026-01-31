# Bastion 节点系统重构计划（菌毯网络 + 子核心支点）

> 目标：将当前"节点 1 格密铺扩张"升级为"依附地面的菌毯式网络"，并引入"子核心/支撑节点（Anchor）"作为核心之外的扩张支点与后续功能承载。

## 0. 背景与问题

### 现状
- 扩张逻辑以 **BastionNode** 为主要落点，节点生成间距过小（观感接近"每格撒点"）。

### 主要问题
1. **节点密度过高**：视觉杂乱、性能与管理成本偏高。
2. **缺少支点机制**：核心之外没有"关键节点"作为扩张支撑与对抗焦点。
3. **后续功能难挂载**：能源/守卫/光环/外壳等系统如果直接塞进普通节点，会导致逻辑膨胀与体验混乱。

## 1. 目标与非目标

### 目标（MVP）
1. 扩张改为"**菌毯主网**"：贴地、缓慢蔓延、可大量生成但价值低。
2. 引入"**子核心/支撑节点（Anchor）**"：数量稀少，作为扩张支点（允许更大扩张步长）。
3. 明确 **连通性**：功能节点必须依附网络并与核心连通（MVP 只做最小连通记录）。
4. 保持现有 **resourcePool** 系统不被破坏（数值可后续再调）。

### 非目标（本阶段不做）
- 不在 MVP 阶段完成全部功能节点（能源/守卫/光环）与外壳细节。
- 不做复杂的"全网 BFS 动态断连计算"；先用"生成时写 owner"与缓存作为简化。

## 2. 设计原则（必须遵守）

1. **分层结构**：主网（低价值、易破坏） + 支点（高价值、功能承载）。
2. **依附地面**：主网必须贴地生成，避免漂浮。
3. **对抗可读**：进攻方可以通过"切断菌丝/拆支点"产生可见效果。
4. **渐进落地**：先让形态与扩张逻辑稳定，再逐步加功能节点。

## 2.5 风险点与实现前决策（必须补齐）

> 本节用于避免“写起来很顺、做起来被细节拖死”。如果这些决策不提前定下来，后续实现会在存档迁移、计数体系、扩张与资源池耦合上反复返工。

### 2.5.1 资源池的计数基准（高风险）

当前代码中 `resourcePool` 的**增长量与上限**都强依赖 `totalNodes`（例如上限=节点数*常量）。
若菌毯“不计入 totalNodes”，而 Anchor 又稀少，会导致：
- **资源池增速偏慢、上限偏低**，更容易出现“资源池不足”（尤其是逆转阵法需要 500+/秒的扣减）。

User reply: 菌毯扩张速度最快，但是对资源池影响是最小的。
**实现前必须选择一个计数方案（建议写死在实现备注里）：**

- 方案 A（推荐）：
  - `totalNodes` 语义改为“核心+Anchor”的战略节点数
  - 增加 `totalMycelium`，并引入权重：`effectiveNodes = anchors*X + mycelium*Y`（Y 小）
  - 资源池公式使用 `effectiveNodes`（或分别作用于 cap/gain）

- 方案 B：
  - 菌毯也计入 `totalNodes`，但在 poolGain/poolCap 里对菌毯使用更低权重，防止“无限铺地滚雪球”。

> 结论：MVP 阶段不必一次做最完美，但必须明确“菌毯是否影响资源池”以及影响的权重，否则会反复遇到数值体验问题。

### 2.5.2 Anchor 自动生成的冷却与失败回退（高风险）

Anchor 的触发规则（距离/数量/玩家放置）如果缺少“冷却”和“失败回退”，容易出现：
- 资源不足时每 tick 反复尝试生成 Anchor（刷提示/刷日志/浪费预算）
- 达到 `max_count` 后前沿仍触发距离阈值，导致扩张逻辑“卡死”或表现异常

**建议新增两条规则（MVP 也要加）：**

1) 冷却：`tryPlaceAnchor` 每 N 秒最多尝试一次（例如 5 秒）

2) 失败回退：
- 若资源不足 / 超出 max_count / 找不到合法位置：
  - 不阻塞菌毯扩张，回退到“小步长从菌毯 frontier 扩张”
  - 并记录“短暂冷却”，避免下一秒又重复失败
User reply: 批准
### 2.5.3 存档迁移时机（高风险）

你在文档中规划了 `BastionData` 新增字段（`schemaVersion/totalMycelium/totalAnchors`）。这类改动会涉及：
- CODEC 兼容
- 旧存档迁移
- SavedData 缓存一致性

User Reply: 当前是测试阶段，无需考虑旧存档兼容性
**建议里程碑约束：**
- Milestone 1（MVP）尽量**不改 BastionData 的持久化结构**，优先把菌毯/Anchor 的位置与计数放在 `BastionSavedData` 的缓存层跑通体验。
- 等形态/扩张体验稳定后，再进入 Milestone 2/3 做 schemaVersion 与迁移。

### 2.5.4 菌毯贴地判定的兼容性（中风险）

贴地判定建议以 `isFaceSturdy(..., UP)` 为主；`isSolidRender` 可能过严（台阶/特殊方块表面可站立但不 solidRender）。
文档层面建议注明：`isSolidRender` 仅作为可选增强条件。

### 2.5.5 缓存拆分的维护成本（中风险）

新增 `myceliumCache/myceliumFrontier/anchorCache` 会显著提升“缓存一致性”维护成本。
建议：
- MVP 先用更少的 cache（例如统一的 networkCache + frontierCache），内部按类型分类即可。
- 或明确约束：任何放置/破坏/爆炸/清理逻辑都必须同步更新对应 cache，否则 debug 成本会非常高。

## 3. 核心概念定义（术语表）

| 术语 | 说明 |
|------|------|
| **核心（Core）** | 基地主核心方块，唯一且不可移动 |
| **菌毯节点（Mycelium）** | 贴地蔓延的网络单元，价值低，数量多，无 BlockEntity |
| **支撑节点（Anchor）** | 稀有的"子核心"，扩张支点与功能节点承载位 |
| **连通（Connected）** | 某个网络单元能追溯到该基地（简化版：生成时写 ownerId） |
| **覆盖层（Chitin Shell）** | 覆盖于菌毯/节点间的轻防护（偏遮蔽与缓冲，后续阶段） |

## 4. 菌毯节点实现细节

### 4.1 实现形式

采用**无 BlockEntity 的简单方块**方案：

```java
public class BastionMyceliumBlock extends Block {
    // 无 BlockEntity，轻量化
    // 仅有基础方块属性：硬度低、可快速破坏
    // 不触发 ThreatEvent，不计入 totalNodes
}
```

**设计理由**：
- 保持与现有 `BastionNodeBlock` 的代码结构一致
- 菌毯方块可以有不同的硬度、视觉效果
- 不需要大改 `BastionSavedData` 的缓存逻辑
- 性能友好：大量菌毯不会导致 BlockEntity 膨胀

### 4.2 菌毯属性

| 属性 | 值 | 说明 |
|------|---|------|
| 硬度 | 0.3 | 比泥土更软，易清理 |
| 可行走 | 是 | 不阻挡移动，但可能有减速效果 |
| 触发事件 | 否 | 破坏菌毯不触发 ThreatEvent |
| 计入节点数 | 否 | 不计入 `totalNodes`，有独立计数 |
| 视觉效果 | 贴地纹理 | 类似菌丝/苔藓的扩散感 |

### 4.3 菌毯的贴地约束

菌毯仅允许生成在：
- 目标位置为空气（或可替换的植物层），且
- 目标位置下方是可站立的固体方块（防止漂浮）

```java
private static boolean isValidMyceliumTarget(ServerLevel level, BlockPos pos) {
    BlockState targetState = level.getBlockState(pos);
    if (!targetState.canBeReplaced()) {
        return false;
    }

    BlockPos below = pos.below();
    BlockState belowState = level.getBlockState(below);
    // 下方必须是固体且可站立
    // 注意：仅用 isFaceSturdy 更符合“可附着表面”的直觉；
    // isSolidRender 可能对台阶/部分特殊方块过严，建议仅作为可选增强。
    return belowState.isFaceSturdy(level, below, Direction.UP);
}
```

> 后续可按道途扩展：例如水道允许在浅水蔓延，金道允许贴墙攀附等。

## 5. Anchor 支撑节点实现细节

### 5.1 Anchor 的定位

| 维度 | 说明 |
|------|------|
| 战略价值 | 高，是基地扩张的关键支点 |
| 生成成本 | 高于菌毯，需要显著资源投入 |
| 功能承载 | 后续能源/守卫/光环节点挂载于此 |
| 破坏后果 | 阻止以该 Anchor 为起点的扩张 |

### 5.2 Anchor 生成规则

采用"**距离触发 + 资源消耗**"的方式：

```
规则 1：扩张距离阈值触发
  - 当扩张前沿距离最近 Anchor 超过 anchorSpacing 时
  - 自动尝试在边界位置生成新 Anchor
  - 消耗 anchorBuildCost 资源

规则 2：菌毯数量触发
  - 每扩张 N 个菌毯节点后（如 20 个）
  - 检查是否需要新 Anchor 作为中继

规则 3：玩家手动放置（可选）
  - 玩家可以消耗特殊材料手动放置 Anchor
  - 加速特定方向的扩张
```

### 5.3 Anchor 作为扩张支点

```java
// 扩张候选点优先以 Anchor 为起点采样
private static BlockPos findExpansionCandidate(...) {
    // 1. 优先从 Anchor 缓存获取起点
    Set<BlockPos> anchors = savedData.getAnchors(bastion.id());
    if (!anchors.isEmpty()) {
        // 从 Anchor 采样，使用较大步长 (anchorSpacing)
        return sampleFromAnchors(anchors, anchorSpacing);
    }

    // 2. 无 Anchor 时，从菌毯边界采样
    Set<BlockPos> frontier = savedData.getMyceliumFrontier(bastion.id());
    if (!frontier.isEmpty()) {
        // 从菌毯采样，使用较小步长 (myceliumSpacing)
        return sampleFromMycelium(frontier, myceliumSpacing);
    }

    // 3. 都没有时，从核心开始
    return sampleFromCore(bastion.corePos(), myceliumSpacing);
}
```

### 5.4 Anchor 与现有 BastionNode 的关系

**迁移策略**：现有的 `BastionNode` 在重构后作为 `BastionAnchorBlock` 的前身。

| 现有 | 重构后 |
|------|--------|
| `BastionNode` | → `BastionAnchorBlock`（继承大部分逻辑） |
| 新增 | `BastionMyceliumBlock`（轻量新增） |

## 6. 连通性处理

### 6.1 MVP 简化方案

```
生成时写入 ownerId：
- 菌毯/Anchor 生成时，记录到 BastionSavedData 的缓存
- 缓存结构：Map<UUID, NodeCacheEntry>
  - NodeCacheEntry 包含 myceliumPositions, anchorPositions

归属判定：
- 功能节点只需检查"是否在缓存中有记录"
- 不做实时 BFS 连通性检查
```

### 6.2 断连效果

| 阶段 | 效果 |
|------|------|
| MVP | 破坏 Anchor → frontier 缓存清理，以该 Anchor 为起点的扩张停止 |
| MVP | 切断菌毯 → 不会立即导致远端失效，但会阻止进一步扩张通过该路径 |
| 后续 | 实现真正的连通性检查，断连后远端节点进入"衰败"状态 |

### 6.3 衰败机制（后续阶段）

```
断连检测（每 N tick）：
- 从核心出发做 BFS/DFS
- 标记所有可达节点
- 不可达节点进入"衰败"状态

衰败状态：
- 停止提供功能（光环/守卫孵化）
- 开始缓慢自毁倒计时
- 重新连通可取消衰败
```

## 7. 配置结构拆分

### 7.1 新配置结构

```java
public record ExpansionConfig(
    MyceliumConfig mycelium,
    AnchorConfig anchor
) {
    public static final Codec<ExpansionConfig> CODEC = ...;
}

public record MyceliumConfig(
    double cost,           // 菌毯扩张成本
    int spacing,           // 菌毯最小间距
    int maxPerTick,        // 每刻最大菌毯扩张数
    int maxRadius          // 菌毯最大扩张半径
) {}

public record AnchorConfig(
    double buildCost,      // Anchor 生成成本
    double upgradeCost,    // Anchor 升级成本（未来）
    int spacing,           // Anchor 最小间距 (6-12)
    int maxCount,          // 最大 Anchor 数量
    int triggerDistance    // 触发新 Anchor 的距离阈值
) {}
```

### 7.2 JSON 配置示例

```json
{
  "id": "default",
  "expansion": {
    "mycelium": {
      "cost": 1.0,
      "spacing": 2,
      "max_per_tick": 5,
      "max_radius": 64
    },
    "anchor": {
      "build_cost": 50.0,
      "upgrade_cost": 100.0,
      "spacing": 8,
      "max_count": 16,
      "trigger_distance": 10
    }
  }
}
```

## 8. 资源池（resourcePool）与扩张的关系

### 8.1 现状保持
- `resourcePool` 的自动增长、玩家祭献、威胁事件注入等机制保持不动。

### 8.2 成本分层

| 操作 | 成本 | 说明 |
|------|------|------|
| 菌毯扩张 | 低（1.0） | 基础蔓延，量大成本低 |
| Anchor 生成 | 高（50.0） | 关键投入点，需要积累 |
| Anchor 升级 | 更高（100.0） | 后续功能解锁 |

### 8.3 资源不足时的行为

```
资源 < 菌毯成本：
  - 扩张完全停止

资源 >= 菌毯成本 && 资源 < Anchor成本：
  - 菌毯可以继续扩张
  - 但无法生成新 Anchor，扩张范围受限

资源 >= Anchor成本：
  - 满足条件时自动生成 Anchor
  - 扩张能力完整
```

## 9. 数据结构变更

### 9.1 BastionData 新增字段

```java
public record BastionData(
    // ... 现有字段 ...
    int totalMycelium,        // 菌毯总数
    int totalAnchors,         // Anchor 总数
    int schemaVersion         // 数据版本，用于迁移
) {}
```

### 9.2 BastionSavedData 缓存变更

```java
public class BastionSavedData {
    // 现有
    private final Map<UUID, Set<BlockPos>> nodeCache;
    private final Map<UUID, Set<BlockPos>> frontierCache;

    // 新增
    private final Map<UUID, Set<BlockPos>> myceliumCache;
    private final Map<UUID, Set<BlockPos>> myceliumFrontier;
    private final Map<UUID, Set<BlockPos>> anchorCache;

    // 新增 API
    public Set<BlockPos> getAnchors(UUID bastionId);
    public Set<BlockPos> getMyceliumFrontier(UUID bastionId);
    public void addMycelium(UUID bastionId, BlockPos pos);
    public void addAnchor(UUID bastionId, BlockPos pos);
}
```

### 9.3 存档迁移策略

```java
// 加载时检查版本并迁移
public static BastionData migrate(BastionData old) {
    if (old.schemaVersion() < 2) {
        // v1 -> v2: 现有节点迁移为 Anchor
        return old.withSchemaVersion(2)
                  .withTotalAnchors(old.totalNodes())
                  .withTotalMycelium(0);
    }
    return old;
}
```

## 10. 代码迁移路径

### 10.1 新增文件

```
src/main/java/com/Kizunad/guzhenrenext/bastion/
├── block/
│   ├── BastionMyceliumBlock.java      # 新增：菌毯方块
│   └── BastionAnchorBlock.java        # 重构自 BastionNodeBlock
├── config/
│   ├── MyceliumConfig.java            # 新增：菌毯配置
│   └── AnchorConfig.java              # 新增：Anchor 配置
└── service/
    └── BastionExpansionService.java   # 重构：拆分扩张逻辑
```

### 10.2 修改文件

| 文件 | 修改内容 |
|------|---------|
 | `BastionBlocks.java` | 注册 `BASTION_NODE(菌毯)` 与 `BASTION_ANCHOR`（节点 id 迁移为菌毯） |
| `BastionExpansionService.java` | 拆分为 `expandMycelium` + `tryPlaceAnchor` |
| `BastionSavedData.java` | 新增菌毯/Anchor 缓存和 API |
| `BastionTypeConfig.java` | `ExpansionConfig` 拆分为 `MyceliumConfig` + `AnchorConfig` |
 | `BastionData.java` | 新增 `counts(totalMycelium,totalAnchors)`（用于 effectiveNodes），兼容性不做要求 |

### 10.3 废弃/重命名

| 原文件 | 处理 |
|--------|------|
 | `BastionNodeBlock.java` | 已迁移为 `BastionAnchorBlock.java`；原 `bastion_node` 方块 id 复用为菌毯 |

## 11. 后续拓展路线（第二阶段+）

### 11.1 能源节点（挂载在 Anchor 上）
- 光合能源：依赖天空光照，雨夜效率下降。
- 汲水能源：依赖水源环境。
- 地热能源：靠近岩浆/深层更强。

作用建议：
- 提升 `resourcePool` 增速或上限；或降低扩张消耗。

### 11.2 守卫节点（挂载在 Anchor 上）
- 孵化巢：持续消耗资源池产兵；资源不足停孵。
- 哨站：提供预警/召集/增幅守卫。

### 11.3 光环节点（正/负极）
- 正极：增幅守卫（抗性/回血/攻速）。
- 负极：压制玩家（减速/挖掘疲劳/真元压制）。

### 11.4 几丁质外壳（覆盖层）
- 覆盖在菌毯/节点之间形成遮蔽层。
- 不需要很难挖：定位为"遮盖 + 轻缓冲"。
- 可再生：连通且资源池充足时缓慢恢复。

## 12. 验收标准（MVP）

1. **形态验收**：扩张不再是 1 格密铺，能看到菌毯"贴地蔓延"的形态。
2. **Anchor 验收**：Anchor 存在且能作为扩张支点，没有 Anchor 时扩张明显受限。
3. **对抗验收**：破坏 Anchor 或切断菌毯能显著削弱扩张（至少能观察到扩张停滞/范围收缩）。
4. **兼容验收**：不破坏现有存档与 `resourcePool` 框架（可以调整数值，但不能让存档炸）。
5. **性能验收**：大量菌毯不会导致明显卡顿（无 BlockEntity 膨胀）。

## 13. 分阶段里程碑

### Milestone 1：形态与扩张（MVP）
- [ ] 新增 `BastionMyceliumBlock`（无 BlockEntity 的轻量方块）
- [ ] 重构 `BastionNodeBlock` → `BastionAnchorBlock`
- [ ] 修改 `isValidExpansionTarget` 添加贴地约束
- [ ] 拆分 `ExpansionConfig` 为 `MyceliumConfig` + `AnchorConfig`
- [ ] 实现 Anchor 作为扩张支点的采样逻辑
- [ ] 实现 Anchor 自动生成规则（距离触发）
- [ ] 新增 `BastionSavedData` 的菌毯/Anchor 缓存
- [ ] 实现存档迁移逻辑（schemaVersion）

### Milestone 2：最小功能节点
- [ ] 先做 1 个能源节点（光合）用于验证"Anchor 挂载功能"的架构
- [ ] 实现 Anchor 升级机制

### Milestone 3：对抗与外壳
- [ ] 实现断连检测（周期性 BFS）
- [ ] 实现衰败机制（断连后自毁倒计时）
- [ ] 几丁质外壳覆盖与再生（轻量）

---

## 附录 A：设计决策记录

| 决策 | 选项 | 选择 | 理由 |
|------|------|------|------|
| 菌毯实现形式 | A.BitSet / B.方块+BE / C.方块无BE | C | 兼顾视觉、兼容性、性能 |
| Anchor 生成 | A.手动 / B.自动 / C.混合 | B（MVP）| 减少玩家操作负担 |
| 连通性检查 | A.实时BFS / B.生成时写入 | B（MVP）| 性能优先，后续增强 |
| 现有节点迁移 | A.保留 / B.迁移为Anchor | B | 统一架构 |

## 附录 B：风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| 菌毯密度仍过高 | 视觉杂乱 | 配置化 spacing，可后续调整 |
| Anchor 太脆弱 | 防守方体验差 | Anchor 有较高硬度和防御事件 |
| 断连效果不明显 | 进攻无成就感 | 后续阶段实现衰败机制 |
| 存档迁移失败 | 数据丢失 | schemaVersion + 备份提示 |
