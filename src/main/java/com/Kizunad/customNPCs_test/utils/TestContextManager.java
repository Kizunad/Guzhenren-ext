package com.Kizunad.customNPCs_test.utils;

import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

/**
 * 测试上下文管理器 - 负责处理测试隔离逻辑
 */
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
        
        if (tagA == null && tagB == null) {
            return true;
        }
        if (tagA == null || tagB == null) {
            return false;
        }
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
        return entity -> {
            String entityTag = extractTestTag(entity);
            // 如果目标实体没有测试标签，或者是同一个测试标签，则允许
            // 注意：通常测试环境中的所有相关实体都应该打上标签
            // 如果目标没有标签，可能是一个通用实体，是否允许取决于具体需求
            // 这里假设测试环境严格隔离，只允许同标签
            return testTag.equals(entityTag);
        };
    }
}
