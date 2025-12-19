package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierTwo.DaHunGuSoulSurgeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierTwo.DaHunGuHunPoMaterialIngestEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierTwo.HunFeiGuSoulDashEffect;

/**
 * 二转魂道蛊虫逻辑注册表。
 */
public final class TierTwoHunDaoRegistry {

    private TierTwoHunDaoRegistry() {}

    public static void registerAll() {
        GuEffectRegistry.register(new GuiHuoGuEffect());
        GuEffectRegistry.register(new DaHunGuEffect());
        GuEffectRegistry.register(new DaHunGuSoulSurgeEffect());
        GuEffectRegistry.register(new DaHunGuHunPoMaterialIngestEffect());
        GuEffectRegistry.register(new GuiJiaoGuSoulShriekEffect());
        GuEffectRegistry.register(new HunFeiGuSoulWingsEffect());
        GuEffectRegistry.register(new HunFeiGuSoulDashEffect());
    }
}
