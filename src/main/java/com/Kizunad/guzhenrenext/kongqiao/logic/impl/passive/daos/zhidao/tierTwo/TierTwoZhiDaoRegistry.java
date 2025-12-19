package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.tierTwo.DaDaHuiGuShazhaoDeriveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.tierTwo.DaZhiGuBestDeriveShazhaoEffect;

/**
 * 二转智道蛊虫效果注册。
 */
public class TierTwoZhiDaoRegistry {

    private TierTwoZhiDaoRegistry() {}

    public static void registerAll() {
        GuEffectRegistry.register(new DaDaHuiGuWisdomDeductionEffect());
        GuEffectRegistry.register(new DaDaHuiGuShazhaoDeriveEffect());
        GuEffectRegistry.register(new DaZhiGuWisdomStrategyEffect());
        GuEffectRegistry.register(new DaZhiGuBestDeriveShazhaoEffect());
    }
}
