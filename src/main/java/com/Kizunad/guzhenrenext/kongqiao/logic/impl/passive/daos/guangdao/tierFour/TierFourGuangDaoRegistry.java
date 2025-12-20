package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common.GuangDaoActiveAreaPhysicalStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.guangdao.common.GuangDaoActiveTargetHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.common.GuangDaoSustainedAttackProcEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.guangdao.common.GuangDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 四转光道蛊虫效果注册。
 */
public final class TierFourGuangDaoRegistry {

    private TierFourGuangDaoRegistry() {}

    public static void registerAll() {
        registerHongBianGu();
        registerLvYaoGu();
    }

    private static void registerHongBianGu() {
        final String passive =
            "guzhenren:hongbiangu_passive_spectrum_edge";
        final String procCooldownKey =
            "GuzhenrenExtCooldown_hongbiangu_passive_spectrum_edge_proc";
        final String active =
            "guzhenren:hongbiangu_active_spectrum_burst";
        final String activeCooldownKey =
            "GuzhenrenExtCooldown_hongbiangu_active_spectrum_burst";

        GuEffectRegistry.register(
            new GuangDaoSustainedAttackProcEffect(passive, procCooldownKey)
        );
        GuEffectRegistry.register(
            new GuangDaoActiveAreaPhysicalStrikeEffect(active, activeCooldownKey)
        );
    }

    private static void registerLvYaoGu() {
        final String passive =
            "guzhenren:lvyaogu_passive_emerald_aegis";
        final String active =
            "guzhenren:lvyaogu_active_emerald_restoration";
        final String activeCooldownKey =
            "GuzhenrenExtCooldown_lvyaogu_active_emerald_restoration";

        GuEffectRegistry.register(
            new GuangDaoSustainedVariableCapEffect(
                passive,
                List.of(
                    new GuangDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
                        "hunpo_resistance_max_bonus"
                    ),
                    new GuangDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "niantou_capacity_bonus"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new GuangDaoActiveTargetHealEffect(
                active,
                activeCooldownKey,
                List.of(
                    new GuangDaoActiveTargetHealEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "regen_duration_ticks",
                        0,
                        "regen_amplifier",
                        0
                    ),
                    new GuangDaoActiveTargetHealEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resistance_duration_ticks",
                        0,
                        "resistance_amplifier",
                        0
                    )
                )
            )
        );
    }
}

