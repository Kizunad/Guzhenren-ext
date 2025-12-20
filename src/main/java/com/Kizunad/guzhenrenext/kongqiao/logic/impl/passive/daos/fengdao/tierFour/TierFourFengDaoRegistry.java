package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.fengdao.common.FengDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.fengdao.common.FengDaoActiveChargeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.fengdao.common.FengDaoActiveTargetDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.fengdao.common.FengDaoActiveWindStepEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.common.FengDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.common.FengDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.common.FengDaoWindBarrierOnHurtEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 四转风道蛊虫效果注册。
 */
public final class TierFourFengDaoRegistry {

    private TierFourFengDaoRegistry() {}

    public static void registerAll() {
        registerDaLongJuanFengGu();
        registerSiZhuanFengLiGu();
        registerFengShangGu();
        registerZhuiFengGu();
        registerNiFengZhanGu();
        registerJuChiFengRenGu();
    }

    private static void registerDaLongJuanFengGu() {
        final String passive =
            "guzhenren:dalongjuanfenggu_passive_wind_lift";
        final String active =
            "guzhenren:dalongjuanfenggu_active_dragon_tornado";
        final String cooldownKey =
            "GuzhenrenExtCooldown_dalongjuanfenggu_active_dragon_tornado";

        GuEffectRegistry.register(
            new FengDaoAttackProcDebuffEffect(passive, MobEffects.LEVITATION)
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

    private static void registerSiZhuanFengLiGu() {
        final String passive =
            "guzhenren:sizhuanfengligu_passive_wind_might";
        final String active = "guzhenren:sizhuanfengligu_active_wind_crash";
        final String cooldownKey =
            "GuzhenrenExtCooldown_sizhuanfengligu_active_wind_crash";

        GuEffectRegistry.register(
            new FengDaoSustainedAttributeModifierEffect(
                passive,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(new FengDaoActiveChargeEffect(active, cooldownKey));
    }

    private static void registerFengShangGu() {
        final String passive = "guzhenren:fengshanggu_passive_wind_wound";
        final String active = "guzhenren:fengshanggu_active_wind_wound";
        final String cooldownKey =
            "GuzhenrenExtCooldown_fengshanggu_active_wind_wound";

        GuEffectRegistry.register(
            new FengDaoAttackProcDebuffEffect(
                passive,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new FengDaoActiveTargetDebuffEffect(
                active,
                cooldownKey,
                List.of(
                    new FengDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "slow_duration_ticks",
                        0,
                        "slow_amplifier",
                        0
                    ),
                    new FengDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "weak_duration_ticks",
                        0,
                        "weak_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerZhuiFengGu() {
        final String passive = "guzhenren:zhui_feng_gu_passive_chase_wind";
        final String active = "guzhenren:zhui_feng_gu_active_chase_wind";
        final String cooldownKey =
            "GuzhenrenExtCooldown_zhui_feng_gu_active_chase_wind";

        GuEffectRegistry.register(
            new FengDaoSustainedAttributeModifierEffect(
                passive,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "speed"
            )
        );
        GuEffectRegistry.register(
            new FengDaoActiveWindStepEffect(
                active,
                cooldownKey,
                List.of(
                    new FengDaoActiveWindStepEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerNiFengZhanGu() {
        final String passive =
            "guzhenren:nifengzhangu_4_passive_counter_wind";
        final String active =
            "guzhenren:nifengzhangu_4_active_counter_wind";
        final String cooldownKey =
            "GuzhenrenExtCooldown_nifengzhangu_4_active_counter_wind";
        final String barrierCooldownKey =
            "GuzhenrenExtCooldown_nifengzhangu_4_passive_counter_wind";

        GuEffectRegistry.register(
            new FengDaoWindBarrierOnHurtEffect(passive, barrierCooldownKey)
        );
        GuEffectRegistry.register(
            new FengDaoActiveTargetDebuffEffect(
                active,
                cooldownKey,
                List.of(
                    new FengDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.DIG_SLOWDOWN,
                        "fatigue_duration_ticks",
                        0,
                        "fatigue_amplifier",
                        0
                    ),
                    new FengDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "weak_duration_ticks",
                        0,
                        "weak_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerJuChiFengRenGu() {
        final String passive =
            "guzhenren:juchifengrengu_passive_serrated_wind";
        final String active =
            "guzhenren:juchifengrengu_active_serrated_wind";
        final String cooldownKey =
            "GuzhenrenExtCooldown_juchifengrengu_active_serrated_wind";

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
                    ),
                    new FengDaoActiveTargetDebuffEffect.EffectSpec(
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
