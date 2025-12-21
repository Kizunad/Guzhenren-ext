package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveChargeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 五转力道。
 */
public final class TierFiveLiDaoRegistry {
    private TierFiveLiDaoRegistry() {}

    public static void registerAll() {
        // 寒月蛊：寒意压制（受击减伤）+ 寒潮震荡
        GuEffectRegistry.register(
            new LiDaoHurtProcReductionEffect(
                "guzhenren:han_yue_gu_passive_cold_moon_guard",
                cooldownKey("guzhenren:han_yue_gu_passive_cold_moon_guard"),
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveAoEBurstEffect(
                "guzhenren:han_yue_gu_active_cold_moon_quake",
                cooldownKey("guzhenren:han_yue_gu_active_cold_moon_quake"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );

        // 虎力蛊：虎力加身（攻击）+ 虎扑突进
        GuEffectRegistry.register(
            new LiDaoSustainedAttributeModifierEffect(
                "guzhenren:hu_li_gu_passive_tiger_force",
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveChargeEffect(
                "guzhenren:hu_li_gu_active_tiger_pounce",
                cooldownKey("guzhenren:hu_li_gu_active_tiger_pounce")
            )
        );

        // 强身健体蛊：提高上限（高转）+ 强身护体
        GuEffectRegistry.register(
            new LiDaoSustainedVariableCapEffect(
                "guzhenren:qiang_shen_jian_ti_gu_passive_body_temper",
                List.of(
                    new LiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    ),
                    new LiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "cap_max_jingli"
                    ),
                    new LiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
                        "cap_max_hunpo"
                    ),
                    new LiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
                        "cap_max_hunpo_resistance"
                    ),
                    new LiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "cap_niantou_capacity"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveSelfBuffEffect(
                "guzhenren:qiang_shen_jian_ti_gu_active_body_temper",
                cooldownKey("guzhenren:qiang_shen_jian_ti_gu_active_body_temper"),
                List.of(
                    new LiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new LiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "regen_duration_ticks",
                        0,
                        "regen_amplifier",
                        0
                    )
                )
            )
        );

        // 铁爪鹰力蛊：攻触发破势 + 断筋重击
        GuEffectRegistry.register(
            new LiDaoAttackProcDebuffEffect(
                "guzhenren:tie_zhua_ying_li_gu_passive_iron_claw_rend",
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveTargetNukeEffect(
                "guzhenren:tie_zhua_ying_li_gu_active_iron_claw_strike",
                cooldownKey("guzhenren:tie_zhua_ying_li_gu_active_iron_claw_strike"),
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
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

