package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.tudao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

public final class TuDaoShazhaoRegistry {

    private TuDaoShazhaoRegistry() {}

    public static void registerAll() {
        ShazhaoEffectRegistry.register(new TuDaoHeavyEarthFortressEffect());
        ShazhaoEffectRegistry.register(new TuDaoImmovableKingWallEffect());
    }
}
