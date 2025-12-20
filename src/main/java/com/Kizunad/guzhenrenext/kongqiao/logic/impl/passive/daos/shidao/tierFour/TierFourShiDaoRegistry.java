package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierFour.JiuYanJiuChongNineEyesInsightEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierFour.YouLongGuBlackOilBreathEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierFour.YangJianHuSwordWineOverdriveEffect;

/**
 * 四转食道蛊虫逻辑注册表。
 */
public final class TierFourShiDaoRegistry {

    private TierFourShiDaoRegistry() {}

    public static void registerAll() {
        GuEffectRegistry.register(new JiuYanJiuChongNineEyesRefineEffect());
        GuEffectRegistry.register(new JiuYanJiuChongNineEyesInsightEffect());
        GuEffectRegistry.register(new YouLongGuOilIgniteEffect());
        GuEffectRegistry.register(new YouLongGuBlackOilBreathEffect());
        GuEffectRegistry.register(new YangJianHuSwordWineAuraEffect());
        GuEffectRegistry.register(new YangJianHuSwordWineOverdriveEffect());
    }
}

