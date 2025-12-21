package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yuedao.common.YueDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yuedao.common.YueDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.common.YueDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.common.YueDaoSustainedRegenEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.common.YueDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

public final class TierFiveYueDaoRegistry {
    private TierFiveYueDaoRegistry() {}

    public static void registerAll() {
        // 吞月蛊：高转字段上限（偏魂魄）+ 吞月重击（单体爆发）
        GuEffectRegistry.register(
            new YueDaoSustainedVariableCapEffect(
                "guzhenren:tunyuegu_passive_swallow_caps",
                List.of(
                    new YueDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
                        "cap_max_hunpo"
                    ),
                    new YueDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
                        "cap_max_hunpo_resistance"
                    ),
                    new YueDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new YueDaoActiveTargetNukeEffect(
                "guzhenren:tunyuegu_active_swallow_moon",
                cooldownKey("guzhenren:tunyuegu_active_swallow_moon"),
                List.of(
                    new YueDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        1
                    )
                )
            )
        );

        // 镜月蛊（未炼化条目）：定身月刃 + 极限单体斩杀
        GuEffectRegistry.register(
            new YueDaoAttackProcDebuffEffect(
                "guzhenren:weilianhuajingyuegu_passive_mirror_bind",
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new YueDaoActiveTargetNukeEffect(
                "guzhenren:weilianhuajingyuegu_active_mirror_lock",
                cooldownKey("guzhenren:weilianhuajingyuegu_active_mirror_lock"),
                List.of(
                    new YueDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "effect_duration_ticks",
                        0,
                        "slow_amplifier",
                        0
                    ),
                    new YueDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        0,
                        "weakness_amplifier",
                        0
                    )
                )
            )
        );

        // 水月蛊：持续滋养 + 团队水月护持
        GuEffectRegistry.register(
            new YueDaoSustainedRegenEffect(
                "guzhenren:shuiyuegu_passive_water_moon_regen"
            )
        );
        GuEffectRegistry.register(
            new YueDaoActiveAllySupportEffect(
                "guzhenren:shuiyuegu_active_water_moon_bless",
                cooldownKey("guzhenren:shuiyuegu_active_water_moon_bless"),
                List.of(
                    new YueDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "regen_amplifier",
                        1
                    ),
                    new YueDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "resistance_amplifier",
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
