package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jiandao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

/**
 * 剑道杀招逻辑注册。
 */
public final class JianDaoShazhaoRegistry {

    private JianDaoShazhaoRegistry() {}

    /**
     * 注册所有剑道杀招逻辑。
     */
    public static void registerAll() {
        ShazhaoEffectRegistry.register(new JianYiFocusShazhaoEffect());
        ShazhaoEffectRegistry.register(new JinWenJianXiaYangFengShazhaoEffect());
        ShazhaoEffectRegistry.register(new JianYingBindingFieldShazhaoEffect());
        ShazhaoEffectRegistry.register(new YuJianLingKongShazhaoEffect());
        ShazhaoEffectRegistry.register(new JianXinReserveShazhaoEffect());

        ShazhaoEffectRegistry.register(new JianXiaFeiCiShazhaoEffect());
        ShazhaoEffectRegistry.register(new JianHenPierceShazhaoEffect());
        ShazhaoEffectRegistry.register(new QiXingJianYuShazhaoEffect());
        ShazhaoEffectRegistry.register(new JianYuSweepShazhaoEffect());
        ShazhaoEffectRegistry.register(new WanJianGuiZongShazhaoEffect());
    }
}
