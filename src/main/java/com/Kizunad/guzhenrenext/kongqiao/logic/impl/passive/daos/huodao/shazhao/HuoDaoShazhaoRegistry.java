package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.huodao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.huodao.shazhao.HuoDaoShazhaoHeavenfireMeteorEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.huodao.shazhao.HuoDaoShazhaoScorchingWaveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.huodao.shazhao.HuoDaoShazhaoSparkThrustEffect;

/**
 * 火/炎道杀招逻辑注册。
 */
public final class HuoDaoShazhaoRegistry {

    private HuoDaoShazhaoRegistry() {}

    /**
     * 注册所有火/炎道杀招逻辑（主动 + 被动）。
     */
    public static void registerAll() {
        ShazhaoEffectRegistry.register(new HuoDaoShazhaoEmberMantleEffect());
        ShazhaoEffectRegistry.register(new HuoDaoShazhaoBlazingWillEffect());
        ShazhaoEffectRegistry.register(new HuoDaoShazhaoFurnaceBreathEffect());

        ShazhaoEffectRegistry.register(new HuoDaoShazhaoSparkThrustEffect());
        ShazhaoEffectRegistry.register(new HuoDaoShazhaoScorchingWaveEffect());
        ShazhaoEffectRegistry.register(new HuoDaoShazhaoHeavenfireMeteorEffect());
    }
}

