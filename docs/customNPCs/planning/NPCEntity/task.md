- [ ] **状态总结** <!-- id: npc-entity-status -->
    - [ ] 当前为预计划草案，尚未落地，实现按下列任务分批推进。

- [ ] **实体定义与注册** <!-- id: npc-entity-definition -->
    - [ ] 注册 `CustomNpcEntity` EntityType 与渲染器 <!-- id: npc-entity-type -->
    - [ ] 定义属性构建器（生命/攻击/移速/护甲） <!-- id: npc-entity-attributes -->
    - [ ] 构造时清空原版 `goalSelector/targetSelector`，只运行自研 AI <!-- id: npc-entity-clear-vanilla-ai -->
    - [ ] 指定/确认合适的 `PathNavigation`（地面/飞行） <!-- id: npc-entity-navigation -->

- [ ] **AI 集成与驱动** <!-- id: npc-entity-ai -->
    - [ ] 绑定 `NpcMind`、`SensorManager`、`UtilityGoalSelector`、`ActionExecutor` <!-- id: npc-entity-mind-bind -->
    - [ ] 实体 tick 驱动 `mind.tick`，初始化传感器（Vision/Damage/Safety） <!-- id: npc-entity-mind-tick -->
    - [ ] 调整 `MoveToAction` 等动作参数（超时/重算/速度因子）确保流畅寻路 <!-- id: npc-entity-move-tuning -->
    - [ ] 验证无原版 Goal 抢占：仅自有目标/动作在跑 <!-- id: npc-entity-no-vanilla-preempt -->

- [ ] **生存能力** <!-- id: npc-entity-survival -->
    - [ ] 基线属性与防御（含盾牌支持） <!-- id: npc-entity-base-stats -->
    - [ ] 进食/回血策略（可先被动回血，预留食物链路） <!-- id: npc-entity-heal -->
    - [ ] 伤害抗性处理（火焰/跌落等） <!-- id: npc-entity-resistance -->
    - [ ] 威胁响应链路：沿用 Sensors → triggerInterrupt → UtilityGoalSelector → Defend/Flee/Ranged/Melee <!-- id: npc-entity-threat -->

- [ ] **交互与装备** <!-- id: npc-entity-interaction -->
    - [ ] 默认装备槽与掉落规则（主/副手、护甲、弓/弩/盾/药水支持） <!-- id: npc-entity-gear -->
    - [ ] 交易接口预留（方法/数据结构占位） <!-- id: npc-entity-trade -->
    - [ ] 任务接口预留（事件/数据槽占位） <!-- id: npc-entity-quest -->

- [ ] **生成与持久化** <!-- id: npc-entity-spawn-persist -->
    - [ ] 命令生成：`spawn_test_entity` 切换为自定义 NPC，附默认装备 <!-- id: npc-entity-command-spawn -->
    - [ ] 自然生成配置（默认禁用或仅命令；预留群系/权重） <!-- id: npc-entity-natural-spawn -->
    - [ ] 持久化：`saveAdditional/readAdditional` 同步 mind/memory/inventory/cap <!-- id: npc-entity-persist -->

- [ ] **测试与调试** <!-- id: npc-entity-test -->
    - [ ] GameTest：生成/存活/威胁响应/持久化/接口存在性 <!-- id: npc-entity-gametest -->
    - [ ] 调试命令：`inspect`/`spawn_test_entity` 适配新实体 <!-- id: npc-entity-debug-cmd -->
