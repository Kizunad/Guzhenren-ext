package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.tierFive;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveAreaFieldEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveAreaHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoLowHealthLastStandEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoSustainedAreaHealEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 五转水道。
 */
public final class TierFiveShuiDaoRegistry {

    private static final int MEDIUM_BUFF_TICKS = 160;
    private static final int LONG_BUFF_TICKS = 200;
    private static final int VERY_LONG_BUFF_TICKS = 260;
    private static final int SUPER_LONG_BUFF_TICKS = 300;

    private TierFiveShuiDaoRegistry() {}

    public static void registerAll() {
        registerShuiHuGu5();
        registerJiShuiGu();
        registerJiTuGu();
        registerShuiJiaGu5();
        registerBeiShuiYiZhanGu();
        registerYanLeiGu();
    }

    private static void registerShuiHuGu5() {
        // 水护蛊（五转）：深水上限（持续上限）+ 深水护体（主动护体/恢复）
        GuEffectRegistry.register(
            new ShuiDaoSustainedVariableCapEffect(
                "guzhenren:shui_hu_gu_5_passive_abyssal_guard",
                List.of(
                    new ShuiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    ),
                    new ShuiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "cap_max_jingli"
                    ),
                    new ShuiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO,
                        "cap_max_hunpo"
                    ),
                    new ShuiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_HUNPO_RESISTANCE,
                        "cap_hunpo_resistance"
                    ),
                    new ShuiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "cap_niantou_capacity"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveSelfBuffEffect(
                "guzhenren:shui_hu_gu_5_active_abyssal_guard",
                cooldownKey("guzhenren:shui_hu_gu_5_active_abyssal_guard"),
                List.of(
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "buff_duration_ticks",
                        VERY_LONG_BUFF_TICKS,
                        "buff_amplifier",
                        2
                    ),
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "buff_duration_ticks",
                        VERY_LONG_BUFF_TICKS,
                        "buff_amplifier",
                        1
                    ),
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "buff_duration_ticks",
                        MEDIUM_BUFF_TICKS,
                        "buff_amplifier",
                        1
                    )
                )
            )
        );
    }

    private static void registerJiShuiGu() {
        // 积水蛊：积水润泽（持续群体回复）+ 积水倾覆（范围潮击）
        GuEffectRegistry.register(
            new ShuiDaoSustainedAreaHealEffect(
                "guzhenren:ji_shui_gu_passive_accumulated_spring"
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveAoEBurstEffect(
                "guzhenren:ji_shui_gu_active_accumulated_deluge",
                cooldownKey("guzhenren:ji_shui_gu_active_accumulated_deluge"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerJiTuGu() {
        // 积土蛊：泥沼牵制（攻触发）+ 泥沼领域（领域）
        GuEffectRegistry.register(
            new ShuiDaoAttackProcDebuffEffect(
                "guzhenren:ji_tu_gu_passive_mire_bind",
                List.of(MobEffects.MOVEMENT_SLOWDOWN, MobEffects.DIG_SLOWDOWN)
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveAreaFieldEffect(
                "guzhenren:ji_tu_gu_active_mire_field",
                cooldownKey("guzhenren:ji_tu_gu_active_mire_field")
            )
        );
    }

    private static void registerShuiJiaGu5() {
        // 水甲蛊（五转）：潮甲上限（持续上限）+ 潮甲凝护（主动护体）
        GuEffectRegistry.register(
            new ShuiDaoSustainedVariableCapEffect(
                "guzhenren:shui_jia_gu_5_passive_armor_tide",
                List.of(
                    new ShuiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_ZHENYUAN,
                        "cap_max_zhenyuan"
                    ),
                    new ShuiDaoSustainedVariableCapEffect.CapSpec(
                        GuzhenrenVariableModifierService.VAR_MAX_JINGLI,
                        "cap_max_jingli"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveSelfBuffEffect(
                "guzhenren:shui_jia_gu_5_active_armor_tide",
                cooldownKey("guzhenren:shui_jia_gu_5_active_armor_tide"),
                List.of(
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "buff_duration_ticks",
                        SUPER_LONG_BUFF_TICKS,
                        "buff_amplifier",
                        2
                    ),
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "buff_duration_ticks",
                        SUPER_LONG_BUFF_TICKS,
                        "buff_amplifier",
                        1
                    )
                )
            )
        );
    }

    private static void registerBeiShuiYiZhanGu() {
        // 背水一战蛊：背水保命（低血量减伤）+ 背水一战（主动爆发）
        GuEffectRegistry.register(
            new ShuiDaoLowHealthLastStandEffect(
                "guzhenren:bei_shui_yi_zhan_gu_passive_last_stand",
                cooldownKey("guzhenren:bei_shui_yi_zhan_gu_passive_last_stand"),
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveSelfBuffEffect(
                "guzhenren:bei_shui_yi_zhan_gu_active_last_stand",
                cooldownKey("guzhenren:bei_shui_yi_zhan_gu_active_last_stand"),
                List.of(
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "buff_duration_ticks",
                        LONG_BUFF_TICKS,
                        "buff_amplifier",
                        2
                    ),
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "buff_duration_ticks",
                        LONG_BUFF_TICKS,
                        "buff_amplifier",
                        1
                    )
                )
            )
        );
    }

    private static void registerYanLeiGu() {
        // 眼泪蛊：泪泉回润（持续回复）+ 泪泉回春（范围治疗/净化）
        GuEffectRegistry.register(
            new ShuiDaoSustainedAreaHealEffect(
                "guzhenren:yan_lei_gu_passive_tears_spring"
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveAreaHealEffect(
                "guzhenren:yan_lei_gu_active_tears_purify",
                cooldownKey("guzhenren:yan_lei_gu_active_tears_purify"),
                List.of(
                    new ShuiDaoActiveAreaHealEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "buff_duration_ticks",
                        MEDIUM_BUFF_TICKS,
                        "buff_amplifier",
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
