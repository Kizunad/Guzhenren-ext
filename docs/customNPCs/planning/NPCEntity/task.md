- [ ] **状态总结** <!-- id: npc-entity-status -->
    - [ ] 当前为预计划草案，尚未落地，实现按下列任务分批推进。

- [x] **实体定义与注册** <!-- id: npc-entity-definition -->
    - [x] 注册 `CustomNpcEntity` EntityType（ModEntities） <!-- id: npc-entity-type -->
    - [x] 定义属性构建器（生命/攻击/移速/护甲）常量化 <!-- id: npc-entity-attributes -->
    - [x] 构造时清空原版 `goalSelector/targetSelector`（留空 registerGoals），仅自研 AI <!-- id: npc-entity-clear-vanilla-ai -->
    - [x] 指定/确认合适的 `PathNavigation`（地面/飞行等，已支持枚举切换） <!-- id: npc-entity-navigation -->

- [x] **AI 集成与驱动** <!-- id: npc-entity-ai -->
    - [x] 绑定 `NpcMind`、`SensorManager`、`UtilityGoalSelector`、`ActionExecutor`（Attachment 时 initializeMind 注册默认 Goals/Sensors） <!-- id: npc-entity-mind-bind -->
    - [x] 实体 tick 驱动 `mind.tick`（全局事件），传感器默认注册（Vision/Damage/Safety） <!-- id: npc-entity-mind-tick -->
    - [x] 调整 `MoveToAction` 等动作参数（超时/重算/速度因子）确保流畅寻路 <!-- id: npc-entity-move-tuning -->
    - [x] 验证无原版 Goal 抢占：registerGoals 留空，仅自有目标/动作在跑 <!-- id: npc-entity-no-vanilla-preempt -->

- [ ] **生存能力** <!-- id: npc-entity-survival -->
    - [x] 基线属性与防御（含盾牌支持） <!-- id: npc-entity-base-stats -->
        - 基线模板集中于 `com.Kizunad.customNPCs.ai.config.NpcAttributeDefaults`（生命/攻速/护甲/移速/击退抗/游泳/重力/交互范围等全量默认值 + nametag/swim_speed 可选注册），供属性注册器直接复用。
        - 盾牌举盾基线：`NpcCombatDefaults.SHIELD_MIN_RAISE_TICKS` + `SHIELD_COOLDOWN_TICKS`，统一由 `BlockWithShieldAction`/`DefendGoal` 使用。
    - [ ] 进食/回血策略（可先被动回血，预留食物链路） <!-- id: npc-entity-heal -->
        - HealGoal 已接入，触发/健康阈值来自 `NpcCombatDefaults`（<50% 启动，≥80% 结束），优先级 = 1 - 当前血量；仅在有治疗物时运行。
        - 当前可用物品：即时治疗/再生药水、金苹果/附魔金苹果、带再生效果的食物；使用后会回滚手持状态，治疗记忆短期存活 200t。
        - 可选食物链路（未实现）：在无治疗物时，允许消耗高饥饿值食物补满饥饿再配合被动回血，或作为低优先级 fallback；需明确是否开启并写 GameTest。
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
    - [ ] Gamerule：`allowCustomNPCLoadChunk`（默认 true）控制是否允许自定义 NPC 通过票据维持当前区块加载 <!-- id: npc-entity-gamerule -->

- [ ] **测试与调试** <!-- id: npc-entity-test -->
    - [ ] GameTest：生成/存活/威胁响应/持久化/接口存在性 <!-- id: npc-entity-gametest -->
    - [ ] 调试命令：`inspect`/`spawn_test_entity` 适配新实体 <!-- id: npc-entity-debug-cmd -->
