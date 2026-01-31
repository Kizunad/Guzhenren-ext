package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yandao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

public final class YanDaoShazhaoRegistry {

    private YanDaoShazhaoRegistry() {}

    public static void registerAll() {
        ShazhaoEffectRegistry.register(new YanDaoExplosiveCrimsonRobeEffect());
        ShazhaoEffectRegistry.register(new YanDaoRedLotusKarmicRobeEffect());
    }
}
