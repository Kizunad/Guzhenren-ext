# Bastion 未完成功能开发 TODO 列表

> 生成时间：2026-02-05
> 最后更新：2026-02-07
> 基于代码搜索整理，按模块/依赖关系排序

---

## 已完成功能（2026-02-07 会话）

### ✅ 词缀系统集成
**状态**：已完成并提交 (fa49deb)
- [x] `HARDENED` - 守卫受伤时减伤
- [x] `VOLATILE` - 死亡时爆炸
- [x] `CLOAKED` - 生成时隐身 + 首击伤害加成
- [x] `PROLIFERATING` - 死亡时分裂生成新守卫
- [x] 事件处理器 `BastionModifierEventHandler` 已创建
- [x] 生成时词缀调用已集成到 `BastionHatcheryService`

### ✅ 守卫行为（已发现完整实现）
- [x] **BastionHealerGuardian** - HealAlliesGoal、目标优先级、治疗粒子
- [x] **BastionBufferGuardian** - AuraBuffGoal、范围增益、粒子效果
- [x] **BastionBerserkerGuardian** - 狂暴状态、ChargeAttackGoal、粒子效果
- [x] **BastionShieldGuardian** - ShieldBlockGoal、投射物反弹、TauntGoal

### ✅ 系统默认启用状态
| 系统 | 默认值 | 状态 |
|------|--------|------|
| 污染系统 | `true` | ✅ 已启用 |
| Boss 系统 | `true` | ✅ 已启用 |
| 接管系统 | `true` | ✅ 已启用 |
| Boss 阶段行为 | 已实现 | ✅ 已启用（含默认阶段配置） |

### ✅ Boss 阶段行为
**状态**：已完整实现
- [x] 阶段切换机制（`tryHandlePhaseSwitch`）
- [x] 血量阈值触发（`resolveTargetPhaseIndex`）
- [x] 阶段属性变化（`applyPhaseAttributes`）
- [x] 视觉/音效提示（`spawnPhaseEffects`）
- [x] 默认阶段配置（75%/50%/25% 三阶段）

---

## 优先级 1：玩法核心（直接影响游戏体验）

### 1.1 封印/占领物品正式化
**当前状态**：使用 Ender Pearl 和 Nether Star 作为临时测试物品
**位置**：`BastionInteractionService.java:457-458, 541-542`

- [ ] 创建 `BastionSealItem` 封印物品类
- [ ] 创建 `BastionCaptureItem` 占领物品类
- [ ] 在 `ModItems` 注册两个物品
- [ ] 添加物品材质和模型（`assets/guzhenrenext/textures/item/`）
- [ ] 添加合成配方（`data/guzhenrenext/recipe/`）
- [ ] 添加本地化文本（`zh_cn.json`, `en_us.json`）
- [ ] 更新 `BastionInteractionService` 使用正式物品

### 1.2 守卫行为补全（剩余）

#### 1.2.3 BastionArcherGuardian（弓手）
- [ ] 实现多重射击
- [ ] 实现瞄准预判
- [ ] 添加特殊箭矢类型

#### 1.2.4 BastionCasterGuardian（法师）
- [ ] 实现法术攻击（与道途关联）
- [ ] 实现法术冷却管理
- [ ] 添加施法动画和粒子效果

---

## 优先级 2：系统完善（功能已有框架，需要启用/完善）

### 2.1 炮台系统启用
**当前状态**：`DEFAULT_TURRET_ENABLED = true`（已启用）
**位置**：`BastionTypeConfig.java`, `BastionTurretService.java`

- [x] 炮台攻击逻辑已实现
- [x] 将默认值改为 `true`
- [ ] 完善 `isHostileToBastion` 敌对判定（当前非玩家全敌对）
- [ ] 添加炮台瞄准/射击动画（可选）

### 2.2 陷阱系统启用
**当前状态**：`DEFAULT_TRAP_ENABLED = true`（已启用）
**位置**：`BastionTypeConfig.java`, `BastionTrapBlock.java`

- [x] 陷阱触发逻辑已实现
- [x] `isHostile` 敌对判定已实现
- [x] 将默认值改为 `true`
- [ ] 添加多种陷阱类型（可选）

### 2.3 外壳系统启用
**当前状态**：`DEFAULT_SHELL_ENABLED = false`
**位置**：`BastionTypeConfig.java`

- [ ] 完善外壳生成逻辑
- [ ] 完善外壳破坏/修复机制
- [ ] 测试验证后将默认值改为 `true`

### 2.4 精英怪系统启用
**当前状态**：`DEFAULT_ELITE_ENABLED = false`
**位置**：`BastionTypeConfig.java`

- [ ] 完善精英怪生成条件
- [ ] 完善精英怪属性加成
- [ ] 完善精英怪掉落表
- [ ] 测试验证后将默认值改为 `true`

### 2.5 能源损耗系统启用
**当前状态**：`DEFAULT_ENERGY_LOSS_ENABLED = false`
**位置**：`BastionTypeConfig.java`, `BastionEnergyService.java`

- [ ] 完善能源损耗计算
- [ ] 完善能源不足时的惩罚机制
- [ ] 测试验证后将默认值改为 `true`

### 2.6 威胁事件扩展
**当前状态**：已扩展至全部 4 种词缀
**位置**：`ThreatEventService.java:445-449`

- [x] 启用 CLOAKED 威胁事件
- [x] 启用 PROLIFERATING 威胁事件
- [ ] 添加更多威胁事件类型
- [ ] 平衡威胁事件触发概率

---

## 优先级 3：配置外置化（提升可配置性）

### 3.1 阵法配置外置
**位置**：`BastionPurificationArrayBlock.java:41`, `BastionReversalArrayBlock.java:52`

- [ ] 将净化阵法常量移至配置文件
- [ ] 将逆转阵法常量移至配置文件
- [ ] 添加配置热重载支持

### 3.2 天赋树数据驱动
**当前状态**：硬编码，后续可替换为数据驱动
**位置**：`BastionTalentRegistry.java:10-12`

- [ ] 设计天赋树 JSON 格式
- [ ] 实现天赋树数据加载器
- [ ] 迁移现有硬编码天赋到 JSON
- [ ] 支持数据包覆盖天赋树

### 3.3 破局配置多套支持
**当前状态**：只读 default.json
**位置**：`BastionBreakingDataLoader.java:40`

- [ ] 支持加载多个破局配置文件
- [ ] 支持按基地类型选择配置
- [ ] 添加配置合并逻辑

### 3.4 道途扩展
**当前状态**：MVP 4 种道途，预留扩展到 8 种
**位置**：`BastionDao.java`

- [ ] 设计额外 4 种道途
- [ ] 实现新道途的技能/效果
- [ ] 添加新道途的视觉标识

---

## 优先级 4：性能优化（不影响功能，提升效率）

### 4.1 Anchor Frontier 优化
**位置**：`BastionSavedData.java:119`

- [ ] 扩展 anchorFrontier 数据结构
- [ ] 优化边界计算算法

### 4.2 孵化巢冷却持久化
**位置**：`BastionSavedData.java:133`

- [ ] 将孵化巢冷却迁移到持久化存储
- [ ] 避免服务器重启丢失冷却状态

### 4.3 基地查询 Chunk 索引
**当前状态**：全量遍历查找基地
**位置**：`BastionSavedData.java:354`

- [ ] 建立 Chunk -> Bastion 索引
- [ ] 优化 `getBastionAt` 等查询方法
- [ ] 索引持久化和同步

### 4.4 能源服务优化
**位置**：`BastionEnergyService.java:42-47`

- [ ] 预算化重建流程
- [ ] 优化 canSeeSky 检测（缓存/批量）

### 4.5 孵化巢范围扫描优化
**位置**：`BastionHatcheryService.java:54-55`

- [ ] 将范围扫描挂载到 Anchor 缓存
- [ ] 减少每 tick 扫描开销

---

## 优先级 5：代码清理

### 5.1 清理遗留注释
- [ ] `BastionCaptureService.java:198-201` - 清理 Round 34 TODO 注释
- [ ] `BastionReversalArrayBlockEntity.java:159` - 确定 tag 命名规则（jiage_XXX）

### 5.2 简化测试代码移除
- [ ] `BastionPurificationArrayBlock.java:160-164` - 移除污染禁用时的简化逻辑（启用污染系统后）

---

## 依赖关系图

```
优先级 1.1（封印/占领物品）
    ↓
优先级 1.2（守卫行为）← 依赖守卫实体基础
    ↓
优先级 1.3（词缀系统）← 词缀影响守卫/怪物
    ↓
优先级 1.4（Boss 阶段）← Boss 可能使用词缀

优先级 2.1-2.6（系统启用）← 相互独立，可并行
    ↓
优先级 2.7（威胁事件扩展）← 依赖词缀系统完善

优先级 3（配置外置）← 独立于功能开发

优先级 4（性能优化）← 功能稳定后进行

优先级 5（代码清理）← 最后进行
```

---

## 快速开始建议

**如果只有有限时间，按以下顺序推进：**

1. **1.1 封印/占领物品正式化**（1-2 小时）
   - 影响：让玩家体验完整的占领流程
   
2. **1.3 词缀系统实现**（2-4 小时）
   - 影响：增加战斗多样性
   
3. **1.2.5 治疗者行为 + 1.2.6 增益者行为**（2-3 小时）
   - 影响：让守卫团队有配合感

4. **2.1 污染系统启用**（1-2 小时）
   - 影响：增加占领前的挑战

---

## 验收标准

每个功能完成后需要：
1. `./gradlew compileJava --quiet` 通过
2. `./gradlew checkstyleMain --quiet` 通过
3. 游戏内功能测试通过
4. 相关 GameTest 编写/更新（如适用）
