# Bastion 未完成功能开发 TODO 列表

> 生成时间：2026-02-05
> 基于代码搜索整理，按模块/依赖关系排序

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

### 1.2 守卫行为补全
**当前状态**：6 种守卫实体均为 Round 11-16 最小实现，只有基础 AI
**位置**：`bastion/guardian/entity/Bastion*Guardian.java`

#### 1.2.1 BastionShieldGuardian（盾卫）
- [ ] 实现格挡行为（减伤/反弹）
- [ ] 实现嘲讽技能（吸引敌人仇恨）
- [ ] 添加盾牌举起/放下动画状态

#### 1.2.2 BastionBerserkerGuardian（狂战士）
- [ ] 实现狂暴状态（低血量增伤）
- [ ] 实现冲锋攻击
- [ ] 添加狂暴视觉效果

#### 1.2.3 BastionArcherGuardian（弓手）
- [ ] 实现多重射击
- [ ] 实现瞄准预判
- [ ] 添加特殊箭矢类型

#### 1.2.4 BastionCasterGuardian（法师）
- [ ] 实现法术攻击（与道途关联）
- [ ] 实现法术冷却管理
- [ ] 添加施法动画和粒子效果

#### 1.2.5 BastionHealerGuardian（治疗者）
- [ ] 实现治疗光环/单体治疗
- [ ] 实现治疗目标优先级（血量最低的友军）
- [ ] 添加治疗视觉效果

#### 1.2.6 BastionBufferGuardian（增益者）
- [ ] 实现增益光环（攻击/防御/速度）
- [ ] 实现增益目标选择逻辑
- [ ] 添加增益视觉效果

### 1.3 词缀/变异系统实现
**当前状态**：4 种词缀效果均为"后续实现"占位
**位置**：`BastionModifier.java`

- [ ] `HARDENED` - 实现硬化效果（增加防御/减伤）
- [ ] `VOLATILE` - 实现易爆效果（死亡爆炸/范围伤害）
- [ ] `CLOAKED` - 实现隐匿效果（隐身/突袭）
- [ ] `PROLIFERATING` - 实现增殖效果（分裂/召唤）
- [ ] 为每种词缀添加视觉标识（粒子/光效）

### 1.4 Boss 阶段行为实现
**当前状态**：`BossPhase` 配置存在但默认为空列表
**位置**：`BastionTypeConfig.java:750-751`, `BastionBossService.java`

- [ ] 设计 Boss 阶段切换机制（血量阈值触发）
- [ ] 实现阶段切换时的技能变化
- [ ] 实现阶段切换时的属性变化
- [ ] 添加阶段切换视觉/音效提示
- [ ] 配置默认 Boss 阶段列表

---

## 优先级 2：系统完善（功能已有框架，需要启用/完善）

### 2.1 污染系统启用
**当前状态**：`DEFAULT_POLLUTION_ENABLED = false`
**位置**：`BastionTypeConfig.java`, `BastionPurificationArrayBlock.java`

- [ ] 完善污染扩散逻辑
- [ ] 完善污染净化流程
- [ ] 添加污染视觉效果（方块变色/粒子）
- [ ] 测试验证后将默认值改为 `true`

### 2.2 外壳系统启用
**当前状态**：`DEFAULT_SHELL_ENABLED = false`
**位置**：`BastionTypeConfig.java`

- [ ] 完善外壳生成逻辑
- [ ] 完善外壳破坏/修复机制
- [ ] 测试验证后将默认值改为 `true`

### 2.3 精英怪系统启用
**当前状态**：`DEFAULT_ELITE_ENABLED = false`
**位置**：`BastionTypeConfig.java`

- [ ] 完善精英怪生成条件
- [ ] 完善精英怪属性加成
- [ ] 完善精英怪掉落表
- [ ] 测试验证后将默认值改为 `true`

### 2.4 炮台系统启用
**当前状态**：`DEFAULT_TURRET_ENABLED = false`
**位置**：`BastionTypeConfig.java`, `BastionTurretService.java`

- [ ] 完善炮台攻击逻辑
- [ ] 完善 `isHostileToBastion` 敌对判定（当前非玩家全敌对）
- [ ] 添加炮台瞄准/射击动画
- [ ] 测试验证后将默认值改为 `true`

### 2.5 陷阱系统启用
**当前状态**：`DEFAULT_TRAP_ENABLED = false`
**位置**：`BastionTypeConfig.java`, `BastionTrapBlock.java`

- [ ] 完善陷阱触发逻辑
- [ ] 完善 `isHostile` 敌对判定
- [ ] 添加多种陷阱类型
- [ ] 测试验证后将默认值改为 `true`

### 2.6 能源损耗系统启用
**当前状态**：`DEFAULT_ENERGY_LOSS_ENABLED = false`
**位置**：`BastionTypeConfig.java`, `BastionEnergyService.java`

- [ ] 完善能源损耗计算
- [ ] 完善能源不足时的惩罚机制
- [ ] 测试验证后将默认值改为 `true`

### 2.7 威胁事件扩展
**当前状态**：MVP 只在 HARDENED/VOLATILE 里选
**位置**：`ThreatEventService.java:445-449`

- [ ] 启用 CLOAKED 威胁事件
- [ ] 启用 PROLIFERATING 威胁事件
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
