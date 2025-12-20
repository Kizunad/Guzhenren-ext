package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.gudao.common.GuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.gudao.common.GuDaoActiveTargetDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.common.GuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.common.GuDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 二转骨道蛊虫效果注册。
 */
public final class TierTwoGuDaoRegistry {

    private TierTwoGuDaoRegistry() {}

    public static void registerAll() {
        registerLuoXuanGuQiangGu();
        registerYuGuGu();
        registerHuGuGu();
    }

    private static void registerLuoXuanGuQiangGu() {
        final String passive =
            "guzhenren:luo_xuan_gu_qiang_gu_passive_spiral_pierce";
        final String active =
            "guzhenren:luo_xuan_gu_qiang_gu_active_spiral_shot";
        final String cooldownKey =
            "GuzhenrenExtCooldown_luo_xuan_gu_qiang_gu_active_spiral_shot";

        GuEffectRegistry.register(
            new GuDaoAttackProcDebuffEffect(passive, MobEffects.WEAKNESS)
        );
        GuEffectRegistry.register(
            new GuDaoActiveTargetDebuffEffect(
                active,
                cooldownKey,
                List.of(
                    new GuDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new GuDaoActiveTargetDebuffEffect.EffectSpec(
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

    private static void registerYuGuGu() {
        final String passive = "guzhenren:yu_gu_gu_passive_jade_bone";
        final String active = "guzhenren:yu_gu_gu_active_jade_guard";
        final String cooldownKey =
            "GuzhenrenExtCooldown_yu_gu_gu_active_jade_guard";

        GuEffectRegistry.register(
            new GuDaoSustainedAttributeModifierEffect(
                passive,
                Attributes.ARMOR_TOUGHNESS,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor_toughness"
            )
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

    private static void registerHuGuGu() {
        final String passive = "guzhenren:hugugu_passive_tiger_bone";
        final String active = "guzhenren:hugugu_active_tiger_fury";
        final String cooldownKey =
            "GuzhenrenExtCooldown_hugugu_active_tiger_fury";

        GuEffectRegistry.register(
            new GuDaoSustainedAttributeModifierEffect(
                passive,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new GuDaoActiveSelfBuffEffect(
                active,
                cooldownKey,
                List.of(
                    new GuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
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
