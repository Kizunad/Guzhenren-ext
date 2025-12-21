package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.rendao.common.RenDaoActiveAreaHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.rendao.common.RenDaoActiveTargetHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.common.RenDaoHurtProcCheatDeathEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.common.RenDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 四转人道。
 */
public final class TierFourRenDaoRegistry {

    private TierFourRenDaoRegistry() {}

    public static void registerAll() {
        registerQiSiRenGu();
        registerWeiLianHuaHuangJinSheLiGu();
    }

    private static void registerQiSiRenGu() {
        // 起死人蛊：濒死保命 + 强力定向治疗（模拟“起死人”）
        final String passiveUsageId =
            "guzhenren:qisiren_passive_cheat_death_4";
        GuEffectRegistry.register(
            new RenDaoHurtProcCheatDeathEffect(
                passiveUsageId,
                passiveCooldownKey(passiveUsageId),
                List.of(
                    new RenDaoHurtProcCheatDeathEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "regen_duration_ticks",
                        0,
                        "regen_amplifier",
                        0
                    ),
                    new RenDaoHurtProcCheatDeathEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resist_duration_ticks",
                        0,
                        "resist_amplifier",
                        0
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new RenDaoActiveTargetHealEffect(
                "guzhenren:qisiren_active_revive_like_heal_4",
                cooldownKey("guzhenren:qisiren_active_revive_like_heal_4"),
                List.of(
                    new RenDaoActiveTargetHealEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new RenDaoActiveTargetHealEffect.EffectSpec(
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

    private static void registerWeiLianHuaHuangJinSheLiGu() {
        // 未炼化黄金舍利蛊：拓宽空窍底蕴（上限）+ 范围滋养（群体治疗）
        GuEffectRegistry.register(
            new RenDaoSustainedVariableCapEffect(
                "guzhenren:wei_lian_hua_huang_jin_she_li_gu_passive_gold_cap_4",
                List.of(
                    new RenDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    ),
                    new RenDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "cap_max_jingli"
                    ),
                    new RenDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "cap_niantou_capacity"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new RenDaoActiveAreaHealEffect(
                "guzhenren:wei_lian_hua_huang_jin_she_li_gu_active_gold_nurture_4",
                cooldownKey(
                    "guzhenren:wei_lian_hua_huang_jin_she_li_gu_active_gold_nurture_4"
                ),
                List.of(
                    new RenDaoActiveAreaHealEffect.EffectSpec(
                        MobEffects.REGENERATION,
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

    private static String passiveCooldownKey(final String usageId) {
        return "GuzhenrenExtPassiveCd_" + usageId + "_cheat_death";
    }
}

