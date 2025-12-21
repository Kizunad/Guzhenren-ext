package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.tierOne;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.gudao.common.GuDaoActiveTargetDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.common.DaoSustainedResourceRegenEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.common.GuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.gudao.common.GuDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 一转骨道蛊虫效果注册。
 */
public final class TierOneGuDaoRegistry {

    private TierOneGuDaoRegistry() {}

    public static void registerAll() {
        registerGuZhuGu();
        registerGuQiangGu();
    }

    private static void registerGuZhuGu() {
        final String passive =
            "guzhenren:wei_lian_hua_gu_zhu_gu_passive_bamboo_guard";
        final String active =
            "guzhenren:wei_lian_hua_gu_zhu_gu_active_bamboo_bind";
        final String cooldownKey =
            "GuzhenrenExtCooldown_wei_lian_hua_gu_zhu_gu_active_bamboo_bind";

        GuEffectRegistry.register(
            new GuDaoSustainedAttributeModifierEffect(
                passive,
                Attributes.KNOCKBACK_RESISTANCE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "knockback_resistance"
            )
        );
        GuEffectRegistry.register(
            new GuDaoActiveTargetDebuffEffect(
                active,
                cooldownKey,
                List.of(
                    new GuDaoActiveTargetDebuffEffect.EffectSpec(
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

    private static void registerGuQiangGu() {
        final String passive = "guzhenren:gu_qiang_gu_passive_bone_impale";
        final String regenPassive = "guzhenren:gu_qiang_gu_passive_sui_yang_zhen_yuan";
        final String active = "guzhenren:gu_qiang_gu_active_bone_spear";
        final String cooldownKey =
            "GuzhenrenExtCooldown_gu_qiang_gu_active_bone_spear";

        GuEffectRegistry.register(
            new GuDaoAttackProcDebuffEffect(passive, MobEffects.MOVEMENT_SLOWDOWN)
        );
        GuEffectRegistry.register(
            new DaoSustainedResourceRegenEffect(
                regenPassive,
                DaoHenHelper.DaoType.GU_DAO,
                false,
                false,
                false,
                true
            )
        );
        GuEffectRegistry.register(
            new GuDaoActiveTargetDebuffEffect(
                active,
                cooldownKey,
                List.of(
                    new GuDaoActiveTargetDebuffEffect.EffectSpec(
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
