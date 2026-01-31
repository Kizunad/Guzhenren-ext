package com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lvdao.shazhao;

import com.Kizunad.guzhenrenext.kongqiao.logic.ShazhaoEffectRegistry;

public final class LvDaoShazhaoRegistry {

    private LvDaoShazhaoRegistry() {}

    public static void registerAll() {
        ShazhaoEffectRegistry.register(new LvDaoKarmaRetributionEffect());
    }
}
