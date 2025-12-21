package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.tierOne;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveChargeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.lidao.common.LiDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoSustainedMobEffectEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.lidao.common.LiDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 一转力道。
 */
public final class TierOneLiDaoRegistry {
    private static final int FOUR_SECONDS_TICKS = 20 * 4;

    private TierOneLiDaoRegistry() {}

    public static void registerAll() {
        registerStones();
        registerBeastStrengths();
        registerJingliAndFlow();
    }

    private static void registerStones() {
        // 白石蛊：硬化护体（受击减伤）+ 护体回气
        GuEffectRegistry.register(
            new LiDaoHurtProcReductionEffect(
                "guzhenren:bai_shi_gu_passive_white_stone_guard",
                cooldownKey("guzhenren:bai_shi_gu_passive_white_stone_guard"),
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveSelfBuffEffect(
                "guzhenren:bai_shi_gu_active_white_stone_shell",
                cooldownKey("guzhenren:bai_shi_gu_active_white_stone_shell"),
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

        // 黑石蛊：负重增势（护甲提升）+ 重击弱化
        GuEffectRegistry.register(
            new LiDaoSustainedAttributeModifierEffect(
                "guzhenren:hei_shi_gu_passive_black_stone_weight",
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor"
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveTargetNukeEffect(
                "guzhenren:hei_shi_gu_active_black_stone_smash",
                cooldownKey("guzhenren:hei_shi_gu_active_black_stone_smash"),
                List.of(
                    new LiDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );

        // 化石蛊：石化迟缓（攻触发）+ 砸地波及
        GuEffectRegistry.register(
            new LiDaoAttackProcDebuffEffect(
                "guzhenren:hua_shi_gu_passive_petrify_chip",
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveAoEBurstEffect(
                "guzhenren:hua_shi_gu_active_fossil_shockwave",
                cooldownKey("guzhenren:hua_shi_gu_active_fossil_shockwave"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerBeastStrengths() {
        // 黄落天牛蛊：蛮冲（突进冲撞）+ 力增
        GuEffectRegistry.register(
            new LiDaoSustainedMobEffectEffect(
                "guzhenren:huang_luo_tian_niu_gu_passive_falling_bull_force",
                MobEffects.DAMAGE_BOOST,
                FOUR_SECONDS_TICKS,
                0
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveChargeEffect(
                "guzhenren:huang_luo_tian_niu_gu_active_falling_bull_charge",
                cooldownKey("guzhenren:huang_luo_tian_niu_gu_active_falling_bull_charge")
            )
        );

        // 蛮力天牛蛊：体魄厚实（生命上限）+ 践踏
        GuEffectRegistry.register(
            new LiDaoSustainedAttributeModifierEffect(
                "guzhenren:man_li_tian_niu_gu_passive_brute_bull_hide",
                Attributes.MAX_HEALTH,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "max_health"
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveAoEBurstEffect(
                "guzhenren:man_li_tian_niu_gu_active_brute_bull_stomp",
                cooldownKey("guzhenren:man_li_tian_niu_gu_active_brute_bull_stomp"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );

        // 熊嚎蛊：战吼援护（范围增益）+ 攻触发压制
        GuEffectRegistry.register(
            new LiDaoAttackProcDebuffEffect(
                "guzhenren:xiong_hao_gu_passive_roar_oppress",
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveAllySupportEffect(
                "guzhenren:xiong_hao_gu_active_battle_roar",
                cooldownKey("guzhenren:xiong_hao_gu_active_battle_roar"),
                List.of(
                    new LiDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new LiDaoActiveAllySupportEffect.EffectSpec(
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

    private static void registerJingliAndFlow() {
        // 精力蛊：精力回流（持续）+ 爆发提振
        GuEffectRegistry.register(
            new LiDaoSustainedRegenEffect(
                "guzhenren:jing_li_gu_passive_jingli_flow"
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveSelfBuffEffect(
                "guzhenren:jing_li_gu_active_jingli_surge",
                cooldownKey("guzhenren:jing_li_gu_active_jingli_surge"),
                List.of(
                    new LiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );

        // 龙弯屈屈蛊：擒拿扭势（攻触发）+ 贴身冲撞
        GuEffectRegistry.register(
            new LiDaoAttackProcDebuffEffect(
                "guzhenren:long_wan_qu_qu_gu_passive_twist_break",
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveChargeEffect(
                "guzhenren:long_wan_qu_qu_gu_active_twist_charge",
                cooldownKey("guzhenren:long_wan_qu_qu_gu_active_twist_charge")
            )
        );

        // 熊力蛊：熊力加身（攻击增幅）+ 单点重击
        GuEffectRegistry.register(
            new LiDaoSustainedAttributeModifierEffect(
                "guzhenren:xiong_li_gu_passive_bear_strength",
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new LiDaoActiveTargetNukeEffect(
                "guzhenren:xiong_li_gu_active_bear_punch",
                cooldownKey("guzhenren:xiong_li_gu_active_bear_punch"),
                List.of()
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
