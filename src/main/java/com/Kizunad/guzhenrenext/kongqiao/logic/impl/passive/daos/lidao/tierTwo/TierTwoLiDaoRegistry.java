package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 二转力道。
 */
public final class TierTwoLiDaoRegistry {
    private TierTwoLiDaoRegistry() {}

    public static void registerAll() {
        // 恶力蛊：恶力侵体（攻触发压制）+ 破势重击
        GuEffectRegistry.register(
            new LiDaoAttackProcDebuffEffect(
                "guzhenren:e_li_gu_passive_evil_pressure",
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveTargetNukeEffect(
                "guzhenren:e_li_gu_active_evil_crush",
                cooldownKey("guzhenren:e_li_gu_active_evil_crush"),
                List.of(
                    new LiDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "slow_duration_ticks",
                        0,
                        "slow_amplifier",
                        0
                    )
                )
            )
        );

        // 十斤力蛊：稳力固身（攻击增幅）+ 范围震荡
        GuEffectRegistry.register(
            new LiDaoSustainedAttributeModifierEffect(
                "guzhenren:shi_jin_li_gu_passive_steady_force",
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveAoEBurstEffect(
                "guzhenren:shi_jin_li_gu_active_ten_jin_quake",
                cooldownKey("guzhenren:shi_jin_li_gu_active_ten_jin_quake"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );

        // 自力更生蛊：自给回气（持续）+ 提振爆发
        GuEffectRegistry.register(
            new LiDaoSustainedRegenEffect(
                "guzhenren:zi_li_geng_sheng_gu_passive_self_reliance"
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveSelfBuffEffect(
                "guzhenren:zi_li_geng_sheng_gu_active_self_uplift",
                cooldownKey("guzhenren:zi_li_geng_sheng_gu_active_self_uplift"),
                List.of(
                    new LiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new LiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resist_duration_ticks",
                        0,
                        "resist_amplifier",
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

