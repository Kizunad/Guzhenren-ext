package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.tierTwo;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jindao.common.JinDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jindao.common.JinDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.jindao.common.JinDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.common.JinDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.jindao.common.JinDaoSustainedAttributeModifierEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 二转金道。
 */
public final class TierTwoJinDaoRegistry {

    private TierTwoJinDaoRegistry() {}

    public static void registerAll() {
        registerIronSkinTwo();
        registerCopperSkinTwo();
        registerGoldenNeedle();
        registerRawIron();
    }

    private static void registerIronSkinTwo() {
        // 铁皮蛊·二转：更高韧性 + 铁肤凝甲
        GuEffectRegistry.register(
            new JinDaoSustainedAttributeModifierEffect(
                "guzhenren:tie_pi_gu_2_passive_iron_hide_2",
                Attributes.ARMOR_TOUGHNESS,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "toughness"
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveSelfBuffEffect(
                "guzhenren:tie_pi_gu_2_active_iron_hide_2",
                cooldownKey("guzhenren:tie_pi_gu_2_active_iron_hide_2"),
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

    private static void registerCopperSkinTwo() {
        // 铜皮蛊·二转：轻盈铜皮（攻防平衡）+ 铜皮震波
        GuEffectRegistry.register(
            new JinDaoSustainedAttributeModifierEffect(
                "guzhenren:tong_pi_gu_2_passive_copper_balance_2",
                Attributes.KNOCKBACK_RESISTANCE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "knockback"
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveAoEBurstEffect(
                "guzhenren:tong_pi_gu_2_active_copper_wave_2",
                cooldownKey("guzhenren:tong_pi_gu_2_active_copper_wave_2"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerGoldenNeedle() {
        // 金针蛊：攻击触发钉刺 + 远程金针击
        GuEffectRegistry.register(
            new JinDaoAttackProcDebuffEffect(
                "guzhenren:jin_zhen_gu_passive_piercing_needle",
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveTargetNukeEffect(
                "guzhenren:jin_zhen_gu_active_needle_shot",
                cooldownKey("guzhenren:jin_zhen_gu_active_needle_shot"),
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

    private static void registerRawIron() {
        // 生铁蛊：持续护甲 + 生铁回气（资源恢复）
        GuEffectRegistry.register(
            new JinDaoSustainedAttributeModifierEffect(
                "guzhenren:shengtiegu_passive_raw_iron_skin",
                Attributes.ARMOR,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "armor"
            )
        );
        GuEffectRegistry.register(
            new JinDaoActiveSelfBuffEffect(
                "guzhenren:shengtiegu_active_raw_iron_refresh",
                cooldownKey("guzhenren:shengtiegu_active_raw_iron_refresh"),
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

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}

