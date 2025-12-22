package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

/**
 * 水道杀招逻辑注册。
 */
public final class ShuiDaoShazhaoRegistry {

    private ShuiDaoShazhaoRegistry() {}

    /**
     * 注册所有水道杀招逻辑。
     */
    public static void registerAll() {
        ShazhaoEffectRegistry.register(new ShuiYiTideAegisShazhaoEffect());
        ShazhaoEffectRegistry.register(new ShuiLianBindingAuraShazhaoEffect());
        ShazhaoEffectRegistry.register(new BeiShuiReserveShazhaoEffect());

        ShazhaoEffectRegistry.register(new ShuiJianPierceShazhaoEffect());
        ShazhaoEffectRegistry.register(new ShuiLaoCollapseShazhaoEffect());
        ShazhaoEffectRegistry.register(new HongShuiCataclysmShazhaoEffect());
    }
}

