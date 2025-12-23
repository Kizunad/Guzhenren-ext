package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

/**
 * 力道杀招逻辑注册。
 */
public final class LiDaoShazhaoRegistry {

    private LiDaoShazhaoRegistry() {}

    /**
     * 注册所有力道杀招逻辑。
     */
    public static void registerAll() {
        ShazhaoEffectRegistry.register(new ManNiuForgeBodyShazhaoEffect());
        ShazhaoEffectRegistry.register(new ShiGangAegisShazhaoEffect());
        ShazhaoEffectRegistry.register(new BaWangReserveShazhaoEffect());

        ShazhaoEffectRegistry.register(new BengShanFistShazhaoEffect());
        ShazhaoEffectRegistry.register(new BaShanQuakeShazhaoEffect());
        ShazhaoEffectRegistry.register(new BaWangZhenYueShazhaoEffect());
    }
}

