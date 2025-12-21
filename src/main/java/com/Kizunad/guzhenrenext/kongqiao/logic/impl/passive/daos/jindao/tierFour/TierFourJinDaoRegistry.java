package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jindao.common.JinDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jindao.common.JinDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jindao.common.JinDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jindao.common.JinDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.common.JinDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.common.JinDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.common.JinDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.common.JinDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 四转金道。
 */
public final class TierFourJinDaoRegistry {

    private TierFourJinDaoRegistry() {}

    public static void registerAll() {
        registerSteelRebar();
        registerAncientBronzeHide();
        registerGold();
        registerGoldBreeze();
        registerRefinedIronHide();
        registerGoldenDragon();
        registerGoldenThread();
        registerGoldenBell();
    }

    private static void registerSteelRebar() {
        // 钢筋蛊：提高上限（高转）+ 钢筋战意
        GuEffectRegistry.register(
            new JinDaoSustainedVariableCapEffect(
                "guzhenren:ganjingu_passive_steel_rebar_cap_4",
                List.of(
                    new JinDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    ),
                    new JinDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "cap_max_jingli"
                    ),
                    new JinDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
                        "cap_max_hunpo_resistance"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveSelfBuffEffect(
                "guzhenren:ganjingu_active_steel_rebar_will_4",
                cooldownKey("guzhenren:ganjingu_active_steel_rebar_will_4"),
                List.of(
                    new JinDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "strength_duration_ticks",
                        0,
                        "strength_amplifier",
                        0
                    ),
                    new JinDaoActiveSelfBuffEffect.EffectSpec(
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

    private static void registerAncientBronzeHide() {
        // 古铜皮蛊：韧性与抗冲击 + 牵引重击
        GuEffectRegistry.register(
            new JinDaoSustainedAttributeModifierEffect(
                "guzhenren:gutongpigu_passive_ancient_bronze_hide_4",
                Attributes.ARMOR_TOUGHNESS,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "toughness"
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveTargetNukeEffect(
                "guzhenren:gutongpigu_active_ancient_grapple_4",
                cooldownKey("guzhenren:gutongpigu_active_ancient_grapple_4"),
                List.of(
                    new JinDaoActiveTargetNukeEffect.EffectSpec(
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

    private static void registerGold() {
        // 黄金蛊：提高上限（高转）+ 黄金滋养
        GuEffectRegistry.register(
            new JinDaoSustainedVariableCapEffect(
                "guzhenren:huangjingu_passive_golden_cap_4",
                List.of(
                    new JinDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "cap_niantou_capacity"
                    ),
                    new JinDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
                        "cap_max_hunpo"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveSelfBuffEffect(
                "guzhenren:huangjingu_active_golden_nourish_4",
                cooldownKey("guzhenren:huangjingu_active_golden_nourish_4"),
                List.of(
                    new JinDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "regen_duration_ticks",
                        0,
                        "regen_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerGoldBreeze() {
        // 金风送爽蛊：持续机动（移速）+ 金风步
        GuEffectRegistry.register(
            new JinDaoSustainedAttributeModifierEffect(
                "guzhenren:jinfengsongshuanggu_passive_gold_breeze_4",
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                0.0,
                "move"
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveBlinkEffect(
                "guzhenren:jinfengsongshuanggu_active_gold_breeze_step_4",
                cooldownKey("guzhenren:jinfengsongshuanggu_active_gold_breeze_step_4"),
                List.of(
                    new JinDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        0,
                        "speed_amplifier",
                        0
                    ),
                    new JinDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "strength_duration_ticks",
                        0,
                        "strength_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerRefinedIronHide() {
        // 精铁皮蛊：受击减伤（偏护体）+ 精铁震击
        GuEffectRegistry.register(
            new JinDaoHurtProcReductionEffect(
                "guzhenren:jingtiepigu_passive_refined_hide_4",
                cooldownKey("guzhenren:jingtiepigu_passive_refined_hide_4"),
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveAoEBurstEffect(
                "guzhenren:jingtiepigu_active_refined_shock_4",
                cooldownKey("guzhenren:jingtiepigu_active_refined_shock_4"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerGoldenDragon() {
        // 金龙蛊：攻击触发龙鳞碎 + 金龙重击
        GuEffectRegistry.register(
            new JinDaoAttackProcDebuffEffect(
                "guzhenren:jinlonggu_passive_dragon_rend_4",
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveTargetNukeEffect(
                "guzhenren:jinlonggu_active_dragon_strike_4",
                cooldownKey("guzhenren:jinlonggu_active_dragon_strike_4"),
                List.of(
                    new JinDaoActiveTargetNukeEffect.EffectSpec(
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

    private static void registerGoldenThread() {
        // 金缕衣蛊：投射物减伤 + 金缕护身
        GuEffectRegistry.register(
            new JinDaoHurtProcReductionEffect(
                "guzhenren:jinlvyigu_passive_golden_thread_guard_4",
                cooldownKey("guzhenren:jinlvyigu_passive_golden_thread_guard_4"),
                MobEffects.MOVEMENT_SPEED
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveSelfBuffEffect(
                "guzhenren:jinlvyigu_active_golden_thread_shroud_4",
                cooldownKey("guzhenren:jinlvyigu_active_golden_thread_shroud_4"),
                List.of(
                    new JinDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "speed_duration_ticks",
                        0,
                        "speed_amplifier",
                        0
                    ),
                    new JinDaoActiveSelfBuffEffect.EffectSpec(
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

    private static void registerGoldenBell() {
        // 金钟蛊：持续护甲（高）+ 金钟护体
        GuEffectRegistry.register(
            new JinDaoSustainedAttributeModifierEffect(
                "guzhenren:jinzhonggu_passive_golden_bell_4",
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor"
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveSelfBuffEffect(
                "guzhenren:jinzhonggu_active_golden_bell_guard_4",
                cooldownKey("guzhenren:jinzhonggu_active_golden_bell_guard_4"),
                List.of(
                    new JinDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "resist_duration_ticks",
                        0,
                        "resist_amplifier",
                        0
                    ),
                    new JinDaoActiveSelfBuffEffect.EffectSpec(
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

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

