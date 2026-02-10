# 地灵意志系统执行报告

## 执行概览
- **总任务数**: 52
- **完成状态**: 100% 完成
- **核心功能**:
  - 狐仙本体与投影实体框架
  - 9格跨维度虚拟行囊 (TinyUI)
  - 伤害分摊与几何护盾
  - 实体捕获与精魄炼化
  - 跨维度跟随与安全瞬移

## 关键交付物

### 数据模型
- `SpiritData`: 记录好感度、表情与行囊物品。
- `SealedSpiritData`: DataComponent，存储被封印实体的快照数据。
- `BastionSavedData`: 扩展了对 `SpiritData` 的持久化支持。

### 实体系统
- `LandSpiritEntity`: 基地核心守护者，负责交互与管理。
- `LandSpiritProjectionEntity`: 玩家随从，支持：
  - 跨维度跟随
  - 自动拾取物品
  - 伤害分摊 (50%)
  - 警戒与护盾渲染
  - 夜视光环

### 交互与 UI
- `SpiritManagementMenu` & `Screen`: 基于 TinyUI 的管理界面。
- `ApertureRefiningStation`: 将精魄转化为资源的自动化设施。

### 渲染技术
- `LandSpiritRenderer`: 使用 FoxModel。
- `LandSpiritProjectionRenderer`: 
  - 几何护盾 (Shader/VertexConsumer)
  - 贝塞尔曲线锁链
  - 黑洞特效

### 核心服务
- `SpiritEntityService`: 管理实体生命周期。
- `SpiritGuardLogic`: 处理伤害重定向。
- `SpiritSaturationService`: 计算环境灵气与投影光环。
- `ApertureBlinkService`: 实现安全的跨维度归还。
- `CrossDimResourceService`: 处理跨维度资源扣除。

## 验证指南

### 调试命令
- `/guzhenren_debug spirit simulate_kill`: 模拟投影死亡与重构。
- `/guzhenren_debug spirit force_capture <target>`: 强制捕获目标。
- `/guzhenren_debug spirit inject_spirit_data`: 为手中物品注入测试数据。

### 手动测试流程
1. **生成**: 确保基地 Active，核心上方应出现狐仙。
2. **交互**: 右键狐仙打开 GUI，测试物品存取。
3. **投影**: 离开基地范围或通过命令召唤投影。
4. **战斗**: 让怪物攻击玩家，观察投影是否举盾并扣除资源。
5. **捕获**: 将怪物打至残血 (<20%)，观察是否有锁链和捕获行为。
6. **跨维度**: 进入下界，确认投影跟随；触发 Blink，确认安全返回核心。

## 遗留事项 / 后续建议
- **动画优化**: 当前狐仙动画基于原版 Fox，建议后续增加更丰富的自定义动画（如尾巴摆动频率随心情变化）。
- **GameTest**: 建议编写自动化 GameTest 用例以覆盖边界情况（如虚空伤害、多重扣费）。
