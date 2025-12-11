package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierOne;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;

/**
 * 一转魂道蛊虫逻辑注册表。
 */
public final class TierOneHunDaoRegistry {

    private TierOneHunDaoRegistry() {}

    public static void registerAll() {
        GuEffectRegistry.register(new XiaoHunGuEffect());
        // 后续添加更多一转魂道蛊虫...
    }
}
