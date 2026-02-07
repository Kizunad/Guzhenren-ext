# Bastion 未完成功能开发 TODO 列表

> 生成时间：2026-02-05
> 最后更新：2026-02-07
> 基于代码搜索整理，按模块/依赖关系排序

---

## 已完成功能汇总

### ✅ 词缀系统
**提交**：`fa49deb`
| 词缀 | 效果 | 状态 |
|------|------|------|
| HARDENED | 守卫受伤时减伤 | ✅ |
| VOLATILE | 死亡时爆炸 | ✅ |
| CLOAKED | 生成时隐身 + 首击伤害加成 | ✅ |
| PROLIFERATING | 死亡时分裂生成新守卫 | ✅ |

- [x] 事件处理器 `BastionModifierEventHandler`
- [x] 生成时词缀调用集成到 `BastionHatcheryService`
- [x] 威胁事件词缀池扩展至全部 4 种

### ✅ 守卫行为（全部 6 种）
| 守卫类型 | 核心能力 | 状态 |
|----------|----------|------|
| BastionHealerGuardian | HealAlliesGoal、目标优先级、治疗粒子 | ✅ |
| BastionBufferGuardian | AuraBuffGoal、范围增益、粒子效果 | ✅ |
| BastionBerserkerGuardian | 狂暴状态、ChargeAttackGoal、粒子效果 | ✅ |
| BastionShieldGuardian | ShieldBlockGoal、投射物反弹、TauntGoal | ✅ |
| BastionArcherGuardian | 多重射击、瞄准预判、火焰箭、抛物线补偿 | ✅ |
| BastionCasterGuardian | 火球/凋零骷髅头、法术冷却、施法粒子 | ✅ |

### ✅ 系统默认启用状态
| 系统 | 默认值 | 实现状态 |
|------|--------|----------|
| 污染系统 | `true` | ✅ 完整实现 |
| Boss 系统 | `true` | ✅ 完整实现 |
| 接管系统 | `true` | ✅ 完整实现 |
| 炮台系统 | `true` | ✅ 完整实现 |
| 陷阱系统 | `true` | ✅ 完整实现 |
| Boss 阶段行为 | 已配置 | ✅ 75%/50%/25% 三阶段 |

---

## 优先级 1：玩法核心（直接影响游戏体验）

### 1.1 封印/占领物品正式化
**当前状态**：使用 Ender Pearl 和 Nether Star 作为临时测试物品
**位置**：`BastionInteractionService.java`
**预计耗时**：1-2 小时

- [ ] 创建 `BastionSealItem` 封印物品类
- [ ] 创建 `BastionCaptureItem` 占领物品类
- [ ] 在 `ModItems` 注册两个物品
- [ ] 添加物品材质和模型
- [ ] 添加合成配方
- [ ] 添加本地化文本

---

## 优先级 2：系统完善（需要新实现服务逻辑）

### 2.1 外壳系统
**当前状态**：`DEFAULT_SHELL_ENABLED = false`，配置已存在但服务未实现
**需要实现**：
- [ ] 创建 `BastionShellService` 服务类
- [ ] 实现外壳生成逻辑（菌毯边界保护层）
- [ ] 实现外壳破坏/修复机制
- [ ] 将默认值改为 `true`

### 2.2 精英怪系统
**当前状态**：`DEFAULT_ELITE_ENABLED = false`，配置已存在但服务未实现
**需要实现**：
- [ ] 创建 `BastionEliteService` 服务类
- [ ] 实现精英怪生成条件（转数/资源门槛）
- [ ] 实现精英怪属性加成（倍率应用）
- [ ] 实现精英怪掉落表
- [ ] 将默认值改为 `true`

### 2.3 能源损耗系统
**当前状态**：`DEFAULT_ENERGY_LOSS_ENABLED = false`
**位置**：`BastionEnergyService.java`
**需要实现**：
- [ ] 完善能源损耗计算逻辑
- [ ] 实现能源不足时的惩罚机制
- [ ] 将默认值改为 `true`

### 2.4 炮台/陷阱优化（可选）
**当前状态**：已启用，基础功能完整
- [ ] 完善炮台 `isHostileToBastion` 敌对判定（当前非玩家全敌对）
- [ ] 添加炮台瞄准/射击动画
- [ ] 添加多种陷阱类型

### 2.5 威胁事件扩展（可选）
**当前状态**：已扩展至全部 4 种词缀
- [ ] 添加更多威胁事件类型
- [ ] 平衡威胁事件触发概率

---

## 优先级 3：配置外置化（提升可配置性）

### 3.1 阵法配置外置
- [ ] 将净化阵法常量移至配置文件
- [ ] 将逆转阵法常量移至配置文件

### 3.2 天赋树数据驱动
**当前状态**：硬编码
- [ ] 设计天赋树 JSON 格式
- [ ] 实现天赋树数据加载器
- [ ] 迁移现有硬编码天赋到 JSON

### 3.3 道途扩展
**当前状态**：4 种道途
- [ ] 设计额外 4 种道途
- [ ] 实现新道途的技能/效果

---

## 优先级 4：性能优化

### 4.1 数据结构优化
- [ ] Anchor Frontier 数据结构扩展
- [ ] Chunk -> Bastion 索引建立
- [ ] 孵化巢冷却持久化

### 4.2 计算优化
- [ ] 能源服务预算化重建
- [ ] canSeeSky 检测缓存
- [ ] 孵化巢范围扫描挂载到 Anchor 缓存

---

## 优先级 5：代码清理

- [x] `BastionCaptureService.java` - Round 34 TODO 注释已清理
- [ ] `BastionPurificationArrayBlock.java:160-164` - 考虑简化污染禁用时的日志

---

## 快速开始建议

**按优先级推进：**

1. **封印/占领物品正式化**（1-2 小时）
   - 当前使用临时测试物品，影响玩家体验完整性

2. **外壳系统实现**（2-4 小时）
   - 增加基地防御层次感

3. **精英怪系统实现**（2-4 小时）
   - 增加战斗挑战性

---

## 验收标准

每个功能完成后需要：
1. `./gradlew compileJava --quiet` 通过
2. `./gradlew checkstyleMain --quiet` 通过
3. 游戏内功能测试通过
4. 相关 GameTest 编写/更新（如适用）
