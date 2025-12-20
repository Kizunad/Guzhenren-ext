package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.common.BingXueDaoActiveSnowDomainEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.common.BingXueDaoActiveSummonSerpentEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.common.BingXueDaoSerpentAegisEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.common.BingXueDaoSnowstormAuraEffect;

/**
 * 五转冰雪道蛊虫效果注册。
 */
public final class TierFiveBingXueDaoRegistry {

    private TierFiveBingXueDaoRegistry() {}

    public static void registerAll() {
        registerBaiXiangSheGu();
        registerManTianFeiXueGu();
    }

    private static void registerBaiXiangSheGu() {
        final String passive = "guzhenren:bai_xiang_she_gu_passive_serpent_aegis";
        final String active = "guzhenren:bai_xiang_she_gu_active_summon_serpent";
        final String cooldownKey =
            "GuzhenrenExtCooldown_bai_xiang_she_gu_active_summon_serpent";

        GuEffectRegistry.register(new BingXueDaoSerpentAegisEffect(passive));
        GuEffectRegistry.register(
            new BingXueDaoActiveSummonSerpentEffect(active, cooldownKey)
        );
    }

    private static void registerManTianFeiXueGu() {
        final String passive = "guzhenren:mantianfeixuegu_passive_snow_domain";
        final String active = "guzhenren:mantianfeixuegu_active_snow_domain";
        final String cooldownKey =
            "GuzhenrenExtCooldown_mantianfeixuegu_active_snow_domain";

        GuEffectRegistry.register(new BingXueDaoSnowstormAuraEffect(passive));
        GuEffectRegistry.register(
            new BingXueDaoActiveSnowDomainEffect(active, cooldownKey)
        );
    }
}

