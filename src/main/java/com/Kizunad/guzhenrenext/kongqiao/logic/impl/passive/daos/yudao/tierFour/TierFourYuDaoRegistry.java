package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveTargetDisplaceEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveTargetNukeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveZhenyuanSurgeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoAttackProcLeechEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedRegenEffect;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 四转·宇道 蛊虫效果注册表。
 */
public final class TierFourYuDaoRegistry {

    private TierFourYuDaoRegistry() {}

    public static void registerAll() {
        registerWuWangGu();
        registerPoKongGu();
        registerYuanQiGu();
        registerYuHeGu();
        registerChiKongGu();
        registerSiZhuanYuMaoGu();
        registerYuGouGu();
        registerYuanLaoGuFour();
    }

    private static void registerWuWangGu() {
        // 无妄蛊：稳固空间（受击减伤）+ 护体
        GuEffectRegistry.register(
            new YuDaoHurtProcReductionEffect(
                "guzhenren:wuwanggu_passive_stabilize",
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:wuwanggu_active_stabilize_field",
                cooldownKey("guzhenren:wuwanggu_active_stabilize_field"),
                List.of(
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
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

    private static void registerPoKongGu() {
        // 破空蛊：虚刃 + 破空打击
        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                "guzhenren:pokonggu_passive_void_edge",
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveTargetNukeEffect(
                "guzhenren:pokonggu_active_void_pierce",
                cooldownKey("guzhenren:pokonggu_active_void_pierce"),
                List.of(
                    new YuDaoActiveTargetNukeEffect.EffectSpec(
                        MobEffects.WEAKNESS,
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

    private static void registerYuanQiGu() {
        // 元气蛊：元气流转（被动）+ 元气涌动（主动转化真元）
        GuEffectRegistry.register(
            new YuDaoSustainedRegenEffect(
                "guzhenren:yuanqigu_passive_yuanqi_flow"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveZhenyuanSurgeEffect(
                "guzhenren:yuanqigu_active_yuanqi_surge",
                cooldownKey("guzhenren:yuanqigu_active_yuanqi_surge")
            )
        );
    }

    private static void registerYuHeGu() {
        // 宇合蛊：和合之气（群体扶持）
        GuEffectRegistry.register(
            new YuDaoSustainedRegenEffect(
                "guzhenren:yuhegu_passive_harmony"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAllySupportEffect(
                "guzhenren:yuhegu_active_harmony_support",
                cooldownKey("guzhenren:yuhegu_active_harmony_support"),
                List.of(
                    new YuDaoActiveAllySupportEffect.EffectSpec(
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

    private static void registerChiKongGu() {
        // 吃空蛊：吞空（汲取）+ 虚空牵引
        GuEffectRegistry.register(
            new YuDaoAttackProcLeechEffect(
                "guzhenren:chikonggu_passive_void_devour"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveTargetDisplaceEffect(
                "guzhenren:chikonggu_active_void_pull",
                cooldownKey("guzhenren:chikonggu_active_void_pull"),
                true,
                MobEffects.WITHER
            )
        );
    }

    private static void registerSiZhuanYuMaoGu() {
        // 四转宇猫蛊：猎步（攻速）+ 猎影挪移
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:sizhuanyumaogu_passive_space_hunt",
                Attributes.ATTACK_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_speed"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveBlinkEffect(
                "guzhenren:sizhuanyumaogu_active_blink_hunt",
                cooldownKey("guzhenren:sizhuanyumaogu_active_blink_hunt"),
                List.of(
                    new YuDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerYuGouGu() {
        // 宇沟蛊：沟壑牵制 + 崩裂
        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                "guzhenren:yugougu_passive_rift_groove",
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAoEBurstEffect(
                "guzhenren:yugougu_active_rift_burst",
                cooldownKey("guzhenren:yugougu_active_rift_burst"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerYuanLaoGuFour() {
        // 元老蛊（四）：稳态扶持（大）
        GuEffectRegistry.register(
            new YuDaoSustainedRegenEffect(
                "guzhenren:yuan_lao_gu_4_passive_elder_nurture"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAllySupportEffect(
                "guzhenren:yuan_lao_gu_4_active_elder_support",
                cooldownKey("guzhenren:yuan_lao_gu_4_active_elder_support"),
                List.of(
                    new YuDaoActiveAllySupportEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveAllySupportEffect.EffectSpec(
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
