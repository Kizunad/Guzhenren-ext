package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.common.DaoSustainedResourceRegenEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.common.BingXueDaoFrostBarrierOnHurtEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveSelfBuffEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 一转冰雪道蛊虫效果注册。
 */
public final class TierOneBingXueDaoRegistry {

    private TierOneBingXueDaoRegistry() {}

    public static void registerAll() {
        registerBingBuGu();
    }

    private static void registerBingBuGu() {
        final String passive = "guzhenren:bing_bu_gu_passive_frost_cloth";
        final String regenPassive = "guzhenren:bing_bu_gu_passive_han_xi_ning_yuan";
        final String active = "guzhenren:bing_bu_gu_active_frost_shroud";
        final String barrierCooldownKey =
            "GuzhenrenExtCooldown_bing_bu_gu_passive_frost_cloth";
        final String activeCooldownKey =
            "GuzhenrenExtCooldown_bing_bu_gu_active_frost_shroud";

        GuEffectRegistry.register(
            new BingXueDaoFrostBarrierOnHurtEffect(passive, barrierCooldownKey)
        );
        GuEffectRegistry.register(
            new DaoSustainedResourceRegenEffect(
                regenPassive,
                DaoHenHelper.DaoType.BING_XUE_DAO,
                false,
                false,
                false,
                true
            )
        );
        GuEffectRegistry.register(
            new ZhiDaoActiveSelfBuffEffect(
                active,
                activeCooldownKey,
                List.of(
                    new ZhiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resistance_duration_ticks",
                        0,
                        "resistance_amplifier",
                        0
                    ),
                    new ZhiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "absorption_duration_ticks",
                        0,
                        "absorption_amplifier",
                        0
                    )
                )
            )
        );
    }
}
