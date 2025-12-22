package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

/**
 * 注册所有雷道杀招逻辑（主动 + 被动）。
 */
public final class LeiDaoShazhaoRegistry {

    private LeiDaoShazhaoRegistry() {
    }

    /**
     * 注册所有雷道杀招。
     */
    public static void registerAll() {
        ShazhaoEffectRegistry.register(new LeiYiGuardShazhaoEffect());
        ShazhaoEffectRegistry.register(new DianLiuNourishShazhaoEffect());
        ShazhaoEffectRegistry.register(new LeiYuBindingFieldShazhaoEffect());

        ShazhaoEffectRegistry.register(new DianHuBurstShazhaoEffect());
        ShazhaoEffectRegistry.register(new LianHuanJingLeiShazhaoEffect());
        ShazhaoEffectRegistry.register(new LeiDunEscapeShazhaoEffect());
    }
}

