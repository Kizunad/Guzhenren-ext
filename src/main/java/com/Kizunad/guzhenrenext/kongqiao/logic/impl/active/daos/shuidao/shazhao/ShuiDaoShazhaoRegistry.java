package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

public final class ShuiDaoShazhaoRegistry {

    private ShuiDaoShazhaoRegistry() {}

    public static void registerAll() {
        ShazhaoEffectRegistry.register(new ShuiDaoGentleWaterRippleEffect());
        ShazhaoEffectRegistry.register(new ShuiDaoWaterScreenSkyFlowerEffect());
    }
}
