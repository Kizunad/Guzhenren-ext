package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierThree.DaFuPianPianGuFullBellyShieldEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierThree.QiXiangJiuChongFragrantDashEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierThree.ShengJiJiuHuLuVitalitySurgeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierThree.YangShengGuLongevityBrewEffect;

/**
 * 三转食道蛊虫逻辑注册表。
 */
public final class TierThreeShiDaoRegistry {

    private TierThreeShiDaoRegistry() {}

    public static void registerAll() {
        GuEffectRegistry.register(new YangShengGuHealthNourishEffect());
        GuEffectRegistry.register(new YangShengGuLongevityBrewEffect());
        GuEffectRegistry.register(new DaFuPianPianGuFatShieldEffect());
        GuEffectRegistry.register(new DaFuPianPianGuFullBellyShieldEffect());
        GuEffectRegistry.register(new ShengJiJiuHuLuVitalityWineEffect());
        GuEffectRegistry.register(new ShengJiJiuHuLuVitalitySurgeEffect());
        GuEffectRegistry.register(new QiXiangJiuChongFragrantBreathEffect());
        GuEffectRegistry.register(new QiXiangJiuChongFragrantDashEffect());
    }
}

