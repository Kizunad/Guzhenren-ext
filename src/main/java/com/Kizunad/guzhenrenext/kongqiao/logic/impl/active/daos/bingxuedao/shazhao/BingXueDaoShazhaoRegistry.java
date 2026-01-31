package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

public final class BingXueDaoShazhaoRegistry {

    private BingXueDaoShazhaoRegistry() {}

    public static void registerAll() {
        ShazhaoEffectRegistry.register(new BingXueDaoAbsoluteZeroEffect());
        ShazhaoEffectRegistry.register(new BingXueDaoMyriadYearIceCoffinEffect());
    }
}
