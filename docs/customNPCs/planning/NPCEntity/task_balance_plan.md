# NPC 任务配置与数值平衡计划 (Task Configuration & Balancing Plan)

## 1. 目标 (Objective)
配置具体的“提交物品”类任务 (SubmitItemTask)，并建立一套基于价值 (Value-Based) 的数值平衡体系。
核心原则：
1.  **需求物品**: 集中于 NPC 生存/战斗必需品 (如药水、金苹果)，属于 `InventoryWhitelist` 范围。
2.  **奖励物品**: 必须 **排除** `InventoryWhitelist` 中的物品，侧重于原材料、货币等价物或特殊资源。
3.  **价值平衡**: 遵循 `Input_Value * Count ≈ Reward_Value` 的公式，并根据任务难度给予 1.1x ~ 1.5x 的溢价奖励。

---

## 2. 物品价值基准 (Value Baseline)
基于 `src/main/resources/data/customnpcs/item_values.json` 进行微调与计算。

### 2.1 需求物品 (NPC Needs - Whitelisted)
| 物品 (Item) | 现有价值 (Base Value) | 修正建议 (Notes) |
| :--- | :--- | :--- |
| `minecraft:potion` | 20 | 普通水瓶/无效果。建议对 **治疗药水** 视为成品，估值 **50**。 |
| `minecraft:golden_apple` | 150 | 包含 8 金锭 (160) + 苹果 (5)，原价 150 略低，维持或升至 165。 |
| `minecraft:enchanted_golden_apple` | 2500 | 稀有品，高价值。 |
| `minecraft:shield` | 20 | 铁锭(10)+木板(1)，合理。 |
| `minecraft:totem_of_undying` | 500 | 唤魔者掉落，高价值保命符。 |

### 2.2 奖励物品 (Rewards - Non-Whitelisted)
这些物品不在 `InventoryWhitelist` 中，适合作为奖励发放给玩家。
| 类别 | 物品 | 单价 (Value) | 用途 |
| :--- | :--- | :--- | :--- |
| **硬通货** | `minecraft:emerald` (绿宝石) | 80 | 交易通用货币 |
| **硬通货** | `minecraft:diamond` (钻石) | 100 | 高级制造/货币 |
| **稀有矿产** | `minecraft:netherite_scrap` (残骸) | 300 | 顶级装备升级 |
| **稀有矿产** | `minecraft:netherite_ingot` | 1200 | (4残骸+4金) |
| **特殊资源** | `minecraft:shulker_shell` (潜影壳) | 50 | 扩充背包组件 |
| **特殊资源** | `minecraft:experience_bottle` | 30 (估) | 经验修补 |
| **基础资源** | `minecraft:iron_ingot` (铁锭) | 10 | 基础建设 |

---

## 3. 任务配置方案 (Task Configuration Scenarios)

### 方案 A: 战地急救 (Field Medic) - 低级
*   **背景**: NPC 急需治疗药水以备不时之需。
*   **输入**: `Potion (Healing)` (估值 50)
*   **目标总价值**: 约 500 (10 瓶)
*   **奖励**: `Iron Ingot` (铁锭) 或 `Emerald` (绿宝石)
*   **计算**:
    *   输入: 10 Potion * 50 = 500
    *   奖励: 6 Emerald * 80 = 480 (略亏，但清库存) 或 50 Iron Ingot * 10 = 500 (等价)
*   **最终配置**:
    *   **Request**: `minecraft:potion{Potion:"minecraft:healing"}` x **10**
    *   **Reward**: `minecraft:emerald` x **7** (溢价奖励: 560 Value)

### 方案 B: 黄金储备 (Golden Reserve) - 中级
*   **背景**: 为了应对强敌，NPC 需要金苹果。
*   **输入**: `Golden Apple` (价值 150)
*   **目标总价值**: 约 1500
*   **计算**:
    *   需求数量: 1500 / 150 = 10 个
*   **奖励选择**:
    *   `Diamond` (100): 需要 15 个。
    *   `Netherite Scrap` (300): 需要 5 个。
*   **最终配置**:
    *   **Request**: `minecraft:golden_apple` x **10**
    *   **Reward**: `minecraft:netherite_scrap` x **6** (溢价奖励: 1800 Value, 鼓励玩家通过合成金苹果换取下界合金)

### 方案 C: 终极保险 (The Lifeline) - 高级
*   **背景**: NPC 只有一条命，需要不死图腾。
*   **输入**: `Totem of Undying` (价值 500)
*   **目标总价值**: 约 2500 (5 个图腾)
*   **计算**:
    *   输入: 5 * 500 = 2500
*   **奖励选择**:
    *   `Netherite Ingot` (1200): 2 个 = 2400。
    *   `Enchanted Golden Apple` (2500): 1 个 (以物易物，虽然也在白名单，但如果作为更高级的战略物资交换也可以，不过本计划优先给非白名单)。
    *   **混合奖励**: `Diamond` x 20 (2000) + `Emerald` x 10 (800) = 2800。
*   **最终配置**:
    *   **Request**: `minecraft:totem_of_undying` x **5**
    *   **Reward**: `minecraft:netherite_ingot` x **2** + `minecraft:diamond` x **5** (总值: 2900, 溢价 1.16x)

---

## 4. 执行步骤 (Action Items)

1.  **更新价格表 (`data/customnpcs/item_values.json`)**:
    *   虽然 Potion 默认为 20，但在代码逻辑或配置中，需明确“有益药水”的加权。
    *   或者简单地，在设计任务所需数量时，人工按 50 计算，无需修改全局 JSON。

2.  **编写任务数据 (JSON Draft)**:
    *   在 `src/main/resources/data/customnpcs/tasks/` (需新建) 下创建任务文件。

3.  **代码实现 (`TaskRegistry`)**:
    *   在后端注册这些预设任务，确保 `SubmitItemObjective` 能正确识别 NBT (如区分水瓶和治疗药水)。

## 5. JSON 配置示例 (Draft)

```json
// data/customnpcs/tasks/supply_medic_1.json
{
  "id": "customnpcs:supply_medic_1",
  "title": "Field Medic Needed",
  "description": "I'm running low on supplies. Bring me some Healing Potions.",
  "type": "side",
  "objectives": [
    {
      "type": "submit_item",
      "item": "minecraft:potion",
      "nbt": "{Potion:\"minecraft:healing\"}", 
      "count": 10,
      "base_value_snapshot": 50
    }
  ],
  "rewards": [
    {
      "type": "item",
      "item": "minecraft:emerald",
      "count": 7
    }
  ]
}
```