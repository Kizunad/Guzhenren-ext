# 动作层标准化计划 (Action Standardization Plan)

## 目标
解决当前动作系统能力边界不清的问题，消除 `Goal` 实现中的重复逻辑 (DRY)，并为未来扩展更多交互行为打下基础。

## 现状分析
目前 `ActionExecutor` 仅支持基础的 `MoveTo`, `LookAt`, `Wait`。
复杂的交互逻辑（如：攻击、使用物品、与方块交互）往往分散在各个 `Goal` 的 `tick` 方法或自定义的 `Action` 中，导致：
1.  **代码重复**: 多个 Goal 可能都需要“攻击”或“使用物品”。
2.  **维护困难**: 修改交互逻辑需要遍历所有相关的 Goal。
3.  **规划受限**: GOAP 规划器只能使用已封装为 `IAction` 的行为。

## 提议的修改

### 1. 定义通用动作接口
我们需要将常用的 Minecraft 交互行为抽象为标准的 `IAction` 实现。

#### [NEW] `AttackAction`
*   **用途**: 攻击指定目标实体。
*   **参数**: `TargetEntity` (目标), `Cooldown` (攻击间隔)。
*   **逻辑**:
    *   检查距离。
    *   调用 `entity.swing(Hand.MAIN_HAND)`。
    *   调用 `entity.doHurtTarget(target)`。
    *   处理攻击冷却。

#### [NEW] `UseItemAction`
*   **用途**: 使用手中的物品（如：吃食物、喝药水、拉弓）。
*   **参数**: `Item` (可选，指定物品), `Duration` (使用时长)。
*   **逻辑**:
    *   调用 `entity.startUsingItem(Hand.MAIN_HAND)`。
    *   等待 `Duration` ticks。
    *   调用 `entity.stopUsingItem()` 或等待自动完成。

#### [NEW] `InteractBlockAction`
*   **用途**: 与方块交互（如：开门、按按钮、打开箱子）。
*   **参数**: `BlockPos` (位置), `Direction` (面)。
*   **逻辑**:
    *   移动到交互距离内。
    *   调用 `BlockState.use(...)` 或模拟玩家交互。

### 2. 消除 Goal 中的重复逻辑 (DRY)
将 `Goal` 变为纯粹的“规划者”和“状态监控者”，将所有实际的物理交互移交给 `ActionExecutor`。

#### 重构 `SurvivalGoal`
*   **当前**: 在 `tick` 中直接调用 `entity.startUsingItem()`。
*   **重构后**:
    *   生成计划: `[UseItemAction(Food, 32)]`。
    *   提交给 Executor。

#### 重构 `CombatGoal` (假设存在或即将实现)
*   **当前**: 在 `tick` 中处理移动和攻击。
*   **重构后**:
    *   生成计划: `[MoveToAction(Target, Range), AttackAction(Target)]`。
    *   注意：战斗可能需要高频重规划，或者使用专门的 `CombatAction` 包含移动+攻击循环。

### 3. 统一动作结果处理
确保所有新动作都正确返回 `ActionStatus`:
*   `RUNNING`: 动作正在进行。
*   `SUCCESS`: 动作成功完成（如：造成了伤害，吃完了食物）。
*   `FAILURE`: 动作失败（如：目标消失，物品被抢走）。

## 实施步骤

1.  **创建基础类**:
    *   `src/main/java/com/Kizunad/customNPCs/ai/actions/common/AttackAction.java`
    *   `src/main/java/com/Kizunad/customNPCs/ai/actions/common/UseItemAction.java`
    *   `src/main/java/com/Kizunad/customNPCs/ai/actions/common/InteractBlockAction.java`

2.  **重构现有 Goal**:
    *   检查 `SurvivalGoal`，替换内联逻辑为 `UseItemAction`。
    *   检查 `GatherItemGoal`，确保 `PickUpItemAction` 符合标准。

3.  **验证**:
    *   编写 `ActionStandardizationTests`，验证新动作的独立执行能力。

## 详细实现规范

### 1. 行为规格补全

#### `AttackAction` 详细规格
**目标引用处理**:
- UUID 解析失败或目标为 null → 立即返回 `FAILURE`
- 目标死亡或切换维度 → 返回 `FAILURE`

**距离与导航**:
- 攻击距离阈值：`3.0` blocks（默认）
- 距离 > 阈值时：
  - 选项 A：返回 `RUNNING` + 内部调用 `MoveTo`
  - 选项 B：直接返回 `FAILURE`，由规划器重新生成 `[MoveTo, Attack]` 计划

**冷却与成功条件**:
- 攻击冷却时长：`20` ticks（1秒）
- 成功条件：命中一次即为 `SUCCESS`
- 失败条件：持续 `60` ticks（3秒）未能命中 → `FAILURE`
- 避免卡死：超时机制强制终止

**参数**:
```java
AttackAction(UUID targetUuid, double attackRange, int cooldownTicks, int maxAttempts)
```

---

#### `UseItemAction` 详细规格
**物品选择策略**:
1. 优先主手 `Hand.MAIN_HAND`
2. 若不匹配，检查副手 `Hand.OFF_HAND`
3. 若仍未找到，搜索背包并切换物品到主手
4. 都未找到 → `FAILURE`

**使用类型分支**:
- **瞬时使用**（食物、药水）：
  - 调用 `startUsingItem()` → 等待完成 → `stopUsingItem()`
  - 使用时长超时后强制 `stopUsingItem()` 并返回 `SUCCESS`
- **持续使用**（弓、盾牌）：
  - 需要外部信号（如战斗结束）调用 `stop()` 中断
  - 支持 `canInterrupt()` 检查

**失效处理**:
- 物品在使用中被其他动作替换 → 返回 `FAILURE`
- 物品耗尽 → 返回 `FAILURE`

**参数**:
```java
UseItemAction(Item targetItem, Hand preferHand, int maxUseTicks, boolean allowInterrupt)
```

---

#### `InteractBlockAction` 详细规格
**交互距离**:
- 默认交互距离：`4.0` blocks
- 超出距离时自动导航到可交互位置

**方块状态校验**:
- 方块不存在或已改变（`BlockState` 不同）→ `FAILURE`
- 需要朝向/方向的方块（如箱子、熔炉）：传入 `Direction` 参数校验

**交互方式**:
- 优先使用 `BlockState.use(level, player, hand, hitResult)`
- 若需要权限或模拟玩家交互，需要传入 `ServerPlayer` 或使用伪玩家

**参数**:
```java
InteractBlockAction(BlockPos pos, Direction face, double interactRange, boolean requirePlayer)
```

---

#### 通用动作基类 (`AbstractStandardAction`)
建议所有标准动作继承统一基类，提供：
- **上下文管理**: `UUID targetUuid`, `BlockPos targetPos`, `Level world`
- **超时控制**: `int timeoutTicks`, `int elapsedTicks`
- **重试机制**: `int maxRetries`, `int currentRetry`
- **导航范围**: `double navRange`
- **失效检查**: 实体/世界变更检测
- **统一日志**: 结构化日志输出（`LOGGER.debug("AttackAction: {}", status)`）

```java
public abstract class AbstractStandardAction implements IAction {
    protected final UUID targetUuid;
    protected final int timeoutTicks;
    protected final int maxRetries;
    protected int elapsedTicks = 0;
    protected int currentRetry = 0;
    
    @Override
    public ActionStatus tick(Mob entity) {
        if (++elapsedTicks > timeoutTicks) {
            return ActionStatus.FAILURE; // 超时失败
        }
        return tickInternal(entity);
    }
    
    protected abstract ActionStatus tickInternal(Mob entity);
}
```

---

### 2. 决策协调对齐

#### 幂等性与中途终止
- 所有标准动作的 `start()` 和 `stop()` 必须幂等（可重复调用）
- 动作内部**禁止缓存实体引用**，始终使用 `UUID + Level.getEntity()` 查询
- 支持中途终止（如计划切换、目标优先级变化）

#### 上下文校验
- 为动作增加 `planId` 或 `contextVersion` 字段
- 避免未来多计划并存时误用旧状态
- `ActionExecutor` 在切换计划时清空所有动作缓存

---

### 3. GOAP 集成与状态契约

#### 动作的前置条件与效果定义

| 动作 | Preconditions | Effects |
|------|---------------|---------|
| `AttackAction` | `target_visible: true`<br>`target_in_range: true` | `target_damaged: true`<br>`attack_cooldown_active: true` |
| `UseItemAction` | `has_item: <item_type>`<br>`item_usable: true` | `item_used: true`<br>`hunger_restored: true` (若是食物) |
| `InteractBlockAction` | `at_block_<pos>: true`<br>`block_exists: true` | `block_interacted: true`<br>`door_open: true` (若是门) |

#### 状态键命名规范
- 使用小写下划线命名：`target_in_range`, `has_food`, `at_block_<pos>`
- 布尔值状态：`target_visible`, `item_usable`
- 数值状态：`health_percent`, `distance_to_target`
- 避免状态名漂移，集中管理在 `WorldStateKeys` 常量类中

#### 状态更新时机
- 动作完成时（`SUCCESS`）：更新 `Memory` 和 `WorldState` 中的 `Effects` 键
- 动作失败时（`FAILURE`）：清除相关状态或标记失败原因
- 规划器需要知道哪些动作会写入哪些状态键，维护状态依赖图

---

### 4. 导航与范围处理

#### 统一到达判定
所有涉及导航的动作（`MoveTo`, `AttackAction`, `InteractBlockAction`）应复用统一的"到达/停留"判定逻辑：
- **距离阈值**: `distanceSq < threshold * threshold`
- **视线判定**: `hasLineOfSight()` 检查
- **最大导航步数**: `300` ticks（15秒）
- **最大导航时间**: 超时后返回 `FAILURE`

缺少统一判定会导致动作无限 `RUNNING`。

#### 移动目标的粘性策略
对于目标移动中的场景（跟随、战斗）：
- 每 `10` ticks 重新计算导航路径
- 连续 `5` 次路径失败后返回 `FAILURE`
- 使用"粘性"范围：目标在范围内移动时不重新导航，仅更新朝向

**默认值表**:
| 参数 | 默认值 | 说明 |
|------|--------|------|
| `attackRange` | 3.0 | 攻击距离（blocks） |
| `interactRange` | 4.0 | 交互距离（blocks） |
| `navTimeout` | 300 | 导航超时（ticks） |
| `renavInterval` | 10 | 重新导航间隔（ticks） |
| `maxNavRetries` | 5 | 最大导航重试次数 |

---

### 5. 性能与节流

#### Tick 频率限制
- 动作的 `tick()` 方法每 `1` tick 调用一次（与游戏循环同步）
- 寻路调用应限流：每 `10` ticks 重算路径
- 避免与感知扫描（VisionSensor、AuditorySensor）叠加造成 TPS 抖动

#### 寻路优化
- 使用 `PathNavigation.moveTo()` 的缓存机制
- 同一目标短时间内不重复计算路径
- 考虑使用 `PathNavigation.isDone()` 检查路径完成状态

---

### 6. 动画与客户端一致性

#### 动作反馈
- **`AttackAction`**: 调用 `entity.swing(Hand.MAIN_HAND)` 触发挥手动画
- **`UseItemAction`**: 播放使用物品动画，同步手部物品状态（主手/副手）
- **`InteractBlockAction`**: 触发交互反馈（如箱子打开音效、门开关动画）

#### 网络同步
- 确保动作触发的状态变化在服务端和客户端一致
- 必要时发送自定义网络包同步动画状态（如弓蓄力进度）
- 玩家可见的反馈是必需的，否则会导致 "NPC 卡住" 的错觉

---

### 7. 测试覆盖补全

#### `ActionStandardizationTests` 覆盖场景
**基础成功路径**:
- 攻击成功（目标在范围内）
- 使用食物成功（物品存在）
- 交互方块成功（方块存在且可交互）

**失败路径**:
- 目标丢失失败（UUID 解析为 null）
- 超时失败（导航超过 300 ticks）
- 距离失败（目标超出攻击范围且无法导航）

**边界条件**:
- 冷却生效（攻击后立即尝试再次攻击）
- 交互方块不存在（方块被破坏）
- 交互方块被替换（方块类型改变）

**持续使用类物品**:
- 弓的 `start()` → `stop()` 行为（蓄力中断）
- 盾牌的持续举起与放下

**组合场景（GameTest）**:
- 导航 + 攻击：`[MoveTo(target), AttackAction(target)]`
- 导航 + 交互：`[MoveTo(blockPos), InteractBlockAction(blockPos)]`
- 战斗循环：`CombatAction` 封装的完整战斗流程

---

### 8. 复合动作与可中断性

#### 复合动作定义
为控制规划深度和搜索空间，以下场景应封装为**复合动作**：
- **`CombatAction`**: 封装 `MoveTo(target) + AttackAction(target)` 循环，直到目标死亡或逃离
- **`ShootBowAction`**: 封装 `UseItemAction(bow,蓄力) + ReleaseAction(射击)`
- **`GatherAndStoreAction`**: 封装 `MoveTo(item) + PickUpAction + MoveTo(chest) + InteractBlockAction(chest, 存放)`

#### 可中断性策略 (`canInterrupt()`)
- **战斗动作**: 默认不可中断（优先级高）
- **使用物品**: 瞬时类可中断，持续类需要平滑中断（如弓需要先释放箭）
- **导航**: 随时可中断
- **交互方块**: 可中断，但需要清理状态（如关闭箱子界面）

**中断优先级表**:
| 动作类型 | 可中断 | 中断成本 |
|----------|--------|----------|
| `MoveToAction` | ✅ 是 | 低（直接停止导航） |
| `AttackAction` | ❌ 否 | 高（打断攻击会浪费冷却） |
| `UseItemAction` (食物) | ✅ 是 | 中（浪费物品） |
| `UseItemAction` (弓) | ⚠️ 条件 | 高（需要先释放） |
| `InteractBlockAction` | ✅ 是 | 中（需要清理状态） |

---

## 实施清单

### 阶段 1: 核心类创建
1. 创建 `AbstractStandardAction` 基类
2. 实现 `AttackAction`, `UseItemAction`, `InteractBlockAction`
3. 创建 `WorldStateKeys` 常量类（状态键管理）

### 阶段 2: 配置与默认值
1. 定义默认值配置文件（TOML/JSON）
2. 实现参数可调（攻击距离、超时时长等）
3. 添加日志与调试信息

### 阶段 3: GOAP 集成
1. 为每个标准动作定义 `Preconditions` 和 `Effects`
2. 更新 `GoapPlanner` 以支持新动作
3. 实现状态更新逻辑（Memory/WorldState）

### 阶段 4: 导航与性能优化
1. 统一"到达判定"逻辑
2. 实现移动目标的粘性策略
3. 添加寻路限流（每 10 ticks）

### 阶段 5: 动画与反馈
1. 触发 `swing()` 和使用动画
2. 同步客户端状态（网络包）
3. 添加音效和粒子效果

### 阶段 6: 测试
1. 编写单元测试（`ActionStandardizationTests`）
2. 创建 GameTest 场景（导航+动作组合）
3. 性能测试（多 NPC 并发执行）

### 阶段 7: 重构现有 Goal
1. 重构 `SurvivalGoal` 使用 `UseItemAction`
2. 重构 `GatherItemGoal` 确保符合标准
3. 实现 `CombatGoal` 使用 `CombatAction`

---

## 风险评估与取舍

### 粒度控制
*   **风险**: 将每个微小操作都变成 Action 可能导致规划器搜索空间爆炸。
*   **对策**: 
    - 对于紧密耦合的动作序列（如：蓄力-射击），封装为复合 Action (`ShootBowAction`)
    - 明确哪些是原子动作、哪些是复合动作
    - 规划器深度限制：最多 `5` 层动作嵌套

### 中断策略
*   **风险**: 所有动作都可中断会导致计划不稳定，所有动作都不可中断会导致 NPC 僵化。
*   **对策**:
    - 定义清晰的 `canInterrupt()` 策略（见上表）
    - 高优先级 Goal 可强制中断低优先级动作
    - 平滑中断：战斗/使用物品需要完成当前周期再中断

### 状态同步
*   **风险**: 客户端与服务端状态不一致导致"幽灵动作"（NPC 看起来在做某事但实际未生效）。
*   **对策**:
    - 所有状态变更在服务端权威
    - 必要时发送同步包
    - 客户端使用插值平滑动画

### 性能影响
*   **风险**: 多 NPC 并发执行复杂动作导致 TPS 下降。
*   **对策**:
    - 限流关键操作（寻路、感知扫描）
    - 使用对象池复用 Action 实例
    - 分帧执行（每帧只处理部分 NPC）

---

## 命名规范与约定

### 状态键命名
- 小写下划线：`target_in_range`, `has_food`
- 布尔值前缀：`is_`, `has_`, `can_`
- 位置相关：`at_block_<x>_<y>_<z>`

### 动作类命名
- 动词 + 名词：`AttackAction`, `UseItemAction`
- 复合动作：`CombatAction`, `GatherAndStoreAction`

### 参数命名
- 距离：`range`, `threshold`（单位：blocks）
- 时间：`ticks`, `duration`（单位：游戏刻）
- 标识符：`uuid`, `entityId`
