package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.dudao.common.DuDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.common.DuDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 五转毒道。
 */
public final class TierFiveDuDaoRegistry {

    private TierFiveDuDaoRegistry() {}

    public static void registerAll() {
        registerBiKongGu();
    }

    private static void registerBiKongGu() {
        // 碧空蛊：碧空毒体（上限型持续）+ 碧空绝杀（指向重击）
        GuEffectRegistry.register(
            new DuDaoSustainedVariableCapEffect(
                "guzhenren:bi_kong_gu_passive_azure_toxin_body",
                List.of(
                    new DuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_zhenyuan"
                    ),
                    new DuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "cap_jingli"
                    ),
                    new DuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
                        "cap_hunpo"
                    ),
                    new DuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
                        "cap_hunpo_resistance"
                    ),
                    new DuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "cap_niantou_capacity"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new DuDaoActiveTargetStrikeEffect(
                "guzhenren:bi_kong_gu_active_azure_execution",
                cooldownKey("guzhenren:bi_kong_gu_active_azure_execution"),
                List.of(
                    new DuDaoActiveTargetStrikeEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new DuDaoActiveTargetStrikeEffect.EffectSpec(
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

