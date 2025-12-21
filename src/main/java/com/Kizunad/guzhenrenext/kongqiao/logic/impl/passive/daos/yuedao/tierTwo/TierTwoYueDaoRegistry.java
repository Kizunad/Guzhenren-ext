package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yuedao.common.YueDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yuedao.common.YueDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yuedao.common.YueDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.common.YueDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.common.YueDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.common.YueDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class TierTwoYueDaoRegistry {
    private TierTwoYueDaoRegistry() {}

    public static void registerAll() {
        // 月霓蛊：受击护体（偏防御）+ 群体护持
        GuEffectRegistry.register(
            new YueDaoHurtProcReductionEffect(
                "guzhenren:yue_ni_gu_passive_moon_barrier",
                cooldownKey("guzhenren:yue_ni_gu_passive_moon_barrier"),
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new YueDaoActiveAllySupportEffect(
                "guzhenren:yue_ni_gu_active_moon_guard",
                cooldownKey("guzhenren:yue_ni_gu_active_moon_guard"),
                List.of(
                    new YueDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YueDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );

        // 月旋蛊：持续提速（攻速）+ 旋斩范围爆发
        GuEffectRegistry.register(
            new YueDaoSustainedAttributeModifierEffect(
                "guzhenren:yue_xuan_gu_passive_moon_spin",
                Attributes.ATTACK_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_speed"
            )
        );
        GuEffectRegistry.register(
            new YueDaoActiveAoEBurstEffect(
                "guzhenren:yue_xuan_gu_active_moon_whirl",
                cooldownKey("guzhenren:yue_xuan_gu_active_moon_whirl"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );

        // 月芒蛊：攻击触发削弱 + 自我提振
        GuEffectRegistry.register(
            new YueDaoAttackProcDebuffEffect(
                "guzhenren:yue_mang_gu_passive_moon_edge",
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new YueDaoActiveSelfBuffEffect(
                "guzhenren:yue_mang_gu_active_moon_surge",
                cooldownKey("guzhenren:yue_mang_gu_active_moon_surge"),
                List.of(
                    new YueDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YueDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
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
