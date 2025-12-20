package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.gudao.common.GuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.gudao.common.GuDaoActiveChargeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.gudao.common.GuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.gudao.common.GuDaoActiveTargetDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.common.GuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.common.GuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.common.GuDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 四转骨道蛊虫效果注册。
 */
public final class TierFourGuDaoRegistry {

    private TierFourGuDaoRegistry() {}

    public static void registerAll() {
        registerWuChangGuGu();
        registerGuRouTuanYuanGu();
        registerJingTieGuGu();
        registerWeiLianHuaGuYiGu();
        registerXiangYaBaiJiaGu();
    }

    private static void registerWuChangGuGu() {
        final String passive = "guzhenren:wuchanggugu_passive_impermanence";
        final String active = "guzhenren:wuchanggugu_active_bone_decay";
        final String cooldownKey =
            "GuzhenrenExtCooldown_wuchanggugu_active_bone_decay";

        GuEffectRegistry.register(
            new GuDaoAttackProcDebuffEffect(passive, MobEffects.WITHER)
        );
        GuEffectRegistry.register(
            new GuDaoActiveTargetDebuffEffect(
                active,
                cooldownKey,
                List.of(
                    new GuDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.WITHER,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new GuDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.BLINDNESS,
                        "blind_duration_ticks",
                        0,
                        "blind_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerGuRouTuanYuanGu() {
        final String passive =
            "guzhenren:guroutuanyuangu_passive_reunion_knit";
        final String active =
            "guzhenren:guroutuanyuangu_active_reunion_blessing";
        final String cooldownKey =
            "GuzhenrenExtCooldown_guroutuanyuangu_active_reunion_blessing";

        GuEffectRegistry.register(new GuDaoSustainedRegenEffect(passive));
        GuEffectRegistry.register(
            new GuDaoActiveSelfBuffEffect(
                active,
                cooldownKey,
                List.of(
                    new GuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new GuDaoActiveSelfBuffEffect.EffectSpec(
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

    private static void registerJingTieGuGu() {
        final String passive = "guzhenren:jingtiegugu_passive_refined_bone";
        final String active = "guzhenren:jingtiegugu_active_iron_shrapnel";
        final String cooldownKey =
            "GuzhenrenExtCooldown_jingtiegugu_active_iron_shrapnel";

        GuEffectRegistry.register(
            new GuDaoSustainedAttributeModifierEffect(
                passive,
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor"
            )
        );
        GuEffectRegistry.register(
            new GuDaoActiveAoEBurstEffect(
                active,
                cooldownKey,
                List.of(
                    new GuDaoActiveAoEBurstEffect.EffectSpec(
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

    private static void registerWeiLianHuaGuYiGu() {
        final String passive =
            "guzhenren:weilianhuaguyigu_passive_unrefined_wing";
        final String active =
            "guzhenren:weilianhuaguyigu_active_unrefined_dash";
        final String cooldownKey =
            "GuzhenrenExtCooldown_weilianhuaguyigu_active_unrefined_dash";

        GuEffectRegistry.register(
            new GuDaoSustainedAttributeModifierEffect(
                passive,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                0.0,
                "move_speed"
            )
        );
        GuEffectRegistry.register(new GuDaoActiveChargeEffect(active, cooldownKey));
    }

    private static void registerXiangYaBaiJiaGu() {
        final String passive =
            "guzhenren:weilianhuaxiangyaibaijiagu_passive_ivory_plate";
        final String active =
            "guzhenren:weilianhuaxiangyaibaijiagu_active_ivory_bulwark";
        final String cooldownKey =
            "GuzhenrenExtCooldown_weilianhuaxiangyaibaijiagu_active_ivory_bulwark";

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
                        MobEffects.ABSORPTION,
                        "absorption_duration_ticks",
                        0,
                        "absorption_amplifier",
                        0
                    ),
                    new GuDaoActiveSelfBuffEffect.EffectSpec(
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
