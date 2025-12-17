package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;

/**
 * 五转魂道：效果注册。
 */
public final class TierFiveHunDaoRegistry {

    private TierFiveHunDaoRegistry() {}

    public static void registerAll() {
        GuEffectRegistry.register(new HunBaoGuChainDetonationEffect());
        GuEffectRegistry.register(new DingHunGuSoulAnchorEffect());
        GuEffectRegistry.register(new BaiGuiYeXingGuHundredGhostsMarchEffect());
    }
}
