# 空窍（Kongqiao）系统开发文档

本文档面向维护/扩展 `com.Kizunad.guzhenrenext.kongqiao` 的开发者，聚焦：
- 空窍背包（容器/容量/同步）
- 念头鉴定（数据驱动的“用途”解锁）
- 杀招（数据驱动的被动/主动效果 + 推演解锁）
- 调整面板（被动开关/轮盘管理/说明展示）

---

## 目录结构（代码）

以 `src/main/java/com/Kizunad/guzhenrenext/kongqiao` 为根：

- `attachment/`：实体附着数据（空窍背包/解锁状态/玩家偏好等）
- `inventory/`：空窍背包与相关容器数据结构
- `menu/`：服务端菜单（容器）定义
- `client/`、`client/ui/`：客户端事件、按键、GUI
- `logic/`：效果接口与注册表
- `logic/impl/`：具体效果实现（按流派/层级组织）
- `niantou/`：念头数据结构/加载/校验/解锁判定
- `shazhao/`：杀招数据结构/加载/校验/解锁服务
- `service/`：运行时服务（tick、战斗、同步、触发）
- `network/`：空窍相关网络包（注意：部分包位于 `src/main/java/com/Kizunad/guzhenrenext/network`）
- `event/`、`handler/`：事件订阅与 tick 处理

---

## 目录结构（数据驱动资源）

数据包配置位于：
- `src/main/resources/data/guzhenrenext/niantou/*.json`：念头（物品用途）配置
- `src/main/resources/data/guzhenrenext/shazhao/*.json`：杀招配置

重要约束（多人服务器）：
- `data/` 属于服务器数据包（SERVER_DATA），专用服务器环境下客户端**不保证**能直接读取。
- UI 的“介绍/调整页面”如果依赖 `NianTouDataManager` / `ShazhaoDataManager`，需要服务端在登录/同步时下发数据（见“多人服数据同步”章节）。

---

## 核心概念

### 1) KongqiaoOwner：空窍持有者抽象

`src/main/java/com/Kizunad/guzhenrenext/kongqiao/KongqiaoOwner.java`
- 让玩家、NPC 甚至其他实体都可以复用空窍/攻击背包/喂养背包逻辑。

### 2) Attachment：运行时与存档的“事实来源”

`src/main/java/com/Kizunad/guzhenrenext/kongqiao/attachment/`

常用附件：
- `KongqiaoData`：空窍背包、攻击背包、喂养背包（并带 `dirty` 同步标记）
- `NianTouUnlocks`：念头用途解锁表 + 杀招解锁表 + UI message（推演结果提示）
- `TweakConfig`：玩家偏好（被动开关、轮盘技能列表）
- `ActivePassives`：当前“已启用的被动集合”（用于运行时态/互斥/开关联动）

设计原则：
- 服务端为权威（解锁、消耗、触发、同步由服务端判定与下发）。
- 客户端只做渲染与交互入口，最终操作走网络包到服务端。

### 3) 数据驱动：念头与杀招

念头：
- `NianTouData`：`itemID + usages[]`
- `usageID` 是逻辑主键，必须与 `GuEffectRegistry` 注册一致。
- `metadata` 支持 `{key}` 占位符替换（用于 UI 展示与可配参数）。

杀招：
- `ShazhaoData`：`shazhaoID + required_items + cost_total_niantou + metadata + desc/info...`
- `shazhaoID` 是逻辑主键，必须与 `ShazhaoEffectRegistry` 注册一致。
- 命名规范：`guzhenrenext:shazhao_passive_xxx` / `guzhenrenext:shazhao_active_xxx`（见 `ShazhaoId`）。

---

## 初始化与注册入口

`src/main/java/com/Kizunad/guzhenrenext/GuzhenrenExt.java`

初始化阶段做了：
- 注册菜单：`KongqiaoMenus`
- 注册附件：`KongqiaoAttachments`
- 注册网络包：`GuzhenrenExtNetworking`
- 注册效果逻辑：`GuModEffects.registerAll()`、`ShazhaoModEffects.registerAll()`
- 注册数据加载（服务端资源重载）：`AddReloadListenerEvent` 添加 `NianTouDataLoader`、`ShazhaoDataLoader`

---

## 运行时链路（从玩家视角）

### 1) 附着数据创建与同步（空窍背包本体）

- 实体进入世界/玩家克隆等：`KongqiaoAttachmentEvents`
- 服务端每 tick：`KongqiaoSyncService.onPlayerTick`
  - `KongqiaoCapacityService.syncCapacity` 根据最大真元动态调整可用格子
  - `KongqiaoData.dirty==true` 时发 `ClientboundKongqiaoSyncPayload` 同步到客户端

### 2) 念头鉴定（解锁用途）

- UI 点击“鉴定”后，服务端容器：`NianTouMenu.clickMenuButton`
  - 读取输入槽物品的 `NianTouData.usages`
  - 随机选择一个未解锁用途，写入 `NianTouUnlocks.startProcess(...)`
  - 立即同步 `PacketSyncNianTouUnlocks`
- 每 tick 推进鉴定：`NianTouTickHandler.onPlayerTick`
  - 扣除念头并推进进度，完成后 `unlocks.unlock(itemId, usageId)`
  - 完成时可触发“额外惊喜”逻辑（如尝试推演杀招）

### 3) 被动效果执行（念头用途）

- 每 tick / 每秒：`GuRunningService.onPlayerTick`
  - 遍历空窍已解锁槽位 -> 找到物品对应 `NianTouData.usages`
  - 对已解锁用途调用 `IGuEffect.onTick/onSecond`

### 4) 战斗触发（念头用途）

- 伤害事件：`GuCombatService.onLivingHurt`
  - 攻击者侧触发 `IGuEffect.onAttack`（可修改伤害）
  - 受害者侧触发 `IGuEffect.onHurt`（可减免/反制等）

### 5) 杀招：被动 tick / 主动轮盘

- 被动杀招每秒：`ShazhaoRunningService.tickUnlockedEffects`
  - 受 `NianTouUnlocks`（是否解锁）与 `TweakConfig`（是否禁用）共同约束
  - 对已注册的 `IShazhaoEffect` 调用 `onSecond`，否则 `onInactive` 撤销效果
- 主动杀招（轮盘触发）：`ServerboundSkillWheelSelectPayload`
  - 如果 `selectedUsageId` 属于 `shazhao_active_`，走 `ShazhaoActiveService.activate`

---

## 多人服数据同步（解决“有鉴定效果但看不到介绍/调整页面”）

问题现象：
- 专用服务器中，客户端 UI 可能拿不到 `data/guzhenrenext/**`（SERVER_DATA），导致：
  - `NianTouDataManager` / `ShazhaoDataManager` 在客户端为空
  - UI 依赖数据管理器渲染说明文本，从而“看不到介绍/调整页面”

推荐做法：
- 在玩家登录时由服务端下发“念头/杀招数据快照”到客户端，并在客户端侧写入 `NianTouDataManager` / `ShazhaoDataManager`：
  - `src/main/java/com/Kizunad/guzhenrenext/kongqiao/event/NianTouSyncEvents.java`
  - `src/main/java/com/Kizunad/guzhenrenext/kongqiao/network/PacketSyncKongqiaoData.java`

调试建议：
- 先确认服务端启动时 `NianTouDataLoader` / `ShazhaoDataLoader` 的加载日志数量正确。
- 再确认登录后客户端确实收到了同步包（必要时可在 handler 加日志）。

---

## “新增一个效果需要改哪些文件”清单

下面按“最常见的扩展类型”给出最小改动集合（KISS/YAGNI：只改你需要的那条路径）。

### A. 新增念头用途（被动：放空窍里持续生效）

1) 增加/修改数据（必做）
- 新增 JSON：`src/main/resources/data/guzhenrenext/niantou/<item>.json`
  - `usageID` 必须符合命名规范（参考 `NianTouUsageId` / `NianTouDataValidator`）
  - `metadata` 用于可配置参数，UI 可通过 `{key}` 进行替换展示

2) 实现逻辑（必做）
- 新增 `IGuEffect` 实现类：建议放到对应流派目录，例如：
  - `src/main/java/com/Kizunad/guzhenrenext/kongqiao/logic/impl/passive/daos/<dao>/.../<Effect>.java`
- 在对应 Registry 中注册（必做）：
  - 例如 `TierOneHunDaoRegistry.registerAll()` 内调用 `GuEffectRegistry.register(...)`

3) 校验与验证（建议）
- 启动时确认 `NianTouDataLoader` 未报命名校验错误
- 在空窍放入物品后，确认 `GuRunningService` 能在 tick 里触发（必要时临时加日志）

### B. 新增念头用途（主动：轮盘触发）

在 A 的基础上，还需要：
- `usageID` 必须是 `_active_`（由 `ServerboundSkillWheelSelectPayload` 判定）
- 在调整面板加入轮盘：
  - 客户端：`TweakScreen`（展示与按钮）
  - 服务端：`ServerboundTweakConfigUpdatePayload`（接收并写入 `TweakConfig`）

### C. 新增杀招（被动：每秒触发）

1) 增加/修改数据（必做）
- 新增 JSON：`src/main/resources/data/guzhenrenext/shazhao/<shazhao>.json`
  - `shazhaoID` 建议使用：`guzhenrenext:shazhao_passive_<name>`
  - `required_items`：列出推演/解锁所需的蛊虫 itemId
  - `metadata`：用于 `{key}` 替换展示/可配参数

2) 实现逻辑（必做）
- 新增 `IShazhaoEffect` 实现类（被动）
  - 推荐目录：`src/main/java/com/Kizunad/guzhenrenext/kongqiao/logic/impl/passive/daos/<dao>/shazhao/`
- 在 `ShazhaoModEffects.registerAll()` 或对应 `...ShazhaoRegistry.registerAll()` 中注册到 `ShazhaoEffectRegistry`

3) 解锁/推演联动（按需）
- 如果涉及“推演候选筛选/概率”：检查 `ShazhaoUnlockService` 的条件是否满足你的设计
- UI 展示与开关：
  - `TweakScreen` 右侧卡片展示（需要客户端已同步到 `ShazhaoDataManager`）
  - 被动开关走 `TweakConfig.disabledPassives`

### D. 新增杀招（主动：轮盘触发）

在 C 的基础上，还需要：
- `shazhaoID` 使用：`guzhenrenext:shazhao_active_<name>`
- 实现 `IShazhaoActiveEffect`（并在 `ShazhaoActiveService.activate` 路径可被识别）
- 允许加入轮盘：同样走 `TweakConfig` + `ServerboundTweakConfigUpdatePayload` + `ServerboundSkillWheelSelectPayload`

---

## 常见问题（快速定位）

1) 专用服务器“能鉴定但看不到介绍/调整页面”
- 优先检查：客户端是否收到 `PacketSyncKongqiaoData` 并写入 `NianTouDataManager` / `ShazhaoDataManager`
- 其次检查：`TweakScreen`/`NianTouScreen` 是否在渲染时只依赖客户端数据管理器（多人服必须确保同步完成）

2) “推演杀招显示无可解锁，但其实已经解锁了”
- `ShazhaoUnlockService.listUnlockCandidates` 会跳过“已解锁”的杀招，因此候选为 0 是合理结果
- 需要在调整面板（`TweakScreen`）里展示已解锁杀招，并提供被动开关/轮盘加入

---

## 规范与命令

- 代码规范检查：`./gradlew checkstyleMain`
- 建议在提交前至少跑：`./gradlew check`

