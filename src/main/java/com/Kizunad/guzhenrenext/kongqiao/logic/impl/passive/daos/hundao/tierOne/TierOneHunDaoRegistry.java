package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.hundao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.common.DaoSustainedResourceRegenEffect;

/**
 * 一转魂道蛊虫逻辑注册表。
 */
public final class TierOneHunDaoRegistry {

    private TierOneHunDaoRegistry() {}

    public static void registerAll() {
        GuEffectRegistry.register(new XiaoHunGuEffect());
        GuEffectRegistry.register(
            new DaoSustainedResourceRegenEffect(
                "guzhenren:xiao_hun_gu_passive_soul_warm_aperture",
                DaoHenHelper.DaoType.HUN_DAO,
                true,
                false,
                false,
                true
            )
        );
        // 后续添加更多一转魂道蛊虫...
    }
}
