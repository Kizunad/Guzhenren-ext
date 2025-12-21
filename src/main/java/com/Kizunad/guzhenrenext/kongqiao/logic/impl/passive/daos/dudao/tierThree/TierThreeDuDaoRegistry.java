package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.dudao.common.DuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.dudao.common.DuDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.common.DuDaoAttackMarkDetonateEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.dudao.common.DuDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 三转毒道。
 */
public final class TierThreeDuDaoRegistry {

    private TierThreeDuDaoRegistry() {}

    public static void registerAll() {
        registerDuHeGu();
        registerHuDuGu();
    }

    private static void registerDuHeGu() {
        // 毒蝎蛊：毒体韧性（上限型持续）+ 毒蝎横扫（范围压制）
        GuEffectRegistry.register(
            new DuDaoSustainedVariableCapEffect(
                "guzhenren:du_he_gu_passive_scorpion_resilience",
                List.of(
                    new DuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
                        "cap_hunpo_resistance"
                    ),
                    new DuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "cap_niantou_capacity"
                    ),
                    new DuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
                        "cap_hunpo"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new DuDaoActiveAoEBurstEffect(
                "guzhenren:du_he_gu_active_scorpion_sweep",
                cooldownKey("guzhenren:du_he_gu_active_scorpion_sweep"),
                MobEffects.WEAKNESS
            )
        );
    }

    private static void registerHuDuGu() {
        // 虎毒蛊：虎毒后发（延迟引爆）+ 虎毒爆袭（指向强压）
        final String passiveId = "guzhenren:hu_du_gu_passive_tiger_poison";
        GuEffectRegistry.register(
            new DuDaoAttackMarkDetonateEffect(
                passiveId,
                cooldownKey(passiveId),
                markUntilKey(passiveId),
                markExpireKey(passiveId),
                List.of(MobEffects.WEAKNESS, MobEffects.MOVEMENT_SLOWDOWN)
            )
        );
        GuEffectRegistry.register(
            new DuDaoActiveTargetStrikeEffect(
                "guzhenren:hu_du_gu_active_tiger_poison_strike",
                cooldownKey("guzhenren:hu_du_gu_active_tiger_poison_strike"),
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

    private static String markUntilKey(final String usageId) {
        return "GuzhenrenExtDuDaoMarkUntil_" + usageId;
    }

    private static String markExpireKey(final String usageId) {
        return "GuzhenrenExtDuDaoMarkExpire_" + usageId;
    }
}

