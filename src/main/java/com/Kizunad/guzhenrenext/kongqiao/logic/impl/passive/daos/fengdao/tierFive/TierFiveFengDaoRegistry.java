package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.fengdao.common.FengDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.common.FengDaoWindBarrierOnHurtEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 五转风道蛊虫效果注册。
 */
public final class TierFiveFengDaoRegistry {

    private TierFiveFengDaoRegistry() {}

    public static void registerAll() {
        registerFengBaWangGu();
    }

    private static void registerFengBaWangGu() {
        final String passive =
            "guzhenren:feng_ba_wang_gu_passive_wind_emperor";
        final String active =
            "guzhenren:feng_ba_wang_gu_active_wind_emperor";
        final String cooldownKey =
            "GuzhenrenExtCooldown_feng_ba_wang_gu_active_wind_emperor";
        final String barrierCooldownKey =
            "GuzhenrenExtCooldown_feng_ba_wang_gu_passive_wind_emperor";

        GuEffectRegistry.register(
            new FengDaoWindBarrierOnHurtEffect(passive, barrierCooldownKey)
        );
        GuEffectRegistry.register(
            new FengDaoActiveAoEBurstEffect(
                active,
                cooldownKey,
                List.of(
                    new FengDaoActiveAoEBurstEffect.EffectSpec(
                        MobEffects.LEVITATION,
                        "levitation_duration_ticks",
                        0,
                        "levitation_amplifier",
                        0
                    ),
                    new FengDaoActiveAoEBurstEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "slow_duration_ticks",
                        0,
                        "slow_amplifier",
                        0
                    )
                )
            )
        );
    }
}

