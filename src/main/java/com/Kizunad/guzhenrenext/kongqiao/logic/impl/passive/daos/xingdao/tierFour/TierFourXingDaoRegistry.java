package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xingdao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xingdao.common.XingDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xingdao.common.XingDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 四转星道：四星立方。
 */
public final class TierFourXingDaoRegistry {

    private TierFourXingDaoRegistry() {}

    public static void registerAll() {
        // 四星立方蛊：大幅增幅 + 立方护阵
        GuEffectRegistry.register(
            new XingDaoSustainedAttributeModifierEffect(
                "guzhenren:sixinglifangti_passive_four_star_frame",
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new XingDaoActiveSelfBuffEffect(
                "guzhenren:sixinglifangti_active_four_star_array",
                cooldownKey("guzhenren:sixinglifangti_active_four_star_array"),
                List.of(
                    new XingDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new XingDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
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
