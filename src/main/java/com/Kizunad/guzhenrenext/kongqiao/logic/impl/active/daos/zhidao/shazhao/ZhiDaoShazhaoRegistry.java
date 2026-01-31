package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

public final class ZhiDaoShazhaoRegistry {

    private ZhiDaoShazhaoRegistry() {}

    public static void registerAll() {
        ShazhaoEffectRegistry.register(new ZhiDaoStarryChessBoardEffect());
        ShazhaoEffectRegistry.register(new ZhiDaoHeavenlySecretCalculationEffect());
    }
}
