package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierOne;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveTargetDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.tierOne.XiaoHuiGuGuidedIdentifyEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.tierOne.XiaoZhiGuFlashAssistIdentifyEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.tierOne.SuiYiGuRandomShiftEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoSustainedAttributeModifierEffect;

import java.util.List;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 一转智道蛊虫效果注册。
 */
public class TierOneZhiDaoRegistry {

    private TierOneZhiDaoRegistry() {}

    public static void registerAll() {
        final String shexinguPassive = "guzhenren:shexingu_passive_mind_slow";
        final String shexinguActive = "guzhenren:shexingu_active_mind_lock";
        final String shexinguCooldownKey =
            "GuzhenrenExtCooldown_shexingu_active_mind_lock";

        final String suiyiguPassive =
            "guzhenren:suiyigu_passive_random_blessing";
        final String suiyiguActive = "guzhenren:suiyigu_active_random_shift";

        final String zhanyiguPassive =
            "guzhenren:zhanyigu_passive_battle_fervor";
        final String zhanyiguActive = "guzhenren:zhanyigu_active_war_cry";
        final String zhanyiguCooldownKey =
            "GuzhenrenExtCooldown_zhanyigu_active_war_cry";

        GuEffectRegistry.register(new XiaoZhiGuEnlightenEffect());
        GuEffectRegistry.register(new XiaoZhiGuFlashAssistIdentifyEffect());
        GuEffectRegistry.register(new XiaoHuiGuFrugalIdentifyEffect());
        GuEffectRegistry.register(new XiaoHuiGuGuidedIdentifyEffect());

        GuEffectRegistry.register(
            new ZhiDaoAttackProcDebuffEffect(
                shexinguPassive,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new ZhiDaoActiveTargetDebuffEffect(
                shexinguActive,
                shexinguCooldownKey,
                List.of(
                    new ZhiDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new ZhiDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.GLOWING,
                        "glowing_duration_ticks",
                        0,
                        "glowing_amplifier",
                        0
                    )
                )
            )
        );

        GuEffectRegistry.register(new SuiYiGuRandomBlessingEffect(suiyiguPassive));
        GuEffectRegistry.register(new SuiYiGuRandomShiftEffect(suiyiguActive));

        GuEffectRegistry.register(
            new ZhiDaoSustainedAttributeModifierEffect(
                zhanyiguPassive,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new ZhiDaoActiveSelfBuffEffect(
                zhanyiguActive,
                zhanyiguCooldownKey,
                List.of(
                    new ZhiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new ZhiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        0,
                        "speed_amplifier",
                        0
                    )
                )
            )
        );
    }
}
