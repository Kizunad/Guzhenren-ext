package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.common.BingXueDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.common.BingXueDaoActiveIceCoffinEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.common.BingXueDaoActiveIceExplosionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.common.BingXueDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.common.BingXueDaoExplosionGuardEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.common.BingXueDaoIceBodyEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 三转冰雪道蛊虫效果注册。
 */
public final class TierThreeBingXueDaoRegistry {

    private TierThreeBingXueDaoRegistry() {}

    public static void registerAll() {
        registerShuangXiGu();
        registerBingBaoGu();
        registerBingJiGu();
        registerLanNiaoBingGuanGu();
    }

    private static void registerShuangXiGu() {
        final String passive = "guzhenren:shuang_xi_gu_passive_frost_breath";
        final String active = "guzhenren:shuang_xi_gu_active_frost_breath";
        final String cooldownKey =
            "GuzhenrenExtCooldown_shuang_xi_gu_active_frost_breath";

        GuEffectRegistry.register(
            new BingXueDaoAttackProcDebuffEffect(
                passive,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new BingXueDaoActiveAoEBurstEffect(
                active,
                cooldownKey,
                List.of(
                    new BingXueDaoActiveAoEBurstEffect.EffectSpec(
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

    private static void registerBingBaoGu() {
        final String passive = "guzhenren:bing_bao_gu_passive_ice_charge";
        final String active = "guzhenren:bing_bao_gu_active_ice_explosion";
        final String passiveCooldownKey =
            "GuzhenrenExtCooldown_bing_bao_gu_passive_ice_charge";
        final String activeCooldownKey =
            "GuzhenrenExtCooldown_bing_bao_gu_active_ice_explosion";

        GuEffectRegistry.register(
            new BingXueDaoExplosionGuardEffect(passive, passiveCooldownKey)
        );
        GuEffectRegistry.register(
            new BingXueDaoActiveIceExplosionEffect(active, activeCooldownKey)
        );
    }

    private static void registerBingJiGu() {
        final String passive = "guzhenren:bing_ji_gu_passive_ice_body";
        final String active = "guzhenren:bing_ji_gu_active_ice_body";
        final String cooldownKey =
            "GuzhenrenExtCooldown_bing_ji_gu_active_ice_body";

        GuEffectRegistry.register(new BingXueDaoIceBodyEffect(passive));
        GuEffectRegistry.register(
            new ZhiDaoActiveSelfBuffEffect(
                active,
                cooldownKey,
                List.of(
                    new ZhiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resistance_duration_ticks",
                        0,
                        "resistance_amplifier",
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
    }

    private static void registerLanNiaoBingGuanGu() {
        final String passive =
            "guzhenren:lan_niao_bing_guan_gu_passive_lock_on";
        final String active =
            "guzhenren:lan_niao_bing_guan_gu_active_ice_coffin";
        final String cooldownKey =
            "GuzhenrenExtCooldown_lan_niao_bing_guan_gu_active_ice_coffin";

        GuEffectRegistry.register(
            new BingXueDaoAttackProcDebuffEffect(
                passive,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new BingXueDaoActiveIceCoffinEffect(active, cooldownKey)
        );
    }
}

