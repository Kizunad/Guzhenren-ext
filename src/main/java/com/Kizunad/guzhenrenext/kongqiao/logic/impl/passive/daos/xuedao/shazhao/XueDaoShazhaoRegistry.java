package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xuedao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

/**
 * 血道杀招逻辑注册。
 */
public final class XueDaoShazhaoRegistry {

    private XueDaoShazhaoRegistry() {}

    /**
     * 注册所有血道杀招逻辑（主动 + 被动）。
     */
    public static void registerAll() {
        ShazhaoEffectRegistry.register(new XueDaoShazhaoBloodBreathRegenEffect());
        ShazhaoEffectRegistry.register(new XueDaoShazhaoBloodArmorEffect());
        ShazhaoEffectRegistry.register(
            new XueDaoShazhaoBloodApertureReserveEffect()
        );

        ShazhaoEffectRegistry.register(new XueDaoShazhaoBloodAwlPursuitEffect());
        ShazhaoEffectRegistry.register(
            new XueDaoShazhaoScarletRendingWaveEffect()
        );
        ShazhaoEffectRegistry.register(new XueDaoShazhaoBloodSeaOverturnEffect());
    }
}

