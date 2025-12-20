package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveCultivationBoostEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.tierFive.KongNianGuSeverThoughtActiveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoSustainedRegenEffect;

import java.util.List;

import net.minecraft.world.effect.MobEffects;

/**
 * 五转智道蛊虫效果注册。
 */
public class TierFiveZhiDaoRegistry {

    private TierFiveZhiDaoRegistry() {}

    public static void registerAll() {
        final String zhiluguPassive =
            "guzhenren:zhi_lu_gu_5_passive_wisdom_reserve";
        final String zhiluguActive =
            "guzhenren:zhi_lu_gu_5_active_insight_cultivation";
        final String zhiluguCooldownKey =
            "GuzhenrenExtCooldown_zhi_lu_gu_5_active_insight_cultivation";

        final String woyiguPassive =
            "guzhenren:wo_yi_gu_passive_resolve";
        final String woyiguActive =
            "guzhenren:wo_yi_gu_active_iron_will";
        final String woyiguCooldownKey =
            "GuzhenrenExtCooldown_wo_yi_gu_active_iron_will";

        final String kongnianguPassive =
            "guzhenren:kong_nian_gu_passive_empty_thought";
        final String kongnianguActive =
            "guzhenren:kong_nian_gu_active_sever_thought";

        GuEffectRegistry.register(new ZhiDaoSustainedRegenEffect(zhiluguPassive));
        GuEffectRegistry.register(
            new ZhiDaoActiveCultivationBoostEffect(
                zhiluguActive,
                zhiluguCooldownKey
            )
        );

        GuEffectRegistry.register(new WoYiGuResolvePassiveEffect(woyiguPassive));
        GuEffectRegistry.register(
            new ZhiDaoActiveSelfBuffEffect(
                woyiguActive,
                woyiguCooldownKey,
                List.of(
                    new ZhiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new ZhiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "regen_duration_ticks",
                        0,
                        "regen_amplifier",
                        0
                    )
                )
            )
        );

        GuEffectRegistry.register(
            new KongNianGuEmptyThoughtPassiveEffect(kongnianguPassive)
        );
        GuEffectRegistry.register(new KongNianGuSeverThoughtActiveEffect(kongnianguActive));
    }
}
