# 测试代码污染生产代码问题

## 问题描述

当前生产代码中包含了大量测试相关的逻辑,违反了代码清洁度原则。测试隔离机制(test tag)的实现代码散布在多个生产类中,导致:

1. **职责混乱**: 生产代码需要了解测试框架的存在
2. **维护困难**: 测试相关代码和业务逻辑耦合在一起
3. **性能开销**: 生产环境中执行不必要的测试标签检查
4. **可读性下降**: 代码中充斥着 `if (testTag != null)` 等测试相关判断

## 受影响的文件

### 1. ActionExecutor.java
**位置**: `src/main/java/com/Kizunad/customNPCs/ai/executor/ActionExecutor.java`

**污染代码**:
- 第 27 行: `private String boundTestTag;` - 测试标签字段
- 第 45-59 行: `bindToEntity()` 中的测试标签提取和绑定逻辑
- 第 62-80 行: `isContextValid()` 中的测试标签验证逻辑
- 第 82-87 行: `extractTestTag()` 方法 - 完全是测试相关

```java
// 当前污染代码示例
private String boundTestTag;

public void bindToEntity(LivingEntity entity) {
    // ... 业务逻辑 ...
    String testTag = extractTestTag(entity);  // ❌ 测试代码
    if (testTag != null) {
        this.boundTestTag = testTag;
    }
}

private String extractTestTag(LivingEntity entity) {  // ❌ 整个方法都是测试代码
    return entity.getTags().stream()
        .filter(tag -> tag.startsWith("test:"))
        .findFirst()
        .orElse(null);
}
```

### 2. VisionSensor.java
**位置**: `src/main/java/com/Kizunad/customNPCs/ai/sensors/VisionSensor.java`

**污染代码**:
- 第 50-54 行: 测试标签提取逻辑
- 第 60-61 行: 基于测试标签的实体过滤

```java
// 当前污染代码示例
String testTag = entity.getTags().stream()  // ❌ 测试代码
    .filter(tag -> tag.startsWith("test:"))
    .findFirst()
    .orElse(null);

List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
    LivingEntity.class,
    searchBox,
    e -> e != entity && e.isAlive() &&
         (testTag == null || e.getTags().contains(testTag))  // ❌ 测试代码
);
```

### 3. AuditorySensor.java
**位置**: `src/main/java/com/Kizunad/customNPCs/ai/sensors/AuditorySensor.java`

**污染代码**:
- 第 54 行: 测试标签过滤逻辑(与 VisionSensor 类似)

## 解决方案

### 方案: 创建测试工具层 (Test Utility Layer)

创建一个专门的测试工具包,将测试隔离逻辑从生产代码中分离出来。

#### 架构设计

```
src/main/java/com/Kizunad/customNPCs_test/
├── utils/
│   ├── NpcTestHelper.java (已存在)
│   ├── TestContextManager.java (新建) - 管理测试上下文和隔离
│   └── TestAwareWrapper.java (新建) - 包装生产类,注入测试逻辑
└── overrides/
    ├── TestActionExecutor.java (新建) - 继承 ActionExecutor,添加测试隔离
    ├── TestVisionSensor.java (新建) - 继承 VisionSensor,添加测试过滤
    └── TestAuditorySensor.java (新建) - 继承 AuditorySensor,添加测试过滤
```

#### 实现步骤

##### 1. 创建 TestContextManager

```java
// src/main/java/com/Kizunad/customNPCs_test/utils/TestContextManager.java
public class TestContextManager {
    private static final String TEST_TAG_PREFIX = "test:";
    
    /**
     * 从实体提取测试标签
     */
    public static String extractTestTag(Entity entity) {
        return entity.getTags().stream()
            .filter(tag -> tag.startsWith(TEST_TAG_PREFIX))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * 检查两个实体是否属于同一测试上下文
     */
    public static boolean isSameTestContext(Entity a, Entity b) {
        String tagA = extractTestTag(a);
        String tagB = extractTestTag(b);
        
        if (tagA == null && tagB == null) return true;
        if (tagA == null || tagB == null) return false;
        return tagA.equals(tagB);
    }
    
    /**
     * 创建实体过滤器,只保留同一测试上下文的实体
     */
    public static Predicate<Entity> createTestContextFilter(Entity observer) {
        String testTag = extractTestTag(observer);
        if (testTag == null) {
            return entity -> true; // 非测试环境,不过滤
        }
        return entity -> entity.getTags().contains(testTag);
    }
}
```

##### 2. 创建测试专用的 ActionExecutor

```java
// src/main/java/com/Kizunad/customNPCs_test/overrides/TestActionExecutor.java
public class TestActionExecutor extends ActionExecutor {
    private String boundTestTag;
    
    @Override
    public void bindToEntity(LivingEntity entity) {
        super.bindToEntity(entity);
        
        // 测试隔离逻辑
        String testTag = TestContextManager.extractTestTag(entity);
        if (testTag != null) {
            this.boundTestTag = testTag;
        }
    }
    
    @Override
    public void tick(INpcMind mind, LivingEntity entity) {
        // 验证测试上下文
        if (boundTestTag != null && !entity.getTags().contains(boundTestTag)) {
            System.err.println("[TestActionExecutor] 检测到测试上下文变化,丢弃计划");
            stopCurrentPlan();
            return;
        }
        
        super.tick(mind, entity);
    }
}
```

##### 3. 创建测试专用的传感器

```java
// src/main/java/com/Kizunad/customNPCs_test/overrides/TestVisionSensor.java
public class TestVisionSensor extends VisionSensor {
    
    @Override
    public void sense(INpcMind mind, LivingEntity entity, ServerLevel level) {
        // 注入测试过滤逻辑
        String testTag = TestContextManager.extractTestTag(entity);
        
        // 调用父类方法前,临时修改扫描逻辑
        // 或者重写整个 sense 方法,在实体过滤时添加测试上下文检查
        
        // ... 实现细节 ...
    }
}
```

##### 4. 在测试中使用测试专用类

```java
// NpcTestHelper.java 中修改
public static <T extends Mob> T spawnNPCWithMind(
        GameTestHelper helper,
        BlockPos pos,
        EntityType<T> entityType) {
    T entity = helper.spawn(entityType, pos);
    applyTestTag(helper, entity);
    
    // 创建带有测试隔离的 NpcMind
    NpcMind mind = new NpcMind();
    
    // 替换为测试专用组件
    mind.getSensorManager().clear();
    mind.getSensorManager().addSensor(new TestVisionSensor());
    mind.getSensorManager().addSensor(new TestAuditorySensor());
    
    // 注入测试专用的 ActionExecutor
    // (需要 NpcMind 支持替换 ActionExecutor)
    
    entity.setData(NpcMindAttachment.NPC_MIND, mind);
    return entity;
}
```

##### 5. 清理生产代码

从以下文件中移除所有测试相关代码:
- [ ] `ActionExecutor.java` - 移除 `boundTestTag`, `extractTestTag()`, 测试验证逻辑
- [ ] `VisionSensor.java` - 移除测试标签提取和过滤逻辑
- [ ] `AuditorySensor.java` - 移除测试标签过滤逻辑

## 优势

1. ✅ **职责分离**: 生产代码专注业务逻辑,测试代码专注测试隔离
2. ✅ **性能优化**: 生产环境不执行测试相关检查
3. ✅ **可维护性**: 测试逻辑集中管理,易于修改和扩展
4. ✅ **可读性**: 生产代码更清晰,没有测试相关的条件判断
5. ✅ **灵活性**: 可以为不同测试场景创建不同的测试工具

## 注意事项

1. **需要 NpcMind 支持组件替换**: 可能需要添加 setter 方法或构造函数参数
2. **传感器过滤逻辑**: 需要设计一个优雅的方式让测试传感器继承并扩展父类逻辑
3. **向后兼容**: 确保现有测试在迁移过程中仍能正常运行

## 优先级

**中等** - 不影响功能,但影响代码质量和长期维护性

## 相关文件

- [ActionExecutor.java](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/executor/ActionExecutor.java)
- [VisionSensor.java](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/sensors/VisionSensor.java)
- [AuditorySensor.java](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs/ai/sensors/AuditorySensor.java)
- [NpcTestHelper.java](file:///home/kiz/Code/java/Guzhenren-ext/src/main/java/com/Kizunad/customNPCs_test/utils/NpcTestHelper.java)
