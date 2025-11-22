# NPC 思维系统实施任务清单

- [ ] **基础：NpcMind Capability** <!-- id: 0 -->
    - [ ] 定义 `INpcMind` 接口（Sensors、Memory、Decision 访问） <!-- id: 1 -->
    - [ ] 实现 `NpcMind` capability provider 和存储 <!-- id: 2 -->
    - [ ] 将 Capability 附加到 `EntityGuzhenren`（或特定 NPC 实体） <!-- id: 3 -->
- [ ] **记忆系统** <!-- id: 4 -->
    - [ ] 创建 `MemoryModule`（短期 & 长期记忆） <!-- id: 5 -->
    - [ ] 实现 `MemoryEntry`（Key-Value 带过期/重要性） <!-- id: 6 -->
- [ ] **决策核心（Utility AI）** <!-- id: 7 -->
    - [ ] 定义 `IGoal` 接口（getPriority、canRun、tick） <!-- id: 8 -->
    - [ ] 实现 `GoalSelector`（评估器） <!-- id: 9 -->
    - [ ] 创建基础目标： <!-- id: 10 -->
        - [ ] `SurvivalGoal`（低血量时吃东西/治疗） <!-- id: 11 -->
        - [ ] `IdleGoal`（无事可做时闲逛/观察） <!-- id: 12 -->
- [ ] **执行层** <!-- id: 13 -->
    - [ ] 将 `NpcMind` 决策桥接到原版 `GoalSelector` 或自定义 Action 系统 <!-- id: 14 -->
- [ ] **集成与测试** <!-- id: 15 -->
    - [ ] 创建调试物品/命令来检查 NPC Mind 状态 <!-- id: 16 -->
    - [ ] 验证 NPC 根据状态变化切换目标 <!-- id: 17 -->
