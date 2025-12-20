package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.bingxuedao.common.BingXueDaoActiveTargetDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.bingxuedao.common.BingXueDaoRangedFrostArrowEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 四转冰雪道蛊虫效果注册。
 */
public final class TierFourBingXueDaoRegistry {

    private TierFourBingXueDaoRegistry() {}

    public static void registerAll() {
        registerShuangJianGu();
    }

    private static void registerShuangJianGu() {
        final String passive = "guzhenren:shuang_jian_gu_passive_frost_arrow";
        final String active = "guzhenren:shuang_jian_gu_active_frost_arrow";
        final String cooldownKey =
            "GuzhenrenExtCooldown_shuang_jian_gu_active_frost_arrow";

        GuEffectRegistry.register(new BingXueDaoRangedFrostArrowEffect(passive));
        GuEffectRegistry.register(
            new BingXueDaoActiveTargetDebuffEffect(
                active,
                cooldownKey,
                List.of(
                    new BingXueDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new BingXueDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.GLOWING,
                        "glowing_duration_ticks",
                        0,
                        "glowing_amplifier",
                        0
                    )
                )
            )
        );
    }
}

