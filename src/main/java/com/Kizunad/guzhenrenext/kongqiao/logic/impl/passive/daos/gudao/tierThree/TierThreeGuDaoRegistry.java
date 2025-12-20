package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.gudao.common.GuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.gudao.common.GuDaoActiveChargeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.gudao.common.GuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.gudao.common.GuDaoActiveTargetDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.common.GuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoHurtProcDebuffAttackerEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 三转骨道蛊虫效果注册。
 */
public final class TierThreeGuDaoRegistry {

    private TierThreeGuDaoRegistry() {}

    public static void registerAll() {
        registerWuZuNian();
        registerJinRenHuGuGu();
        registerRouBaiGu();
        registerGuCiGu();
        registerLeGuDunGu();
        registerBiGuYiGu();
        registerTieGuGu();
        registerFeiGuDunGu();
    }

    private static void registerWuZuNian() {
        final String passive = "guzhenren:wu_zu_nian_passive_bone_stride";
        final String active = "guzhenren:wu_zu_nian_active_bone_dash";
        final String cooldownKey =
            "GuzhenrenExtCooldown_wu_zu_nian_active_bone_dash";

        GuEffectRegistry.register(
            new ZhiDaoSustainedAttributeModifierEffect(
                passive,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                0.0,
                "move_speed"
            )
        );
        GuEffectRegistry.register(new GuDaoActiveChargeEffect(active, cooldownKey));
    }

    private static void registerJinRenHuGuGu() {
        final String passive =
            "guzhenren:jinrenhugugu_passive_golden_bone_cut";
        final String active =
            "guzhenren:jinrenhugugu_active_golden_sweep";
        final String cooldownKey =
            "GuzhenrenExtCooldown_jinrenhugugu_active_golden_sweep";

        GuEffectRegistry.register(
            new GuDaoAttackProcDebuffEffect(passive, MobEffects.WITHER)
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

    private static void registerRouBaiGu() {
        final String passive = "guzhenren:rou_bai_gu_passive_flesh_knit";
        final String active = "guzhenren:rou_bai_gu_active_white_marrow";
        final String cooldownKey =
            "GuzhenrenExtCooldown_rou_bai_gu_active_white_marrow";

        GuEffectRegistry.register(new ZhiDaoSustainedRegenEffect(passive));
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

    private static void registerGuCiGu() {
        final String passive = "guzhenren:gu_ci_gu_passive_spike_counter";
        final String active = "guzhenren:gu_ci_gu_active_spike_burst";
        final String cooldownKey =
            "GuzhenrenExtCooldown_gu_ci_gu_active_spike_burst";

        GuEffectRegistry.register(
            new ZhiDaoHurtProcDebuffAttackerEffect(
                passive,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new GuDaoActiveAoEBurstEffect(
                active,
                cooldownKey,
                List.of(
                    new GuDaoActiveAoEBurstEffect.EffectSpec(
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

    private static void registerLeGuDunGu() {
        final String passive = "guzhenren:le_gu_dun_gu_passive_rib_shield";
        final String active = "guzhenren:le_gu_dun_gu_active_rib_bulwark";
        final String cooldownKey =
            "GuzhenrenExtCooldown_le_gu_dun_gu_active_rib_bulwark";

        GuEffectRegistry.register(
            new ZhiDaoSustainedAttributeModifierEffect(
                passive,
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor"
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
                    )
                )
            )
        );
    }

    private static void registerBiGuYiGu() {
        final String passive = "guzhenren:bi_gu_yi_gu_passive_bone_wing";
        final String active = "guzhenren:bi_gu_yi_gu_active_wing_dash";
        final String cooldownKey =
            "GuzhenrenExtCooldown_bi_gu_yi_gu_active_wing_dash";

        GuEffectRegistry.register(
            new ZhiDaoSustainedAttributeModifierEffect(
                passive,
                Attributes.ATTACK_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                0.0,
                "attack_speed"
            )
        );
        GuEffectRegistry.register(new GuDaoActiveChargeEffect(active, cooldownKey));
    }

    private static void registerTieGuGu() {
        final String passive = "guzhenren:tie_gu_gu_passive_iron_bone";
        final String active = "guzhenren:tie_gu_gu_active_iron_hide";
        final String cooldownKey =
            "GuzhenrenExtCooldown_tie_gu_gu_active_iron_hide";

        GuEffectRegistry.register(
            new ZhiDaoSustainedAttributeModifierEffect(
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
                        MobEffects.DAMAGE_BOOST,
                        "strength_duration_ticks",
                        0,
                        "strength_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerFeiGuDunGu() {
        final String passive = "guzhenren:fei_gu_dun_gu_passive_flying_guard";
        final String active = "guzhenren:fei_gu_dun_gu_active_bone_orbit";
        final String cooldownKey =
            "GuzhenrenExtCooldown_fei_gu_dun_gu_active_bone_orbit";

        GuEffectRegistry.register(
            new ZhiDaoHurtProcDebuffAttackerEffect(passive, MobEffects.WEAKNESS)
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
                    )
                )
            )
        );
    }
}

