package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.tierThree;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.rendao.common.RenDaoActiveTargetHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.rendao.common.RenDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 三转人道。
 */
public final class TierThreeRenDaoRegistry {

    private TierThreeRenDaoRegistry() {}

    public static void registerAll() {
        registerBaiYinSheLiGu();
        registerRenShouZangShengGu();
    }

    private static void registerBaiYinSheLiGu() {
        // 白银舍利蛊：三转起允许“拔升上限”——提升真元上限/念头容量，并可稳态恢复魂魄抗性
        GuEffectRegistry.register(
            new RenDaoSustainedVariableCapEffect(
                "guzhenren:bai_yin_she_li_gu_passive_silver_cap_3",
                List.of(
                    new RenDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    ),
                    new RenDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "cap_niantou_capacity"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new RenDaoActiveTargetHealEffect(
                "guzhenren:bai_yin_she_li_gu_active_silver_replenish_3",
                cooldownKey("guzhenren:bai_yin_she_li_gu_active_silver_replenish_3"),
                List.of(
                    new RenDaoActiveTargetHealEffect.EffectSpec(
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

    private static void registerRenShouZangShengGu() {
        // 人兽葬生蛊：以“强行破境”表现——常驻提升攻击 + 主动自疗回元（自用）
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:ren_shou_zang_sheng_gu_passive_breakthrough_force_3",
                DaoHenHelper.DaoType.REN_DAO,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_VALUE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new RenDaoActiveTargetHealEffect(
                "guzhenren:ren_shou_zang_sheng_gu_active_breakthrough_surge_3",
                cooldownKey(
                    "guzhenren:ren_shou_zang_sheng_gu_active_breakthrough_surge_3"
                ),
                List.of(
                    new RenDaoActiveTargetHealEffect.EffectSpec(
                        MobEffects.REGENERATION,
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

