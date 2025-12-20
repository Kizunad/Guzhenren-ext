package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.common.BingXueDaoActiveClearHeatEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.common.BingXueDaoActiveTargetDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.common.BingXueDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.common.BingXueDaoCleanseOnSecondEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 二转冰雪道蛊虫效果注册。
 */
public final class TierTwoBingXueDaoRegistry {

    private TierTwoBingXueDaoRegistry() {}

    public static void registerAll() {
        registerBingZhuiGu();
        registerBingRenGu();
        registerQingReGu();
    }

    private static void registerBingZhuiGu() {
        final String passive = "guzhenren:bing_zhui_gu_passive_ice_spike";
        final String active = "guzhenren:bing_zhui_gu_active_ice_spike";
        final String cooldownKey =
            "GuzhenrenExtCooldown_bing_zhui_gu_active_ice_spike";

        GuEffectRegistry.register(
            new BingXueDaoAttackProcDebuffEffect(
                passive,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new BingXueDaoActiveTargetDebuffEffect(
                active,
                cooldownKey,
                List.of(
                    new BingXueDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerBingRenGu() {
        final String passive = "guzhenren:bing_ren_gu_passive_ice_blade";
        final String active = "guzhenren:bing_ren_gu_active_ice_blade";
        final String cooldownKey =
            "GuzhenrenExtCooldown_bing_ren_gu_active_ice_blade";

        GuEffectRegistry.register(
            new BingXueDaoAttackProcDebuffEffect(passive, MobEffects.WEAKNESS)
        );
        GuEffectRegistry.register(
            new ZhiDaoActiveSelfBuffEffect(
                active,
                cooldownKey,
                List.of(
                    new ZhiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "strength_duration_ticks",
                        0,
                        "strength_amplifier",
                        0
                    ),
                    new ZhiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        0,
                        "speed_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerQingReGu() {
        final String passive = "guzhenren:qing_re_gu_passive_clear_heat";
        final String active = "guzhenren:qing_re_gu_active_clear_heat";
        final String cooldownKey =
            "GuzhenrenExtCooldown_qing_re_gu_active_clear_heat";

        GuEffectRegistry.register(new BingXueDaoCleanseOnSecondEffect(passive));
        GuEffectRegistry.register(
            new BingXueDaoActiveClearHeatEffect(active, cooldownKey)
        );
    }
}

