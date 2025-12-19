package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

/**
 * 魂道杀招逻辑注册。
 */
public final class HunDaoShazhaoRegistry {

    private HunDaoShazhaoRegistry() {}

    /**
     * 注册所有魂道杀招逻辑。
     */
    public static void registerAll() {
        ShazhaoEffectRegistry.register(new HunDunShazhaoIronWallEffect());
        ShazhaoEffectRegistry.register(new GuiYanShazhaoGhostArmorEffect());
    }
}
