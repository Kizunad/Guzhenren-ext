package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.tierFour;

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
 * 四转力道。
 */
public final class TierFourLiDaoRegistry {
    private TierFourLiDaoRegistry() {}

    public static void registerAll() {
        registerCapsAndDefense();
        registerHeavyOffense();
        registerDashAndAllIn();
    }

    private static void registerCapsAndDefense() {
        // 赤心金尾蛊：提高上限（高转）+ 金尾护体
        GuEffectRegistry.register(
            new LiDaoSustainedVariableCapEffect(
                "guzhenren:chixinjinweigu_passive_redheart_goldtail",
                List.of(
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
                "guzhenren:chixinjinweigu_active_goldtail_guard",
                cooldownKey("guzhenren:chixinjinweigu_active_goldtail_guard"),
                List.of(
                    new LiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );

        // 苦力蛊：硬扛苦功（受击减伤）+ 苦战回气
        GuEffectRegistry.register(
            new LiDaoHurtProcReductionEffect(
                "guzhenren:kuligu_passive_bitter_endure",
                cooldownKey("guzhenren:kuligu_passive_bitter_endure"),
                MobEffects.REGENERATION
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveSelfBuffEffect(
                "guzhenren:kuligu_active_bitter_uplift",
                cooldownKey("guzhenren:kuligu_active_bitter_uplift"),
                List.of(
                    new LiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
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

    private static void registerHeavyOffense() {
        // 大力蛊：持续大力（攻击）+ 范围崩裂
        GuEffectRegistry.register(
            new LiDaoSustainedAttributeModifierEffect(
                "guzhenren:daligu_passive_great_force",
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveAoEBurstEffect(
                "guzhenren:daligu_active_great_quake",
                cooldownKey("guzhenren:daligu_active_great_quake"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerDashAndAllIn() {
        // 横冲直撞蛊：持久冲势（击退抗性）+ 直撞突破
        GuEffectRegistry.register(
            new LiDaoSustainedAttributeModifierEffect(
                "guzhenren:hengchongzhizhuanggu_passive_headlong_momentum",
                Attributes.KNOCKBACK_RESISTANCE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "knockback_resistance"
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveChargeEffect(
                "guzhenren:hengchongzhizhuanggu_active_headlong_breakthrough",
                cooldownKey("guzhenren:hengchongzhizhuanggu_active_headlong_breakthrough")
            )
        );

        // 全力以赴蛊·四转：攻触发压制 + 一击破军
        GuEffectRegistry.register(
            new LiDaoAttackProcDebuffEffect(
                "guzhenren:quanliyifugusizhuan_passive_all_in_4",
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveTargetNukeEffect(
                "guzhenren:quanliyifugusizhuan_active_all_in_blow_4",
                cooldownKey("guzhenren:quanliyifugusizhuan_active_all_in_blow_4"),
                List.of()
            )
        );

        // 自力更生蛊·四转：提高上限（高转）+ 体魄提振
        GuEffectRegistry.register(
            new LiDaoSustainedVariableCapEffect(
                "guzhenren:ziligengshenggusizhuan_passive_self_reliance_4",
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
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveSelfBuffEffect(
                "guzhenren:ziligengshenggusizhuan_active_self_uplift_4",
                cooldownKey("guzhenren:ziligengshenggusizhuan_active_self_uplift_4"),
                List.of(
                    new LiDaoActiveSelfBuffEffect.EffectSpec(
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
