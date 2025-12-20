package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveTargetDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.tierTwo.DaDaHuiGuShazhaoDeriveEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.tierTwo.DaZhiGuBestDeriveShazhaoEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoHurtProcDebuffAttackerEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoSustainedRegenEffect;

import java.util.List;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 二转智道蛊虫效果注册。
 */
public class TierTwoZhiDaoRegistry {

    private TierTwoZhiDaoRegistry() {}

    public static void registerAll() {
        final String zhiliaoguPassive = "guzhenren:zhiliaogu_passive_wisdom_heal";
        final String zhiliaoguActive = "guzhenren:zhiliaogu_active_wisdom_mend";
        final String zhiliaoguCooldownKey =
            "GuzhenrenExtCooldown_zhiliaogu_active_wisdom_mend";

        final String erzhuanzhanyiguPassive =
            "guzhenren:erzhuanzhanyigu_passive_battle_stride";
        final String erzhuanzhanyiguActive =
            "guzhenren:erzhuanzhanyigu_active_battle_surge";
        final String erzhuanzhanyiguCooldownKey =
            "GuzhenrenExtCooldown_erzhuanzhanyigu_active_battle_surge";

        final String erzhuanenianguPassive =
            "guzhenren:erzhuaneniangu_passive_evil_wither";
        final String erzhuanenianguActive =
            "guzhenren:erzhuaneniangu_active_evil_spike";
        final String erzhuanenianguCooldownKey =
            "GuzhenrenExtCooldown_erzhuaneniangu_active_evil_spike";

        final String erzhuanshexinguPassive =
            "guzhenren:erzhuanshexingu_passive_mind_counter";
        final String erzhuanshexinguActive =
            "guzhenren:erzhuanshexingu_active_mind_bind";
        final String erzhuanshexinguCooldownKey =
            "GuzhenrenExtCooldown_erzhuanshexingu_active_mind_bind";

        GuEffectRegistry.register(new DaDaHuiGuWisdomDeductionEffect());
        GuEffectRegistry.register(new DaDaHuiGuShazhaoDeriveEffect());
        GuEffectRegistry.register(new DaZhiGuWisdomStrategyEffect());
        GuEffectRegistry.register(new DaZhiGuBestDeriveShazhaoEffect());

        GuEffectRegistry.register(new ZhiDaoSustainedRegenEffect(zhiliaoguPassive));
        GuEffectRegistry.register(
            new ZhiDaoActiveSelfBuffEffect(
                zhiliaoguActive,
                zhiliaoguCooldownKey,
                List.of(
                    new ZhiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new ZhiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "absorption_duration_ticks",
                        0,
                        "absorption_amplifier",
                        0
                    )
                )
            )
        );

        GuEffectRegistry.register(
            new ZhiDaoSustainedAttributeModifierEffect(
                erzhuanzhanyiguPassive,
                Attributes.ATTACK_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                0.0,
                "attack_speed"
            )
        );
        GuEffectRegistry.register(
            new ZhiDaoActiveSelfBuffEffect(
                erzhuanzhanyiguActive,
                erzhuanzhanyiguCooldownKey,
                List.of(
                    new ZhiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new ZhiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resistance_duration_ticks",
                        0,
                        "resistance_amplifier",
                        0
                    )
                )
            )
        );

        GuEffectRegistry.register(
            new ZhiDaoAttackProcDebuffEffect(
                erzhuanenianguPassive,
                MobEffects.WITHER
            )
        );
        GuEffectRegistry.register(
            new ZhiDaoActiveTargetDebuffEffect(
                erzhuanenianguActive,
                erzhuanenianguCooldownKey,
                List.of(
                    new ZhiDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.WITHER,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new ZhiDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "slow_duration_ticks",
                        0,
                        "slow_amplifier",
                        0
                    )
                )
            )
        );

        GuEffectRegistry.register(
            new ZhiDaoHurtProcDebuffAttackerEffect(
                erzhuanshexinguPassive,
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new ZhiDaoActiveTargetDebuffEffect(
                erzhuanshexinguActive,
                erzhuanshexinguCooldownKey,
                List.of(
                    new ZhiDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new ZhiDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "weakness_duration_ticks",
                        0,
                        "weakness_amplifier",
                        0
                    )
                )
            )
        );
    }
}
