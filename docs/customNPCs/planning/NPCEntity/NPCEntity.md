# 自定义 NPC 实体计划

## 目标与范围
- **核心目标**: 定义专属 `CustomNpcEntity`，完全使用自研 AI（NpcMind + Sensor + UtilityGoalSelector + ActionExecutor + GOAP），避免原版 Goal 干扰。
- **功能特性**:
    - 自主生存：饥饿、回血、受伤反应。
    - 智能行为：基于 GOAP 的任务执行（如收集、制作、战斗）。
    - 丰富交互：交易、送礼、对话、指令控制。
    - 动态性格：基于“七情六欲”的性格系统影响决策。
- **技术基石**: 保持原版级别的寻路流畅度，优先复用原版导航，必要时调优 MoveToAction/PathNavigation。

## 设计要点

### 1. 实体定义与注册
- **基类**: `CustomNpcEntity extends PathfinderMob`。
- **属性**: 注册 EntityType/Renderer/Attributes（生命、攻击、速度、护甲、防御）。
- **AI 托管**: 构造/初始化时清空 `goalSelector/targetSelector`，只运行 `NpcMind` 驱动的自有行动体系。
- **导航**: 指定正确的 `PathNavigation`（地面/飞行），确保与 `MoveToAction` 配合。

### 2. AI 集成架构
- **核心组件**:
    - `NpcMind`: 大脑核心，管理记忆、性格、状态。
    - `SensorManager`: 感知环境（Vision, Damage, Auditory）。
    - `UtilityGoalSelector`: 决策层，选择当前最优目标（Goal）。
    - `ActionExecutor`: 执行层，执行具体动作（Action）。
    - `GoapPlanner`: 规划层，将复杂目标拆解为动作序列。
- **驱动方式**: 实体 `tick()` 中驱动 `mind.tick()`。
- **冲突处理**: 移除原版 Goal 抢占，完全依赖 Utility 权重。

### 3. 交互系统 (Interaction)
详见 `interact_main_planning.md`。
- **Dashboard**: 状态概览、功能入口 (Trade, Gift, Chat, Owner)。
- **Dialogue**: 沉浸式对话、任务接取、指令下达。
- **网络同步**: S2C 数据包同步状态，C2S 数据包发送指令。

### 4. 任务与规划 (Task & GOAP)
详见 `task_goap_integration.md`。
- **理念**: 任务即动态目标状态。
- **流程**: Task -> 注入 Memory -> 生成 Desired WorldState -> GOAP 规划 -> Action 序列。
- **通用动作**: `GatherItemAction`, `InteractAction` 等支持上下文读取，而非硬编码。

### 5. 生存与状态
- **属性基线**: 血量、攻击力、移速、护甲（含盾牌防御）。
- **饥饿系统**: `NpcStatus` 管理 Hunger/Saturation，驱动进食行为。
- **威胁响应**:
    - 感知威胁 -> 触发中断 -> 重新评估。
    - 策略：战斗 (Melee/Ranged/Shield) 或 逃跑 (Flee)。

## 开发进度与顺序

### 已完成 (Phase 1 & 2)
- [x] **实体骨架**: `CustomNpcEntity` 注册与渲染。
- [x] **AI 核心**: NpcMind, UtilityGoalSelector, ActionExecutor, Sensors。
- [x] **GOAP 基础**: Planner, WorldState, 基础动作。
- [x] **生存基础**: 属性、伤害感知。
- [x] **交易基础**: 交易界面后端逻辑、网络包。

### 进行中 (Phase 3 - 交互与任务)
- [ ] **交互 UI**: Dashboard & Dialogue 前端实现 (`interact_menu.md`)。
- [ ] **功能集成**: Gift (背包交互), Owner Opts (指令)。
- [ ] **任务系统**: TaskManager, Task-GOAP 桥接 (`PerformTaskGoal`)。

### 待计划 (Phase 4 - 完善与扩展)
- [ ] **职业系统**: 基于 Task 的职业行为（农夫、卫兵）。
- [ ] **社交系统**: 好感度、关系网。
- [ ] **自然生成**: 配置化生成规则。

## 常用属性速查（Minecraft 1.21.x）
| Key (Namespaced) | 说明 |
| --- | --- |
| `generic.max_health` | 最大生命值 |
| `generic.follow_range` | 目标搜索距离 |
| `generic.knockback_resistance` | 击退抗性 |
| `generic.movement_speed` | 移动速度 |
| `generic.flying_speed` | 飞行速度 |
| `generic.attack_damage` | 近战攻击力 |
| `generic.attack_speed` | 攻击速度 |
| `generic.armor` | 护甲值 |
| `generic.armor_toughness` | 护甲韧性 |
| `generic.attack_knockback` | 攻击击退 |
| `generic.luck` | 幸运值 |
| `generic.jump_strength` | 跳跃力（常用于坐骑） |
| `generic.scale` | 实体缩放 |
| `generic.step_height` | 跨越台阶高度 |
| `generic.block_break_speed` | 方块破坏速度 |
| `generic.gravity` | 重力影响 |
| `generic.safe_fall_distance` | 安全落差 |
| `generic.fall_damage_multiplier` | 摔落伤害系数 |
| `horse.jump_strength` | 马系专用跳跃力 |
| `zombie.spawn_reinforcements` | 僵尸增援概率 |
| `player.block_interaction_range` | 玩家方块交互距离 |
| `player.entity_interaction_range` | 玩家实体交互距离 |