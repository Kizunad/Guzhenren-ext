package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

public final class MuDaoShazhaoRegistry {

    private MuDaoShazhaoRegistry() {}

    public static void registerAll() {
        ShazhaoEffectRegistry.register(new MuDaoForestManifestationEffect());
        ShazhaoEffectRegistry.register(new MuDaoJianMuEternalSpringRealmEffect());
    }
}
