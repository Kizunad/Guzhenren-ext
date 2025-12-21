package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.tierThree;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yuedao.common.YueDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yuedao.common.YueDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yuedao.common.YueDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yuedao.common.YueDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.common.YueDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.common.YueDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.common.YueDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yuedao.common.YueDaoSustainedMobEffectEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class TierThreeYueDaoRegistry {
    private TierThreeYueDaoRegistry() {}

    public static void registerAll() {
        registerFrostMoon();
        registerBloodMoon();
        registerPoisonMoon();
        registerMirageMoon();
        registerGoldenMoon();
        registerScarMoon();
    }

    private static void registerFrostMoon() {
        // 霜霖月蛊：冻结月刃（减速）+ 单体霜斩
        GuEffectRegistry.register(
            new YueDaoAttackProcDebuffEffect(
                "guzhenren:shuang_lin_yue_gu_passive_frost_mark",
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new YueDaoActiveTargetNukeEffect(
                "guzhenren:shuang_lin_yue_gu_active_frost_crescent",
                cooldownKey("guzhenren:shuang_lin_yue_gu_active_frost_crescent"),
                List.of(
                    new YueDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        1
                    )
                )
            )
        );
    }

    private static void registerBloodMoon() {
        // 血月蛊：侵蚀（月蚀/枯败）+ 血月斩击
        GuEffectRegistry.register(
            new YueDaoAttackProcDebuffEffect(
                "guzhenren:xie_yue_gu_passive_blood_moon",
                MobEffects.WITHER
            )
        );
        GuEffectRegistry.register(
            new YueDaoActiveTargetNukeEffect(
                "guzhenren:xie_yue_gu_active_blood_moon_slash",
                cooldownKey("guzhenren:xie_yue_gu_active_blood_moon_slash"),
                List.of(
                    new YueDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.WITHER,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YueDaoActiveTargetNukeEffect.EffectSpec(
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

    private static void registerPoisonMoon() {
        // 月毒蛊：毒月侵染 + 范围毒爆
        GuEffectRegistry.register(
            new YueDaoAttackProcDebuffEffect(
                "guzhenren:yue_du_gu_passive_moon_poison",
                MobEffects.POISON
            )
        );
        GuEffectRegistry.register(
            new YueDaoActiveAoEBurstEffect(
                "guzhenren:yue_du_gu_active_poison_burst",
                cooldownKey("guzhenren:yue_du_gu_active_poison_burst"),
                MobEffects.POISON
            )
        );
    }

    private static void registerMirageMoon() {
        // 幻影月蛊：隐匿（月影）+ 眩目虚弱打击
        GuEffectRegistry.register(
            new YueDaoSustainedMobEffectEffect(
                "guzhenren:huan_ying_yue_gu_passive_moon_mirage",
                MobEffects.INVISIBILITY
            )
        );
        GuEffectRegistry.register(
            new YueDaoActiveTargetNukeEffect(
                "guzhenren:huan_ying_yue_gu_active_blind_mirage",
                cooldownKey("guzhenren:huan_ying_yue_gu_active_blind_mirage"),
                List.of(
                    new YueDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.BLINDNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YueDaoActiveTargetNukeEffect.EffectSpec(
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

    private static void registerGoldenMoon() {
        // 黄金月蛊：持续强势（攻击）+ 自我鼓舞
        GuEffectRegistry.register(
            new YueDaoSustainedAttributeModifierEffect(
                "guzhenren:huang_jin_yue_gu_passive_golden_edge",
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new YueDaoActiveSelfBuffEffect(
                "guzhenren:huang_jin_yue_gu_active_golden_might",
                cooldownKey("guzhenren:huang_jin_yue_gu_active_golden_might"),
                List.of(
                    new YueDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "effect_duration_ticks",
                        0,
                        "damage_boost_amplifier",
                        1
                    ),
                    new YueDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "resistance_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerScarMoon() {
        // 月痕蛊：受击退避 + 影步位移
        GuEffectRegistry.register(
            new YueDaoHurtProcReductionEffect(
                "guzhenren:yue_hen_gu_passive_scar_guard",
                cooldownKey("guzhenren:yue_hen_gu_passive_scar_guard"),
                MobEffects.MOVEMENT_SPEED
            )
        );
        GuEffectRegistry.register(
            new YueDaoActiveBlinkEffect(
                "guzhenren:yue_hen_gu_active_scar_step",
                cooldownKey("guzhenren:yue_hen_gu_active_scar_step"),
                List.of(
                    new YueDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        1
                    )
                )
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
