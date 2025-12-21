package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jindao.common.JinDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jindao.common.JinDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.common.JinDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.common.JinDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.common.JinDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 五转金道。
 */
public final class TierFiveJinDaoRegistry {

    private TierFiveJinDaoRegistry() {}

    public static void registerAll() {
        registerTransmutation();
        registerVajraFury();
        registerIronHandCapture();
    }

    private static void registerTransmutation() {
        // 点金蛊：提高上限（高转）+ 点金夺势
        GuEffectRegistry.register(
            new JinDaoSustainedVariableCapEffect(
                "guzhenren:dian_jin_gu_passive_transmutation_cap_5",
                List.of(
                    new JinDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    ),
                    new JinDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "cap_niantou_capacity"
                    ),
                    new JinDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
                        "cap_max_hunpo_resistance"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveTargetNukeEffect(
                "guzhenren:dian_jin_gu_active_gilded_judgment_5",
                cooldownKey("guzhenren:dian_jin_gu_active_gilded_judgment_5"),
                List.of(
                    new JinDaoActiveTargetNukeEffect.EffectSpec(
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

    private static void registerVajraFury() {
        // 怒目金刚蛊：受击反震/减伤 + 金刚怒目
        GuEffectRegistry.register(
            new JinDaoHurtProcReductionEffect(
                "guzhenren:nu_mu_jin_gang_gu_passive_vajra_counter_5",
                cooldownKey("guzhenren:nu_mu_jin_gang_gu_passive_vajra_counter_5"),
                MobEffects.DAMAGE_BOOST
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveSelfBuffEffect(
                "guzhenren:nu_mu_jin_gang_gu_active_vajra_fury_5",
                cooldownKey("guzhenren:nu_mu_jin_gang_gu_active_vajra_fury_5"),
                List.of(
                    new JinDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "strength_duration_ticks",
                        0,
                        "strength_amplifier",
                        0
                    ),
                    new JinDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resist_duration_ticks",
                        0,
                        "resist_amplifier",
                        0
                    ),
                    new JinDaoActiveSelfBuffEffect.EffectSpec(
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

    private static void registerIronHandCapture() {
        // 铁手擒拿蛊：攻击触发束缚 + 擒拿牵引
        GuEffectRegistry.register(
            new JinDaoAttackProcDebuffEffect(
                "guzhenren:tie_shou_qin_na_gu_passive_iron_grapple_5",
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveTargetNukeEffect(
                "guzhenren:tie_shou_qin_na_gu_active_iron_capture_5",
                cooldownKey("guzhenren:tie_shou_qin_na_gu_active_iron_capture_5"),
                List.of(
                    new JinDaoActiveTargetNukeEffect.EffectSpec(
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

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

