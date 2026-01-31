package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jindao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jin_dao.shazhao.JinDaoIndestructibleGoldenBellEffect;

public final class JinDaoShazhaoRegistry {

    private JinDaoShazhaoRegistry() {}

    public static void registerAll() {
        ShazhaoEffectRegistry.register(new JinDaoGengJinSwordArrayEffect());
        ShazhaoEffectRegistry.register(new JinDaoIndestructibleGoldenBellEffect());
    }
}
