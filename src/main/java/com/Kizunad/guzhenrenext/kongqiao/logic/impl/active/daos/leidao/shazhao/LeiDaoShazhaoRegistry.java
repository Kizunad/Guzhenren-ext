package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.leidao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

public final class LeiDaoShazhaoRegistry {

    private LeiDaoShazhaoRegistry() {}

    public static void registerAll() {
        ShazhaoEffectRegistry.register(new LeiDaoLightningMagneticGridEffect());
        ShazhaoEffectRegistry.register(new LeiDaoThunderPoolForbiddenGroundEffect());
    }
}
