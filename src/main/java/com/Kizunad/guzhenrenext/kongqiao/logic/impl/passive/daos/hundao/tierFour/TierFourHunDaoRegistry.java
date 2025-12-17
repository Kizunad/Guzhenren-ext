package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierFour.HuPoGuBlackTigerHeartDigEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.tierFour.HuPoGuTigerEvilWaveEffect;

/**
 * 四转魂道：效果注册。
 */
public final class TierFourHunDaoRegistry {

    private TierFourHunDaoRegistry() {}

    public static void registerAll() {
        GuEffectRegistry.register(new HuPoGuTigerMomentumEffect());
        GuEffectRegistry.register(new HuPoGuTigerEvilWaveEffect());
        GuEffectRegistry.register(new HuPoGuBlackTigerHeartDigEffect());
    }
}

