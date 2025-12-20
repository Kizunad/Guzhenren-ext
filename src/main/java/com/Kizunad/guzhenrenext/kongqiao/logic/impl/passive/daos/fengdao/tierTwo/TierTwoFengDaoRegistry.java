package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.fengdao.common.FengDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.fengdao.common.FengDaoActiveTargetDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.common.FengDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.fengdao.common.FengDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 二转风道蛊虫效果注册。
 */
public final class TierTwoFengDaoRegistry {

    private TierTwoFengDaoRegistry() {}

    public static void registerAll() {
        registerXuanFengLunGu();
        registerBuFengGu();
    }

    private static void registerXuanFengLunGu() {
        final String passive = "guzhenren:xuan_feng_lun_gu_passive_swirl_cut";
        final String active = "guzhenren:xuan_feng_lun_gu_active_swirl_blast";
        final String cooldownKey =
            "GuzhenrenExtCooldown_xuan_feng_lun_gu_active_swirl_blast";

        GuEffectRegistry.register(
            new FengDaoAttackProcDebuffEffect(
                passive,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new FengDaoActiveAoEBurstEffect(
                active,
                cooldownKey,
                List.of(
                    new FengDaoActiveAoEBurstEffect.EffectSpec(
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

    private static void registerBuFengGu() {
        final String passive = "guzhenren:bufenggu_passive_wind_capture";
        final String active = "guzhenren:bufenggu_active_wind_bind";
        final String cooldownKey =
            "GuzhenrenExtCooldown_bufenggu_active_wind_bind";

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
            new FengDaoActiveTargetDebuffEffect(
                active,
                cooldownKey,
                List.of(
                    new FengDaoActiveTargetDebuffEffect.EffectSpec(
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
}
