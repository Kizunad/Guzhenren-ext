# NPC 交互系统主规划 (Main Planning)

## 1. 核心理念
构建一个现代化、沉浸式且高度可扩展的 NPC 交互系统。该系统将传统的“功能菜单”与“剧情对话”分离，采用双层级 UI 架构，利用 `tinyUI` 库实现。

### 架构分层
1.  **Dashboard (仪表盘层)**: 
    - **定位**: 信息概览与功能入口。
    - **风格**: 科技感/现代化，半透明，信息密度高。
    - **核心**: 展示 NPC 状态、模型预览、核心功能入口 (Trade/Gift/Chat/Owner)。
2.  **Dialogue (对话层)**:
    - **定位**: 剧情交互与指令下达。
    - **风格**: RPG 沉浸式，底部宽幅面板。
    - **核心**: 立绘/肖像、剧情文本、决策选项 (Chat Options)。

---

## 2. 功能模块详解

### 2.1 状态看板 (Status Dashboard) - 可扩展
**目标**: 提供一个可视化的 NPC 健康与状态监视器，支持模块化扩展。
*   **基础数据**: HP (条/数值)、Level/EXP、职业/身份。
*   **扩展接口 (`IStatusProvider`)**: 允许后续模块（如饥饿系统、修仙境界、好感度模组）向面板注册并渲染自定义状态行。

### 2.2 功能入口 (Functional Buttons)
*   **Trade (交易)**: 发送网络包打开已有的 `NpcTradeMenu`。
*   **Gift (赠礼/背包)**: 
    *   *逻辑*: 打开一个特殊的容器界面 (ContainerMenu)。
    *   *功能*: 玩家将物品放入 NPC 背包，增加好感度或更新 NPC 装备。
*   **Chat (对话)**: 切换至 Dialogue 视图。
*   **Owner Opts (控制)**: 仅拥有者可见。进入管理子菜单。

### 2.3 对话系统 (Chat/Dialogue) - 可扩展
**目标**: 承载剧情、任务与指令。
*   **数据源**: 
    *   **静态**: 预定义的对话树 (JSON)。
    *   **动态 (LLM)**: 实时生成的文本 (预留接口)。
*   **选项扩展 (`IDialogueOption`)**:
    *   标准选项: "你好" -> 跳转节点。
    *   功能选项: "跟我走" -> 触发 AI 状态变更。
    *   交易选项: "我想买东西" -> 触发 Trade 逻辑。

### 2.4 所有者控制 (Owner Options) - `tmp/owneropts`
**目标**: 快捷控制 NPC 的行为模式。
*   **设计**: 可能是 Dashboard 上的二级菜单或 Dialogue 中的特殊选项组。
*   **指令集**: 
    *   **State**: 待机 (Sit) / 跟随 (Follow) / 巡逻 (Patrol)。
    *   **Strategy**: 主动攻击 / 被动防御 / 避战。

---

## 3. 技术架构与数据流

### 3.1 网络通信 (Networking)
由于 UI 是客户端逻辑，而 NPC 状态与 AI 是服务端逻辑，需要严格的同步机制。

*   **S -> C (Sync)**: 玩家右键 NPC -> 服务端发送 `OpenInteractGuiPacket`，携带：
    *   NPC 基本属性 (HP, Name)。
    *   **序列化的扩展状态列表** (Hunger: 80%, Mood: Happy)。
    *   所有权状态 (IsOwner)。
*   **C -> S (Action)**: UI 按钮点击 -> 发送指令包：
    *   `ServerboundInteractActionPacket`: (ActionType: TRADE/GIFT/SET_STATE, Payload)。

### 3.2 扩展性设计 (Extensibility)
为了满足 "Status 板块扩展" 和 "Chat Options 扩展"，将采用注册表模式：

```java
// 伪代码示例
public interface INpcInteractionRegistry {
    // 注册状态提供者 (用于 Dashboard)
    void registerStatusProvider(ResourceLocation id, Function<NpcEntity, StatusLine> provider);
    
    // 注册对话动作 (用于 Chat Options)
    void registerDialogueAction(String actionId, BiConsumer<Player, NpcEntity> handler);
}
```

---

## 4. 交互流程图 (User Flow)

1.  **Start**: 玩家 `Shift + 右键` 或 `右键` NPC。
2.  **Check**: 服务端检查权限与状态，发送 Sync Packet。
3.  **Dashboard Open**: 客户端渲染 Dashboard。
    *   *分支 A*: 玩家看了一眼状态，关闭 (ESC)。
    *   *分支 B*: 玩家点击 "Trade" -> 关闭 Dashboard -> 发送包 -> 服务端打开 Container UI。
    *   *分支 C*: 玩家点击 "Chat" -> Dashboard 隐藏 -> Dialogue Panel 滑出/显示。
        *   玩家选择选项 "跟我走" -> 发送包 -> 服务端修改 AI Goal -> Dialogue 更新文本 "好的，主人"。
4.  **Close**: 任意阶段 ESC 或 点击关闭。

