package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.tierFive;

import com.Kizunad.guzhenrenext.guzhenrenBridge.DaoHenHelper;
import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAllySupportEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveBlinkEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveSwapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.yudao.common.YuDaoActiveTargetDisplaceEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoAttackProcLeechEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedAttributeModifierEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedMobEffectEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedRegenEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.yudao.common.YuDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 五转·宇道 蛊虫效果注册表。
 */
public final class TierFiveYuDaoRegistry {

    private TierFiveYuDaoRegistry() {}

    public static void registerAll() {
        registerYuZengGu();
        registerJieYunGu();
        registerShuoYuGu();
        registerNuoYiGu();
        registerDingKongGu();
        registerYuBengGu();
        registerJieLiGu();
        registerDouKongGu();
        registerYuanLaoGuFive();
        registerWuZhuanYuMaoGu();
    }

    private static void registerYuZengGu() {
        // 宇增蛊：空间增幅（增伤）+ 空间过载（范围崩裂）
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:yuzenggu_passive_space_amplify",
                DaoHenHelper.DaoType.YU_DAO,
                Attributes.ATTACK_DAMAGE,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_damage"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAoEBurstEffect(
                "guzhenren:yuzenggu_active_space_overload",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:yuzenggu_active_space_overload"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerJieYunGu() {
        // 解云蛊：常驻明见 + 净化扶持
        GuEffectRegistry.register(
            new YuDaoSustainedMobEffectEffect(
                "guzhenren:jieyungu_passive_clear_mist",
                DaoHenHelper.DaoType.YU_DAO,
                MobEffects.NIGHT_VISION
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAllySupportEffect(
                "guzhenren:jieyungu_active_purify_mist",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:jieyungu_active_purify_mist"),
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

    private static void registerShuoYuGu() {
        // 说宇蛊：号令（虚弱）+ 空间律令（推斥）
        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                "guzhenren:shuoyugu_passive_spatial_command",
                DaoHenHelper.DaoType.YU_DAO,
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveTargetDisplaceEffect(
                "guzhenren:shuoyugu_active_spatial_order",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:shuoyugu_active_spatial_order"),
                false,
                MobEffects.WEAKNESS
            )
        );
    }

    private static void registerNuoYiGu() {
        // 挪移蛊：常驻脚力 + 长距挪移
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:nuoyigu_passive_moving_space",
                DaoHenHelper.DaoType.YU_DAO,
                Attributes.MOVEMENT_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "speed"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveBlinkEffect(
                "guzhenren:nuoyigu_active_long_blink",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:nuoyigu_active_long_blink"),
                List.of(
                    new YuDaoActiveBlinkEffect.EffectSpec(
                        MobEffects.MOVEMENT_SPEED,
                        "effect_duration_ticks",
                        0,
                        "effect_amplifier",
                        0
                    ),
                    new YuDaoActiveBlinkEffect.EffectSpec(
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

    private static void registerDingKongGu() {
        // 定空蛊：定空（高转字段上限）+ 定空领域（主动）
        GuEffectRegistry.register(
            new YuDaoSustainedVariableCapEffect(
                "guzhenren:dingkonggu_passive_space_fix",
                DaoHenHelper.DaoType.YU_DAO,
                List.of(
                    new YuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    ),
                    new YuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "cap_max_jingli"
                    ),
                    new YuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
                        "cap_max_hunpo"
                    ),
                    new YuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
                        "cap_max_hunpo_resistance"
                    ),
                    new YuDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "cap_niantou_capacity"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAoEBurstEffect(
                "guzhenren:dingkonggu_active_space_lockdown",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:dingkonggu_active_space_lockdown"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerYuBengGu() {
        // 宇崩蛊：坍缩（被动）+ 坍缩（主动）
        GuEffectRegistry.register(
            new YuDaoAttackProcDebuffEffect(
                "guzhenren:yubenggu_passive_space_collapse",
                DaoHenHelper.DaoType.YU_DAO,
                MobEffects.WITHER
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAoEBurstEffect(
                "guzhenren:yubenggu_active_space_collapse",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:yubenggu_active_space_collapse"),
                MobEffects.WITHER
            )
        );
    }

    private static void registerJieLiGu() {
        // 借力蛊（二）：受击借力 + 借力护体
        GuEffectRegistry.register(
            new YuDaoHurtProcReductionEffect(
                "guzhenren:jieligu_2_passive_borrow_force",
                DaoHenHelper.DaoType.YU_DAO,
                MobEffects.DAMAGE_BOOST
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSelfBuffEffect(
                "guzhenren:jieligu_2_active_borrow_force",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:jieligu_2_active_borrow_force"),
                List.of(
                    new YuDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
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

    private static void registerDouKongGu() {
        // 斗空蛊：以战养战 + 斗空崩裂
        GuEffectRegistry.register(
            new YuDaoAttackProcLeechEffect(
                "guzhenren:doukonggu_passive_battle_void",
                DaoHenHelper.DaoType.YU_DAO
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAoEBurstEffect(
                "guzhenren:doukonggu_active_void_burst",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:doukonggu_active_void_burst"),
                MobEffects.WEAKNESS
            )
        );
    }

    private static void registerYuanLaoGuFive() {
        // 元老蛊（五）：稳态扶持（极）
        GuEffectRegistry.register(
            new YuDaoSustainedRegenEffect(
                "guzhenren:yuan_lao_gu_5_passive_elder_nurture",
                DaoHenHelper.DaoType.YU_DAO
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveAllySupportEffect(
                "guzhenren:yuan_lao_gu_5_active_elder_support",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:yuan_lao_gu_5_active_elder_support"),
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

    private static void registerWuZhuanYuMaoGu() {
        // 五转宇猫蛊：猎空常驻 + 换位猎杀
        GuEffectRegistry.register(
            new YuDaoSustainedAttributeModifierEffect(
                "guzhenren:wuzhuanyumaogu_passive_space_hunt",
                DaoHenHelper.DaoType.YU_DAO,
                Attributes.ATTACK_SPEED,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE,
                0.0,
                "attack_speed"
            )
        );
        GuEffectRegistry.register(
            new YuDaoActiveSwapEffect(
                "guzhenren:wuzhuanyumaogu_active_space_hunt",
                DaoHenHelper.DaoType.YU_DAO,
                cooldownKey("guzhenren:wuzhuanyumaogu_active_space_hunt"),
                List.of(
                    new YuDaoActiveSwapEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
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
                    )
                )
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
