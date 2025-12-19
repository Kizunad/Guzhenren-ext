package com.Kizunad.guzhenrenext.kongqiao.logic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 杀招效果注册表。
 * <p>
 * 负责维护 shazhaoID 到 IShazhaoEffect 实例的映射。
 * </p>
 */
public final class ShazhaoEffectRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        ShazhaoEffectRegistry.class
    );
    private static final Map<ResourceLocation, IShazhaoEffect> REGISTRY =
        new HashMap<>();

    private ShazhaoEffectRegistry() {}

    /**
     * 注册一个杀招效果逻辑。
     *
     * @param effect 效果实现
     */
    public static void register(IShazhaoEffect effect) {
        if (effect == null || effect.getShazhaoId() == null) {
            LOGGER.warn("尝试注册无效的杀招效果: {}", effect);
            return;
        }

        final ResourceLocation id;
        try {
            id = ResourceLocation.parse(effect.getShazhaoId());
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("杀招 ID 非法，忽略注册: {}", effect.getShazhaoId());
            return;
        }

        if (REGISTRY.containsKey(id)) {
            LOGGER.warn("覆盖已存在的杀招效果 ID: {}", id);
        }
        REGISTRY.put(id, effect);
        LOGGER.debug("已注册杀招效果逻辑: {}", id);
    }

    /**
     * 获取指定 ID 的杀招效果逻辑。
     *
     * @param shazhaoId 杀招 ID
     * @return 对应效果实现
     */
    @Nullable
    public static IShazhaoEffect get(ResourceLocation shazhaoId) {
        return REGISTRY.get(shazhaoId);
    }

    /**
     * 获取当前已注册的全部杀招效果。
     *
     * @return 不可变映射（ID -> 效果）
     */
    public static Map<ResourceLocation, IShazhaoEffect> getAll() {
        return Collections.unmodifiableMap(REGISTRY);
    }
}
