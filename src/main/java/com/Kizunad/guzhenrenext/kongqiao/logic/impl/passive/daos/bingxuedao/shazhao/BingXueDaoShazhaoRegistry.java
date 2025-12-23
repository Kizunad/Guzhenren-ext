package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

/**
 * 冰雪道杀招逻辑注册。
 */
public final class BingXueDaoShazhaoRegistry {

    private BingXueDaoShazhaoRegistry() {}

    /**
     * 注册所有冰雪道杀招逻辑。
     */
    public static void registerAll() {
        ShazhaoEffectRegistry.register(new BingBuTaXueShazhaoEffect());
        ShazhaoEffectRegistry.register(new BingZhuiNingShenShazhaoEffect());
        ShazhaoEffectRegistry.register(new BingJiYuGuShazhaoEffect());
        ShazhaoEffectRegistry.register(new JinFengSongShuangShazhaoEffect());
        ShazhaoEffectRegistry.register(new ManTianFeiXueReserveShazhaoEffect());

        ShazhaoEffectRegistry.register(new BingBuHanRenShazhaoEffect());
        ShazhaoEffectRegistry.register(new BingZhuiHanDunShazhaoEffect());
        ShazhaoEffectRegistry.register(new LanNiaoBingGuanShazhaoEffect());
        ShazhaoEffectRegistry.register(new ShuangJianHuiChaoShazhaoEffect());
        ShazhaoEffectRegistry.register(new HanYueFuXueShazhaoEffect());
    }
}

