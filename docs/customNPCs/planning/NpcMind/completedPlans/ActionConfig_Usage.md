# ActionConfig 使用指南

## 📖 概述

`ActionConfig` 是动作层标准化配置系统的核心类，采用单例模式，提供所有动作的默认参数管理。

## 🔧 基本用法

### 获取配置实例

```java
ActionConfig config = ActionConfig.getInstance();
```

### 读取配置值

```java
// 攻击动作配置
double attackRange = config.getAttackRange();  // 3.0 blocks
int cooldown = config.getAttackCooldownTicks(); // 20 ticks

// 交互动作配置
double interactRange = config.getInteractRange(); // 4.0 blocks

// 导航配置
int navTimeout = config.getNavTimeout(); // 300 ticks
```

### 运行时调整配置

```java
// 链式调用
ActionConfig.getInstance()
    .setAttackRange(5.0)
    .setNavTimeout(600)
    .setDebugLoggingEnabled(true);
```

### 重置为默认值

```java
ActionConfig.getInstance().resetToDefaults();
```

---

## 🎮 调试模式

### 启用调试日志

```java
ActionConfig.getInstance().setDebugLoggingEnabled(true);
```

启用后，动作将输出详细的调试日志（每20 ticks）：
- 动作当前状态
- 执行tick数
- 距离检测结果
- 失败原因等

### 禁用调试日志

```java
ActionConfig.getInstance().setDebugLoggingEnabled(false);
```

---

## 📊 配置参数列表

### 通用配置

| 参数 | 默认值 | 单位 | 说明 |
|------|--------|------|------|
| `defaultTimeoutTicks` | 300 | ticks | 动作默认超时时长（15秒） |
| `defaultMaxRetries` | 3 | 次 | 默认最大重试次数 |
| `defaultNavRange` | 10.0 | blocks | 默认导航范围 |
| `logIntervalTicks` | 20 | ticks | 日志输出间隔（1秒） |

### 攻击动作配置

| 参数 | 默认值 | 单位 | 说明 |
|------|--------|------|------|
| `attackRange` | 3.0 | blocks | 攻击距离 |
| `attackCooldownTicks` | 20 | ticks | 攻击冷却时长（1秒） |
| `maxAttackAttemptTicks` | 60 | ticks | 最大攻击尝试时长（3秒） |

### 使用物品动作配置

| 参数 | 默认值 | 单位 | 说明 |
|------|--------|------|------|
| `defaultItemUseTicks` | 32 | ticks | 默认物品使用时长 |
| `bowMaxUseTicks` | 72000 | ticks | 弓的最大蓄力时长 |
| `timeoutBufferTicks` | 20 | ticks | 超时缓冲 |

### 方块交互配置

| 参数 | 默认值 | 单位 | 说明 |
|------|--------|------|------|
| `interactRange` | 4.0 | blocks | 交互距离 |
| `interactTimeoutTicks` | 200 | ticks | 交互超时时长（10秒） |

### 导航配置

| 参数 | 默认值 | 单位 | 说明 |
|------|--------|------|------|
| `pathUpdateInterval` | 10 | ticks | 路径更新间隔 |
| `navTimeout` | 300 | ticks | 导航超时（15秒） |
| `renavInterval` | 10 | ticks | 重新导航间隔 |
| `maxNavRetries` | 5 | 次 | 最大导航重试次数 |

---

## 💡 使用示例

### 示例1：调整战斗参数

```java
// 创建更激进的战斗配置
ActionConfig.getInstance()
    .setAttackRange(5.0)           // 增加攻击距离到5格
    .setAttackCooldownTicks(10);   // 减少冷却到0.5秒
```

### 示例2：调试模式

```java
// 启用调试模式，查看详细日志
ActionConfig.getInstance()
    .setDebugLoggingEnabled(true);

// 创建攻击动作，将输出详细日志
AttackAction attack = new AttackAction(targetUuid);
```

### 示例3：性能优化

```java
// 减少路径更新频率以提升性能
ActionConfig.getInstance()
    .setPathUpdateInterval(20)     // 每20 ticks更新一次路径
    .setLogIntervalTicks(40);      // 减少日志输出频率
```

### 示例4：测试环境配置

```java
// 测试环境：更短的超时和更详细的日志
ActionConfig.getInstance()
    .setNavTimeout(100)            // 减少超时到5秒
    .setDebugLoggingEnabled(true)  // 启用调试日志
    .setMaxNavRetries(2);          // 减少重试次数
```

---

## 🔄 配置生命周期

### 配置持久化

目前配置仅存在于内存中，重启后会重置为默认值。

**未来扩展**：
- 支持从外部配置文件（JSON/TOML）加载
- 支持保存配置到文件
- 支持热重载

### 配置优先级

1. **代码中直接设置** > **ActionConfig** > **硬编码默认值**
2. 动作构造函数的显式参数优先于配置值

```java
// 显式参数优先
AttackAction attack = new AttackAction(
    targetUuid,
    10.0,  // 使用10.0而非配置中的3.0
    30,
    120
);
```

---

## ⚠️ 注意事项

1. **线程安全**：`ActionConfig` 是单例，所有修改都是全局的
2. **配置生效时机**：新创建的动作会立即使用新配置，已存在的动作使用创建时的配置
3. **调试日志性能**：启用调试日志会增加日志输出，可能影响性能
4. **配置验证**：目前没有参数验证，请确保设置合理的值

---

## 🚀 未来扩展计划

### 阶段1：配置文件支持（已完成）
- ✅ 创建 `ActionConfig` 类
- ✅ 集成到所有动作类
- ✅ 支持运行时调整

### 阶段2：外部配置文件（计划中）
- [ ] 支持从 `config/npc_actions.toml` 加载
- [ ] 热重载配置
- [ ] 配置验证和默认值回退

### 阶段3：GUI配置界面（未来）
- [ ] 游戏内配置界面
- [ ] 预设配置模板（激进/保守/平衡）
- [ ] 配置导入/导出

---

## 📝 最佳实践

1. **生产环境**：禁用调试日志，使用默认配置
2. **开发环境**：启用调试日志，适当减少超时时长
3. **性能测试**：调整路径更新间隔和日志间隔
4. **特殊NPC**：为特定NPC创建自定义配置快照
