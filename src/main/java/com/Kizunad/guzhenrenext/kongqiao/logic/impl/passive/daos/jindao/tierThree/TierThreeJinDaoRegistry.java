package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.tierThree;

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
 * 三转金道。
 */
public final class TierThreeJinDaoRegistry {

    private TierThreeJinDaoRegistry() {}

    public static void registerAll() {
        registerIronSkinThree();
        registerCopperSkinThree();
        registerRefinedIron();
        registerGoldenEye();
        registerGoldenLung();
        registerSawToothCentipede();
    }

    private static void registerIronSkinThree() {
        // 铁皮蛊·三转：受击减伤（更稳）+ 铁壁护体
        GuEffectRegistry.register(
            new JinDaoHurtProcReductionEffect(
                "guzhenren:tie_pi_gu_3_passive_iron_wall_3",
                cooldownKey("guzhenren:tie_pi_gu_3_passive_iron_wall_3"),
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveSelfBuffEffect(
                "guzhenren:tie_pi_gu_3_active_iron_bulwark_3",
                cooldownKey("guzhenren:tie_pi_gu_3_active_iron_bulwark_3"),
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

    private static void registerCopperSkinThree() {
        // 三转铜皮蛊：提高上限（高转）+ 铜皮震荡
        GuEffectRegistry.register(
            new JinDaoSustainedVariableCapEffect(
                "guzhenren:tong_pi_gu_san_zhuan_passive_copper_cap_3",
                List.of(
                    new JinDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    ),
                    new JinDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "cap_max_jingli"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveAoEBurstEffect(
                "guzhenren:tong_pi_gu_san_zhuan_active_copper_shock_3",
                cooldownKey("guzhenren:tong_pi_gu_san_zhuan_active_copper_shock_3"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerRefinedIron() {
        // 精铁蛊：持续增伤（攻击）+ 牵引重击
        GuEffectRegistry.register(
            new JinDaoSustainedAttributeModifierEffect(
                "guzhenren:jingtiegu_passive_refined_edge",
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "attack"
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveTargetNukeEffect(
                "guzhenren:jingtiegu_active_refined_pummel",
                cooldownKey("guzhenren:jingtiegu_active_refined_pummel"),
                List.of(
                    new JinDaoActiveTargetNukeEffect.EffectSpec(
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

    private static void registerGoldenEye() {
        // 金睛蛊：攻击触发洞穿（压制）+ 远程锁定
        GuEffectRegistry.register(
            new JinDaoAttackProcDebuffEffect(
                "guzhenren:jin_jing_gu_passive_golden_gaze",
                MobEffects.GLOWING
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveTargetNukeEffect(
                "guzhenren:jin_jing_gu_active_golden_snipe",
                cooldownKey("guzhenren:jin_jing_gu_active_golden_snipe"),
                List.of(
                    new JinDaoActiveTargetNukeEffect.EffectSpec(
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

    private static void registerGoldenLung() {
        // 金肺蛊：持续机动（移速）+ 闪身
        GuEffectRegistry.register(
            new JinDaoSustainedAttributeModifierEffect(
                "guzhenren:jinfeigu_passive_golden_breath",
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
                0.0,
                "move"
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveBlinkEffect(
                "guzhenren:jinfeigu_active_golden_step",
                cooldownKey("guzhenren:jinfeigu_active_golden_step"),
                List.of(
                    new JinDaoActiveBlinkEffect.EffectSpec(
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

    private static void registerSawToothCentipede() {
        // 锯齿金蜈蛊：攻击触发割裂 + 范围撕裂
        GuEffectRegistry.register(
            new JinDaoAttackProcDebuffEffect(
                "guzhenren:ju_chi_jin_wu_gu_passive_sawtooth_rend",
                MobEffects.DIG_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveAoEBurstEffect(
                "guzhenren:ju_chi_jin_wu_gu_active_sawtooth_sweep",
                cooldownKey("guzhenren:ju_chi_jin_wu_gu_active_sawtooth_sweep"),
                MobEffects.WEAKNESS
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

