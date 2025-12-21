package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common.MuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common.MuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common.MuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.mudao.common.MuDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.common.MuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.common.MuDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.common.MuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.mudao.common.MuDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 三转木道。
 */
public final class TierThreeMuDaoRegistry {
    private TierThreeMuDaoRegistry() {}

    public static void registerAll() {
        // 天元宝莲：持续生机涌动 + 群体莲息庇护
        GuEffectRegistry.register(
            new MuDaoSustainedRegenEffect(
                "guzhenren:tian_yuan_bao_lian_passive_lotus_aura"
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveAllySupportEffect(
                "guzhenren:tian_yuan_bao_lian_active_lotus_shelter",
                cooldownKey("guzhenren:tian_yuan_bao_lian_active_lotus_shelter"),
                List.of(
                    new MuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new MuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "regen_duration_ticks",
                        0,
                        "regen_amplifier",
                        0
                    )
                )
            )
        );

        // 木肝蛊：体魄增益（生命上限）+ 生机回返
        GuEffectRegistry.register(
            new MuDaoSustainedAttributeModifierEffect(
                "guzhenren:mugangu_passive_wood_liver_endurance",
                Attributes.MAX_HEALTH,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "max_health"
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveSelfBuffEffect(
                "guzhenren:mugangu_active_liver_renewal",
                cooldownKey("guzhenren:mugangu_active_liver_renewal"),
                List.of(
                    new MuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new MuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "boost_duration_ticks",
                        0,
                        "boost_amplifier",
                        0
                    )
                )
            )
        );

        // 天蓬蛊：冲阵范围爆发 + 攻击触发迟滞
        GuEffectRegistry.register(
            new MuDaoAttackProcDebuffEffect(
                "guzhenren:tian_peng_gu_passive_boar_fury",
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveAoEBurstEffect(
                "guzhenren:tian_peng_gu_active_boar_charge",
                cooldownKey("guzhenren:tian_peng_gu_active_boar_charge"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );

        // 铁壳花种蛊：受击护体 + 穿刺打击
        GuEffectRegistry.register(
            new MuDaoHurtProcReductionEffect(
                "guzhenren:tiekehuazhonggu_passive_iron_seed_shell",
                cooldownKey("guzhenren:tiekehuazhonggu_passive_iron_seed_shell"),
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveTargetNukeEffect(
                "guzhenren:tiekehuazhonggu_active_iron_seed_spike",
                cooldownKey("guzhenren:tiekehuazhonggu_active_iron_seed_spike"),
                List.of(
                    new MuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "weakness_duration_ticks",
                        0,
                        "weakness_amplifier",
                        0
                    )
                )
            )
        );

        // 毒松针蛊：毒素附伤 + 范围毒刺爆发
        GuEffectRegistry.register(
            new MuDaoAttackProcDebuffEffect(
                "guzhenren:dusongzhengu_passive_toxic_needles",
                MobEffects.POISON
            )
        );
        GuEffectRegistry.register(
            new MuDaoActiveAoEBurstEffect(
                "guzhenren:dusongzhengu_active_toxic_burst",
                cooldownKey("guzhenren:dusongzhengu_active_toxic_burst"),
                MobEffects.POISON
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

