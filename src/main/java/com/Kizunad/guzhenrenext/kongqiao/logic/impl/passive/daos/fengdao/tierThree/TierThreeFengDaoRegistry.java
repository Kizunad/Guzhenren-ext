package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.fengdao.common.FengDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.fengdao.common.FengDaoActiveChargeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.fengdao.common.FengDaoActiveTargetDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.gudao.common.GuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.common.FengDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.common.FengDaoWindBarrierOnHurtEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedMobEffectEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 三转风道蛊虫效果注册。
 */
public final class TierThreeFengDaoRegistry {

    private TierThreeFengDaoRegistry() {}

    public static void registerAll() {
        registerLongJuanFengGu();
        registerFengLiGu();
        registerFengRenGu();
        registerKuangFengGu();
        registerBiFengLunGu();
        registerSanZhuanBuFengGu();
        registerNianFengZhanGu();
        registerWeiFengGu();
    }

    private static void registerLongJuanFengGu() {
        final String passive =
            "guzhenren:long_juan_feng_gu_passive_wind_shelter";
        final String active =
            "guzhenren:long_juan_feng_gu_active_tornado_field";
        final String cooldownKey =
            "GuzhenrenExtCooldown_long_juan_feng_gu_active_tornado_field";
        final String barrierCooldownKey =
            "GuzhenrenExtCooldown_long_juan_feng_gu_passive_wind_shelter";

        GuEffectRegistry.register(
            new FengDaoWindBarrierOnHurtEffect(passive, barrierCooldownKey)
        );
        GuEffectRegistry.register(
            new FengDaoActiveAoEBurstEffect(
                active,
                cooldownKey,
                List.of(
                    new FengDaoActiveAoEBurstEffect.EffectSpec(
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

    private static void registerFengLiGu() {
        final String passive = "guzhenren:fengligu_passive_wind_force";
        final String active = "guzhenren:fengligu_active_gale_charge";
        final String cooldownKey =
            "GuzhenrenExtCooldown_fengligu_active_gale_charge";

        GuEffectRegistry.register(
            new ZhiDaoSustainedAttributeModifierEffect(
                passive,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(new FengDaoActiveChargeEffect(active, cooldownKey));
    }

    private static void registerFengRenGu() {
        final String passive = "guzhenren:fengrengu_passive_wind_edge";
        final String active = "guzhenren:fengrengu_active_wind_slash";
        final String cooldownKey =
            "GuzhenrenExtCooldown_fengrengu_active_wind_slash";

        GuEffectRegistry.register(
            new FengDaoAttackProcDebuffEffect(passive, MobEffects.WEAKNESS)
        );
        GuEffectRegistry.register(
            new FengDaoActiveTargetDebuffEffect(
                active,
                cooldownKey,
                List.of(
                    new FengDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerKuangFengGu() {
        final String passive = "guzhenren:kuangfenggu_passive_wild_gale";
        final String active = "guzhenren:kuangfenggu_active_wild_gale";
        final String cooldownKey =
            "GuzhenrenExtCooldown_kuangfenggu_active_wild_gale";

        GuEffectRegistry.register(
            new ZhiDaoSustainedAttributeModifierEffect(
                passive,
                Attributes.ATTACK_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_speed"
            )
        );
        GuEffectRegistry.register(
            new GuDaoActiveSelfBuffEffect(
                active,
                cooldownKey,
                List.of(
                    new GuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        0,
                        "speed_amplifier",
                        0
                    ),
                    new GuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DIG_SPEED,
                        "haste_duration_ticks",
                        0,
                        "haste_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerBiFengLunGu() {
        final String passive = "guzhenren:bifenglungu_passive_wind_barrier";
        final String active = "guzhenren:bifenglungu_active_wind_barrier";
        final String cooldownKey =
            "GuzhenrenExtCooldown_bifenglungu_active_wind_barrier";
        final String barrierCooldownKey =
            "GuzhenrenExtCooldown_bifenglungu_passive_wind_barrier";

        GuEffectRegistry.register(
            new FengDaoWindBarrierOnHurtEffect(passive, barrierCooldownKey)
        );
        GuEffectRegistry.register(
            new GuDaoActiveSelfBuffEffect(
                active,
                cooldownKey,
                List.of(
                    new GuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new GuDaoActiveSelfBuffEffect.EffectSpec(
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

    private static void registerSanZhuanBuFengGu() {
        final String passive =
            "guzhenren:sanzhuanbufenggu_passive_wind_net";
        final String active = "guzhenren:sanzhuanbufenggu_active_wind_net";
        final String cooldownKey =
            "GuzhenrenExtCooldown_sanzhuanbufenggu_active_wind_net";

        GuEffectRegistry.register(
            new FengDaoAttackProcDebuffEffect(
                passive,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new FengDaoActiveAoEBurstEffect(
                active,
                cooldownKey,
                List.of(
                    new FengDaoActiveAoEBurstEffect.EffectSpec(
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

    private static void registerNianFengZhanGu() {
        final String passive =
            "guzhenren:nianfengzhangu_passive_mind_wind";
        final String active = "guzhenren:nianfengzhangu_active_mind_slash";
        final String cooldownKey =
            "GuzhenrenExtCooldown_nianfengzhangu_active_mind_slash";

        GuEffectRegistry.register(
            new FengDaoAttackProcDebuffEffect(passive, MobEffects.WEAKNESS)
        );
        GuEffectRegistry.register(
            new FengDaoActiveTargetDebuffEffect(
                active,
                cooldownKey,
                List.of(
                    new FengDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.BLINDNESS,
                        "blind_duration_ticks",
                        0,
                        "blind_amplifier",
                        0
                    ),
                    new FengDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerWeiFengGu() {
        final String passive = "guzhenren:weifenggu_passive_soft_fall";
        final String active = "guzhenren:weifenggu_active_soft_fall";
        final String cooldownKey =
            "GuzhenrenExtCooldown_weifenggu_active_soft_fall";

        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                passive,
                MobEffects.SLOW_FALLING
            )
        );
        GuEffectRegistry.register(
            new GuDaoActiveSelfBuffEffect(
                active,
                cooldownKey,
                List.of(
                    new GuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.SLOW_FALLING,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new GuDaoActiveSelfBuffEffect.EffectSpec(
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
}
