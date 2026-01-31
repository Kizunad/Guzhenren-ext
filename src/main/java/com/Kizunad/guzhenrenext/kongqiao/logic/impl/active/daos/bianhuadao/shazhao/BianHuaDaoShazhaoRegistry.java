package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bianhuadao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

public final class BianHuaDaoShazhaoRegistry {

    private BianHuaDaoShazhaoRegistry() {}

    public static void registerAll() {
        ShazhaoEffectRegistry.register(new BianHuaDaoHeavenlyDemonBodyEffect());
    }
}
