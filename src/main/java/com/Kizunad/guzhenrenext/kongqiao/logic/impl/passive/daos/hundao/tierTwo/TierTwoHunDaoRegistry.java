package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;

/**
 * 二转魂道蛊虫逻辑注册表。
 */
public final class TierTwoHunDaoRegistry {

    private TierTwoHunDaoRegistry() {}

    public static void registerAll() {
        GuEffectRegistry.register(new GuiHuoGuEffect());
        GuEffectRegistry.register(new DaHunGuEffect());
        GuEffectRegistry.register(new GuiJiaoGuSoulShriekEffect());
        GuEffectRegistry.register(new HunFeiGuSoulWingsEffect());
    }
}
