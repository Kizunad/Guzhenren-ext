package com.Kizunad.customNPCs.ai.decision.registry;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 猎杀目标注册表，允许外部模组注册自定义敌对实体或标签。
 * <p>
 * 该注册表设计为线程安全，避免基础 CustomNPC 依赖具体扩展实现。
 * </p>
 */
public final class HuntTargetRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        HuntTargetRegistry.class
    );

    private static final Set<EntityType<?>> DIRECT_TYPES =
        ConcurrentHashMap.newKeySet();
    private static final Set<TagKey<EntityType<?>>> TAGS =
        ConcurrentHashMap.newKeySet();
    private static final List<Predicate<LivingEntity>> PREDICATES =
        new CopyOnWriteArrayList<>();

    private HuntTargetRegistry() {}

    /**
     * 注册单个实体类型。
     */
    public static void registerEntityType(EntityType<?> type) {
        if (type != null) {
            DIRECT_TYPES.add(type);
        }
    }

    /**
     * 注册实体标签，所有匹配该标签的实体都将被视为猎杀目标。
     */
    public static void registerTag(TagKey<EntityType<?>> tag) {
        if (tag != null) {
            TAGS.add(tag);
        }
    }

    /**
     * 注册自定义判定逻辑。
     */
    public static void registerPredicate(
        Predicate<LivingEntity> predicate
    ) {
        if (predicate != null) {
            PREDICATES.add(predicate);
        }
    }

    /**
     * 判断实体是否在注册表中。
     */
    public static boolean isRegistered(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        EntityType<?> type = entity.getType();
        if (DIRECT_TYPES.contains(type)) {
            return true;
        }
        for (TagKey<EntityType<?>> tag : TAGS) {
            if (type.is(tag)) {
                return true;
            }
        }
        for (Predicate<LivingEntity> predicate : PREDICATES) {
            boolean matched;
            try {
                matched = predicate.test(entity);
            } catch (Exception e) {
                LOGGER.error("[HuntTargetRegistry] 自定义判定异常", e);
                continue;
            }
            if (matched) {
                return true;
            }
        }
        return false;
    }
}
