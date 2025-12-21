package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.daodao.common.DaoDaoActiveBladeLightPierceEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.daodao.common.DaoDaoSustainedCapRegenGuardEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 五转刀道：拔刀斩（极致）。
 */
public final class TierFiveDaoDaoRegistry {

    private static final int DEFAULT_EFFECT_TICKS = 100;

    private TierFiveDaoDaoRegistry() {}

    public static void registerAll() {
        registerBaDaoZhanGuFive();
    }

    private static void registerBaDaoZhanGuFive() {
        // 五转拔刀斩蛊：被动提升真元/魂魄抗性上限并少量回气 + 主动超远刀光
        GuEffectRegistry.register(
            new DaoDaoSustainedCapRegenGuardEffect(
                "guzhenren:ba_dao_zhan_gu_5_passive_dao_intent_return",
                List.of(
                    new DaoDaoSustainedCapRegenGuardEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    ),
                    new DaoDaoSustainedCapRegenGuardEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
                        "cap_max_hunpo_resistance"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new DaoDaoActiveBladeLightPierceEffect(
                "guzhenren:ba_dao_zhan_gu_5_active_draw_slash_heaven",
                cooldownKey("guzhenren:ba_dao_zhan_gu_5_active_draw_slash_heaven"),
                List.of(
                    new DaoDaoActiveBladeLightPierceEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        DEFAULT_EFFECT_TICKS,
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

