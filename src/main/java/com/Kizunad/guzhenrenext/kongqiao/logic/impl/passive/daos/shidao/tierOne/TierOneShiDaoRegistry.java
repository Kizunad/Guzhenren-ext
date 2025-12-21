package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shidao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierOne.FanDaiCaoGuRiceFeastEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierOne.JiuChongWarmBrewEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shidao.tierOne.JiuNangHuaGuWineMistEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.common.DaoSustainedResourceRegenEffect;

/**
 * 一转食道蛊虫逻辑注册表。
 */
public final class TierOneShiDaoRegistry {

    private TierOneShiDaoRegistry() {}

    public static void registerAll() {
        GuEffectRegistry.register(new JiuChongRefineZhenyuanEffect());
        GuEffectRegistry.register(
            new DaoSustainedResourceRegenEffect(
                "guzhenren:jiu_chong_passive_wine_nourish_spirit",
                DaoHenHelper.DaoType.SHI_DAO,
                false,
                true,
                true,
                false
            )
        );
        GuEffectRegistry.register(new JiuChongWarmBrewEffect());
        GuEffectRegistry.register(new JiuNangHuaGuBrewScentEffect());
        GuEffectRegistry.register(new JiuNangHuaGuWineMistEffect());
        GuEffectRegistry.register(new FanDaiCaoGuRiceSupplyEffect());
        GuEffectRegistry.register(new FanDaiCaoGuRiceFeastEffect());
    }
}
