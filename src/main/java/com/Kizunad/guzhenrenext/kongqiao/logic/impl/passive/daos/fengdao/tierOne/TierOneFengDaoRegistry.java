package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.tierOne;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.fengdao.common.FengDaoActiveWindStepEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.common.FengDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 一转风道蛊虫效果注册。
 */
public final class TierOneFengDaoRegistry {

    private TierOneFengDaoRegistry() {}

    public static void registerAll() {
        registerQingFengLunGu();
    }

    private static void registerQingFengLunGu() {
        final String passive =
            "guzhenren:qing_feng_lun_gu_passive_gentle_breeze";
        final String active =
            "guzhenren:qing_feng_lun_gu_active_wind_step";
        final String cooldownKey =
            "GuzhenrenExtCooldown_qing_feng_lun_gu_active_wind_step";

        GuEffectRegistry.register(
            new FengDaoSustainedAttributeModifierEffect(
                passive,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "speed"
            )
        );
        GuEffectRegistry.register(
            new FengDaoActiveWindStepEffect(
                active,
                cooldownKey,
                List.of(
                    new FengDaoActiveWindStepEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }
}
