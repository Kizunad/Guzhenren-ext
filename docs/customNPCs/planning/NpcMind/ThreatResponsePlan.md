# 威胁响应策略细化（撤退/格挡/远程） @planning

## 目标与边界
- 目标：让 NPC 在受到威胁时按距离/血量/装备状况选择撤退、格挡、远程或近战反击，并与现有中断/滞后机制兼容。
- 非目标：不新增全局战术协作/多 NPC 编队；不修改核心规划算法，仅在状态/动作/Goal 层补全。

## 现状缺口
- 传感器：`VisionSensor` 未写入 `target_visible/target_in_range` 等世界状态，也未记录最近威胁的 UUID/距离。
- 状态管线：`WorldStateKeys` 缺少战斗距离/武器可用性等键；`NpcMind.getCurrentWorldState` 未输出威胁细节。
- 行为：只有近战 `AttackAction`（不可中断）；无格挡动作、无远程攻击动作；撤退逻辑仅存在于未注册的 `FleeGoal`。
- 注册：`NpcMindRegistry` 未注册 `FleeGoal/DefendGoal/SeekShelterGoal`，默认不会参与决策。
- 测试：缺少针对威胁响应的 GameTest 场景。

## 交付清单
- 文档：本计划 + `task.md` checklist 补充。
- 代码：
  - 传感器/状态：威胁目标 UUID/距离、`target_visible`/`target_in_range`、`hostile_nearby` 等写入记忆与世界状态。
  - Goal：注册并调优 `FleeGoal`（逃离危害/敌对）、`DefendGoal`（格挡/反击）、必要时 `SeekShelterGoal`（环境危害时退避）。
  - Action：新增格挡动作（盾牌/举盾），新增远程攻击动作（弓/可投射武器），必要的配置项。
  - 选择策略：基于距离/血量/装备动态切换（撤退>格挡/防御>远程>近战），兼容滞后与中断。
- 测试：GameTest 覆盖撤退成功、格挡减伤、远程命中/弹药不足回退、目标切换时清理队列。

## 任务分解（KISS/YAGNI 顺序）
1) **状态与键补全**
   - 在 `VisionSensor` 写入：最近威胁 UUID、距离，`target_visible`、`target_in_range`、`hostile_nearby`。
   - `WorldStateKeys` 增加：`distance_to_target`、`has_ranged_weapon`（若需要）、`can_block` 等；`NpcMind.getCurrentWorldState` 输出。
2) **目标注册与基线策略**
   - 在 `NpcMindRegistry` 注册 `FleeGoal`、`DefendGoal`（及可选 `SeekShelterGoal`），校验默认优先级与滞后阈值：CRITICAL → 直接可切换；IMPORTANT → 滞后判定。
3) **撤退细化**
   - `FleeGoal`：使用最近威胁/危害方向反向的安全点，增加“到达安全距离”判定与重算；进入时清理计划队列。
4) **格挡/防御**
   - 新建 `BlockAction`（基于盾或可格挡物品），支持持续举盾/超时；在 `DefendGoal` 中按威胁类型选择格挡或近战反击。
5) **远程攻击**
   - 新建 `RangedAttackAction` + GOAP 包装：检查可用远程武器/弹药，保持距离窗（如 6~12 格），写效果 `target_damaged`。
   - 防御/战斗目标在中远距离优先使用远程，近距离回退近战。
6) **行为清理与中断**
   - 目标切换时调用 `stopCurrentPlan`；攻击类动作可选中断配置，确保 CRITICAL 时撤退能抢占。
7) **测试**
   - GameTest：低血+敌对触发撤退并远离；远程命中测试；投射物命中时举盾减伤；无弹药时回退近战；切换目标后动作队列清空。

## 验收标准
- 交互：CRITICAL 事件下能从近战中断并撤退；IMPORTANT 事件在滞后门槛内保持当前目标。
- 行为：根据距离自动在撤退/远程/近战之间切换；有盾时可格挡并降低伤害。
- 状态：`WorldStateKeys` 中的威胁相关键在规划器可读且实时更新。
- 测试：新增 GameTest 全部通过，现有测试不回归。

## 风险与缓解
- **装备缺失**：无盾/无弓时回退到防御近战或直接撤退。
- **性能**：传感器更新频率上限保持现值，避免额外 O(n^2) 扫描；日志受配置控制。
- **振荡**：依赖滞后阈值与冷却，必要时为撤退/防御加短冷却避免频繁切换。
