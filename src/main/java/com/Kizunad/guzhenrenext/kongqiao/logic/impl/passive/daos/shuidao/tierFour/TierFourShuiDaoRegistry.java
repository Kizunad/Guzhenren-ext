package com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.tierFour;

import com.Kizunad.guzhenrenext.kongqiao.logic.GuEffectRegistry;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveAoEBurstEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveAreaFieldEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveRevealPulseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.active.daos.shuidao.common.ShuiDaoActiveTargetStrikeEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoAttackProcDebuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoHurtProcReductionEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoHurtProcRetaliationEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoSustainedCleanseEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoSustainedSelfBuffEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.impl.passive.daos.shuidao.common.ShuiDaoSustainedVariableCapEffect;
import com.Kizunad.guzhenrenext.kongqiao.logic.util.GuzhenrenVariableModifierService;
import java.util.List;
import net.minecraft.world.effect.MobEffects;

/**
 * 四转水道。
 */
public final class TierFourShuiDaoRegistry {

    private static final int SHORT_BUFF_TICKS = 120;
    private static final int MEDIUM_BUFF_TICKS = 160;
    private static final int LONG_BUFF_TICKS = 200;
    private static final int VERY_LONG_BUFF_TICKS = 220;

    private TierFourShuiDaoRegistry() {}

    public static void registerAll() {
        registerJieZeGu();
        registerShuiPuGu();
        registerShuiLongGu4();
        registerShiSuoGu();
        registerShuiJiaGu4();
        registerQuanYongMingGu();
        registerShuiHuGu();
        registerFanShuiGu();
        registerShiShuiGu();
        registerZhongShuiGu();
    }

    private static void registerJieZeGu() {
        // 洁泽蛊：洁泽净化（持续净化）+ 洁泽回润（主动净化/恢复）
        GuEffectRegistry.register(
            new ShuiDaoSustainedCleanseEffect(
                "guzhenren:jiezegu_passive_clean_spring"
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveSelfBuffEffect(
                "guzhenren:jiezegu_active_clean_spring",
                cooldownKey("guzhenren:jiezegu_active_clean_spring"),
                List.of(
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.REGENERATION,
                        "buff_duration_ticks",
                        MEDIUM_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerShuiPuGu() {
        // 水瀑蛊：瀑流余势（持续维持）+ 水瀑崩击（范围潮击）
        GuEffectRegistry.register(
            new ShuiDaoSustainedSelfBuffEffect(
                "guzhenren:shuipugu_passive_waterfall_aura",
                List.of(
                    new ShuiDaoSustainedSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "buff_duration_ticks",
                        SHORT_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    ),
                    new ShuiDaoSustainedSelfBuffEffect.EffectSpec(
                        MobEffects.WATER_BREATHING,
                        "buff_duration_ticks",
                        VERY_LONG_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveAoEBurstEffect(
                "guzhenren:shuipugu_active_waterfall_crash",
                cooldownKey("guzhenren:shuipugu_active_waterfall_crash"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerShuiLongGu4() {
        // 水龙蛊（四转）：龙潮余威（攻触发）+ 龙潮冲击（指向+溅射）
        GuEffectRegistry.register(
            new ShuiDaoAttackProcDebuffEffect(
                "guzhenren:shui_long_gu_4_passive_dragon_surge",
                List.of(MobEffects.WEAKNESS)
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveTargetStrikeEffect(
                "guzhenren:shui_long_gu_4_active_dragon_surge",
                cooldownKey("guzhenren:shui_long_gu_4_active_dragon_surge"),
                List.of(
                    new ShuiDaoActiveTargetStrikeEffect.EffectSpec(
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

    private static void registerShiSuoGu() {
        // 石锁蛊：石锁牵制（攻触发）+ 石锁泥沼（领域）
        GuEffectRegistry.register(
            new ShuiDaoAttackProcDebuffEffect(
                "guzhenren:shi_suo_gu_passive_stone_lock",
                List.of(MobEffects.MOVEMENT_SLOWDOWN, MobEffects.DIG_SLOWDOWN)
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveAreaFieldEffect(
                "guzhenren:shi_suo_gu_active_stone_lock",
                cooldownKey("guzhenren:shi_suo_gu_active_stone_lock")
            )
        );
    }

    private static void registerShuiJiaGu4() {
        // 水甲蛊（四转）：水甲上限（持续上限）+ 水甲凝护（自 buff）
        GuEffectRegistry.register(
            new ShuiDaoSustainedVariableCapEffect(
                "guzhenren:shui_jia_gu_4_passive_armor_flow",
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
                        GuzhenrenVariableModifierService.VAR_NIANTOU_CAPACITY,
                        "cap_niantou_capacity"
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveSelfBuffEffect(
                "guzhenren:shui_jia_gu_4_active_armor_flow",
                cooldownKey("guzhenren:shui_jia_gu_4_active_armor_flow"),
                List.of(
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "buff_duration_ticks",
                        LONG_BUFF_TICKS,
                        "buff_amplifier",
                        1
                    ),
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "buff_duration_ticks",
                        LONG_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerQuanYongMingGu() {
        // 权用命蛊：以命卸力（受击减伤）+ 借命强身（主动爆发）
        GuEffectRegistry.register(
            new ShuiDaoHurtProcReductionEffect(
                "guzhenren:quan_yong_ming_gu_passive_life_exchange",
                cooldownKey("guzhenren:quan_yong_ming_gu_passive_life_exchange"),
                MobEffects.DAMAGE_BOOST
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveSelfBuffEffect(
                "guzhenren:quan_yong_ming_gu_active_life_exchange",
                cooldownKey("guzhenren:quan_yong_ming_gu_active_life_exchange"),
                List.of(
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_BOOST,
                        "buff_duration_ticks",
                        MEDIUM_BUFF_TICKS,
                        "buff_amplifier",
                        1
                    ),
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "buff_duration_ticks",
                        MEDIUM_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerShuiHuGu() {
        // 水护蛊：水护卸力（受击减伤）+ 水护回潮（主动护体/恢复）
        GuEffectRegistry.register(
            new ShuiDaoHurtProcReductionEffect(
                "guzhenren:shui_hu_gu_passive_protection_current",
                cooldownKey("guzhenren:shui_hu_gu_passive_protection_current"),
                MobEffects.DAMAGE_RESISTANCE
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveSelfBuffEffect(
                "guzhenren:shui_hu_gu_active_protection_current",
                cooldownKey("guzhenren:shui_hu_gu_active_protection_current"),
                List.of(
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.DAMAGE_RESISTANCE,
                        "buff_duration_ticks",
                        VERY_LONG_BUFF_TICKS,
                        "buff_amplifier",
                        1
                    ),
                    new ShuiDaoActiveSelfBuffEffect.EffectSpec(
                        MobEffects.ABSORPTION,
                        "buff_duration_ticks",
                        VERY_LONG_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    )
                )
            )
        );
    }

    private static void registerFanShuiGu() {
        // 反水蛊：逆潮反制（受击反制）+ 逆潮回卷（范围潮击）
        GuEffectRegistry.register(
            new ShuiDaoHurtProcRetaliationEffect(
                "guzhenren:fan_shui_gu_passive_reverse_tide",
                cooldownKey("guzhenren:fan_shui_gu_passive_reverse_tide"),
                MobEffects.WEAKNESS
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveAoEBurstEffect(
                "guzhenren:fan_shui_gu_active_reverse_tide",
                cooldownKey("guzhenren:fan_shui_gu_active_reverse_tide"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static void registerShiShuiGu() {
        // 识水蛊：识水明察（持续维持）+ 识水照映（显形）
        GuEffectRegistry.register(
            new ShuiDaoSustainedSelfBuffEffect(
                "guzhenren:shi_shui_gu_passive_water_sense",
                List.of(
                    new ShuiDaoSustainedSelfBuffEffect.EffectSpec(
                        MobEffects.NIGHT_VISION,
                        "buff_duration_ticks",
                        VERY_LONG_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    ),
                    new ShuiDaoSustainedSelfBuffEffect.EffectSpec(
                        MobEffects.WATER_BREATHING,
                        "buff_duration_ticks",
                        VERY_LONG_BUFF_TICKS,
                        "buff_amplifier",
                        0
                    )
                )
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveRevealPulseEffect(
                "guzhenren:shi_shui_gu_active_water_sense",
                cooldownKey("guzhenren:shi_shui_gu_active_water_sense")
            )
        );
    }

    private static void registerZhongShuiGu() {
        // 重水蛊：重水压制（攻触发）+ 重水浪击（范围潮击）
        GuEffectRegistry.register(
            new ShuiDaoAttackProcDebuffEffect(
                "guzhenren:zhong_shui_gu_passive_heavy_water",
                List.of(MobEffects.MOVEMENT_SLOWDOWN, MobEffects.WEAKNESS)
            )
        );
        GuEffectRegistry.register(
            new ShuiDaoActiveAoEBurstEffect(
                "guzhenren:zhong_shui_gu_active_heavy_water",
                cooldownKey("guzhenren:zhong_shui_gu_active_heavy_water"),
                MobEffects.MOVEMENT_SLOWDOWN
            )
        );
    }

    private static String cooldownKey(final String usageId) {
        return "GuzhenrenExtCooldown_" + usageId;
    }
}
