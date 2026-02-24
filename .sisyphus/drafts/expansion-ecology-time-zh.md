# 草案：扩展包一 —— 生态与光阴长河

> **状态**：详细设计草案
> **前置依赖**：`xianqiao-tier-and-tribulation`（已完成规划）
> **目标**：将仙窍从一个“战斗竞技场”升级为“高风险经营模拟”。

## 1. 核心理念
- **生态 (Ecology)**：玩家必须建设和管理生态系统，而不仅仅是当仓库。道痕是“土壤”，资源点是“庄稼”。
- **光阴 (Time)**：时间是一把双刃剑。加速时间可以产出资源，但也会加速死亡（灾劫）。
- **唯一性 (Uniqueness)**：真正的 Tier 3 天地秘境是全服唯一的造物，而非量产方块。

---

## 2. 资源点体系 (Resource Point Architecture)

### 数据结构
存储于 `ApertureWorldData` (NBT) 中。
```java
class ResourcePointData {
    BlockPos corePos;
    ResourceTier tier; // MORTAL(凡), IMMORTAL(仙), SECLUDED_DOMAIN(天地秘境)
    ResourceType type; // FIRE, WATER, SOUL, TIME...
    int level;
    // 仅限 Tier 2/3:
    boolean isStructureComplete;
}
```

### Tier 1: 凡级资源点 (Mortal Nodes)
> **概念**：“微型元泉”、“地火喷口”。
- **形态**：单方块 (`BlockEntity`)。
- **逻辑**：**环境扫描 (Environmental Scan)**。
  - 例如：`地火喷口` 扫描周围 3x3 范围内的 `岩浆` 或 `岩浆块`。
  - 效率 = `(岩浆数量 * 0.1) * 时间流速`。
- **产出**：凡级材料（如 `火精`、`元石`）。
- **放置**：无限制。

### Tier 2: 仙级资源点 (Immortal Formations)
> **概念**：“龙鱼海域”、“炼丹大殿”、“云土花园”。
- **形态**：**多方块结构 (Multiblock Structure)**（阵法）。
  - 核心：`ImmortalFormationCore`。
  - 框架：特定方块（如 `阵旗`、`界碑`）。
- **逻辑**：**结构完整性检查**。
  - 结构破损：立即停机。
  - 结构完整：产出 = `基础值 * 道痕加成 * 时间流速`。
- **Buff**：在所在 Chunk 提供区域性增益（如 `生命恢复`、`移动速度`）。
- **放置**：受限于仙窍大小 / 道痕底蕴。

### Tier 3: 天地秘境 (Secluded Domains of Heaven and Earth)
> **概念**：“荡魂山”、“落魄谷”。
- **形态**：**独一无二的巨型结构**。
  - **唯一性**：每个存档（或全服）**仅存在一个**。只能通过混沌灾劫掉落或 GM 活动获取。
  - **不可破坏**：基岩级硬度。只能被 `混沌灾劫` 损伤。
- **实现**：
  - **不修改 Biome ID**：规避底层风险。
  - **氛围渲染**：使用客户端事件监听 (`RenderLevelStageEvent`)，当玩家靠近结构中心时改变天空颜色/迷雾。
  - **逻辑**：
    - **全域 Buff**：被动增加整个维度的底蕴（例如：魂道道痕 +100/天）。
    - **专属特产**：产出 `胆识蛊` —— 唯一获取途径。
    - **环境危机**：靠近的生物/玩家受到特定伤害（如 `荡魂` 伤害）。

---

## 3. 光阴长河机制 (River of Time Mechanics)

采用 **逻辑倍率 (Logic Multiplier)** 方案以保证服务器稳定性。

### 时间流速 (`timeFlowRate`)
- **存储**：`ApertureWorldData` 中的 `double` 类型。默认为 `1.0`。
- **修改**：
  - 需要 **宙道仙蛊**（如 `春秋蝉` 残片）。
  - 代价：维持高倍率每 tick 需消耗 `仙元`。

### 影响
1.  **资源产出**：
    - `实际产出 = 基础产出 * timeFlowRate`。
2.  **灾劫倒计时**：
    - `下一次灾劫Tick -= (1 * timeFlowRate)`。
    - *风险*：在 `10倍速` 下，原本 10 年的灾劫周期仅需 1 年就会降临。
3.  **作物生长（可选）**：
    - 挂钩 `RandomTick`。如果 `timeFlowRate > 1`，则执行多次 `block.randomTick()`（上限 5 次以防卡顿）。

---

## 4. 道痕联动 (Dao Mark Coupling)

道痕不再仅仅是战斗数值，而是 **环境修正器**。

| 流派 (Dao Type) | 对生态的影响 | 对战斗的影响 |
|:---:|:---:|:---:|
| **炎 (Fire)** | 增加 `火系` 资源点产出。 | 火系伤害 +%。燃烧时间 +%。 |
| **水 (Water)** | 增加 `水系`/`冰系` 资源点产出。 | 灭火。水下呼吸。 |
| **宙 (Time)** | 降低维持 `时间流速` 的消耗。 | 冷却缩减。 |
| **魂 (Soul)** | 增加 `荡魂山` 产出。 | 魂道伤害 +%。 |
| **炼 (Refinement)** | 提高自动炼蛊成功率。 | - |

---

## 5. 技术路线图 (Wave 4 之后)

### Phase 1: 地基
1.  实现 `ResourcePointManager` 能力。
2.  在 `ApertureWorldData` 中添加 `timeFlowRate`。

### Phase 2: 内容
3.  实现 Tier 1 方块（扫描逻辑）。
4.  实现 Tier 2 多方块验证器。
5.  实现 Tier 3 “荡魂山”（结构模板 + 事件逻辑）。

### Phase 3: 集成
6.  挂钩 `TribulationManager` 以响应 `timeFlowRate`。
7.  挂钩 `DaoMarkManager` 以影响资源产出。

---

## 6. 待决问题 (Open Questions)
- **Q1**: 如何展示 Tier 3 秘境的“氛围感”？
  - *计划*: 使用 `FogEvents` 和 `RenderSystem`，在结构包围盒内将屏幕染成粉色/灰色。
- **Q2**: 如何防止玩家滥铺 Tier 1 方块？
  - *计划*: 如果同一区块内存在过多同类资源点，产出效率递减（干扰机制）。
