package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

public final class LiDaoShazhaoRegistry {

    private LiDaoShazhaoRegistry() {}

    public static void registerAll() {
        ShazhaoEffectRegistry.register(new LiDaoOverlordForceFieldEffect());
        ShazhaoEffectRegistry.register(new LiDaoAbsoluteStrengthSuppressionEffect());
    }
}
