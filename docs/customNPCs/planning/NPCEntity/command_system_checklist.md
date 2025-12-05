- [ ] **Phase 1: 后端基础 (Backend)**
    - [ ] 定义 `NpcCommandType` 枚举 (NONE, FOLLOW, SIT, WORK, GUARD)。
    - [ ] 定义 `WorldStateKeys` 常量 (`OWNER_UUID`, `CURRENT_COMMAND`, `RELATIONSHIP_TYPE`)。
    - [ ] 创建 `CommandGoal` 类 (基础框架，优先级 90)。
    - [ ] 实现 `CommandGoal` 的 `FOLLOW` 逻辑 (调用 `MoveToOwnerAction`)。

- [ ] **Phase 2: 交互逻辑 (Interaction Logic)**
    - [ ] 更新 `InteractActionPayload` 处理逻辑，支持 `NPC_HIRE` 和 `NPC_OPPRESS`。
    - [ ] 更新 `OpenInteractGuiPayload` 生成逻辑，根据 `OWNER_UUID` 动态添加 Chat Options。
    - [ ] 验证：右键 NPC -> Chat -> 点击 Hire -> 状态变为 Owner。

- [ ] **Phase 3: 指令 UI (Command UI)**
    - [ ] 定义 `SetNpcCommandPayload` (C2S) 网络包。
    - [ ] 创建 `NpcCommandScreen` (简单的指令选择界面)。
    - [ ] 绑定 `Owner Opts` 按钮打开 `NpcCommandScreen`。
    - [ ] 验证：Owner Opts -> 点击 Follow -> NPC 开始跟随。
