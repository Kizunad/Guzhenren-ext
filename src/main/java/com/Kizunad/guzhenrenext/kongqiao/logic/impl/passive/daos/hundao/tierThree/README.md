# 三转魂道：鬼炎蛊 (Gui Yan Gu)

本目录包含 **三转魂道蛊虫——鬼炎蛊** 的核心逻辑实现。该蛊虫被设计为攻防一体的领域型被动手段，并演示了如何高效使用 NeoForge 的 **Data Attachments (原 Capabilities)** 系统。

## 1. 核心机制

### 攻防一体 (Offense & Defense)
- **视觉效果**：在玩家周围生成环绕的 `CYAN_WOOL` (青色羊毛) 粒子与魂火粒子，模拟鬼火护盾。
- **防御属性**：提供高额盔甲值加成 (`AttributeModifier`)。
- **AOE 伤害**：
  - 每秒扫描半径 4.0 格内的敌对生物。
  - 造成 **魂道 (Magic)** + **火焰 (Fire)** 混合伤害。
  - 智能识别同伴（队友/宠物），避免误伤。

### 停滞期 (Stasis)
为了平衡强度，引入了“过载停滞”机制：
- **触发条件**：单次受到 `>100` 点伤害（通过事件监听器检测）。
- **惩罚**：
  - 失去所有防御与攻击能力。
  - 物品耐久度减少。
  - 进入 **60秒** 停滞冷却期。
- **代价**：停滞期间每秒消耗 **1点魂魄** 用于自我修复。

---

## 2. 技术实现：ActivePassives Attachment

为了优化性能，避免在每次受伤 (`LivingDamageEvent`) 时遍历玩家背包，我们引入了 **`ActivePassives`** 数据附件系统。

### 传统做法 (低效)
`OnHurt Event` -> 遍历背包 (36格) -> 检查每个物品 NBT -> 判断是否为鬼炎蛊 -> 判断是否激活 -> 执行逻辑。
*缺点：高频事件中性能开销大。*

### 优化做法 (高效 - O(1))
1. **激活时**：`GuiYanGuEffect.onSecond()` 确认真元充足后，将 `USAGE_ID` 注入玩家的 `ActivePassives` 附件。
2. **受伤时**：`GuiYanDamageHandler` 直接检查 `ActivePassives.isActive("guzhenren:guiyangu_passive_shield")`。
   - 如果为 `false`，直接返回（零开销）。
   - 如果为 `true`，才进行后续处理。

### 关键类
- **`GuiYanGuEffect.java`**: 
  - 实现了 `IGuEffect` 接口。
  - 负责每秒的真元消耗、粒子特效渲染、AOE伤害计算。
  - 维护 `NBT_STASIS_TIMER` (停滞计时器)。
- **`GuiYanDamageHandler.java`**:
  - 监听 `LivingDamageEvent`。
  - 处理“重创破防”逻辑，将蛊虫打入停滞状态。
