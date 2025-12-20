package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveCultivationBoostEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveTargetDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoSustainedRegenEffect;

import java.util.List;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 四转智道蛊虫效果注册。
 */
public class TierFourZhiDaoRegistry {

    private TierFourZhiDaoRegistry() {}

    public static void registerAll() {
        registerZhiLuGu();
        registerSheXinGu();
        registerZhanYiGu();
        registerENianGu();
        registerBaoNaoGu();
    }

    private static void registerZhiLuGu() {
        final String passive =
            "guzhenren:zhi_lu_gu_4_passive_wisdom_reserve";
        final String active =
            "guzhenren:zhi_lu_gu_4_active_insight_cultivation";
        final String cooldownKey =
            "GuzhenrenExtCooldown_zhi_lu_gu_4_active_insight_cultivation";

        GuEffectRegistry.register(new ZhiDaoSustainedRegenEffect(passive));
        GuEffectRegistry.register(
            new ZhiDaoActiveCultivationBoostEffect(active, cooldownKey)
        );
    }

    private static void registerSheXinGu() {
        final String passive =
            "guzhenren:shexingusizhuan_passive_mind_pressure";
        final String active =
            "guzhenren:shexingusizhuan_active_mind_domain";
        final String cooldownKey =
            "GuzhenrenExtCooldown_shexingusizhuan_active_mind_domain";

        GuEffectRegistry.register(
            new ZhiDaoAttackProcDebuffEffect(passive, MobEffects.WEAKNESS)
        );
        GuEffectRegistry.register(
            new ZhiDaoActiveAoEBurstEffect(
                active,
                cooldownKey,
                List.of(
                    new ZhiDaoActiveAoEBurstEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new ZhiDaoActiveAoEBurstEffect.EffectSpec(
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

    private static void registerZhanYiGu() {
        final String passive =
            "guzhenren:sizhuanzhanyigu_passive_battle_rage";
        final String active =
            "guzhenren:sizhuanzhanyigu_active_battle_breakthrough";
        final String cooldownKey =
            "GuzhenrenExtCooldown_sizhuanzhanyigu_active_battle_breakthrough";

        GuEffectRegistry.register(
            new ZhiDaoSustainedAttributeModifierEffect(
                passive,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new ZhiDaoActiveSelfBuffEffect(
                active,
                cooldownKey,
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
    }

    private static void registerENianGu() {
        final String passive =
            "guzhenren:sizhuaneniangu_passive_evil_wither";
        final String active = "guzhenren:sizhuaneniangu_active_evil_curse";
        final String cooldownKey =
            "GuzhenrenExtCooldown_sizhuaneniangu_active_evil_curse";

        GuEffectRegistry.register(
            new ZhiDaoAttackProcDebuffEffect(passive, MobEffects.WITHER)
        );
        GuEffectRegistry.register(
            new ZhiDaoActiveTargetDebuffEffect(
                active,
                cooldownKey,
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
    }

    private static void registerBaoNaoGu() {
        final String passive = "guzhenren:baonaogu_passive_brain_shock";
        final String active = "guzhenren:baonaogu_active_brain_burst";
        final String cooldownKey =
            "GuzhenrenExtCooldown_baonaogu_active_brain_burst";

        GuEffectRegistry.register(
            new ZhiDaoAttackProcDebuffEffect(
                passive,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new ZhiDaoActiveTargetDebuffEffect(
                active,
                cooldownKey,
                List.of(
                    new ZhiDaoActiveTargetDebuffEffect.EffectSpec(
                        MobEffects.WEAKNESS,
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
