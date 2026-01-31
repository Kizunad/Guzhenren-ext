package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.hundao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

public final class HunDaoShazhaoRegistry {

    private HunDaoShazhaoRegistry() {}

    public static void registerAll() {
        ShazhaoEffectRegistry.register(new HunDaoNetherGhostDomainEffect());
        ShazhaoEffectRegistry.register(new HunDaoDangHunMountainPhantomEffect());
    }
}
