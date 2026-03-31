# 势力驱动的升仙压力事件设计（Bastion 退场后的正式方案）

## 1. 目的

这份文档用于明确一件事：

**Bastion 已废弃。**

后续升仙流程中的外部压力，不再来自“地图上的敌对基地生态”，而是来自：

- 宗门
- 家族
- 敌对势力
- 散修群体

也就是说，势力系统不能继续只做：

- 数据结构
- UI 面板
- 关系图
- 升仙倍率修正器

它必须变成**真正会在升仙阶段出手、索取、护持、背刺、断供、追猎的活系统**。

---

## 2. 当前代码现状

目前 faction 分支里，骨架已经足够完整：

### 2.1 已有基础

- `FactionWorldData`
  - 已负责持久化势力、关系矩阵、成员列表。
- `FactionService`
  - 已能创建势力、加成员、改关系、查数据。
- `FactionEventBus`
  - 已有内部事件总线，支持创建 / 解散 / 成员变动 / 关系变化 / 宣战事件。
- `FactionBehaviorHooks`
  - 已能把关系变化映射成 NPC 记忆标签。
- `FactionAscensionModifier`
  - 已能根据势力资源、战力、敌对数量，把势力状态折算成：
    - readiness 修正
    - tribulation intensity / invasion spawn 修正
- `FactionInfoScreen` / `FactionRelationGraph`
  - 已有基础信息展示路径。

### 2.2 当前缺口

现在最大问题不是“没有系统”，而是：

> 势力仍然主要活在后台参数里，没有真正成为升仙流程中的事件参与者。

具体表现为：

1. 宗门现在只是倍率保护，不会真的提出条件。
2. 家族现在只是身份概念，不会在失败后接应或逼债。
3. 敌对势力现在只是 hostility severity，不会真的侦查、断供、派人。
4. 散修现在没有形成“交易 / 结盟 / 背刺 / 求援”的事件角色。

---

## 3. 核心目标

势力系统后续必须完成下面的角色转变：

| 旧状态 | 新状态 |
|---|---|
| 势力影响升仙数值 | 势力影响升仙事件 |
| UI 里能看到势力 | 升仙过程中必须回应势力 |
| 宗门 / 家族是背景身份 | 宗门 / 家族是可索取、可庇护、可惩罚的组织 |
| 敌对势力只存在于关系值 | 敌对势力会实际制造侦查、断供、袭击与追猎 |

一句话说：

**要把“势力影响升仙”从后台修正，升级成前台剧情压力。**

---

## 4. 权威边界

## 4.1 谁拥有升仙主线真相

升仙主线的阶段真相，仍然必须由仙窍系统持有。

也就是：

- `AscensionAttemptStage`
- `ApertureWorldData` / 升仙尝试持久化记录

仍然是主线权威。

势力系统**不能反过来拥有升仙阶段**。

## 4.2 谁拥有势力事件真相

建议分两层：

### 势力系统拥有

- 事件定义
- 事件触发条件
- 势力来源（宗门 / 家族 / 敌对 / 散修）
- 响应选项
- 结果如何修改关系 / 债务 / hostility / resources

### 升仙系统拥有

- 当前这次升仙尝试有哪些事件正在生效
- 这些事件处于 `OFFERED / ACCEPTED / EXPIRED / RESOLVED` 的哪一步
- relog / restart 后如何恢复事件状态

## 4.3 `FactionEventBus` 的定位

`FactionEventBus` 仍然只做**运行时广播**，不做权威持久化。

也就是说：

- 持久化状态落盘在 `FactionWorldData` 或升仙尝试附带记录中
- `FactionEventBus` 只负责通知：
  - 事件生成
  - 事件升级
  - 事件解决
  - 关系变化

不能把 Bus 当数据库用。

---

## 5. 升仙阶段接点

后续势力事件必须绑定到明确的升仙阶段，而不是“随缘弹窗”。

建议绑定到以下阶段：

| 升仙阶段 | 事件重点 |
|---|---|
| `ASCENSION_PREPARATION_UNLOCKED` | 宗门护持报价、敌对侦查、散修情报交易 |
| `QI_OBSERVATION_AND_HARMONIZATION` | 断供、材料卡脖子、家族调配、人情交换 |
| `READY_TO_CONFIRM` | 最终站队、护持承诺、背刺窗口 |
| `CONFIRMED` | 债务锁定、援手锁定、敌意升级 |
| `WORLD_TRIBULATION_IN_PLACE` | 护持兑现、敌对袭扰、散修趁火打劫或临时援手 |
| `FAILED_SEVERE_INJURY` | 家族接应、宗门问责、敌对追猎、散修黑市恢复 |
| `FAILED_DEATH` | 势力关系大幅波动、遗留债务、遗产争夺、敌对势力庆功 |

这样玩家每推进一步，势力压力都会换一张脸，而不是始终是同一种“数值加减”。

---

## 6. 事件总结构

## 6.1 事件实例状态

建议每个事件实例至少包含：

- `eventId`
- `eventType`
- `ownerPlayerId`
- `sourceFactionId`（散修事件可为空）
- `stageGate`
- `severity`
- `state`
- `offerExpireGameTime`
- `resolutionExpireGameTime`
- `selectedResponse`
- `snapshotModifiers`
- `followupEventId`

状态建议：

- `OFFERED`
- `ACCEPTED`
- `REJECTED`
- `ESCALATED`
- `RESOLVED_SUCCESS`
- `RESOLVED_FAILURE`
- `EXPIRED`

## 6.2 事件定义字段

事件定义层建议至少包含：

- `eventTypeId`
- `sourceType`（SECT / CLAN / HOSTILE / ROGUE）
- `allowedStages`
- `minRelation`
- `maxRelation`
- `requiresMembership`
- `requiresHostilePresence`
- `resourceCost`
- `contributionCost`
- `tribulationModifierDelta`
- `readinessModifierDelta`
- `followupRules`

---

## 7. 首批正式事件（替代 Bastion 的外部压力）

下面这批事件，不是 lore 草案，而是建议作为第一批真正落地的玩法事件。

## 7.1 宗门护持：`sect_protection_pact`

### 触发阶段

- `ASCENSION_PREPARATION_UNLOCKED`
- `READY_TO_CONFIRM`

### 触发条件

- 玩家属于宗门
- 宗门关系稳定
- 宗门资源 / 战力达到最低要求

### 玩家选项

1. **接受护持**
   - 获得 readiness / tribulation 保护修正
   - 立即登记贡献债务
2. **拒绝护持**
   - 保持独行
   - 宗门好感下降

### 设计意义

宗门不该只是“给你减点劫难倍率”，而应该是：

> “我可以保你一程，但你之后得还。”

## 7.2 宗门索取：`sect_merit_collection`

### 触发阶段

- `CONFIRMED`
- `WORLD_TRIBULATION_IN_PLACE`
- `FAILED_SEVERE_INJURY`

### 触发条件

- 玩家接受过宗门护持
- 宗门债务未清

### 玩家选项

1. 交资源
2. 交贡献
3. 拒绝

### 后果

- 清账、缓期、降关系、宗门问责四种分支

## 7.3 家族接应：`clan_bloodline_escort`

### 触发阶段

- `READY_TO_CONFIRM`
- `FAILED_SEVERE_INJURY`

### 触发条件

- 玩家属于家族
- 家族未敌视玩家

### 玩家选项

1. 请求家族接应
2. 拒绝家族介入

### 效果

- 接受：获得恢复帮助 / 撤离帮助 / 资源补给
- 代价：未来出现血脉义务 / 资源索取 / 站队压力

## 7.4 家族逼债：`clan_debt_recall`

### 触发阶段

- `FAILED_SEVERE_INJURY`
- 恢复阶段

### 触发条件

- 曾接受家族援助
- 恢复资源不足

### 设计意义

让“家族帮助”不是纯回血按钮，而是：

> 你活下来了，但以后得听家里的。

## 7.5 敌对侦查：`hostile_scout_net`

### 触发阶段

- `ASCENSION_PREPARATION_UNLOCKED`
- `QI_OBSERVATION_AND_HARMONIZATION`

### 触发条件

- 存在敌对势力（关系 < -50）

### 玩法效果

- 持续累计“暴露度”
- 暴露度过高会转化为：
  - 更高 invasion modifier
  - 更容易触发后续袭扰事件

### 玩家应对

- 花资源压消息
- 主动清理线人
- 付钱给散修买反情报

## 7.6 敌对断供：`hostile_supply_embargo`

### 触发阶段

- `QI_OBSERVATION_AND_HARMONIZATION`
- `READY_TO_CONFIRM`

### 玩法效果

- 仪式材料获取成本上升
- 部分准备指标推进变慢
- 玩家必须改线、补线或冒险绕路

### 设计意义

把“敌对势力影响升仙”从简单数值修正，变成真正的资源压力。

## 7.7 敌对突袭：`hostile_strike_team`

### 触发阶段

- `CONFIRMED`
- `WORLD_TRIBULATION_IN_PLACE`

### 玩法效果

- 这不是 Bastion 式基地生态，而是**明确归属某势力的袭扰事件**
- 第一期可以先做成：
  - 数值加压
  - 入侵倍率上升
  - UI 警告
- 第二期再进化为真正的 NPC 小队来袭

## 7.8 散修黑市：`rogue_black_market_offer`

### 触发阶段

- `ASCENSION_PREPARATION_UNLOCKED`
- `FAILED_SEVERE_INJURY`

### 玩家选项

1. 高价买情报
2. 高价买恢复物资
3. 卖未来人情

### 风险

- 便宜的方案可能带来背刺标记
- 物资买到了，但也把你的升仙消息卖出去了

## 7.9 散修求援 / 背刺：`rogue_mutual_aid_or_betrayal`

### 设计目标

散修不该只有“商店”角色。

更适合做成：

- 你帮过的散修，在你渡劫或重伤时来帮你
- 也可能在高价值局面里反手背刺你

这能让散修关系不只是一个 UI 标签，而是带有人情与风险的动态来源。

---

## 8. 玩家动词（必须真实可玩）

势力事件如果只有“弹窗 + 同意 / 拒绝”，就会再次退化为后台参数系统。

后续至少要支持这些玩家动词：

- 接受护持
- 拒绝护持
- 上交资源
- 上交贡献
- 压消息
- 买反情报
- 清线人
- 请求接应
- 接受黑市交易
- 拒绝带债援助

这批动词不一定第一天全做成实体交互，但事件系统必须围绕它们设计。

---

## 9. 与现有代码的具体对接

## 9.1 `FactionAscensionModifier`

这个类后续不应废弃。

它的正确定位应改成：

- **基础快照计算器**
- 给事件选择器提供底层数值背景

例如：

- hostileFactionCount 高 → 提高侦查 / 突袭事件权重
- factionResources 高 → 提高宗门护持可用性
- factionPower 高 → 提高护持强度或接应强度

## 9.2 `FactionEventBus`

建议新增事件类型：

- `AscensionPressureEventOffered`
- `AscensionPressureEventAccepted`
- `AscensionPressureEventEscalated`
- `AscensionPressureEventResolved`

这些事件用于：

- UI 更新
- NPC 记忆标签刷新
- 调试命令输出

## 9.3 `FactionWorldData`

建议不要把“这次升仙的活动事件实例”硬塞成一堆临时 map。

正确做法应是：

- `FactionWorldData` 继续持久化组织侧事实
- 升仙尝试相关事件实例，落到升仙尝试持久化记录，或在其上挂一层明确的 pressure ledger

## 9.4 `FactionInfoScreen`

当前界面只显示：

- 势力名
- 势力类型
- 成员数
- 战力
- 资源
- 玩家关系值

后续最少要补：

- 当前活动中的升仙压力事件
- 当前债务 / 义务
- 当前敌对热度
- 宗门 / 家族是否可调用护持

## 9.5 `XianqiaoUiProjection`

仙窍 UI 投影层后续需要补：

- 当前阶段对应的势力风险摘要
- 活动事件倒计时
- 如果拒绝处理，会导致什么后果

玩家应该能在升仙 UI 里直接感受到：

> 不是天地要劈你，而是人也在趁你病要你命。

---

## 10. 推荐分阶段实现

## 阶段 1：先把事件做成可持久化的玩法层，不急着上 NPC 表现

包含：

- 事件定义表
- 事件实例状态机
- 与 `AscensionAttemptStage` 的对接
- UI 警示与选择
- 关系变化与债务变化

这一阶段就已经能把 faction 从“后台参数”拉到“前台玩法”。

## 阶段 2：再让事件长出具体实体表现

例如：

- 敌对 strike team 真正刷出 NPC 小队
- 家族接应派人护送
- 散修黑市在地图上以临时接头点呈现

## 阶段 3：再做更深的长期后果

例如：

- 宗门护持换来的后续征召
- 家族援助后的婚配 / 资源绑定 / 血脉义务
- 敌对势力长期追猎标记

---

## 11. 测试要求

必须至少覆盖以下场景：

1. **事件候选生成**
   - 玩家无势力 → 只应优先生成敌对 / 散修类事件
   - 玩家属于宗门 → 可生成宗门护持类事件
   - 玩家属于家族 → 可生成家族接应类事件

2. **阶段门控**
   - 侦查类事件只在准备期出现
   - 债务追讨类事件不会在准备早期乱入

3. **持久化恢复**
   - `CONFIRMED` 后 relog / restart，活动事件不能丢

4. **结果幂等**
   - 同一个事件不能重复结算两次
   - 不能重复扣债 / 重复改关系

5. **数值联动**
   - 接受宗门护持后，`FactionAscensionModifier` 的护持效应确实改变升仙外部修正

---

## 12. 一句话结论

既然 Bastion 已经废弃，那么后续升仙外部压力的正式接班人，就不该是另一套抽象生态系统。

它应该是：

- 宗门的人情
- 家族的血债
- 敌对势力的盯防
- 散修的交易与背刺

只有这样，势力系统才会从“你能在面板里看到的东西”，变成“你升仙时真的绕不开的东西”。
