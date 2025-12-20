package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierTwo.SiWeiJiuChongFourFlavorsFuryEffect;

/**
 * 二转食道蛊虫逻辑注册表。
 */
public final class TierTwoShiDaoRegistry {

    private TierTwoShiDaoRegistry() {}

    public static void registerAll() {
        GuEffectRegistry.register(new SiWeiJiuChongFourFlavorsMeditateEffect());
        GuEffectRegistry.register(new SiWeiJiuChongFourFlavorsFuryEffect());
    }
}

