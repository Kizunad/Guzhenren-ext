package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveCultivationBoostEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.zhidao.common.ZhiDaoActiveTargetDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoHurtProcDebuffAttackerEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.zhidao.common.ZhiDaoSustainedRegenEffect;

import java.util.List;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 三转智道蛊虫效果注册。
 */
public class TierThreeZhiDaoRegistry {

    private TierThreeZhiDaoRegistry() {}

    public static void registerAll() {
        registerZhiLuGu();
        registerENianGu();
        registerSanZhuanSheXinGu();
        registerSanZhuanZhanYiGu();
        registerJiZhiGu();
        registerErZhanSuiYiGu();
    }

    private static void registerZhiLuGu() {
        final String zhiluguPassive =
            "guzhenren:zhi_lu_gu_passive_wisdom_reserve";
        final String zhiluguActive =
            "guzhenren:zhi_lu_gu_active_insight_cultivation";
        final String zhiluguCooldownKey =
            "GuzhenrenExtCooldown_zhi_lu_gu_active_insight_cultivation";

        GuEffectRegistry.register(new ZhiDaoSustainedRegenEffect(zhiluguPassive));
        GuEffectRegistry.register(
            new ZhiDaoActiveCultivationBoostEffect(
                zhiluguActive,
                zhiluguCooldownKey
            )
        );
    }

    private static void registerENianGu() {
        final String enianguPassive =
            "guzhenren:e_nian_gu_passive_evil_wither";
        final String enianguActive =
            "guzhenren:e_nian_gu_active_evil_burst";
        final String enianguCooldownKey =
            "GuzhenrenExtCooldown_e_nian_gu_active_evil_burst";

        GuEffectRegistry.register(
            new ZhiDaoAttackProcDebuffEffect(enianguPassive, MobEffects.WITHER)
        );
        GuEffectRegistry.register(
            new ZhiDaoActiveAoEBurstEffect(
                enianguActive,
                enianguCooldownKey,
                List.of(
                    new ZhiDaoActiveAoEBurstEffect.EffectSpec(
                        MobEffects.WITHER,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerSanZhuanSheXinGu() {
        final String passive =
            "guzhenren:sanzhuanshexinggu_passive_mind_fray";
        final String active =
            "guzhenren:sanzhuanshexinggu_active_mind_shatter";
        final String cooldownKey =
            "GuzhenrenExtCooldown_sanzhuanshexinggu_active_mind_shatter";

        GuEffectRegistry.register(
            new ZhiDaoAttackProcDebuffEffect(passive, MobEffects.WEAKNESS)
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

    private static void registerSanZhuanZhanYiGu() {
        final String passive =
            "guzhenren:sanzhuanzhanyigu_passive_iron_will";
        final String active =
            "guzhenren:sanzhuanzhanyigu_active_war_banner";
        final String cooldownKey =
            "GuzhenrenExtCooldown_sanzhuanzhanyigu_active_war_banner";

        GuEffectRegistry.register(
            new ZhiDaoSustainedAttributeModifierEffect(
                passive,
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor"
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
                        MobEffects.ABSORPTION,
                        "absorption_duration_ticks",
                        0,
                        "absorption_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerJiZhiGu() {
        final String passive = "guzhenren:jizhigu_passive_clear_mind";
        final String active = "guzhenren:jizhigu_active_flaw_insight";
        final String cooldownKey =
            "GuzhenrenExtCooldown_jizhigu_active_flaw_insight";

        GuEffectRegistry.register(new ZhiDaoSustainedRegenEffect(passive));
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
    }

    private static void registerErZhanSuiYiGu() {
        final String passive =
            "guzhenren:erzhansuijigu_passive_adapt_guard";
        final String active = "guzhenren:erzhansuijigu_active_adapt_shift";
        final String cooldownKey =
            "GuzhenrenExtCooldown_erzhansuijigu_active_adapt_shift";

        GuEffectRegistry.register(
            new ZhiDaoHurtProcDebuffAttackerEffect(
                passive,
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new ZhiDaoActiveSelfBuffEffect(
                active,
                cooldownKey,
                List.of(
                    new ZhiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
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
}
