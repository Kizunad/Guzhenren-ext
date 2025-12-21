package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yuedao.common.YueDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yuedao.common.YueDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.common.YueDaoSustainedMobEffectEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.common.YueDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

public final class TierFourYueDaoRegistry {
    private TierFourYueDaoRegistry() {}

    public static void registerAll() {
        // 焕月蛊：高转字段上限 + 团队回复
        GuEffectRegistry.register(
            new YueDaoSustainedVariableCapEffect(
                "guzhenren:huahuanyuegu_passive_moon_renew_caps",
                List.of(
                    new YueDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    ),
                    new YueDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "cap_niantou_capacity"
                    ),
                    new YueDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "cap_max_jingli"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new YueDaoActiveAllySupportEffect(
                "guzhenren:huahuanyuegu_active_moon_renew",
                cooldownKey("guzhenren:huahuanyuegu_active_moon_renew"),
                List.of(
                    new YueDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YueDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );

        // 月影蛊：身法（月影疾行）+ 影步位移
        GuEffectRegistry.register(
            new YueDaoSustainedMobEffectEffect(
                "guzhenren:yueyinggu_passive_moon_shadow",
                MobEffects.MOVEMENT_SPEED
            )
        );
        GuEffectRegistry.register(
            new YueDaoActiveBlinkEffect(
                "guzhenren:yueyinggu_active_shadow_step",
                cooldownKey("guzhenren:yueyinggu_active_shadow_step"),
                List.of(
                    new YueDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.INVISIBILITY,
                        "invis_duration_ticks",
                        0,
                        "invis_amplifier",
                        0
                    ),
                    new YueDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        0,
                        "speed_amplifier",
                        1
                    )
                )
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
