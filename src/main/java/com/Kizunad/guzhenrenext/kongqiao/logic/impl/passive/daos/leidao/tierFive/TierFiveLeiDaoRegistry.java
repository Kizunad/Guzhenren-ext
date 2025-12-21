package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.leidao.common.LeiDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.leidao.common.LeiDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 五转雷道。
 */
public final class TierFiveLeiDaoRegistry {

    private TierFiveLeiDaoRegistry() {}

    public static void registerAll() {
        // 未炼化雷锤蛊：镇元扩容（高转）+ 雷锤碎敌（单点重击 + 溅射）
        GuEffectRegistry.register(
            new LeiDaoSustainedVariableCapEffect(
                "guzhenren:weilianhualeichuigu_passive_thunder_hammer_cap",
                List.of(
                    new LeiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    ),
                    new LeiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "cap_max_jingli"
                    ),
                    new LeiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
                        "cap_max_hunpo"
                    ),
                    new LeiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
                        "cap_hunpo_resistance"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new LeiDaoActiveTargetStrikeEffect(
                "guzhenren:weilianhualeichuigu_active_thunder_hammer_crush",
                cooldownKey("guzhenren:weilianhualeichuigu_active_thunder_hammer_crush"),
                List.of(
                    new LeiDaoActiveTargetStrikeEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "slow_duration_ticks",
                        0,
                        "slow_amplifier",
                        0
                    ),
                    new LeiDaoActiveTargetStrikeEffect.EffectSpec(
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

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

