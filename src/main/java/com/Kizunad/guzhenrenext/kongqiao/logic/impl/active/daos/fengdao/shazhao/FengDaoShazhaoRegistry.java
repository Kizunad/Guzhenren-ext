package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.fengdao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

public final class FengDaoShazhaoRegistry {

    private FengDaoShazhaoRegistry() {}

    public static void registerAll() {
        ShazhaoEffectRegistry.register(new FengDaoInvisibleWindCloakEffect());
        ShazhaoEffectRegistry.register(new FengDaoNineHeavensAstralWindEffect());
    }
}
