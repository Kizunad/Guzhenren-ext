package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.xingdao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSwapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 二转星道：星箭/辉映/星火/星盾。
 */
public final class TierTwoXingDaoRegistry {

    private TierTwoXingDaoRegistry() {}

    public static void registerAll() {
        registerXingJianGu();
        registerLiangXingHuiYingGu();
        registerXingHuoGu();
        registerXinDunGu();
    }

    private static void registerXingJianGu() {
        // 星箭蛊：箭势（攻速）+ 视线锁定打击
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:xing_jian_gu_passive_star_draw",
                Attributes.ATTACK_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_speed"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveTargetNukeEffect(
                "guzhenren:xing_jian_gu_active_star_volley",
                cooldownKey("guzhenren:xing_jian_gu_active_star_volley"),
                List.of(
                    new YuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.GLOWING,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveTargetNukeEffect.EffectSpec(
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

    private static void registerLiangXingHuiYingGu() {
        // 两星辉映蛊：增幅（攻强）+ 换位扰阵
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:liang_xing_hui_ying_gu_passive_twin_star_power",
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSwapEffect(
                "guzhenren:liang_xing_hui_ying_gu_active_twin_swap",
                cooldownKey("guzhenren:liang_xing_hui_ying_gu_active_twin_swap"),
                List.of(
                    new YuDaoActiveSwapEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                ),
                List.of(
                    new YuDaoActiveSwapEffect.EffectSpec(
                        MobEffects.WEAKNESS,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveSwapEffect.EffectSpec(
                        MobEffects.GLOWING,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerXingHuoGu() {
        // 星火蛊：星火灼身（攻击触发）+ 星火爆焰（范围）
        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                "guzhenren:xinghuogu_passive_starfire_burn",
                MobEffects.WITHER
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAoEBurstEffect(
                "guzhenren:xinghuogu_active_starfire_wave",
                cooldownKey("guzhenren:xinghuogu_active_starfire_wave"),
                MobEffects.WITHER
            )
        );
    }

    private static void registerXinDunGu() {
        // 星盾蛊：受击触发减伤 + 护体爆发
        GuEffectRegistry.register(
            new YuDaoHurtProcReductionEffect(
                "guzhenren:xindungu_passive_star_shield",
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:xindungu_active_constellation_guard",
                cooldownKey("guzhenren:xindungu_active_constellation_guard"),
                List.of(
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
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

