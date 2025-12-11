package com.Kizunad.guzhenrenext.kongqiao.logic;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 蛊虫效果注册表。
 * <p>
 * 负责维护 usageID 到 IGuEffect 实例的映射。
 * </p>
 */
public final class GuEffectRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuEffectRegistry.class);
    private static final Map<String, IGuEffect> REGISTRY = new HashMap<>();

    private GuEffectRegistry() {}

    /**
     * 注册一个蛊虫效果逻辑。
     */
    public static void register(IGuEffect effect) {
        if (effect == null || effect.getUsageId() == null) {
            LOGGER.warn("尝试注册无效的蛊虫效果: {}", effect);
            return;
        }
        if (REGISTRY.containsKey(effect.getUsageId())) {
            LOGGER.warn("覆盖已存在的蛊虫效果 ID: {}", effect.getUsageId());
        }
        REGISTRY.put(effect.getUsageId(), effect);
        LOGGER.debug("已注册蛊虫效果逻辑: {}", effect.getUsageId());
    }

    /**
     * 获取指定 ID 的效果逻辑。
     */
    @Nullable
    public static IGuEffect get(String usageId) {
        return REGISTRY.get(usageId);
    }
}
