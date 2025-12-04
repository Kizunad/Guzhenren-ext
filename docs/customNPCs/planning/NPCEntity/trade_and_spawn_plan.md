# 交易系统与生成配置落地计划 (Trade & Spawn Plan)

## 1. 目标
构建完整的自定义 NPC 生态基础，包括：
*   **交易系统**：实现从物品定价、交互界面 (UI) 到网络同步、服务端执行的完整交易闭环。
*   **生成控制**：实现基于配置文件的自然生成控制（开关、最大数量限制），并提供命名/皮肤的随机化池。
*   **实体落地**：完善 `CustomNpcEntity` 的交互与生成逻辑，使其成为游戏内的可用实体。

## 2. 交易系统 (Trade System)

### 2.1 核心逻辑 (Logic)
*   **定价服务 (`ItemValueManager`)**
    *   **当前状态**：`ItemValueManager` 已实现硬编码的基础价格表。
    *   **计划**：提供 API 查询 `ItemStack` 的基础价值 (`BaseValue`)。未来支持 JSON 配置加载。
*   **交易状态 (`NpcTradeState`)**
    *   **当前状态**：包含 `priceMultiplier` (价格系数) 和 NBT 序列化。
    *   **计划**：
        *   生成时随机初始化 `priceMultiplier` [1.0, 3.0]。
        *   集成到 `NpcMind` 或实体数据中，确保状态持久化。
*   **交易计算 (`NpcTradeHooks`)**
    *   **卖价公式**: `BaseValue * Multiplier * (1 + Fee)` (Fee = 10%)。
    *   **买价公式**: `BaseValue` (玩家以物易物)。
    *   **验证逻辑**: 确保 `Player Offer Total >= NPC Offer Total`。
    *   **库存交互**: 
        *   打开界面时：从 `NpcInventory` 随机复制物品到 UI 的 NPC 槽位。
        *   交易执行时：**再次验证** NPC 背包中是否仍有该物品，扣除后再给予玩家。

### 2.2 界面与交互 (UI & Interaction)
*   **菜单容器 (`NpcTradeMenu`)**
    *   **结构**：
        *   Slot 0-8: `playerOffer` (玩家放入筹码)。
        *   Slot 9-17: `npcOffer` (NPC 展示商品，**Read-Only**)。
        *   Slot 18+: 玩家背包。
    *   **同步**：通过 `FriendlyByteBuf` 同步 `priceMultiplier` 到客户端。
*   **屏幕界面 (`NpcTradeScreen`)**
    *   **技术栈**：基于 `tinyUI` 库实现。
    *   **布局**：全屏居中，三栏式布局 (Player | Controls | NPC)，动态显示 "Total Value"。
    *   **状态**：每 tick 重新计算双方价值并更新 Label。
*   **触发入口**
    *   `CustomNpcEntity.mobInteract`: 玩家右键 -> 服务端创建 State -> 打开 GUI。

### 2.3 网络通信 (Networking)
*   **需求**：客户端点击 "Trade" 按钮 -> 服务端执行交易。
*   **实现**：
    *   定义 `ServerboundTradePacket`。
    *   注册 Packet Handler。
    *   服务端调用 `NpcTradeHooks.performTrade(npc, menu, player)`。

## 3. 生成与实体配置 (Spawn & Entity Config)

### 3.1 配置管理
*   **`SpawnConfig`**
    *   `naturalSpawnEnabled` (Boolean): 是否允许自然生成。
    *   `maxNaturalSpawns` (Int): 全局自然生成最大数量限制 (避免泛滥)。
    *   集成到 `CustomNpcConfigs` (JSON 加载)。

### 3.2 生成控制 (`NpcSpawningHandler`)
*   **监听事件**：
    *   `EntityJoinLevelEvent`: 追踪当前活跃 NPC 数量 (`ACTIVE_NPCS` 计数)。
    *   `EntityLeaveLevelEvent`: 实体卸载/死亡时减少计数。
    *   `MobSpawnEvent.PositionCheck`: 在生成前检查 Config 开关和数量限制，返回 `Result.DENY` 拦截。
*   **注册表 (`NpcSpawnRegistry`)**
    *   提供 `onSpawn` 回调，用于第三方或内部模块注入生成后逻辑。

### 3.3 个性化 (Identity)
*   **命名池 (`NamePool`)**
    *   在 `onSpawn` 时为没有名字的 NPC 随机分配名字 (NameTag)。
*   **皮肤池 (`SkinPool`)**
    *   提供随机皮肤纹理路径。
    *   `CustomNpcEntity` 需同步皮肤数据 (EntityDataAccessor) 到客户端渲染。

## 4. 实施路线图 (Execution Roadmap)

### 阶段一：基础逻辑与界面 (已完成 ✅)
- [x] `ItemValueManager` (基础定价)
- [x] `NpcTradeState` (价格系数与序列化)
- [x] `NpcTradeHooks` (打开逻辑、价值计算、交易执行逻辑)
- [x] `NpcTradeMenu` (容器定义、只读槽位)
- [x] `NpcTradeScreen` (tinyUI 界面、动态价值显示)
- [x] `MindDebugCommand` 添加 `testTrade` 用于预览

### 阶段二：交互与实体落地 (已完成 ✅)
- [x] `SpawnConfig` & `CustomNpcConfigs` 更新
- [x] `NamePool` & `SkinPool`
- [x] `NpcSpawningHandler` (数量限制、自动命名)
- [x] `CustomNpcEntity` 集成右键打开交易
- [x] `CustomNpcEntity` 集成皮肤同步与生成回调

### 阶段三：网络闭环 (待执行 🚧)
- [ ] **Packet 定义**: 创建 `ServerboundTradePacket` (包含 `npcId` 或直接使用当前 `containerMenu` 上下文)。
- [ ] **Handler 实现**: 在服务端收到包后，验证 `Container` 类型，调用 `NpcTradeHooks.performTrade`。
- [ ] **注册**: 注册网络通道与包。
- [ ] **UI 绑定**: `NpcTradeScreen` 按钮点击发送网络包。

### 阶段四：数据完善与持久化 (待执行 ⏳)
- [ ] **State 集成**: 将 `NpcTradeState` 正式放入 `NpcMind` 或 Capability，确保重启后价格系数不丢失。
- [ ] **价格表扩充**: 完善 `ItemValueManager`，覆盖更多物品，或支持 JSON 配置。
- [ ] **反馈优化**: 交易成功/失败的音效与粒子。

## 5. 风险评估
*   **刷物品风险**: 交易时必须严格校验 NPC 实际库存，而非仅依赖 UI 中的 Ghost Item。目前逻辑已采用 "Copy for UI -> Remove from Inventory on Trade" 策略，需确保原子性。
*   **并发问题**: `ActiveNPCs` 计数在多维度的准确性（目前基于 `EntityJoin/Leave`，基本涵盖 Loaded 实体）。
