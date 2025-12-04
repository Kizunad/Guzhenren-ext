# NPC 交互系统开发任务清单 (Checklist)

## Phase 1: 基础框架与网络 (Infrastructure)
- [ ] **网络包定义 (Network Packets)**
    - [ ] `OpenInteractGuiPacket` (S -> C): 携带 NPC ID, 基本信息, 状态列表数据。
    - [ ] `InteractActionPacket` (C -> S): 携带 NPC ID, 动作类型 (TRADE/GIFT/CHAT/CUSTOM), 附加数据。
- [ ] **数据结构 (Data Structures)**
    - [ ] 定义 `NpcStatusEntry` (Icon, Label, Value)。
    - [ ] 定义 `DialogueOption` (Text, ActionID)。
- [ ] **注册表接口 (Registry API)**
    - [ ] 创建 `IStatusProvider` 接口与注册表。
    - [ ] 创建 `IDialogueAction` 接口与注册表。

## Phase 2: 仪表盘 UI (Dashboard UI)
- [ ] **主容器 (Main Container)**
    - [ ] 创建 `NpcDashboardScreen` 类 (继承 `TinyScreen`)。
    - [ ] 实现 680x400 居中背景渲染。
- [ ] **组件实现 (Components)**
    - [ ] **Entity Preview**: 集成 `EntityWidget`，实现旋转控制。
    - [ ] **Status List**: 实现 `VerticalScrollList`，支持动态渲染 `NpcStatusEntry`。
    - [ ] **Action Buttons**: 实现右侧按钮组，绑定点击事件。
- [ ] **功能绑定 (Logic Binding)**
    - [ ] `Trade` 按钮 -> 发送打开交易包。
    - [ ] `Gift` 按钮 -> 发送打开背包包 (需后端 `GiftMenu` 支持)。
    - [ ] `Chat` 按钮 -> 切换 `screen` 状态到 Dialogue 模式。

## Phase 3: 对话面板 UI (Dialogue UI)
- [ ] **主容器 (Main Container)**
    - [ ] 创建 `NpcDialoguePanel` (作为 Dashboard 的子组件或独立 Screen)。
    - [ ] 实现 680x360 底部沉浸式布局。
- [ ] **组件实现 (Components)**
    - [ ] **Portrait**: 左侧显示立绘或 EntityWidget。
    - [ ] **Text Box**: 实现打字机效果的文本显示。
    - [ ] **Option List**: 实现可点击的选项列表。
- [ ] **交互逻辑 (Interaction)**
    - [ ] 点击选项 -> 发送 `InteractActionPacket`。
    - [ ] 处理服务端回包 -> 更新文本或关闭界面。

## Phase 4: 功能集成 (Feature Integration)
- [ ] **Gift 功能 (Inventory)**
    - [ ] 后端：创建 `NpcGiftMenu` (Container)。
    - [ ] 后端：处理背包同步与物品转移逻辑。
- [ ] **Owner Options (管理菜单)**
    - [ ] 定义 `tmp/owneropts` 数据结构。
    - [ ] 实现 "State" 切换 (Sit/Follow) 的网络逻辑。
- [ ] **Status 扩展测试**
    - [ ] 编写一个测试 Provider (例如显示 "Mood: Happy") 验证扩展性。

## Phase 5: 调试与优化 (Polish)
- [ ] **UI 适配测试**: 验证在不同 GUI Scale 下的显示效果。
- [ ] **异常处理**: NPC 死亡/消失时的 UI 关闭逻辑。
- [ ] **视觉优化**: 添加过渡动画 (Fade in/out)。
