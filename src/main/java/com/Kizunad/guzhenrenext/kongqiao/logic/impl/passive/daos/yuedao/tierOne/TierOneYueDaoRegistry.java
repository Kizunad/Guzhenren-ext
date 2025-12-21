package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.tierOne;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yuedao.common.YueDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.common.YueDaoSustainedMobEffectEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

public final class TierOneYueDaoRegistry {
    private TierOneYueDaoRegistry() {}

    public static void registerAll() {
        // 月光蛊：视野强化（持续）+ 小型月刃点杀
        GuEffectRegistry.register(
            new YueDaoSustainedMobEffectEffect(
                "guzhenren:yue_guang_gu_passive_moon_sight",
                MobEffects.NIGHT_VISION
            )
        );
        GuEffectRegistry.register(
            new YueDaoActiveTargetNukeEffect(
                "guzhenren:yue_guang_gu_active_moon_cut",
                cooldownKey("guzhenren:yue_guang_gu_active_moon_cut"),
                List.of(
                    new YueDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.GLOWING,
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
