# NPC 测试最佳实践

本文档提供编写NPC GameTest的最佳实践指南，基于重构后的测试框架。

## 核心工具类

### NpcTestHelper

位于 `com.Kizunad.customNPCs_test.utils.NpcTestHelper`，提供标准化测试辅助方法：

#### 1. 创建带NpcMind的NPC

```java
Zombie zombie = NpcTestHelper.spawnNPCWithMind(
    helper, 
    new BlockPos(2, 2, 2),
    EntityType.ZOMBIE
);
```

#### 2. 等待异步条件（推荐）

```java
NpcTestHelper.waitForCondition(
    helper,
    () -> {
        // 返回 true 表示条件满足
        return zombie.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.STICK;
    },
    100,  // 超时tick数
    "Zombie未持有木棍"  // 失败消息
);
```

#### 3. 驱动NPC Mind

```java
// 在测试开始时调用，确保NpcMind每tick都被更新
NpcTestHelper.tickMind(helper, zombie);
```

#### 4. 断言辅助方法

```java
// 断言当前目标类型
NpcTestHelper.assertGoal(helper, zombie, IdleGoal.class);

// 断言NPC位置
NpcTestHelper.assertNPCPosition(helper, zombie, new Vec3(5, 2, 5), 1.0);

// 断言手持物品
NpcTestHelper.assertItemInHand(helper, zombie, Items.STICK, 1);
```

### TestEntityFactory

位于 `com.Kizunad.customNPCs_test.utils.TestEntityFactory`，提供预配置的测试实体：

```java
// 创建标准测试Zombie（自动附加NpcMind和VisionSensor）
Zombie zombie = TestEntityFactory.createTestZombie(helper, new BlockPos(2, 2, 2));

// 创建自定义NPC（可选择是否添加Vision传感器）
Zombie zombie = TestEntityFactory.createTestNPC(
    helper,
    new BlockPos(2, 2, 2),
    EntityType.ZOMBIE,
    true  // 添加VisionSensor
);
```

---

## 编写测试的正确模式

### ❌ 错误模式：多个成功条件

**问题**：GameTest框架不允许同时使用多个final子句（succeedWhen/succeedOnTickWhen）。

```java
@GameTest(template = "empty")
public void badTest(GameTestHelper helper) {
    // ... 测试逻辑
    
    helper.succeedWhen(() -> {
        // 验证逻辑
    });
    
    // ❌ 错误！这会导致 "This test already has final clause" 错误
    helper.succeedOnTickWhen(100, () -> {
        // 超时检查
    });
}
```

### ✅ 正确模式1：使用 NpcTestHelper.waitForCondition

**推荐用于异步行为测试**。

```java
@GameTest(template = "empty", timeoutTicks = 200)
public static void goodTest(GameTestHelper helper) {
    Zombie zombie = TestEntityFactory.createTestZombie(helper, new BlockPos(2, 2, 2));
    
    // 配置目标和行为
    INpcMind mind = NpcTestHelper.getMind(helper, zombie);
    mind.getGoalSelector().registerGoal(new SomeGoal());
    
    // 启动Mind tick
    NpcTestHelper.tickMind(helper, zombie);
    
    // 等待条件（自动处理超时）
    NpcTestHelper.waitForCondition(
        helper,
        () -> {
            // 检查所有成功条件
            return checkAllConditions();
        },
        150,
        "测试条件未满足"
    );
}
```

### ✅ 正确模式2：手动tick计数（性能测试）

**推荐用于需要精确tick计数的场景**。

```java
@GameTest(template = "empty", timeoutTicks = 100)
public static void performanceTest(GameTestHelper helper) {
    final int[] tickCount = {0};
    final boolean[] completed = {false};
    final int targetTicks = 50;
    
    helper.onEachTick(() -> {
        if (!completed[0] && tickCount[0] < targetTicks) {
            // 执行测试逻辑
            doSomething();
            tickCount[0]++;
            
            // 当达到目标tick时
            if (tickCount[0] >= targetTicks) {
                completed[0] = true;
                // 验证结果
                verifyResults();
                // 手动调用成功
                helper.succeed();
            }
        }
    });
}
```

---

## 超时设置

使用 `@GameTest` 注解的 `timeoutTicks` 参数设置测试总超时：

```java
@GameTest(
    template = "empty",
    timeoutTicks = 300  // 15秒超时（1秒=20 ticks）
)
public static void longRunningTest(GameTestHelper helper) {
    // ...
}
```

**推荐超时时间**：
- 简单测试：100-200 ticks (5-10秒)
- 移动/路径查找：200-300 ticks (10-15秒)
- 复杂场景：400-600 ticks (20-30秒)

## 批次与隔离

- **按标签过滤**：使用 `com.Kizunad.customNPCs_test.utils.TestBatches` 中的常量作为 `batch`，便于通过 `-Dforge.gametest.filter=<batch>` 运行子集，例如 `guzhenrenext_real_api`。
- **命名空间**：统一使用 `@GameTestHolder("guzhenrenext")`，模板统一指定 `template = "empty"`，避免结构文件冲突。
- **跨测试隔离**：`NpcTestHelper.applyTestTag` 自动为每个测试生成唯一标签，VisionSensor 只感知同标签实体，减少并行用例干扰。
- **地形初始化**：`NpcTestHelper.ensureFloorAround` 会在生成点周围铺设地板，避免掉落或寻路失败。

---

## 常见陷阱

### 1. 忘记驱动NpcMind

```java
// ❌ 错误：NpcMind不会自动tick
Zombie zombie = TestEntityFactory.createTestZombie(helper, pos);
// ... NpcMind永远不会执行

// ✅ 正确：显式启动Mind tick
NpcTestHelper.tickMind(helper, zombie);
```

### 2. 验证条件中使用 helper.fail()

```java
// ❌ 错误：在waitForCondition的lambda中不要使用helper.fail()
NpcTestHelper.waitForCondition(helper, () -> {
    if (condition) {
        return true;
    }
    helper.fail("错误");  // ❌ 不要这样做
    return false;
}, 100, "失败消息");

// ✅ 正确：直接返回boolean，失败消息在第4个参数中
NpcTestHelper.waitForCondition(helper, () -> {
    return condition;  // 仅返回true/false
}, 100, "条件未满足");
```

### 3. 在lambda中修改外部状态

```java
// ✅ 使用数组或final包装类
final int[] counter = {0};
helper.onEachTick(() -> {
    counter[0]++;  // 正确
});
```

---

## 完整示例

```java
@GameTest(template = "empty", timeoutTicks = 200)
public static void testNPCPicksUpItem(GameTestHelper helper) {
    // 1. 创建测试实体
    BlockPos spawnPos = new BlockPos(2, 2, 2);
    BlockPos itemPos = new BlockPos(10, 2, 2);
    Zombie zombie = TestEntityFactory.createTestZombie(helper, spawnPos);
    
    // 2. 生成测试物品
    ItemEntity itemEntity = new ItemEntity(
        helper.getLevel(),
        itemPos.getX() + 0.5,
        itemPos.getY(),
        itemPos.getZ() + 0.5,
        new ItemStack(Items.DIAMOND, 1)
    );
    helper.getLevel().addFreshEntity(itemEntity);
    
    // 3. 配置NPC行为
    INpcMind mind = NpcTestHelper.getMind(helper, zombie);
    mind.getGoalSelector().registerGoal(new PickUpItemGoal(itemEntity, 1.0f));
    
    // 4. 启动Mind
    NpcTestHelper.tickMind(helper, zombie);
    
    // 5. 等待成功条件
    NpcTestHelper.waitForCondition(
        helper,
        () -> !itemEntity.isAlive() && 
              zombie.getMainHandItem().getItem() == Items.DIAMOND,
        150,
        "NPC未能拾取钻石"
    );
}
```

---

## 调试技巧

1. **添加日志输出**
   ```java
   System.out.println("[TestName] 检查点: " + someValue);
   ```

2. **使用分阶段验证**
   ```java
   NpcTestHelper.waitForCondition(helper, () -> {
       if (phase1Complete) {
           System.out.println("✓ 阶段1完成");
           return phase2Complete;
       }
       return false;
   }, 200, "测试失败");
   ```

3. **检查NpcMind状态**
   ```java
   INpcMind mind = NpcTestHelper.getMind(helper, zombie);
   IGoal currentGoal = mind.getGoalSelector().getCurrentGoal();
   System.out.println("当前目标: " + (currentGoal != null ? currentGoal.getName() : "null"));
   ```

---

## 参考

- [重构计划](./plan.md)
- [ComplexScenarios.java](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs_test/tests/ComplexScenarios.java) - 重构后的完整示例
- [NpcTestHelper.java](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs_test/utils/NpcTestHelper.java) - 工具类源码
