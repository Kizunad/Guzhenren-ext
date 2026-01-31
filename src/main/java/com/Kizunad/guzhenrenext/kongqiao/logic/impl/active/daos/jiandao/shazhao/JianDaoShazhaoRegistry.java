package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jiandao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

public final class JianDaoShazhaoRegistry {

    private JianDaoShazhaoRegistry() {}

    public static void registerAll() {
        ShazhaoEffectRegistry.register(new JianDaoTenThousandSwordsEffect());
        ShazhaoEffectRegistry.register(new JianDaoImmortalExecutionerArrayEffect());
    }
}
