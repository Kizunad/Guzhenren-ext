package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xuedao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

public final class XueDaoShazhaoRegistry {

    private XueDaoShazhaoRegistry() {}

    public static void registerAll() {
        ShazhaoEffectRegistry.register(new XueDaoAsuraBloodSeaEffect());
        ShazhaoEffectRegistry.register(new XueDaoBloodRiverChariotEffect());
    }
}
