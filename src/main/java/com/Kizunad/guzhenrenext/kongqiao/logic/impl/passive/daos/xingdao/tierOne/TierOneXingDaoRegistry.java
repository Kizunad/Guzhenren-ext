package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xingdao.tierOne;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xingdao.common.XingDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.xingdao.common.XingDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xingdao.common.XingDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xingdao.common.XingDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 一转星道：星光/半点。
 */
public final class TierOneXingDaoRegistry {

    private TierOneXingDaoRegistry() {}

    public static void registerAll() {
        // 星光蛊：星辉照耀（显形）+ 范围爆闪
        GuEffectRegistry.register(
            new XingDaoAttackProcDebuffEffect(
                "guzhenren:xingguanggu_passive_starlight_mark",
                MobEffects.GLOWING
            )
        );
        GuEffectRegistry.register(
            new XingDaoActiveAoEBurstEffect(
                "guzhenren:xingguanggu_active_starlight_burst",
                cooldownKey("guzhenren:xingguanggu_active_starlight_burst"),
                MobEffects.GLOWING
            )
        );

        // 一星半点蛊：小幅增幅 + 短促爆发
        GuEffectRegistry.register(
            new XingDaoSustainedAttributeModifierEffect(
                "guzhenren:xing_ban_dian_gu_passive_star_boost",
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new XingDaoActiveSelfBuffEffect(
                "guzhenren:xing_ban_dian_gu_active_star_spark",
                cooldownKey("guzhenren:xing_ban_dian_gu_active_star_spark"),
                List.of(
                    new XingDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new XingDaoActiveSelfBuffEffect.EffectSpec(
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

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
