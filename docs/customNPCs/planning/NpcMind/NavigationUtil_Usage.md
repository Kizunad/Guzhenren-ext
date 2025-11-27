# NavigationUtil 使用指南

## 📖 概述

`NavigationUtil` 是导航工具类，统一管理所有导航相关的逻辑，包括到达判定、粘性导航、寻路限流和卡住检测。

---

## 🔧 核心功能

### 1. 统一的到达判定

#### `isInRange()` - 简单距离检查
```java
Vec3 entityPos = mob.position();
Vec3 targetPos = target.position();
boolean inRange = NavigationUtil.isInRange(entityPos, targetPos, 3.0);
```

#### `hasArrived()` - 带缓冲的到达判定
```java
// 使用 ARRIVAL_BUFFER (0.75) 避免寻路边缘误判
boolean arrived = NavigationUtil.hasArrived(
    currentPos,
    targetPos,
    acceptableDistance
);
```

**区别**:
- `isInRange`: 严格距离检查（用于攻击距离等）
- `hasArrived`: 宽松到达判定（用于导航完成）

---

### 2. 粘性导航策略

对于移动中的目标（如敌对实体），使用更高的路径更新频率：

```java
// 粘性导航到实体（自动使用更短的更新间隔）
int newCooldown = NavigationUtil.stickyNavigateToEntity(
    mob,
    targetEntity,
    speed,
    pathUpdateCooldown
);

// 更新冷却值
pathUpdateCooldown = newCooldown;
```

**优势**:
- 移动目标: 更新间隔减半（例如 10 ticks → 5 ticks）
- 固定目标: 使用配置的标准间隔
- 自动优化性能

---

### 3. 寻路限流

防止每tick都重新计算路径，优化性能：

```java
// 导航到固定位置（带限流）
int newCooldown = NavigationUtil.navigateToPosition(
    mob,
    targetPos,
    speed,
    pathUpdateCooldown
);
```

**工作原理**:
- 冷却中（cooldown > 0）: 跳过路径更新
- 冷却结束: 执行路径更新并重置冷却
- 冷却间隔从 `ActionConfig` 获取

---

### 4. 卡住检测

检测NPC是否长时间未移动：

```java
boolean stuck = NavigationUtil.isStuck(
    currentPos,
    lastPos,
    stuckTicks,
    maxStuckTicks
);

if (stuck) {
    // 处理卡住情况（例如传送或失败）
}
```

---

## 📊 配置参数

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `ARRIVAL_BUFFER` | 0.75 | 到达判定缓冲距离 |
| `MIN_MOVEMENT` | 0.1 | 最小移动距离（卡住检测） |
| `pathUpdateInterval` | 10 ticks | 路径更新间隔（可配置） |
| 粘性导航间隔 | interval / 2 | 移动目标的更新间隔 |

---

## 💡 使用示例

### 示例1：优化AttackAction的导航

```java
public class AttackAction extends AbstractStandardAction {
    private int pathUpdateCooldown = 0;
    
    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        // 获取目标实体
        LivingEntity target = (LivingEntity) resolveEntity(mob.level());
        
        // 检查距离
        if (!NavigationUtil.isInRange(mob.position(), target.position(), attackRange)) {
            // 使用粘性导航追踪移动目标
            pathUpdateCooldown = NavigationUtil.stickyNavigateToEntity(
                mob,
                target,
                1.0,
                pathUpdateCooldown
            );
            return ActionStatus.RUNNING;
        }
        
        // 在范围内，执行攻击
        performAttack(mob, target);
        return ActionStatus.SUCCESS;
    }
}
```

### 示例2：InteractBlockAction的固定位置导航

```java
public class InteractBlockAction extends AbstractStandardAction {
    private int pathUpdateCooldown = 0;
    
    @Override
    protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
        Vec3 blockCenter = Vec3.atCenterOf(blockPos);
        
        // 检查是否到达
        if (NavigationUtil.hasArrived(mob.position(), blockCenter, interactRange)) {
            // 执行交互
            return performInteraction(mob);
        }
        
        // 导航到方块（固定位置）
        pathUpdateCooldown = NavigationUtil.navigateToPosition(
            mob,
            blockCenter,
            1.0,
            pathUpdateCooldown
        );
        return ActionStatus.RUNNING;
    }
}
```

### 示例3：卡住检测和处理

```java
private Vec3 lastPosition = null;
private int stuckTicks = 0;
private static final int MAX_STUCK_TICKS = 40;

@Override
protected ActionStatus tickInternal(INpcMind mind, Mob mob) {
    Vec3 currentPos = mob.position();
    
    // 卡住检测
    if (NavigationUtil.isStuck(currentPos, lastPosition, stuckTicks, MAX_STUCK_TICKS)) {
        LOGGER.warn("[动作] NPC卡住，停止执行");
        return ActionStatus.FAILURE;
    }
    
    // 更新卡住计数
    if (lastPosition != null) {
        double movement = NavigationUtil.distance(currentPos, lastPosition);
        stuckTicks = (movement < 0.1) ? stuckTicks + 1 : 0;
    }
    lastPosition = currentPos;
    
    // ... 正常逻辑
}
```

---

## 🔄 集成到AbstractStandardAction

`AbstractStandardAction` 已经集成了 `NavigationUtil`：

```java
protected boolean isInRange(Vec3 entityPos, Vec3 targetPos, double threshold) {
    return NavigationUtil.isInRange(entityPos, targetPos, threshold);
}
```

所有继承 `AbstractStandardAction` 的类都可以直接使用 `isInRange()` 方法。

---

## ⚙️ 性能优化建议

### 1. 合理设置更新间隔
```java
// 高性能场景：降低更新频率
ActionConfig.getInstance().setPathUpdateInterval(20); // 每20 ticks

// 高精度场景：提高更新频率
ActionConfig.getInstance().setPathUpdateInterval(5);  // 每5 ticks
```

### 2. 区分固定和移动目标
- 固定目标（方块、坐标）: 使用 `navigateToPosition()`
- 移动目标（实体）: 使用 `stickyNavigateToEntity()`

### 3. 调试模式
```java
// 启用调试日志查看路径更新详情
ActionConfig.getInstance().setDebugLoggingEnabled(true);
```

---

## 🚀 未来扩展

### 计划中的功能
- [ ] 视线检查（射线追踪）
- [ ] 动态避障
- [ ] 路径平滑优化
- [ ] A*路径优化提示

---

## 📝 最佳实践

1. **总是使用NavigationUtil**: 避免直接调用 `PathNavigation.moveTo()`
2. **选择正确的方法**: 固定目标用 `navigateToPosition`，移动目标用 `stickyNavigateToEntity`
3. **保存冷却状态**: 将 `pathUpdateCooldown` 作为动作的成员变量
4. **合理的超时**: 配合 `AbstractStandardAction` 的超时机制
5. **调试优先**: 遇到导航问题时启用调试日志
